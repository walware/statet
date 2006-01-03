/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.runtime;

import java.util.EnumMap;
import java.util.EnumSet;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;


public class ToolStreamMonitor implements IStreamMonitor {

	
	private EnumMap<SubmitType, ListenerList> fListeners;
	
	
	public ToolStreamMonitor() {
		
		// Create listener lists
		fListeners = new EnumMap<SubmitType, ListenerList>(SubmitType.class);
		for (SubmitType type : EnumSet.allOf(SubmitType.class)) {
			fListeners.put(type, new ListenerList());
		}
	}

	public void addListener(IStreamListener listener) {
		// Assign all SubmitTypes to this listener 
		
		addListener(listener, EnumSet.allOf(SubmitType.class));
	}
	
	/**
	 * Adds the given listener to this stream monitor's registered listeners.
	 * Has no effect if an identical listener is already registered.
	 * The events (streamAppended) are filtered by the specified set.
	 *
	 * @param listener the listener to add
	 * @param types the types to listen for 
	 */
	public void addListener(IStreamListener listener, EnumSet<SubmitType> types) {
		
		for (SubmitType type : types) {
			ListenerList list = fListeners.get(type);
			list.add(listener);
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
	 */
	public void append(String text, SubmitType type) {
		
		ListenerList list = fListeners.get(type);
		for (Object obj : list.getListeners()) {
			IStreamListener listener = (IStreamListener) obj;
			listener.streamAppended(text, this);
		}
	}
}