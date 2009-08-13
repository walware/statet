/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.ITypedRegion;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;

import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 * 
 */
public class RAssistInvocationContext extends AssistInvocationContext {
	
	
	public RAssistInvocationContext(final ISourceEditor editor, final int offset, final boolean isProposal) {
		super(editor, offset, (isProposal) ? IModelManager.MODEL_FILE : IModelManager.NONE);
	}
	
	
	@Override
	protected String computeIdentifierPrefix(int offset) {
		final AbstractDocument document = (AbstractDocument) getSourceViewer().getDocument();
		if (offset <= 0 || offset > document.getLength()) {
			return ""; //$NON-NLS-1$
		}
		try {
			ITypedRegion partition = document.getPartition(getEditor().getPartitioning().getPartitioning(), offset, true);
			if (partition.getType() == IRDocumentPartitions.R_QUOTED_SYMBOL) {
				offset = partition.getOffset();
			}
			int goodStart = offset;
			SEARCH_START: while (offset > 0) {
				final char c = document.getChar(offset - 1);
				if (RTokens.isRobustSeparator(c, false)) {
					switch (c) {
					case ':':
					case '$':
					case '@':
						offset --;
						continue SEARCH_START;
					case ' ':
					case '\t':
						if (offset >= 2) {
							final char c2 = document.getChar(offset - 2);
							if ((offset == getInvocationOffset()) ? 
									!RTokens.isRobustSeparator(c, false) :
									(c2 == ':' && c2 == '$' && c2 == '@')) {
								offset -= 2;
								continue SEARCH_START;
							}
						}
						break SEARCH_START;
					case '`':
						partition = document.getPartition(getEditor().getPartitioning().getPartitioning(), offset - 1, false);
						if (partition.getType() == IRDocumentPartitions.R_QUOTED_SYMBOL) {
							offset = goodStart = partition.getOffset();
							continue SEARCH_START;
						}
						else {
							break SEARCH_START;
						}
					
					default:
						break SEARCH_START;
					}
				}
				else {
					offset --;
					goodStart = offset;
					continue SEARCH_START;
				}
			}
			
			return document.get(offset, getInvocationOffset() - goodStart);
		}
		catch (final BadLocationException e) {
		}
		catch (final BadPartitioningException e) {
		}
		return ""; //$NON-NLS-1$
	}
	
}
