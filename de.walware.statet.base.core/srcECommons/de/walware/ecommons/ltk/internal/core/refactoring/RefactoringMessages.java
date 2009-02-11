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

package de.walware.ecommons.ltk.internal.core.refactoring;

import org.eclipse.osgi.util.NLS;


public class RefactoringMessages extends NLS {
	
	
	public static String Common_FinalCheck_label;
	public static String Common_CreateChanges_label;
	public static String Common_Source_Project_label;
	public static String Common_Source_Workspace_label;
	
	public static String Common_error_CannotCreateFromDescr_message;
	public static String Common_error_AnalyzingSourceDocument_message;
	public static String Common_error_CreatingElementChange_message;
	
	public static String Check_FileUnsavedChanges_message;
	
	public static String DynamicValidationState_WorkspaceChanged_message;
	
	public static String DeleteRefactoring_label;
	public static String DeleteRefactoring_description_singular;
	public static String DeleteRefactoring_description_plural;
	
	
	static {
		initializeMessages(RefactoringMessages.class.getName(), RefactoringMessages.class);
	}
	private RefactoringMessages() {}
	
}
