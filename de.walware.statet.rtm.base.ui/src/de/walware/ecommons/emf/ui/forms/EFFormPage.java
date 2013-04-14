/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


public abstract class EFFormPage extends FormPage
		implements IEFFormPage {
	
	
	private final List<EFFormSection> fSections = new ArrayList<EFFormSection>();
	
	private ISelectionProvider fSelectionProvider;
	
	
	public EFFormPage(final EFEditor editor, final String id, final String title) {
		super(editor, id, title);
	}
	
	
	@Override
	public EFEditor getEditor() {
		return (EFEditor) super.getEditor();
	}
	
	protected void registerSection(final EFFormSection section) {
		fSections.add(section);
	}
	
	
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		final FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());
		
		final IToolBarManager manager = form.getToolBarManager();
		getEditor().contributeToPages(manager);
		
		final IEFModelDescriptor descriptor = getEditor().getModelDescriptor();
		form.setImage(descriptor.getImage());
		form.setText(descriptor.getName());
		
		final Composite body = form.getBody();
		body.setLayout(createBodyLayout());
		
		createFormBodyContent(body);
		
		initBindings();
		
		form.updateToolBar();
		
		body.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getManagedForm().reflow(true);
			}
		});
	}
	
	@Override
	public EFToolkit getToolkit() {
		return (EFToolkit) getManagedForm().getToolkit();
	}
	
	protected void initBindings() {
		final EFDataBindingSupport databinding = getEditor().getDataBinding();
		for (final EFFormSection section : fSections) {
			section.addBindings(databinding);
		}
	}
	
	protected Layout createBodyLayout() {
		return EFLayoutUtil.createBodyTableLayout(1);
	}
	
	protected Composite addBodyComposite() {
		final IManagedForm managedForm = getManagedForm();
		final FormToolkit toolkit = managedForm.getToolkit();
		
		final Composite body = managedForm.getForm().getBody();
		final Composite composite = toolkit.createComposite(body);
		final TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB);
		layoutData.colspan = ((TableWrapLayout) body.getLayout()).numColumns;
		composite.setLayoutData(layoutData);
		
		return composite;
	}
	
	protected MasterDetailComposite addBodySashComposite() {
		final IManagedForm managedForm = getManagedForm();
		
		final Composite body = managedForm.getForm().getBody();
		final MasterDetailComposite composite = new MasterDetailComposite(body, managedForm);
		final TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB);
		layoutData.colspan = ((TableWrapLayout) body.getLayout()).numColumns;
		composite.setLayoutData(layoutData);
		
		managedForm.getToolkit().adapt(composite);
		
		return composite;
	}
	
	protected void setSelectionProvider(final ISelectionProvider selectionProvider) {
		fSelectionProvider = selectionProvider;
		getEditor().getSelectionProvider().update();
	}
	
	
	protected abstract void createFormBodyContent(Composite body);
	
	@Override
	public void reflow(final boolean flushCache) {
		getManagedForm().reflow(flushCache);
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(ISelectionProvider.class)) {
			return fSelectionProvider;
		}
		return super.getAdapter(required);
	}
	
}
