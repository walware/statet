/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.breadcrumb.AbstractBreadcrumb;
import de.walware.ecommons.ui.breadcrumb.BreadcrumbViewer;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPackageHelp;


public class RHelpBreadcrumb extends AbstractBreadcrumb {
	
	
	private class RHelpBreadcrumbViewer extends BreadcrumbViewer {
		
		
		public RHelpBreadcrumbViewer(final Composite parent) {
			super(parent, SWT.HORIZONTAL);
		}
		
		
		@Override
		protected void configureDropDownViewer(final TreeViewer viewer, final Object input) {
			viewer.setContentProvider(new RHelpContentProvider());
			viewer.setLabelProvider(new RHelpLabelProvider());
		}
		
		@Override
		public void fillDropDownContextMenu(final IMenuManager manager, final Object selection) {
			if (selection instanceof IREnv) {
				final IREnv rEnv = (IREnv) selection;
				manager.add(new SimpleContributionItem(
						NLS.bind("Show overview of ''{0}''", rEnv.getName()), "o") {
					@Override
					protected void execute() throws ExecutionException {
						RHelpBreadcrumb.this.reveal(selection);
						setFocusToInput();
					}
				});
				manager.add(new SimpleContributionItem(
						NLS.bind("Show current page in ''{0}''", rEnv.getName()), "p") {
					@Override
					protected void execute() throws ExecutionException {
						switchTo((IREnv) selection);
					}
				});
			}
		}
		
	}
	
	private class RHelpContentProvider implements ITreeContentProvider {
		
		
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public Object[] getElements(final Object inputElement) {
			return getChildren(inputElement);
		}
		
		@Override
		public Object getParent(final Object element) {
			if (element instanceof IREnv || element instanceof String) {
				return fHelpManager;
			}
			if (element instanceof IRPackageHelp) {
				return ((IRPackageHelp) element).getREnv();
			}
			if (element instanceof IRHelpPage) {
				return ((IRHelpPage) element).getPackage();
			}
			if (element instanceof Object[]) {
				return ((Object[]) element)[0];
			}
			return null;
		}
		
		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof IRHelpManager) {
				return true;
			}
			if (element instanceof IREnv) {
				return fHelpManager.hasHelp((IREnv) element);
			}
			if (element instanceof IRPackageHelp) {
				return !((IRPackageHelp) element).getHelpPages().isEmpty();
			}
			return false;
		}
		
		@Override
		public Object[] getChildren(final Object element) {
			if (element instanceof IRHelpManager) {
				return fHelpManager.getREnvWithHelp().toArray();
			}
			if (element instanceof IREnv) {
				final IREnvHelp help = fHelpManager.getHelp((IREnv) element);
				if (help != null) {
					try {
						return help.getRPackages().toArray();
					}
					finally {
						help.unlock();
					}
				}
				return new Object[0];
			}
			if (element instanceof IRPackageHelp) {
				return ((IRPackageHelp) element).getHelpPages().toArray();
			}
			return new Object[0];
		}
		
	}
	
	
	private final IRHelpManager fHelpManager = RCore.getRHelpManager();
	
	private final RHelpViewPage fPage;
	
	private RHelpLabelProvider fLabelProvider;
	
	
	public RHelpBreadcrumb(final RHelpViewPage page) {
		fPage = page;
	}
	
	
	@Override
	public void setInput(Object element) {
		if (element == null) {
			element = fHelpManager;
		}
		if (fLabelProvider != null) {
//			fLabelProvider.setFocusObject(element);
		}
		super.setInput(element);
	}
	
	@Override
	protected BreadcrumbViewer createViewer(final Composite parent) {
		final RHelpBreadcrumbViewer viewer = new RHelpBreadcrumbViewer(parent);
		
		fLabelProvider = new RHelpLabelProvider(0);
		viewer.setLabelProvider(fLabelProvider);
		viewer.setToolTipLabelProvider(new RHelpLabelProvider(RHelpLabelProvider.TOOLTIP));
		
		viewer.setContentProvider(new RHelpContentProvider());
		
		return viewer;
	}
	
	@Override
	protected boolean hasInputFocus() {
		return fPage.isBrowserFocusControl();
	}
	
	@Override
	protected void setFocusToInput() {
		fPage.setFocusToBrowser();
	}
	
	@Override
	protected IServiceLocator getParentServiceLocator() {
		return fPage.getSite();
	}
	
	@Override
	protected boolean reveal(final Object element) {
		final String url = fHelpManager.toHttpUrl(element, RHelpUIServlet.BROWSE_TARGET);
		if (url != null) {
			fPage.setUrl(url);
			return true;
		}
		return false;
	}
	
	protected void switchTo(final IREnv rEnv) {
		final String url = fHelpManager.toHttpUrl(fPage.getCurrentUrl(), rEnv, RHelpUIServlet.BROWSE_TARGET);
		if (url != null) {
			setFocusToInput();
			fPage.setUrl(url);
		}
	}
	
	@Override
	protected boolean open(final Object element) {
		return false;
	}
	
}
