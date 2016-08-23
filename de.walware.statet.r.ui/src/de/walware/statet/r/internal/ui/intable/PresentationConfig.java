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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

import de.walware.jcommons.collections.CopyOnWriteIdentityListSet;

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

import de.walware.workbench.ui.IWaThemeConstants;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class PresentationConfig extends AbstractRegistryConfiguration implements IDisposable,
		IPropertyChangeListener {
	
	
	private static PresentationConfig gInstance;
	
	public static PresentationConfig getInstance(final Display display) {
		if (gInstance == null) {
			gInstance= new PresentationConfig(display);
			RUIPlugin.getDefault().registerPluginDisposable(gInstance);
		}
		return gInstance;
	}
	
	
	private final Display display;
	
	private final Color headerGridColor;
	private final Color headerBackgroundColor;
	private final Color headerForegroundColor;
	
	private final Color bodyGridColor;
	private final Color bodyBackgroundColor;
	private final Color bodyEvenRowBackgroundColor;
	private final Color bodyOddRowBackgroundColor;
	private final Color bodyForegroundColor;
	
	private final Color headerSelectionBackgroundColor;
	private final Color headerSelectionForegroundColor;
	
	private final Color headerPlaceholderColor;
	
	private final Color headerFullSelectionBackgroundColor;
	private final Color headerFullSelectionForegroundColor;
	
	private final Color bodySelectionBackgroundColor;
	private final Color bodySelectionForegroundColor;
	
	private final Color bodyFreezeSeparatorColor;
	
	private final ILayerPainter headerLayerPainter;
	private final ILayerPainter headerLabelLayerPainter;
	
	private ICellPainter baseCellPainter;
	
	private ICellPainter headerCellPainter;
	private ICellPainter headerSortedCellPainter;
	private ICellPainter headerCornerCellPainter;
	
	private final BorderStyle bodyAnchorBorderStyle;
	
	private Font baseFont;
	private Font infoFont;
	
	private LayoutSizeConfig baseSizeConfig;
	
	private final CopyOnWriteIdentityListSet<Runnable> listeners= new CopyOnWriteIdentityListSet<>();
	
	
	public PresentationConfig(final Display display) {
		this.display= display;
		
		final ColorRegistry colorRegistry= JFaceResources.getColorRegistry();
		
		this.headerGridColor= display.getSystemColor(SWT.COLOR_DARK_GRAY);
		this.headerBackgroundColor= display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		this.headerForegroundColor= display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
		
		this.bodyGridColor= display.getSystemColor(SWT.COLOR_GRAY);
		this.bodyBackgroundColor= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		this.bodyEvenRowBackgroundColor= this.bodyBackgroundColor;
		this.bodyOddRowBackgroundColor= new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW).getRGB(),
				this.bodyEvenRowBackgroundColor.getRGB(), 20 ));
		
		this.bodyForegroundColor= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		
		this.headerSelectionBackgroundColor= new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(),
				this.headerBackgroundColor.getRGB(), 25 ));
		this.headerSelectionForegroundColor= this.headerForegroundColor;
		
		this.headerPlaceholderColor= new Color(display, ColorUtil.blend(
				this.bodyGridColor.getRGB(),
				this.headerBackgroundColor.getRGB(), 25 ));
		
		this.headerFullSelectionBackgroundColor= new Color(display, ColorUtil.blend(
				display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(),
				display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW).getRGB(), 25 ));
		this.headerFullSelectionForegroundColor= display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		
		this.bodySelectionBackgroundColor= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		this.bodySelectionForegroundColor= display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		
		this.bodyFreezeSeparatorColor= colorRegistry.get(JFacePreferences.DECORATIONS_COLOR);
		
		this.headerLayerPainter= new GridLineCellLayerPainter(this.headerGridColor);
		this.headerLabelLayerPainter= new CornerGridLineCellLayerPainter(this.headerGridColor);
		
		this.bodyAnchorBorderStyle= new BorderStyle(2, this.bodyForegroundColor, LineStyle.SOLID, -1);
		
		updateFonts();
		updateCellPainters();
		
		final FontRegistry fontRegistry= JFaceResources.getFontRegistry();
		fontRegistry.addListener(this);
	}
	
	@Override
	public void dispose() {
		final FontRegistry fontRegistry= JFaceResources.getFontRegistry();
		if (fontRegistry != null) {
			fontRegistry.removeListener(this);
		}
		
		this.bodyOddRowBackgroundColor.dispose();
		this.headerSelectionBackgroundColor.dispose();
		this.headerFullSelectionBackgroundColor.dispose();
		this.headerPlaceholderColor.dispose();
	}
	
	
	private void updateFonts() {
		final FontRegistry fontRegistry= JFaceResources.getFontRegistry();
		this.baseFont= fontRegistry.get(IWaThemeConstants.TABLE_FONT);
		this.infoFont= fontRegistry.getItalic(IWaThemeConstants.TABLE_FONT);
		
		{	final GC gc= new GC(this.display);
			gc.setFont(this.baseFont);
			final FontMetrics fontMetrics= gc.getFontMetrics();
			final int textHeight= fontMetrics.getHeight();
			final int charWidth= (gc.textExtent("1234567890.-120").x + 5) / 15;
			gc.dispose();
			final int textSpace= 3;
			
			this.baseSizeConfig= new LayoutSizeConfig(textSpace, textHeight, charWidth);
		}
	}
	
	private void updateCellPainters() {
		this.baseCellPainter= new LineBorderDecorator(
				new RTextPainter(this.baseSizeConfig.getDefaultSpace()) );
		
		this.headerCellPainter= new RTextPainter(this.baseSizeConfig.getDefaultSpace());
		this.headerSortedCellPainter= new SortableHeaderTextPainter(
				new RTextPainter(this.baseSizeConfig.getDefaultSpace()), true, true );
		this.headerCornerCellPainter= new DiagCellPainter(this.headerGridColor);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		switch (event.getProperty()) {
		case IWaThemeConstants.TABLE_FONT:
			updateFonts();
			updateCellPainters();
			notifyListeners();
			return;
		default:
			return;
		}
	}
	
	public ILayerPainter getHeaderLayerPainter() {
		return this.headerLayerPainter;
	}
	
	public ILayerPainter getHeaderLabelLayerPainter() {
		return this.headerLabelLayerPainter;
	}
	
	public LayoutSizeConfig getBaseSizeConfig() {
		return this.baseSizeConfig;
	}
	
	
	@Override
	public void configureRegistry(final IConfigRegistry configRegistry) {
		// base
		{	final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.bodyBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.bodyForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.baseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.infoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.baseCellPainter);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);
			
			configRegistry.registerConfigAttribute(LayoutSizeConfig.CONFIG, this.baseSizeConfig);
		}
		
		// headers
		{	// column header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.headerBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.headerForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.CENTER);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.baseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.infoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.headerCellPainter,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER );
		}
		{	// column header label
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.headerBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.headerForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.baseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.infoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER_LABEL );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.headerCellPainter,
					DisplayMode.NORMAL, GridRegion.COLUMN_HEADER_LABEL );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.CORNER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.headerCornerCellPainter,
					DisplayMode.NORMAL, GridRegion.CORNER );
		}
		{	// row header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.headerBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.headerForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.baseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.infoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.headerCellPainter,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER );
		}
		{	// row header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.headerBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.headerForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.LEFT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.baseFont);
			cellStyle.setAttributeValue(CellStyleAttributes.CONTROL_FONT, this.infoFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER_LABEL );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.headerCellPainter,
					DisplayMode.NORMAL, GridRegion.ROW_HEADER_LABEL );
		}
		{	// placeholder header
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.headerPlaceholderColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.headerForegroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignment.RIGHT);
			cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, null);
			cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.baseFont);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, GridRegion.HEADER_PLACEHOLDER );
		}
		
		// alternating rows
		{	// body even row
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.bodyEvenRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE );
		}
		{	// body odd row
			final Style cellStyle= new Style();
			
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.bodyOddRowBackgroundColor);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE );
		}
		
		// selection
		{	// body selected cell
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.bodySelectionBackgroundColor);
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.bodySelectionForegroundColor);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT );
		}
		{	// body selection anchor
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, this.bodyAnchorBorderStyle);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.NORMAL, SelectionStyleLabels.SELECTION_ANCHOR_STYLE );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE );
		}
		{	// header with selection
			final Style cellStyle= new Style();
			cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					this.headerSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					this.headerSelectionBackgroundColor );
			
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
					this.headerFullSelectionForegroundColor );
			cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					this.headerFullSelectionBackgroundColor );
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE );
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
					DisplayMode.SELECT, SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE );
		}
		
		// sorting
		{	// header column sorted
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					this.headerSortedCellPainter,
					DisplayMode.NORMAL, DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					this.headerSortedCellPainter,
					DisplayMode.NORMAL, DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
		}
		
		// freezing
		{	// body freezed
			configRegistry.registerConfigAttribute(IFreezeConfigAttributes.SEPARATOR_COLOR,
					this.bodyFreezeSeparatorColor );
		}
	}
	
	
	public void addListener(final Runnable listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(final Runnable listener) {
		this.listeners.remove(listener);
	}
	
	private void notifyListeners() {
		for (final Runnable listener : this.listeners) {
			listener.run();
		}
	}
	
}
