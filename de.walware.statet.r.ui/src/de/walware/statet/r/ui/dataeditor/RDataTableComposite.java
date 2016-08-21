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

package de.walware.statet.r.ui.dataeditor;

import static de.walware.ecommons.waltable.coordinate.Orientation.HORIZONTAL;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.waltable.NatTable;
import de.walware.ecommons.waltable.command.ILayerCommandHandler;
import de.walware.ecommons.waltable.config.CellConfigAttributes;
import de.walware.ecommons.waltable.config.IConfigRegistry;
import de.walware.ecommons.waltable.config.LayoutSizeConfig;
import de.walware.ecommons.waltable.coordinate.LRange;
import de.walware.ecommons.waltable.coordinate.LRangeList;
import de.walware.ecommons.waltable.coordinate.Orientation;
import de.walware.ecommons.waltable.coordinate.PositionCoordinate;
import de.walware.ecommons.waltable.coordinate.PositionId;
import de.walware.ecommons.waltable.copy.CopyToClipboardCommandHandler;
import de.walware.ecommons.waltable.data.ControlData;
import de.walware.ecommons.waltable.data.IDataProvider;
import de.walware.ecommons.waltable.data.ISpanningDataProvider;
import de.walware.ecommons.waltable.freeze.CompositeFreezeLayer;
import de.walware.ecommons.waltable.freeze.FreezeLayer;
import de.walware.ecommons.waltable.grid.GridRegion;
import de.walware.ecommons.waltable.grid.cell.AlternatingRowConfigLabelAccumulator;
import de.walware.ecommons.waltable.grid.data.DefaultCornerDataProvider;
import de.walware.ecommons.waltable.grid.labeled.ExtColumnHeaderLayer;
import de.walware.ecommons.waltable.grid.labeled.ExtGridLayer;
import de.walware.ecommons.waltable.grid.labeled.ExtRowHeaderLayer;
import de.walware.ecommons.waltable.grid.labeled.LabelCornerLayer;
import de.walware.ecommons.waltable.grid.layer.ColumnHeaderLayer;
import de.walware.ecommons.waltable.grid.layer.CornerLayer;
import de.walware.ecommons.waltable.grid.layer.GridLayer;
import de.walware.ecommons.waltable.grid.layer.RowHeaderLayer;
import de.walware.ecommons.waltable.layer.DataLayer;
import de.walware.ecommons.waltable.layer.ILayerListener;
import de.walware.ecommons.waltable.layer.SpanningDataLayer;
import de.walware.ecommons.waltable.layer.cell.AggregrateConfigLabelAccumulator;
import de.walware.ecommons.waltable.layer.event.ILayerEvent;
import de.walware.ecommons.waltable.layer.event.IVisualChangeEvent;
import de.walware.ecommons.waltable.layer.event.RowStructuralRefreshEvent;
import de.walware.ecommons.waltable.layer.event.RowUpdateEvent;
import de.walware.ecommons.waltable.resize.InitializeAutoResizeCommandHandler;
import de.walware.ecommons.waltable.selection.ISelectionEvent;
import de.walware.ecommons.waltable.selection.SelectAllCommand;
import de.walware.ecommons.waltable.selection.SelectDimPositionsCommand;
import de.walware.ecommons.waltable.selection.SelectRelativeCommandHandler;
import de.walware.ecommons.waltable.selection.SelectionLayer;
import de.walware.ecommons.waltable.sort.ClearSortCommand;
import de.walware.ecommons.waltable.sort.ClearSortCommandHandler;
import de.walware.ecommons.waltable.sort.ISortModel;
import de.walware.ecommons.waltable.sort.SortDimPositionCommand;
import de.walware.ecommons.waltable.sort.SortDirection;
import de.walware.ecommons.waltable.sort.SortHeaderLayer;
import de.walware.ecommons.waltable.sort.SortPositionCommandHandler;
import de.walware.ecommons.waltable.style.DisplayMode;
import de.walware.ecommons.waltable.tickupdate.config.DefaultTickUpdateConfiguration;
import de.walware.ecommons.waltable.viewport.IViewportDim;
import de.walware.ecommons.waltable.viewport.ViewportLayer;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;

import de.walware.statet.r.internal.ui.dataeditor.AbstractRDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.FTableDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.FindTask;
import de.walware.statet.r.internal.ui.dataeditor.IFindFilter;
import de.walware.statet.r.internal.ui.dataeditor.IFindListener;
import de.walware.statet.r.internal.ui.dataeditor.RDataFormatter;
import de.walware.statet.r.internal.ui.dataeditor.RDataFormatterConverter;
import de.walware.statet.r.internal.ui.dataeditor.RDataFrameDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.RDataTableContentDescription;
import de.walware.statet.r.internal.ui.dataeditor.RMatrixDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.RVectorDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.ResolveCellIndexes;
import de.walware.statet.r.internal.ui.intable.PresentationConfig;
import de.walware.statet.r.internal.ui.intable.RDataLayer;
import de.walware.statet.r.internal.ui.intable.TableLayers;
import de.walware.statet.r.internal.ui.intable.UIBindings;
import de.walware.statet.r.ui.RUI;


public class RDataTableComposite extends Composite implements ISelectionProvider {
	
	
	private class FindListener implements IFindListener {
		
		@Override
		public void handleFindEvent(final FindEvent event) {
			if (RDataTableComposite.this.tableLayers != null) {
				if (event.rowIdx >= 0) {
					RDataTableComposite.this.tableLayers.setAnchor(event.colIdx, event.rowIdx, true);
				}
				for (final IFindListener listener : RDataTableComposite.this.findListeners.toArray()) {
					listener.handleFindEvent(event);
				}
			}
		}
		
	}
	
	private class SelectionFindFilter implements IFindFilter {
		
		@Override
		public boolean match(final long rowIdx, final long columnIdx) {
			if (RDataTableComposite.this.tableLayers != null) {
				if (columnIdx >= 0) {
					return RDataTableComposite.this.tableLayers.selectionLayer.isCellPositionSelected(columnIdx, rowIdx);
				}
				else {
					return RDataTableComposite.this.tableLayers.selectionLayer.isRowPositionSelected(rowIdx);
				}
			}
			return false;
		}
		
	}
	
	private class SetAnchorByDataIndexes extends ResolveCellIndexes {
		
		public SetAnchorByDataIndexes(final AbstractRDataProvider<?> dataProvider) {
			super(dataProvider);
		}
		
		
		@Override
		protected void execute(final long columnIndex, final long rowIndex) {
			RDataTableComposite.this.display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (getDataProvider() != RDataTableComposite.this.dataProvider) {
						return;
					}
					setAnchorViewIdxs(columnIndex, rowIndex);
				}
			});
		}
		
	}
	
	
	private final Display display;
	
	private final StackLayout layout;
	
	private final Label messageControl;
	
	private Composite reloadControl;
	private final Runnable closeRunnable;
	
	private IRDataTableInput input;
	
	private AbstractRDataProvider<?> dataProvider;
	private TableLayers tableLayers;
	private boolean tableInitialized;
	
	private PositionCoordinate currentAnchor;
	private PositionCoordinate currentLastSelectedCell;
	private RDataTableSelection selection;
	private final Object selectionLock= new Object();
	private boolean selectionUpdateScheduled;
	private long selectionUpdateScheduleStamp;
	private boolean selectionCheckLabel;
	private final Runnable selectionUpdateRunnable= new Runnable() {
		@Override
		public void run() {
			if (isDisposed()) {
				return;
			}
			updateSelection();
		}
	};
	private final FastList<ISelectionChangedListener> selectionListeners= new FastList<>(ISelectionChangedListener.class);
	private final FastList<IFindListener> findListeners= new FastList<>(IFindListener.class);
	private final FastList<IRDataTableListener> tableListeners= new FastList<>(IRDataTableListener.class);
	
	private final RDataFormatter formatter= new RDataFormatter();
	
	private ResolveCellIndexes setAnchorByData;
	
	private final FindListener findListener= new FindListener();
	
	
	/**
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 */
	public RDataTableComposite(final Composite parent, final Runnable closeRunnable) {
		super(parent, SWT.NONE);
		
		this.display= getDisplay();
		this.closeRunnable= closeRunnable;
		
		this.layout= new StackLayout();
		setLayout(this.layout);
		
		this.messageControl= new Label(this, SWT.NONE);
		showDummy("Preparing...");
	}
	
	
	protected void initTable(final IRDataTableInput input,
			final AbstractRDataProvider<? extends RObject> dataProvider) {
		this.input= input;
		this.dataProvider= dataProvider;
		
		final PresentationConfig presentation= PresentationConfig.getInstance(this.display);
		final LayoutSizeConfig sizeConfig= presentation.getBaseSizeConfig();
		
		final TableLayers layers= new TableLayers();
		
		layers.dataLayer= new RDataLayer(dataProvider, presentation.getBaseSizeConfig());
		
		if (!this.dataProvider.getAllColumnsEqual()) {
//			final ColumnOverrideLabelAccumulator columnLabelAccumulator =
//					new ColumnOverrideLabelAccumulator(dataLayer);
//			for (long i= 0; i < fDataProvider.getColumnCount(); i++) {
//				columnLabelAccumulator.registerColumnOverrides(i, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + i);
//			}
			final AggregrateConfigLabelAccumulator aggregateLabelAccumulator =
					new AggregrateConfigLabelAccumulator();
//			aggregateLabelAccumulator.add(columnLabelAccumulator);
//			dataLayer.setConfigLabelAccumulator(aggregateLabelAccumulator);
		}
		
//		final WColumnReorderLayer columnReorderLayer= new WColumnReorderLayer(dataLayer);
//		final ColumnHideShowLayer columnHideShowLayer= new ColumnHideShowLayer(columnReorderLayer);
		
		layers.selectionLayer= new SelectionLayer(layers.dataLayer, false);
		layers.selectionLayer.addConfiguration(new UIBindings.SelectionConfiguration());
		layers.selectionLayer.addConfiguration(new DefaultTickUpdateConfiguration());
		
		layers.viewportLayer= new ViewportLayer(layers.selectionLayer);
		
		{	final FreezeLayer freezeLayer= new FreezeLayer(layers.selectionLayer);
			final CompositeFreezeLayer compositeFreezeLayer= new CompositeFreezeLayer(freezeLayer,
					layers.viewportLayer, layers.selectionLayer, true);
			layers.topBodyLayer= compositeFreezeLayer;
		}
		
		{	final IDataProvider headerDataProvider= dataProvider.getColumnDataProvider();
			layers.dataColumnHeaderLayer= (headerDataProvider instanceof ISpanningDataProvider) ?
				new SpanningDataLayer((ISpanningDataProvider) headerDataProvider,
						PositionId.BODY_CAT, sizeConfig.getRowHeight(),
						PositionId.HEADER_CAT, sizeConfig.getRowHeight() ) :
				new DataLayer(headerDataProvider,
						PositionId.BODY_CAT, sizeConfig.getRowHeight(),
						PositionId.HEADER_CAT, sizeConfig.getRowHeight() );
			
			final ColumnHeaderLayer layer= new ColumnHeaderLayer(
					layers.dataColumnHeaderLayer,
					layers.topBodyLayer, layers.selectionLayer,
					false, presentation.getHeaderLayerPainter() );
			layer.addConfiguration(new UIBindings.ColumnHeaderConfiguration());
			layers.topColumnHeaderLayer= layer;
		}
		final ISortModel sortModel= dataProvider.getSortModel();
		if (sortModel != null) {
			final SortHeaderLayer<?> sortHeaderLayer= new SortHeaderLayer<>(
					layers.topColumnHeaderLayer, sortModel, false);
			sortHeaderLayer.addConfiguration(new UIBindings.SortConfiguration());
			layers.topColumnHeaderLayer= sortHeaderLayer;
		}
		{	final IDataProvider headerDataProvider= dataProvider.getRowDataProvider();
			final int width= sizeConfig.getCharWidth() * 8 + sizeConfig.getDefaultSpace() * 2;
			layers.dataRowHeaderLayer= (headerDataProvider instanceof ISpanningDataProvider) ?
					new SpanningDataLayer((ISpanningDataProvider) headerDataProvider,
							PositionId.HEADER_CAT, width,
							PositionId.BODY_CAT, sizeConfig.getRowHeight() ) :
					new DataLayer(headerDataProvider,
							PositionId.HEADER_CAT, width,
							PositionId.BODY_CAT, sizeConfig.getRowHeight() );
			
			layers.topRowHeaderLayer= new RowHeaderLayer(
					layers.dataRowHeaderLayer,
					layers.topBodyLayer, layers.selectionLayer,
					false, presentation.getHeaderLayerPainter() );
		}
		final IDataProvider cornerDataProvider= new DefaultCornerDataProvider(
				layers.dataColumnHeaderLayer.getDataProvider(),
				layers.dataRowHeaderLayer.getDataProvider() );
		
		final GridLayer gridLayer;
		if (dataProvider.getColumnLabelProvider() != null || dataProvider.getRowLabelProvider() != null) {
			layers.topColumnHeaderLayer= new ExtColumnHeaderLayer(layers.topColumnHeaderLayer);
			layers.topRowHeaderLayer= new ExtRowHeaderLayer(layers.topRowHeaderLayer);
			final CornerLayer cornerLayer= new LabelCornerLayer(
					new DataLayer(cornerDataProvider,
							PositionId.HEADER_CAT, sizeConfig.getRowHeight(),
							PositionId.HEADER_CAT, sizeConfig.getRowHeight() ),
					layers.topRowHeaderLayer, layers.topColumnHeaderLayer,
					dataProvider.getColumnLabelProvider(), dataProvider.getRowLabelProvider(),
					false, presentation.getHeaderLabelLayerPainter() );
			gridLayer= new ExtGridLayer(layers.topBodyLayer,
					layers.topColumnHeaderLayer, layers.topRowHeaderLayer,
					cornerLayer, false );
		}
		else {
			final CornerLayer cornerLayer= new CornerLayer(
					new DataLayer(cornerDataProvider, PositionId.HEADER_CAT),
					layers.topRowHeaderLayer, layers.topColumnHeaderLayer,
					false, presentation.getHeaderLayerPainter() );
			gridLayer= new GridLayer(layers.topBodyLayer,
					layers.topColumnHeaderLayer, layers.topRowHeaderLayer, cornerLayer, false);
		}
		gridLayer.addConfigLabelAccumulatorForRegion(GridRegion.BODY, new AlternatingRowConfigLabelAccumulator());
		
//		{	final ILayerCommandHandler<?> commandHandler= new ScrollCommandHandler(fTableLayers.viewportLayer);
//			fTableLayers.viewportLayer.registerCommandHandler(commandHandler);
//		}
		{	final ILayerCommandHandler<?> commandHandler= new SelectRelativeCommandHandler(
					layers.selectionLayer );
			layers.viewportLayer.registerCommandHandler(commandHandler);
			layers.selectionLayer.registerCommandHandler(commandHandler);
		}
		{	final ILayerCommandHandler<?> commandHandler= new CopyToClipboardCommandHandler(
					layers.selectionLayer );
			layers.selectionLayer.registerCommandHandler(commandHandler);
		}
		{	final ILayerCommandHandler<?> commandHandler= new InitializeAutoResizeCommandHandler(
					layers.selectionLayer );
			gridLayer.registerCommandHandler(commandHandler);
		}
		if (sortModel != null) {
			final ILayerCommandHandler<?> commandHandler= new SortPositionCommandHandler(sortModel);
			layers.dataLayer.registerCommandHandler(commandHandler);
		}
		if (sortModel != null) {
			final ILayerCommandHandler<?> commandHandler= new ClearSortCommandHandler(sortModel);
			layers.dataLayer.registerCommandHandler(commandHandler);
		}
		
		layers.table= new NatTable(this, gridLayer, false);
		layers.table.addConfiguration(presentation);
		layers.table.addConfiguration(new UIBindings.HeaderContextMenuConfiguration(layers.table));
		
		layers.table.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				dataProvider.dispose();
			}
		});
		
		final IConfigRegistry registry= layers.table.getConfigRegistry();
		registry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				new RDataFormatterConverter(dataProvider));
		registry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				new RDataFormatterConverter.RowHeader(dataProvider),
				DisplayMode.NORMAL, GridRegion.ROW_HEADER);
		
		this.tableLayers= layers;
		this.tableInitialized= false;
		
		configRunnable.run();
		
		this.tableLayers.table.addLayerListener(new ILayerListener() {
			@Override
			public void handleLayerEvent(final ILayerEvent event) {
				if (event instanceof ISelectionEvent) {
					scheduleUpdateSelection(false, 100);
					return;
				}
				if (event instanceof IVisualChangeEvent) {
					scheduleUpdateSelection(true, 100);
					return;
				}
			}
		});
		dataProvider.addDataChangedListener(new AbstractRDataProvider.IDataProviderListener() {
			@Override
			public void onInputInitialized(final boolean structChanged) {
				if (layers != RDataTableComposite.this.tableLayers || layers.table.isDisposed()) {
					return;
				}
				
				if (layers.table != RDataTableComposite.this.layout.topControl) {
					layers.table.configure();
				}
				
				if (layers.table != RDataTableComposite.this.layout.topControl) {
					RDataTableComposite.this.layout.topControl= layers.table;
					layout();
				}
				if (!RDataTableComposite.this.tableInitialized) {
					layers.selectionLayer.setSelectedCell(0, 0);
				}
				else if (structChanged) {
					layers.dataLayer.fireLayerEvent(new RowStructuralRefreshEvent(layers.dataLayer));
				}
				else {
					layers.dataLayer.fireLayerEvent(new RowUpdateEvent(layers.dataLayer,
							new LRange(0, layers.dataLayer.getRowCount()) ));
				}
				
				RDataTableComposite.this.selection= null;
				scheduleUpdateSelection(true, 0);
				
				for (final IRDataTableListener listener : RDataTableComposite.this.tableListeners.toArray()) {
					listener.inputChanged(input, dataProvider.getDescription());
				}
			}
			
			@Override
			public void onInputFailed(final int error) {
				if (error == ERROR_STRUCT_CHANGED) {
					showReload();
				}
				else {
					showDummy("An error occurred when loading the table input.");
					for (final IRDataTableListener listener : RDataTableComposite.this.tableListeners.toArray()) {
						listener.inputChanged(null, null);
					}
				}
			}
			
			@Override
			public void onRowCountChanged() {
				final TableLayers layers= RDataTableComposite.this.tableLayers;
				if (layers == null || layers.table != layers.table || layers.table.isDisposed()) {
					return;
				}
				
				layers.dataLayer.fireLayerEvent(new RowStructuralRefreshEvent(layers.dataLayer));
				
				RDataTableComposite.this.selection= null;
				scheduleUpdateSelection(true, 0);
			}
			
			@Override
			public void onRowsChanged(final long beginIdx, final long endIdx) {
				RDataTableComposite.this.display.asyncExec(new Runnable() {
					@Override
					public void run() {
						final TableLayers layers= RDataTableComposite.this.tableLayers;
						if (layers == null || layers.table != layers.table || layers.table.isDisposed()) {
							return;
						}
						layers.dataLayer.fireLayerEvent(new RowUpdateEvent(layers.dataLayer,
								new LRange(beginIdx, endIdx) ));
					}
				});
			}
			
		});
		dataProvider.addFindListener(this.findListener);
	}
	
	public NatTable getTable() {
		return (this.tableLayers != null) ? this.tableLayers.table : null;
	}
	
	protected void scheduleUpdateSelection(final boolean checkLabel, final int delay) {
		synchronized (this.selectionLock) {
			this.selectionUpdateScheduleStamp= System.nanoTime() + delay * 1000000L;
			if (checkLabel) {
				this.selectionCheckLabel= true;
			}
			if (this.selectionUpdateScheduled) {
				return;
			}
			this.selectionUpdateScheduled= true;
		}
		this.display.asyncExec(this.selectionUpdateRunnable);
	}
	
	protected void updateSelection() {
		final boolean checkLabel;
		synchronized (this.selectionLock) {
			final long diff= (this.selectionUpdateScheduleStamp - System.nanoTime()) / 1000000L;
			if (diff > 5) {
				this.display.timerExec((int) diff, this.selectionUpdateRunnable);
				return;
			}
			checkLabel= this.selectionCheckLabel;
			this.selectionCheckLabel= false;
			this.selectionUpdateScheduled= false;
		}
		final RDataTableSelection selection;
		if (this.tableLayers == null) {
			selection= new RDataTableSelection(null, null, null, null);
		}
		else {
			final SelectionLayer selectionLayer= this.tableLayers.selectionLayer;
			final PositionCoordinate anchor= selectionLayer.getSelectionAnchor();
			PositionCoordinate lastSelected= selectionLayer.getLastSelectedCellPosition();
			if (lastSelected.equals(anchor)) {
				lastSelected= null;
			}
			
			final boolean anchorChanged;
			if ((anchorChanged= !anchor.equals(this.currentAnchor))) {
				this.currentAnchor= new PositionCoordinate(anchor);
			}
			final boolean lastSelectedChanged;
			if ((lastSelectedChanged= !((lastSelected != null) ?
					lastSelected.equals(this.currentLastSelectedCell) : null == this.currentLastSelectedCell ))) {
				this.currentLastSelectedCell= (lastSelected != null) ? new PositionCoordinate(lastSelected) : null;
			}
			
			if (!checkLabel && !anchorChanged && !lastSelectedChanged) {
				return;
			}
			
			String anchorRowLabel= null;
			String anchorColumnLabel= null;
			if (this.currentAnchor.columnPosition >= 0 && this.currentAnchor.rowPosition >= 0) {
				if (anchorChanged || checkLabel) {
					anchorRowLabel= getRowLabel(this.currentAnchor.rowPosition);
					if (anchorRowLabel != null) {
						anchorColumnLabel= getColumnLabel(this.currentAnchor.columnPosition);
						if (anchorColumnLabel == null) {
							anchorRowLabel= null;
						}
					}
				}
				else if (this.selection != null) {
					anchorRowLabel= this.selection.getAnchorRowLabel();
					anchorColumnLabel= this.selection.getAnchorColumnLabel();
				}
			}
			
			if (anchorRowLabel == null) {
				return;
			}
			
			String lastSelectedRowLabel= null;
			String lastSelectedColumnLabel= null;
			if (this.currentLastSelectedCell != null
					&& this.currentLastSelectedCell.columnPosition >= 0 && this.currentLastSelectedCell.rowPosition >= 0) {
				if (lastSelectedChanged || checkLabel) {
					lastSelectedRowLabel= getRowLabel(this.currentLastSelectedCell.rowPosition);
					if (lastSelectedRowLabel != null) {
						lastSelectedColumnLabel= getColumnLabel(this.currentLastSelectedCell.columnPosition);
						if (lastSelectedColumnLabel == null) {
							lastSelectedRowLabel= null;
						}
					}
				}
				else if (this.selection != null) {
					lastSelectedRowLabel= this.selection.getLastSelectedCellRowLabel();
					lastSelectedColumnLabel= this.selection.getLastSelectedCellColumnLabel();
				}
			}
			selection= new RDataTableSelection(
					anchorRowLabel, anchorColumnLabel,
					lastSelectedRowLabel, lastSelectedColumnLabel);
		}
		
		if (selection.equals(this.selection)) {
			return;
		}
		this.selection= selection;
		final SelectionChangedEvent event= new SelectionChangedEvent(this, this.selection);
		
		for (final ISelectionChangedListener listener : this.selectionListeners.toArray()) {
			listener.selectionChanged(event);
		}
	}
	
	protected String getRowLabel(final long row) {
		if (!this.dataProvider.hasRealRows()) {
			return ""; //$NON-NLS-1$
		}
		final IDataProvider dataProvider= this.dataProvider.getRowDataProvider();
		if (dataProvider.getColumnCount() <= 1) {
			return getHeaderLabel(dataProvider.getDataValue(0, row, 0));
		}
		final StringBuilder sb= new StringBuilder();
		for (long i= 0; i < dataProvider.getColumnCount(); i++) {
			final String label= getHeaderLabel(dataProvider.getDataValue(i, row, 0));
			if (label == null) {
				return null;
			}
			sb.append(label);
			sb.append(", "); //$NON-NLS-1$
		}
		return sb.substring(0, sb.length()-2);
	}
	
	protected String getColumnLabel(final long column) {
		if (!this.dataProvider.hasRealColumns()) {
			return ""; //$NON-NLS-1$
		}
		final IDataProvider dataProvider= this.dataProvider.getColumnDataProvider();
		if (dataProvider.getRowCount() <= 1) {
			return getHeaderLabel(dataProvider.getDataValue(column, 0, 0));
		}
		final StringBuilder sb= new StringBuilder();
		for (long i= 0; i < dataProvider.getRowCount(); i++) {
			final String label= getHeaderLabel(dataProvider.getDataValue(column, i, 0));
			if (label == null) {
				return null;
			}
			sb.append(label);
			sb.append(", "); //$NON-NLS-1$
		}
		return sb.substring(0, sb.length()-2);
	}
	
	private String getHeaderLabel(final Object value) {
		if (value != null) {
			if (value instanceof ControlData && value != AbstractRDataProvider.NA) {
				return null;
			}
			final Object displayValue= this.formatter.modelToDisplayValue(value);
			if (displayValue.getClass() == String.class) {
				return (String) displayValue;
			}
			if (displayValue == AbstractRDataProvider.DUMMY) {
				return ""; //$NON-NLS-1$
			}
			return null;
		}
		else {
			return this.formatter.modelToDisplayValue(null).toString();
		}
	}
	
	protected void showDummy(final String message) {
		if (isDisposed()) {
			return;
		}
		this.messageControl.setText(message);
		this.layout.topControl= this.messageControl;
		layout();
	}
	
	
	public long[] getTableDimension() {
		if (this.tableLayers != null) {
			return new long[] { this.dataProvider.getRowCount(), this.dataProvider.getColumnCount() };
		}
		return null;
	}
	
	public boolean isOK() {
		return (this.tableLayers != null && this.layout.topControl == this.tableLayers.table);
	}
	
	public IViewportDim getViewport(final Orientation orientation) {
		return this.tableLayers.viewportLayer.getDim(orientation);
	}
	
	@Override
	public ISelection getSelection() {
		return null;
	}
	
	@Override
	public void setSelection(final ISelection selection) {
	}
	
	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		this.selectionListeners.add(listener);
	}
	
	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		this.selectionListeners.remove(listener);
	}
	
	
	public void find(final String expression, final boolean selectedOnly, final boolean firstInRow, final boolean forward) {
		if (this.tableLayers != null) {
			final PositionCoordinate anchor= this.tableLayers.selectionLayer.getSelectionAnchor();
			final FindTask task= new FindTask(expression,
					anchor.getRowPosition(), anchor.getColumnPosition(), firstInRow, forward,
					(selectedOnly) ? new SelectionFindFilter() : null);
			this.dataProvider.find(task);
		}
	}
	
	public void addFindListener(final IFindListener listener) {
		this.findListeners.add(listener);
	}
	
	public void removeFindListener(final IFindListener listener) {
		this.findListeners.remove(listener);
	}
	
	
	public void setInput(final IRDataTableInput input) {
		if (this.tableLayers != null) {
			showDummy(""); //$NON-NLS-1$
			this.tableLayers.table.dispose();
			this.tableLayers.table= null;
			this.dataProvider= null;
			this.setAnchorByData= null;
		}
		if (input != null) {
			showDummy("Preparing (" + input.getName() + ")...");
			try {
				final IToolRunnable runnable= new ISystemRunnable() {
					
					@Override
					public String getTypeId() {
						return "r/dataeditor/init"; //$NON-NLS-1$
					}
					
					@Override
					public String getLabel() {
						return "Prepare Data Viewer (" + input.getName() + ")";
					}
					
					@Override
					public boolean isRunnableIn(final ITool tool) {
						return true; // TODO
					}
					
					@Override
					public boolean changed(final int event, final ITool process) {
						if (event == MOVING_FROM) {
							return false;
						}
						return true;
					}
					
					@Override
					public void run(final IToolService service,
							final IProgressMonitor monitor) throws CoreException {
						final IRToolService r= (IRToolService) service;
						
						final AtomicReference<AbstractRDataProvider<?>> dataProvider= new AtomicReference<>();
						Exception error= null;
						try {
							final RObject struct= r.evalData(input.getFullName(),
									null, RObjectFactory.F_ONLY_STRUCT, 1, monitor);
							RCharacterStore classNames= null;
							{	final FunctionCall call= r.createFunctionCall("class"); //$NON-NLS-1$
								call.add(input.getFullName());
								classNames= RDataUtil.checkRCharVector(call.evalData(monitor)).getData();
							}
							
							switch (struct.getRObjectType()) {
							case RObject.TYPE_VECTOR:
								dataProvider.set(new RVectorDataProvider(input, (RVector<?>) struct));
								break;
							case RObject.TYPE_ARRAY: {
								final RArray<?> array= (RArray<?>) struct;
								if (array.getDim().getLength() == 2) {
									if (classNames.contains("ftable")) { //$NON-NLS-1$
										dataProvider.set(new FTableDataProvider(input, array));
										break;
									}
									dataProvider.set(new RMatrixDataProvider(input, array));
									break;
								}
								break; }
							case RObject.TYPE_DATAFRAME:
								dataProvider.set(new RDataFrameDataProvider(input, (RDataFrame) struct));
								break;
							default:
								break;
							}
						}
						catch (final CoreException e) {
							error= e;
						}
						catch (final UnexpectedRDataException e) {
							error= e;
						}
						final IStatus status;
						if (error != null) {
							status= new Status(IStatus.ERROR, RUI.PLUGIN_ID,
									"An error occurred when preparing the R data viewer.", error );
							StatusManager.getManager().handle(status);
						}
						else if (dataProvider.get() == null) {
							status= new Status(IStatus.INFO, RUI.PLUGIN_ID,
									"This R element type is not supported.", null );
						}
						else {
							status= null;
						}
						RDataTableComposite.this.display.asyncExec(new Runnable() {
							@Override
							public void run() {
								RDataTableComposite.this.dataProvider= null;
								if (!UIAccess.isOkToUse(RDataTableComposite.this)) {
									return;
								}
								if (status == null) {
									initTable(input, dataProvider.get());
								}
								else {
									showDummy(status.getMessage());
								}
							}
						});
					}
				};
				final IStatus status= input.getTool().getQueue().add(runnable);
				if (status.getSeverity() >= IStatus.ERROR) {
					throw new CoreException(status);
				}
			}
			catch (final CoreException e) {
				showDummy(e.getLocalizedMessage());
			}
		}
	}
	
	protected void showReload() {
		if (this.reloadControl == null) {
			this.reloadControl= new Composite(this, SWT.NONE);
			this.reloadControl.setLayout(LayoutUtil.createCompositeGrid(4));
			
			final Label label= new Label(this.reloadControl, SWT.WRAP);
			label.setText("The structure of the R element is changed (columns / data type).");
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
			
			{	final Button button= new Button(this.reloadControl, SWT.PUSH);
				button.setLayoutData(LayoutUtil.hintWidth(new GridData(
						SWT.FILL, SWT.CENTER, false, false), button));
				button.setText("Refresh");
				button.setToolTipText("Refresh table with old structure");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						refresh();
					}
				});
			}
			{	final Button button= new Button(this.reloadControl, SWT.PUSH);
				button.setLayoutData(LayoutUtil.hintWidth(new GridData(
						SWT.FILL, SWT.CENTER, false, false), button));
				button.setText("Reopen");
				button.setToolTipText("Reopen table with new structure");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						setInput(RDataTableComposite.this.input);
					}
				});
			}
			if (this.closeRunnable != null) {
				final Button button= new Button(this.reloadControl, SWT.PUSH);
				button.setLayoutData(LayoutUtil.hintWidth(new GridData(
						SWT.FILL, SWT.CENTER, false, false), button));
				button.setText("Close");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						RDataTableComposite.this.closeRunnable.run();
					}
				});
			}
//			
			LayoutUtil.addSmallFiller(this.reloadControl, true);
		}
		this.layout.topControl= this.reloadControl;
		layout(true);
	}
	
	
	public void setFilter(final String filter) {
		this.dataProvider.setFilter(filter);
	}
	
	
	public RDataTableContentDescription getDescription() {
		return this.dataProvider.getDescription();
	}
	
	public void addTableListener(final IRDataTableListener listener) {
		this.tableListeners.add(listener);
		if (this.tableLayers != null) {
			listener.inputChanged(this.input, this.dataProvider.getDescription());
		}
	}
	
	public void removeTableListener(final IRDataTableListener listener) {
		this.tableListeners.remove(listener);
	}
	
	
	@Override
	public boolean setFocus() {
		if (this.layout == null || this.layout.topControl == null) {
			return false;
		}
		return this.layout.topControl.forceFocus();
	}
	
	public void revealColumn(final long index) {
		if (this.tableLayers != null) {
			this.tableLayers.viewportLayer.getDim(HORIZONTAL).movePositionIntoViewport(index);
		}
	}
	
	public void selectColumns(final Collection<LRange> indexes) {
		if (this.tableLayers != null) {
			final LRangeList columns= LRangeList.toRangeList(indexes);
//			final long rowIndex= fTableBodyLayerStack.getViewportLayer().getRowIndexByPosition(0);
			final long rowIndex= 0;
			this.tableLayers.table.doCommand(new SelectDimPositionsCommand(
					this.tableLayers.selectionLayer.getDim(HORIZONTAL),
					0, columns, rowIndex,
					0,
					!(columns.isEmpty()) ? columns.values().first() : SelectionLayer.NO_SELECTION ));
		}
	}
	
	public void setAnchorViewIdxs(final long columnIndex, final long rowIndex) {
		if (this.tableLayers != null) {
			this.tableLayers.selectionLayer.setSelectionAnchor(columnIndex, rowIndex, true);
		}
	}
	
	public long[] getAnchorDataIdxs() {
		if (this.tableLayers != null) {
			final PositionCoordinate coordinate= this.tableLayers.selectionLayer.getSelectionAnchor();
			if (coordinate.columnPosition < 0 || coordinate.rowPosition < 0) {
				return null;
			}
			return this.dataProvider.toDataIdxs(coordinate.columnPosition, coordinate.rowPosition);
		}
		return null;
	}
	
	public void setAnchorDataIdxs(final long columnIdx, final long rowIdx) {
		if (this.tableLayers != null) {
			if (this.setAnchorByData == null) {
				this.setAnchorByData= new SetAnchorByDataIndexes(this.dataProvider);
			}
			this.setAnchorByData.resolve(columnIdx, rowIdx);
		}
	}
	
	public void selectAll() {
		if (this.tableLayers != null) {
			this.tableLayers.table.doCommand(new SelectAllCommand());
		}
	}
	
	public void sortByColumn(final long index, final boolean increasing) {
		if (this.tableLayers != null) {
			this.tableLayers.dataLayer.doCommand(new SortDimPositionCommand(
					this.tableLayers.dataLayer.getDim(HORIZONTAL), index,
					increasing ? SortDirection.ASC : SortDirection.DESC, false ));
//			final ISortModel sortModel= this.fDataProvider.getSortModel();
//			if (sortModel != null) {
//				sortModel.sort(
//						PositionId.BODY_CAT | index, increasing ? SortDirection.ASC : SortDirection.DESC, false );
//				this.fTableLayers.topColumnHeaderLayer.fireLayerEvent(
//						new SortColumnEvent(this.fTableLayers.topColumnHeaderLayer.getDim(HORIZONTAL), 0) );
//			}
		}
	}
	
	public void clearSorting() {
		if (this.tableLayers != null) {
			this.tableLayers.table.doCommand(new ClearSortCommand());
		}
	}
	
	public void refresh() {
		if (this.tableLayers != null) {
			this.dataProvider.reset();
			this.tableLayers.table.redraw();
		}
	}
	
}
