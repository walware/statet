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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.databinding.IDataBindingPart;


public abstract class EFFormSection extends SectionPart
		implements IAdaptable, IDataBindingPart {
	
	
	public final static int TITLE_STYLE = ExpandableComposite.TITLE_BAR;
	public final static int TITLE_DESCRIPTION_STYLE = TITLE_STYLE | Section.DESCRIPTION;
	public final static int EXPANDABLE_STYLE = ExpandableComposite.TWISTIE;
	
	
	private final IEFFormPage fPage;
	
	
	public EFFormSection(final IEFFormPage page, final Composite parent,
			final String title, final String description) {
		this(page, parent, (description != null) ? TITLE_DESCRIPTION_STYLE : TITLE_STYLE);
		
		final Section section = getSection();
		section.setText(title);
		if (description != null) {
			section.setDescription(description);
		}
	}
	
	public EFFormSection(final IEFFormPage page, final Composite parent, final int style) {
		super(parent, page.getToolkit(), style);
		fPage = page;
//		initialize(page.getManagedForm());
		
		final Section section = getSection();
		section.clientVerticalSpacing = EFLayoutUtil.SECTION_HEADER_V_SPACING;
		section.setData("part", this); //$NON-NLS-1$
	}
	
	
	public IEFFormPage getPage() {
		return fPage;
	}
	
	protected void createClient() {
		final Section section = getSection();
		final EFToolkit toolkit = fPage.getToolkit();
		
		final Composite composite = toolkit.createComposite(section);
		composite.setLayout(createClientLayout());
		section.setClient(composite);
		
		createContent(composite);
	}
	
	protected Layout createClientLayout() {
		final GridLayout layout = EFLayoutUtil.createSectionPropGridLayout();
//		TableWrapLayout layout = FormLayoutUtil.createSectionClientTableWrapLayout(3);
		return layout;
	}
	
	protected abstract void createContent(Composite composite);
	
	@Override
	public void addBindings(final IEMFEditContext context) {
	}
	
	
	@Override
	protected void expansionStateChanged(final boolean expanded) {
		getPage().reflow(false);
	}
	
	@Override
	public Object getAdapter(final Class adapter) {
		return null;
	}
	
}
