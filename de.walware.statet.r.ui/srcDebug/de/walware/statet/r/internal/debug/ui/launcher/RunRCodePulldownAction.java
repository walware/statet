/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class RunRCodePulldownAction implements IWorkbenchWindowPulldownDelegate {
	
	
	private static final String LAST_LAUNCH_REMEMBER_KEY = "de.walware.statet.r.RunRCodePullDown.LastLaunch.id"; //$NON-NLS-1$
	
	
	private class LaunchShortcutAction extends Action {
		
		private String fMode;
		private LaunchShortcutExtension fShortcut;
		
		
		/**
		 * Constructor for LaunchShortcutAction.
		 */
		public LaunchShortcutAction(final String mode, final LaunchShortcutExtension shortcut) {
			super();
			setText(NLS.bind(RLaunchingMessages.RLaunchPulldown_Item_label, shortcut.getLabel()));
			setImageDescriptor(shortcut.getImageDescriptor());
			setId(shortcut.getId());
			
			fMode = mode;
			fShortcut = shortcut;
		}
		
		public boolean isApplicable(final IEvaluationContext context) {
			try {
				final Expression expr = fShortcut.getShortcutEnablementExpression();
				return fShortcut.evalEnablementExpression(context, expr);
			} catch (final CoreException e) {
				return false;
			}
		}
		
		/**
		 * Runs with either the active editor or workbench selection.
		 *
		 * @see IAction#run()
		 */
		@Override
		public void run() {
			final IWorkbenchWindow wb = UIAccess.getActiveWorkbenchWindow(true);
			if (wb != null) {
				final IWorkbenchPage page = wb.getActivePage();
				if (page != null) {
					final ISelection selection = page.getSelection();
					if (selection instanceof IStructuredSelection) {
						fShortcut.launch(selection, fMode);
					} else {
						final IEditorPart editor = page.getActiveEditor();
						if (editor != null) {
							fShortcut.launch(editor, fMode);
						}
					}
				}
			}
			updateLastLaunch(this);
		}
		
	}
	
	
	private String fMode = ILaunchManager.RUN_MODE;
	private LaunchShortcutAction[] fActions;
	
	private IAction fButtonAction;
	private LaunchShortcutAction fLastAction;
	
	
	public void init(final IWorkbenchWindow window) {
		loadShortcuts();
	}
	
	protected void initAction(final IAction action) {
		fButtonAction = action;
		
		final String lastLaunchId = RUIPlugin.getDefault().getDialogSettings().get(LAST_LAUNCH_REMEMBER_KEY);
		if (lastLaunchId != null) {
			for (final LaunchShortcutAction item : fActions) {
				if (lastLaunchId.equals(item.getId())) {
					fLastAction = item;
					break;
				}
			}
		}
		if (fLastAction == null)
			fLastAction = fActions[0];
		
		updateTooltip();
	}
	
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (fButtonAction == null)
			initAction(action);
	}
	
	public void dispose() {
		fButtonAction = null;
		fActions = null;
	}
	
	
	public Menu getMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		fillMenu(menu);
		updateEnablement();
		return menu;
	}
	
	public void run(final IAction action) {
		updateEnablement();
		
		final LaunchShortcutAction lastLaunched = getLastLaunch();
		if (lastLaunched != null && lastLaunched.isEnabled())
			lastLaunched.run();
	}
	
	
	private void loadShortcuts() {
		@SuppressWarnings("unchecked")
		final List<LaunchShortcutExtension> list = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchShortcuts("de.walware.statet.r.basic"); //$NON-NLS-1$
		final List<LaunchShortcutAction> actions = new ArrayList<LaunchShortcutAction>(list.size());
		
		for (final LaunchShortcutExtension ext : list) {
			if (ext.getModes().contains(fMode))
				actions.add(new LaunchShortcutAction(fMode, ext));
		}
		fActions = actions.toArray(new LaunchShortcutAction[actions.size()]);
	}
	
	private void fillMenu(final Menu menu) {
		for (final LaunchShortcutAction shortcut : fActions) {
			final ActionContributionItem item = new ActionContributionItem(shortcut);
			item.fill(menu, -1);
		}
	}
	
	
	protected void updateLastLaunch(final LaunchShortcutAction action) {
		if (fLastAction != action) {
			fLastAction = action;
			updateTooltip();
			
			RUIPlugin.getDefault().getDialogSettings().put(LAST_LAUNCH_REMEMBER_KEY, action.getId());
		}
	}
	
	protected LaunchShortcutAction getLastLaunch() {
		return fLastAction;
	}
	
	/**
	 * Updates this action's tooltip to correspond to the most recent launch.
	 */
	protected void updateTooltip() {
		final LaunchShortcutAction lastLaunched = getLastLaunch();
		fButtonAction.setToolTipText(lastLaunched.getText());
		fButtonAction.setImageDescriptor(lastLaunched.getImageDescriptor());
	}
	
	protected void updateEnablement() {
		final IEvaluationContext context = createContext();
		
		for (final LaunchShortcutAction action : fActions) {
			if (action.isApplicable(context)) {
				action.setEnabled(true);
			}
			else {
				action.setEnabled(false);
			}
		}
	}
	
	/**
	 * org from LaunchShortcutsAction
	 * @return an Evaluation context with default variable = selection
	 */
	@SuppressWarnings("unchecked")
	private IEvaluationContext createContext() {
		List list = null;
		final IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
		if (window != null) {
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				final IWorkbenchPart activePart = page.getActivePart();
				if (activePart instanceof IEditorPart) {
					list = new ArrayList();
					list.add(((IEditorPart)activePart).getEditorInput());
				} else if (activePart != null) {
					final IWorkbenchPartSite site = activePart.getSite();
					if (site != null) {
						final ISelectionProvider selectionProvider = site.getSelectionProvider();
						if (selectionProvider != null) {
							final ISelection selection = selectionProvider.getSelection();
							if (selection instanceof IStructuredSelection) {
								list = ((IStructuredSelection)selection).toList();
							}
						}
					}
				}
			}
		}
		// create a default evaluation context with default variable
		// of the user selection or editor input
		if (list == null) {
		    list = Collections.EMPTY_LIST;
		}
		final IEvaluationContext context = new EvaluationContext(null, list);
		context.addVariable("selection", list); //$NON-NLS-1$
		
		return context;
	}
	
}
