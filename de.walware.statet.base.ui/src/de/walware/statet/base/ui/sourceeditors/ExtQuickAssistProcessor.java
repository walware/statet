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

package de.walware.statet.base.ui.sourceeditors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;


/**
 *
 */
public class ExtQuickAssistProcessor implements IQuickAssistProcessor {
	
	
	private StatextEditor1 fEditor;
	private String fErrorMessage;
	
	
	public ExtQuickAssistProcessor() {
		this(null);
	}
	
	public ExtQuickAssistProcessor(final StatextEditor1 editor) {
		fEditor = editor;
	}
	
	
	public boolean canAssist(final IQuickAssistInvocationContext invocationContext) {
		return false;
	}
	
	public boolean canFix(final Annotation annotation) {
		if (annotation.isMarkedDeleted()) {
			return false;
		}
		final String type = annotation.getType();
		if (type.equals(SpellingAnnotation.TYPE)) {
			return true;
		}
		return false;
	}
	
	public ICompletionProposal[] computeQuickAssistProposals(final IQuickAssistInvocationContext invocationContext) {
		fErrorMessage = null;
		
		final ExtTextInvocationContext context = new ExtTextInvocationContext(fEditor, invocationContext);
		final ISourceViewer viewer = context.getSourceViewer();
		final int offset = context.getOffset();
		if (viewer == null) {
			return null;
		}
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		
		final IAnnotationModel model = viewer.getAnnotationModel();
		if (model != null) {
			addAnnotationProposals(proposals, context, model);
		}
		if (context.getModelInfo() != null) {
			addModelAssistProposals(proposals, context);
		}
		
		if (proposals.isEmpty()) {
			return null;
		}
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
	
	protected boolean isMatchingPosition(final Position pos, final int offset) {
		return (pos != null)
				&& (offset >= pos.getOffset())
				&& (offset <= pos.getOffset()+pos.getLength());
	}
	
	private void addAnnotationProposals(final List<ICompletionProposal> proposals,
			final IQuickAssistInvocationContext invocationContext,
			final IAnnotationModel model) {
		final int offset = invocationContext.getOffset();
		final Iterator<Annotation> iter = model.getAnnotationIterator();
		while (iter.hasNext()) {
			final Annotation annotation = iter.next();
			if (annotation.isMarkedDeleted()) {
				continue;
			}
			final String type = annotation.getType();
			if (type.equals(SpellingAnnotation.TYPE)) {
				if (!isMatchingPosition(model.getPosition(annotation), offset)) {
					continue;
				}
				if (annotation instanceof SpellingAnnotation) {
					final SpellingProblem problem = ((SpellingAnnotation) annotation).getSpellingProblem();
					final ICompletionProposal[] annotationProposals = problem.getProposals();
					if (annotationProposals != null && annotationProposals.length > 0) {
						proposals.addAll(Arrays.asList(annotationProposals));
					}
				}
			}
		}
	}
	
	protected void addModelAssistProposals(final List<ICompletionProposal> proposals, final ExtTextInvocationContext context) {
	}
	
	public String getErrorMessage() {
		return fErrorMessage;
	}
	
}
