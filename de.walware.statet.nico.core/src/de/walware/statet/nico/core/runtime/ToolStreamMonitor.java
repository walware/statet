/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;

import java.util.EnumMap;
import java.util.EnumSet;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;


public class ToolStreamMonitor implements IStreamMonitor {
	
	
	private final EnumMap<SubmitType, ListenerList> fListeners;
	private int fCurrentMeta;
	
	
	public ToolStreamMonitor() {
		// Create listener lists
		fListeners= new EnumMap<>(SubmitType.class);
		for (final SubmitType type : EnumSet.allOf(SubmitType.class)) {
			fListeners.put(type, new ListenerList(ListenerList.IDENTITY));
		}
	}
	
	
	@Override
	public void addListener(final IStreamListener listener) {
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
	public void addListener(final IStreamListener listener, final EnumSet<SubmitType> types) {
		for (final SubmitType type : SubmitType.values()) {
			final ListenerList list = fListeners.get(type);
			if (types.contains(type)) {
				list.add(listener);
			}
			else {
				list.remove(listener);
			}
		}
	}
	
	@Override
	public void removeListener(final IStreamListener listener) {
		for (final ListenerList list : fListeners.values()) {
			list.remove(listener);
		}
	}
	
	@Override
	public String getContents() {
		// not buffered
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Should only called by the controller or its runnables.
	 * 
	 * @param text text to append.
	 * @param type the type of the runnable.
	 * @param optional meta data
	 */
	public void append(final String text, final SubmitType type, final int meta) {
		final ListenerList list = fListeners.get(type);
		fCurrentMeta = meta;
		for (final Object obj : list.getListeners()) {
			final IStreamListener listener = (IStreamListener) obj;
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
