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

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import de.walware.statet.base.ui.sourceeditors.ExtQuickAssistProcessor;
import de.walware.statet.base.ui.sourceeditors.ExtTextInvocationContext;
import de.walware.statet.base.ui.sourceeditors.StatextEditor1;
import de.walware.statet.r.core.rmodel.IElementAccess;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class RQuickAssistProcessor extends ExtQuickAssistProcessor {
	
	
	public RQuickAssistProcessor(final StatextEditor1 editor) {
		super(editor);
	}
	
	
	@Override
	protected void addModelAssistProposals(final List<ICompletionProposal> proposals,
			final ExtTextInvocationContext context) {
		if (!(context.getAstSelection().getCovering() instanceof RAstNode)) {
			return;
		}
		final RAstNode node = (RAstNode) context.getAstSelection().getCovering();
		
		RAstNode candidate = node;
		SEARCH_ACCESS : while (candidate != null) {
			final Object[] attachments = candidate.getAttachments();
			for (int i = 0; i < attachments.length; i++) {
				if (attachments[i] instanceof IElementAccess) {
					IElementAccess access = (IElementAccess) attachments[i]; 
					SUB: while (access != null) {
						if (access.getName() == null) {
							break SUB;
						}
						if (access.getNameNode() == node) {
							proposals.add(new LinkedNamesAssistProposal(context, access));
							break SEARCH_ACCESS;
						}
						access = access.getSubElementAccess();
					}
				}
			}
			candidate = candidate.getParent();
		}
	}
	
	protected void addAccessAssistProposals(final List<ICompletionProposal> proposals,
			final ExtTextInvocationContext invocationContext, final IElementAccess access) {
		proposals.add(new LinkedNamesAssistProposal(invocationContext, access));
	}
	
}
