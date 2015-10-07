/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.List;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.core.model.ISourceElement;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.text.BasicHeuristicTokenScanner;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RChunkElement;
import de.walware.statet.r.core.refactoring.RRefactoringAdapter;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


public class RSourceCodeAdapter extends RRefactoringAdapter {
	
	
	public RSourceCodeAdapter() {
	}
	
	
	@Override
	protected void getSourceCode(final ISourceElement element, final AbstractDocument doc,
			final BasicHeuristicTokenScanner scanner, final List<String> codeFragments)
			throws BadLocationException, BadPartitioningException {
		if (element instanceof IRLangElement) {
			if (element instanceof RChunkElement) {
				final List<SourceComponent> components = (List<SourceComponent>) element.getAdapter(SourceComponent.class);
				for (final SourceComponent component : components) {
					final IRegion range = expandSourceRange(component.getOffset(), component.getEndOffset(), doc, scanner);
					if (range != null && range.getLength() > 0) {
						codeFragments.add(doc.get(range.getOffset(), range.getLength()));
					}
				}
				return;
			}
			super.getSourceCode(element, doc, scanner, codeFragments);
			return;
		}
		if (element instanceof ISourceStructElement) {
			final List<? extends ISourceStructElement> children = ((ISourceStructElement) element).getSourceChildren(null);
			for (final ISourceStructElement child : children) {
				getSourceCode(child, doc, scanner, codeFragments);
			}
		}
	}
	
}
