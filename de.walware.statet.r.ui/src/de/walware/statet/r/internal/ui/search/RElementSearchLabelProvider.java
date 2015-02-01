/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.search;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.workbench.search.ui.LineElement;
import de.walware.ecommons.workbench.search.ui.TextSearchLabelUtil;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.ui.RLabelProvider;


public class RElementSearchLabelProvider extends RLabelProvider implements IStyledLabelProvider {
	
	
	private final RElementSearchResultPage page;
	
	private final TextSearchLabelUtil util;
	private final WorkbenchLabelProvider workbenchLabelProvider;
	
	
	public RElementSearchLabelProvider(final RElementSearchResultPage page, final int style) {
		super(style);
		this.page= page;
		this.util= new TextSearchLabelUtil();
		this.workbenchLabelProvider= new WorkbenchLabelProvider();
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		this.workbenchLabelProvider.dispose();
	}
	
	@Override
	public void addListener(final ILabelProviderListener listener) {
		super.addListener(listener);
		this.workbenchLabelProvider.addListener(listener);
	}
	
	@Override
	public void removeListener(final ILabelProviderListener listener) {
		super.removeListener(listener);
		this.workbenchLabelProvider.removeListener(listener);
	}
	
	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		if (element instanceof IModelElement) {
			return super.isLabelProperty(element, property);
		}
		if (element instanceof RElementMatch || element instanceof LineElement<?>) {
			return true;
		}
		return this.workbenchLabelProvider.isLabelProperty(element, property);
	}
	
	
	@Override
	public Image getImage(final Object element) {
		if (element instanceof IModelElement) {
			if (element instanceof ISourceUnit) {
				return this.workbenchLabelProvider.getImage(((ISourceUnit) element).getResource());
			}
			return super.getImage((IModelElement) element);
		}
		if (element instanceof RElementMatch || element instanceof LineElement<?>) {
			return SharedUIResources.getImages().get(SharedUIResources.OBJ_LINE_MATCH_IMAGE_ID);
		}
		return this.workbenchLabelProvider.getImage(element);
	}
	
	@Override
	public StyledString getStyledText(final Object element) {
		if (element instanceof IModelElement) {
			final StyledString text= super.getStyledText((IModelElement) element);
			if (element instanceof IRSourceUnit) {
				final int count= this.page.getDisplayedMatchCount(element);
				final StringBuilder sb= getTextBuilder();
				sb.append(" ("); //$NON-NLS-1$
				sb.append(count);
				sb.append(' ');
				sb.append((count == 1) ? Messages.Search_Match_sing_label : Messages.Search_Match_plural_label);
				sb.append(')');
				text.append(sb.toString(), StyledString.COUNTER_STYLER);
			}
			return text;
		}
		if (element instanceof LineElement<?>) {
			final LineElement<?> lineElement= (LineElement<?>) element;
			return this.util.getStyledText(lineElement, this.page.getDisplayedMatches(lineElement));
		}
		return this.workbenchLabelProvider.getStyledText(element);
	}
	
}
