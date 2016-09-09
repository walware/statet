/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
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
import de.walware.rj.services.utils.dataaccess.RDataAssignment;

import de.walware.statet.r.internal.ui.dataeditor.IFindListener.FindEvent;
import de.walware.statet.r.ui.RUI;


class FindManager {
	
	private static final int FIND_CELL= 1;
	private static final int FIND_ROW= 2;
	private static final int FIND_ERROR= -1;
	
	
	private class FindLock extends Lock implements LazyRStore.Updater<RObject> {
		
		
		@Override
		public void scheduleUpdate(final LazyRStore<RObject> store,
				final RDataAssignment assignment, final LazyRStore.Fragment<RObject> fragment,
				final int flags, final IProgressMonitor monitor) {
			if (fragment != null && this.state > 0) {
				return;
			}
			if (!this.scheduled) {
				this.scheduled= true;
				FindManager.this.dataProvider.schedule(FindManager.this.findRunnable);
			}
		}
		
	}
	
	private final IToolRunnable findRunnable= new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/find"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Find Data (" + FindManager.this.dataProvider.getInput().getName() + ")";
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
				FindManager.this.lock.lock();
				try {
					FindManager.this.lock.scheduled= false;
					FindManager.this.lock.clear();
				}
				finally {
					FindManager.this.lock.unlock();
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
	
	
	private FindTask scheduledTask;
	
	private String rCacheFind; // only in R jobs
	private FindTask currentTask;
	private int activeMode;
	private String activeExpression;
	private long findTotalCount;
	private long findFilteredCount;
	private long findLastMatchIdx;
	
	private final FindLock lock= new FindLock();
	private final LazyRStore<RObject> findStore;
	
	private final FastList<IFindListener> listeners= new FastList<>(IFindListener.class);
	
	private final AbstractRDataProvider<?> dataProvider;
	
	
	public FindManager(final AbstractRDataProvider<?> dataProvider) {
		this.dataProvider= dataProvider;
		
		this.findStore= new LazyRStore<>(0, 1, 5, this.lock);
	}
	
	
	public void addFindListener(final IFindListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeFindListener(final IFindListener listener) {
		this.listeners.remove(listener);
	}
	
	void clean(final IRToolService r, final IProgressMonitor monitor) throws CoreException {
		if (this.rCacheFind != null) {
			AbstractRDataProvider.cleanTmp(this.rCacheFind, r, monitor);
			this.rCacheFind= null;
		}
	}
	
	void clear(final int newState) {
		clear(newState, true);
	}
	void clear(final int newState, final boolean forceUpdate) {
		this.lock.lock();
		try {
			this.scheduledTask= null;
			if (!forceUpdate && this.findFilteredCount >= 0) {
				this.findStore.clear(this.findFilteredCount);
			}
			else {
				this.findStore.clear(0);
				this.activeExpression= null;
			}
			this.findLastMatchIdx= -1;
			
			if (newState >= 0 && this.lock.state < Lock.ERROR_STATE) {
				this.lock.state= newState;
			}
		}
		finally {
			this.lock.unlock();
		}
		notifyListeners(null, new StatusInfo(IStatus.CANCEL, ""), -1, -1, -1);
	}
	
	void reset(final boolean filter) {
		this.lock.lock();
		try {
			this.scheduledTask= null;
			this.findStore.clear(-1);
			if (filter) {
				this.findFilteredCount= -1;
			}
			this.findLastMatchIdx= -1;
			
			if (this.lock.state < Lock.LOCAL_PAUSE_STATE) {
				this.lock.state= Lock.LOCAL_PAUSE_STATE;
			}
		}
		finally {
			this.lock.unlock();
		}
	}
	
	
	public void find(final FindTask task) {
		this.lock.lock();
		try {
			this.scheduledTask= task;
			if (!task.equals(this.currentTask)) {
				clear(-1, !task.expression.equals(this.activeExpression));
				this.scheduledTask= task;
				if (this.lock.state < Lock.LOCAL_PAUSE_STATE) {
					this.lock.state= Lock.LOCAL_PAUSE_STATE;
				}
			}
			if (this.dataProvider.getLockState() > Lock.LOCAL_PAUSE_STATE) {
				return;
			}
		}
		finally {
			this.lock.unlock();
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
			this.lock.lock();
			try {
				this.currentTask= this.scheduledTask;
				this.lock.scheduled= false;
				
				if (this.currentTask == null) {
					return;
				}
				updateFinding= !this.currentTask.expression.equals(this.activeExpression);
				if (this.lock.state > Lock.LOCAL_PAUSE_STATE) {
					return;
				}
				if (this.lock.state == Lock.LOCAL_PAUSE_STATE && !updateFinding) {
					this.lock.state= 0;
				}
			}
			finally {
				this.lock.unlock();
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
				notifyListeners(this.currentTask, new StatusInfo(IStatus.CANCEL, ""), //$NON-NLS-1$
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
		int mode= 0;
		long count= 0;
		long filteredCount= 0;
		try {
			if (this.rCacheFind == null) {
				this.rCacheFind= this.dataProvider.createTmp(".find");
			}
			final boolean runWhich;
			{	final StringBuilder cmd= this.dataProvider.getRCmdStringBuilder();
				cmd.append("local({");
				cmd.append("x <- ").append(this.dataProvider.getInput().getFullName()).append("; ");
				cmd.append("x.find <- (").append(this.currentTask.expression).append("); ");
				cmd.append("dimnames(").append("x.find").append(") <- NULL; ");
				cmd.append("assign('").append(this.rCacheFind).append("', envir= ").append(RJTmp.ENV).append(", value= x.find); ");
				cmd.append("})");
				final RObject logi= r.evalData(cmd.toString(), null, RObjectFactory.F_ONLY_STRUCT, RService.DEPTH_ONE, monitor);
				if (logi.getRObjectType() == RObject.TYPE_ARRAY
						&& logi.getData().getStoreType() == RStore.LOGICAL
						&& logi.getLength() == this.dataProvider.getFullRowCount() * this.dataProvider.getColumnCount()) {
					mode= (this.dataProvider.getColumnCount() == 1) ? FIND_ROW : FIND_CELL;
					runWhich= true;
				}
				else if (logi.getRObjectType() == RObject.TYPE_VECTOR
						&& logi.getData().getStoreType() == RStore.LOGICAL
						&& logi.getLength() == this.dataProvider.getFullRowCount()) {
					mode= FIND_ROW;
					runWhich= true;
				}
				else if (logi.getRObjectType() == RObject.TYPE_VECTOR
						&& logi.getData().getStoreType() == RStore.INTEGER) {
					mode= FIND_ROW;
					runWhich= false;
				}
				else {
					throw new UnexpectedRDataException(logi.toString());
				}
			}
			if (runWhich) {
				final FunctionCall call= r.createFunctionCall(RJTmp.SET);
				call.addChar(RJTmp.NAME_PAR, this.rCacheFind);
				final StringBuilder cmd= this.dataProvider.getRCmdStringBuilder();
				cmd.append("which(").append(RJTmp.ENV+'$').append(this.rCacheFind).append(", arr.ind= TRUE)");
				call.add(RJTmp.VALUE_PAR, cmd.toString());
				call.evalVoid(monitor);
			}
			
			{	final FunctionCall call= r.createFunctionCall("NROW");
				call.add(RJTmp.ENV+'$'+this.rCacheFind);
				count= RDataUtil.checkSingleIntValue(call.evalData(monitor));
			}
			filteredCount= getFilteredCount(count, r, monitor);
		}
		catch (final CoreException e) {
			clean(r, monitor);
			AbstractRDataProvider.checkCancel(e);
			mode= FIND_ERROR;
			throw e;
		}
		catch (final UnexpectedRDataException e) {
			clean(r, monitor);
			AbstractRDataProvider.checkCancel(e);
			mode= FIND_ERROR;
			throw e;
		}
		finally {
			if (mode == FIND_ERROR) {
				notifyListeners(this.currentTask, new StatusInfo(IStatus.ERROR, "Error"), -1, -1, -1);
			}
			this.lock.lock();
			try {
				this.activeMode= mode;
				this.activeExpression= this.currentTask.expression;
				this.findTotalCount= count;
				this.findFilteredCount= filteredCount;
				this.findStore.clear(filteredCount);
				this.findLastMatchIdx= -1;
				if (mode != FIND_ERROR && this.lock.state < Lock.PAUSE_STATE) {
					this.lock.state= 0;
				}
			}
			finally {
				this.lock.unlock();
			}
		}
	}
	
	private long getFilteredCount(final long count, final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String filterVar= this.dataProvider.checkFilter();
		if (filterVar == null || count == 0) {
			return count;
		}
		final FunctionCall call= r.createFunctionCall(RJTmp.GET_FILTERED_COUNT);
		call.addChar(RJTmp.FILTER_PAR, filterVar);
		call.addChar(RJTmp.INDEX_PAR, this.rCacheFind);
		return RDataUtil.checkSingleIntValue(call.evalData(monitor));
	}
	
	private void updateFindingFragments(final IRToolService r, final IProgressMonitor monitor) throws CoreException {
		try {
			while (true) {
				final Fragment<RObject> fragment;
				this.lock.lock();
				try {
					fragment= this.findStore.getNextScheduledFragment();
				}
				finally {
					this.lock.unlock();
				}
				if (fragment == null) {
					break;
				}
				if (monitor.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				
				final RObject fragmentObject= loadFindFragment(fragment, r, monitor);
				this.lock.lock();
				try {
					this.findStore.updateFragment(fragment, fragmentObject);
				}
				finally {
					this.lock.unlock();
				}
			}
		}
		catch (final Exception e) {
			AbstractRDataProvider.checkCancel(e);
			this.lock.lock();
			try {
				clear(-1);
				if (this.lock.state < Lock.RELOAD_STATE) {
					this.lock.state= Lock.RELOAD_STATE;
				}
			}
			finally {
				this.lock.unlock();
			}
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when loading find matches for data viewer.", e));
			return;
		}
	}
	
	private RObject loadFindFragment(final LazyRStore.Fragment<RObject> fragment,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String revIndexName= this.dataProvider.checkRevIndex(r, monitor);
		{	final StringBuilder cmd= this.dataProvider.getRCmdStringBuilder();
			cmd.append("local({");
			if (revIndexName != null) {
				if (this.activeMode == FIND_CELL) {
					cmd.append("x <- ").append(RJTmp.ENV+'$').append(revIndexName)
							.append("[").append(RJTmp.ENV+'$').append(this.rCacheFind).append("[,1L]").append("]\n");
					cmd.append("x <- cbind(x, ").append(RJTmp.ENV+'$').append(this.rCacheFind).append("[,2L]").append(")\n");
				}
				else {
					cmd.append("x <- ").append(RJTmp.ENV+'$').append(revIndexName)
							.append("[").append(RJTmp.ENV+'$').append(this.rCacheFind).append("]\n");
				}
				cmd.append("x <- na.omit(x)\n");
			}
			else {
				cmd.append("x <- ").append(RJTmp.ENV+'$').append(this.rCacheFind).append("\n");
			}
			cmd.append("x <- x[order(");
			if (this.activeMode == FIND_CELL) {
				cmd.append((this.currentTask.firstInRow) ? "x[,1L], x[,2L]" : "x[,2L], x[,1L]");
			}
			else {
				cmd.append("x");
			}
			cmd.append(")").append("[").append(fragment.getRowBeginIdx() + 1).append(":").append(fragment.getRowEndIdx()).append("]");
			if (this.activeMode == FIND_CELL) {
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
		this.lock.lock();
		try {
			task= this.scheduledTask;
			mode= this.activeMode;
			totalCount= this.findTotalCount;
			filteredCount= this.findFilteredCount;
			globalMatchIdx= this.findLastMatchIdx;
			
			if (task == null || !task.equals(this.currentTask)
					|| this.lock.state == Lock.LOCAL_PAUSE_STATE) {
				notifyListeners(task, new StatusInfo(IStatus.INFO, "Finding..."), -1, -1, -1);
				this.lock.scheduleUpdate(null, null, null, 0, null);
				return;
			}
			if (mode == FIND_ERROR) {
				notifyListeners(task, new StatusInfo(IStatus.ERROR, "Error."), -1, -1, -1);
				return;
			}
		}
		finally {
			this.lock.unlock();
		}
		
		if (filteredCount < 0) {
			if (r != null) {
				filteredCount= getFilteredCount(totalCount, r, monitor);
				this.lock.lock();
				try {
					this.findFilteredCount= filteredCount;
					this.findStore.clear(filteredCount);
				}
				finally {
					this.lock.unlock();
				}
			}
			else {
				this.lock.lock();
				try {
					if (task != this.scheduledTask) {
						return;
					}
					this.lock.scheduleUpdate(null, null, null, 0, null);
				}
				finally {
					this.lock.unlock();
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
			globalMatchIdx= filteredCount - 1;
		}
		else if (globalMatchIdx < 0) {
			globalMatchIdx= filteredCount / 2;
		}
		try {
			final long[] rPos;
			final long[] low;
			final long[] high;
			final int rowIdx;
			final int colIdx;
			if (mode == FIND_CELL) {
				rowIdx= (task.firstInRow) ? 0 : 1;
				colIdx= (task.firstInRow) ? 1 : 0;
				
				rPos= new long[2];
				rPos[rowIdx]= task.rowIdx + 1;
				rPos[colIdx]= task.columnIdx + 1;
				low= new long[2];
				high= new long[2];
			}
			else {
				rowIdx= 0;
				colIdx= -1;
				
				rPos= new long[] { task.rowIdx + 1 };
				low= new long[1];
				high= new long[1];
			}
			
			while (true) {
				int last= 0;
				LazyRStore.Fragment<RObject> fragment;
				while (true) {
					if (monitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					
					fragment= this.lock.getFragment(this.findStore, globalMatchIdx, 0, 0, monitor);
					if (fragment != null) {
						final RStore<?> data= fragment.getRObject().getData();
						final int length= (int) fragment.getRowCount();
						low[rowIdx]= data.getInt(0);
						high[rowIdx]= data.getInt(length - 1);
						if (mode == FIND_CELL) {
							low[colIdx]= data.getInt(length);
							high[colIdx]= data.getInt(length + length - 1);
						}
						if (RDataUtil.compare(rPos, low) < 0) {
							globalMatchIdx= fragment.getRowBeginIdx() - 1;
							if (globalMatchIdx < 0
									|| (task.forward && last == +1)) {
								break;
							}
							last= -1;
						}
						if (RDataUtil.compare(rPos, high) > 0) {
							globalMatchIdx= fragment.getRowEndIdx();
							if (globalMatchIdx > filteredCount
									|| (!task.forward && last == -1)) {
								break;
							}
							last= +1;
						}
						break;
					}
					else if (r != null) {
						updateFindingFragments(r, monitor);
						this.lock.lock();
						try {
							if (task != this.scheduledTask
									|| this.lock.state > 0) {
								return;
							}
						}
						finally {
							this.lock.unlock();
						}
					}
					else {
						this.lock.lock();
						try {
							if (task != this.scheduledTask) {
								return;
							}
							this.lock.scheduleUpdate(null, null, null, 0, null);
						}
						finally {
							this.lock.unlock();
						}
						return;
					}
				}
				
				final RStore<?> data= fragment.getRObject().getData();
				final int length= (int) fragment.getRowCount();
				long localMatchIdx;
				if (mode == FIND_CELL) {
					low[rowIdx]= 0;
					low[colIdx]= length;
					localMatchIdx= RDataUtil.binarySearch(data, low, length, rPos);
				}
				else {
					localMatchIdx= RDataUtil.binarySearch(data, rPos[rowIdx]);
				}
				if (localMatchIdx >= 0) {
					localMatchIdx += (task.forward) ? +1 : -1;
				}
				else {
					localMatchIdx= -(localMatchIdx + 1);
					localMatchIdx += (task.forward) ? 0 : -1;
				}
				if (localMatchIdx < 0 || localMatchIdx >= length) {
					notifyListeners(task, new StatusInfo(IStatus.INFO,
							NLS.bind("No further match (total {0}/{1}).", filteredCount, totalCount)),
							filteredCount, -1, -1 );
					return;
				}
				this.lock.lock();
				try {
					if (task != this.scheduledTask) {
						return;
					}
					this.findLastMatchIdx= globalMatchIdx= fragment.getRowBeginIdx() + localMatchIdx;
				}
				finally {
					this.lock.unlock();
				}
				{	final long posCol;
					final long posRow;
					rPos[rowIdx]= data.getInt(localMatchIdx);
					posRow= rPos[rowIdx] - 1;
					if (mode == FIND_CELL) {
						rPos[colIdx]= data.getInt(length + localMatchIdx);
						posCol= rPos[colIdx] - 1;
					}
					else {
						posCol= -1;
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
				FindManager.this.lock.lock();
				try {
					if (task != null && task != FindManager.this.scheduledTask) {
						return;
					}
				}
				finally {
					FindManager.this.lock.unlock();
				}
				final FindEvent event= new FindEvent(status, total, rowIdx, colIdx);
				for (final IFindListener listener : FindManager.this.listeners.toArray()) {
					listener.handleFindEvent(event);
				}
			}
		});
	}
	
}
