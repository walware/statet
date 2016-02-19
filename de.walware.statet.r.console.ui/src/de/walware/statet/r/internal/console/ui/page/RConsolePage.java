/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.page;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsolePage;

import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.ui.IRConsoleHelpContextIds;
import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.console.ui.tools.ChangeWorkingDirectoryWizard;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.internal.console.ui.Messages;
import de.walware.statet.r.ui.RUIHelp;
import de.walware.statet.r.ui.editors.IRSourceEditor;
import de.walware.statet.r.ui.pkgmanager.OpenRPkgManagerHandler;
import de.walware.statet.r.ui.pkgmanager.StartAction;
import de.walware.statet.r.ui.rhelp.OpenRHelpHandler;


public class RConsolePage extends NIConsolePage {
	
	
	private IContextProvider fHelpContextProvider;
	
	
	public RConsolePage(final RConsole console, final IConsoleView view) {
		super(console, view);
	}
	
	
	@Override
	public RConsole getConsole() {
		return (RConsole) super.getConsole();
	}
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
	}
	
	@Override
	protected RInputConfigurator createInputEditorConfigurator() {
		return new RInputConfigurator(this, (IRSourceEditor) getInputGroup());
	}
	
	@Override
	protected RConsoleEditor createInputGroup() {
		return new RConsoleEditor(this);
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
		super.initActions(serviceLocator, handlers);
		
		final IContextService contextService = (IContextService) serviceLocator.getService(IContextService.class);
		contextService.activateContext("de.walware.statet.r.actionSets.RSessionTools"); //$NON-NLS-1$
		
		fHelpContextProvider = RUIHelp.createEnrichedRHelpContextProvider(
				getInputGroup().getViewer(), IRConsoleHelpContextIds.R_CONSOLE );
		getInputGroup().getViewer().getTextWidget().addHelpListener(new HelpListener() {
			@Override
			public void helpRequested(final HelpEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(fHelpContextProvider.getContext(null));
			}
		});
	}
	
	@Override
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final IActionBars actionBars, final HandlerCollection handlers) {
		super.contributeToActionBars(serviceLocator, actionBars, handlers);
		
		final IMenuManager menuManager = actionBars.getMenuManager();
		
		menuManager.appendToGroup(NICO_CONTROL_MENU_ID,
				new CommandContributionItem(new CommandContributionItemParameter(
					getSite(), null, NicoUI.PAUSE_COMMAND_ID, null,
					null, null, null,
					null, null, null,
					CommandContributionItem.STYLE_CHECK, null, false)));
		if (getConsole().getProcess().isProvidingFeatureSet(IRemoteEngineController.FEATURE_SET_ID)) {
			menuManager.appendToGroup(NICO_CONTROL_MENU_ID,
					new CommandContributionItem(new CommandContributionItemParameter(
						getSite(), null, NicoUI.DISCONNECT_COMMAND_ID, null,
						null, null, null,
						null, null, null,
						CommandContributionItem.STYLE_PUSH, null, false)));
			menuManager.appendToGroup(NICO_CONTROL_MENU_ID,
					new CommandContributionItem(new CommandContributionItemParameter(
						getSite(), null, NicoUI.RECONNECT_COMMAND_ID, null,
						null, null, null,
						null, null, null,
						CommandContributionItem.STYLE_PUSH, null, false)));
		}
		
		menuManager.insertBefore(SharedUIResources.ADDITIONS_MENU_ID, new Separator("workspace")); //$NON-NLS-1$
		menuManager.appendToGroup("workspace", //$NON-NLS-1$
				new ChangeWorkingDirectoryWizard.ChangeAction(this));
		
		menuManager.insertBefore(SharedUIResources.ADDITIONS_MENU_ID, new Separator("view")); //$NON-NLS-1$
		
		final RProcess process = (RProcess) getConsole().getProcess();
		final IREnv rEnv = (IREnv) process.getAdapter(IREnv.class);
		if (process.isProvidingFeatureSet(RConsoleTool.R_DATA_FEATURESET_ID) && rEnv != null) {
			menuManager.appendToGroup(NICO_CONTROL_MENU_ID,
					new HandlerContributionItem(new CommandContributionItemParameter(
							getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
							null, null, null,
							"Open Package Manager", "P", null,
							CommandContributionItem.STYLE_PUSH, null, false ),
					new OpenRPkgManagerHandler((RProcess) getTool(), getSite().getShell()) ));
			
			final MenuManager rEnvMenu = new MenuManager("R &Environment");
			menuManager.appendToGroup(NICO_CONTROL_MENU_ID, rEnvMenu);
			
			rEnvMenu.add(new HandlerContributionItem(new CommandContributionItemParameter(
							getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
							null, null, null,
							"Open Package Manager", "P", null,
							CommandContributionItem.STYLE_PUSH, null, false ),
					new OpenRPkgManagerHandler((RProcess) getTool(), getSite().getShell()) ));
			if (RPkgUtil.DEBUG) {
				rEnvMenu.add(new HandlerContributionItem(new CommandContributionItemParameter(
								getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
								null, null, null,
								"Open Package Manager - Clean", null, null, //$NON-NLS-1$
								CommandContributionItem.STYLE_PUSH, null, false ),
						new OpenRPkgManagerHandler((RProcess) getTool(), getSite().getShell()) {
							@Override
							protected IRPkgManager getPackageManager() {
								final IRPkgManager packageManager = super.getPackageManager();
								packageManager.clear();
								return packageManager;
							}
						}));
				rEnvMenu.add(new HandlerContributionItem(new CommandContributionItemParameter(
								getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
								null, null, null,
								"Open Package Manager - Install 'zic'", null, null, //$NON-NLS-1$
								CommandContributionItem.STYLE_PUSH, null, false ),
						new OpenRPkgManagerHandler((RProcess) getTool(), getSite().getShell()) {
							@Override
							protected StartAction getStartAction() {
								return new StartAction(StartAction.INSTALL, ImCollections.newList("zic")); //$NON-NLS-1$
							}
				}));
			}
			rEnvMenu.add(new HandlerContributionItem(new CommandContributionItemParameter(
							getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
							null, null, null,
							"Open R Help", "H", null,
							CommandContributionItem.STYLE_PUSH, null, false ),
					new OpenRHelpHandler(rEnv, process, true) ));
			
			rEnvMenu.add(new Separator());
			
			rEnvMenu.add(new CommandContributionItem(new CommandContributionItemParameter(
					getSite(), null, "de.walware.statet.r.commands.UpdateREnvIndex", null, //$NON-NLS-1$
					null, null, null,
					"Update &index (changes)", null, null,
					CommandContributionItem.STYLE_PUSH, null, false )));
			rEnvMenu.add(new CommandContributionItem(new CommandContributionItemParameter(
					getSite(), null, "de.walware.statet.r.commands.ResetREnvIndex", null, //$NON-NLS-1$
					null, null, null,
					"Reset inde&x (completely)", null, null,
					CommandContributionItem.STYLE_PUSH, null, false)));
		}
	}
	
	@Override
	protected void fillOutputContextMenu(final IMenuManager menuManager) {
		super.fillOutputContextMenu(menuManager);
		
		menuManager.appendToGroup("view", //$NON-NLS-1$
				new CommandContributionItem(new CommandContributionItemParameter(
						getSite(), null, NIConsole.ADJUST_OUTPUT_WIDTH_COMMAND_ID, null,
						null, null, null,
						Messages.AdjustWidth_label, Messages.AdjustWidth_mnemonic, null,
						CommandContributionItem.STYLE_PUSH, null, false )));
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (IContextProvider.class.equals(required)) {
			return fHelpContextProvider;
		}
		return super.getAdapter(required);
	}
	
	@Override
	protected void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		super.handleSettingsChanged(groupIds, options);
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID) 
				&& UIAccess.isOkToUse(getOutputViewer())) {
			final RCodeStyleSettings codeStyle = (getConsole()).getRCodeStyle();
			if (codeStyle.isDirty()) {
				getOutputViewer().setTabWidth(codeStyle.getTabSize());
			}
		}
	}
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		super.collectContextMenuPreferencePages(pageIds);
		pageIds.add("de.walware.statet.r.preferencePages.REditorOptions"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RTextStyles"); //$NON-NLS-1$
	}
	
}
