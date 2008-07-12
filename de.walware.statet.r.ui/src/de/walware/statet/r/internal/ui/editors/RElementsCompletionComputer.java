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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import de.walware.eclipsecommons.ltk.IElementName;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.ast.AstSelection;
import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.eclipsecommons.ui.text.sourceediting.IContentAssistComputer;
import de.walware.eclipsecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.eclipsecommons.ui.text.sourceediting.SimpleCompletionProposal;

import de.walware.statet.r.core.model.IEnvirInSource;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.RLabelProvider;


public class RElementsCompletionComputer implements IContentAssistComputer {
	
	
	private final RLabelProvider fLabelProvider = new RLabelProvider();
	
	
	public void sessionStarted(final ISourceEditor editor) {
	}
	
	public void sessionEnded() {
	}
	
	public IStatus computeCompletionProposals(final AssistInvocationContext context,
			final List<ICompletionProposal> tenders, final IProgressMonitor monitor) {
		final AstSelection astSelection = context.getAstSelection();
		IAstNode node = astSelection.getCovering();
		if (node == null) {
			node = context.getAstInfo().root;
		}
		if (!(node instanceof RAstNode)) {
			return null;
		}
		final IEnvirInSource envir = RModel.searchEnvir((RAstNode) node);
		if (envir == null) {
			return null;
		}
		final IEnvirInSource[] envirList = RModel.createEnvirList(envir);
		
		final String prefix = context.getIdentifierPrefix();
		if (prefix == null) {
			return null;
		}
		final RElementName prefixSegments = RElementName.parseDefault(prefix);
		if (prefixSegments == null) {
			return null;
		}
		if (prefixSegments.getNextSegment() == null) {
			doComputeMainProposals(context, envirList, prefix, prefixSegments.getSegmentName(), tenders, monitor);
		}
		else {
			doComputeSubProposals(context, envirList, prefixSegments, tenders, monitor);
		}
		return null;
	}
	
	protected void doComputeMainProposals(final AssistInvocationContext context, final IEnvirInSource[] envirList, final String orgPrefix, final String namePrefix,
			final List<ICompletionProposal> tenders, final IProgressMonitor monitor) {
		final Set<String> methodNames = new HashSet<String>();
		final Set<String> mainNames = new HashSet<String>();
		
		int sourceLevel = -1;
		for (final IEnvirInSource envir : envirList) {
			int distance;
			switch (envir.getType()) {
			case IEnvirInSource.T_CLASS:
			case IEnvirInSource.T_FUNCTION:
				distance = Math.min(++sourceLevel, 7);
				break;
			case IEnvirInSource.T_PROJ:
				distance = 8;
				break;
			case IEnvirInSource.T_PKG:
				distance = 9;
				break;
			case IEnvirInSource.T_EXPLICIT:
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
							&& isCompletable(elementName) && elementName.getSegmentName().startsWith(namePrefix)) {
						final Set<String> names = (element.getElementType() == IRLangElement.R_S4METHOD) ?
								methodNames : mainNames;
						if ((distance > 0) && names.contains(elementName.getSegmentName()) ) {
							continue;
						}
						final ICompletionProposal proposal = createProposal(context, orgPrefix, elementName, element, distance);
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
		for (final IEnvirInSource envir : envirList) {
			final Set<String> elementNames = envir.getElementNames();
			for (final String name : elementNames) {
				if (name.startsWith(namePrefix) && !mainNames.contains(name)) {
					if (name.equals(namePrefix) && envir.getAllAccessOfElement(name).size() <= 1) {
						continue; // prefix itself
					}
					final ICompletionProposal proposal = createProposal(context, orgPrefix, name);
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
	
	protected void doComputeSubProposals(final AssistInvocationContext context, final IEnvirInSource[] envirList, final RElementName prefixSegments,
			final List<ICompletionProposal> tenders, final IProgressMonitor monitor) {
	}
	
	protected ICompletionProposal createProposal(final AssistInvocationContext context, final String prefix, final String name) {
		final int offset = context.getInvocationOffset()-prefix.length();
		return new SimpleCompletionProposal(name, offset);
	}
	
	protected ICompletionProposal createProposal(final AssistInvocationContext context, final String prefix, final IElementName elementName, final IModelElement element, final int distance) {
		final int offset = context.getInvocationOffset()-prefix.length();
		return new RCompletionProposal(context, elementName, offset, element, distance, fLabelProvider);
	}
	
	public IStatus computeContextInformation(final AssistInvocationContext context,
			final List<IContextInformation> tenders, final IProgressMonitor monitor) {
		return null;
	}
	
}
