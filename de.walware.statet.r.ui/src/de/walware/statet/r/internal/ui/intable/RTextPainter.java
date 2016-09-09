/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.intable;

import static de.walware.ecommons.waltable.painter.cell.GraphicsUtils.safe;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;

import de.walware.ecommons.waltable.config.CellConfigAttributes;
import de.walware.ecommons.waltable.config.IConfigRegistry;
import de.walware.ecommons.waltable.coordinate.LRectangle;
import de.walware.ecommons.waltable.data.ControlData;
import de.walware.ecommons.waltable.data.convert.IDisplayConverter;
import de.walware.ecommons.waltable.layer.cell.ILayerCell;
import de.walware.ecommons.waltable.painter.cell.AbstractTextPainter;
import de.walware.ecommons.waltable.style.CellStyleAttributes;
import de.walware.ecommons.waltable.style.CellStyleUtil;
import de.walware.ecommons.waltable.style.HorizontalAlignment;
import de.walware.ecommons.waltable.style.IStyle;
import de.walware.ecommons.waltable.style.VerticalAlignmentEnum;
import de.walware.ecommons.waltable.swt.SWTUtil;
import de.walware.ecommons.waltable.util.GUIHelper;


public class RTextPainter extends AbstractTextPainter {
	
	
	/**
	 * Convert the data value of the cell using the {@link IDisplayConverter} from the {@link IConfigRegistry}
	 */
	private static Object getData(final ILayerCell cell, final IConfigRegistry configRegistry) {
		final IDisplayConverter displayConverter= configRegistry.getConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, cell.getDisplayMode(),
				cell.getConfigLabels().getLabels() );
		return (displayConverter != null) ?
				displayConverter.canonicalToDisplayValue(cell, configRegistry, cell.getDataValue(0, null)) : EMPTY;
	}
	
	
	private final StringBuilder tempText= new StringBuilder();
	
	private String currentText;
	private int currentTextWidth;
	
	
	public RTextPainter(final int space) {
		super(false, true, space, false, false, SWT.DRAW_TRANSPARENT);
	}
	
	
	@Override
	public long getPreferredWidth(final ILayerCell cell, final GC gc, final IConfigRegistry configRegistry){
		final Object data= getData(cell, configRegistry);
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry), data);
		return getWidthFromCache(gc, data.toString()) + (this.spacing * 2);
	}
	
	@Override
	public long getPreferredHeight(final ILayerCell cell, final GC gc, final IConfigRegistry configRegistry) {
		final Object data= getData(cell, configRegistry);
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry), data);
//		return gc.textExtent(data.toString(), swtDrawStyle).y;
		return gc.getFontMetrics().getHeight();
	}
	
	protected void setupGCFromConfig(final GC gc, final IStyle cellStyle, final Object data) {
		Color fg= cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		if (fg == null) {
			fg= GUIHelper.COLOR_LIST_FOREGROUND;
		}
//		Color bg= cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
//		if (bg == null) {
//			bg= GUIHelper.COLOR_LIST_BACKGROUND;
//		}
		Font font;
		if ((!(data instanceof ControlData)) || 
				(font= cellStyle.getAttributeValue(CellStyleAttributes.CONTROL_FONT)) == null) {
			font= cellStyle.getAttributeValue(CellStyleAttributes.FONT);
		}
		
//		gc.setAntialias(SWT.DEFAULT);
		gc.setTextAntialias(SWT.DEFAULT);
		gc.setFont(font);
		gc.setForeground(fg);
//		gc.setBackground(bg);
	}
	
	@Override
	public void paintCell(final ILayerCell cell, final GC gc, final LRectangle lRectangle,
			final IConfigRegistry configRegistry) {
		if (this.paintBg) {
			super.paintCell(cell, gc, lRectangle, configRegistry);
		}
		
		final org.eclipse.swt.graphics.Rectangle originalClipping= gc.getClipping();
		gc.setClipping(SWTUtil.toSWT(lRectangle).intersection(originalClipping));
		
		final Object data= getData(cell, configRegistry);
		final IStyle cellStyle= CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle, data);
		
		// Draw Text
		String text= data.toString();
		final long width= lRectangle.width - (this.spacing * 2);
//		if (text.length() > width * 4) {
//			text= text.substring(0, width * 4);
//		}
		if (gc.getFont() == null) {
			gc.setFont(null);
		}
		// first get height because https://bugs.eclipse.org/bugs/show_bug.cgi?id=319125
		final int contentHeight= gc.getFontMetrics().getHeight();
		text= getTextToDisplay(cell, gc, width, text);
		final int contentWidth= getWidthFromCache(gc, text);
		
		gc.drawText(
				text,
				safe(lRectangle.x + this.spacing + CellStyleUtil.getHorizontalAlignmentPadding(
						(data instanceof ControlData) ?
								HorizontalAlignment.LEFT :
								cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT),
						width, contentWidth )),
				safe(lRectangle.y + CellStyleUtil.getVerticalAlignmentPadding(VerticalAlignmentEnum.MIDDLE,
						lRectangle.height, contentHeight )),
				this.swtDrawStyle );
		
		gc.setClipping(originalClipping);
	}
	
	private void createCurrentTextToDisplay(final GC gc, final int width, final String originalText) {
		int textWidth= getWidthFromCache(gc, originalText);
		if (textWidth <= width + 1) {
			this.currentText= originalText;
			this.currentTextWidth= textWidth;
			return;
		}
		
		int textLength= originalText.length();
		int tooLong;
		String shortedText;
		do {
			tooLong= textLength;
			textLength -= Math.max((width + 1 - textWidth) / gc.getFontMetrics().getAverageCharWidth(), 1 );
			if (textLength <= 0) {
				textLength= 0;
				shortedText= EMPTY;
				break;
			}
			this.tempText.setLength(0);
			this.tempText.append(originalText.substring(0, textLength));
			this.tempText.append(DOT);
			shortedText= this.tempText.toString();
			textWidth= getWidthFromCache(gc, shortedText);
		} while (textWidth > width + 1);
		
		this.currentText= shortedText;
		this.currentTextWidth= textWidth;
		while (++textLength < tooLong) {
			this.tempText.setLength(0);
			this.tempText.append(originalText.substring(0, textLength));
			this.tempText.append(DOT);
			shortedText= this.tempText.toString();
			textWidth= getWidthFromCache(gc, shortedText);
			if (textWidth <= width + 1) {
				this.currentText= shortedText;
				this.currentTextWidth= textWidth;
			}
			else {
				return;
			}
		}
	}

}
