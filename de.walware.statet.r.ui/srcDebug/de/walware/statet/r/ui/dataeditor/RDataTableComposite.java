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

package de.walware.statet.r.ui.dataeditor;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RVector;
import de.walware.rj.services.RService;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.copy.command.CopyDataCommandHandler;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.freeze.CompositeFreezeLayer;
import net.sourceforge.nattable.freeze.FreezeLayer;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.grid.layer.CornerLayer;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.grid.layer.RowHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import net.sourceforge.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IVisualChangeEvent;
import net.sourceforge.nattable.layer.event.RowStructuralRefreshEvent;
import net.sourceforge.nattable.layer.event.RowUpdateEvent;
import net.sourceforge.nattable.selection.SelectRelativelyCommandHandler;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.ISelectionCommand.SelectionFlag;
import net.sourceforge.nattable.selection.command.SelectAllCommand;
import net.sourceforge.nattable.selection.command.SelectColumnsCommand;
import net.sourceforge.nattable.selection.event.ISelectionEvent;
import net.sourceforge.nattable.sort.ISortModel;
import net.sourceforge.nattable.sort.SortDirectionEnum;
import net.sourceforge.nattable.sort.SortHeaderLayer;
import net.sourceforge.nattable.sort.command.ClearSortCommand;
import net.sourceforge.nattable.sort.event.SortColumnsEvent;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.tickupdate.config.DefaultTickUpdateConfiguration;
import net.sourceforge.nattable.viewport.ScrollCommandHandler;
import net.sourceforge.nattable.viewport.ViewportLayer;
import net.sourceforge.nattable.viewport.command.ViewportSelectColumnsCommandHandler;
import net.sourceforge.nattable.viewport.command.ViewportSelectRowCommandHandler;

import de.walware.statet.r.internal.ui.dataeditor.AbstractRDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.AbstractRDataProvider.FindTask;
import de.walware.statet.r.internal.ui.dataeditor.IFindFilter;
import de.walware.statet.r.internal.ui.dataeditor.IFindListener;
import de.walware.statet.r.internal.ui.dataeditor.RDataFormatter;
import de.walware.statet.r.internal.ui.dataeditor.RDataFormatterConverter;
import de.walware.statet.r.internal.ui.dataeditor.RDataFrameDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.RMatrixDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.RVectorDataProvider;
import de.walware.statet.r.internal.ui.intable.PresentationConfig;
import de.walware.statet.r.internal.ui.intable.RDataLayer;
import de.walware.statet.r.internal.ui.intable.TableLayers;
import de.walware.statet.r.internal.ui.intable.UIBindings;


public class RDataTableComposite extends Composite implements ISelectionProvider {
	
	
	private class FindListener implements IFindListener {
		
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
		
		public boolean match(final int rowIdx, final int columnIdx) {
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
		public void run() {
			updateSelection();
		}
	};
	private final FastList<ISelectionChangedListener> fSelectionListeners =
			new FastList<ISelectionChangedListener>(ISelectionChangedListener.class);
	private final FastList<IFindListener> fFindListeners =
			new FastList<IFindListener>(IFindListener.class);
	private final FastList<IRDataTableListener> fTableListeners =
			new FastList<IRDataTableListener>(IRDataTableListener.class);
	
	private final RDataFormatter fFormatter = new RDataFormatter();
	
	private final FindListener fFindListener = new FindListener();
	
	
	/**
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 */
	public RDataTableComposite(final Composite parent, final Runnable closeRunnable) {
		super(parent, SWT.NONE);
		
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
		final RDataLayer dataLayer = new RDataLayer(dataProvider);
		
		if (!fDataProvider.getAllColumnsEqual()) {
			final ColumnOverrideLabelAccumulator columnLabelAccumulator =
					new ColumnOverrideLabelAccumulator(dataLayer);
			for (int i = 0; i < fDataProvider.getColumnCount(); i++) {
				columnLabelAccumulator.registerColumnOverrides(i, ColumnOverrideLabelAccumulator.COLUMN_LABEL_PREFIX + i);
			}
			final AggregrateConfigLabelAccumulator aggregateLabelAccumulator =
					new AggregrateConfigLabelAccumulator();
			aggregateLabelAccumulator.add(columnLabelAccumulator);
			dataLayer.setConfigLabelAccumulator(aggregateLabelAccumulator);
		}
		
		final PresentationConfig presentation = new PresentationConfig(getDisplay());
		
		fTableLayers = new TableLayers();
//		final WColumnReorderLayer columnReorderLayer = new WColumnReorderLayer(dataLayer);
//		final ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		
		fTableLayers.selectionLayer = new SelectionLayer(dataLayer, false);
		fTableLayers.selectionLayer.addConfiguration(new UIBindings.SelectionConfiguration());
		fTableLayers.selectionLayer.addConfiguration(new DefaultTickUpdateConfiguration());
		
		fTableLayers.viewportLayer = new ViewportLayer(fTableLayers.selectionLayer);
		
		{	final ILayerCommandHandler<?> commandHandler = new ScrollCommandHandler(fTableLayers.viewportLayer);
			fTableLayers.viewportLayer.registerCommandHandler(commandHandler);
		}
		{	final ILayerCommandHandler<?> commandHandler = new SelectRelativelyCommandHandler(fTableLayers.selectionLayer);
			fTableLayers.viewportLayer.registerCommandHandler(commandHandler);
			fTableLayers.selectionLayer.registerCommandHandler(commandHandler);
		}
		{	final ILayerCommandHandler<?> commandHandler = new CopyDataCommandHandler(fTableLayers.selectionLayer);
			fTableLayers.selectionLayer.registerCommandHandler(commandHandler);
		}
		
		final FreezeLayer freezeLayer = new FreezeLayer(fTableLayers.selectionLayer);
		final CompositeFreezeLayer compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer,
				fTableLayers.viewportLayer, fTableLayers.selectionLayer, true);
		final AbstractLayer freezedColumnLayer = (AbstractLayer) compositeFreezeLayer.getChildLayerByLayoutCoordinate(0, 1);
		freezedColumnLayer.registerCommandHandler(new ViewportSelectColumnsCommandHandler(freezedColumnLayer));
		final AbstractLayer freezedRowLayer = (AbstractLayer) compositeFreezeLayer.getChildLayerByLayoutCoordinate(1, 0);
		freezedRowLayer.registerCommandHandler(new ViewportSelectRowCommandHandler(freezedRowLayer));
		fTableLayers.topBodyLayer = compositeFreezeLayer;
		
		final IDataProvider columnHeaderDataProvider = dataProvider.getColumnDataProvider();
		final ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(
				new DataLayer(columnHeaderDataProvider),
				fTableLayers.topBodyLayer, fTableLayers.selectionLayer,
				false, presentation.getHeaderLayerPainter() );
		columnHeaderLayer.addConfiguration(new UIBindings.ColumnHeaderConfiguration());
		
		final ISortModel sortModel = dataProvider.getSortModel();
		fTableLayers.sortColumnHeaderLayer = new SortHeaderLayer<Object>(
				columnHeaderLayer, sortModel, false);
		fTableLayers.sortColumnHeaderLayer.addConfiguration(new UIBindings.SortConfiguration());
		
		final IDataProvider rowHeaderDataProvider = dataProvider.getRowDataProvider();
		final RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(
				new DataLayer(rowHeaderDataProvider, 50, 20),
				fTableLayers.topBodyLayer, fTableLayers.selectionLayer,
				false, presentation.getHeaderLayerPainter() );
		
		final IDataProvider cornerDataProvider = new DefaultCornerDataProvider(
				columnHeaderDataProvider, rowHeaderDataProvider );
		final CornerLayer cornerLayer = new CornerLayer(
				new DataLayer(cornerDataProvider), rowHeaderLayer, fTableLayers.sortColumnHeaderLayer,
				presentation.getHeaderLayerPainter() );
		
		final GridLayer gridLayer = new GridLayer(fTableLayers.topBodyLayer,
				fTableLayers.sortColumnHeaderLayer, rowHeaderLayer, cornerLayer, false);
		gridLayer.setConfigLabelAccumulatorForRegion(GridRegion.BODY, new AlternatingRowConfigLabelAccumulator());
		
		final NatTable table = new NatTable(this, gridLayer, false);
		table.addConfiguration(presentation);
		table.addConfiguration(new UIBindings.HeaderContextMenuConfiguration(table));
		
		table.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				dataProvider.dispose();
				presentation.dispose();
			}
		});
		
		final IConfigRegistry registry = table.getConfigRegistry();
		registry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				new RDataFormatterConverter.DataColumn(dataProvider, -1));
		registry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				new RDataFormatterConverter.RowHeader(dataProvider),
				DisplayMode.NORMAL, GridRegion.ROW_HEADER);
		if (!dataProvider.getAllColumnsEqual()) {
			for (int i = 0; i < dataProvider.getColumnCount(); i++) {
				registry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
						new RDataFormatterConverter.DataColumn(dataProvider, i),
						DisplayMode.NORMAL, ColumnOverrideLabelAccumulator.COLUMN_LABEL_PREFIX + i);
			}
		}
		
		fTable = table;
		fTableInitialized = false;
		fTable.addLayerListener(new ILayerListener() {
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
			public void onInputInitialized(final boolean structChanged) {
				if (fTable != table || table.isDisposed()) {
					return;
				}
				
				if (fTable != fLayout.topControl) {
					table.configure();
				}
				
				final GC gc = new GC(table);
				gc.setFont(presentation.getBaseFont());
				final FontMetrics fontMetrics = gc.getFontMetrics();
				final int charWidth = (gc.textExtent("1234567890.-120").x + 5) / 15;
				dataLayer.setSizeConfig(fontMetrics.getHeight(), charWidth);
				gc.dispose();
				
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
			
//			public void onRowCountChanged() {
//				dataLayer.fireLayerEvent(new RowStructuralRefreshEvent(dataLayer));
//			}
			
			public void onRowsChanged(final int beginIdx, final int endIdx) {
				dataLayer.fireLayerEvent(new RowUpdateEvent(dataLayer, new Range(beginIdx, endIdx)));
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
		getDisplay().asyncExec(fSelectionUpdateRunnable);
	}
	
	protected void updateSelection() {
		final boolean checkLabel;
		synchronized (fSelectionLock) {
			final long diff = (fSelectionUpdateScheduleStamp - System.nanoTime()) / 1000000L;
			if (diff > 5) {
				getDisplay().timerExec((int) diff, fSelectionUpdateRunnable);
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
			if (lastSelected != null && lastSelected.equals(anchor)) {
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
	
	protected String getRowLabel(final int row) {
		final Object value = fDataProvider.getRowDataProvider().getDataValue(0, row);
		if (value != null) {
			final Object displayValue = fFormatter.modelToDisplayValue(value);
			if (displayValue.getClass() == String.class) {
				return (String) displayValue;
			}
			return null;
		}
		else {
			return fFormatter.modelToDisplayValue(null).toString();
		}
	}
	
	protected String getColumnLabel(final int column) {
		final Object value = fDataProvider.getColumnDataProvider().getDataValue(column, 0);
		if (value != null) {
			final Object displayValue = fFormatter.modelToDisplayValue(value);
			if (displayValue.getClass() == String.class) {
				return (String) displayValue;
			}
			return null;
		}
		else {
			return fFormatter.modelToDisplayValue(null).toString();
		}
	}
	
	protected void showDummy(final String message) {
		fMessageControl.setText(message);
		fLayout.topControl = fMessageControl;
		layout();
	}
	
	
	public int[] getTableDimension() {
		if (fTable != null) {
			return new int[] { fDataProvider.getRowCount(), fDataProvider.getColumnCount() };
		}
		return null;
	}
	
	public ISelection getSelection() {
		return null;
	}
	
	public void setSelection(final ISelection selection) {
	}
	
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}
	
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
		}
		if (input != null) {
			showDummy("Preparing (" + input.getLastName() + ")...");
			try {
				((RProcessDataTableInput) input).run(new ISystemRunnable() {
					
					public String getTypeId() {
						return "r/dataeditor/init"; //$NON-NLS-1$
					}
					
					public String getLabel() {
						return "Prepare Data Viewer (" + input.getLastName() + ")";
					}
					
					public boolean isRunnableIn(final ITool tool) {
						return true; // TODO
					}
					
					public boolean changed(final int event, final ITool process) {
						if (event == MOVING_FROM) {
							return false;
						}
						return true;
					}
					
					public void run(final IToolService service,
							final IProgressMonitor monitor) throws CoreException {
						final RService r = (RService) service;
						final RObject struct = r.evalData(input.getFullName(),
								null, RObjectFactory.F_ONLY_STRUCT, 1, monitor);
						
						final AbstractRDataProvider<?> dataProvider;
						switch (struct.getRObjectType()) {
						case RObject.TYPE_VECTOR:
							dataProvider = new RVectorDataProvider(input, (RVector<?>) struct);
							break;
						case RObject.TYPE_ARRAY: {
							final RArray<?> array = (RArray<?>) struct;
							if (array.getDim().getLength() == 2) {
								dataProvider = new RMatrixDataProvider(input, array);
								break;
							}
							dataProvider = null;
							break; }
						case RObject.TYPE_DATAFRAME:
							dataProvider = new RDataFrameDataProvider(input, (RDataFrame) struct);
							break;
						default:
							dataProvider = null;
							break;
						}
						UIAccess.getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (dataProvider != null) {
									initTable(input, dataProvider);
								}
								else {
									showDummy("This R element type is not supported.");
								}
							}
						});
					}
				});
			}
			catch (final CoreException e) {
				showDummy(e.getLocalizedMessage());
			}
		}
	}
	
	protected void showReload() {
		if (fReloadControl == null) {
			fReloadControl = new Composite(this, SWT.NONE);
			fReloadControl.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(0, false), 4));
			
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
	
	public void revealColumn(final int index) {
		if (fTable != null) {
			fTableLayers.viewportLayer.moveColumnPositionIntoViewport(
					index, true);
		}
	}
	
	public void selectColumns(final Collection<Integer> indexes) {
		if (fTable != null) {
//			final int rowIndex = fTableBodyLayerStack.getViewportLayer().getRowIndexByPosition(0);
			final int rowIndex = 0;
			fTable.doCommand(new SelectColumnsCommand(
					fTableLayers.selectionLayer, indexes, rowIndex, SelectionFlag.NONE));
		}
	}
	
	public void selectAll() {
		if (fTable != null) {
			fTable.doCommand(new SelectAllCommand());
		}
	}
	
	public void sortByColumn(final int index, final boolean increasing) {
		if (fTable != null) {
			fDataProvider.getSortModel().sort(
					index, increasing ? SortDirectionEnum.ASC : SortDirectionEnum.DESC, false );
			fTableLayers.sortColumnHeaderLayer.fireLayerEvent(new SortColumnsEvent(
					fTableLayers.sortColumnHeaderLayer, 0) ); // 
		}
	}
	
	public void clearSorting() {
		if (fTable != null) {
			fTable.doCommand(new ClearSortCommand());
		}
	}
	
	public RDataTableColumn[] getColumns() {
		if (fTable != null) {
			return fDataProvider.getDescription().dataColumns;
		}
		return null;
	}
	
	public void refresh() {
		if (fTable != null) {
			fDataProvider.reset();
			fTable.redraw();
		}
	}
	
}
