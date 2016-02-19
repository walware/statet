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

package de.walware.statet.r.ui.editors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Point;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.sourceediting.AbstractMarkOccurrencesProvider.RunData;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.DocuTag;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


public class RMarkOccurrencesLocator {
	
	
	public void run(final RunData run, final ISourceUnitModelInfo info,
			final AstSelection astSelection, final ITextSelection orgSelection)
			throws BadLocationException, BadPartitioningException, UnsupportedOperationException {
		RAstNode node = (RAstNode) astSelection.getCovering();
		if (checkForAccess(run, node)) {
			return;
		}
		
		if (orgSelection != null && info instanceof IRModelInfo) {
			final int start = orgSelection.getOffset();
			final int stop = start + orgSelection.getLength();
			if (info.getAst().root instanceof SourceComponent) {
				final List<RAstNode> comments = ((SourceComponent) info.getAst().root).getComments();
				for (final RAstNode comment : comments) {
					if (comment.getEndOffset() < start) {
						continue;
					}
					if (comment.getOffset() > stop) {
						break;
					}
					if (comment.getNodeType() == NodeType.DOCU_AGGREGATION) {
						final DocuComment docuComment = (DocuComment) comment;
						final List<DocuTag> tags = docuComment.getTags();
						for (final DocuTag tag : tags) {
							if (tag.getEndOffset() < start) {
								continue;
							}
							if (tag.getOffset() > stop) {
								break;
							}
							final AstSelection selection = AstSelection.search(tag, start, stop, AstSelection.MODE_COVERING_SAME_LAST);
							node = (RAstNode) selection.getCovering();
							if (checkForAccess(run, node)) {
								return;
							}
						}
					}
				}
			}
		}
	}
	
	private boolean checkForAccess(final RunData run, RAstNode node) throws BadLocationException {
		if (node == null
				|| !(node.getNodeType() == NodeType.SYMBOL || node.getNodeType() == NodeType.STRING_CONST)) {
			return false;
		}
		do {
			final List<Object> attachments= node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof RElementAccess) {
					final RElementAccess access= (RElementAccess) attachment;
					final Map<Annotation, Position> annotations = checkDefault(run, access);
					
					if (annotations != null) {
						run.set(annotations);
						return true;
					}
				}
			}
			node = node.getRParent();
		} while (node != null);
		
		return false;
	}
	
	private Map<Annotation, Position> checkDefault(final RunData run, RElementAccess access) throws BadLocationException {
		while (access != null) {
			final RAstNode nameNode = access.getNameNode();
			if (nameNode == null) {
				return null;
			}
			if (run.accept(new Point(nameNode.getOffset(), nameNode.getEndOffset()))) {
				final ImList<? extends RElementAccess> allAccess= access.getAllInUnit(false);
				final Map<Annotation, Position> annotations= new LinkedHashMap<>(allAccess.size());
				for (final RElementAccess aAccess : allAccess) {
					final String message= run.doc.get(aAccess.getNode().getOffset(), aAccess.getNode().getLength());
					annotations.put(
							new Annotation(aAccess.isWriteAccess() ? 
									ITextPresentationConstants.ANNOTATIONS_WRITE_OCCURRENCES_TYPE:
									ITextPresentationConstants.ANNOTATIONS_COMMON_OCCURRENCES_TYPE,
									false, message),
							RAst.getElementNamePosition(aAccess.getNameNode()) );
				}
				return annotations;
			}
			access = access.getNextSegment();
		}
		return null;
	}
	
}
