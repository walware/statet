/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.debug;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITypedRegion;

import de.walware.eclipsecommons.ltk.text.TextUtil;

import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.internal.sweave.editors.RweaveTexDocumentSetupParticipant;
import de.walware.statet.r.launching.ICodeLaunchContentHandler;


public class RweaveContentHandler implements ICodeLaunchContentHandler {
	
	
	public String[] getCodeLines(final IDocument document) throws BadLocationException, CoreException {
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 doc3 = (IDocumentExtension3) document;
			if (doc3.getDocumentPartitioner(Rweave.R_TEX_PARTITIONING) == null) {
				new RweaveTexDocumentSetupParticipant().setup(document);
			}
			final ITypedRegion[] cats = Rweave.R_TEX_CAT_UTIL.getCats(document, 0, document.getLength());
			final ArrayList<String> lines = new ArrayList<String>(document.getNumberOfLines());
			for (int i = 0; i < cats.length; i++) {
				final ITypedRegion cat = cats[i];
				if (cat.getType() == Rweave.R_CAT) {
					TextUtil.getLines(document, cat.getOffset(), cat.getLength(), lines);
				}
			}
			return lines.toArray(new String[lines.size()]);
		}
		return null;
	}
	
}
