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
	
	
	public RPathCompletionProcessor(final NIConsolePage page) {
		fPage = page;
	}
	
	
	@Override
	protected IRegion getContentRange(final IDocument document, final int offset) throws BadLocationException {
		final ITypedRegion partition = TextUtilities.getPartition(document, IRDocumentPartitions.R_PARTITIONING, offset, true);
		int start = partition.getOffset();
		int end = partition.getOffset() + partition.getLength();
		if (start == end) {
			return null;
		}
		
		final char bound = document.getChar(start);
		if (bound == '\"' || bound == '\'') {
			start++;
		}
		else {
			return null;
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
	protected String checkPrefix(final String prefix) {
		String unescaped = RUtil.unescapeCompletly(prefix);
		// keep a single (not escaped) backslash
		if (prefix.length() > 0 && prefix.charAt(prefix.length()-1) == '\\' && 
				(unescaped.length() == 0 || unescaped.charAt(unescaped.length()-1) != '\\')) {
			unescaped = unescaped + '\\';
		}
		return super.checkPrefix(prefix);
	}
	
	@Override
	protected String checkPathCompletion(final IDocument document, final int completionOffset, String completion)
			throws BadLocationException {
		completion = RUtil.escapeCompletly(completion);
		int existingBackslashCount = 0;
		if (completionOffset >= 1) {
			if (document.getChar(completionOffset-1) == '\\') {
				existingBackslashCount++;
				if (completionOffset >= 2) {
					if (document.getChar(completionOffset-2) == '\\') {
						existingBackslashCount++;
					}
				}
			}
		}
		final boolean startsWithBackslash = (completion.length() >= 2 && 
				completion.charAt(0) == '\\' && completion.charAt(1) == '\\');
		if ((existingBackslashCount % 2) == 1) {
			if (startsWithBackslash) {
				completion = completion.substring(1);
			}
			else {
				completion = '\\' + completion;
			}
		}
		else if (existingBackslashCount > 0) {
			if (startsWithBackslash) {
				completion = completion.substring(2);
			}
		}
		return completion;
	}
	
}
