/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.core.refactoring;

import org.eclipse.osgi.util.NLS;


public class RefactoringMessages extends NLS {
	
	
	public static String Common_FinalCheck_label;
	public static String Common_CreateChanges_label;
	public static String Common_Source_Project_label;
	public static String Common_Source_Workspace_label;
	
	
	static {
		initializeMessages(RefactoringMessages.class.getName(), RefactoringMessages.class);
	}
	private RefactoringMessages() {}
	
}
