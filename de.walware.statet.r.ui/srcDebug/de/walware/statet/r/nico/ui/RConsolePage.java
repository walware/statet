/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import java.util.Set;

import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;

import de.walware.statet.nico.ui.console.InputGroup;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsolePage;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.nico.ui.tools.ChangeWorkingDirectoryWizard;
import de.walware.statet.r.ui.RUIHelp;


public class RConsolePage extends NIConsolePage {

	
	private IContextProvider fHelpContextProvider;

	
	public RConsolePage(NIConsole console, IConsoleView view) {
		super(console, view);
	}

	@Override
	protected RInputConfiguration createInputEditorConfigurator() {
		return new RInputConfiguration((RConsole) getConsole());
	}
	
	@Override
	protected InputGroup createInputGroup() {
		return new RInputGroup(this);
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		
		fHelpContextProvider = RUIHelp.createEnrichedRHelpContextProvider(
				getInputGroup().getSourceViewer(), IRUIHelpContextIds.R_CONSOLE);
		getInputGroup().getSourceViewer().getTextWidget().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(fHelpContextProvider.getContext(null));
			}
		});
	}
	
	@Override
	protected void contributeToActionBars() {
		super.contributeToActionBars();
		
		IMenuManager menuManager = getSite().getActionBars().getMenuManager();
		menuManager.add(new ChangeWorkingDirectoryWizard.ChangeAction(this));
	}
	
	@Override
	public Object getAdapter(Class required) {
		if (IContextProvider.class.equals(required)) {
			return fHelpContextProvider;
		}
		return super.getAdapter(required);
	}
	
	@Override
	protected void handleSettingsChanged(Set<String> contexts) {
		super.handleSettingsChanged(contexts);
		if (contexts.contains(RCodeStyleSettings.CONTEXT_ID)) {
			RCodeStyleSettings codeStyle = ((RConsole) getConsole()).getRCodeStyle();
			if (codeStyle.isDirty()) {
				getOutputViewer().setTabWidth(codeStyle.getTabSize());
			}
		}
	}
	
}
