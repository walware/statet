/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.refactoring;

import de.walware.ecommons.ltk.core.refactoring.CommonDeleteProcessor;
import de.walware.ecommons.ltk.core.refactoring.RefactoringElementSet;


public class RDeleteProcessor extends CommonDeleteProcessor {
	
	
	public RDeleteProcessor(final RefactoringElementSet elements, final RRefactoringAdapter adapter) {
		super(elements, adapter);
	}
	
	
	@Override
	public String getIdentifier() {
		return RRefactoring.DELETE_ELEMENTS_PROCESSOR_ID;
	}
	
	@Override
	protected String getRefactoringIdentifier() {
		return RRefactoring.DELETE_ELEMENTS_REFACTORING_ID;
	}
	
}
