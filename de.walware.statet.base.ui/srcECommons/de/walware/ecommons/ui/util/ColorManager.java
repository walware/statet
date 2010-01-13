/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


public class ColorManager {
	
	
	protected Map<String, RGB> fKeyTable = new HashMap<String, RGB>(10);
	protected Map<Display, Map<RGB, Color>> fDisplayTable = new HashMap<Display, Map<RGB, Color>>(2);
	
	
	/**
	 * Flag which tells if the colors are automatically disposed when
	 * the current display gets disposed.
	 */
	private boolean fAutoDisposeOnDisplayDispose;
	
	private Listener fDisposeListener = new Listener() {
		public void handleEvent(final Event event) {
			dispose(event.display);
		}
	};
	
	
	/**
	 * Creates a new Java color manager which automatically
	 * disposes the allocated colors when the current display
	 * gets disposed.
	 */
	public ColorManager() {
		this(true);
	}
	
	/**
	 * Creates a new Java color manager.
	 * 
	 * @param autoDisposeOnDisplayDispose 	if <code>true</code>  the color manager
	 * automatically disposes all managed colors when the current display gets disposed
	 * and all calls to {@link org.eclipse.jface.text.source.ISharedTextColors#dispose()} are ignored.
	 */
	private ColorManager(final boolean autoDisposeOnDisplayDispose) {
		fAutoDisposeOnDisplayDispose = autoDisposeOnDisplayDispose;
	}
	
	
	public void dispose() {
		for (final Display display : fDisplayTable.keySet()) {
			final Display ref = display;
			display.asyncExec(new Runnable() {
				public void run() {
					dispose(ref);
				}
			});
		}
	}
	
	private void dispose(final Display display) {
		display.removeListener(SWT.Dispose, fDisposeListener);
		final Map<RGB, Color> colorTable = fDisplayTable.remove(display);
		if (colorTable != null) {
			for (final Color color : colorTable.values()) {
				if (color != null && !color.isDisposed()) {
					color.dispose();
				}
			}
		}
	}
	
	public Color getColor(final RGB rgb) {
		if (rgb == null)
			return null;
		
		final Display display = Display.getCurrent();
		Map<RGB, Color> colorTable = fDisplayTable.get(display);
		if (colorTable == null) {
			colorTable = new HashMap<RGB, Color>(10);
			fDisplayTable.put(display, colorTable);
			if (fAutoDisposeOnDisplayDispose) {
				display.addListener(SWT.Dispose, fDisposeListener);
			}
		}
		
		Color color = colorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			colorTable.put(rgb, color);
		}
		return color;
	}
	
	public Color getColor(final String key) {
		if (key == null)
			return null;
		
		final RGB rgb = fKeyTable.get(key);
		return getColor(rgb);
	}
	
	public void bindColor(final String key, final RGB rgb) {
		fKeyTable.put(key, rgb);
	}
	
}
