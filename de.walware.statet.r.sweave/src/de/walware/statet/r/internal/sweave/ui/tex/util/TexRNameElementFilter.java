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

package de.walware.statet.r.internal.sweave.ui.tex.util;

import org.eclipse.ui.dialogs.SearchPattern;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.models.core.util.IElementProxy;
import de.walware.ecommons.ui.content.ITextElementFilter;
import de.walware.ecommons.ui.content.MultiTextElementFilter;
import de.walware.ecommons.ui.content.TextElementFilter;

import de.walware.docmlet.tex.ui.util.TexNameSearchPattern;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.ui.util.RNameSearchPattern;


public class TexRNameElementFilter extends MultiTextElementFilter {
	
	
	public TexRNameElementFilter() {
		super(new ITextElementFilter[] {
				new TextElementFilter() {
					@Override
					protected SearchPattern createSearchPattern() {
						return new TexNameSearchPattern();
					}
				},
				new TextElementFilter() {
					@Override
					protected SearchPattern createSearchPattern() {
						return new RNameSearchPattern();
					}
				}
		});
	}
	
	
	@Override
	protected int getIdx(Object element) {
		if (element instanceof IElementProxy) {
			element= ((IElementProxy) element).getElement();
			if (element instanceof IModelElement) {
				if (((IModelElement) element).getModelTypeId() == RModel.R_TYPE_ID) {
					return 1;
				}
			}
		}
		return 0;
	}
	
}
