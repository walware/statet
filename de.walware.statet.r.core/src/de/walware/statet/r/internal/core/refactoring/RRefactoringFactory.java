/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.refactoring;

import org.eclipse.ltk.core.refactoring.participants.DeleteProcessor;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringFactory;
import de.walware.ecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.ecommons.ltk.core.refactoring.RefactoringDestination;

import de.walware.statet.r.core.refactoring.RDeleteProcessor;
import de.walware.statet.r.core.refactoring.RPasteCodeProcessor;
import de.walware.statet.r.core.refactoring.RRefactoringAdapter;


public class RRefactoringFactory extends CommonRefactoringFactory {
	
	
	public RRefactoringFactory() {
	}
	
	
	@Override
	public RefactoringAdapter createAdapter(Object elements) {
		return new RRefactoringAdapter();
	}
	
	@Override
	public DeleteProcessor createDeleteProcessor(final Object elements, final RefactoringAdapter adapter) {
		return new RDeleteProcessor(createElementSet(elements), (RRefactoringAdapter) adapter);
	}
	
	@Override
	public RefactoringProcessor createPasteProcessor(final Object elementsToPaste,
			final RefactoringDestination destination, final RefactoringAdapter adapter) {
		if (elementsToPaste instanceof String) {
			return new RPasteCodeProcessor((String) elementsToPaste, destination, (RRefactoringAdapter) adapter);
		}
		return super.createPasteProcessor(elementsToPaste, destination, adapter);
	}
	
}
