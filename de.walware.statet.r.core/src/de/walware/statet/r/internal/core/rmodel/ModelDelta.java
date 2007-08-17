/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.IModelElementDelta;


public class ModelDelta implements IModelElementDelta {

	private final IModelElement fElement;
	private final AstInfo fOldAst;
	private final AstInfo fNewAst;
	
	
	public ModelDelta(final IModelElement element,
			final AstInfo oldAst, final AstInfo newAst) {
		fElement = element;
		fOldAst = oldAst;
		fNewAst = newAst;
	}

	public IModelElement getModelElement() {
		return fElement;
	}

	public AstInfo getOldAst() {
		return fOldAst;
	}
	
	public AstInfo getNewAst() {
		return fNewAst;
	}

}
