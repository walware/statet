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

package de.walware.statet.r.ui.dataeditor;

import de.walware.rj.data.RStore;


public interface IRDataTableVariable {
	
	
	int LOGI = RStore.LOGICAL;
	int INT = RStore.INTEGER;
	int NUM = RStore.NUMERIC;
	int CPLX = RStore.COMPLEX;
	int CHAR = RStore.CHARACTER;
	int RAW = RStore.RAW;
	int FACTOR = RStore.FACTOR;
	int DATE = 0x11;
	//	int TIME = 0x12;
	int DATETIME = 0x13;
	
	
	int COLUMN = 1;
	int ROW = 2;
	
	
	String getName();
	
	int getVarType();
	
	int getVarPresentation();
	
}
