/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.intable;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.BackgroundPainter;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.style.VerticalAlignmentEnum;
import net.sourceforge.nattable.util.GUIHelper;


public class RTextPainter extends BackgroundPainter {
	
	
	public static final String EMPTY = "";
	public static final char DOT = '\u2026';
	
	
	private static final class TemporaryMapKey {
		
		private final String text;
		private final Font font;
		
		public TemporaryMapKey(final String text, final Font font) {
			this.text = text;
			this.font = font;
		}
		
		
		@Override
		public int hashCode() {
			return text.hashCode() + font.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TemporaryMapKey)) {
				return false;
			}
			final TemporaryMapKey other = (TemporaryMapKey) obj;
			return (text.equals(other.text) && font.equals(other.font));
		}
		
	}
	
	
	private static final Map<TemporaryMapKey, Integer> temporaryMap = new WeakHashMap<TemporaryMapKey, Integer>();
	
	
	private final boolean paintBg;
	private final int spacing = 3;
	
	private final StringBuilder tempText = new StringBuilder();
	
	private String currentText;
	private int currentTextWidth;
	
	
	public RTextPainter() {
		this(true);
	}
	
	/**
	 * @param paintBg skips painting the background if is FALSE
	 */
	public RTextPainter(final boolean paintBg) {
		this(paintBg, 0);
	}
	
	/**
	 * @param paintBg skips painting the background if is FALSE
	 * @param spacing
	 */
	public RTextPainter(final boolean paintBg, final int spacing) {
		this.paintBg = paintBg;
	}
	
	
	@Override
	public int getPreferredWidth(final LayerCell cell, final GC gc, final IConfigRegistry configRegistry){
		final Object data = getData(cell, configRegistry);
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry), data);
		return getWidthFromCache(gc, data.toString()) + (spacing * 2);
	}
	
	@Override
	public int getPreferredHeight(final LayerCell cell, final GC gc, final IConfigRegistry configRegistry) {
		final Object data = getData(cell, configRegistry);
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry), data);
		return gc.textExtent(data.toString()).y;
	}
	
	/**
	 * Convert the data value of the cell using the {@link IDisplayConverter} from the {@link IConfigRegistry}
	 */
	private Object getData(final LayerCell cell, final IConfigRegistry configRegistry) {
		final IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, cell.getDisplayMode(),
				cell.getConfigLabels().getLabels() );
		return (displayConverter != null) ?
				displayConverter.canonicalToDisplayValue(cell.getDataValue()) : EMPTY;
	}
	
	public void setupGCFromConfig(final GC gc, final IStyle cellStyle, final Object data) {
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		if (fg == null) {
			fg = GUIHelper.COLOR_LIST_FOREGROUND;
		}
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		if (bg == null) {
			bg = GUIHelper.COLOR_LIST_BACKGROUND;
		}
		Font font;
		if ((!(data instanceof InfoString)) || 
				(font = cellStyle.getAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE)) == null) {
			font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);
		}
		
		gc.setAntialias(SWT.DEFAULT);
		gc.setTextAntialias(SWT.DEFAULT);
		gc.setFont(font);
		gc.setForeground(fg);
		gc.setBackground(bg);
	}
	
	@Override
	public void paintCell(final LayerCell cell, final GC gc, final Rectangle rectangle, final IConfigRegistry configRegistry) {
		if (paintBg) {
			super.paintCell(cell, gc, rectangle, configRegistry);
		}
		
		final Rectangle originalClipping = gc.getClipping();
		gc.setClipping(rectangle.intersection(originalClipping));
		
		final Object data = getData(cell, configRegistry);
		final IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle, data);
		
		// Draw Text
		String text = data.toString();
		final int width = rectangle.width - (spacing * 2);
		if (text.length() > width * 4) {
			text = text.substring(0, width * 4);
		}
		if (gc.getFont() == null) {
			gc.setFont(null);
		}
		createCurrentTextToDisplay(gc, width, text);
		final int contentWidth = (currentText != text) ? width : currentTextWidth;
		final int contentHeight = gc.getFontMetrics().getHeight();
		
		gc.drawText(
				currentText,
				rectangle.x + spacing + getHorizontalAlignmentPadding((data instanceof InfoString) ?
						HorizontalAlignmentEnum.LEFT :
						cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT),
						width, contentWidth ),
				rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(VerticalAlignmentEnum.MIDDLE,
						rectangle, contentHeight ),
				SWT.DRAW_TRANSPARENT
		);
		
		gc.setClipping(originalClipping);
	}
	
	public static int getHorizontalAlignmentPadding(HorizontalAlignmentEnum horizontalAlignment, final int width, final int contentWidth) {
		if (horizontalAlignment == null) {
			horizontalAlignment = HorizontalAlignmentEnum.CENTER;
		}
		switch (horizontalAlignment) {
		case CENTER:
			return (width - contentWidth) / 2;
		case RIGHT:
			return (width - contentWidth);
		default:
			return 0;
		}
	}
	
	private int getWidthFromCache(final GC gc, final String text) {
		final TemporaryMapKey key = new TemporaryMapKey(text, gc.getFont());
		Integer width = temporaryMap.get(key);
		if (width == null) {
			width = Integer.valueOf(gc.textExtent(text, SWT.DRAW_TRANSPARENT).x);
			temporaryMap.put(key, width);
		}
		return width.intValue();
	}
	
	private void createCurrentTextToDisplay(final GC gc, final int width, final String originalText) {
		int textWidth = getWidthFromCache(gc, originalText);
		if (textWidth <= width + 1) {
			currentText = originalText;
			currentTextWidth = textWidth;
			return;
		}
		
		int textLength = originalText.length();
		int tooLong;
		String shortedText;
		do {
			tooLong = textLength;
			textLength -= Math.max((width + 1 - textWidth) / gc.getFontMetrics().getAverageCharWidth(), 1 );
			if (textLength <= 0) {
				textLength = 0;
				shortedText = EMPTY;
				break;
			}
			tempText.setLength(0);
			tempText.append(originalText.substring(0, textLength));
			tempText.append(DOT);
			shortedText = tempText.toString();
			textWidth = getWidthFromCache(gc, shortedText);
		} while (textWidth > width + 1);
		
		currentText = shortedText;
		currentTextWidth = textWidth;
		while (++textLength < tooLong) {
			tempText.setLength(0);
			tempText.append(originalText.substring(0, textLength));
			tempText.append(DOT);
			shortedText = tempText.toString();
			textWidth = getWidthFromCache(gc, shortedText);
			if (textWidth <= width + 1) {
				currentText = shortedText;
				currentTextWidth = textWidth;
			}
			else {
				return;
			}
		}
	}
	
}
