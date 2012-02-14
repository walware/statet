/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.refactoring;

import org.eclipse.ltk.core.refactoring.participants.DeleteProcessor;

import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringFactory;
import de.walware.ecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.ecommons.ltk.core.refactoring.RefactoringElementSet;

import de.walware.statet.r.core.refactoring.RDeleteProcessor;
import de.walware.statet.r.core.refactoring.RRefactoringAdapter;


public class RRefactoringFactory extends CommonRefactoringFactory {
	
	
	public RRefactoringFactory() {
	}
	
	
	@Override
	public RefactoringAdapter createAdapter() {
		return new RRefactoringAdapter();
	}
	
	@Override
	public DeleteProcessor createDeleteProcessor(final Object[] elements, final RefactoringAdapter adapter) {
		return new RDeleteProcessor(new RefactoringElementSet(elements), (RRefactoringAdapter) adapter);
	}
	
}
