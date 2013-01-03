/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RService;

import de.walware.statet.r.internal.ui.dataeditor.IFindListener.FindEvent;
import de.walware.statet.r.internal.ui.dataeditor.Store.Item;
import de.walware.statet.r.internal.ui.dataeditor.Store.LoadDataException;
import de.walware.statet.r.internal.ui.dataeditor.Store.Lock;
import de.walware.statet.r.ui.RUI;


class FindManager {
	
	private static final int FIND_CELL = 1;
	private static final int FIND_ROW = 2;
	private static final int FIND_ERROR = -1;
	
	
	private class FindLock extends Lock {
		
		boolean scheduled;
		
		@Override
		void schedule(final Object obj) {
			if (obj != null && state > 0) {
				return;
			}
			if (!scheduled) {
				scheduled = true;
				fDataProvider.schedule(fFindRunnable);
			}
		}
		
	}
	
	private final IToolRunnable fFindRunnable = new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/find"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Find Data (" + fDataProvider.fInput.getLastName() + ")";
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
				synchronized (fLock) {
					fLock.scheduled = false;
					fLock.notifyAll();
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
			runFind((IRToolService) service, monitor);
		}
		
	};
	
	
	private FindTask fScheduledTask;
	
	private String fRCacheFind; // only in R jobs
	private FindTask fCurrentTask;
	private int fActiveMode;
	private String fActiveExpression;
	private int fFindTotalCount;
	private int fFindFilteredCount;
	private int fFindLastMatchIdx;
	
	private final FindLock fLock = new FindLock();
	private final Store<RObject> fFindStore;
	
	private final FastList<IFindListener> fListeners = new FastList<IFindListener>(IFindListener.class);
	
	private final AbstractRDataProvider<?> fDataProvider;
	
	
	public FindManager(final AbstractRDataProvider<?> dataProvider) {
		fDataProvider = dataProvider;
		
		fFindStore = new Store<RObject>(fLock, 1, 0, 5);
	}
	
	
	public void addFindListener(final IFindListener listener) {
		fListeners.add(listener);
	}
	
	public void removeFindListener(final IFindListener listener) {
		fListeners.remove(listener);
	}
	
	void clean(final IRToolService r, final IProgressMonitor monitor) throws CoreException {
		if (fRCacheFind != null) {
			AbstractRDataProvider.cleanTmp(fRCacheFind, r, monitor);
			fRCacheFind = null;
		}
	}
	
	void clear(final int newState) {
		synchronized (fLock) {
			fScheduledTask = null;
			fFindStore.internalClear(0);
			fActiveExpression = null;
			fFindLastMatchIdx = -1;
			
			if (newState >= 0 && fLock.state < Lock.ERROR_STATE) {
				fLock.state = newState;
			}
		}
		notifyListeners(null, new StatusInfo(IStatus.CANCEL, ""), -1, -1, -1);
	}
	
	void reset(final boolean filter) {
		synchronized (fLock) {
			fScheduledTask = null;
			fFindStore.internalClear(-1);
			if (filter) {
				fFindFilteredCount = -1;
			}
			fFindLastMatchIdx = -1;
			
			if (fLock.state < Lock.LOCAL_PAUSE_STATE) {
				fLock.state = Lock.LOCAL_PAUSE_STATE;
			}
		}
	}
	
	
	public void find(final FindTask task) {
		synchronized (fLock) {
			fScheduledTask = task;
			if (!task.equals(fCurrentTask)) {
				clear(-1);
				fScheduledTask = task;
				if (fLock.state < Lock.LOCAL_PAUSE_STATE) {
					fLock.state = Lock.LOCAL_PAUSE_STATE;
				}
			}
			if (fDataProvider.getLockState() > Lock.LOCAL_PAUSE_STATE) {
				return;
			}
		}
		try {
			findMatch(null, new NullProgressMonitor());
		}
		catch (final CoreException e) {}
		catch (final UnexpectedRDataException e) {}
	}
	
	private void runFind(final IRToolService r, final IProgressMonitor monitor) throws CoreException {
		try {
			final boolean updateFinding;
			synchronized (fLock) {
				fCurrentTask = fScheduledTask;
				fLock.scheduled = false;
				
				if (fCurrentTask == null) {
					return;
				}
				updateFinding = !fCurrentTask.expression.equals(fActiveExpression);
				if (fLock.state > Lock.LOCAL_PAUSE_STATE) {
					return;
				}
				if (fLock.state == Lock.LOCAL_PAUSE_STATE && !updateFinding) {
					fLock.state = 0;
				}
			}
			if (updateFinding) {
				try {
					updateFindingCache(r, monitor);
				}
				catch (final Exception e) {
					AbstractRDataProvider.checkCancel(e);
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
				notifyListeners(fCurrentTask, new StatusInfo(IStatus.CANCEL, ""), //$NON-NLS-1$
						-1, -1, -1 );
			}
			throw e;
		}
		catch (final UnexpectedRDataException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					"An error occurred when evaluating find result for data viewer.", e));
		}
	}
	
	private void updateFindingCache(final IRToolService r, final IProgressMonitor monitor) throws UnexpectedRDataException, CoreException {
		int mode = 0;
		int count = 0;
		int filteredCount = 0;
		try {
			if (fRCacheFind == null) {
				fRCacheFind = fDataProvider.createTmp(".find");
			}
			final boolean runWhich;
			{	final StringBuilder cmd = fDataProvider.getRCmdStringBuilder();
				cmd.append("local({");
				cmd.append("x <- ").append(fDataProvider.fInput.getFullName()).append("; ");
				cmd.append("x.find <- (").append(fCurrentTask.expression).append("); ");
				cmd.append("dimnames(").append("x.find").append(") <- NULL; ");
				cmd.append("assign('").append(fRCacheFind).append("', envir= ").append(RJTmp.ENV).append(", value= x.find); ");
				cmd.append("})");
				final RObject logi = r.evalData(cmd.toString(), null, RObjectFactory.F_ONLY_STRUCT, RService.DEPTH_ONE, monitor);
				if (logi.getRObjectType() == RObject.TYPE_ARRAY
						&& logi.getData().getStoreType() == RStore.LOGICAL
						&& logi.getLength() == fDataProvider.getFullRowCount() * fDataProvider.getColumnCount()) {
					mode = (fDataProvider.getColumnCount() == 1) ? FIND_ROW : FIND_CELL;
					runWhich = true;
				}
				else if (logi.getRObjectType() == RObject.TYPE_VECTOR
						&& logi.getData().getStoreType() == RStore.LOGICAL
						&& logi.getLength() == fDataProvider.getFullRowCount()) {
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
				final FunctionCall call = r.createFunctionCall(RJTmp.SET);
				call.addChar(RJTmp.NAME_PAR, fRCacheFind);
				final StringBuilder cmd = fDataProvider.getRCmdStringBuilder();
				cmd.append("which(").append(RJTmp.ENV+'$').append(fRCacheFind).append(", arr.ind= TRUE)");
				call.add(RJTmp.VALUE_PAR, cmd.toString());
				call.evalVoid(monitor);
			}
			
			{	final FunctionCall call = r.createFunctionCall("NROW");
				call.add(RJTmp.ENV+'$'+fRCacheFind);
				count = RDataUtil.checkSingleIntValue(call.evalData(monitor));
			}
			filteredCount = getFilteredCount(count, r, monitor);
		}
		catch (final CoreException e) {
			clean(r, monitor);
			AbstractRDataProvider.checkCancel(e);
			mode = FIND_ERROR;
			throw e;
		}
		catch (final UnexpectedRDataException e) {
			clean(r, monitor);
			AbstractRDataProvider.checkCancel(e);
			mode = FIND_ERROR;
			throw e;
		}
		finally {
			if (mode == FIND_ERROR) {
				notifyListeners(fCurrentTask, new StatusInfo(IStatus.ERROR, "Error"), -1, -1, -1);
			}
			synchronized (fLock) {
				fActiveMode = mode;
				fActiveExpression = fCurrentTask.expression;
				fFindTotalCount = count;
				fFindFilteredCount = filteredCount;
				fFindStore.internalClear(filteredCount);
				fFindLastMatchIdx = -1;
				if (mode != FIND_ERROR && fLock.state < Lock.PAUSE_STATE) {
					fLock.state = 0;
				}
			}
		}
	}
	
	private int getFilteredCount(final int count, final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String filterVar = fDataProvider.checkFilter();
		if (filterVar == null || count == 0) {
			return count;
		}
		final FunctionCall call = r.createFunctionCall(RJTmp.GET_FILTERED_COUNT);
		call.addChar(RJTmp.FILTER_PAR, filterVar);
		call.addChar(RJTmp.INDEX_PAR, fRCacheFind);
		return RDataUtil.checkSingleIntValue(call.evalData(monitor));
	}
	
	private void updateFindingFragments(final IRToolService r, final IProgressMonitor monitor) throws CoreException {
		try {
			final Item<RObject>[] toUpdate;
			synchronized (fLock) {
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
				synchronized (fLock) {
					if (!item.scheduled) {
						continue;
					}
				}
				final RObject fragment = loadFindFragment(item, r, monitor);
				synchronized (fLock) {
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
			AbstractRDataProvider.checkCancel(e);
			synchronized (fLock) {
				clear(-1);
				if (fLock.state < Lock.RELOAD_STATE) {
					fLock.state = Lock.RELOAD_STATE;
				}
			}
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when loading find matches for data viewer.", e));
			return;
		}
	}
	
	private RObject loadFindFragment(final Store.Fragment<RObject> f,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String revIndexName = fDataProvider.checkRevIndex(r, monitor);
		{	final StringBuilder cmd = fDataProvider.getRCmdStringBuilder();
			cmd.append("local({");
			if (revIndexName != null) {
				if (fActiveMode == FIND_CELL) {
					cmd.append("x <- ").append(RJTmp.ENV+'$').append(revIndexName)
							.append("[").append(RJTmp.ENV+'$').append(fRCacheFind).append("[,1]").append("]\n");
					cmd.append("x <- cbind(x, ").append(RJTmp.ENV+'$').append(fRCacheFind).append("[,2]").append(")\n");
				}
				else {
					cmd.append("x <- ").append(RJTmp.ENV+'$').append(revIndexName)
							.append("[").append(RJTmp.ENV+'$').append(fRCacheFind).append("]\n");
				}
				cmd.append("x <- na.omit(x)\n");
			}
			else {
				cmd.append("x <- ").append(RJTmp.ENV+'$').append(fRCacheFind).append("\n");
			}
			cmd.append("x <- x[order(");
			if (fActiveMode == FIND_CELL) {
				cmd.append((fCurrentTask.firstInRow) ? "x[,1], x[,2]" : "x[,2], x[,1]");
			}
			else {
				cmd.append("x");
			}
			cmd.append(")").append("[").append(f.beginRowIdx + 1).append("L:").append(f.endRowIdx).append("L]");
			if (fActiveMode == FIND_CELL) {
				cmd.append(",");
			}
			cmd.append("]; ");
			cmd.append("x; ");
			cmd.append("})");
			return r.evalData(cmd.toString(), monitor);
		}
	}
	
	private void findMatch(final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final FindTask task;
		final int mode;
		int filteredCount;
		int totalCount;
		int globalMatchIdx;
		synchronized (fLock) {
			task = fScheduledTask;
			mode = fActiveMode;
			totalCount = fFindTotalCount;
			filteredCount = fFindFilteredCount;
			globalMatchIdx = fFindLastMatchIdx;
			
			if (task == null || !task.equals(fCurrentTask)
					|| fLock.state == Lock.LOCAL_PAUSE_STATE) {
				notifyListeners(task, new StatusInfo(IStatus.INFO, "Finding..."), -1, -1, -1);
				fLock.schedule(null);
				return;
			}
			if (mode == FIND_ERROR) {
				notifyListeners(task, new StatusInfo(IStatus.ERROR, "Error."), -1, -1, -1);
				return;
			}
		}
		
		if (filteredCount < 0) {
			if (r != null) {
				filteredCount = getFilteredCount(totalCount, r, monitor);
				synchronized (fLock) {
					fFindFilteredCount = filteredCount;
					fFindStore.internalClear(filteredCount);
				}
			}
			else {
				synchronized (fLock) {
					if (task != fScheduledTask) {
						return;
					}
					fLock.schedule(null);
				}
				return;
			}
		}
		if (filteredCount <= 0) {
			notifyListeners(task, new StatusInfo(IStatus.INFO,
					NLS.bind("Not found (total {0}/{1}).", filteredCount, totalCount)),
					filteredCount, -1, -1 );
			return;
		}
		else {
			notifyListeners(task, new StatusInfo(IStatus.INFO,
					NLS.bind("Finding {0} (total {1}/{2})...", new Object[] {
							(task.forward ? "next" : "previous"), filteredCount, totalCount })),
					filteredCount, -1, -1 );
		}
		if (globalMatchIdx >= filteredCount) {
			globalMatchIdx = filteredCount - 1;
		}
		else if (globalMatchIdx < 0) {
			globalMatchIdx = filteredCount / 2;
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
							if (globalMatchIdx > filteredCount
									|| (!task.forward && last == -1)) {
								break;
							}
							last = +1;
						}
						break;
					}
					else if (r != null) {
						updateFindingFragments(r, monitor);
						synchronized (fLock) {
							if (task != fScheduledTask
									|| fLock.state > 0) {
								return;
							}
						}
					}
					else {
						synchronized (fLock) {
							if (task != fScheduledTask) {
								return;
							}
							fLock.schedule(null);
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
					notifyListeners(task, new StatusInfo(IStatus.INFO,
							NLS.bind("No further match (total {0}/{1}).", filteredCount, totalCount)),
							filteredCount, -1, -1 );
					return;
				}
				synchronized (fLock) {
					if (task != fScheduledTask) {
						return;
					}
					fFindLastMatchIdx = globalMatchIdx = fragment.beginRowIdx + localMatchIdx;
				}
				{	final int posCol;
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
						notifyListeners(task, new StatusInfo(IStatus.INFO,
								NLS.bind("Match {0} (total {1}/{2}).", new Object[] {
										(globalMatchIdx + 1), filteredCount, totalCount })),
								filteredCount, posRow, posCol );
						return;
					}
				}
			}
		}
		catch (final LoadDataException e) {
		}
	}
	
	private void notifyListeners(final FindTask task, final IStatus status, final int total,
			final int rowIdx, final int colIdx) {
		UIAccess.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (fLock) {
					if (task != null && task != fScheduledTask) {
						return;
					}
				}
				final FindEvent event = new FindEvent(status, total, rowIdx, colIdx);
				for (final IFindListener listener : fListeners.toArray()) {
					listener.handleFindEvent(event);
				}
			}
		});
	}
	
}
