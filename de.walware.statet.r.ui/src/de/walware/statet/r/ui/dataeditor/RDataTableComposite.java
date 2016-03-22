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

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;

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
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.LayoutSizeConfig;
import org.eclipse.nebula.widgets.nattable.coordinate.Orientation;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ISpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.labeled.ExtColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.labeled.ExtGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.labeled.ExtRowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.labeled.LabelCornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.group.NColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.NRowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowUpdateEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectRelativeCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.ISelectionEvent;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.command.ClearSortCommand;
import org.eclipse.nebula.widgets.nattable.sort.event.SortColumnEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tickupdate.config.DefaultTickUpdateConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.IViewportDim;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
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
import de.walware.statet.r.internal.ui.intable.InfoString;
import de.walware.statet.r.internal.ui.intable.PresentationConfig;
import de.walware.statet.r.internal.ui.intable.RDataLayer;
import de.walware.statet.r.internal.ui.intable.TableLayers;
import de.walware.statet.r.internal.ui.intable.UIBindings;
import de.walware.statet.r.ui.RUI;


public class RDataTableComposite extends Composite implements ISelectionProvider {
	
	
	private class FindListener implements IFindListener {
		
		@Override
		public void handleFindEvent(final FindEvent event) {
			if (fTable != null) {
				if (event.rowIdx >= 0) {
					fTableLayers.setAnchor(event.colIdx, event.rowIdx, true);
				}
				for (final IFindListener listener : fFindListeners.toArray()) {
					listener.handleFindEvent(event);
				}
			}
		}
		
	}
	
	private class SelectionFindFilter implements IFindFilter {
		
		@Override
		public boolean match(final long rowIdx, final long columnIdx) {
			if (fTable != null) {
				if (columnIdx >= 0) {
					return fTableLayers.selectionLayer.isCellPositionSelected(columnIdx, rowIdx);
				}
				else {
					return fTableLayers.selectionLayer.isRowPositionSelected(rowIdx);
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
			fDisplay.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (getDataProvider() != RDataTableComposite.this.fDataProvider) {
						return;
					}
					setAnchorViewIdxs(columnIndex, rowIndex);
				}
			});
		}
		
	}
	
	
	private final Display fDisplay;
	
	private final StackLayout fLayout;
	
	private final Label fMessageControl;
	
	private Composite fReloadControl;
	private final Runnable fCloseRunnable;
	
	private IRDataTableInput fInput;
	
	private NatTable fTable;
	private boolean fTableInitialized;
	
	private AbstractRDataProvider<?> fDataProvider;
	private TableLayers fTableLayers;
	
	private PositionCoordinate fCurrentAnchor;
	private PositionCoordinate fCurrentLastSelectedCell;
	private RDataTableSelection fSelection;
	private final Object fSelectionLock = new Object();
	private boolean fSelectionUpdateScheduled;
	private long fSelectionUpdateScheduleStamp;
	private boolean fSelectionCheckLabel;
	private final Runnable fSelectionUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			if (isDisposed()) {
				return;
			}
			updateSelection();
		}
	};
	private final FastList<ISelectionChangedListener> fSelectionListeners= new FastList<>(ISelectionChangedListener.class);
	private final FastList<IFindListener> fFindListeners= new FastList<>(IFindListener.class);
	private final FastList<IRDataTableListener> fTableListeners= new FastList<>(IRDataTableListener.class);
	
	private final RDataFormatter fFormatter = new RDataFormatter();
	
	private ResolveCellIndexes setAnchorByData;
	
	private final FindListener fFindListener = new FindListener();
	
	
	/**
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 */
	public RDataTableComposite(final Composite parent, final Runnable closeRunnable) {
		super(parent, SWT.NONE);
		
		fDisplay = getDisplay();
		fCloseRunnable = closeRunnable;
		
		fLayout = new StackLayout();
		setLayout(fLayout);
		
		fMessageControl = new Label(this, SWT.NONE);
		showDummy("Preparing...");
	}
	
	
	protected void initTable(final IRDataTableInput input,
			final AbstractRDataProvider<? extends RObject> dataProvider) {
		fInput = input;
		fDataProvider = dataProvider;
		
		final PresentationConfig presentation = PresentationConfig.getInstance(fDisplay);
		final LayoutSizeConfig sizeConfig = presentation.getBaseSizeConfig();
		
		final RDataLayer dataLayer = new RDataLayer(dataProvider, sizeConfig);
		
		if (!fDataProvider.getAllColumnsEqual()) {
//			final ColumnOverrideLabelAccumulator columnLabelAccumulator =
//					new ColumnOverrideLabelAccumulator(dataLayer);
//			for (long i = 0; i < fDataProvider.getColumnCount(); i++) {
//				columnLabelAccumulator.registerColumnOverrides(i, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + i);
//			}
			final AggregrateConfigLabelAccumulator aggregateLabelAccumulator =
					new AggregrateConfigLabelAccumulator();
//			aggregateLabelAccumulator.add(columnLabelAccumulator);
			dataLayer.setConfigLabelAccumulator(aggregateLabelAccumulator);
		}
		
		fTableLayers = new TableLayers();
//		final WColumnReorderLayer columnReorderLayer = new WColumnReorderLayer(dataLayer);
//		final ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		
		fTableLayers.selectionLayer = new SelectionLayer(dataLayer, false);
		fTableLayers.selectionLayer.addConfiguration(new UIBindings.SelectionConfiguration());
		fTableLayers.selectionLayer.addConfiguration(new DefaultTickUpdateConfiguration());
		
		fTableLayers.viewportLayer = new ViewportLayer(fTableLayers.selectionLayer);
		
//		{	final ILayerCommandHandler<?> commandHandler = new ScrollCommandHandler(fTableLayers.viewportLayer);
//			fTableLayers.viewportLayer.registerCommandHandler(commandHandler);
//		}
		{	final ILayerCommandHandler<?> commandHandler = new SelectRelativeCommandHandler(fTableLayers.selectionLayer);
			fTableLayers.viewportLayer.registerCommandHandler(commandHandler);
			fTableLayers.selectionLayer.registerCommandHandler(commandHandler);
		}
		{	final ILayerCommandHandler<?> commandHandler = new CopyDataCommandHandler(fTableLayers.selectionLayer);
			fTableLayers.selectionLayer.registerCommandHandler(commandHandler);
		}
		
		final FreezeLayer freezeLayer = new FreezeLayer(fTableLayers.selectionLayer);
		final CompositeFreezeLayer compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer,
				fTableLayers.viewportLayer, fTableLayers.selectionLayer, true);
		fTableLayers.topBodyLayer = compositeFreezeLayer;
		
		final IDataProvider columnHeaderDataProvider = dataProvider.getColumnDataProvider();
		final NColumnGroupHeaderLayer columnHeaderLayer = new NColumnGroupHeaderLayer(
				(columnHeaderDataProvider instanceof ISpanningDataProvider) ?
						new SpanningDataLayer((ISpanningDataProvider) columnHeaderDataProvider,
								sizeConfig.getRowHeight(), sizeConfig.getRowHeight(), true, false ) :
						new DataLayer(columnHeaderDataProvider,
								sizeConfig.getRowHeight(), sizeConfig.getRowHeight(), true, false ),
				fTableLayers.topBodyLayer, fTableLayers.selectionLayer,
				false, presentation.getHeaderLayerPainter() );
		columnHeaderLayer.addConfiguration(new UIBindings.ColumnHeaderConfiguration());
		fTableLayers.topColumnHeaderLayer = columnHeaderLayer;
		
		final ISortModel sortModel = dataProvider.getSortModel();
		if (sortModel != null) {
			final SortHeaderLayer<?> sortHeaderLayer= new SortHeaderLayer<>(
					columnHeaderLayer, sortModel, false);
			sortHeaderLayer.addConfiguration(new UIBindings.SortConfiguration());
			fTableLayers.topColumnHeaderLayer = sortHeaderLayer;
		}
		
		final IDataProvider rowHeaderDataProvider = dataProvider.getRowDataProvider();
		{	final int width = sizeConfig.getCharWidth() * 8 + sizeConfig.getDefaultSpace() * 2;
			fTableLayers.topRowHeaderLayer = new NRowGroupHeaderLayer(
				(rowHeaderDataProvider instanceof ISpanningDataProvider) ?
						new SpanningDataLayer((ISpanningDataProvider) rowHeaderDataProvider,
								width, sizeConfig.getRowHeight(), false, true ) :
						new DataLayer(rowHeaderDataProvider,
								width, sizeConfig.getRowHeight(), false, true ),
				fTableLayers.topBodyLayer, fTableLayers.selectionLayer,
				false, presentation.getHeaderLayerPainter() );
		}
		
		final IDataProvider cornerDataProvider = new DefaultCornerDataProvider(
				columnHeaderDataProvider, rowHeaderDataProvider );
		
		final GridLayer gridLayer;
		if (dataProvider.getColumnLabelProvider() != null || dataProvider.getRowLabelProvider() != null) {
			fTableLayers.topColumnHeaderLayer = new ExtColumnHeaderLayer(fTableLayers.topColumnHeaderLayer, sizeConfig);
			fTableLayers.topRowHeaderLayer = new ExtRowHeaderLayer(fTableLayers.topRowHeaderLayer, sizeConfig);
			final CornerLayer cornerLayer = new LabelCornerLayer(new DataLayer(cornerDataProvider,
					sizeConfig.getRowHeight(), sizeConfig.getRowHeight(), false, false ),
					fTableLayers.topRowHeaderLayer, fTableLayers.topColumnHeaderLayer,
					dataProvider.getColumnLabelProvider(), dataProvider.getRowLabelProvider(),
					false, presentation.getHeaderLabelLayerPainter() );
			gridLayer = new ExtGridLayer(fTableLayers.topBodyLayer,
					fTableLayers.topColumnHeaderLayer, fTableLayers.topRowHeaderLayer, cornerLayer, false);
		}
		else {
			final CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider),
					fTableLayers.topRowHeaderLayer, fTableLayers.topColumnHeaderLayer,
					false, presentation.getHeaderLayerPainter() );
			gridLayer = new GridLayer(fTableLayers.topBodyLayer,
					fTableLayers.topColumnHeaderLayer, fTableLayers.topRowHeaderLayer, cornerLayer, false);
		}
		gridLayer.setConfigLabelAccumulatorForRegion(GridRegion.BODY, new AlternatingRowConfigLabelAccumulator());
		
		final NatTable table = new NatTable(this, gridLayer, false);
		table.addConfiguration(presentation);
		table.addConfiguration(new UIBindings.HeaderContextMenuConfiguration(table));
		
		table.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				dataProvider.dispose();
			}
		});
		
		final IConfigRegistry registry = table.getConfigRegistry();
		registry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				new RDataFormatterConverter(dataProvider));
		registry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				new RDataFormatterConverter.RowHeader(dataProvider),
				DisplayMode.NORMAL, GridRegion.ROW_HEADER);
		
		fTable = table;
		fTableInitialized = false;
		fTable.addLayerListener(new ILayerListener() {
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
				if (fTable != table || table.isDisposed()) {
					return;
				}
				
				if (fTable != fLayout.topControl) {
					table.configure();
				}
				
				if (fTable != fLayout.topControl) {
					fLayout.topControl = table;
					layout();
				}
				if (!fTableInitialized) {
					fTableLayers.selectionLayer.setSelectedCell(0, 0);
				}
				else if (structChanged) {
					dataLayer.fireLayerEvent(new RowStructuralRefreshEvent(dataLayer));
				}
				else {
					dataLayer.fireLayerEvent(new RowUpdateEvent(dataLayer, new Range(0, dataLayer.getRowCount())));
				}
				
				fSelection = null;
				scheduleUpdateSelection(true, 0);
				
				for (final IRDataTableListener listener : fTableListeners.toArray()) {
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
					for (final IRDataTableListener listener : fTableListeners.toArray()) {
						listener.inputChanged(null, null);
					}
				}
			}
			
			@Override
			public void onRowCountChanged() {
				if (fTable != table || table.isDisposed()) {
					return;
				}
				
				dataLayer.fireLayerEvent(new RowStructuralRefreshEvent(dataLayer));
				
				fSelection = null;
				scheduleUpdateSelection(true, 0);
			}
			
			@Override
			public void onRowsChanged(final long beginIdx, final long endIdx) {
				fDisplay.asyncExec(new Runnable() {
					@Override
					public void run() {
						dataLayer.fireLayerEvent(new RowUpdateEvent(dataLayer, new Range(beginIdx, endIdx)));
					}
				});
			}
			
		});
		dataProvider.addFindListener(fFindListener);
	}
	
	public NatTable getNatTable() {
		return fTable;
	}
	
	protected void scheduleUpdateSelection(final boolean checkLabel, final int delay) {
		synchronized (fSelectionLock) {
			fSelectionUpdateScheduleStamp = System.nanoTime() + delay * 1000000L;
			if (checkLabel) {
				fSelectionCheckLabel = true;
			}
			if (fSelectionUpdateScheduled) {
				return;
			}
			fSelectionUpdateScheduled = true;
		}
		fDisplay.asyncExec(fSelectionUpdateRunnable);
	}
	
	protected void updateSelection() {
		final boolean checkLabel;
		synchronized (fSelectionLock) {
			final long diff = (fSelectionUpdateScheduleStamp - System.nanoTime()) / 1000000L;
			if (diff > 5) {
				fDisplay.timerExec((int) diff, fSelectionUpdateRunnable);
				return;
			}
			checkLabel = fSelectionCheckLabel;
			fSelectionCheckLabel = false;
			fSelectionUpdateScheduled = false;
		}
		final RDataTableSelection selection;
		if (fTable == null) {
			selection = new RDataTableSelection(null, null, null, null);
		}
		else {
			final SelectionLayer selectionLayer = fTableLayers.selectionLayer;
			final PositionCoordinate anchor = selectionLayer.getSelectionAnchor();
			PositionCoordinate lastSelected = selectionLayer.getLastSelectedCellPosition();
			if (lastSelected.equals(anchor)) {
				lastSelected = null;
			}
			
			final boolean anchorChanged;
			if ((anchorChanged = !anchor.equals(fCurrentAnchor))) {
				fCurrentAnchor = new PositionCoordinate(anchor);
			}
			final boolean lastSelectedChanged;
			if ((lastSelectedChanged = !((lastSelected != null) ?
					lastSelected.equals(fCurrentLastSelectedCell) : null == fCurrentLastSelectedCell ))) {
				fCurrentLastSelectedCell = (lastSelected != null) ? new PositionCoordinate(lastSelected) : null;
			}
			
			if (!checkLabel && !anchorChanged && !lastSelectedChanged) {
				return;
			}
			
			String anchorRowLabel = null;
			String anchorColumnLabel = null;
			if (fCurrentAnchor.columnPosition >= 0 && fCurrentAnchor.rowPosition >= 0) {
				if (anchorChanged || checkLabel) {
					anchorRowLabel = getRowLabel(fCurrentAnchor.rowPosition);
					if (anchorRowLabel != null) {
						anchorColumnLabel = getColumnLabel(fCurrentAnchor.columnPosition);
						if (anchorColumnLabel == null) {
							anchorRowLabel = null;
						}
					}
				}
				else if (fSelection != null) {
					anchorRowLabel = fSelection.getAnchorRowLabel();
					anchorColumnLabel = fSelection.getAnchorColumnLabel();
				}
			}
			
			if (anchorRowLabel == null) {
				return;
			}
			
			String lastSelectedRowLabel = null;
			String lastSelectedColumnLabel = null;
			if (fCurrentLastSelectedCell != null
					&& fCurrentLastSelectedCell.columnPosition >= 0 && fCurrentLastSelectedCell.rowPosition >= 0) {
				if (lastSelectedChanged || checkLabel) {
					lastSelectedRowLabel = getRowLabel(fCurrentLastSelectedCell.rowPosition);
					if (lastSelectedRowLabel != null) {
						lastSelectedColumnLabel = getColumnLabel(fCurrentLastSelectedCell.columnPosition);
						if (lastSelectedColumnLabel == null) {
							lastSelectedRowLabel = null;
						}
					}
				}
				else if (fSelection != null) {
					lastSelectedRowLabel = fSelection.getLastSelectedCellRowLabel();
					lastSelectedColumnLabel = fSelection.getLastSelectedCellColumnLabel();
				}
			}
			selection = new RDataTableSelection(
					anchorRowLabel, anchorColumnLabel,
					lastSelectedRowLabel, lastSelectedColumnLabel);
		}
		
		if (selection.equals(fSelection)) {
			return;
		}
		fSelection = selection;
		final SelectionChangedEvent event = new SelectionChangedEvent(this, fSelection);
		
		for (final ISelectionChangedListener listener : fSelectionListeners.toArray()) {
			listener.selectionChanged(event);
		}
	}
	
	protected String getRowLabel(final long row) {
		if (!fDataProvider.hasRealRows()) {
			return ""; //$NON-NLS-1$
		}
		final IDataProvider dataProvider = fDataProvider.getRowDataProvider();
		if (dataProvider.getColumnCount() <= 1) {
			return getHeaderLabel(dataProvider.getDataValue(0, row));
		}
		final StringBuilder sb = new StringBuilder();
		for (long i = 0; i < dataProvider.getColumnCount(); i++) {
			final String label = getHeaderLabel(dataProvider.getDataValue(i, row));
			if (label == null) {
				return null;
			}
			sb.append(label);
			sb.append(", "); //$NON-NLS-1$
		}
		return sb.substring(0, sb.length()-2);
	}
	
	protected String getColumnLabel(final long column) {
		if (!fDataProvider.hasRealColumns()) {
			return ""; //$NON-NLS-1$
		}
		final IDataProvider dataProvider = fDataProvider.getColumnDataProvider();
		if (dataProvider.getRowCount() <= 1) {
			return getHeaderLabel(dataProvider.getDataValue(column, 0));
		}
		final StringBuilder sb = new StringBuilder();
		for (long i = 0; i < dataProvider.getRowCount(); i++) {
			final String label = getHeaderLabel(dataProvider.getDataValue(column, i));
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
			final Object displayValue = fFormatter.modelToDisplayValue(value);
			if (displayValue.getClass() == String.class) {
				return (String) displayValue;
			}
			if (displayValue == InfoString.DUMMY) {
				return ""; //$NON-NLS-1$
			}
			return null;
		}
		else {
			return fFormatter.modelToDisplayValue(null).toString();
		}
	}
	
	protected void showDummy(final String message) {
		if (isDisposed()) {
			return;
		}
		fMessageControl.setText(message);
		fLayout.topControl = fMessageControl;
		layout();
	}
	
	
	public long[] getTableDimension() {
		if (fTable != null) {
			return new long[] { fDataProvider.getRowCount(), fDataProvider.getColumnCount() };
		}
		return null;
	}
	
	public boolean isOK() {
		return (fLayout.topControl == fTable);
	}
	
	public IViewportDim getViewport(final Orientation orientation) {
		return fTableLayers.viewportLayer.getDim(orientation);
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
		fSelectionListeners.add(listener);
	}
	
	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}
	
	
	public void find(final String expression, final boolean selectedOnly, final boolean firstInRow, final boolean forward) {
		if (fTable != null) {
			final PositionCoordinate anchor = fTableLayers.selectionLayer.getSelectionAnchor();
			final FindTask task = new FindTask(expression,
					anchor.getRowPosition(), anchor.getColumnPosition(), firstInRow, forward,
					(selectedOnly) ? new SelectionFindFilter() : null);
			fDataProvider.find(task);
		}
	}
	
	public void addFindListener(final IFindListener listener) {
		fFindListeners.add(listener);
	}
	
	public void removeFindListener(final IFindListener listener) {
		fFindListeners.remove(listener);
	}
	
	
	public void setInput(final IRDataTableInput input) {
		if (fTable != null) {
			showDummy(""); //$NON-NLS-1$
			fTable.dispose();
			fTable = null;
			fDataProvider = null;
			this.setAnchorByData= null;
		}
		if (input != null) {
			showDummy("Preparing (" + input.getName() + ")...");
			try {
				final IToolRunnable runnable = new ISystemRunnable() {
					
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
						final IRToolService r = (IRToolService) service;
						
						final AtomicReference<AbstractRDataProvider<?>> dataProvider= new AtomicReference<>();
						Exception error = null;
						try {
							final RObject struct = r.evalData(input.getFullName(),
									null, RObjectFactory.F_ONLY_STRUCT, 1, monitor);
							RCharacterStore classNames = null;
							{	final FunctionCall call = r.createFunctionCall("class"); //$NON-NLS-1$
								call.add(input.getFullName());
								classNames = RDataUtil.checkRCharVector(call.evalData(monitor)).getData();
							}
							
							switch (struct.getRObjectType()) {
							case RObject.TYPE_VECTOR:
								dataProvider.set(new RVectorDataProvider(input, (RVector<?>) struct));
								break;
							case RObject.TYPE_ARRAY: {
								final RArray<?> array = (RArray<?>) struct;
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
							error = e;
						}
						catch (final UnexpectedRDataException e) {
							error = e;
						}
						final IStatus status;
						if (error != null) {
							status = new Status(IStatus.ERROR, RUI.PLUGIN_ID,
									"An error occurred when preparing the R data viewer.", error );
							StatusManager.getManager().handle(status);
						}
						else if (dataProvider.get() == null) {
							status = new Status(IStatus.INFO, RUI.PLUGIN_ID,
									"This R element type is not supported.", null );
						}
						else {
							status = null;
						}
						fDisplay.asyncExec(new Runnable() {
							@Override
							public void run() {
								fDataProvider = null;
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
		if (fReloadControl == null) {
			fReloadControl = new Composite(this, SWT.NONE);
			fReloadControl.setLayout(LayoutUtil.createCompositeGrid(4));
			
			final Label label = new Label(fReloadControl, SWT.WRAP);
			label.setText("The structure of the R element is changed (columns / data type).");
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
			
			{	final Button button = new Button(fReloadControl, SWT.PUSH);
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
			{	final Button button = new Button(fReloadControl, SWT.PUSH);
				button.setLayoutData(LayoutUtil.hintWidth(new GridData(
						SWT.FILL, SWT.CENTER, false, false), button));
				button.setText("Reopen");
				button.setToolTipText("Reopen table with new structure");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						setInput(fInput);
					}
				});
			}
			if (fCloseRunnable != null) {
				final Button button = new Button(fReloadControl, SWT.PUSH);
				button.setLayoutData(LayoutUtil.hintWidth(new GridData(
						SWT.FILL, SWT.CENTER, false, false), button));
				button.setText("Close");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						fCloseRunnable.run();
					}
				});
			}
//			
			LayoutUtil.addSmallFiller(fReloadControl, true);
		}
		fLayout.topControl = fReloadControl;
		layout(true);
	}
	
	
	public void setFilter(final String filter) {
		fDataProvider.setFilter(filter);
	}
	
	
	public RDataTableContentDescription getDescription() {
		return fDataProvider.getDescription();
	}
	
	public void addTableListener(final IRDataTableListener listener) {
		fTableListeners.add(listener);
		if (fTable != null) {
			listener.inputChanged(fInput, fDataProvider.getDescription());
		}
	}
	
	public void removeTableListener(final IRDataTableListener listener) {
		fTableListeners.remove(listener);
	}
	
	
	@Override
	public boolean setFocus() {
		if (fLayout == null || fLayout.topControl == null) {
			return false;
		}
		return fLayout.topControl.forceFocus();
	}
	
	public void revealColumn(final long index) {
		if (fTable != null) {
			fTableLayers.viewportLayer.getDim(HORIZONTAL).movePositionIntoViewport(index);
		}
	}
	
	public void selectColumns(final Collection<Range> indexes) {
		if (this.fTable != null) {
			final RangeList columns = RangeList.toRangeList(indexes);
//			final long rowIndex = fTableBodyLayerStack.getViewportLayer().getRowIndexByPosition(0);
			final long rowIndex = 0;
			this.fTable.doCommand(new SelectColumnsCommand(this.fTableLayers.selectionLayer,
					columns, rowIndex, 0,
					!(columns.isEmpty()) ? columns.values().first() : SelectionLayer.NO_SELECTION ));
		}
	}
	
	public void setAnchorViewIdxs(final long columnIndex, final long rowIndex) {
		if (this.fTable != null) {
			this.fTableLayers.selectionLayer.setSelectionAnchor(columnIndex, rowIndex, true);
		}
	}
	
	public long[] getAnchorDataIdxs() {
		if (this.fTable != null) {
			final PositionCoordinate coordinate= this.fTableLayers.selectionLayer.getSelectionAnchor();
			if (coordinate.columnPosition < 0 || coordinate.rowPosition < 0) {
				return null;
			}
			return this.fDataProvider.toDataIdxs(coordinate.columnPosition, coordinate.rowPosition);
		}
		return null;
	}
	
	public void setAnchorDataIdxs(final long columnIdx, final long rowIdx) {
		if (this.fTable != null) {
			if (this.setAnchorByData == null) {
				this.setAnchorByData= new SetAnchorByDataIndexes(this.fDataProvider);
			}
			this.setAnchorByData.resolve(columnIdx, rowIdx);
		}
	}
	
	public void selectAll() {
		if (this.fTable != null) {
			this.fTable.doCommand(new SelectAllCommand());
		}
	}
	
	public void sortByColumn(final long index, final boolean increasing) {
		if (this.fTable != null) {
			final ISortModel sortModel = this.fDataProvider.getSortModel();
			if (sortModel != null) {
				sortModel.sort(
						index, increasing ? SortDirectionEnum.ASC : SortDirectionEnum.DESC, false );
				this.fTableLayers.topColumnHeaderLayer.fireLayerEvent(
						new SortColumnEvent(this.fTableLayers.topColumnHeaderLayer, 0) );
			}
		}
	}
	
	public void clearSorting() {
		if (this.fTable != null) {
			this.fTable.doCommand(new ClearSortCommand());
		}
	}
	
	public void refresh() {
		if (this.fTable != null) {
			this.fDataProvider.reset();
			this.fTable.redraw();
		}
	}
	
}
