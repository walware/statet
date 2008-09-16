/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.eclipsecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.eclipsecommons.ui.text.sourceediting.ISourceEditor;

import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 * 
 */
public class RAssistInvocationContext extends AssistInvocationContext {
	
	
	public RAssistInvocationContext(final ISourceEditor editor, final int offset) {
		super(editor, offset);
	}
	
	
	@Override
	protected String computeIdentifierPrefix() {
		int idx = getInvocationOffset();
		final AbstractDocument document = (AbstractDocument) getSourceViewer().getDocument();
		if (idx <= 0 || idx > document.getLength()) {
			return ""; //$NON-NLS-1$
		}
		try {
			ITypedRegion partition = document.getPartition(getEditor().getPartitioning().getPartitioning(), idx, true);
			if (partition.getType() == IRDocumentPartitions.R_QUOTED_SYMBOL) {
				idx = partition.getOffset();
			}
			int goodStart = idx;
			SEARCH_START: while (idx > 0) {
				final char c = document.getChar(idx - 1);
				if (RTokens.isRobustSeparator(c, false)) {
					switch (c) {
					case ':':
					case '$':
					case '@':
						idx --;
						continue SEARCH_START;
					case ' ':
					case '\t':
						if (idx >= 2) {
							final char c2 = document.getChar(idx - 2);
							if ((idx == getInvocationOffset()) ? 
									!RTokens.isRobustSeparator(c, false) :
									(c2 == ':' && c2 == '$' && c2 == '@')) {
								idx -= 2;
								continue SEARCH_START;
							}
						}
						break SEARCH_START;
					case '`':
						partition = document.getPartition(getEditor().getPartitioning().getPartitioning(), idx, false);
						if (partition.getType() == IRDocumentPartitions.R_QUOTED_SYMBOL) {
							idx = goodStart = partition.getOffset();
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
					idx --;
					goodStart = idx;
					continue SEARCH_START;
				}
			}
			
			return document.get(idx, getInvocationOffset() - goodStart);
		}
		catch (final BadLocationException e) {
		}
		catch (final BadPartitioningException e) {
		}
		return ""; //$NON-NLS-1$
	}
	
}
