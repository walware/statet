/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.sweave;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceStructElement;

import de.walware.docmlet.tex.core.model.IEmbeddedForeignElement;
import de.walware.docmlet.tex.core.model.ILtxSourceElement;
import de.walware.docmlet.tex.ui.TexLabelProvider;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.ui.RLabelProvider;


public class TexRweaveLabelProvider extends TexLabelProvider {
	
	
	private final RLabelProvider fRProvider;
	
	
	public TexRweaveLabelProvider(final int rStyle) {
		fRProvider = new RLabelProvider(rStyle);
	}
	
	
	@Override
	public Image getImage(final IModelElement element) {
		if (element.getModelTypeId() == RModel.TYPE_ID) {
			return fRProvider.getImage(element);
		}
		if (element.getElementType() == ILtxSourceElement.C1_EMBEDDED) {
			return SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_OBJ_RCHUNK);
		}
		return super.getImage(element);
	}
	
	@Override
	public String getText(final IModelElement element) {
		if (element.getModelTypeId() == RModel.TYPE_ID) {
			return fRProvider.getText(element);
		}
		if (element.getElementType() == ILtxSourceElement.C1_EMBEDDED) {
			final ISourceStructElement rElement = ((IEmbeddedForeignElement) element).getForeignElement();
			if (rElement != null) {
				return fRProvider.getText(rElement);
			}
		}
		return super.getText(element);
	}
	
	@Override
	public StyledString getStyledText(final IModelElement element) {
		if (element.getModelTypeId() == RModel.TYPE_ID) {
			return fRProvider.getStyledText(element);
		}
		if (element.getElementType() == ILtxSourceElement.C1_EMBEDDED) {
			final ISourceStructElement rElement = ((IEmbeddedForeignElement) element).getForeignElement();
			if (rElement != null) {
				return fRProvider.getStyledText(rElement);
			}
		}
		return super.getStyledText(element);
	}
	
}
