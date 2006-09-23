/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.dialogs;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;


/**
 * Replacement for CLabel.
 * 
 * While CLabel is inflexible, use this class instead. Features:
 *  - No border (customisable)
 *  - Better algorithm (faster and prettier).
 *  - Hover text.
 */
public class ShortedLabel {

	private static final Pattern fLineBreakPattern = Pattern.compile("\\r[\\n]?|\\n");
	
	
	private String fText;
	private String fCheckedText;
	private Label fLabel;
	private String fLineBreakReplacement = " ";
	
	
	public ShortedLabel(Composite parent, int style) {
		
		fLabel = new Label(parent, style);
		fLabel.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				updateShortening();
			}
		});
	}
	
	public Label getControl() {
		
		return fLabel;
	}
	
	public void setText(String label) {
		
		assert (label != null);
		
		if (label.equals(fText)) {
			return;
		}
		fText = label;
		updateChecking();
		updateShortening();
	}
	
	public void setLineBreakReplacement(String s) {
		
		assert (s != null);
		
		fLineBreakReplacement = s;
		if (fText != null) {
			updateChecking();
		}
	}
	
	private void updateChecking() {
		
		fCheckedText = fLineBreakPattern.matcher(fText).replaceAll(fLineBreakReplacement);
	}
	
	private void updateShortening() {
		
		String text = new Shorter(fLabel).shorten(fCheckedText);
		fLabel.setText(text);
		fLabel.setToolTipText((text == fCheckedText) ? null : fText);
	}
	
	
	private static class Shorter {
		
		private static final String ELLIPSIS = " ... ";
		private static final int DRAW_FLAGS = SWT.DRAW_TAB;

		Control fControl;
		GC fGC;
		int fMaxWidth;
		
		String fText;

		
		public Shorter(Control control) {
			
			fControl = control;
		}
		
		public String shorten(final String text) {
			
			if (text == null || text.length() == 0) {
				return text;
			}
			
			if (fGC == null) {
				fGC = new GC(fControl);
				fMaxWidth = fControl.getBounds().width;
			}
			if (fGC.textExtent(text, DRAW_FLAGS).x <= fMaxWidth) {
				return text;
			}
			
			fText = text;
			String shortedText = doShorten();
			fText = null;
			return shortedText;
		}
		
		private String doShorten() {
			
			int avgCharWidth = fGC.getFontMetrics().getAverageCharWidth();
			int textLength = fText.length();

			int ellipsisWidth = fGC.textExtent(ELLIPSIS, DRAW_FLAGS).x;

			int max2 = (fMaxWidth-ellipsisWidth) * 42 / 100;
			if (max2 < avgCharWidth*3) {
				max2 = 0;
			}
			int e = Math.max(textLength - max2/avgCharWidth, 0);
			int w2 = measurePart2(e);
			while (w2 > max2 && e < textLength) {
				w2 = measurePart2(e++);
			}
			while (e > 0) {
				int test = measurePart2(e-1);
				if (test <= max2) {
					e--;
					w2 = test;
					continue;
				}
				else {
					break;
				}
			}
			
			int max1 = fMaxWidth-ellipsisWidth-w2;
			int s = Math.min(max2/avgCharWidth, textLength);
			int w1 = measurePart1(s);
			while (w1 > max1 && s > 3) {
				w1 = measurePart1(s--);
			}
			while (s < textLength) {
				int test = measurePart1(s+1);
				if (test <= max1) {
					s++;
					w1 = test;
					continue;
				}
				else {
					break;
				}
			}

			return fText.substring(0, s)+ELLIPSIS+fText.substring(e, textLength);
		}

		private int measurePart1(int end) {
			
			return fGC.textExtent(fText.substring(0, end), DRAW_FLAGS).x; 
		}
		
		private int measurePart2(int start) {
			
			return fGC.textExtent(fText.substring(start), DRAW_FLAGS).x;
		}
	}
}

