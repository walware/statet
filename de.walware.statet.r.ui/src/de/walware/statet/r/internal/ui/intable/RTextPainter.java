/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.intable;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.AbstractTextPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignment;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


public class RTextPainter extends AbstractTextPainter {
	
	
	/**
	 * Convert the data value of the cell using the {@link IDisplayConverter} from the {@link IConfigRegistry}
	 */
	private static Object getData(final ILayerCell cell, final IConfigRegistry configRegistry) {
		final IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, cell.getDisplayMode(),
				cell.getConfigLabels().getLabels() );
		return (displayConverter != null) ?
				displayConverter.canonicalToDisplayValue(cell, configRegistry, cell.getDataValue()) : EMPTY;
	}
	
	
	private final StringBuilder tempText = new StringBuilder();
	
	private String currentText;
	private int currentTextWidth;
	
	
	public RTextPainter(final int space) {
		super(false, true, space, false, false, SWT.DRAW_TRANSPARENT);
	}
	
	
	@Override
	public int getPreferredWidth(final ILayerCell cell, final GC gc, final IConfigRegistry configRegistry){
		final Object data = getData(cell, configRegistry);
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry), data);
		return getWidthFromCache(gc, data.toString()) + (spacing * 2);
	}
	
	@Override
	public int getPreferredHeight(final ILayerCell cell, final GC gc, final IConfigRegistry configRegistry) {
		final Object data = getData(cell, configRegistry);
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry), data);
//		return gc.textExtent(data.toString(), swtDrawStyle).y;
		return gc.getFontMetrics().getHeight();
	}
	
	protected void setupGCFromConfig(final GC gc, final IStyle cellStyle, final Object data) {
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		if (fg == null) {
			fg = GUIHelper.COLOR_LIST_FOREGROUND;
		}
//		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
//		if (bg == null) {
//			bg = GUIHelper.COLOR_LIST_BACKGROUND;
//		}
		Font font;
		if ((!(data instanceof InfoString)) || 
				(font = cellStyle.getAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE)) == null) {
			font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);
		}
		
//		gc.setAntialias(SWT.DEFAULT);
		gc.setTextAntialias(SWT.DEFAULT);
		gc.setFont(font);
		gc.setForeground(fg);
//		gc.setBackground(bg);
	}
	
	@Override
	public void paintCell(final ILayerCell cell, final GC gc, final Rectangle rectangle, final IConfigRegistry configRegistry) {
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
//		if (text.length() > width * 4) {
//			text = text.substring(0, width * 4);
//		}
		if (gc.getFont() == null) {
			gc.setFont(null);
		}
		text = getTextToDisplay(cell, gc, width, text);
		final int contentWidth = getWidthFromCache(gc, text);
		final int contentHeight = gc.getFontMetrics().getHeight();
		
		gc.drawText(
				text,
				rectangle.x + spacing + CellStyleUtil.getHorizontalAlignmentPadding((data instanceof InfoString) ?
						HorizontalAlignment.LEFT :
						cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT),
						width, contentWidth ),
				rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(VerticalAlignmentEnum.MIDDLE,
						rectangle, contentHeight ),
				swtDrawStyle );
		
		gc.setClipping(originalClipping);
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
