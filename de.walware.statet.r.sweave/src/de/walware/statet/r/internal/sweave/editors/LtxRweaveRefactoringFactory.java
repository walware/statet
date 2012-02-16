/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.ltk.core.refactoring.participants.CopyProcessor;
import org.eclipse.ltk.core.refactoring.participants.DeleteProcessor;
import org.eclipse.ltk.core.refactoring.participants.MoveProcessor;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringFactory;
import de.walware.ecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.ecommons.ltk.core.refactoring.RefactoringDestination;
import de.walware.ecommons.ltk.core.refactoring.RefactoringElementSet;

import de.walware.docmlet.tex.core.sourcecode.TexRefactoring;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RRefactoring;
import de.walware.statet.r.core.refactoring.RRefactoringAdapter;
import de.walware.statet.r.sweave.text.LtxRweaveSwitch;


public class LtxRweaveRefactoringFactory extends CommonRefactoringFactory {
	
	
	private final CommonRefactoringFactory fLtxFactory;
	private final CommonRefactoringFactory fRFactory;
	
	
	public LtxRweaveRefactoringFactory() {
		fLtxFactory = TexRefactoring.getLtxFactory();
		fRFactory = RRefactoring.getFactory();
	}
	
	
	@Override
	public RefactoringAdapter createAdapter(Object elements) {
		LtxRweaveSwitch type = null;
		if (elements instanceof RefactoringElementSet) {
			elements = ((RefactoringElementSet) elements).getInitialObjects();
		}
		if (elements == null) {
			type = LtxRweaveSwitch.LTX;
		}
		else if (elements instanceof Object[]) {
			final Object[] array = (Object[]) elements;
			for (int i = 0; i < array.length; i++) {
				if (array[i] instanceof IModelElement) {
					final String modelTypeId = ((IModelElement) array[i]).getModelTypeId();
					final LtxRweaveSwitch current = (modelTypeId == RModel.TYPE_ID) ?
							LtxRweaveSwitch.R : LtxRweaveSwitch.LTX;
					if (type == null) {
						type = current;
						continue;
					}
					else if (type == current) {
						continue;
					}
					else {
						return null;
					}
				}
			}
		}
		else if (elements instanceof IModelElement) {
			final String modelTypeId = ((IModelElement) elements).getModelTypeId();
			type = (modelTypeId == RModel.TYPE_ID) ?
					LtxRweaveSwitch.R : LtxRweaveSwitch.LTX;
		}
		if (type != null) {
			switch (type) {
			case LTX:
				return fLtxFactory.createAdapter(elements);
			case R:
				return fRFactory.createAdapter(elements);
			}
		}
		return null;
	}
	
	
	@Override
	public DeleteProcessor createDeleteProcessor(final Object elementsToDelete, final RefactoringAdapter adapter) {
		if (adapter instanceof RRefactoringAdapter) {
			return fRFactory.createDeleteProcessor(elementsToDelete, adapter);
		}
		return fLtxFactory.createDeleteProcessor(elementsToDelete, adapter);
	}
	
	@Override
	public MoveProcessor createMoveProcessor(final Object elementsToMove, final RefactoringDestination destination,
			final RefactoringAdapter adapter) {
		if (adapter instanceof RRefactoringAdapter) {
			return fRFactory.createMoveProcessor(elementsToMove, destination, adapter);
		}
		return fLtxFactory.createMoveProcessor(elementsToMove, destination, adapter);
	}
	
	@Override
	public CopyProcessor createCopyProcessor(final Object elementsToCopy, final RefactoringDestination destination,
			final RefactoringAdapter adapter) {
		if (adapter instanceof RRefactoringAdapter) {
			return fRFactory.createCopyProcessor(elementsToCopy, destination, adapter);
		}
		return fLtxFactory.createCopyProcessor(elementsToCopy, destination, adapter);
	}
	
	@Override
	public RefactoringProcessor createPasteProcessor(final Object elementsToPaste, final RefactoringDestination destination,
			final RefactoringAdapter adapter) {
		if (adapter instanceof RRefactoringAdapter) {
			return fRFactory.createPasteProcessor(elementsToPaste, destination, adapter);
		}
		return fLtxFactory.createPasteProcessor(elementsToPaste, destination, adapter);
	}
	
}
