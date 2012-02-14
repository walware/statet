/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

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

import de.walware.ecommons.databinding.jface.DatabindingSupport;
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
		super(parentShell, projects);
		
		fProjects = projects;
	}
	
	
	@Override
	protected Control createContents(final Composite parent) {
		final Control control = super.createContents(parent);
		initBindings();
		return control;
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		dialogArea.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 1));
		
		final Composite checkboxComposite = createCheckboxComposite(dialogArea, "&R projects:");
		checkboxComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		return dialogArea;
	}
	
	@Override
	protected void configureViewer(final CheckboxTableViewer viewer) {
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		viewer.setInput(fProjects);
	}
	
	@Override
	protected void addBindings(final DatabindingSupport databinding) {
		super.addBindings(databinding);
		
		final MultiValidator notEmptyValidator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				if (getCheckedElements().isEmpty()) {
					return ValidationStatus.error("Please select at least one project.");
				}
				return ValidationStatus.ok();
			}
		};
		databinding.getContext().addValidationStatusProvider(notEmptyValidator);
	}
	
}
