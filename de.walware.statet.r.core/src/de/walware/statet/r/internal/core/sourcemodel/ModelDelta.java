/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelElementDelta;
import de.walware.ecommons.ltk.IModelManager;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.rsource.ast.RAstInfo;


/**
 * Currently not really a delta
 */
public class ModelDelta implements IModelElementDelta {
	
	
	private final int fLevel;
	private final IModelElement fElement;
	private final IRModelInfo fOldInfo;
	private final RAstInfo fOldAst;
	private final IRModelInfo fNewInfo;
	private final RAstInfo fNewAst;
	
	
	public ModelDelta(final IModelElement element,
			final IRModelInfo oldInfo, final IRModelInfo newInfo) {
		fLevel = IModelManager.MODEL_FILE;
		fElement = element;
		fOldInfo = oldInfo;
		fOldAst = ((oldInfo != null) ? oldInfo.getAst() : null);
		fNewInfo = newInfo;
		fNewAst = ((newInfo != null) ? newInfo.getAst() : null);
	}
	
	
	@Override
	public IModelElement getModelElement() {
		return fElement;
	}
	
	@Override
	public RAstInfo getOldAst() {
		return fOldAst;
	}
	
	@Override
	public RAstInfo getNewAst() {
		return fNewAst;
	}
	
}
