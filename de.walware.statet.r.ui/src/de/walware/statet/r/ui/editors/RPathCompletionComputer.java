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

import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.text.sourceediting.PathCompletionComputor;

import de.walware.statet.nico.core.ITool;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 * 
 */
public class RPathCompletionComputer extends PathCompletionComputor {
	
	
	private ToolProcess<ToolWorkspace> fAssociatedTool;
	
	
	public RPathCompletionComputer() {
	}
	
	
	@Override
	public void sessionStarted(final ISourceEditor editor) {
		super.sessionStarted(editor);
		fAssociatedTool = (ToolProcess<ToolWorkspace>) editor.getAdapter(ITool.class);
	}
	
	@Override
	public void sessionEnded() {
		super.sessionEnded();
		fAssociatedTool = null;
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
		if (fAssociatedTool != null) {
			return fAssociatedTool.getWorkspaceData().getWorkspaceDir();
		}
		return null;
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
