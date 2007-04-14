/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.launchconfigs;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;

import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.internal.debug.launchconfigs.dialogs.ElementListWDescriptionSelectionDialog;


/**
 * 
 * @author Stephan Wahlbrink
 */
public class ROptionsSelectionDialog extends ElementListWDescriptionSelectionDialog {

	
	protected static final Element[] fOptions;
	static {
		String os = Platform.getOS();
		boolean isWin = os.startsWith("win"); //$NON-NLS-1$
		ArrayList<Element> list = new ArrayList<Element>();
		ResourceBundle resources = ResourceBundle.getBundle(ROptionsSelectionDialog.class.getPackage().getName()+".ROptionsMessages"); //$NON-NLS-1$
		
		addOption(list, resources, "--version", false); //$NON-NLS-1$
		addOption(list, resources, "--save", false); //$NON-NLS-1$
		addOption(list, resources, "--no-save", false); //$NON-NLS-1$
		addOption(list, resources, "--no-environ", false); //$NON-NLS-1$
		addOption(list, resources, "--no-site-file", false); //$NON-NLS-1$
		addOption(list, resources, "--no-init-file", false); //$NON-NLS-1$
		addOption(list, resources, "--restore", false); //$NON-NLS-1$
		addOption(list, resources, "--no-restore", false); //$NON-NLS-1$
		addOption(list, resources, "--no-restore-data", false); //$NON-NLS-1$
		addOption(list, resources, "--vanilla", false); //$NON-NLS-1$
		if (!isWin) addOption(list, resources, "--no-readline", false); //$NON-NLS-1$
		if (isWin) addOption(list, resources, "--ess", false); //$NON-NLS-1$
		addOption(list, resources, "--min-vsize", true); //$NON-NLS-1$
		addOption(list, resources, "--max-vsize", true); //$NON-NLS-1$
		addOption(list, resources, "--min-nsize", true); //$NON-NLS-1$
		addOption(list, resources, "--max-nsize", true); //$NON-NLS-1$
		addOption(list, resources, "--max-ppsize", true); //$NON-NLS-1$
		if (isWin) addOption(list, resources, "--max-mem-size", true); //$NON-NLS-1$
		addOption(list, resources, "--quiet", false); //$NON-NLS-1$
		addOption(list, resources, "--slave", false); //$NON-NLS-1$
		addOption(list, resources, "--verbose", false); //$NON-NLS-1$
		if (!isWin) addOption(list, resources, "--debugger", true); //$NON-NLS-1$
		if (!isWin) addOption(list, resources, "--gui", true); //$NON-NLS-1$
		addOption(list, resources, "--args", false); //$NON-NLS-1$
		
		fOptions = list.toArray(new Element[list.size()]);
	}
	private static void addOption(ArrayList<Element> list, ResourceBundle resources, String name, boolean hasArgument) {
		list.add(new Element(name, resources.getString("ROption_"+name.substring(2)+"_description"), hasArgument)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	/**
	 * @param parent
	 * @param renderer
	 */
	public ROptionsSelectionDialog(Shell parent) {
		super(parent, true);
		
		setTitle(RLaunchingMessages.ROptionsSelectionDialog_title);
		setMessage(RLaunchingMessages.ROptionsSelectionDialog_message);

		setElements(fOptions);
	}

}
