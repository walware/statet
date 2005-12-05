/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
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
		boolean isWin = os.startsWith("win");
		ArrayList<Element> list = new ArrayList<Element>();
		ResourceBundle resources = ResourceBundle.getBundle(ROptionsSelectionDialog.class.getPackage().getName()+".ROptionsMessages");
		
		addOption(list, resources, "--version", false);
		addOption(list, resources, "--save", false);
		addOption(list, resources, "--no-save", false);
		addOption(list, resources, "--no-environ", false);
		addOption(list, resources, "--no-site-file", false);
		addOption(list, resources, "--no-init-file", false);
		addOption(list, resources, "--restore", false);
		addOption(list, resources, "--no-restore", false);
		addOption(list, resources, "--no-restore-data", false);
		addOption(list, resources, "--vanilla", false);
		if (!isWin) addOption(list, resources, "--no-readline", false);
		if (isWin) addOption(list, resources, "--ess", false);
		addOption(list, resources, "--min-vsize", true);
		addOption(list, resources, "--max-vsize", true);
		addOption(list, resources, "--min-nsize", true);
		addOption(list, resources, "--max-nsize", true);
		addOption(list, resources, "--max-ppsize", true);
		if (isWin) addOption(list, resources, "--max-mem-size", true);
		addOption(list, resources, "--quiet", false);
		addOption(list, resources, "--slave", false);
		addOption(list, resources, "--verbose", false);
		if (!isWin) addOption(list, resources, "--debugger", true);
		if (!isWin) addOption(list, resources, "--gui", true);
		addOption(list, resources, "--args", false);
		
		fOptions = list.toArray(new Element[list.size()]);
	}
	private static void addOption(ArrayList<Element> list, ResourceBundle resources, String name, boolean hasArgument) {
		list.add(new Element(name, resources.getString("ROption_"+name.substring(2)+"_description"), hasArgument));
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
