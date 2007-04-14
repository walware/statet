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

package de.walware.statet.r.internal.debug.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
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

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.r.internal.debug.RLaunchingMessages;


public class RunRCodePulldownAction implements IWorkbenchWindowPulldownDelegate {
	
	
	private static final String LAST_LAUNCH_REMEMBER_KEY = "de.walware.statet.r.RunRCodePullDown.LastLaunch.id"; //$NON-NLS-1$
	
	
	private class LaunchShortcutAction extends Action {
		
		private String fMode;
		private LaunchShortcutExtension fShortcut;
		
		
		/**
		 * Constructor for LaunchShortcutAction.
		 */
		public LaunchShortcutAction(String mode, LaunchShortcutExtension shortcut) {
			super();
			setText(NLS.bind(RLaunchingMessages.RLaunchPulldown_Item_label, shortcut.getLabel()));
			setImageDescriptor(shortcut.getImageDescriptor());
			setId(shortcut.getId());
			
			fMode = mode;
			fShortcut = shortcut;
		}
		
		public boolean isApplicable(IEvaluationContext context) {
			try {
				Expression expr = fShortcut.getShortcutEnablementExpression();
				return fShortcut.evalEnablementExpression(context, expr);
			} catch (CoreException e) {
				return false;
			}
		}
		
		/**
		 * Runs with either the active editor or workbench selection.
		 *
		 * @see IAction#run()
		 */
		public void run() {
			
			IWorkbenchWindow wb = UIAccess.getActiveWorkbenchWindow(true);
			if (wb != null) {
				IWorkbenchPage page = wb.getActivePage();
				if (page != null) {
					ISelection selection = page.getSelection();
					if (selection instanceof IStructuredSelection) {
						fShortcut.launch(selection, fMode);
					} else {
						IEditorPart editor = page.getActiveEditor();
						if (editor != null) {
							fShortcut.launch(editor, fMode);
						}
					}
				}
			}
			updateLastLaunch(this);
		}

	}
	
	
	private String fMode = "run"; //$NON-NLS-1$
	private LaunchShortcutAction[] fActions;
	
	private IAction fButtonAction;
	private LaunchShortcutAction fLastAction;
	
	
	public void init(IWorkbenchWindow window) {
		
		loadShortcuts();
	}
	
	public void initAction(IAction action) {
		
		fButtonAction = action;
		
		String lastLaunchId = StatetPlugin.getDefault().getDialogSettings().get(LAST_LAUNCH_REMEMBER_KEY);
		if (lastLaunchId != null) {
			for (LaunchShortcutAction item : fActions) {
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
	
	public void selectionChanged(IAction action, ISelection selection) {
		
		if (fButtonAction == null)
			initAction(action);
	}

	public void dispose() {
		
		fButtonAction = null;
		fActions = null;
	}

	
	public Menu getMenu(Control parent) {
		
		Menu menu = new Menu(parent);
		fillMenu(menu);
		updateEnablement();
		return menu;
	}
	
	public void run(IAction action) {
		
		updateEnablement();
		
		LaunchShortcutAction lastLaunched = getLastLaunch();
		if (lastLaunched != null && lastLaunched.isEnabled())
			lastLaunched.run();
	}
	
	
	private void loadShortcuts() {
		
		@SuppressWarnings("unchecked")
		List<LaunchShortcutExtension> list = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchShortcuts("de.walware.statet.r.basic"); //$NON-NLS-1$
		List<LaunchShortcutAction> actions = new ArrayList<LaunchShortcutAction>(list.size());
		
		for (LaunchShortcutExtension ext : list) {
			if (ext.getModes().contains(fMode))
				actions.add(new LaunchShortcutAction(fMode, ext));
		}
		fActions = actions.toArray(new LaunchShortcutAction[actions.size()]);
	}

	private void fillMenu(Menu menu) {
		
		for (LaunchShortcutAction shortcut : fActions) {
			ActionContributionItem item = new ActionContributionItem(shortcut);
			item.fill(menu, -1);
		}
	}
	
	
	protected void updateLastLaunch(LaunchShortcutAction action) {
		
		if (fLastAction != action) {
			fLastAction = action;
			updateTooltip();
			
			StatetPlugin.getDefault().getDialogSettings().put(LAST_LAUNCH_REMEMBER_KEY, action.getId());
		}
	}
	
	protected LaunchShortcutAction getLastLaunch() {
		
		return fLastAction;
	}

	/**
	 * Updates this action's tooltip to correspond to the most recent launch.
	 */
	protected void updateTooltip() {
		
		LaunchShortcutAction lastLaunched = getLastLaunch();
		fButtonAction.setToolTipText(lastLaunched.getText());
		fButtonAction.setImageDescriptor(lastLaunched.getImageDescriptor());
	}

	protected void updateEnablement() {
		
		IEvaluationContext context = createContext();
		
		for (LaunchShortcutAction action : fActions) {
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
		IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
			    IWorkbenchPart activePart = page.getActivePart();
			    if (activePart instanceof IEditorPart) {
			        list = new ArrayList();
			        list.add(((IEditorPart)activePart).getEditorInput());
			    } else if (activePart != null) {
			        IWorkbenchPartSite site = activePart.getSite();
			        if (site != null) {
	                    ISelectionProvider selectionProvider = site.getSelectionProvider();
	                    if (selectionProvider != null) {
	                        ISelection selection = selectionProvider.getSelection();
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
		IEvaluationContext context = new EvaluationContext(null, list);
		context.addVariable("selection", list); //$NON-NLS-1$
		
		return context;
	}

}
