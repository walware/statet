/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui;

import java.util.List;

import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.dialogs.AbstractCheckboxSelectionDialog;
import de.walware.ecommons.ui.util.LayoutUtil;


/**
 * A dialog for selecting projects to add to a classpath or source
 * lookup path. Optionally specifies whether
 * exported entries and required projects should also be added.
 */
public class ProjectSelectionDialog extends AbstractCheckboxSelectionDialog {
	
	
	private final List<IProject> fProjects;
	
	
	public ProjectSelectionDialog(final Shell parentShell, final List<IProject> projects){
		super(parentShell, WITH_DATABINDING_CONTEXT, projects);
		
		fProjects = projects;
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		area.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 1));
		
		final Composite checkboxComposite = createCheckboxComposite(area, "&R projects:");
		checkboxComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		applyDialogFont(area);
		
		return area;
	}
	
	@Override
	protected void configureViewer(final CheckboxTableViewer viewer) {
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		viewer.setInput(fProjects);
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		super.addBindings(db);
		
		final MultiValidator notEmptyValidator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				if (getCheckedElements().isEmpty()) {
					return ValidationStatus.error("Please select at least one project.");
				}
				return ValidationStatus.ok();
			}
		};
		db.getContext().addValidationStatusProvider(notEmptyValidator);
	}
	
}
