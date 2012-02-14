/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.wizards;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.databinding.DirtyTracker;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.rlang.RPkgNameValidator;


public class RPkgProjectWizardPage extends WizardPage {
	
	
	private NewRProjectWizardPage fProjectPage;
	
	private Text fPkgNameControl;
	
	private DataBindingContext fDbc;
	private WritableValue fPkgNameValue;
	
	private boolean fWasVisible;
	private DirtyTracker fPkgUserChanged;
	
	
	public RPkgProjectWizardPage(final NewRProjectWizardPage projectPage) {
		super("RPkgWizardPage"); //$NON-NLS-1$
		
		setTitle(Messages.RPkgWizardPage_title);
		setDescription(Messages.RPkgWizardPage_description);
		
		fProjectPage = projectPage;
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyContentDefaults(new GridLayout(), 1));
		
		{	final Composite group = createRPkgGroup(composite);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		Dialog.applyDialogFont(composite);
		setControl(composite);
		
		final Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);
		addBindings(fDbc, realm);
		WizardPageSupport.create(this, fDbc);
	}
	
	protected Composite createRPkgGroup(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		composite.setText("R Package");
		
		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText("Package Name: ");
		
		fPkgNameControl = new Text(composite, SWT.LEFT | SWT.BORDER);
		fPkgNameControl.setFont(JFaceResources.getTextFont());
		fPkgNameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		return composite;
	}
	
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fPkgNameValue = new WritableValue(realm, "", String.class);
		
		fPkgUserChanged = new DirtyTracker();
		
		final Binding binding = dbc.bindValue(
				SWTObservables.observeText(fPkgNameControl, SWT.Modify),
				fPkgNameValue,
				new UpdateValueStrategy().setAfterGetValidator(new RPkgNameValidator()), null );
		fPkgUserChanged.add(binding);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			final boolean firstTime = !fWasVisible;
			fWasVisible = true;
			
			if (!fPkgUserChanged.isDirty() && fProjectPage != null) {
				fPkgNameControl.setText(fProjectPage.getProjectName());
				fPkgUserChanged.resetDirty();
			}
			if (firstTime) {
				setMessage(null);
				setErrorMessage(null);
			}
		}
	}
	
	
	public String getPkgName() {
		return (String) fPkgNameValue.getValue();
	}
	
}
