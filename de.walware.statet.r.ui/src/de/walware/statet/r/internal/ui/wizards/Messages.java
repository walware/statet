/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String NewRScriptFileWizard_title;
	public static String NewRScriptFileWizardPage_title;
	public static String NewRScriptFileWizardPage_description;
	
	public static String NewRDocFileWizard_title;
	public static String NewRDocFileWizardPage_title;
	public static String NewRDocFileWizardPage_description;
	
	public static String NewRProjectWizard_title;
	public static String NewRProjectWizardPage_title;
	public static String NewRProjectWizardPage_description;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
