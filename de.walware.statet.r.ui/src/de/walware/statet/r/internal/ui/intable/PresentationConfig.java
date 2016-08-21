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

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.waltable.config.AbstractRegistryConfiguration;
import de.walware.ecommons.waltable.config.CellConfigAttributes;
import de.walware.ecommons.waltable.config.IConfigRegistry;
import de.walware.ecommons.waltable.config.LayoutSizeConfig;
import de.walware.ecommons.waltable.freeze.IFreezeConfigAttributes;
import de.walware.ecommons.waltable.grid.GridRegion;
import de.walware.ecommons.waltable.grid.cell.AlternatingRowConfigLabelAccumulator;
import de.walware.ecommons.waltable.grid.labeled.CornerGridLineCellLayerPainter;
import de.walware.ecommons.waltable.painter.cell.DiagCellPainter;
import de.walware.ecommons.waltable.painter.cell.ICellPainter;
import de.walware.ecommons.waltable.painter.cell.decorator.LineBorderDecorator;
import de.walware.ecommons.waltable.painter.layer.GridLineCellLayerPainter;
import de.walware.ecommons.waltable.painter.layer.ILayerPainter;
import de.walware.ecommons.waltable.sort.config.DefaultSortConfiguration;
import de.walware.ecommons.waltable.sort.painter.SortableHeaderTextPainter;
import de.walware.ecommons.waltable.style.BorderStyle;
import de.walware.ecommons.waltable.style.BorderStyle.LineStyle;
import de.walware.ecommons.waltable.style.CellStyleAttributes;
import de.walware.ecommons.waltable.style.DisplayMode;
import de.walware.ecommons.waltable.style.HorizontalAlignment;
import de.walware.ecommons.waltable.style.SelectionStyleLabels;
import de.walware.ecommons.waltable.style.Style;
import de.walware.ecommons.waltable.style.VerticalAlignmentEnum;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class PresentationConfig extends AbstractRegistryConfiguration implements IDisposable {
	
	
	private static PresentationConfig gInstance;
	
	public static PresentationConfig getInstance(final Display display) {
		if (gInstance == null) {
			gInstance= new PresentationConfig(display);
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
		final FontRegistry jFaceFontRegistry= JFaceResources.getFontRegistry();
		final ColorRegistry jFaceColorRegistry= JFaceResources.getColorRegistry();
		
		this.fHeaderGridColor= display.getSystemColor(SWT.COLOR_DARK_GRAY);
		this.fHeaderBackgroundColor= display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		this.fHeaderForegroundColor= display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
		
		this.fBodyGridColor= display.getSystemColor(SWT.COLOR_GRAY);
		this.fBodyBackgroundColor= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		this.fBodyEvenRowBackgroundColor= this.fBodyBackgroundColor;
		this.fBodyOddRowBackgroundColor= new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW).getRGB(),
				this.fBodyEvenRowBackgroundColor.getRGB(), 20 ));
		
		this.fBodyForegroundColor= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		
		this.fHeaderSelectionBackgroundColor= new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(),
				this.fHeaderBackgroundColor.getRGB(), 25 ));
		this.fHeaderSelectionForegroundColor= this.fHeaderForegroundColor;
		
		this.fHeaderPlaceholderColor= new Color(display, ColorUtil.blend(
				this.fBodyGridColor.getRGB(),
				this.fHeaderBackgroundColor.getRGB(), 25 ));
		
		this.fHeaderFullSelectionBackgroundColor= new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(),
				display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW).getRGB(), 25 ));
		this.fHeaderFullSelectionForegroundColor= display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		
		this.fBodySelectionBackgroundColor= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		this.fBodySelectionForegroundColor= display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		
		this.fBodyFreezeSeparatorColor= jFaceColorRegistry.get(JFacePreferences.DECORATIONS_COLOR);
		
		this.fHeaderLayerPainter= new GridLineCellLayerPainter(this.fHeaderGridColor);
		this.fHeaderLabelLayerPainter= new CornerGridLineCellLayerPainter(this.fHeaderGridColor);
		
		this.fBodyAnchorBorderStyle= new BorderStyle(2, this.fBodyForegroundColor, LineStyle.SOLID, -1);
		
		this.fBaseFont= jFaceFontRegistry.get(JFaceResources.DIALOG_FONT);
		this.fInfoFont= jFaceFontRegistry.getItalic(JFaceResources.DIALOG_FONT);
		
		{	final GC gc= new GC(display);
			gc.setFont(this.fBaseFont);
			final FontMetrics fontMetrics= gc.getFontMetrics();
			final int textHeight= fontMetrics.getHeight();
			final int charWidth= (gc.textExtent("1234567890.-120").x + 5) / 15;
			gc.dispose();
			final int textSpace= 3;
			
			this.fBaseSizeConfig= new LayoutSizeConfig(textSpace, textHeight, charWidth);
		}
		
		this.fBaseCellPainter= new LineBorderDecorator(
				new RTextPainter(this.fBaseSizeConfig.getDefaultSpace()) );
		
		this.fHeaderCellPainter= new RTextPainter(this.fBaseSizeConfig.getDefaultSpace());
		this.fHeaderSortedCellPainter= new SortableHeaderTextPainter(
				new RTextPainter(this.fBaseSizeConfig.getDefaultSpace()), true, true );
		this.fHeaderCornerCellPainter= new DiagCellPainter(this.fHeaderGridColor);
	}
	
	
	public ILayerPainter getHeaderLayerPainter() {
		return this.fHeaderLayerPainter;
	}
	
	public ILayerPainter getHeaderLabelLayerPainter() {
		return this.fHeaderLabelLayerPainter;
	}
	
	public LayoutSizeConfig getBaseSizeConfig() {
		return this.fBaseSizeConfig;
	}
	
	
	@Override
	public void dispose() {
		this.fBodyOddRowBackgroundColor.dispose();
		this.fHeaderSelectionBackgroundColor.dispose();
		this.fHeaderFullSelectionBackgroundColor.dispose();
		this.fHeaderPlaceholderColor.dispose();
	}
	
	
	@Override
	public void configureRegistry(final IConfigRegistry configRegistry) {
		// base
		{	final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fBodyBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.fBodyForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.fBaseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.fBaseCellPainter);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);
			
			configRegistry.registerConfigAttribute(LayoutSizeConfig.CONFIG, this.fBaseSizeConfig);
		}
		
		// headers
		{	// column header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.CENTER);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.fBaseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.fHeaderCellPainter,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER );
		}
		{	// column header label
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.fBaseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER_LABEL );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.fHeaderCellPainter,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER_LABEL );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.CORNER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.fHeaderCornerCellPainter,
					DisplayMode.NORMAL, GridRegion.CORNER );
		}
		{	// row header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.fBaseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.fHeaderCellPainter,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER );
		}
		{	// row header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fHeaderBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.LEFT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.fBaseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.fInfoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER_LABEL );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.fHeaderCellPainter,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER_LABEL );
		}
		{	// placeholder header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fHeaderPlaceholderColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.fHeaderForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.fBaseFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.HEADER_PLACEHOLDER );
		}
		
		// alternating rows
		{	// body even row
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fBodyEvenRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE );
		}
		{	// body odd row
			final Style cellStyle= new Style();
			
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fBodyOddRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE );
		}
		
		// selection
		{	// body selected cell
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.fBodySelectionBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.fBodySelectionForegroundColor);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT );
		}
		{	// body selection anchor
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, this.fBodyAnchorBorderStyle);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, SelectionStyleLabels.SELECTION_ANCHOR_STYLE );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE );
		}
		{	// header with selection
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					this.fHeaderSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					this.fHeaderSelectionBackgroundColor );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.COLUMN_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.CORNER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, GridRegion.ROW_HEADER );
		}
		{	// header fully selected
			final Style cellStyle= new Style();
			
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					this.fHeaderFullSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					this.fHeaderFullSelectionBackgroundColor );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE );
		}
		
		// sorting
		{	// header column sorted
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					this.fHeaderSortedCellPainter,
					DisplayMode.NORMAL, DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					this.fHeaderSortedCellPainter,
					DisplayMode.NORMAL, DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
		}
		
		// freezing
		{	// body freezed
			configRegistry.registerConfigAttribute(IFreezeConfigAttributes.SEPARATOR_COLOR,
					this.fBodyFreezeSeparatorColor );
		}
		
	}
	
}
