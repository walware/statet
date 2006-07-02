/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.EnumMap;
import java.util.EnumSet;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;


public class ToolStreamMonitor implements IStreamMonitor {

	
	private EnumMap<SubmitType, ListenerList> fListeners;
	private int fCurrentMeta;
	
	
	public ToolStreamMonitor() {
		
		// Create listener lists
		fListeners = new EnumMap<SubmitType, ListenerList>(SubmitType.class);
		for (SubmitType type : EnumSet.allOf(SubmitType.class)) {
			fListeners.put(type, new ListenerList(ListenerList.IDENTITY));
		}
	}

	public void addListener(IStreamListener listener) {
		// Assign all SubmitTypes to this listener 
		
		addListener(listener, SubmitType.getDefaultSet());
	}
	
	/**
	 * Adds the given listener to this stream monitor's registered listeners.
	 * <p>
	 * The events (streamAppended) are filtered by the specified set.
	 * If an identical listener is already registered, the filter settings
	 * are replaced.</p>
	 *
	 * @param listener the listener to add
	 * @param types the types to listen for 
	 */
	public void addListener(IStreamListener listener, EnumSet<SubmitType> types) {
		
		for (SubmitType type : SubmitType.values()) {
			ListenerList list = fListeners.get(type);
			if (types.contains(type)) {
				list.add(listener);
			}
			else {
				list.remove(listener);
			}
		}
	}

	public void removeListener(IStreamListener listener) {
		
		for (ListenerList list : fListeners.values()) {
			list.remove(listener);
		}
	}

	public String getContents() {
		// not buffered
		
		return "";
	}
	
	/**
	 * Should only called by the controller or its runnables.
	 * 
	 * @param text text to append.
	 * @param type the type of the runnable.
	 * @param optional meta data
	 */
	public void append(String text, SubmitType type, int meta) {
		
		ListenerList list = fListeners.get(type);
		fCurrentMeta = meta;
		for (Object obj : list.getListeners()) {
			IStreamListener listener = (IStreamListener) obj;
			listener.streamAppended(text, this);
		}
	}
	
	public int getMeta() {
		
		return fCurrentMeta;
	}
	
	
	void dispose() {
		
		fListeners.clear();
	}
}