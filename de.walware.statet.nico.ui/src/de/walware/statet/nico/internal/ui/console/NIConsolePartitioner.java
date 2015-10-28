/*=============================================================================#
 # Copyright (c) 2000-2015 IBM Corporation and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 *
 # Contributors:
 #     IBM Corporation - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.progress.WorkbenchJob;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleOutputStream;


/**
 * Partitioner for a NIConsole's document.
 */
public class NIConsolePartitioner implements IConsoleDocumentPartitioner, IDocumentPartitionerExtension {
	
	
	/**
	 * Holds data until updateJob can be run and the document can be updated.
	 */
	final class PendingPartition {
		
		
		private final NIConsoleOutputStream stream;
		
		private final StringBuilder text;
		
		
		PendingPartition(final NIConsoleOutputStream stream, final String text) {
			this.stream= stream;
			this.text= new StringBuilder(Math.max(4, 2 + (text.length()/1014)) * 1024);
			append(text);
		}
		
		
		public NIConsoleOutputStream getStream() {
			return this.stream;
		}
		
		public StringBuilder getText() {
			return this.text;
		}
		
		private void append(final String text) {
			this.text.append(text);
			NIConsolePartitioner.this.pendingTextLength+= text.length();
		}
		
	}
	
	
	private final NIConsole console;
	
	private final String[] partitionIds;
	
	private AbstractDocument document;
	
	private boolean isConnected= false;
	
	private final ArrayList<NIConsolePartition> partitions= new ArrayList<>();
	
	/**
	 * The last partition appended to the document
	 */
	private NIConsolePartition lastPartition;
	
	/**
	 * Blocks of data that have not yet been appended to the document.
	 */
	private final ArrayList<PendingPartition> pendingPartitions= new ArrayList<>();
	
	private int pendingTextLength; 
	
	/**
	 * A list of PendingPartitions to be appended by the updateJob
	 */
	private ImList<PendingPartition> updatePartitions;
	
	private final QueueProcessingJob queueJob= new QueueProcessingJob();
	
	private final TrimJob trimJob= new TrimJob();
	
	/**
	 * Flag to indicate that the updateJob is updating the document.
	 */
	private boolean updateInProgress;
	/**
	 * offset used by updateJob
	 */
	private final StreamProcessor streamProcessor;
	
	
	/**
	 * An array of legal line delimiters
	 */
	private int highWaterMark= -1;
	private int lowWaterMark= -1;
	
	/**
	 * Lock for appending to and removing from the document - used
	 * to synchronize addition of new text/partitions in the update
	 * job and handling buffer overflow/clearing of the console. 
	 */
	private final Object overflowLock= new Object();
	
	
	public NIConsolePartitioner(final NIConsole console, final List<String> ids) {
		this.console= console;
		this.streamProcessor= new StreamProcessor(this);
		this.partitionIds= ids.toArray(new String[ids.size()]);
		this.trimJob.setRule(console.getSchedulingRule());
		
		this.queueJob.setRule(this.console.getSchedulingRule());
	}
	
	
	public NIConsole getConsole() {
		return this.console;
	}
	
	public AbstractDocument getDocument() {
		return this.document;
	}
	
	NIConsolePartition getLastPartition() {
		return this.lastPartition;
	}
	
	
	@Override
	public void connect(final IDocument doc) {
		this.document= (AbstractDocument) doc;
		this.document.setDocumentPartitioner(this);
		
		this.isConnected= true;
	}
	
	public int getHighWaterMark() {
		return this.highWaterMark;
	}
	
	public int getLowWaterMark() {
		return this.lowWaterMark;
	}
	
	public void setWaterMarks(final int low, final int high) {
		this.lowWaterMark= low;
		this.highWaterMark= high;
		ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				checkBufferSize();
			}
		});
	}
	
	/**
	 * Notification from the console that all of its streams have been closed.
	 */
	public void finish() {
		synchronized (this.pendingPartitions) {
			this.pendingPartitions.add(null);
		}
		this.queueJob.schedule(); //ensure that all pending partitions are processed.
	}
	
	@Override
	public void disconnect() {
		synchronized (this.overflowLock) {
			this.isConnected= false;
			this.document= null;
			this.partitions.clear();
		}
	}
	
	@Override
	public void documentAboutToBeChanged(final DocumentEvent event) {
	}
	
	@Override
	public boolean documentChanged(final DocumentEvent event) {
		return documentChanged2(event) != null;
	}
	
	@Override
	public String[] getLegalContentTypes() {
		return this.partitionIds;
	}
	
	@Override
	public String getContentType(final int offset) {
		return getPartition(offset).getType();
	}
	
	@Override
	public ITypedRegion[] computePartitioning(final int offset, final int length) {
		final int rangeEnd= offset + length;
		int left= 0;
		int right= this.partitions.size() - 1;
		int mid= 0;
		NIConsolePartition position= null;
		
		if (right <= 0) {
			if (right == 0) {
				return new NIConsolePartition[] { this.partitions.get(0) };
			}
			return new NIConsolePartition[0];
		}
		while (left < right) {
			mid= (left + right) / 2;
			position= this.partitions.get(mid);
			if (rangeEnd < position.getOffset()) {
				if (left == mid) {
					right= left;
				} else {
					right= mid -1;
				}
			}
			else if (offset > (position.getOffset() + position.getLength() - 1)) {
				if (right == mid) {
					left= right;
				} else {
					left= mid  +1;
				}
			}
			else {
				left= right= mid;
			}
		}
		
		final List<NIConsolePartition> list= new ArrayList<>();
		int index= left - 1;
		if (index >= 0) {
			position= this.partitions.get(index);
			while (index >= 0 && (position.getOffset() + position.getLength()) > offset) {
				index--;
				if (index >= 0) {
					position= this.partitions.get(index);
				}
			}
		}
		index++;
		position= this.partitions.get(index);
		while (index < this.partitions.size() && (position.getOffset() < rangeEnd)) {
			list.add(position);
			index++;
			if (index < this.partitions.size()) {
				position= this.partitions.get(index);
			}
		}
		
		return list.toArray(new NIConsolePartition[list.size()]);
	}
	
	@Override
	public ITypedRegion getPartition(final int offset) {
		for (int i= 0; i < this.partitions.size(); i++) {
			final ITypedRegion partition= this.partitions.get(i);
			final int start= partition.getOffset();
			final int end= start + partition.getLength();
			if (offset >= start && offset < end) {
				return partition;
			}
		}
		
		return (this.lastPartition != null) ? 
				this.lastPartition : new NIConsolePartition(this.partitionIds[0], null);
	}
	
	/**
	 * Enforces the buffer size.
	 * When the number of lines in the document exceeds the high water mark, the 
	 * beginning of the document is trimmed until the number of lines equals the 
	 * low water mark.
	 */
	private void checkBufferSize() {
		if (this.document != null && this.highWaterMark > 0) {
			final int length= this.document.getLength();
			if (length > this.highWaterMark) {
				if (this.trimJob.getState() == Job.NONE) { //if the job isn't already running
					this.trimJob.setOffset(length - this.lowWaterMark);
					this.trimJob.schedule();
				}
			}
		}
	}
	
	/**
	 * Clears the console
	 */
	public void clearBuffer() {
		synchronized (this.overflowLock) {
			this.trimJob.setOffset(-1);
			this.trimJob.schedule();
		}
	}
	
	@Override
	public IRegion documentChanged2(final DocumentEvent event) {
		if (this.document == null) {
			return null; //another thread disconnected the partitioner
		}
		if (this.document.getLength() == 0) { //document cleared
			this.partitions.clear();
			this.lastPartition= null;
			this.streamProcessor.clear();
			return new Region(0, 0);
		}
		
		if (this.updateInProgress) {
			if (this.updatePartitions != null) {
				int offset= this.streamProcessor.getTextOffsetInDoc();
				NIConsolePartition partition= this.lastPartition;
				for (final PendingPartition pp : this.updatePartitions) {
					if (pp != null) {
						final int ppLen= pp.text.length();
						if (partition != null) {
							if (partition.getStream() == pp.stream) {
								partition.setLength(
										offset + ppLen - partition.getOffset() );
								offset+= ppLen;
								continue;
							}
							
							if (partition.getLength() == 0) {
								final int idx= this.partitions.lastIndexOf(partition);
								if (idx >= 0) {
									this.partitions.remove(idx);
								}
							}
							partition= null;
						}
						
						if (ppLen > 0) {
							partition= new NIConsolePartition(
									pp.stream.getId(), pp.stream, offset, ppLen);
							this.partitions.add(partition);
							offset+= ppLen;
						}
					}
				}
				this.lastPartition= partition;
				this.streamProcessor.updateApplied();
			}
		}
		
		return new Region(event.fOffset, event.fText.length());
	}
	
	private void setUpdateInProgress(final boolean b) {
		this.updateInProgress= b;
	}
	
	/**
	 * A stream has been appended, add to pendingPartions list and schedule updateJob.
	 * updateJob is scheduled with a slight delay, this allows the console to run the job
	 * less frequently and update the document with a greater amount of data each time 
	 * the job is run
	 * @param stream The stream that was written to.
	 * @param s The string that should be appended to the document.
	 */
	public void streamAppended(final NIConsoleOutputStream stream, final String s) throws IOException {
		if (this.document == null) {
			throw new IOException("Document is closed"); //$NON-NLS-1$
		}
		if (stream == null) {
			throw new NullPointerException("stream"); //$NON-NLS-1$
		}
		synchronized(this.pendingPartitions) {
			final PendingPartition last= (this.pendingPartitions.size() > 0 ?
					this.pendingPartitions.get(this.pendingPartitions.size() - 1) : null );
			if (last != null && last.stream == stream) {
				last.append(s);
			}
			else {
				this.pendingPartitions.add(new PendingPartition(stream, s));
				if (this.pendingTextLength > 0x1ff) {
					this.queueJob.schedule();
				} else {
					this.queueJob.schedule(50);
				}
			}
			
			if (this.pendingTextLength > 0xffff) {
				if (Display.getCurrent() == null){
					try {
						this.pendingPartitions.wait();
					}
					catch (final InterruptedException e) {
					}
				}
				else {
					/*
					 * if we are in UI thread we cannot lock it, so process
					 * queued output directly.
					 */
					processQueue();
				}
			}
		}
	}
	
	/**
	 * Updates the document. Will append everything that is available before 
	 * finishing.
	 */
	private class QueueProcessingJob extends WorkbenchJob {
		
		QueueProcessingJob() {
			super("IOConsole Updater"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}
		
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			processQueue();
			return Status.OK_STATUS;
		}
		
		/* 
		 * Job will process as much as it can each time it's run, but it gets
		 * scheduled everytime a PendingPartition is added to the list, meaning
		 * that this job could get scheduled unnecessarily in cases of heavy output.
		 * Note however, that schedule() will only reschedule a running/scheduled Job
		 * once even if it's called many times.
		 */
		@Override
		public boolean shouldRun() {
			final boolean shouldRun= NIConsolePartitioner.this.isConnected
					&& NIConsolePartitioner.this.pendingPartitions.size() > 0;
			return shouldRun;
		}
		
	}
	
	private void processQueue() {
		synchronized (this.overflowLock) {
			final ImList<PendingPartition> pendingCopy;
			final int pendingLength;
			synchronized(this.pendingPartitions) {
				pendingCopy= ImCollections.toList(this.pendingPartitions);
				this.pendingPartitions.clear();
				pendingLength= this.pendingTextLength;
				this.pendingTextLength= 0;
				this.pendingPartitions.notifyAll();
			}
			if (pendingCopy.isEmpty()) {
				return;
			}
			
			this.streamProcessor.prepareUpdate(pendingCopy, pendingLength);
			
			if (this.isConnected) {
				setUpdateInProgress(true);
				this.updatePartitions= pendingCopy;
				try {
					this.document.replace(
							this.streamProcessor.getTextOffsetInDoc(),
							this.streamProcessor.getTextReplaceLengthInDoc(),
							this.streamProcessor.getText() );
				}
				catch (final BadLocationException e) {}
				finally {
					this.updatePartitions= null;
					setUpdateInProgress(false);
				}
			}
			
			if (this.streamProcessor.wasFinished()) {
				this.console.partitionerFinished();
			}
			
			this.streamProcessor.updateDone();
			
			checkBufferSize();
		}
	}
	
	
	/**
	 * Job to trim the console document, runs in the  UI thread.
	 */
	private class TrimJob extends WorkbenchJob {
		
		/**
		 * trims output up to the line containing the given offset,
		 * or all output if -1.
		 */
		private int truncateOffset;
		
		/**
		 * Creates a new job to trim the buffer.
		 */
		TrimJob() {
			super("Trim Job"); //$NON-NLS-1$
			setSystem(true);
		}
		
		/**
		 * Sets the trim offset.
		 * 
		 * @param offset trims output up to the line containing the given offset
		 */
		public void setOffset(final int offset) {
			this.truncateOffset= offset;
		}
		
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			final IJobManager jobManager= Job.getJobManager();
			try {
				jobManager.join(NIConsolePartitioner.this.console, monitor);
			}
			catch (final OperationCanceledException e1) {
				return Status.CANCEL_STATUS;
			}
			catch (final InterruptedException e1) {
				return Status.CANCEL_STATUS;
			}
			if (NIConsolePartitioner.this.document == null) {
				return Status.OK_STATUS;
			}
			
			final int length= NIConsolePartitioner.this.document.getLength();
			if (this.truncateOffset < length) {
				synchronized (NIConsolePartitioner.this.overflowLock) {
					try {
						if (this.truncateOffset < 0) {
							// clear
							setUpdateInProgress(true);
							NIConsolePartitioner.this.document.set(""); //$NON-NLS-1$
							setUpdateInProgress(false);
							NIConsolePartitioner.this.partitions.clear();
						}
						else {
							// overflow
							final int cutoffLine= NIConsolePartitioner.this.document.getLineOfOffset(this.truncateOffset);
							final int cutOffset= NIConsolePartitioner.this.document.getLineOffset(cutoffLine);
							
							// set the new length of the first partition
							final NIConsolePartition partition= (NIConsolePartition) getPartition(cutOffset);
							partition.setLength(partition.getOffset() + partition.getLength() - cutOffset);
							
							setUpdateInProgress(true);
							NIConsolePartitioner.this.document.replace(0, cutOffset, ""); //$NON-NLS-1$
							setUpdateInProgress(false);
							
							//remove partitions and reset Partition offsets
							final int index= NIConsolePartitioner.this.partitions.indexOf(partition);
							for (int i= 0; i < index; i++) {
								NIConsolePartitioner.this.partitions.remove(0);
							}
							
							int offset= 0;
							for (final Iterator<NIConsolePartition> i= NIConsolePartitioner.this.partitions.iterator(); i.hasNext();) {
								final NIConsolePartition p= i.next();
								p.setOffset(offset);
								offset += p.getLength();
							}
						}
					} catch (final BadLocationException e) {
					}
				}
			}
			return Status.OK_STATUS;
		}
	}
	
	@Override
	public boolean isReadOnly(final int offset) {
		return true;
	}
	
	@Override
	public StyleRange[] getStyleRanges(final int offset, final int length) {
		if (!this.isConnected) {
			return new StyleRange[0];
		}
		final NIConsolePartition[] computedPartitions= (NIConsolePartition[]) computePartitioning(offset, length);
		final StyleRange[] styles= new StyleRange[computedPartitions.length];
		for (int i= 0; i < computedPartitions.length; i++) {
			final int rangeStart= Math.max(computedPartitions[i].getOffset(), offset);
			final int rangeLength= computedPartitions[i].getLength();
			styles[i]= new StyleRange(rangeStart, rangeLength,
					computedPartitions[i].getStream().getColor(), null,
					computedPartitions[i].getStream().getFontStyle() );
		}
		return styles;
	}
	
}
