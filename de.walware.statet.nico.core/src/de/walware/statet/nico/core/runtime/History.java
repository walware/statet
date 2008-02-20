/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.eclipsecommons.FileUtil;
import de.walware.eclipsecommons.FileUtil.ReadTextFileOperation;
import de.walware.eclipsecommons.FileUtil.ReaderAction;
import de.walware.eclipsecommons.FileUtil.WriteTextFileOperation;
import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.NicoCoreMessages;
import de.walware.statet.nico.core.NicoPreferenceNodes;
import de.walware.statet.nico.internal.core.Messages;
import de.walware.statet.nico.internal.core.preferences.HistoryPreferences;


/**
 * Command history.
 */
public class History {
	
	
	private int fMaxSize = 1000;
	private int fCurrentSize = 0;
	private boolean fIgnoreCommentLines;
	
	private volatile Entry fNewest;
	private volatile Entry fOldest;
	
	private final ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);
	private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
	
	private ToolProcess fProcess;
	private IPreferenceChangeListener fPreferenceListener;
	private HistoryPreferences fCurrentPreferences;
	private IStreamListener fStreamListener;
	
	private volatile Entry[] fArrayCache;
	
	
	/**
	 * An entry of this history.
	 */
	public final class Entry {
		
		private final String fCommand;
		private final long fTimeStamp;
		private final boolean fIsEmpty;
		private volatile Entry fOlder;
		private volatile Entry fNewer;
		
		private Entry(final Entry older, final String command, final long stamp) {
			fCommand = command;
			fIsEmpty = isCommandEmpty(command);
			fTimeStamp = stamp;
			fOlder = older;
			if (older != null) {
				older.fNewer = this;
			}
		}
		
		public String getCommand() {
			return fCommand;
		}
		
		public long getTimeStamp() {
			return fTimeStamp;
		}
		
		public boolean isEmpty() {
			return fIsEmpty;
		}
		
		public Entry getNewer() {
			return fNewer;
		}
		
		public Entry getOlder() {
			return fOlder;
		}
		
		/**
		 * Returns the history, this entry belong to.
		 * 
		 * @return the history.
		 */
		public History getHistory() {
			return History.this;
		}
		
		private Entry dispose() {
			if (fNewer != null) {
				fNewer.fOlder = null;
			}
			return fNewer;
		}
	}
	
	
	public History(final ToolProcess process) {
		fProcess = process;
		
		fStreamListener = new IStreamListener() {
			public void streamAppended(final String text, final IStreamMonitor monitor) {
				if ((((ToolStreamMonitor) monitor).getMeta() & IToolRunnableControllerAdapter.META_HISTORY_DONTADD) == 0) {
					addCommand(text);
				}
			}
		};
		
		fPreferenceListener = new IPreferenceChangeListener() {
			public void preferenceChange(final PreferenceChangeEvent event) {
				checkSettings();
			}
		};
		final IEclipsePreferences[] nodes = PreferencesUtil.getInstancePrefs().getPreferenceNodes(NicoPreferenceNodes.CAT_HISTORY_QUALIFIER);
		for (final IEclipsePreferences node : nodes) {
			node.addPreferenceChangeListener(fPreferenceListener);
		}
		checkSettings();
	}
	
	public Lock getReadLock() {
		return fLock.readLock();
	}
	
	private void checkSettings() {
		final HistoryPreferences prefs = new HistoryPreferences(PreferencesUtil.getInstancePrefs());
		if (prefs.equals(fCurrentPreferences)) {
			return;
		}
		final ToolController controller = fProcess.getController();
		if (controller != null) {
			final ToolStreamProxy streams = controller.getStreams();
			fIgnoreCommentLines = prefs.filterComments();
			
			final EnumSet<SubmitType> types = prefs.getSelectedTypes();
			streams.getInputStreamMonitor().addListener(fStreamListener, types);
		}
		
		fLock.writeLock().lock();
		try {
			fMaxSize = prefs.getLimitCount();
			if (fCurrentSize > fMaxSize) {
				trimSize();
				fireCompleteChange();
			}
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
	private void trimSize() {
		while (fCurrentSize > fMaxSize) {
			fOldest = fOldest.dispose();
			fCurrentSize--;
		}
	}
	
	private class HistoryData {
		Entry oldest;
		Entry newest;
		int size;
	}
	
	/**
	 * Load the history from a text file. Previous entries are removed.
	 * 
	 * Note: The thread can be blocked because of workspace operations. So
	 * it is a good idea, that the user have the chance to cancel the action.
	 * 
	 * @param file, type must be supported by IFileUtil impl.
	 * @param charset the charset (if not detected automatically)
	 * @param forceCharset use always the specified charset
	 * @param monitor
	 * 
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	public void load(final Object file, final String charset, final boolean forceCharset, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(NicoCoreMessages.LoadHistoryJob_label, 100);
		
		try {
			final HistoryData exch = new HistoryData();
			final ReaderAction action = new ReaderAction() {
				public void run(final BufferedReader reader, final IProgressMonitor monitor) throws IOException, CoreException {
					long timeStamp = FileUtil.getTimeStamp(file, new SubProgressMonitor(monitor, 1));
					if (timeStamp < 0) {
						timeStamp = System.currentTimeMillis();
					}
					if (reader.ready()) {
						exch.oldest = new Entry(null, reader.readLine(), timeStamp);
						exch.newest = exch.oldest;
						exch.size = 1;
						final int maxSize = fMaxSize;
						while (reader.ready()) {
							exch.newest = new Entry(exch.newest, reader.readLine(), timeStamp);
							if (exch.size < maxSize) {
								exch.size++;
							}
							else {
								exch.oldest = exch.oldest.dispose();
							}
						}
					}
					monitor.done();
				}
			};
			final ReadTextFileOperation op = FileUtil.createReadTextFileOp(action, file);
			op.setCharset(charset, forceCharset);
			op.doOperation(new SubProgressMonitor(monitor, 90));
			monitor.subTask(NLS.bind(Messages.LoadHistory_AllocatingTask_label, fProcess.getToolLabel(false)));
			
			fLock.writeLock().lock();
			try {
				fOldest = exch.oldest;
				fNewest = exch.newest;
				fCurrentSize = exch.size;
				if (fCurrentSize > fMaxSize) {
					trimSize();
				}
				fireCompleteChange();
			}
			finally {
				fLock.writeLock().unlock();
			}
		} 
		catch (final CoreException e) {
			throw new CoreException(new Status(Status.ERROR, NicoCore.PLUGIN_ID, 0,
					NLS.bind(Messages.LoadHistory_error_message,
							new Object[] { fProcess.getToolLabel(true), file.toString() }), e));
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Save the history to a text file.
	 * 
	 * Note: The thread can be blocked because of workspace operations. So
	 * it is a good idea, that the user have the chance to cancel the action.
	 * 
	 * @param file, type must be supported by IFileUtil impl.
	 * @param mode allowed: EFS.OVERWRITE, EFS.APPEND
	 * @param charset the charset (if not appended)
	 * @param forceCharset use always the specified charset
	 * @param monitor
	 * 
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	public void save(final Object file, final int mode, final String charset, final boolean forceCharset,
			IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(NicoCoreMessages.SaveHistoryJob_label, 4);
		try {
			final String newLine = fProcess.getWorkspaceData().getLineSeparator();
			StringBuilder buffer;
			fLock.readLock().lock();
			try {
				buffer = new StringBuilder(fCurrentSize * 10);
				Entry e = fOldest;
				while (e != null) {
					buffer.append(e.fCommand);
					buffer.append(newLine);
					e = e.fNewer;
				}
			}
			finally {
				fLock.readLock().unlock();
			}
			final String content = buffer.toString();
			buffer = null;
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
			
			final WriteTextFileOperation op = FileUtil.createWriteTextFileOp(content, file);
			op.setCharset(charset, forceCharset);
			op.setFileOperationMode(mode);
			op.doOperation(new SubProgressMonitor(monitor, 2));
		} catch (final CoreException e) {
			throw new CoreException(new Status(Status.ERROR, NicoCore.PLUGIN_ID, 0,
					NLS.bind(Messages.SaveHistory_error_message,
							new Object[] { fProcess.getLabel(), file.toString() }), e));
		} finally {
			monitor.done();
		}
	}
	
	private void addCommand(final String command) {
		assert(command != null);
		final long stamp = System.currentTimeMillis();
		
		final Entry removedEntry = null;
		Entry newEntry = null;
		
		fLock.writeLock().lock();
		try {
			newEntry = new Entry(fNewest, command, stamp);
			if (fNewest != null) {
				fNewest.fNewer = newEntry;
			}
			else {
				fOldest = newEntry;
			}
			fNewest = newEntry;
			
			if (fCurrentSize == fMaxSize) {
				fOldest = fOldest.dispose();
			}
			else {
				fCurrentSize++;
			}
			
			final Object[] listeners = fListeners.getListeners();
			for (final Object obj : listeners) {
				final IHistoryListener listener = (IHistoryListener) obj;
				if (removedEntry != null)
					listener.entryRemoved(this, removedEntry);
				listener.entryAdded(this, newEntry);
			}
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
	/**
	 * Return the newest history entry.
	 * 
	 * @return newest entry
	 * 		or <code>null</null>, if history is empty.
	 */
	public Entry getNewest() {
		return fNewest;
	}
	
	/**
	 * Return an array with all entries.
	 * <p>
	 * Make shure, that you have a read lock.
	 * 
	 * @return array with all entries
	 * 		or an array with length 0, if history is empty.
	 */
	public Entry[] toArray() {
		Entry[] array = fArrayCache;
		if (array != null) {
			return array;
		}
		array = new Entry[fCurrentSize];
		Entry e = fOldest;
		for (int i = 0; i < array.length; i++) {
			array[i] = e;
			e = e.fNewer;
		}
		return array;
	}
	
	
	/**
	 * Adds the given listener to this history.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener
	 */
	public void addListener(final IHistoryListener listener) {
		fListeners.add(listener);
	}
	
	/**
	 * Removes the given listener from this history.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener the listener
	 */
	public void removeListener(final IHistoryListener listener) {
		fListeners.remove(listener);
	}
	
	private void fireCompleteChange() {
		fArrayCache = toArray();
		for (final Object obj : fListeners.getListeners()) {
			((IHistoryListener) obj).completeChange(this, fArrayCache);
		}
		fArrayCache = null;
	}
	
	/**
	 * Checks, if this command is empty or an command
	 */
	private boolean isCommandEmpty(final String command) {
		final int length = command.length();
		for (int i = 0; i < length; i++) {
			final char c = command.charAt(i);
			switch(c) {
			
			case ' ':
			case '\t':
				continue;
			case '#':
				return fIgnoreCommentLines;
			default:
				return false;
			}
		}
		return true;
	}
	
}
