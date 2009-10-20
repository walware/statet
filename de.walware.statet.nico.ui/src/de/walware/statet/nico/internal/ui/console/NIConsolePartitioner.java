/*******************************************************************************
 * Copyright (c) 2000-2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

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

import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleOutputStream;


/**
 * Partitioner for a NIConsole's document.
 */
public class NIConsolePartitioner implements IConsoleDocumentPartitioner, IDocumentPartitionerExtension {
	
	
	private NIConsole fConsole;
	
	private String[] fPartitionIds;
	
	private IDocument fDocument;
	
	private boolean fConnected = false;
	
	
	private final ArrayList<NIConsolePartition> fPartitions = new ArrayList<NIConsolePartition>();
	
	/**
	 * Blocks of data that have not yet been appended to the document.
	 */
	private final ArrayList<PendingPartition> fPendingPartitions = new ArrayList<PendingPartition>();
	/**
	 * A list of PendingPartitions to be appended by the updateJob
	 */
	private PendingPartition[] fUpdatePartitions;
	/**
	 * The last partition appended to the document
	 */
	private NIConsolePartition fLastPartition;
	
	private PendingPartition fConsoleClosedPartition;
	
	private final QueueProcessingJob fQueueJob = new QueueProcessingJob();
	
	private final TrimJob fTrimJob = new TrimJob();
	
	/**
	 * Flag to indicate that the updateJob is updating the document.
	 */
	private boolean fUpdateInProgress;
	/**
	 * offset used by updateJob
	 */
	private int fFirstOffset;
	
	/**
	 * An array of legal line delimiters
	 */
	private int fHighWaterMark = -1;
	private int fLowWaterMark = -1;
	
	/**
	 * Lock for appending to and removing from the document - used
	 * to synchronize addition of new text/partitions in the update
	 * job and handling buffer overflow/clearing of the console. 
	 */
	private Object fOverflowLock = new Object();
	
	private int fBuffer; 
	
	
	public NIConsolePartitioner(final NIConsole console, final List<String> ids) {
		fConsole = console;
		fPartitionIds = ids.toArray(new String[ids.size()]);
		fTrimJob.setRule(console.getSchedulingRule());
		
		fQueueJob.setRule(fConsole.getSchedulingRule());
	}
	
	public IDocument getDocument() {
		return fDocument;
	}
	
	
	public void connect(final IDocument doc) {
		fDocument = doc;
		fDocument.setDocumentPartitioner(this);
		
		fConnected = true;
	}
	
	public int getHighWaterMark() {
		return fHighWaterMark;
	}
	
	public int getLowWaterMark() {
		return fLowWaterMark;
	}
	
	public void setWaterMarks(final int low, final int high) {
		fLowWaterMark = low;
		fHighWaterMark = high;
		ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				checkBufferSize();
			}
		});
	}
	
	/**
	 * Notification from the console that all of its streams have been closed.
	 */
	public void streamsClosed() {
		fConsoleClosedPartition = new PendingPartition(null, null);
		synchronized (fPendingPartitions) {
			fPendingPartitions.add(fConsoleClosedPartition);
		}
		fQueueJob.schedule(); //ensure that all pending partitions are processed.
	}
	
	public void disconnect() {
		synchronized (fOverflowLock) {
			fConnected = false;
			fDocument = null;
			fPartitions.clear();
		}
	}
	
	public void documentAboutToBeChanged(final DocumentEvent event) {
	}
	
	public boolean documentChanged(final DocumentEvent event) {
		return documentChanged2(event) != null;
	}
	
	public String[] getLegalContentTypes() {
		return fPartitionIds;
	}
	
	public String getContentType(final int offset) {
		return getPartition(offset).getType();
	}
	
	public ITypedRegion[] computePartitioning(final int offset, final int length) {
		final int rangeEnd = offset + length;
		int left = 0;
		int right = fPartitions.size() - 1;
		int mid = 0;
		NIConsolePartition position = null;
		
		if (right <= 0) {
			if (right == 0) {
				return new NIConsolePartition[] { fPartitions.get(0) };
			}
			return new NIConsolePartition[0];
		}
		while (left < right) {
			mid = (left + right) / 2;
			position = fPartitions.get(mid);
			if (rangeEnd < position.getOffset()) {
				if (left == mid)
					right = left;
				else
					right = mid -1;
			}
			else if (offset > (position.getOffset() + position.getLength() - 1)) {
				if (right == mid)
					left = right;
				else
					left = mid  +1;
			}
			else {
				left = right = mid;
			}
		}
		
		final List<NIConsolePartition> list = new ArrayList<NIConsolePartition>();
		int index = left - 1;
		if (index >= 0) {
			position = fPartitions.get(index);
			while (index >= 0 && (position.getOffset() + position.getLength()) > offset) {
				index--;
				if (index >= 0) {
					position = fPartitions.get(index);
				}
			}
		}
		index++;
		position = fPartitions.get(index);
		while (index < fPartitions.size() && (position.getOffset() < rangeEnd)) {
			list.add(position);
			index++;
			if (index < fPartitions.size()) {
				position = fPartitions.get(index);
			}
		}
		
		return list.toArray(new NIConsolePartition[list.size()]);
	}
	
	public ITypedRegion getPartition(final int offset) {
		for (int i = 0; i < fPartitions.size(); i++) {
			final ITypedRegion partition = fPartitions.get(i);
			final int start = partition.getOffset();
			final int end = start + partition.getLength();
			if (offset >= start && offset < end) {
				return partition;
			}
		}
		
		return (fLastPartition != null) ? 
				fLastPartition : new NIConsolePartition(fPartitionIds[0], 0, null);
	}
	
	/**
	 * Enforces the buffer size.
	 * When the number of lines in the document exceeds the high water mark, the 
	 * beginning of the document is trimmed until the number of lines equals the 
	 * low water mark.
	 */
	private void checkBufferSize() {
		if (fDocument != null && fHighWaterMark > 0) {
			final int length = fDocument.getLength();
			if (length > fHighWaterMark) {
				if (fTrimJob.getState() == Job.NONE) { //if the job isn't already running
					fTrimJob.setOffset(length - fLowWaterMark);
					fTrimJob.schedule();
				}
			}
		}
	}
	
	/**
	 * Clears the console
	 */
	public void clearBuffer() {
		synchronized (fOverflowLock) {
			fTrimJob.setOffset(-1);
			fTrimJob.schedule();
		}
	}
	
	public IRegion documentChanged2(final DocumentEvent event) {
		if (fDocument == null) {
			return null; //another thread disconnected the partitioner
		}
		if (fDocument.getLength() == 0) { //document cleared
			fPartitions.clear();
			fLastPartition = null;
			return new Region(0, 0);
		}
		
		if (fUpdateInProgress) {
			if (fUpdatePartitions != null) {
				for (int i = 0; i < fUpdatePartitions.length; i++) {
					final PendingPartition pp = fUpdatePartitions[i];
					if (pp == fConsoleClosedPartition) {
						continue;
					}
					
					final int ppLen = pp.text.length();
					if (fLastPartition != null && fLastPartition.fOutputStream == pp.stream) {
						final int len = fLastPartition.getLength();
						fLastPartition.setLength(len + ppLen);
					}
					else {
						final NIConsolePartition partition = new NIConsolePartition(pp.stream.getId(), ppLen, pp.stream);
						partition.setOffset(fFirstOffset);
						fLastPartition = partition;
						fPartitions.add(partition);
					}
					fFirstOffset += ppLen;
				}
			}
		}
		
		return new Region(event.fOffset, event.fText.length());
	}
	
	private void setUpdateInProgress(final boolean b) {
		fUpdateInProgress = b;
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
		if (fDocument == null) {
			throw new IOException("Document is closed"); //$NON-NLS-1$
		}
		synchronized(fPendingPartitions) {
			final PendingPartition last = (fPendingPartitions.size() > 0 ? fPendingPartitions.get(fPendingPartitions.size()-1) : null);
			if (last != null && last.stream == stream) {
				last.append(s);
			}
			else {
				fPendingPartitions.add(new PendingPartition(stream, s));
				if (fBuffer > 1000) {
					fQueueJob.schedule();
				} else {
					fQueueJob.schedule(50);
				}
			}
			
			if (fBuffer > 160000) {
				if (Display.getCurrent() == null){
					try {
						fPendingPartitions.wait();
					}
					catch (final InterruptedException e) {
					}
				}
				else {
					/*
					 * if we are in UI thread we cannot lock it, so process
					 * queued output.
					 */
					processQueue();
				}
			}
		}
	}
	
	/**
	 * Holds data until updateJob can be run and the document can be updated.
	 */
	private class PendingPartition {
		StringBuffer text = new StringBuffer(8192);
		NIConsoleOutputStream stream;
		
		PendingPartition(final NIConsoleOutputStream stream, final String text) {
			this.stream = stream;
			if (text != null) {
				append(text);
			}
		}
		
		void append(final String moreText) {
			text.append(moreText);
			fBuffer += moreText.length();
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
			final boolean shouldRun = fConnected && fPendingPartitions.size() > 0;
			return shouldRun;
		}
		
	}
	
	void processQueue() {
		synchronized (fOverflowLock) {
			final PendingPartition[] pendingCopy;
			StringBuffer buffer = null;
			boolean consoleClosed = false;
			synchronized(fPendingPartitions) {
				pendingCopy = fPendingPartitions.toArray(new PendingPartition[fPendingPartitions.size()]);
				fPendingPartitions.clear();
				fBuffer = 0;
				fPendingPartitions.notifyAll();
			}
			// determine buffer size
			int size = 0;
			for (int i = 0; i < pendingCopy.length; i++) {
				if (pendingCopy[i] != fConsoleClosedPartition) { 
					size += pendingCopy[i].text.length();
				}
			}
			buffer = new StringBuffer(size);
			for (int i = 0; i < pendingCopy.length; i++) {
				if (pendingCopy[i] != fConsoleClosedPartition) { 
					buffer.append(pendingCopy[i].text);
				}
				else {
					consoleClosed = true;
				}
			}
			
			if (fConnected) {
				setUpdateInProgress(true);
				fUpdatePartitions = pendingCopy;
				fFirstOffset = fDocument.getLength();
				try {
					if (buffer != null) {
						fDocument.replace(fFirstOffset, 0, buffer.toString());
					}
				} catch (final BadLocationException e) {}
				fUpdatePartitions = null;
				setUpdateInProgress(false);
			}
			if (consoleClosed) {
				fConsole.partitionerFinished();
			}
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
			truncateOffset = offset;
		}
		
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			final IJobManager jobManager = Job.getJobManager();
			try {
				jobManager.join(fConsole, monitor);
			}
			catch (final OperationCanceledException e1) {
				return Status.CANCEL_STATUS;
			}
			catch (final InterruptedException e1) {
				return Status.CANCEL_STATUS;
			}
			if (fDocument == null) {
				return Status.OK_STATUS;
			}
			
			final int length = fDocument.getLength();
			if (truncateOffset < length) {
				synchronized (fOverflowLock) {
					try {
						if (truncateOffset < 0) {
							// clear
							setUpdateInProgress(true);
							fDocument.set(""); //$NON-NLS-1$
							setUpdateInProgress(false);
							fPartitions.clear();
						}
						else {
							// overflow
							final int cutoffLine = fDocument.getLineOfOffset(truncateOffset);
							final int cutOffset = fDocument.getLineOffset(cutoffLine);
							
							// set the new length of the first partition
							final NIConsolePartition partition = (NIConsolePartition) getPartition(cutOffset);
							partition.setLength(partition.getOffset() + partition.getLength() - cutOffset);
							
							setUpdateInProgress(true);
							fDocument.replace(0, cutOffset, ""); //$NON-NLS-1$
							setUpdateInProgress(false);
							
							//remove partitions and reset Partition offsets
							final int index = fPartitions.indexOf(partition);
							for (int i = 0; i < index; i++) {
								fPartitions.remove(0);
							}
							
							int offset = 0;
							for (final Iterator<NIConsolePartition> i = fPartitions.iterator(); i.hasNext();) {
								final NIConsolePartition p = i.next();
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
	
	public boolean isReadOnly(final int offset) {
		return true;
	}
	
	public StyleRange[] getStyleRanges(final int offset, final int length) {
		if (!fConnected) {
			return new StyleRange[0];
		}
		final NIConsolePartition[] computedPartitions = (NIConsolePartition[]) computePartitioning(offset, length);
		final StyleRange[] styles = new StyleRange[computedPartitions.length];
		for (int i = 0; i < computedPartitions.length; i++) {
			final int rangeStart = Math.max(computedPartitions[i].getOffset(), offset);
			final int rangeLength = computedPartitions[i].getLength();
			styles[i] = new StyleRange(rangeStart, rangeLength, computedPartitions[i].fOutputStream.getColor(), null, computedPartitions[i].fOutputStream.getFontStyle());
		}
		return styles;
	}
	
}
