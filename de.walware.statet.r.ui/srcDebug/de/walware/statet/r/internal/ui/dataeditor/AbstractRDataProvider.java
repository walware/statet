/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.List;

import com.ibm.icu.util.TimeZone;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.FastList;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RFactorDataStruct;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;
import de.walware.rj.services.RService;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.sort.ISortModel;
import net.sourceforge.nattable.sort.SortDirectionEnum;

import de.walware.statet.r.internal.ui.dataeditor.IFindListener.FindEvent;
import de.walware.statet.r.internal.ui.dataeditor.Store.Item;
import de.walware.statet.r.internal.ui.dataeditor.Store.LoadDataException;
import de.walware.statet.r.internal.ui.dataeditor.Store.Lock;
import de.walware.statet.r.internal.ui.intable.InfoString;
import de.walware.statet.r.nico.ICombinedRDataAdapter;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;
import de.walware.statet.r.ui.dataeditor.RProcessDataTableInput;


public abstract class AbstractRDataProvider<T extends RObject> implements IDataProvider {
	
	
	public static final Object LOADING = new InfoString("LOADING");
	public static final Object ERROR = new InfoString("ERROR");
	
	
	public static void checkCancel(final Exception e) throws CoreException {
		if (e instanceof CoreException
				&& ((CoreException) e).getStatus().getSeverity() == IStatus.CANCEL) {
			throw (CoreException) e;
		}
	}
	
	
	public static final class SortColumn {
		
		
		public final int columnIdx;
		public final boolean decreasing;
		
		
		public SortColumn(final int columnIdx, final boolean decreasing) {
			this.columnIdx = columnIdx;
			this.decreasing = decreasing;
		}
		
		
		@Override
		public int hashCode() {
			return (decreasing) ? (-columnIdx) : columnIdx;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof SortColumn)) {
				return false;
			}
			final SortColumn other = (SortColumn) obj;
			return (columnIdx == other.columnIdx && decreasing == other.decreasing);
		}
		
	}
	
	public static final class FindTask {
		
		
		public final String expression;
		
		public final int rowIdx;
		public final int columnIdx;
		public final boolean firstInRow;
		public final boolean forward;
		
		public final IFindFilter filter;
		
		
		public FindTask(final String expression,
				final int rowIdx, final int columnIdx,
				final boolean firstByRow, final boolean forward,
				final IFindFilter filter) {
			this.expression = expression;
			
			this.rowIdx = rowIdx;
			this.columnIdx = columnIdx;
			this.firstInRow = firstByRow;
			this.forward = forward;
			
			this.filter = filter;
		}
		
		
		@Override
		public int hashCode() {
			return expression.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof FindTask)) {
				return false;
			}
			final FindTask other = (FindTask) obj;
			return (expression.equals(other.expression)
					&& firstInRow == other.firstInRow);
		}
		
	}
	
	public static interface IDataProviderListener {
		
		
		static final int ERROR_STRUCT_CHANGED = 1;
		
		
		void onInputInitialized(boolean structChanged);
		
		void onInputFailed(int error);
		
//		void onRowCountChanged();
		
		void onRowsChanged(int begin, int end);
		
	}
	
	
	private static final int FIND_CELL = 1;
	private static final int FIND_ROW = 2;
	private static final int FIND_ERROR = -1;
	
	
	private class MainLock extends Lock {
		
		boolean scheduled;
		Object waiting;
		
		@Override
		void schedule(final Object obj) {
			if (!scheduled) {
				scheduled = true;
				AbstractRDataProvider.this.schedule(fUpdateRunnable);
				if (obj != null) {
					waiting = obj;
					try {
						fFragmentsLock.wait(25);
					}
					catch (final InterruptedException e) {
					}
					finally {
						if (waiting == obj) {
							waiting = null;
						}
					}
				}
			}
		}
		
		void notify(final Object obj) {
			if (obj == waiting) {
				notifyAll();
			}
		}
		
	}
	
	private class FindLock extends Lock {
		
		boolean scheduled;
		
		@Override
		void schedule(final Object obj) {
			if (obj != null && state > 0) {
				return;
			}
			if (!scheduled) {
				scheduled = true;
				AbstractRDataProvider.this.schedule(fFindRunnable);
			}
		}
		
	}
	
	protected class ColumnDataProvider implements IDataProvider {
		
		
		public ColumnDataProvider() {
		}
		
		
		@Override
		public int getColumnCount() {
			return AbstractRDataProvider.this.getColumnCount();
		}
		
		@Override
		public int getRowCount() {
			return 1;
		}
		
		@Override
		public Object getDataValue(final int columnIndex, final int rowIndex) {
			try {
				final Store.Fragment<T> fragment = fDataStore.getFor(0, columnIndex);
				if (fragment != null) {
					return getColumnName(fragment, columnIndex);
				}
				else {
					return LOADING;
				}
			}
			catch (final LoadDataException e) {
				return handleLoadDataException(e);
			}
		}
		
		@Override
		public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	protected class RowDataProvider implements IDataProvider {
		
		
		public RowDataProvider() {
		}
		
		
		@Override
		public int getColumnCount() {
			return 1;
		}
		
		@Override
		public int getRowCount() {
			return AbstractRDataProvider.this.getRowCount();
		}
		
		@Override
		public Object getDataValue(final int columnIndex, final int rowIndex) {
			try {
				final Store.Fragment<RVector<?>> fragment = fRowNamesStore.getFor(rowIndex, 0);
				if (fragment != null) {
					return fragment.rObject.getData().get(rowIndex - fragment.beginRowIdx);
				}
				else {
					return LOADING;
				}
			}
			catch (final LoadDataException e) {
				return handleLoadDataException(e);
			}
		}
		
		@Override
		public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	protected class SortModel implements ISortModel {
		
		
		@Override
		public void sort(final int columnIndex, final SortDirectionEnum sortDirection, final boolean accumulate) {
			SortColumn sortColumn;
			switch (sortDirection) {
			case ASC:
				sortColumn = new SortColumn(columnIndex, false);
				break;
			case DESC:
				sortColumn = new SortColumn(columnIndex, true);
				break;
			default:
				sortColumn = null;
				break;
			}
			setSortColumn(sortColumn);
		}
		
		@Override
		public int getSortOrder(final int columnIndex) {
			final SortColumn sortColumn = getSortColumn();
			if (sortColumn != null && sortColumn.columnIdx == columnIndex) {
				return 0;
			}
			return -1;
		}
		
		@Override
		public boolean isColumnIndexSorted(final int columnIndex) {
			final SortColumn sortColumn = getSortColumn();
			if (sortColumn != null && sortColumn.columnIdx == columnIndex) {
				return true;
			}
			return false;
		}
		
		@Override
		public SortDirectionEnum getSortDirection(final int columnIndex) {
			final SortColumn sortColumn = getSortColumn();
			if (sortColumn != null && sortColumn.columnIdx == columnIndex) {
				return (!sortColumn.decreasing) ? SortDirectionEnum.ASC : SortDirectionEnum.DESC;
			}
			return SortDirectionEnum.NONE;
		}
		
		@Override
		public void clear() {
			setSortColumn(null);
		}
		
	}
	
	
	private final IToolRunnable fInitRunnable = new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/init";
		}
		
		@Override
		public String getLabel() {
			return "Prepare Data Viewer (" + fInput.getLastName() + ")";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return true; // TODO
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			runInit((RService) service, monitor);
		}
		
	};
	
	private final IToolRunnable fUpdateRunnable = new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/load"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Load Data (" + fInput.getLastName() + ")";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return true; // TODO
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case MOVING_FROM:
				return false;
			case REMOVING_FROM:
			case BEING_ABANDONED:
				synchronized (fFragmentsLock) {
					fFragmentsLock.scheduled = false;
					fFragmentsLock.notifyAll();
				}
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			runUpdate((RService) service, monitor);
		}
		
	};
	
	private final IToolRunnable fFindRunnable = new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/find"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Find Data (" + fInput.getLastName() + ")";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return true; // TODO
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			switch (event) {
			case MOVING_FROM:
				return false;
			case REMOVING_FROM:
			case BEING_ABANDONED:
				synchronized (fFindLock) {
					fFindLock.scheduled = false;
					fFindLock.notifyAll();
				}
				break;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			runFind((RService) service, monitor);
		}
		
	};
	
	private final IToolRunnable fCleanRunnable = new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/clean"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Clean Cache (" + fInput.getLastName() + ")";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return true; // TODO
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case MOVING_FROM:
			case REMOVING_FROM:
				return false;
			default:
				break;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			runClean((RService) service, monitor);
		}
		
	};
	
	
	protected final IRDataTableInput fInput;
	
	private final int fColumnCount;
	private int fRowCount;
	
	private final FastList<IDataProviderListener> fDataListeners =
			new FastList<IDataProviderListener>(IDataProviderListener.class);
	
	private boolean fInitScheduled;
	
	protected RDataTableContentDescription fDescription;
	
	private final IDataProvider fRowDataProvider;
	private final IDataProvider fColumnDataProvider;
	
	private final MainLock fFragmentsLock = new MainLock();
	
	private final Store<T> fDataStore;
	private final Store<RVector<?>> fRowNamesStore;
	
	private boolean fUpdateSorting;
	
	private final StringBuilder fRStringBuilder = new StringBuilder(128);
	private String fRCacheId; // only in R jobs
	private T fRObjectStruct;
	
	private final ISortModel fSortModel;
	private SortColumn fSortColumn = null;
	private String fRCacheSort; // only in R jobs
	private String fRCacheSortR; // only in R jobs
	
	private FindTask fFindScheduledTask;
	
	private String fRCacheFind; // only in R jobs
	private FindTask fFindCurrentTask;
	private int fFindActiveMode;
	private String fFindActiveExpression;
	private int fFindCount;
	private int fFindLastMatchIdx;
	
	private final FindLock fFindLock = new FindLock();
	private final Store<RObject> fFindStore;
	
	private final FastList<IFindListener> fFindListeners = new FastList<IFindListener>(IFindListener.class);
	
	
	protected AbstractRDataProvider(final IRDataTableInput input, final T initialRObject) {
		fInput = input;
		
		fRowCount = getRowCount(initialRObject);
		fColumnCount = getColumnCount(initialRObject);
		
		final int dataMax;
		if (fColumnCount <= 25) {
			dataMax = 10;
		}
		else if (fColumnCount <= 50) {
			dataMax = 20;
		}
		else {
			dataMax = 25;
		}
		fDataStore = new Store<T>(fFragmentsLock, fColumnCount, fRowCount, dataMax);
		fRowNamesStore = new Store<RVector<?>>(fFragmentsLock, 1, fRowCount, 10);
		fFindStore = new Store<RObject>(fFindLock, 1, 0, 5);
		
		fColumnDataProvider = createColumnDataProvider();
		fRowDataProvider = createRowDataProvider();
		fSortModel = createSortModel();
	}
	
	
	public abstract RObject getRObject();
	
	private void schedule(final IToolRunnable runnable) {
		try {
			((RProcessDataTableInput) fInput).run(runnable);
		}
		catch (final CoreException e) {
			clear(Lock.ERROR_STATE, -1, true);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when scheduling job for data viewer.", e));
		}
	}
	
	private void runInit(final RService r, final IProgressMonitor monitor) throws CoreException {
		try {
			if (fRCacheId == null) {
				r.evalVoid("require(\"rj\", quietly = TRUE)", monitor);
				fRCacheId = RDataUtil.checkSingleCharValue(r.evalData(".rj.nextId(\"viewer\")", monitor));
			}
		}
		catch (final Exception e) {
			synchronized (fInitRunnable) {
				fInitScheduled = false;
			}
			checkCancel(e);
			clear(Lock.ERROR_STATE, -1, true);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when preparing tmp variables for data viewer.", e));
			
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					for (final IDataProviderListener listener : fDataListeners.toArray()) {
						listener.onInputFailed(0);
					}
				}
			});
			return;
		}
		
		try {
			final RObject rObject = (r instanceof ICombinedRDataAdapter) ?
					((ICombinedRDataAdapter) r).evalCombinedStruct(fInput.getElementName(), 0, 1, monitor) :
						r.evalData(fInput.getFullName(), null, RObjectFactory.F_ONLY_STRUCT, 1, monitor) ;
			fRObjectStruct = validateObject(rObject);
		}
		catch (final Exception e) {
			synchronized (fInitRunnable) {
				fInitScheduled = false;
			}
			checkCancel(e);
			clear(Lock.RELOAD_STATE, -1, true);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when initializing structure data for data viewer.", e));
			
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					for (final IDataProviderListener listener : fDataListeners.toArray()) {
						listener.onInputFailed(IDataProviderListener.ERROR_STRUCT_CHANGED);
					}
				}
			});
			return;
		}
		final RDataTableContentDescription description;
		try {
			description = loadDescription(fRObjectStruct, r, monitor);
		}
		catch (final Exception e) {
			synchronized (fInitRunnable) {
				fInitScheduled = false;
			}
			checkCancel(e);
			clear(Lock.RELOAD_STATE, -1, true);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when initializing default formats for data viewer.", e));
			return;
		}
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				fDescription = description;
				final int rowCount = getRowCount(fRObjectStruct);
				final boolean rowsChanged = (rowCount != getRowCount());
				clear(0, rowCount, true);
				
				synchronized (fInitRunnable) {
					fInitScheduled = false;
				}
//				if (rowsChanged) {
//					for (final IDataProviderListener listener : dataListeners) {
//						listener.onRowCountChanged();
//					}
//				}
				for (final IDataProviderListener listener : fDataListeners.toArray()) {
					listener.onInputInitialized(rowsChanged);
				}
			}
		});
	}
	
	private void runUpdate(final RService r, final IProgressMonitor monitor) throws CoreException {
		boolean work = true;
		while (work) {
			try {
				final Item<T>[] dataToUpdate;
				final Item<RVector<?>>[] rowNamesToUpdate;
				
				boolean updateSorting = false;
				SortColumn sortColumn = null;
				work = false;
				
				synchronized (fFragmentsLock) {
					dataToUpdate = fDataStore.internalForUpdate();
					rowNamesToUpdate = fRowNamesStore.internalForUpdate();
					
					if (fUpdateSorting) {
						updateSorting = true;
						sortColumn = fSortColumn;
					}
					
					fFragmentsLock.scheduled = false;
				}
				
				if (updateSorting) {
					updateSorting(sortColumn, r, monitor);
					work = true;
				}
				
				for (int i = 0; i < rowNamesToUpdate.length; i++) {
					final Item<RVector<?>> item = rowNamesToUpdate[i];
					if (item == null) {
						break;
					}
					work = true;
					synchronized (fFragmentsLock) {
						if (!item.scheduled) {
							continue;
						}
					}
					final RVector<?> fragment = loadRowNamesFragment(item, r, monitor);
					synchronized (fFragmentsLock) {
						if (!item.scheduled) {
							continue;
						}
						item.fragment = new Store.Fragment<RVector<?>>(fragment,
								item.beginRowIdx, item.endRowIdx,
								item.beginColumnIdx, item.endColumnIdx );
						item.scheduled = false;
						
						fFragmentsLock.notify(item);
					}
					notifyListener(item);
				}
				for (int i = 0; i < dataToUpdate.length; i++) {
					final Item<T> item = dataToUpdate[i];
					if (item == null) {
						break;
					}
					work = true;
					synchronized (fFragmentsLock) {
						if (!item.scheduled) {
							continue;
						}
					}
					final T fragment = loadDataFragment(item, r, monitor);
					synchronized (fFragmentsLock) {
						if (!item.scheduled) {
							continue;
						}
						item.fragment = new Store.Fragment<T>(fragment,
								item.beginRowIdx, item.endRowIdx,
								item.beginColumnIdx, item.endColumnIdx );
						item.scheduled = false;
						
						fFragmentsLock.notify(item);
					}
					notifyListener(item);
				}
			}
			catch (final Exception e) {
				checkCancel(e);
				clear(Lock.RELOAD_STATE, -1, true);
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						NLS.bind("An error occurred when loading data of ''{0}'' for data viewer.", fInput.getFullName()), e));
				return;
			}
		}
	}
	
	private void updateSorting(final SortColumn sortColumn,
			final RService r, final IProgressMonitor monitor) throws UnexpectedRDataException, CoreException {
		if (sortColumn == null) {
			cleanSorting(r, monitor);
		}
		else {
			if (fRCacheSortR != null) {
				cleanTmp(fRCacheSortR, r, monitor);
				fRCacheSortR = null;
			}
			if (fRCacheSort == null) {
				fRCacheSort = fRCacheId + ".order";
			}
			final StringBuilder cmd = getRCmdStringBuilder();
			cmd.append("assign(\"").append(fRCacheSort).append("\", envir = .rj.tmp, value = ");
			appendOrderCmd(cmd, sortColumn);
			cmd.append(")");
			r.evalVoid(cmd.toString(), monitor);
		}
		synchronized (fFragmentsLock) {
			if (fSortColumn == sortColumn) {
				fUpdateSorting = false;
			}
		}
	}
	
	protected abstract int getColumnCount(T struct);
	protected abstract int getRowCount(T struct);
	
	protected abstract T loadDataFragment(Store.Fragment<T> f,
			RService r, IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
	
	protected abstract RVector<?> loadRowNamesFragment(final Store.Fragment<RVector<?>> f,
			RService r, IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
	
	protected abstract T validateObject(RObject struct) throws UnexpectedRDataException;
	
	protected abstract RDataTableContentDescription loadDescription(T struct,
			RService r, IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
	
	
	protected RDataTableColumn createColumn(final RStore store, final String expression,
			final int columnIndex, final String columnName,
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		
		final ConstList<String> classNames;
		
		RObject rObject;
		{	final StringBuilder cmd = getRCmdStringBuilder();
			cmd.append("class(").append(expression).append(')');
			rObject = r.evalData(cmd.toString(), monitor);
			final RVector<RCharacterStore> names = RDataUtil.checkRCharVector(rObject);
			classNames = new ConstList<String>(names.getData().toArray());
		}
		RDataTableColumn column;
		final RDataFormatter format = new RDataFormatter();
		switch (store.getStoreType()) {
		case RStore.LOGICAL:
			format.setAutoWidth(5);
			column = new RDataTableColumn(columnIndex, columnName,
					RDataTableColumn.LOGI, store, classNames, format);
			break;
		case RStore.NUMERIC:
			if (checkDateFormat(expression, classNames, format, r, monitor)) {
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.DATE, store, classNames, format);
				break;
			}
			if (checkDateTimeFormat(expression, classNames, format, r, monitor)) {
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.DATETIME, store, classNames, format);
				break;
			}
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("format.info(").append(expression).append(')');
				rObject = r.evalData(cmd.toString(), monitor);
			}
			{	final RIntegerStore formatInfo = RDataUtil.checkRIntVector(rObject).getData();
				RDataUtil.checkLengthGreaterOrEqual(formatInfo, 3);
				format.setAutoWidth(Math.max(formatInfo.getInt(0), 3));
				format.initNumFormat(formatInfo.getInt(1), formatInfo.getInt(2) > 0 ?
						formatInfo.getInt(2) + 1 : 0);
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.NUM, store, classNames, format);
				break;
			}
		case RStore.INTEGER:
			if (checkDateFormat(expression, classNames, format, r, monitor)) {
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.DATE, store, classNames, format);
				break;
			}
			if (checkDateTimeFormat(expression, classNames, format, r, monitor)) {
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.DATETIME, store, classNames, format);
				break;
			}
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("format.info(").append(expression).append(')');
				rObject = r.evalData(cmd.toString(), monitor);
			}
			{	final RIntegerStore formatInfo = RDataUtil.checkRIntVector(rObject).getData();
				RDataUtil.checkLengthGreaterOrEqual(formatInfo, 1);
				format.setAutoWidth(Math.max(formatInfo.getInt(0), 3));
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.INT, store, classNames, format);
				break;
			}
		case RStore.CHARACTER:
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("format.info(").append(expression).append(')');
				rObject = r.evalData(cmd.toString(), monitor);
			}
			{	final RIntegerStore formatInfo = RDataUtil.checkRIntVector(rObject).getData();
				RDataUtil.checkLengthGreaterOrEqual(formatInfo, 1);
				format.setAutoWidth(Math.max(formatInfo.getInt(0), 3));
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.CHAR, store, classNames, format);
				break;
			}
		case RStore.COMPLEX:
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("format.info(").append(expression).append(')');
				rObject = r.evalData(cmd.toString(), monitor);
			}
			{	final RIntegerStore formatInfo = RDataUtil.checkRIntVector(rObject).getData();
				RDataUtil.checkLengthGreaterOrEqual(formatInfo, 3);
				format.setAutoWidth(Math.max(formatInfo.getInt(0), 3));
				format.initNumFormat(formatInfo.getInt(1), formatInfo.getInt(2) > 0 ?
						formatInfo.getInt(2) + 1 : 0);
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.CPLX, store, classNames, format);
				break;
			}
		case RStore.RAW:
			format.setAutoWidth(2);
			column = new RDataTableColumn(columnIndex, columnName,
					RDataTableColumn.RAW, store, classNames, format);
			break;
		case RStore.FACTOR:
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("levels(").append(expression).append(')');
				rObject = r.evalData(cmd.toString(), monitor);
			}
			{	format.setAutoWidth(3);
				final RCharacterStore levels = RDataUtil.checkRCharVector(rObject).getData();
				for (int i = 0; i < levels.getLength(); i++) {
					if (!levels.isNA(i)) {
						final int length = levels.getChar(i).length();
						if (length > format.getAutoWidth()) {
							format.setAutoWidth(length);
						}
					}
				}
				format.initFactorLevels(levels);
				column = new RDataTableColumn(columnIndex, columnName,
						RDataTableColumn.FACTOR, RFactorDataStruct.addLevels((RFactorStore) store, levels),
						classNames, format);
				break;
			}
		default:
			throw new UnexpectedRDataException("store type: " + store.getStoreType());
		}
		return column;
	}
	
	protected boolean checkDateFormat(final String expression, final List<String> classNames,
			final RDataFormatter formatter,
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		if (classNames.contains("Data")) {
			formatter.initDateFormat(RDataFormatter.MILLIS_PER_DAY);
			formatter.setAutoWidth(10);
			return true;
		}
		return false;
	}
	
	protected boolean checkDateTimeFormat(final String expression, final List<String> classNames,
			final RDataFormatter formatter,
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		RObject rObject;
		if (classNames.contains("POSIXct")) {
			formatter.initDateTimeFormat(RDataFormatter.MILLIS_PER_SECOND);
			formatter.setAutoWidth(27);
			
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("attr(").append(expression).append(",\"tzone\")");
				rObject = r.evalData(cmd.toString(), monitor);
			}
			if (rObject.getRObjectType() != RObject.TYPE_NULL) {
				formatter.setDateTimeZone(TimeZone.getTimeZone(RDataUtil.checkSingleCharValue(rObject)));
			}
			return true;
		}
		return false;
	}
	
	protected RDataTableColumn createNamesColumn(final String expression, final int count,
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RObject names = r.evalData(expression, null, RObjectFactory.F_ONLY_STRUCT, 1, monitor);
		if (names != null && names.getRObjectType() == RObject.TYPE_VECTOR
				&& names.getLength() == count
				&& (names.getData().getStoreType() == RStore.CHARACTER
						|| names.getData().getStoreType() == RStore.INTEGER)) {
			return createColumn(names.getData(), expression, -1, null, r, monitor);
		}
		return createAutoNamesColumn(count);
	}
	
	private RDataTableColumn createAutoNamesColumn(final int count) {
		final RDataFormatter format = new RDataFormatter();
		format.setAutoWidth(Math.max(Integer.toString(count).length(), 3));
		return new RDataTableColumn(-1, null,
				RDataTableColumn.INT, RObjectFactoryImpl.INT_STRUCT_DUMMY,
				new ConstList<String>(RObject.CLASSNAME_INTEGER),
				format);
	}
	
	protected abstract void appendOrderCmd(StringBuilder cmd, SortColumn sortColumn);
	
	
	private void runClean(final RService r, final IProgressMonitor monitor) throws CoreException {
		clear(Lock.ERROR_STATE, -1, true);
		cleanSorting(r, monitor);
		cleanFinding(r, monitor);
		cleanTmp(fRCacheId, r, monitor);
	}
	
	private void cleanSorting(final RService r, final IProgressMonitor monitor) throws CoreException {
		if (fRCacheSort != null) {
			cleanTmp(fRCacheSort, r, monitor);
			fRCacheSort = null;
		}
		if (fRCacheSortR != null) {
			cleanTmp(fRCacheSortR, r, monitor);
			fRCacheSortR = null;
		}
	}
	
	private void cleanFinding(final RService r, final IProgressMonitor monitor) throws CoreException {
		if (fRCacheFind != null) {
			cleanTmp(fRCacheFind, r, monitor);
			fRCacheFind = null;
		}
	}
	
	private void cleanTmp(final String tmp, final RService r, final IProgressMonitor monitor) throws CoreException {
		final StringBuilder cmd = getRCmdStringBuilder();
		cmd.append("if ");
		cmd.append("(exists(\"").append(tmp).append("\", envir = .rj.tmp, inherits = FALSE)) ");
		cmd.append("{");
		cmd.append("rm(\"").append(tmp).append("\", envir = .rj.tmp, inherits = FALSE)");
		cmd.append("}");
		r.evalVoid(cmd.toString(), monitor);
	}
	
	
	protected StringBuilder getRCmdStringBuilder() {
		fRStringBuilder.setLength(0);
		return fRStringBuilder;
	}
	
	protected void appendRowIdxs(final StringBuilder cmd, final int beginRowIdx, final int endRowIdx) {
		if (fRCacheSort != null) {
			cmd.append(".rj.tmp$");
			cmd.append(fRCacheSort);
			cmd.append('[');
			cmd.append((beginRowIdx + 1));
			cmd.append('L');
			cmd.append(':');
			cmd.append(endRowIdx);
			cmd.append('L');
			cmd.append(']');
		}
		else {
			cmd.append((beginRowIdx + 1));
			cmd.append('L');
			cmd.append(':');
			cmd.append(endRowIdx);
			cmd.append('L');
		}
	}
	
	protected void appendColumnIdxs(final StringBuilder cmd, final int beginColumnIdx, final int endColumnIdx) {
		cmd.append((beginColumnIdx + 1));
		cmd.append('L');
		cmd.append(':');
		cmd.append(endColumnIdx);
		cmd.append('L');
	}
	
	
	public boolean getAllColumnsEqual() {
		return false;
	}
	
	public RDataTableContentDescription getDescription() {
		return fDescription;
	}
	
	@Override
	public int getColumnCount() {
		return fColumnCount;
	}
	
	@Override
	public int getRowCount() {
		return fRowCount;
	}
	
	@Override
	public Object getDataValue(final int columnIndex, final int rowIndex) {
		try {
			final Store.Fragment<T> fragment = fDataStore.getFor(rowIndex, columnIndex);
			if (fragment != null) {
				return getDataValue(fragment, rowIndex, columnIndex);
			}
			else {
				return LOADING;
			}
		}
		catch (final LoadDataException e) {
			return handleLoadDataException(e);
		}
	}
	
	protected abstract Object getDataValue(Store.Fragment<T> fragment, int rowIdx, int columnIdx);
	
	@Override
	public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
		throw new UnsupportedOperationException();
	}
	
	protected abstract Object getColumnName(Store.Fragment<T> fragment, int columnIdx);
	
	public IDataProvider getColumnDataProvider() {
		return fColumnDataProvider;
	}
	
	public IDataProvider getRowDataProvider() {
		return fRowDataProvider;
	}
	
	protected IDataProvider createColumnDataProvider() {
		return new ColumnDataProvider();
	}
	
	protected IDataProvider createRowDataProvider() {
		return new RowDataProvider();
	}
	
	protected ISortModel createSortModel() {
		return new SortModel();
	}
	
	private Object handleLoadDataException(final LoadDataException e) {
		if (e.isUnrecoverable()) {
			return ERROR;
		}
		reset();
		return LOADING;
	}
	
	
	public ISortModel getSortModel() {
		return fSortModel;
	}
	
	public SortColumn getSortColumn() {
		return fSortColumn;
	}
	
	private void setSortColumn(final SortColumn column) {
		synchronized (fFragmentsLock) {
			if ((fSortColumn != null) ? fSortColumn.equals(column) : null == column) {
				return;
			}
			fSortColumn = column;
			
			clear(-1, -1, false);
			
			synchronized (fFindLock) {
				fFindScheduledTask = null;
				fFindStore.internalClear(-1);
				fFindLastMatchIdx = -1;
				
				if (fFindLock.state < Lock.LOCAL_PAUSE_STATE) {
					fFindLock.state = Lock.LOCAL_PAUSE_STATE;
				}
			}
		}
	}
	
	
	public void addFindListener(final IFindListener listener) {
		fFindListeners.add(listener);
	}
	
	public void removeFindListener(final IFindListener listener) {
		fFindListeners.remove(listener);
	}
	
	public void find(final FindTask task) {
		synchronized (fFindLock) {
			fFindScheduledTask = task;
			if (!task.equals(fFindCurrentTask)) {
				clearFind(-1);
				fFindScheduledTask = task;
				if (fFindLock.state < Lock.LOCAL_PAUSE_STATE) {
					fFindLock.state = Lock.LOCAL_PAUSE_STATE;
				}
			}
			if (fFragmentsLock.state > Lock.LOCAL_PAUSE_STATE) {
				return;
			}
		}
		try {
			findMatch(null, new NullProgressMonitor());
		}
		catch (final CoreException e) {}
	}
	
	private void runFind(final RService r, final IProgressMonitor monitor) throws CoreException {
		try {
			final boolean updateFinding;
			synchronized (fFindLock) {
				fFindCurrentTask = fFindScheduledTask;
				fFindLock.scheduled = false;
				
				if (fFindCurrentTask == null) {
					return;
				}
				updateFinding = !fFindCurrentTask.expression.equals(fFindActiveExpression);
				if (fFindLock.state > Lock.LOCAL_PAUSE_STATE) {
					return;
				}
				if (fFindLock.state == Lock.LOCAL_PAUSE_STATE && !updateFinding) {
					fFindLock.state = 0;
				}
			}
			if (updateFinding) {
				try {
					updateFindingCache(r, monitor);
				}
				catch (final Exception e) {
					checkCancel(e);
					StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
							"An error occurred when evaluating find criteria for data viewer.", e));
					return;
				}
			}
			
			updateFindingFragments(r, monitor);
			
			findMatch(r, monitor);
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				notifyFindListeners(fFindCurrentTask, new StatusInfo(IStatus.CANCEL, ""), -1, -1, -1);
			}
			throw e;
		}
	}
	
	private void updateFindingCache(final RService r, final IProgressMonitor monitor) throws UnexpectedRDataException, CoreException {
		int mode = 0;
		int count = 0;
		try {
			if (fRCacheFind == null) {
				fRCacheFind = fRCacheId + ".find";
			}
			final boolean runWhich;
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("local({");
				cmd.append("x <- ").append(fInput.getFullName()).append("; ");
				cmd.append("assign(\"").append(fRCacheFind).append("\", envir = .rj.tmp, value = (")
						.append(fFindCurrentTask.expression).append(")); ");
				cmd.append("dimnames(").append(".rj.tmp$").append(fRCacheFind).append(") <- NULL; ");
				cmd.append(".rj.tmp$").append(fRCacheFind).append("; ");
				cmd.append("})");
				final RObject logi = r.evalData(cmd.toString(), null, RObjectFactory.F_ONLY_STRUCT, RService.DEPTH_ONE, monitor);
				if (logi.getRObjectType() == RObject.TYPE_ARRAY
						&& logi.getData().getStoreType() == RStore.LOGICAL
						&& logi.getLength() == getRowCount() * getColumnCount()) {
					mode = (getColumnCount() == 1) ? FIND_ROW : FIND_CELL;
					runWhich = true;
				}
				else if (logi.getRObjectType() == RObject.TYPE_VECTOR
						&& logi.getData().getStoreType() == RStore.LOGICAL
						&& logi.getLength() == getRowCount()) {
					mode = FIND_ROW;
					runWhich = true;
				}
				else if (logi.getRObjectType() == RObject.TYPE_VECTOR
						&& logi.getData().getStoreType() == RStore.INTEGER) {
					mode = FIND_ROW;
					runWhich = false;
				}
				else {
					throw new UnexpectedRDataException(logi.toString());
				}
			}
			if (runWhich) {
				final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append("assign(\"").append(fRCacheFind).append("\", envir = .rj.tmp, value = ")
						.append("which(").append(".rj.tmp$").append(fRCacheFind).append(", arr.ind = TRUE))");
				r.evalVoid(cmd.toString(), monitor);
			}
			
			{	final StringBuilder cmd = getRCmdStringBuilder();
				cmd.append((mode == FIND_CELL) ? "nrow(" : "length(")
						.append(".rj.tmp$").append(fRCacheFind).append(")");
				count = RDataUtil.checkSingleIntValue(r.evalData(cmd.toString(), monitor));
			}
		}
		catch (final CoreException e) {
			cleanFinding(r, monitor);
			checkCancel(e);
			mode = FIND_ERROR;
			throw e;
		}
		catch (final UnexpectedRDataException e) {
			cleanFinding(r, monitor);
			checkCancel(e);
			mode = FIND_ERROR;
			throw e;
		}
		finally {
			if (mode == FIND_ERROR) {
				notifyFindListeners(fFindCurrentTask, new StatusInfo(IStatus.ERROR, "Error"), -1, -1, -1);
			}
			synchronized (fFindLock) {
				fFindActiveMode = mode;
				fFindActiveExpression = fFindCurrentTask.expression;
				fFindCount = count;
				fFindStore.internalClear(count);
				fFindLastMatchIdx = -1;
				if (mode != FIND_ERROR && fFindLock.state < Lock.PAUSE_STATE) {
					fFindLock.state = 0;
				}
			}
		}
	}
	
	private void updateFindingFragments(final RService r, final IProgressMonitor monitor) throws CoreException {
		try {
			final Item<RObject>[] toUpdate;
			synchronized (fFindLock) {
				toUpdate = fFindStore.internalForUpdate();
			}
			for (int i = 0; i < toUpdate.length; i++) {
				if (monitor.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				
				final Item<RObject> item = toUpdate[i];
				if (item == null) {
					break;
				}
				synchronized (fFindLock) {
					if (!item.scheduled) {
						continue;
					}
				}
				final RObject fragment = loadFindFragment(item, r, monitor);
				synchronized (fFindLock) {
					if (!item.scheduled) {
						continue;
					}
					item.fragment = new Store.Fragment<RObject>(fragment,
							item.beginRowIdx, item.endRowIdx,
							item.beginColumnIdx, item.endColumnIdx );
					item.scheduled = false;
				}
			}
		}
		catch (final Exception e) {
			checkCancel(e);
			synchronized (fFindLock) {
				clearFind(-1);
				if (fFindLock.state < Lock.RELOAD_STATE) {
					fFindLock.state = Lock.RELOAD_STATE;
				}
			}
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when loading find matches for data viewer.", e));
			return;
		}
	}
	
	private RObject loadFindFragment(final Store.Fragment<RObject> f,
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		{	final StringBuilder cmd = getRCmdStringBuilder();
			cmd.append("local({");
			if (fRCacheSort != null && fRCacheSortR == null) {
				fRCacheSortR = fRCacheSort + ".r";
				cmd.append(".rj.tmp$").append(fRCacheSortR).append(" <- ")
						.append("integer(length(").append(".rj.tmp$").append(fRCacheSort).append(")); ");
				cmd.append(".rj.tmp$").append(fRCacheSortR)
						.append("[").append(".rj.tmp$").append(fRCacheSort).append("] <- ")
						.append("1L:length(").append(".rj.tmp$").append(fRCacheSortR).append("); ");
			}
			if (fRCacheSortR != null) {
				if (fFindActiveMode == FIND_CELL) {
					cmd.append("x <- ").append(".rj.tmp$").append(fRCacheSortR)
							.append("[").append(".rj.tmp$").append(fRCacheFind).append("[,1]").append("]; ");
					cmd.append("x <- cbind(x, ").append(".rj.tmp$").append(fRCacheFind).append("[,2]").append("); ");
				}
				else {
					cmd.append("x <- ").append(".rj.tmp$").append(fRCacheSortR)
							.append("[").append(".rj.tmp$").append(fRCacheFind).append("]; ");
				}
			}
			else {
				cmd.append("x <- ").append(".rj.tmp$").append(fRCacheFind).append("; ");
			}
			cmd.append("x <- x[order(");
			if (fFindActiveMode == FIND_CELL) {
				cmd.append((fFindCurrentTask.firstInRow) ? "x[,1], x[,2]" : "x[,2], x[,1]");
			}
			else {
				cmd.append("x");
			}
			cmd.append(")").append("[").append(f.beginRowIdx + 1).append("L:").append(f.endRowIdx).append("L]");
			if (fFindActiveMode == FIND_CELL) {
				cmd.append(",");
			}
			cmd.append("]; ");
			cmd.append("x; ");
			cmd.append("})");
			return r.evalData(cmd.toString(), monitor);
		}
	}
	
	private void findMatch(final RService r, final IProgressMonitor monitor) throws CoreException {
		final FindTask task;
		final int mode;
		final int count;
		int globalMatchIdx;
		synchronized (fFindLock) {
			task = fFindScheduledTask;
			mode = fFindActiveMode;
			count = fFindCount;
			globalMatchIdx = fFindLastMatchIdx;
			
			if (task == null || !task.equals(fFindCurrentTask)
					|| fFindLock.state == Lock.LOCAL_PAUSE_STATE) {
				notifyFindListeners(task, new StatusInfo(IStatus.INFO, "Finding..."), -1, -1, -1);
				fFindLock.schedule(null);
				return;
			}
			if (mode == FIND_ERROR) {
				notifyFindListeners(task, new StatusInfo(IStatus.ERROR, "Error"), -1, -1, -1);
				return;
			}
		}
		
		if (count <= 0) {
			notifyFindListeners(task, new StatusInfo(IStatus.INFO, "Not found."), count, -1, -1);
			return;
		}
		else {
			notifyFindListeners(task, new StatusInfo(IStatus.INFO,
					"Finding " + (task.forward ? "next" : "previous") + "... (total " + count + ")"), count, -1, -1);
		}
		if (globalMatchIdx >= count) {
			globalMatchIdx = count - 1;
		}
		else if (globalMatchIdx < 0) {
			globalMatchIdx = count / 2;
		}
		try {
			final int[] rPos;
			final int[] low;
			final int[] high;
			final int rowIdx;
			final int colIdx;
			if (mode == FIND_CELL) {
				rowIdx = task.firstInRow ? 0 : 1;
				colIdx = task.firstInRow ? 1 : 0;
				
				rPos = new int[2];
				rPos[rowIdx] = task.rowIdx + 1;
				rPos[colIdx] = task.columnIdx + 1;
				low = new int[2];
				high = new int[2];
			}
			else {
				rowIdx = 0;
				colIdx = -1;
				
				rPos = new int[] { task.rowIdx + 1 };
				low = new int[1];
				high = new int[1];
			}
			
			while (true) {
				int last = 0;
				Store.Fragment<RObject> fragment;
				while (true) {
					if (monitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					
					fragment = fFindStore.getFor(globalMatchIdx, 0);
					if (fragment != null) {
						final RStore data = fragment.rObject.getData();
						final int length = fragment.endRowIdx - fragment.beginRowIdx;
						low[rowIdx] = data.getInt(0);
						high[rowIdx] = data.getInt(length-1);
						if (mode == FIND_CELL) {
							low[colIdx] = data.getInt(length);
							high[colIdx] = data.getInt(length+length-1);
						}
						if (RDataUtil.compare(rPos, low) < 0) {
							globalMatchIdx = fragment.beginRowIdx - 1;
							if (globalMatchIdx < 0
									|| (task.forward && last == +1)) {
								break;
							}
							last = -1;
						}
						if (RDataUtil.compare(rPos, high) > 0) {
							globalMatchIdx = fragment.endRowIdx;
							if (globalMatchIdx > count
									|| (!task.forward && last == -1)) {
								break;
							}
							last = +1;
						}
						break;
					}
					else if (r != null) {
						updateFindingFragments(r, monitor);
						synchronized (fFindLock) {
							if (task != fFindScheduledTask
									|| fFindLock.state > 0) {
								return;
							}
						}
					}
					else {
						synchronized (fFindLock) {
							if (task != fFindScheduledTask) {
								return;
							}
							fFindLock.schedule(null);
						}
						return;
					}
				}
				
				final RStore data = fragment.rObject.getData();
				final int length = fragment.endRowIdx - fragment.beginRowIdx;
				int localMatchIdx;
				if (mode == FIND_CELL) {
					low[rowIdx] = 0;
					low[colIdx] = length;
					localMatchIdx = RDataUtil.binarySearch(data, low, length, rPos);
				}
				else {
					localMatchIdx = RDataUtil.binarySearch(data, rPos[rowIdx]);
				}
				if (localMatchIdx >= 0) {
					localMatchIdx += (task.forward) ? +1 : -1;
				}
				else {
					localMatchIdx = -(localMatchIdx + 1);
					localMatchIdx += (task.forward) ? 0 : -1;
				}
				if (localMatchIdx < 0 || localMatchIdx >= length) {
					notifyFindListeners(task, new StatusInfo(IStatus.INFO, "No further match (total " + count + ")"), count, -1, -1);
					return;
				}
				synchronized (fFindLock) {
					if (task != fFindScheduledTask) {
						return;
					}
					fFindLastMatchIdx = globalMatchIdx = fragment.beginRowIdx + localMatchIdx;
				}
				{	
					final int posCol;
					final int posRow;
					rPos[rowIdx] = data.getInt(localMatchIdx);
					posRow = rPos[rowIdx] - 1;
					if (mode == FIND_CELL) {
						rPos[colIdx] = data.getInt(length + localMatchIdx);
						posCol = rPos[colIdx] - 1;
					}
					else {
						posCol = -1;
					}
					if (task.filter == null || task.filter.match(posRow, posCol)) {
						notifyFindListeners(task, new StatusInfo(IStatus.INFO, "Match " + (globalMatchIdx + 1) + " (total " + count + ")"), count, posRow, posCol);
						return;
					}
				}
			}
		}
		catch (final LoadDataException e) {
		}
	}
	
	private void notifyFindListeners(final FindTask task, final IStatus status, final int total,
			final int rowIdx, final int colIdx) {
		UIAccess.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (fFindLock) {
					if (task != null && task != fFindScheduledTask) {
						return;
					}
				}
				final FindEvent event = new FindEvent(status, total, rowIdx, colIdx);
				for (final IFindListener listener : fFindListeners.toArray()) {
					listener.handleFindEvent(event);
				}
			}
		});
	}
	
	
	public void addDataChangedListener(final IDataProviderListener listener) {
		fDataListeners.add(listener);
	}
	
	public void removeDataChangedListener(final IDataProviderListener listener) {
		fDataListeners.remove(listener);
	}
	
	protected void notifyListener(final Store.Fragment<?> item) {
		try {
			for (final IDataProviderListener listener : fDataListeners.toArray()) {
				listener.onRowsChanged(item.beginRowIdx, item.endRowIdx);
			}
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when notifying about row updates.", e));
		}
	}
	
	
	public void reset() {
		clear(Lock.PAUSE_STATE, -1, true);
		synchronized (fInitRunnable) {
			if (fInitScheduled) {
				return;
			}
			fInitScheduled = true;
		}
		try {
			((RProcessDataTableInput) fInput).run(fInitRunnable);
		}
		catch (final CoreException e) {
		}
	}
	
	private void clear(final int newState, final int rowCount, final boolean clearFind) {
		synchronized (fFragmentsLock) {
			fDataStore.internalClear(rowCount);
			fRowNamesStore.internalClear(rowCount);
			fUpdateSorting = true;
			
			if (newState >= 0 && fFragmentsLock.state < Lock.ERROR_STATE) {
				fFragmentsLock.state = newState;
			}
			if (rowCount >= 0) {
				fRowCount = rowCount;
			}
			
			if (clearFind) {
				clearFind(newState);
			}
		}
	}
	
	private void clearFind(final int newState) {
		synchronized (fFindLock) {
			fFindScheduledTask = null;
			fFindStore.internalClear(0);
			fFindActiveExpression = null;
			fFindLastMatchIdx = -1;
			
			if (newState >= 0 && fFindLock.state < Lock.ERROR_STATE) {
				fFindLock.state = newState;
			}
		}
		notifyFindListeners(null, new StatusInfo(IStatus.CANCEL, ""), -1, -1, -1);
	}
	
	public void dispose() {
		schedule(fCleanRunnable);
	}
	
}
