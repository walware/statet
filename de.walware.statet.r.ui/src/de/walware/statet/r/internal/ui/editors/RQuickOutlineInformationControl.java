/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SearchPattern;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelElement.Filter;
import de.walware.ecommons.ltk.ui.sourceediting.QuickOutlineInformationControl;
import de.walware.ecommons.ui.content.TextFilterProvider;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.internal.ui.RNameSearchPattern;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.sourceediting.ROpenDeclaration;


public class RQuickOutlineInformationControl extends QuickOutlineInformationControl {
	
	
	private class ContentFilter implements IModelElement.Filter {
		
		@Override
		public boolean include(final IModelElement element) {
			switch (element.getElementType()) {
			case IRElement.R_ARGUMENT:
				return false;
			default:
				return true;
			}
		};
		
	}
	
	
	private final ContentFilter contentFilter = new ContentFilter();
	
	
	public RQuickOutlineInformationControl(final Shell parent, final String modelType,
			final String commandId) {
		super(parent, modelType, commandId, new ROpenDeclaration());
	}
	
	
	@Override
	protected Filter getContentFilter() {
		return this.contentFilter;
	}
	
	@Override
	protected TextFilterProvider createNameFilter() {
		return new TextFilterProvider() {
			@Override
			protected SearchPattern createSearchPattern() {
				return new RNameSearchPattern();
			}
		};
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		super.configureViewer(viewer);
		
		viewer.setLabelProvider(new RLabelProvider());
	}
	
}
