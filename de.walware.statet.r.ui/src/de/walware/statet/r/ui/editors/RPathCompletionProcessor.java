/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import de.walware.statet.base.ui.sourceeditors.PathCompletionProcessor;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.console.NIConsolePage;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 *
 */
public class RPathCompletionProcessor extends PathCompletionProcessor {
	
	
	private NIConsolePage fPage;
	private ToolWorkspace fCurrentWorkspace;
	
	
	/**
	 * 
	 */
	public RPathCompletionProcessor(NIConsolePage page) {
		fPage = page;
	}
	
	
	@Override
	protected IRegion getContentRange(IDocument document, int offset) throws BadLocationException {
		ITypedRegion partition = TextUtilities.getPartition(document, IRDocumentPartitions.R_DOCUMENT_PARTITIONING, offset, true);
		int start = partition.getOffset();
		int end = partition.getOffset() + partition.getLength();
		if (start == end) {
			return null;
		}
		
		char bound = document.getChar(start);
		if (bound == '\"' || bound == '\'') {
			start++;
		}
		else {
			bound = 0;
		}
		if (start > offset) {
			return null;
		}
		if (end > start && document.getChar(end-1) == bound) {
			if (end == offset) {
				return null;
			}
			end--;
		}
		
		return new Region(start, end-start);
	}
	
	@Override
	protected IFileStore getRelativeBase() {
		fCurrentWorkspace = fPage.getTool().getWorkspaceData();
		return fCurrentWorkspace.getWorkspaceDir();
	}
	
	@Override
	protected String checkPathCompletion(String completion) {
		return RUtil.escapeCompletly(completion);
	}
	
}
