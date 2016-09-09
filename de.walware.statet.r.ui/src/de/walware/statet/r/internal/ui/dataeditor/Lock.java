/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.services.utils.dataaccess.LazyRStore;


abstract class Lock extends ReentrantLock {
	
	
	static final int ERROR_STATE= 4;
	static final int RELOAD_STATE= 3;
	static final int PAUSE_STATE= 2;
	static final int LOCAL_PAUSE_STATE= 1;
	
	
	int state;
	
	protected final Condition requestor= newCondition();
	protected final Condition worker= newCondition();
	
	boolean scheduled;
	
	
	Lock() {
	}
	
	
	final boolean isReady() throws LoadDataException {
		if (this.state > 0) {
			switch (this.state) {
			case Lock.LOCAL_PAUSE_STATE:
			case Lock.PAUSE_STATE:
				return false;
			case Lock.RELOAD_STATE:
				throw new LoadDataException(true);
			default:
				throw new LoadDataException(false);
			}
		}
		return true;
	}
	
	/* Not nice at this place, but handy */
	final <T> LazyRStore.Fragment<T> getFragment(final LazyRStore<T> store,
			final long rowIdx, final long columnIdx,
			final int flags, final IProgressMonitor monitor) throws LoadDataException {
		lock();
		try {
			return (isReady()) ?
					store.getFragment(rowIdx, columnIdx, flags, monitor) :
					null;
		}
		finally {
			unlock();
		}
	}
	
	
	void notifyWorker() {
		this.worker.signalAll();
	}
	
	void clear() {
		this.requestor.signalAll();
		this.worker.signalAll();
	}
	
}
