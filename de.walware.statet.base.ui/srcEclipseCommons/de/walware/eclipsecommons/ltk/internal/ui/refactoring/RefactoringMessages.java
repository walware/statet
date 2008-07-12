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

package de.walware.eclipsecommons.ltk.internal.ui.refactoring;

import org.eclipse.osgi.util.NLS;


public class RefactoringMessages extends NLS {
	
	
	public static String CutElements_error_message;
	public static String CopyElements_error_message;
	public static String PastingElements_error_message;
	public static String DeleteElements_error_message;
	
	public static String ExecutionHelper_CannotExecute_message;
	
	
	static {
		initializeMessages(RefactoringMessages.class.getName(), RefactoringMessages.class);
	}
	private RefactoringMessages() {}
	
}
