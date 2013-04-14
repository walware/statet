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

import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.TableWrapData;

import de.walware.ecommons.emf.core.util.IEMFEditContext;


public abstract class MainDetail extends Detail {
	
	
	private final List<EFFormSection> fSections = new ArrayList<EFFormSection>();
	
	
	public MainDetail(final DetailStack parent) {
		super(parent);
	}
	
	
	protected void registerSection(final EFFormSection section) {
		fSections.add(section);
	}
	
	
	@Override
	protected Layout createContentLayout() {
		return EFLayoutUtil.createMainTableLayout(1);
	}
	
	protected Object createSectionLayoutData() {
		return new TableWrapData(TableWrapData.FILL_GRAB);
	}
	
	@Override
	public void addBindings(final IEMFEditContext context) {
		for (final EFFormSection section : fSections) {
			section.addBindings(context);
		}
	}
	
	
}
