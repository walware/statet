/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.Iterator;
import java.util.List;

import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.IModelElement.Filter;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.RDocuLink;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.DocuTag;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RClass;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RMethod;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RVariable;


public class RoxygenAnalyzer implements Filter<IRLangSourceElement> {
	
	
	private IRoxygenAnalyzeContext fContext;
	
	private List<RAstNode> fComments;
	
	private Iterator<RAstNode> fCommentsIterator;
	private DocuComment fNextComment;
	private int fNextCommentRefOffset;
	
	
	public RoxygenAnalyzer() {
	}
	
	
	public void updateModel(IRoxygenAnalyzeContext context) {
		fContext = context;
		IRModelInfo model = context.getModelInfo();
		fComments = model.getAst().root.getComments();
		if (fComments == null || fComments.isEmpty()) {
			return;
		}
		fCommentsIterator = fComments.iterator();
		if (!nextDocuComment()) {
			return;
		}
		final ISourceStructElement sourceElement = model.getSourceElement();
		if (sourceElement instanceof IRLangSourceElement) {
			include((IRLangSourceElement) sourceElement);
		}
		if (fNextComment != null) {
			checkElement(null);
		}
	}
	
	private boolean nextDocuComment() {
		while (fCommentsIterator.hasNext()) {
			final RAstNode next = fCommentsIterator.next();
			if (next.getNodeType() == NodeType.DOCU_AGGREGATION) {
				fNextComment = (DocuComment) next;
				fNextCommentRefOffset = fNextComment.getSubsequentNodeOffset();
				if (fNextCommentRefOffset != Integer.MIN_VALUE) {
					return true;
				}
				else {
					checkElement(null);
				}
			}
		}
		fNextComment = null;
		fNextCommentRefOffset = Integer.MAX_VALUE;
		return false;
	}
	
	
	public boolean include(final IRLangSourceElement element) {
		if (fNextComment == null) {
			return true;
		}
		final int offset = element.getSourceRange().getOffset();
		while (fNextCommentRefOffset < offset) {
			checkElement(null);
			nextDocuComment();
		}
		if (fNextCommentRefOffset == offset) {
			if (element instanceof RClass) {
				final RClass rClass = (RClass) element;
				final RDocuLink link = new RDocuLink(element, fNextComment);
				fNextComment.addAttachment(link);
				rClass.fDocu = fNextComment;
				checkElement(rClass);
				nextDocuComment();
			}
			else if (element instanceof RMethod) {
				final RMethod rMethod = (RMethod) element;
				final RDocuLink link = new RDocuLink(element, fNextComment);
				fNextComment.addAttachment(link);
				rMethod.fDocu = fNextComment;
				checkElement(rMethod);
				nextDocuComment();
			}
			else if (element instanceof RVariable) {
				final RVariable rVariable = (RVariable) element;
				final RDocuLink link = new RDocuLink(element, fNextComment);
				fNextComment.addAttachment(link);
				rVariable.fDocu = fNextComment;
				checkElement(rVariable);
				nextDocuComment();
			}
		}
		
		if (fNextCommentRefOffset < offset+element.getSourceRange().getLength()) {
			return element.hasSourceChildren(this);
		}
		return false;
	}
	
	private void checkElement(final IRLangSourceElement element) {
		final List<DocuTag> tags = fNextComment.getTags();
		for (final DocuTag tag : tags) {
			final RoxygenTagType tagType = RoxygenTagType.TYPES.get(tag.getText());
			if (tagType != null) {
				tagType.analyze(fContext, tag, element);
			}
		}
	}
	
}
