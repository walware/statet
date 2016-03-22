/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.ui.RUI;


public class ResolveCellIndexes {
	
	
	private final IToolRunnable findRunnable= new ISystemRunnable() {
		
		@Override
		public String getTypeId() {
			return "r/dataeditor/find"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Find Data (" + ResolveCellIndexes.this.dataProvider.getInput().getName() + ")";
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
				synchronized (this) {
					ResolveCellIndexes.this.findScheduled= false;
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
			final long[] coord;
			synchronized (this) {
				coord= ResolveCellIndexes.this.index;
				ResolveCellIndexes.this.findScheduled= false;
			}
			if (coord == null
					|| ResolveCellIndexes.this.dataProvider.getLockState() > Lock.LOCAL_PAUSE_STATE) {
				return;
			}
			try {
				coord[1]= resolveRowIdx(coord[1], (IRToolService) service, monitor);
				execute(coord[0], coord[1]);
			}
			catch (final CoreException | UnexpectedRDataException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
						"An error occurred when resolving indexes for data viewer.", e));
			}
		}
		
	};
	
	private boolean findScheduled;
	
	private final AbstractRDataProvider<?> dataProvider;
	
	private long[] index;
	
	
	public ResolveCellIndexes(final AbstractRDataProvider<?> dataProvider) {
		this.dataProvider= dataProvider;
	}
	
	
	public AbstractRDataProvider<?> getDataProvider() {
		return this.dataProvider;
	}
	
	public void resolve(final long columnIdx, final long rowIdx) {
		synchronized (this.findRunnable) {
			this.index= null;
			if (this.dataProvider.getFilter() != null
					|| this.dataProvider.getSortColumn() != null) {
				this.index= new long[] { columnIdx, rowIdx };
				if (!this.findScheduled) {
					this.findScheduled= true;
					this.dataProvider.schedule(this.findRunnable);
				}
				return;
			}
		}
		
		execute(columnIdx, rowIdx);
	}
	
	private long resolveRowIdx(final long rowIdx,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String revIndexName= this.dataProvider.checkRevIndex(r, monitor);
		if (revIndexName != null) {
			final StringBuilder cmd= this.dataProvider.getRCmdStringBuilder();
			cmd.append(RJTmp.ENV+'$').append(revIndexName)
					.append("[").append(rowIdx + 1).append("]");
			final RVector<?> vector= RDataUtil.checkRVector(r.evalData(cmd.toString(), monitor));
			final RStore<?> data= RDataUtil.checkLengthEqual(vector, 1).getData();
			if (data.isNA(0)) {
				return -1;
			}
			return ((data.getStoreType() == RStore.INTEGER) ?
							(long) data.getInt(0) : (long) data.getNum(0) )
					- 1;
		}
		return rowIdx;
	}
	
	protected void execute(final long columnIndex, final long rowIndex) {
	}
	
}
