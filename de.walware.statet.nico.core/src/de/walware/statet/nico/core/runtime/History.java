/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EnumSet;

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
	
	private Entry fNewest;
	private Entry fOldest;
	
	private ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);
	
	private ToolProcess fProcess;
	private IPreferenceChangeListener fPreferenceListener;
	private HistoryPreferences fCurrentPreferences;
	private IStreamListener fStreamListener;
	
	private volatile Entry[] fArrayCache;
	
	
	/**
	 * An entry of this history.
	 */
	public class Entry {
		
		private final String fCommand;
		private final boolean fIsEmpty;
		private Entry fOlder;
		private Entry fNewer;
		
		private Entry(Entry older, String command) {
			fCommand = command;
			fIsEmpty = isCommandEmpty(command);
			fOlder = older;
			if (older != null) {
				older.fNewer = this;
			}
		}
		
		public String getCommand() {
			return fCommand;
		}
		
		public boolean isEmpty() {
			return fIsEmpty;
		}
		
		public Entry getNewer() {
			synchronized (History.this) {
				return fNewer;
			}
		}
		
		public Entry getOlder() {
			synchronized (History.this) {
				return fOlder;
			}
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
	
	
	public History(ToolProcess process) {
		fProcess = process;
		
		fStreamListener = new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
				if ((((ToolStreamMonitor) monitor).getMeta() & IToolRunnableControllerAdapter.META_HISTORY_DONTADD) == 0) {
					addCommand(text);
				}
			}
		};
		
		fPreferenceListener = new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				checkSettings();
			}
		};
		IEclipsePreferences[] nodes = PreferencesUtil.getInstancePrefs().getPreferenceNodes(NicoPreferenceNodes.CAT_HISTORY_QUALIFIER);
		for (IEclipsePreferences node : nodes) {
			node.addPreferenceChangeListener(fPreferenceListener);
		}
		checkSettings();
	}
	
	private void checkSettings() {
		HistoryPreferences prefs = new HistoryPreferences(PreferencesUtil.getInstancePrefs());
		if (prefs.equals(fCurrentPreferences)) {
			return;
		}
		ToolController controller = fProcess.getController();
		if (controller != null) {
			ToolStreamProxy streams = controller.getStreams();
			fIgnoreCommentLines = prefs.filterComments();
			
			EnumSet<SubmitType> types = prefs.getSelectedTypes();
			streams.getInputStreamMonitor().addListener(fStreamListener, types);
		}

		synchronized (this) {
			fMaxSize = prefs.getLimitCount();
			if (fCurrentSize > fMaxSize) {
				trimSize();
				for (Object obj : fListeners.getListeners()) {
					((IHistoryListener) obj).completeChange();
				}
			}
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
	public void load(Object file, String charset, boolean forceCharset, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(NicoCoreMessages.LoadHistoryJob_label, 100);

		try {
			final HistoryData exch = new HistoryData();
			ReaderAction action = new ReaderAction() {
				public void run(BufferedReader reader, IProgressMonitor monitor) throws IOException {
					if (reader.ready()) {
						exch.oldest = new Entry(null, reader.readLine());
						exch.newest = exch.oldest;
						exch.size = 1;
						int maxSize = fMaxSize;
						while (reader.ready()) {
							exch.newest = new Entry(exch.newest, reader.readLine());
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
			ReadTextFileOperation op = FileUtil.createReadTextFileOp(action, file);
			op.setCharset(charset, forceCharset);
			op.doOperation(new SubProgressMonitor(monitor, 90));
			monitor.subTask(NLS.bind(Messages.LoadHistory_AllocatingTask_label, fProcess.getToolLabel(false)));

			synchronized (this) {
				fOldest = exch.oldest;
				fNewest = exch.newest;
				fCurrentSize = exch.size;
				if (fCurrentSize > fMaxSize) {
					trimSize();
				}
				fireCompleteChange();
			}
		} catch (CoreException e) {
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
	public void save(Object file, int mode, String charset, boolean forceCharset,
			IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(NicoCoreMessages.SaveHistoryJob_label, 4);
		try {
			String newLine = fProcess.getWorkspaceData().getLineSeparator();
			StringBuilder buffer;
			synchronized (this) {
				buffer = new StringBuilder(fCurrentSize * 10);
				Entry e = fOldest;
				while (e != null) {
					buffer.append(e.fCommand);
					buffer.append(newLine);
					e = e.fNewer;
				}
			}
			final String content = buffer.toString();
			buffer = null;
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
			
			WriteTextFileOperation op = FileUtil.createWriteTextFileOp(content, file);
			op.setCharset(charset, forceCharset);
			op.setFileOperationMode(mode);
			op.doOperation(new SubProgressMonitor(monitor, 2));
		} catch (CoreException e) {
			throw new CoreException(new Status(Status.ERROR, NicoCore.PLUGIN_ID, 0,
					NLS.bind(Messages.SaveHistory_error_message,
							new Object[] { fProcess.getLabel(), file.toString() }), e));
		} finally {
			monitor.done();
		}
	}
	
	private void addCommand(String command) {
		assert(command != null);
		
		Entry removedEntry = null;
		Entry newEntry = null;
		
		synchronized (this) {
			newEntry = new Entry(fNewest, command);
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
		
			Object[] listeners = fListeners.getListeners();
			for (Object obj : listeners) {
				IHistoryListener listener = (IHistoryListener) obj;
				if (removedEntry != null)
					listener.entryRemoved(removedEntry);
				listener.entryAdded(newEntry);
			}
		}
	}
	
	/**
	 * Return the newest history entry.
	 * 
	 * @return newest entry
	 * 		or <code>null</null>, if history is empty.
	 */
	public synchronized Entry getNewest() {
		return fNewest;
	}
	
	/**
	 * Return an array with all entries.
	 * <p>
	 * Don't use this method to frequently.
	 * 
	 * @return array with all entries
	 * 		or an array with length 0, if history is empty.
	 */
	public Entry[] toArray() {
		Entry[] array = fArrayCache;
		if (array != null) {
			return array;
		}
		synchronized (this) {
			array = new Entry[fCurrentSize];
			Entry e = fOldest;
			for (int i = 0; i < array.length; i++) {
				array[i] = e;
				e = e.fNewer;
			}
		}
		return array;
	}

	
    /**
     * Adds the given listener to this history.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener the listener
     */
	public void addListener(IHistoryListener listener) {
		fListeners.add(listener);
	}
	
    /**
     * Removes the given listener from this history.
     * Has no effect if an identical listener was not already registered.
     *
     * @param listener the listener
     */
	public void removeListener(IHistoryListener listener) {
		fListeners.remove(listener);
	}
	
	private void fireCompleteChange() {
		fArrayCache = toArray();
		for (Object obj : fListeners.getListeners()) {
			((IHistoryListener) obj).completeChange();
		}
		fArrayCache = null;
	}
	
	/**
	 * Checks, if this command is empty or an command
	 */
	private boolean isCommandEmpty(String command) {
		int length = command.length();
		for (int i = 0; i < length; i++) {
			char c = command.charAt(i);
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
