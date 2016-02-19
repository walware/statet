/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;

import de.walware.statet.nico.core.runtime.History.Entry;


/**
 * 
 * @see History
 * @see History#addListener(IHistoryListener)
 */
public interface IHistoryListener {
	
	
	void entryAdded(History source, Entry e);
	void entryRemoved(History source, Entry e);
	
	void completeChange(History source, Entry[] es);
	
	
}
