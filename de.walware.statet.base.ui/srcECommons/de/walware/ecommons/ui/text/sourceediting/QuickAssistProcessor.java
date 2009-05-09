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

package de.walware.ecommons.ui.text.sourceediting;

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

import de.walware.ecommons.ltk.IModelManager;


/**
 *
 */
public class QuickAssistProcessor implements IQuickAssistProcessor {
	
	
	private final ISourceEditor fEditor;
	private String fErrorMessage;
	
	
	public QuickAssistProcessor() {
		this(null);
	}
	
	public QuickAssistProcessor(final ISourceEditor editor) {
		fEditor = editor;
	}
	
	
	/**
	 * @return the editor
	 */
	public ISourceEditor getEditor() {
		return fEditor;
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
	
	/**
	 * Creates the context that is passed to the completion proposal
	 * computers.
	 * 
	 * @return the context to be passed to the computers
	 */
	protected AssistInvocationContext createContext() {
		return new AssistInvocationContext(getEditor(), -1, IModelManager.MODEL_FILE);
	}
	
	public ICompletionProposal[] computeQuickAssistProposals(final IQuickAssistInvocationContext invocationContext) {
		fErrorMessage = null;
		
		final AssistInvocationContext context = createContext();
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
					final ICompletionProposal[] annotationProposals = problem.getProposals(invocationContext);
					if (annotationProposals != null && annotationProposals.length > 0) {
						proposals.addAll(Arrays.asList(annotationProposals));
					}
				}
			}
		}
	}
	
	protected void addModelAssistProposals(final List<ICompletionProposal> proposals, final AssistInvocationContext context) {
	}
	
	public String getErrorMessage() {
		return fErrorMessage;
	}
	
}
