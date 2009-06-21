/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk;

import java.util.List;

import org.eclipse.jface.text.IRegion;


public class LTKUtil {
	
	
	public static ISourceStructElement getCoveringSourceElement(final ISourceStructElement root, final IRegion region) {
		return getCoveringSourceElement(root, region.getOffset(), region.getOffset()+region.getLength());
	}
	
	public static ISourceStructElement getCoveringSourceElement(final ISourceStructElement root, final int startOffset, final int endOffset) {
		ISourceStructElement ok = root;
		CHECK: while (ok != null) {
			final List<? extends ISourceStructElement> children = ok.getSourceChildren(null);
			for (final ISourceStructElement child : children) {
				final IRegion sourceRange = child.getSourceRange();
				final IRegion docRange = child.getDocumentationRange();
				final int childOffset = (docRange != null) ?
						Math.min(sourceRange.getOffset(), docRange.getOffset()) :
						sourceRange.getOffset();
				if (startOffset >= childOffset) {
					final int childEnd = (docRange != null) ?
							Math.max(sourceRange.getOffset()+sourceRange.getLength(), docRange.getOffset()+docRange.getLength()) :
							sourceRange.getOffset()+sourceRange.getLength();
					if (startOffset < endOffset ? 
							(endOffset <= childEnd) : (endOffset < childEnd)) {
						ok = child;
						continue CHECK;
					}
				}
				else {
					break CHECK;
				}
			}
			break CHECK;
		}
		return ok;
	}
	
}
