/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.ui.actions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.ui.IToolRunnableDecorator;

import de.walware.statet.rtm.base.internal.ui.actions.Messages;
import de.walware.statet.rtm.base.ui.RTaskSnippet;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.tool.AbstractStatetRRunnable;
import de.walware.statet.r.core.tool.IRConsoleService;
import de.walware.statet.r.ui.pkgmanager.RPkgManagerUI;


public class RTaskRunnable extends AbstractStatetRRunnable implements IToolRunnableDecorator {
	
	
	private static final int PACKAGE_CHECK = 1;
	
	protected static String createTypeId(final RTaskSnippet snippet) {
		return "r/rtask/" + snippet.getDescriptor().getTaskId(); //$NON-NLS-1$
	}
	
	protected static String createLabel(final RTaskSnippet snippet) {
		return snippet.getDescriptor().getName();
	}
	
	
	private final RTaskSnippet fSnippet;
	private final IWorkbenchPage fPage;
	
	private int fState;
	
	
	public RTaskRunnable(final RTaskSnippet snippet, final IWorkbenchPage page) {
		this(createTypeId(snippet), snippet.getLabel(), snippet, page);
	}
	
	public RTaskRunnable(final String typeId, final String label, final RTaskSnippet snippet,
			final IWorkbenchPage page) {
		super(typeId, label);
		
		fSnippet = snippet;
		fPage = page;
	}
	
	
	@Override
	public Image getImage() {
		return fSnippet.getDescriptor().getImage();
	}
	
	@Override
	protected void run(final IRConsoleService r, final IProgressMonitor monitor) throws CoreException {
		checkNewCommand(r, monitor);
		if (!checkPackages(r, monitor)) {
			return;
		}
		
		r.submitToConsole(fSnippet.getRCode(), monitor);
	}
	
	
	protected boolean checkPackages(final IRConsoleService r,
			final IProgressMonitor monitor) throws CoreException {
		final IREnv rEnv = (IREnv) r.getTool().getAdapter(IREnv.class);
		if (rEnv == null) {
			return true;
		}
		final IRPkgManager rPkgMgr = RCore.getRPkgManager(rEnv);
		if (rPkgMgr == null) {
			return true;
		}
		final List<String> requiredPkgs = fSnippet.getRequiredPkgs();
		if (RPkgUtil.areInstalled(rPkgMgr, requiredPkgs)) {
			return true;
		}
		if (fState == PACKAGE_CHECK) {
			return false; // still missing
		}
		fState = PACKAGE_CHECK;
		final ITool tool = r.getTool();
		return RPkgManagerUI.requestRequiredRPkgs((IRPkgManager.Ext) rPkgMgr, requiredPkgs,
				r, monitor, fPage.getWorkbenchWindow(),
				NLS.bind(Messages.RunTask_RequirePkgs_message, fSnippet.getDescriptor().getName()),
				new Runnable() {
					@Override
					public void run() {
						tool.getQueue().add(RTaskRunnable.this);
					}
				}, null);
	}
	
}
