/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.dataeditor;

import de.walware.rj.services.utils.dataaccess.LazyRStore;


abstract class Lock {
	
	static final int ERROR_STATE = 4;
	static final int RELOAD_STATE = 3;
	static final int PAUSE_STATE = 2;
	static final int LOCAL_PAUSE_STATE = 1;
	
	int state;
	
	
	boolean isReady() throws LoadDataException {
		if (state > 0) {
			switch (state) {
			case Lock.LOCAL_PAUSE_STATE:
			case Lock.PAUSE_STATE:
				return false;
			case Lock.RELOAD_STATE:
				throw new LoadDataException(false);
			default:
				throw new LoadDataException(true);
			}
		}
		return true;
	}
	
	/* Not nice at this place, but handy */
	synchronized <T> LazyRStore.Fragment<T> getFragment(final LazyRStore<T> store,
			final long rowIdx, final long columnIdx) throws LoadDataException {
		return (isReady()) ? store.getFragment(rowIdx, columnIdx) : null;
	}
	
}
