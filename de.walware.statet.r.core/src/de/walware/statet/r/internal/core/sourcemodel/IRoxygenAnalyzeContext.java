/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RClass;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RMethod;


public interface IRoxygenAnalyzeContext {
	
	
	IRModelInfo getModelInfo();
	
	IRFrameInSource getNamespaceFrame(String name);
	
	void createSelfAccess(IRLangSourceElement element, RAstNode symbol);
	void createNamespaceImportAccess(RAstNode symbol);
	void createNamespaceObjectImportAccess(IRFrameInSource namespace, RAstNode symbol);
	void createSlotAccess(RClass rClass, RAstNode symbol);
	void createArgAccess(RMethod rMethod, RAstNode symbol);
	void createRSourceRegion(RAstNode node);
	
}
