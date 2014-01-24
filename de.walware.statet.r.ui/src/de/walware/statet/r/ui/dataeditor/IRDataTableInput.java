/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.dataeditor;

import de.walware.statet.r.core.model.RElementName;


public interface IRDataTableInput {
	
	
	interface StateListener {
		
		void tableUnavailable();
		
	}
	
	
	RElementName getElementName();
	
	String getFullName();
	
	String getLastName();
	
	boolean isAvailable();
	
	void addStateListener(StateListener listener);
	void removeStateListener(StateListener listener);
	
}
