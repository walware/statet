/*******************************************************************************
 * Copyright (c) 2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import de.walware.ecommons.databinding.DataBindingSubContext;
import de.walware.ecommons.emf.core.util.IEMFEditContext;


/**
 * An abstract implementation of a section in a tab in the tabbed property sheet
 * page for EF.
 */
public abstract class EFPropertySection extends AbstractPropertySection
		implements IEFFormPage {
	
	
	private EFPropertySheetPage fPage;
	
	private EFEditor fEditor;
	
	private Composite fComposite;
	
	private DataBindingSubContext fSubContext;
	
	
	protected EFPropertySection() {
	}
	
	
	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		if (!(aTabbedPropertySheetPage instanceof EFPropertySheetPage)) {
			return;
		}
		
		fPage = (EFPropertySheetPage) aTabbedPropertySheetPage;
		fEditor = fPage.getEditor();
		
		fComposite = getToolkit().createComposite(parent);
		fComposite.setLayout(createContentLayout());
		
		createContent(fComposite);
		
		fSubContext = new DataBindingSubContext(fEditor.getDataBinding().getContext());
		fSubContext.run(new Runnable() {
			@Override
			public void run() {
				initBindings();
			}
		});
	}
	
	protected Layout createContentLayout() {
//		return EFLayoutUtil.createPropertiesColumnLayout();
		return EFLayoutUtil.createPropertiesTableLayout(1);
	}
	
	protected abstract void createContent(Composite parent);
	
	protected IEMFEditContext getRootContext() {
		return fEditor.getDataBinding();
	}
	
	protected void initBindings() {
	}
	
	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
	}
	
	protected EObject adapt(final Object object) {
		if (object instanceof IAdaptable) {
			return (EObject) ((IAdaptable) object).getAdapter(EObject.class);
		}
		return null;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if (fSubContext != null) {
			fSubContext.dispose();
			fSubContext = null;
		}
		fEditor = null;
		fPage = null;
	}
	
	
	@Override
	public EFEditor getEditor() {
		return fEditor;
	}
	
	@Override
	public EFToolkit getToolkit() {
		return fEditor.getToolkit();
	}
	
	@Override
	public void reflow(final boolean flushCache) {
	}
	
}
