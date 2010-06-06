/*******************************************************************************
 * Copyright (c) 2006-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.actions.ToolAction;
import de.walware.statet.nico.ui.console.ConsolePageEditor;
import de.walware.statet.nico.ui.console.NIConsolePage;

import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.nico.ui.REnvIndexUpdateHandler;
import de.walware.statet.r.internal.nico.ui.RInputConfigurator;
import de.walware.statet.r.internal.nico.ui.RInputGroup;
import de.walware.statet.r.internal.nico.ui.RNicoMessages;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.nico.RProcess;
import de.walware.statet.r.nico.RTool;
import de.walware.statet.r.nico.ui.tools.ChangeWorkingDirectoryWizard;
import de.walware.statet.r.ui.RUIHelp;


public class RConsolePage extends NIConsolePage {
	
	
	private class AdjustWithAction extends ToolAction implements IToolRunnable {
		
		
		public AdjustWithAction() {
			super(RConsolePage.this, true);
			setText(RNicoMessages.AdjustWidth_label);
			setDescription(RNicoMessages.AdjustWidth_description);
		}
		
		
		@Override
		public void run() {
			try {
				final ToolController controller = NicoUITools.accessController(RTool.TYPE, getConsole().getProcess());
				controller.submit(this);
			}
			catch (final CoreException e) {
			}
		}
		
		
		public void changed(final int event, final ToolProcess process) {
		}
		
		public String getTypeId() {
			return "r/console/width"; //$NON-NLS-1$
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.TOOLS;
		}
		
		public String getLabel() {
			return RNicoMessages.AdjustWidth_task;
		}
		
		public void run(final IToolRunnableControllerAdapter tools, final IProgressMonitor monitor) 
				throws InterruptedException, CoreException {
			final AtomicInteger width = new AtomicInteger(-1);
			UIAccess.getDisplay().syncExec(new Runnable() {
				public void run() {
					final TextConsoleViewer outputViewer = getOutputViewer();
					if (UIAccess.isOkToUse(outputViewer)) {
						final GC gc = new GC(Display.getCurrent());
						gc.setFont(outputViewer.getTextWidget().getFont());
						final FontMetrics fontMetrics = gc.getFontMetrics();
						final int charWidth = fontMetrics.getAverageCharWidth();
						final int clientWidth = outputViewer.getTextWidget().getClientArea().width;
						width.set(clientWidth/charWidth);
						gc.dispose();
					}
				}
			});
			int setWidth = width.get();
			if (setWidth >= 0) {
				if (setWidth < 10) {
					setWidth = 10;
				}
				tools.submitToConsole("options(width = "+setWidth+")", monitor); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
	}
	
	
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
		return new RInputConfigurator(this, getInputGroup());
	}
	
	@Override
	protected ConsolePageEditor createInputGroup() {
		return new RInputGroup(this);
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
		super.initActions(serviceLocator, handlers);
		
		final IContextService contextService = (IContextService) serviceLocator.getService(IContextService.class);
		contextService.activateContext("de.walware.statet.r.actionSets.RSessionTools"); //$NON-NLS-1$
		
		fHelpContextProvider = RUIHelp.createEnrichedRHelpContextProvider(
				getInputGroup().getViewer(), IRUIHelpContextIds.R_CONSOLE);
		getInputGroup().getViewer().getTextWidget().addHelpListener(new HelpListener() {
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
		menuManager.appendToGroup("view", //$NON-NLS-1$
				new AdjustWithAction());
		
		final RProcess process = (RProcess) getConsole().getProcess();
		if (process.isProvidingFeatureSet(RTool.R_DATA_FEATURESET_ID)
				&& process.getAdapter(IREnvConfiguration.class) != null) {
			final MenuManager rEnv = new MenuManager("R &Environment");
			menuManager.appendToGroup(NICO_CONTROL_MENU_ID, rEnv);
			
			rEnv.add(new HandlerContributionItem(new CommandContributionItemParameter(
					getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
					null, null, null,
					"Index c&ompletely", null, null,
					CommandContributionItem.STYLE_PUSH, null, false),
					new REnvIndexUpdateHandler(process, true) ));
			rEnv.add(new HandlerContributionItem(new CommandContributionItemParameter(
					getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
					null, null, null,
					"Index ch&anges", null, null,
					CommandContributionItem.STYLE_PUSH, null, false),
					new REnvIndexUpdateHandler(process, false) ));
			
		}
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
		pageIds.add("de.walware.statet.r.preferencePages.RTextStylesPage"); //$NON-NLS-1$
	}
	
}
