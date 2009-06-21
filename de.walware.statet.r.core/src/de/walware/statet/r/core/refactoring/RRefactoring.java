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

import de.walware.ecommons.ltk.core.refactoring.CommonRefactoringFactory;

import de.walware.statet.r.internal.core.refactoring.RRefactoringFactory;


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
	
	public static final String RENAME_IN_WORKSPACE_REFACTORING_ID = "de.walware.r.refactoring.RenameInWorkspaceOperation"; //$NON-NLS-1$
	
	public static final String RENAME_IN_REGION_REFACTORING_ID = "de.walware.r.refactoring.RenameInRegionOperation"; //$NON-NLS-1$
	
	public static final String INLINE_TEMP_REFACTORING_ID = "de.walware.r.refactoring.InlineTempOperation"; //$NON-NLS-1$
	
	public static final String EXTRACT_TEMP_REFACTORING_ID = "de.walware.r.refactoring.ExtractTempOperation"; //$NON-NLS-1$
	
	public static final String EXTRACT_FUNCTION_REFACTORING_ID = "de.walware.r.refactoring.ExtractFunctionOperation"; //$NON-NLS-1$
	
	public static final String CONVERT_FUNCTION_TO_S4_METHOD_REFACTORING_ID = "de.walware.r.refactoring.ConvertFunctionToS4MethodOperation"; //$NON-NLS-1$
	
}
