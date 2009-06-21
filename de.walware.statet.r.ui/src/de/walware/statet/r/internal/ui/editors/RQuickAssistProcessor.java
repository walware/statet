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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.QuickAssistProcessor;

import de.walware.statet.base.ui.sourceeditors.StatextEditor1;

import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class RQuickAssistProcessor extends QuickAssistProcessor {
	
	
	public RQuickAssistProcessor(final StatextEditor1 editor) {
		super(editor);
	}
	
	
	@Override
	protected AssistInvocationContext createContext() {
		return new RAssistInvocationContext(getEditor(), -1, false);
	}
	
	@Override
	protected void addModelAssistProposals(final List<ICompletionProposal> proposals,
			final AssistInvocationContext context) {
		if (!(context.getAstSelection().getCovering() instanceof RAstNode)) {
			return;
		}
		final RAstNode node = (RAstNode) context.getAstSelection().getCovering();
		
		RAstNode candidate = node;
		SEARCH_ACCESS : while (candidate != null) {
			final Object[] attachments = candidate.getAttachments();
			for (int i = 0; i < attachments.length; i++) {
				if (attachments[i] instanceof RElementAccess) {
					RElementAccess access = (RElementAccess) attachments[i]; 
					SUB: while (access != null) {
						if (access.getSegmentName() == null) {
							break SUB;
						}
						if (access.getNameNode() == node) {
							addAccessAssistProposals(proposals, context, access);
							break SEARCH_ACCESS;
						}
						access = access.getNextSegment();
					}
				}
			}
			candidate = candidate.getRParent();
		}
	}
	
	protected void addAccessAssistProposals(final List<ICompletionProposal> proposals,
			final AssistInvocationContext invocationContext, final RElementAccess access) {
		final RElementAccess[] allInUnit = access.getAllInUnit();
		proposals.add(new LinkedNamesAssistProposal(LinkedNamesAssistProposal.IN_FILE, invocationContext, access));
		if (allInUnit.length > 2) {
			Arrays.sort(allInUnit, RElementAccess.NAME_POSITION_COMPARATOR);
			int current = 0;
			for (; current < allInUnit.length; current++) {
				if (access == allInUnit[current]) {
					break;
				}
			}
			if (current > 0 && current < allInUnit.length-1) {
				proposals.add(new LinkedNamesAssistProposal(LinkedNamesAssistProposal.IN_FILE_PRECEDING, invocationContext, access));
				proposals.add(new LinkedNamesAssistProposal(LinkedNamesAssistProposal.IN_FILE_FOLLOWING, invocationContext, access));
			}
		}
	}
	
}
