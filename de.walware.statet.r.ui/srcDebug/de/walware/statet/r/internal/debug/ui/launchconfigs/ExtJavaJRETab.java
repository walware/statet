/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.debug.InputArgumentsComposite;


/**
 * Adds:
 *   <li>Optional requirement/validation of JRE</li>
 *   <li>VM Arguments</li>
 */
class ExtJavaJRETab extends JavaJRETab {
	
	
	private final RConsoleMainTab fMain;
	
	private InputArgumentsComposite fVmArgsControl;
	
	
	public ExtJavaJRETab(final RConsoleMainTab mainTab) {
		fMain = mainTab;
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		
		final Composite tabHolder = getDynamicTabHolder();
		final Composite composite = tabHolder.getParent();
		final GridLayout layout = (GridLayout) composite.getLayout();
		
		tabHolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, layout.numColumns, 1));
		
		final Composite extComposite = new Composite(composite, SWT.NONE);
		final GridLayout extLayout = new GridLayout();
		extLayout.marginHeight = 0;
		extComposite.setLayout(extLayout);
		extComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, layout.numColumns, 1));
		final Group group = new Group(extComposite, SWT.NONE);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		fVmArgsControl = new InputArgumentsComposite(group, "VM &Arguments:");
		fVmArgsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fVmArgsControl.getTextControl().addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		final Label note = new Label(group, SWT.WRAP);
		note.setText(fVmArgsControl.getNoteText());
		note.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	}
	
	@Override
	protected void loadDynamicJREArea() {
		super.loadDynamicJREArea();
		
		final Composite tabHolder = getDynamicTabHolder();
		tabHolder.getParent().layout(new Control[] { tabHolder });
	}
	
	
	@Override
	public boolean isValid(final ILaunchConfiguration config) {
		final RConsoleType type = fMain.getSelectedType();
		if (type != null && type.requireJRE()) {
			return super.isValid(config);
		}
		return true;
	}
	
	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		
		String vmArgs = null;
		try {
			vmArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		}
		catch (final CoreException e) {
		}
		fVmArgsControl.getTextControl().setText(vmArgs != null ? vmArgs : ""); //$NON-NLS-1$
	}
	
	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		
		final String vmArgs = fVmArgsControl.getTextControl().getText();
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
				(vmArgs.length() > 0) ? vmArgs : (String) null);
	}
	
}
