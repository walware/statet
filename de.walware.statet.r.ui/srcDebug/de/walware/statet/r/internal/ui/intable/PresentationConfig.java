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

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

import net.sourceforge.nattable.config.AbstractRegistryConfiguration;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.freeze.IFreezeConfigAttributes;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.decorator.ExtLineBorderDecorator;
import net.sourceforge.nattable.painter.cell.decorator.LineBorderDecorator;
import net.sourceforge.nattable.painter.layer.GridLineCellLayerPainter;
import net.sourceforge.nattable.painter.layer.ILayerPainter;
import net.sourceforge.nattable.sort.SortHeaderLayer;
import net.sourceforge.nattable.sort.painter.SortableHeaderTextPainter;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.BorderStyle.LineStyleEnum;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.SelectionStyleLabels;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.VerticalAlignmentEnum;


public class PresentationConfig extends AbstractRegistryConfiguration {
	
	
	private final Color fHeaderGridColor;
	private final Color fHeaderBackgroundColor;
	private final Color fHeaderForegroundColor;
	
	private final Color fBodyGridColor;
	private final Color fBodyBackgroundColor;
	private final Color fBodyEvenRowBackgroundColor;
	private final Color fBodyOddRowBackgroundColor;
	private final Color fBodyForegroundColor;
	
	private final Color fHeaderSelectionBackgroundColor;
	private final Color fHeaderSelectionForegroundColor;
	
	private final Color fHeaderFullSelectionBackgroundColor;
	private final Color fHeaderFullSelectionForegroundColor;
	
	private final Color fBodySelectionBackgroundColor;
	private final Color fBodySelectionForegroundColor;;
	
	private final Color fBodyFreezeSeparatorColor;
	
	private final ILayerPainter fHeaderLayerPainter;
	
	private final ICellPainter fBaseCellPainter;
	
	private final ICellPainter fHeaderCellPainter;
	private final ICellPainter fHeaderSortedCellPainter;
	
	private final ICellPainter fBodyAnchorCellPainter;
	
	private final BorderStyle fBodyAnchorBorderStyle;
	
	private final Font fBaseFont;
	private final Font fInfoFont;
	
	
	public PresentationConfig(final Display display) {
		final FontRegistry jFaceFontRegistry = JFaceResources.getFontRegistry();
		final ColorRegistry jFaceColorRegistry = JFaceResources.getColorRegistry();
		
		fHeaderGridColor = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		fHeaderBackgroundColor = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		fHeaderForegroundColor = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
		
		fBodyGridColor = display.getSystemColor(SWT.COLOR_GRAY);
		fBodyBackgroundColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		fBodyEvenRowBackgroundColor = fBodyBackgroundColor;
		fBodyOddRowBackgroundColor = new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW).getRGB(),
				fBodyEvenRowBackgroundColor.getRGB(), 20 ));
		
		fBodyForegroundColor = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		
		fHeaderSelectionBackgroundColor = new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(),
				fHeaderBackgroundColor.getRGB(), 25 ));
		fHeaderSelectionForegroundColor = fHeaderForegroundColor;
		
		fHeaderFullSelectionBackgroundColor = new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(),
				display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW).getRGB(), 25 ));
		fHeaderFullSelectionForegroundColor = display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		
		fBodySelectionBackgroundColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		fBodySelectionForegroundColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		
		fBodyFreezeSeparatorColor = jFaceColorRegistry.get(JFacePreferences.DECORATIONS_COLOR);
		
		fHeaderLayerPainter = new GridLineCellLayerPainter(fHeaderGridColor);
		
		fBaseCellPainter = new LineBorderDecorator(new RTextPainter());
		
		fHeaderCellPainter = new RTextPainter();
		fHeaderSortedCellPainter = new SortableHeaderTextPainter(new RTextPainter(), true);
		
		fBodyAnchorCellPainter = new ExtLineBorderDecorator(new RTextPainter());
		
		fBodyAnchorBorderStyle = new BorderStyle(1, fBodyForegroundColor, LineStyleEnum.SOLID);
		
		fBaseFont = jFaceFontRegistry.get(JFaceResources.DIALOG_FONT);
		fInfoFont = jFaceFontRegistry.getItalic(JFaceResources.DIALOG_FONT);
	}
	
	
	public ILayerPainter getHeaderLayerPainter() {
		return fHeaderLayerPainter;
	}
	
	public Font getBaseFont() {
		return fBaseFont;
	}
	
	
	public void dispose() {
		fBodyOddRowBackgroundColor.dispose();
		fHeaderSelectionBackgroundColor.dispose();
		fHeaderFullSelectionBackgroundColor.dispose();
	}
	
	
	@Override
	public void configureRegistry(final IConfigRegistry configRegistry) {
		// base
		{	final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodyBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fBodyForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fBaseCellPainter);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);
		}
		
		// headers
		{	// column header
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.CORNER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCellPainter, DisplayMode.NORMAL, GridRegion.CORNER);
		}
		{	// row header
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCellPainter,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER);
		}
		
		// alternating rows
		{	// body even row
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodyEvenRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
		}
		{	// body odd row
			final Style cellStyle = new Style();
			
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodyOddRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
		}
		
		// selection
		{	// body selected cell
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodySelectionBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fBodySelectionForegroundColor);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT);
		}
		{	// body selection anchor
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, fBodyAnchorBorderStyle);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					fBodyAnchorCellPainter,
					DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
		}
		{	// header with selection
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					fHeaderSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					fHeaderSelectionBackgroundColor );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.CORNER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.ROW_HEADER);
		}
		{	// header fully selected
			final Style cellStyle = new Style();
			
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					fHeaderFullSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					fHeaderFullSelectionBackgroundColor );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
		}
		
		// sorting
		{	// header column sorted
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					fHeaderSortedCellPainter,
					DisplayMode.NORMAL, SortHeaderLayer.SORT_DOWN_CONFIG_TYPE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					fHeaderSortedCellPainter,
					DisplayMode.NORMAL, SortHeaderLayer.SORT_UP_CONFIG_TYPE);
		}
		
		// freezing
		{	// body freezed
			configRegistry.registerConfigAttribute(IFreezeConfigAttributes.SEPARATOR_COLOR,
					fBodyFreezeSeparatorColor );
		}
		
	}
	
}
