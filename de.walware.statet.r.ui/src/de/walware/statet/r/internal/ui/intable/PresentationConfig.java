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

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.LayoutSizeConfig;
import org.eclipse.nebula.widgets.nattable.freeze.IFreezeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.labeled.CornerGridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.grid.labeled.LabelCornerLayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.DiagCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignment;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

import de.walware.ecommons.IDisposable;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class PresentationConfig extends AbstractRegistryConfiguration implements IDisposable {
	
	
	private static PresentationConfig gInstance;
	
	public static PresentationConfig getInstance(final Display display) {
		if (gInstance == null) {
			gInstance = new PresentationConfig(display);
			RUIPlugin.getDefault().registerPluginDisposable(gInstance);
		}
		return gInstance;
	}
	
	
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
	
	private final Color fHeaderPlaceholderColor;
	
	private final Color fHeaderFullSelectionBackgroundColor;
	private final Color fHeaderFullSelectionForegroundColor;
	
	private final Color fBodySelectionBackgroundColor;
	private final Color fBodySelectionForegroundColor;
	
	private final Color fBodyFreezeSeparatorColor;
	
	private final ILayerPainter fHeaderLayerPainter;
	private final ILayerPainter fHeaderLabelLayerPainter;
	
	private final ICellPainter fBaseCellPainter;
	
	private final ICellPainter fHeaderCellPainter;
	private final ICellPainter fHeaderSortedCellPainter;
	private final ICellPainter fHeaderCornerCellPainter;
	
	private final BorderStyle fBodyAnchorBorderStyle;
	
	private final Font fBaseFont;
	private final Font fInfoFont;
	
	private final LayoutSizeConfig fBaseSizeConfig;
	
	
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
		
		fHeaderPlaceholderColor = new Color(display, ColorUtil.blend(
				fBodyGridColor.getRGB(),
				fHeaderBackgroundColor.getRGB(), 25 ));
		
		fHeaderFullSelectionBackgroundColor = new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(),
				display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW).getRGB(), 25 ));
		fHeaderFullSelectionForegroundColor = display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		
		fBodySelectionBackgroundColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		fBodySelectionForegroundColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		
		fBodyFreezeSeparatorColor = jFaceColorRegistry.get(JFacePreferences.DECORATIONS_COLOR);
		
		fHeaderLayerPainter = new GridLineCellLayerPainter(fHeaderGridColor);
		fHeaderLabelLayerPainter = new CornerGridLineCellLayerPainter(fHeaderGridColor);
		
		fBodyAnchorBorderStyle = new BorderStyle(2, fBodyForegroundColor, LineStyle.SOLID, -1);
		
		fBaseFont = jFaceFontRegistry.get(JFaceResources.DIALOG_FONT);
		fInfoFont = jFaceFontRegistry.getItalic(JFaceResources.DIALOG_FONT);
		
		{	final GC gc = new GC(display);
			gc.setFont(fBaseFont);
			final FontMetrics fontMetrics = gc.getFontMetrics();
			final int textHeight = fontMetrics.getHeight();
			final int charWidth = (gc.textExtent("1234567890.-120").x + 5) / 15;
			gc.dispose();
			final int textSpace = 3;
			
			fBaseSizeConfig = new LayoutSizeConfig(textSpace, textHeight, charWidth);
		}
		
		fBaseCellPainter = new LineBorderDecorator(
				new RTextPainter(fBaseSizeConfig.getDefaultSpace()) );
		
		fHeaderCellPainter = new RTextPainter(fBaseSizeConfig.getDefaultSpace());
		fHeaderSortedCellPainter = new SortableHeaderTextPainter(
				new RTextPainter(fBaseSizeConfig.getDefaultSpace()), true, true );
		fHeaderCornerCellPainter = new DiagCellPainter(fHeaderGridColor);
	}
	
	
	public ILayerPainter getHeaderLayerPainter() {
		return fHeaderLayerPainter;
	}
	
	public ILayerPainter getHeaderLabelLayerPainter() {
		return fHeaderLabelLayerPainter;
	}
	
	public LayoutSizeConfig getBaseSizeConfig() {
		return fBaseSizeConfig;
	}
	
	
	@Override
	public void dispose() {
		fBodyOddRowBackgroundColor.dispose();
		fHeaderSelectionBackgroundColor.dispose();
		fHeaderFullSelectionBackgroundColor.dispose();
		fHeaderPlaceholderColor.dispose();
	}
	
	
	@Override
	public void configureRegistry(final IConfigRegistry configRegistry) {
		// base
		{	final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodyBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fBodyForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fBaseCellPainter);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);
			
			configRegistry.registerConfigAttribute(LayoutSizeConfig.CONFIG, fBaseSizeConfig);
		}
		
		// headers
		{	// column header
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.CENTER);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCellPainter,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER );
		}
		{	// column header label
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, LabelCornerLayer.COLUMN_HEADER_LABEL );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCellPainter,
					DisplayMode.NORMAL, LabelCornerLayer.COLUMN_HEADER_LABEL );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.CORNER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCornerCellPainter,
					DisplayMode.NORMAL, GridRegion.CORNER );
		}
		{	// row header
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCellPainter,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER );
		}
		{	// row header
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.LEFT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			cellStyle.setAttributeValue(InfoString.CELL_STYLE_FONT_ATTRIBUTE, fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, LabelCornerLayer.ROW_HEADER_LABEL );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, fHeaderCellPainter,
					DisplayMode.NORMAL, LabelCornerLayer.ROW_HEADER_LABEL );
		}
		{	// placeholder header
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fHeaderPlaceholderColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, fBaseFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, "PLACEHOLDER" );
		}
		
		// alternating rows
		{	// body even row
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodyEvenRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE );
		}
		{	// body odd row
			final Style cellStyle = new Style();
			
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodyOddRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE );
		}
		
		// selection
		{	// body selected cell
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, fBodySelectionBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fBodySelectionForegroundColor);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT );
		}
		{	// body selection anchor
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, fBodyAnchorBorderStyle);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, SelectionStyleLabels.SELECTION_ANCHOR_STYLE );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE );
		}
		{	// header with selection
			final Style cellStyle = new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					fHeaderSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					fHeaderSelectionBackgroundColor );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.COLUMN_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.CORNER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.ROW_HEADER );
		}
		{	// header fully selected
			final Style cellStyle = new Style();
			
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					fHeaderFullSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					fHeaderFullSelectionBackgroundColor );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE );
		}
		
		// sorting
		{	// header column sorted
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					fHeaderSortedCellPainter,
					DisplayMode.NORMAL, DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					fHeaderSortedCellPainter,
					DisplayMode.NORMAL, DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
		}
		
		// freezing
		{	// body freezed
			configRegistry.registerConfigAttribute(IFreezeConfigAttributes.SEPARATOR_COLOR,
					fBodyFreezeSeparatorColor );
		}
		
	}
	
}
