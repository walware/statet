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

package de.walware.ecommons.ltk.internal.ui.refactoring;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String CutElements_error_message;
	public static String CopyElements_error_message;
	public static String PastingElements_error_message;
	public static String DeleteElements_error_message;
	
	public static String ExecutionHelper_CannotExecute_message;
	
	public static String RefactoringStarter_ConfirmSave_Always_message;
	public static String RefactoringStarter_ConfirmSave_message;
	public static String RefactoringStarter_ConfirmSave_title;
	public static String RefactoringStarter_UnexpectedException;
	
	
	static {
		initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
