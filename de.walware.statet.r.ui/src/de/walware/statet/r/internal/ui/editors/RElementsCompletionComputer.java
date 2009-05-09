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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.icu.text.Collator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.IAssistCompletionProposal;
import de.walware.ecommons.ui.text.sourceediting.IAssistInformationProposal;
import de.walware.ecommons.ui.text.sourceediting.IContentAssistComputer;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.text.sourceediting.KeywordCompletionProposal;
import de.walware.ecommons.ui.text.sourceediting.SimpleCompletionProposal;

import de.walware.statet.r.core.model.IFrame;
import de.walware.statet.r.core.model.IFrameInSource;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.RLabelProvider;


public class RElementsCompletionComputer implements IContentAssistComputer {
	
	
	private static final List<String> fgKeywords;
	static {
		final ArrayList<String> list = new ArrayList<String>();
		Collections.addAll(list, RTokens.CONSTANT_WORDS);
		Collections.addAll(list, RTokens.FLOWCONTROL_WORDS);
		Collections.sort(list, Collator.getInstance());
		list.trimToSize();
		fgKeywords = Collections.unmodifiableList(list);
	}
	
	
	private final RLabelProvider fLabelProvider = new RLabelProvider();
	
	
	public RElementsCompletionComputer() {
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void sessionStarted(final ISourceEditor editor) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void sessionEnded() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IStatus computeCompletionProposals(final AssistInvocationContext context,
			final int mode, final List<IAssistCompletionProposal> tenders, final IProgressMonitor monitor) {
		if (mode == IContentAssistComputer.INFORMATION_MODE) {
			return null;
		}
		
		if (context.getModelInfo() == null) {
			return null;
		}
		
		// Get node
		final AstSelection astSelection = context.getAstSelection();
		IAstNode node = astSelection.getCovering();
		if (node == null) {
			node = context.getAstInfo().root;
		}
		if (!(node instanceof RAstNode)) {
			return null;
		}
		
		// Get envir
		final IFrameInSource envir = RModel.searchEnvir((RAstNode) node);
		if (envir == null) {
			return null;
		}
		final IFrameInSource[] envirList = RModel.createEnvirList(envir);
		
		// Get prefix
		final String prefix = context.getIdentifierPrefix();
		if (prefix == null) {
			return null;
		}
		final RElementName prefixSegments = RElementName.parseDefault(prefix);
		if (prefixSegments == null) {
			return null;
		}
		
		// Collect proposals
		if (prefixSegments.getNextSegment() == null) {
			doComputeMainProposals(context, envirList, prefix, prefixSegments.getSegmentName(), tenders, monitor);
			doComputeKeywordProposals(context, prefixSegments.getSegmentName(), tenders, monitor);
		}
		else {
			doComputeSubProposals(context, envirList, prefixSegments, tenders, monitor);
		}
		return null;
	}
	
	protected void doComputeMainProposals(final AssistInvocationContext context, final IFrameInSource[] envirList, final String orgPrefix, final String namePrefix,
			final List<IAssistCompletionProposal> tenders, final IProgressMonitor monitor) {
		final Set<String> methodNames = new HashSet<String>();
		final Set<String> mainNames = new HashSet<String>();
		
		int sourceLevel = -1;
		for (final IFrameInSource envir : envirList) {
			int distance;
			switch (envir.getFrameType()) {
			case IFrame.T_CLASS:
			case IFrame.T_FUNCTION:
				distance = Math.min(++sourceLevel, 7);
				break;
			case IFrame.T_PROJ:
				distance = 8;
				break;
			case IFrame.T_PKG:
				distance = 9;
				break;
			case IFrame.T_EXPLICIT:
				continue;
			default:
				distance = 10;
				break;
			}
			final IModelElement envirElement = envir.getModelElement();
			if (envirElement != null) {
				final List<? extends IModelElement> elements = envirElement.getChildren(null);
				for (final IModelElement element : elements) {
					final IElementName elementName = element.getElementName();
					final int c1type = (element.getElementType() & IModelElement.MASK_C1);
					if ((c1type == IModelElement.C1_METHOD || c1type == IModelElement.C1_VARIABLE)
							&& isCompletable(elementName)
							&& elementName.getSegmentName().regionMatches(true, 0, namePrefix, 0, namePrefix.length())) {
						final Set<String> names = (element.getElementType() == IRLangElement.R_S4METHOD) ?
								methodNames : mainNames;
						if ((distance > 0) && names.contains(elementName.getSegmentName()) ) {
							continue;
						}
						final IAssistCompletionProposal proposal = createProposal(context, orgPrefix, elementName, element, distance);
						if (proposal != null) {
							if (elementName.getNextSegment() == null) {
								names.add(elementName.getSegmentName());
							}
							tenders.add(proposal);
						}
					}
				}
			}
		}
		
		mainNames.addAll(methodNames);
		for (final IFrameInSource envir : envirList) {
			final Set<String> elementNames = envir.getElementNames();
			for (final String name : elementNames) {
				if (name != null && name.regionMatches(true, 0, namePrefix, 0, namePrefix.length()) 
						&& !mainNames.contains(name)) {
					if (name.equals(namePrefix) && envir.getAllAccessOfElement(name).size() <= 1) {
						continue; // prefix itself
					}
					final IAssistCompletionProposal proposal = createProposal(context, orgPrefix, name);
					if (proposal != null) {
						mainNames.add(name);
						tenders.add(proposal);
					}
				}
			}
		}
	}
	
	private boolean isCompletable(IElementName elementName) {
		while (elementName != null) {
			switch (elementName.getType()) {
			case RElementName.SUB_INDEXED_S:
			case RElementName.SUB_INDEXED_D:
				return false;
			}
			if (elementName.getSegmentName() == null) {
				return false;
			}
			elementName = elementName.getNextSegment();
		}
		return true;
	}
	
	private void doComputeKeywordProposals(final AssistInvocationContext context, final String prefix,
			final List<IAssistCompletionProposal> tenders, final IProgressMonitor monitor) {
		if (prefix.length() > 0) {
			final int offset = context.getInvocationOffset()-prefix.length();
			final List<String> keywords = fgKeywords;
			for (final String keyword : keywords) {
				if (keyword.regionMatches(true, 0, prefix, 0, prefix.length())) {
					tenders.add(new KeywordCompletionProposal(keyword, offset));
				}
			}
		}
	}
	
	protected void doComputeSubProposals(final AssistInvocationContext context, final IFrame[] envirList, final RElementName prefixSegments,
			final List<IAssistCompletionProposal> tenders, final IProgressMonitor monitor) {
	}
	
	protected IAssistCompletionProposal createProposal(final AssistInvocationContext context, final String prefix, final String name) {
		final int offset = context.getInvocationOffset()-prefix.length();
		return new SimpleCompletionProposal(name, offset);
	}
	
	protected IAssistCompletionProposal createProposal(final AssistInvocationContext context, final String prefix, final IElementName elementName, final IModelElement element, final int distance) {
		final int offset = context.getInvocationOffset()-prefix.length();
		return new RCompletionProposal(context, elementName, offset, element, distance, fLabelProvider);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IStatus computeContextInformation(final AssistInvocationContext context,
			final List<IAssistInformationProposal> tenders, final IProgressMonitor monitor) {
		return null;
	}
	
}
