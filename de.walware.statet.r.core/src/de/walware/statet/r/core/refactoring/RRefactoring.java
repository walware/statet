/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.refactoring;

import de.walware.eclipsecommons.ltk.core.refactoring.CommonRefactoringFactory;

import de.walware.statet.r.internal.core.RRefactoringFactory;


/**
 * R refactoring
 */
public class RRefactoring {
	
	
	private static final CommonRefactoringFactory R_FACTORY = new RRefactoringFactory();
	
	public static CommonRefactoringFactory getFactory() {
		return R_FACTORY;
	}
	
	
	public static final String DELETE_ELEMENTS_REFACTORING_ID = "de.walware.r.refactoring.DeleteElementsOperation"; //$NON-NLS-1$
	
	public static final String DELETE_ELEMENTS_PROCESSOR_ID = "de.walware.r.refactoring.DeleteElementsProcessor"; //$NON-NLS-1$
	
}
