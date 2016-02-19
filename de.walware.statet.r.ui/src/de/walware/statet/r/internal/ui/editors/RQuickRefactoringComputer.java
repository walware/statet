/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImIdentityList;

import de.walware.ecommons.ltk.LTKUtil;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IQuickAssistComputer;

import de.walware.statet.r.core.model.IRCompositeSourceElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.correction.RLinkedNamesAssistProposal;
import de.walware.statet.r.internal.ui.correction.RenameInRegionAssistProposal;
import de.walware.statet.r.internal.ui.correction.RenameInWorkspaceAssistProposal;


public class RQuickRefactoringComputer implements IQuickAssistComputer {
	
	
	public RQuickRefactoringComputer() {
	}
	
	
	@Override
	public IStatus computeAssistProposals(final AssistInvocationContext context,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		if (!(context.getAstSelection().getCovering() instanceof RAstNode)) {
			return Status.OK_STATUS;
		}
		final RAstNode node = (RAstNode) context.getAstSelection().getCovering();
		
		if (node.getNodeType() == NodeType.SYMBOL || node.getNodeType() == NodeType.STRING_CONST) {
			RAstNode candidate = node;
			SEARCH_ACCESS : while (candidate != null) {
				final List<Object> attachments= candidate.getAttachments();
				for (final Object attachment : attachments) {
					if (attachment instanceof RElementAccess) {
						RElementAccess access= (RElementAccess) attachment; 
						SUB: while (access != null) {
							if (access.getSegmentName() == null) {
								break SUB;
							}
							if (access.getNameNode() == node) {
								addAccessAssistProposals(context, access, proposals);
								break SEARCH_ACCESS;
							}
							access = access.getNextSegment();
						}
					}
				}
				candidate = candidate.getRParent();
			}
		}
		else if (context.getLength() > 0 && context.getSourceUnit() instanceof IRSourceUnit) {
			proposals.add(new RenameInRegionAssistProposal(context));
		}
		return Status.OK_STATUS;
	}
	
	protected void addAccessAssistProposals(final AssistInvocationContext context,
			final RElementAccess access,
			final AssistProposalCollector proposals) {
		final ImIdentityList<? extends RElementAccess> allAccess= ImCollections.toIdentityList(
				access.getAllInUnit(false) );
		
		proposals.add(new RLinkedNamesAssistProposal(RLinkedNamesAssistProposal.IN_FILE, context, access));
		
		if (allAccess.size() > 2) {
			IRegion chunk= null;
			{	final ISourceStructElement sourceElement= context.getModelInfo().getSourceElement();
				if (sourceElement instanceof IRCompositeSourceElement) {
					final List<? extends IRLangSourceElement> elements= ((IRCompositeSourceElement) sourceElement).getCompositeElements();
					final IRLangSourceElement element= LTKUtil.getCoveringSourceElement(elements, access.getNameNode().getOffset());
					if (element != null) {
						chunk= element.getSourceRange();
					}
				}
			}
			final int current= allAccess.indexOf(access);
			if (current >= 0) {
				if (current > 0 && current < allAccess.size() - 1) {
					proposals.add(new RLinkedNamesAssistProposal(RLinkedNamesAssistProposal.IN_FILE_PRECEDING, context, access));
					proposals.add(new RLinkedNamesAssistProposal(RLinkedNamesAssistProposal.IN_FILE_FOLLOWING, context, access));
				}
				if (chunk != null) {
					int chunkBegin= 0;
					for (final int offset= chunk.getOffset();
							chunkBegin < current; chunkBegin++) {
						if (offset <= allAccess.get(chunkBegin).getNameNode().getOffset()) {
							break;
						}
					}
					int chunkEnd= current + 1;
					for (final int offset= chunk.getOffset() + chunk.getLength();
							chunkEnd < allAccess.size(); chunkEnd++) {
						if (offset <= allAccess.get(chunkEnd).getNameNode().getOffset()) {
							break;
						}
					}
					if (chunkEnd - chunkBegin > 1) {
						proposals.add(new RLinkedNamesAssistProposal(RLinkedNamesAssistProposal.IN_CHUNK, context, access, chunk));
					}
				}
			}
		}
		if (context.getSourceUnit() instanceof IRWorkspaceSourceUnit) {
			proposals.add(new RenameInWorkspaceAssistProposal(context, access.getNameNode()));
		}
	}
	
}
