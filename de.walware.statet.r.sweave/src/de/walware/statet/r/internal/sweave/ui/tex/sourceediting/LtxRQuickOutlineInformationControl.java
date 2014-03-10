/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.ui.tex.sourceediting;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelElement.Filter;
import de.walware.ecommons.ltk.ISourceElement;
import de.walware.ecommons.ltk.ui.sourceediting.OutlineContentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.QuickOutlineInformationControl;
import de.walware.ecommons.ltk.ui.sourceediting.actions.OpenDeclaration;
import de.walware.ecommons.ui.content.ITextElementFilter;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.sweave.ui.tex.util.TexRNameElementFilter;
import de.walware.statet.r.sweave.Sweave;
import de.walware.statet.r.sweave.TexRweaveLabelProvider;


class LtxRQuickOutlineInformationControl extends QuickOutlineInformationControl {
	
	
	private class ContentFilter implements IModelElement.Filter {
		
		@Override
		public boolean include(final IModelElement element) {
			if (element.getModelTypeId() == RModel.R_TYPE_ID) {
				switch (element.getElementType()) {
				case IRElement.R_ARGUMENT:
					return false;
				default:
					return true;
				}
			}
			return true;
		};
		
	}
	
	
	private final ContentFilter contentFilter= new ContentFilter();
	
	
	public LtxRQuickOutlineInformationControl(final Shell parent, final String modelType,
			final String commandId) {
		super(parent, commandId, 2, new OpenDeclaration());
	}
	
	
	@Override
	public String getModelTypeId() {
		if (getIterationPosition() == 1) {
			return RModel.R_TYPE_ID;
		}
		return Sweave.LTX_R_MODEL_TYPE_ID;
	}
	
	@Override
	protected int getInitialIterationPage(final ISourceElement element) {
//		if (element.getModelTypeId() == RModel.R_TYPE_ID) {
//			return 1;
//		}
		return 0;
	}
	
	@Override
	protected String getDescription(final int iterationPage) {
		if (iterationPage == 1) {
			return "R Outline";
		}
		return super.getDescription(iterationPage);
	}
	
	@Override
	protected OutlineContentProvider createContentProvider() {
		return new LtxROutlineContentProvider(new OutlineContent());
	}
	
	@Override
	protected Filter getContentFilter() {
		return this.contentFilter;
	}
	
	@Override
	protected ITextElementFilter createNameFilter() {
		return new TexRNameElementFilter();
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		super.configureViewer(viewer);
		
		viewer.setLabelProvider(new TexRweaveLabelProvider(0));
	}
	
}
