/*******************************************************************************
 * Copyright (c) 2000-2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;


/**
 * A content assist processor for template variables.
 */
public final class TemplateVariableProcessor implements IContentAssistProcessor {
	
	private static Comparator<TemplateVariableProposal> fgTemplateVariableProposalComparator = 
				new Comparator<TemplateVariableProposal>() {
		
		public int compare(final TemplateVariableProposal proposal0, final TemplateVariableProposal proposal1) {
			return proposal0.getDisplayString().compareTo(proposal1.getDisplayString());
		}
		
		@Override
		public boolean equals(final Object arg0) {
			return false;
		}
		
		@Override
		public int hashCode() {
			return super.hashCode();
		}
	};
	
	
	/** the context type */
	private TemplateContextType fContextType;
	
	
	public TemplateVariableProcessor() {
	}
	
	
	/**
	 * Sets the context type.
	 * 
	 * @param contextType the context type for this processor
	 */
	public void setContextType(final TemplateContextType contextType) {
		fContextType= contextType;
	}
	
	/**
	 * Returns the context type.
	 * 
	 * @return the context type
	 */
	public TemplateContextType getContextType() {
		return fContextType;
	}
	
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer,	final int documentOffset) {
		if (fContextType == null)
			return null;
		
		final List<TemplateVariableProposal> proposals= new ArrayList<TemplateVariableProposal>();
		
		final String text= viewer.getDocument().get();
		final int start= getStart(text, documentOffset);
		final int end= documentOffset;
		
		final String string= text.substring(start, end);
		final String prefix= (string.length() >= 2)
			? string.substring(2)
			: null;
		
		final int offset= start;
		final int length= end - start;
		
		for (final Iterator iterator= fContextType.resolvers(); iterator.hasNext(); ) {
			final TemplateVariableResolver variable= (TemplateVariableResolver) iterator.next();
			
			if (prefix == null || variable.getType().startsWith(prefix))
				proposals.add(new TemplateVariableProposal(variable, offset, length, viewer));
		}
		
		Collections.sort(proposals, fgTemplateVariableProposalComparator);
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
	
	/* Guesses the start position of the completion */
	private int getStart(final String string, final int end) {
		int start= end;
		
		if (start >= 1 && string.charAt(start - 1) == '$')
			return start - 1;
		
		while ((start != 0) && Character.isUnicodeIdentifierPart(string.charAt(start - 1)))
			start--;
		
		if (start >= 2 && string.charAt(start - 1) == '{' && string.charAt(start - 2) == '$')
			return start - 2;
		
		return end;
	}
	
	public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int documentOffset) {
		return null;
	}
	
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {'$'};
	}
	
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}
	
	public String getErrorMessage() {
		return null;
	}
	
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
	
}
