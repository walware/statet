/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.debug.ui.InputArgumentsComposite;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier.ChangeListener;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Adds:
 *   <li>Optional requirement/validation of JRE</li>
 *   <li>VM Arguments</li>
 */
class ExtJavaJRETab extends JavaJRETab implements ChangeListener {
	
	
	private final RConsoleMainTab fMainTab;
	private final REnvTab fREnvTab;
	
	private InputArgumentsComposite fVmArgsControl;
	
	private IVMInstall fLastCheckedVM;
	private int fLastCheckedVMBits;
	private int fLastCheckedRBits;
	
	
	public ExtJavaJRETab(final RConsoleMainTab mainTab, final REnvTab renvTab) {
		fMainTab = mainTab;
		fREnvTab = renvTab;
		
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(this);
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
		
		fVmArgsControl = new InputArgumentsComposite(group, RLaunchingMessages.JavaJRE_Tab_VMArguments_label);
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
	
	public void settingsChanged(final Set<String> groupIds) {
		if (groupIds.contains(IREnvManager.SETTINGS_GROUP_ID)) {
			UIAccess.getDisplay().syncExec(new Runnable() {
				public void run() {
					final int previous = fLastCheckedRBits;
					updateRBits();
					if (previous != fLastCheckedRBits) {
						getLaunchConfigurationDialog().updateMessage();
						getLaunchConfigurationDialog().updateButtons();
					}
				}
			});
		}
	}
	
	@Override
	public void dispose() {
		PreferencesUtil.getSettingsChangeNotifier().removeChangeListener(this);
		
		super.dispose();
	}
	
	
	@Override
	public boolean isValid(final ILaunchConfiguration config) {
		final RConsoleType type = fMainTab.getSelectedType();
		if (type == null || !type.requireJRE()) {
			setErrorMessage(null);
			setMessage(null);
			return true;
		}
		if (!super.isValid(config)) {
			return false;
		}
		// try to check compatibility
		
		updateRBits();
		if (fLastCheckedRBits == -1) {
			// check not necessary
			return true;
		}
		
		IVMInstall jre = null;
		try {
			jre = JavaRuntime.computeVMInstall(config);
		}
		catch (final CoreException e) {
		}
		if (jre instanceof VMStandin) {
			jre = ((VMStandin) jre).convertToRealVM();
		}
		if (jre != null && fLastCheckedVM != jre) {
			fLastCheckedVM = jre;
			updateVMBits();
		}
		if (jre == null || fLastCheckedVMBits == -1) {
			// check failed technically
			return true;
		}
		if (fLastCheckedVMBits == fLastCheckedRBits) {
			return true;
		}
		else {
			setErrorMessage(NLS.bind(RLaunchingMessages.JavaJRE_RCompatibility_error_DifferentBits_message, fLastCheckedRBits, fLastCheckedVMBits));
			return false;
		}
	}
	
	private void updateRBits() {
		final REnvConfiguration renv = fREnvTab.getSelectedEnv();
		fLastCheckedRBits = (renv != null) ? renv.getRBits() : -1;
	}
	
	private void updateVMBits() {
		fLastCheckedVMBits = -1;
		if (fLastCheckedVM instanceof IVMInstall3) {
			try {
				getLaunchConfigurationDialog().run(true, true, new IRunnableWithProgress() {
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						final String[] propertyNames = new String[] { "java.vm.name", "sun.arch.data.model", "com.ibm.vm.bitmode" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						try {
							final Map<String, String> properties = ((IVMInstall3) fLastCheckedVM).evaluateSystemProperties(propertyNames, monitor);
							
							{	// proprietary property for bits
								String p = properties.get("sun.arch.data.model"); //$NON-NLS-1$
								if (p == null || p.length() == 0) {
									p = properties.get("com.ibm.vm.bitmode"); //$NON-NLS-1$
								}
								if (p != null && p.length() > 0) {
									try {
										fLastCheckedVMBits = Integer.parseInt(p);
										return;
									}
									catch (final NumberFormatException e) {
									}
								}
							}
							{	// search in vm name
								String p = properties.get("java.vm.name"); //$NON-NLS-1$
								if (p != null && p.length() > 0) {
									p = p.toLowerCase();
									if (p.contains("64-bit")) { //$NON-NLS-1$
										fLastCheckedVMBits = 64;
										return;
									}
									if (p.contains("32-bit")) { //$NON-NLS-1$
										fLastCheckedVMBits = 32;
										return;
									}
								}
							}
						}
						catch (final CoreException e) {
							throw new InvocationTargetException(e);
						}
					}
				});
			}
			catch (final InvocationTargetException e) {
				RUIPlugin.logError(-1, "Error when trying to fetch VM properties for JRE validation.", e.getTargetException());  //$NON-NLS-1$
			}
			catch (final InterruptedException e) {
			}
		}
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
