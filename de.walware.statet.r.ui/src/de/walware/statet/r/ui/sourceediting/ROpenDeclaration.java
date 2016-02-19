/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;

import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.actions.OpenDeclaration;

import de.walware.statet.r.ui.RLabelProvider;


public class ROpenDeclaration extends OpenDeclaration {
	
	
	public ROpenDeclaration() {
	}
	
	
	@Override
	public ILabelProvider createLabelProvider() {
		return new RLabelProvider() {
			@Override
			public StyledString getStyledText(final IModelElement element) {
				final StyledString styledText = super.getStyledText(element);
				if (element instanceof ISourceElement) {
					final ISourceUnit su = ((ISourceElement) element).getSourceUnit();
					if (su instanceof IWorkspaceSourceUnit) {
						styledText.append("  ∙  "); //$NON-NLS-1$
						styledText.append(((IResource) su.getResource()).getFullPath().toString(),
								StyledString.QUALIFIER_STYLER );
					}
				}
				return styledText;
			}
			
		};
	}
	
}