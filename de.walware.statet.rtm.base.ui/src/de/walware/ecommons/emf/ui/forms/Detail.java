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

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.databinding.IDataBindingPart;


public abstract class Detail implements IDataBindingPart {
	
	
	private final DetailStack fParent;
	
	private final Composite fComposite;
	
	
	public Detail(final DetailStack parent) {
		fParent = parent;
		
		fComposite = createComposite(fParent);
	}
	
	
	protected Composite createComposite(final DetailStack parent) {
		final Composite composite = parent.getPage().getToolkit().createComposite(parent);
		return composite;
	}
	
	
	public IEFFormPage getPage() {
		return fParent.getPage();
	}
	
	protected Composite getComposite() {
		return fComposite;
	}
	
	protected void createContent() {
		fComposite.setLayout(createContentLayout());
		createContent(fComposite);
	}
	
	protected Layout createContentLayout() {
		return EFLayoutUtil.createCompositePropGridLayout();
	}
	
	protected abstract void createContent(Composite composite);
	
	@Override
	public void addBindings(final IEMFEditContext context) {
	}
	
}
