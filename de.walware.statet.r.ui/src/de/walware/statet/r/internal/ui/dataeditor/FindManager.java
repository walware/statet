/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.LazyRStore.Fragment;

import de.walware.statet.r.internal.ui.dataeditor.IFindListener.FindEvent;
import de.walware.statet.r.ui.RUI;


class FindManager {
	
	private static final int FIND_CELL = 1;
	private static final int FIND_ROW = 2;
	private static final int FIND_ERROR = -1;
	
	
	private class FindLock extends Lock implements LazyRStore.Updater<RObject> {
		
		boolean scheduled;
		
		@Override
		public void scheduleUpdate(final LazyRStore<RObject> store, final LazyRStore.Fragment<RObject> fragment) {
			if (fragment != null && state > 0) {
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
	private long fFindTotalCount;
	private long fFindFilteredCount;
	private long fFindLastMatchIdx;
	
	private final FindLock fLock = new FindLock();
	private final LazyRStore<RObject> fFindStore;
	
	private final FastList<IFindListener> fListeners = new FastList<IFindListener>(IFindListener.class);
	
	private final AbstractRDataProvider<?> fDataProvider;
	
	
	public FindManager(final AbstractRDataProvider<?> dataProvider) {
		fDataProvider = dataProvider;
		
		fFindStore = new LazyRStore<RObject>(0, 1, 5, fLock);
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
			fFindStore.clear(0);
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
			fFindStore.clear(-1);
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
		long count = 0;
		long filteredCount = 0;
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
				fFindStore.clear(filteredCount);
				fFindLastMatchIdx = -1;
				if (mode != FIND_ERROR && fLock.state < Lock.PAUSE_STATE) {
					fLock.state = 0;
				}
			}
		}
	}
	
	private long getFilteredCount(final long count, final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
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
			while (true) {
				final Fragment<RObject> fragment;
				synchronized (fLock) {
					fragment = fFindStore.getNextScheduledFragment();
				}
				if (fragment == null) {
					break;
				}
				if (monitor.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				
				final RObject fragmentObject = loadFindFragment(fragment, r, monitor);
				synchronized (fLock) {
					fFindStore.updateFragment(fragment, fragmentObject);
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
	
	private RObject loadFindFragment(final LazyRStore.Fragment<RObject> fragment,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String revIndexName = fDataProvider.checkRevIndex(r, monitor);
		{	final StringBuilder cmd = fDataProvider.getRCmdStringBuilder();
			cmd.append("local({");
			if (revIndexName != null) {
				if (fActiveMode == FIND_CELL) {
					cmd.append("x <- ").append(RJTmp.ENV+'$').append(revIndexName)
							.append("[").append(RJTmp.ENV+'$').append(fRCacheFind).append("[,1L]").append("]\n");
					cmd.append("x <- cbind(x, ").append(RJTmp.ENV+'$').append(fRCacheFind).append("[,2L]").append(")\n");
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
				cmd.append((fCurrentTask.firstInRow) ? "x[,1L], x[,2L]" : "x[,2L], x[,1L]");
			}
			else {
				cmd.append("x");
			}
			cmd.append(")").append("[").append(fragment.getRowBeginIdx() + 1).append(":").append(fragment.getRowEndIdx()).append("]");
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
		long filteredCount;
		long totalCount;
		long globalMatchIdx;
		synchronized (fLock) {
			task = fScheduledTask;
			mode = fActiveMode;
			totalCount = fFindTotalCount;
			filteredCount = fFindFilteredCount;
			globalMatchIdx = fFindLastMatchIdx;
			
			if (task == null || !task.equals(fCurrentTask)
					|| fLock.state == Lock.LOCAL_PAUSE_STATE) {
				notifyListeners(task, new StatusInfo(IStatus.INFO, "Finding..."), -1, -1, -1);
				fLock.scheduleUpdate(null, null);
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
					fFindStore.clear(filteredCount);
				}
			}
			else {
				synchronized (fLock) {
					if (task != fScheduledTask) {
						return;
					}
					fLock.scheduleUpdate(null, null);
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
			final long[] rPos;
			final long[] low;
			final long[] high;
			final int rowIdx;
			final int colIdx;
			if (mode == FIND_CELL) {
				rowIdx = task.firstInRow ? 0 : 1;
				colIdx = task.firstInRow ? 1 : 0;
				
				rPos = new long[2];
				rPos[rowIdx] = task.rowIdx + 1;
				rPos[colIdx] = task.columnIdx + 1;
				low = new long[2];
				high = new long[2];
			}
			else {
				rowIdx = 0;
				colIdx = -1;
				
				rPos = new long[] { task.rowIdx + 1 };
				low = new long[1];
				high = new long[1];
			}
			
			while (true) {
				int last = 0;
				LazyRStore.Fragment<RObject> fragment;
				while (true) {
					if (monitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					
					fragment = fLock.getFragment(fFindStore, globalMatchIdx, 0);
					if (fragment != null) {
						final RStore data = fragment.getRObject().getData();
						final int length = fragment.getRowCount();
						low[rowIdx] = data.getInt(0);
						high[rowIdx] = data.getInt(length-1);
						if (mode == FIND_CELL) {
							low[colIdx] = data.getInt(length);
							high[colIdx] = data.getInt(length+length-1);
						}
						if (RDataUtil.compare(rPos, low) < 0) {
							globalMatchIdx = fragment.getRowBeginIdx() - 1;
							if (globalMatchIdx < 0
									|| (task.forward && last == +1)) {
								break;
							}
							last = -1;
						}
						if (RDataUtil.compare(rPos, high) > 0) {
							globalMatchIdx = fragment.getRowEndIdx();
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
							fLock.scheduleUpdate(null, null);
						}
						return;
					}
				}
				
				final RStore data = fragment.getRObject().getData();
				final int length = fragment.getRowCount();
				long localMatchIdx;
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
					fFindLastMatchIdx = globalMatchIdx = fragment.getRowBeginIdx() + localMatchIdx;
				}
				{	final long posCol;
					final long posRow;
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
	
	private void notifyListeners(final FindTask task, final IStatus status, final long total,
			final long rowIdx, final long colIdx) {
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
