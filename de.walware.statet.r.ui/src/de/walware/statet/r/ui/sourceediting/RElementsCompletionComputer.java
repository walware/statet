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

package de.walware.statet.r.ui.sourceediting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IAssistCompletionProposal;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IContentAssistComputer;
import de.walware.ecommons.text.core.IFragmentDocument;
import de.walware.ecommons.text.core.IPartitionConstraint;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.RSymbolComparator.PrefixPattern;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.RCoreFunctions;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.pkgmanager.IRPkgCollection;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAst.FCallArgMatch;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.FCallNamePattern;
import de.walware.statet.r.internal.ui.editors.RElementCompletionProposal;
import de.walware.statet.r.internal.ui.editors.RKeywordCompletionProposal;
import de.walware.statet.r.internal.ui.editors.RSimpleCompletionProposal;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.editors.IRSourceEditor;
import de.walware.statet.r.ui.sourceediting.RAssistInvocationContext.FCallInfo;
import de.walware.statet.r.ui.sourceediting.RFrameSearchPath.RFrameIterator;


public class RElementsCompletionComputer implements IContentAssistComputer {
	
	
	private static final IPartitionConstraint NO_R_COMMENT_CONSTRAINT= new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType != IRDocumentConstants.R_COMMENT_CONTENT_TYPE);
		};
	};
	
	
	private static final List<String> fgKeywords;
	static {
		final ArrayList<String> list= new ArrayList<>();
		Collections.addAll(list, RTokens.CONSTANT_WORDS);
		Collections.addAll(list, RTokens.FLOWCONTROL_WORDS);
		Collections.sort(list, RSymbolComparator.R_NAMES_COLLATOR);
		list.trimToSize();
		fgKeywords= Collections.unmodifiableList(list);
	}
	
	
	public static class CompleteRuntime extends RElementsCompletionComputer {
		
		public CompleteRuntime() {
			super(RFrameSearchPath.ENGINE_MODE);
		}
		
	}
	
	
	protected static final int NA_PRIO= Integer.MIN_VALUE;
	
	protected static final int ARG_NAME_PRIO= 80;
	
	protected static final int ARG_TYPE_PRIO= 40;
	protected static final int ARG_TYPE_NO_PRIO= -40;
	
	
	private final IElementLabelProvider labelProvider= new RLabelProvider(RLabelProvider.NAMESPACE);
	
	private final int mode;
	
	private IRSourceEditor editor;
	private ContentAssist assist;
	
	private final RFrameSearchPath searchPath= new RFrameSearchPath();
	
	private boolean inDefault;
	private boolean inString;
	
	private int pkgNamePrio;
	private int helpTopicPrio;
	
	private IStatus resultStatus;
	
	
	public RElementsCompletionComputer() {
		this(0);
	}
	
	protected RElementsCompletionComputer(final int mode) {
		this.mode= mode;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionStarted(final ISourceEditor editor, final ContentAssist assist) {
		this.editor= (editor instanceof IRSourceEditor) ? (IRSourceEditor) editor : null;
		
		this.assist= assist;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionEnded() {
		this.searchPath.clear();
		
		this.assist= null;
		this.resultStatus= null;
	}
	
	protected final void setStatus(final IStatus status) {
		this.resultStatus= status;
	}
	
	
	protected final int getSearchMode(final RAssistInvocationContext context) {
		if (this.mode != 0) {
			return this.mode;
		}
		return context.getDefaultRFrameSearchMode();
	}
	
	
	protected final boolean isSymbolCandidate(final String name) {
		for (int i= 0; i < name.length(); i++) {
			if (RTokens.isRobustSeparator(name.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isCompletable(RElementName elementName) {
		if (elementName == null) {
			return false;
		}
		do {
			switch (elementName.getType()) {
			case RElementName.SUB_INDEXED_S:
			case RElementName.SUB_INDEXED_D:
				return false;
			}
			if (elementName.getSegmentName() == null) {
				return false;
			}
			elementName= elementName.getNextSegment();
		}
		while (elementName != null);
		return true;
	}
	
	private RAstNode getRAstNode(final RAssistInvocationContext context) {
		IAstNode node= context.getInvocationAstSelection().getCovering();
		if (node == null) {
			node= context.getAstInfo().root;
		}
		return (node instanceof RAstNode) ? (RAstNode) node : null;
	}
	
	
	@Override
	public IStatus computeCompletionProposals(final AssistInvocationContext context,
			final int mode, final AssistProposalCollector proposals,
			final IProgressMonitor monitor) {
		this.resultStatus= null;
		
		if (context instanceof RAssistInvocationContext) {
			computeCompletionProposals((RAssistInvocationContext) context, mode, proposals,
					monitor );
		}
		
		return this.resultStatus;
	}
	
	@Override
	public IStatus computeInformationProposals(final AssistInvocationContext context,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		this.resultStatus= null;
		
		if (context instanceof RAssistInvocationContext) {
			doComputeContextProposals((RAssistInvocationContext) context, proposals, monitor);
		}
		
		return this.resultStatus;
	}
	
	protected void computeCompletionProposals(final RAssistInvocationContext context, final int mode,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		if (context.getModelInfo() == null) {
			return;
		}
		
		// Get node
		final RAstNode node= getRAstNode(context);
		
		// Get prefix
		final RElementName prefixName= context.getIdentifierElementName();
		if (prefixName == null) {
			return;
		}
		
		this.inString= (context.getInvocationContentType() == IRDocumentConstants.R_STRING_CONTENT_TYPE);
		this.inDefault= !this.inString;
		
		this.pkgNamePrio= NA_PRIO;
		this.helpTopicPrio= NA_PRIO;
		
		if (prefixName.getNextSegment() == null) {
			final String help= checkHelp(context);
			if (help != null) {
				doComputeHelpTopicProposals(context, help, ARG_TYPE_PRIO, proposals);
				
				if (prefixName.getScope() == null) {
					doComputePkgNameProposals(context, ARG_TYPE_NO_PRIO, proposals);
				}
				return;
			}
			
			doComputeArgumentProposals(context, proposals, monitor);
			if (this.inDefault) {
				doComputeMainProposals(context, node, proposals, monitor);
			}
			
			if (this.mode == 0 && this.inDefault) {
				doComputeKeywordProposals(context, proposals, monitor);
			}
			
			if (this.mode == 0) {
				if (isPackageName(prefixName)) {
					if (this.pkgNamePrio > 0) {
						doComputePkgNameProposals(context, this.pkgNamePrio, proposals);
					}
					else if (!prefixName.getSegmentName().isEmpty()) {
						doComputePkgNameProposals(context, ARG_TYPE_NO_PRIO, proposals);
					}
				}
				{	if (this.helpTopicPrio > 0) {
						doComputeHelpTopicProposals(context, null, this.helpTopicPrio, proposals);
					}
				}
			}
		}
		else {
			doComputeSubProposals(context, node, proposals, monitor);
		}
	}
	
	
	private String checkHelp(final RAssistInvocationContext context) {
		try {
			if (context.getIdentifierOffset() > 0
					&& context.getDocument().getChar(context.getIdentifierOffset() - 1) == '?') {
				final String prefix= context.computeIdentifierPrefix(context.getIdentifierOffset() - 1);
				if (prefix != null && !prefix.isEmpty()) {
					if (prefix.equals("class") || prefix.equals("methods")) { //$NON-NLS-1$ //$NON-NLS-2$
						return prefix;
					}
					return null;
				}
				return ""; //$NON-NLS-1$
			}
			return null;
		}
		catch (final BadPartitioningException | BadLocationException e) {
			return null;
		}
	}
	
	protected List<? extends IRLangElement> getChildren(final RAssistInvocationContext context,
			IRLangElement e) {
		if (e instanceof RReference) {
			final RReference ref= (RReference) e;
			final RObject rObject= ref.getResolvedRObject();
			if (rObject == null && e instanceof ICombinedRElement && context.getTool() != null) {
				context.getToolReferencesUtil().resolve(ref, 0);
			}
			if (rObject instanceof ICombinedRElement) {
				e= (ICombinedRElement) rObject;
			}
		}
		return e.getModelChildren(null);
	}
	
	protected boolean doComputeArgumentProposals(final RAssistInvocationContext context,
			final AssistProposalCollector proposals,
			final IProgressMonitor monitor) {
		final FCallInfo fCallInfo= context.getFCallInfo();
		if (fCallInfo != null) {
			boolean argName= false;
			boolean argValue= false;
			final int argIdx= fCallInfo.getArgIdx(context.getInvocationOffset());
			if (argIdx >= 0) {
				final FCall.Arg arg= fCallInfo.getArg(argIdx);
				final int argBeginOffset= fCallInfo.getArgBeginOffset(argIdx);
				
				IDocument document= context.getDocument();
				int offsetShift= 0;
				if (document instanceof IFragmentDocument) {
					final IFragmentDocument inputDoc= (IFragmentDocument) document;
					document= inputDoc.getMasterDocument();
					offsetShift= inputDoc.getOffsetInMasterDocument();
				}
				
				if (argBeginOffset != Integer.MIN_VALUE) {
					final RHeuristicTokenScanner scanner= context.getRHeuristicTokenScanner();
					scanner.configure(document, NO_R_COMMENT_CONSTRAINT);
					final int offset= context.getIdentifierOffset();
					if (argBeginOffset == offset
							|| scanner.findNonBlankForward(
									argBeginOffset + offsetShift, offset + offsetShift, true) < 0 ) {
						argName= (this.inDefault
								&& (context.getIdentifierElementName().getScope() == null) );
						argValue= true;
					}
				}
				
				if (!argValue && arg != null && arg.getAssignOffset() != IAstNode.NA_OFFSET) {
					final RHeuristicTokenScanner scanner= context.getRHeuristicTokenScanner();
					scanner.configure(document, NO_R_COMMENT_CONSTRAINT);
					final int offset= context.getIdentifierOffset();
					if (argBeginOffset == offset
							|| scanner.findNonBlankForward(
									arg.getAssignOffset() + offsetShift + 1, offset + offsetShift, true) < 0 ) {
						argValue= true;
					}
				}
			}
			if (argName || argValue) {
				doComputeFCallArgumentProposals(context, fCallInfo, argIdx, argName, argValue,
						proposals);
				return true;
			}
		}
		return false;
	}
	
	protected void doComputeMainProposals(final RAssistInvocationContext context,
			final RAstNode node,
			final AssistProposalCollector proposals,
			final IProgressMonitor monitor) {
		final RElementName prefixName= context.getIdentifierElementName();
		final String prefixSegmentName= prefixName.getSegmentName();
		
		this.searchPath.init(context, node, getSearchMode(context), prefixName.getScope());
		
		final RSymbolComparator.PrefixPattern pattern= new RSymbolComparator.PrefixPattern(prefixSegmentName); 
		final int offset= context.getIdentifierLastSegmentOffset();
		final Set<String> mainNames= new HashSet<>();
		final List<String> methodNames= new ArrayList<>();
		
		for (final RFrameIterator iter= this.searchPath.iterator(); iter.hasNext();) {
			final IRFrame envir= iter.next();
			
			final List<? extends IRElement> elements= envir.getModelChildren(null);
			for (final IRElement element : elements) {
				final RElementName elementName= element.getElementName();
				final int c1type= (element.getElementType() & IModelElement.MASK_C1);
				final boolean isRich= (c1type == IModelElement.C1_METHOD);
				if ((isRich || c1type == IModelElement.C1_VARIABLE)
						&& isCompletable(elementName)
						&& pattern.matches(elementName.getSegmentName())) {
					final int relevance= iter.getRelevance();
					if ((relevance < 0) && !isRich
							&& mainNames.contains(elementName.getSegmentName()) ) {
						continue;
					}
					final IAssistCompletionProposal proposal= createProposal(context,
							offset, elementName, element, relevance );
					if (proposal != null) {
						if (elementName.getNextSegment() == null) {
							if (isRich) {
								methodNames.add(elementName.getSegmentName());
							}
							else {
								mainNames.add(elementName.getSegmentName());
							}
						}
						proposals.add(proposal);
					}
				}
			}
		}
		
		mainNames.addAll(methodNames);
		for (final RFrameIterator iter= this.searchPath.iterator(); iter.hasNext();) {
			final IRFrame envir= iter.next();
			if (envir instanceof IRFrameInSource) {
				final IRFrameInSource sframe= (IRFrameInSource) envir;
				final Set<String> elementNames= sframe.getAllAccessNames();
				for (final String candidate : elementNames) {
					if (candidate != null
							&& pattern.matches(candidate) 
							&& !mainNames.contains(candidate)
							&& !(candidate.equals(prefixSegmentName)
									&& (sframe.getAllAccessOf(candidate, false).size() <= 1) )) {
						final IAssistCompletionProposal proposal= createProposal(context,
								offset, candidate );
						if (proposal != null) {
							mainNames.add(candidate);
							proposals.add(proposal);
						}
					}
				}
			}
		}
	}
	
	private void doComputeKeywordProposals(final RAssistInvocationContext context,
			final AssistProposalCollector proposals,
			final IProgressMonitor monitor) {
		final RElementName prefixName= context.getIdentifierElementName();
		final String prefixSegmentName= prefixName.getSegmentName();
		
		if (prefixName.getScope() != null) {
			return;
		}
		
		final String prefixSource= context.getIdentifierPrefix();
		if (!prefixSegmentName.isEmpty() && prefixSource.charAt(0) != '`') {
			final int offset= context.getIdentifierOffset();
			final List<String> keywords= fgKeywords;
			for (final String keyword : keywords) {
				if (keyword.regionMatches(true, 0, prefixSegmentName, 0, prefixSegmentName.length())) {
					proposals.add(new RKeywordCompletionProposal(context, keyword, offset));
				}
			}
		}
	}
	
	protected void doComputeSubProposals(final RAssistInvocationContext context,
			final RAstNode node,
			final AssistProposalCollector proposals,
			final IProgressMonitor monitor) {
		final RElementName prefixName= context.getIdentifierElementName();
		
		this.searchPath.init(context, node, getSearchMode(context), prefixName.getScope());
		
		int count= 0;
		final String namePrefix;
		{	RElementName prefixSegment= prefixName;
			while (true) {
				count++;
				if (prefixSegment.getNextSegment() != null) {
					prefixSegment= prefixSegment.getNextSegment();
					continue;
				}
				else {
					break;
				}
			}
			namePrefix= (prefixSegment.getSegmentName() != null) ?
					prefixSegment.getSegmentName() : ""; //$NON-NLS-1$
		}
		final RSymbolComparator.PrefixPattern pattern= new RSymbolComparator.PrefixPattern(namePrefix);
		final int offset= context.getIdentifierLastSegmentOffset();
		
		final Set<String> mainNames= new HashSet<>();
		final List<String> methodNames= new ArrayList<>();
		
		for (final RFrameIterator iter= this.searchPath.iterator(); iter.hasNext();) {
			final IRFrame envir= iter.next();
			
			final List<? extends IRLangElement> elements= envir.getModelChildren(null);
			ITER_ELEMENTS: for (final IRLangElement rootElement : elements) {
				final RElementName elementName= rootElement.getElementName();
				final int c1type= (rootElement.getElementType() & IModelElement.MASK_C1);
				final boolean isRich= (c1type == IModelElement.C1_METHOD);
				if (isRich || c1type == IModelElement.C1_VARIABLE) {
					IRLangElement element= rootElement;
					RElementName prefixSegment= prefixName;
					RElementName elementSegment= elementName;
					ITER_SEGMENTS: for (int i= 0; i < count-1; i++) {
						if (elementSegment == null) {
							final List<? extends IRLangElement> children= getChildren(context, element);
							for (final IRLangElement child : children) {
								elementSegment= child.getElementName();
								if (isCompletable(elementSegment)
										&& elementSegment.getSegmentName().equals(prefixSegment.getSegmentName())) {
									element= child;
									prefixSegment= prefixSegment.getNextSegment();
									elementSegment= elementSegment.getNextSegment();
									continue ITER_SEGMENTS;
								}
							}
							continue ITER_ELEMENTS;
						}
						else {
							if (isCompletable(elementSegment)
									&& elementSegment.getSegmentName().equals(prefixSegment.getSegmentName())) {
								prefixSegment= prefixSegment.getNextSegment();
								elementSegment= elementSegment.getNextSegment();
								continue ITER_SEGMENTS;
							}
							continue ITER_ELEMENTS;
						}
					}
					
					final boolean childMode;
					final List<? extends IRLangElement> children;
					if (elementSegment == null) {
						childMode= true;
						children= getChildren(context, element);
					}
					else {
						childMode= false;
						children= Collections.singletonList(element);
					}
					for (final IRLangElement child : children) {
						if (childMode) {
							elementSegment= child.getElementName();
						}
						final String candidate= elementSegment.getSegmentName();
						final int relevance= iter.getRelevance();
						if (isCompletable(elementSegment)
								&& pattern.matches(candidate) ) {
							if ((relevance > 0) && !isRich
									&& mainNames.contains(candidate) ) {
								continue ITER_ELEMENTS;
							}
							final IAssistCompletionProposal proposal= createProposal(context,
									offset, elementSegment, child, relevance );
							if (proposal != null) {
								if (elementSegment.getNextSegment() == null) {
									if (isRich) {
										methodNames.add(candidate);
									}
									else {
										mainNames.add(candidate);
									}
								}
								proposals.add(proposal);
							}
						}
					}
				}
			}
		}
		
		mainNames.addAll(methodNames);
		for (final RFrameIterator iter= this.searchPath.iterator(); iter.hasNext();) {
			final IRFrame envir= iter.next();
			if (envir instanceof IRFrameInSource) {
				final IRFrameInSource sframe= (IRFrameInSource) envir;
				final List<? extends RElementAccess> allAccess= sframe.getAllAccessOf(
						prefixName.getSegmentName(), true );
				if (allAccess != null) {
					ITER_ELEMENTS: for (final RElementAccess elementAccess : allAccess) {
						RElementAccess elementSegment= elementAccess;
						RElementName prefixSegment= prefixName;
						ITER_SEGMENTS: for (int i= 0; i < count - 1; i++) {
							if (isCompletable(elementSegment)
									&& elementSegment.getSegmentName().equals(prefixSegment.getSegmentName())) {
								prefixSegment= prefixSegment.getNextSegment();
								elementSegment= elementSegment.getNextSegment();
								continue ITER_SEGMENTS;
							}
							continue ITER_ELEMENTS;
						}
						
						if (elementSegment == null || elementSegment.isSlave()) {
							continue ITER_ELEMENTS;
						}
						final String candidate= elementSegment.getSegmentName();
						if (candidate != null && isCompletable(elementSegment)
								&& pattern.matches(candidate)
								&& !mainNames.contains(candidate)
								&& !candidate.equals(namePrefix) ) {
							final IAssistCompletionProposal proposal= createProposal(context,
									offset, candidate );
							if (proposal != null) {
								mainNames.add(candidate);
								proposals.add(proposal);
							}
						}
					}
				}
			}
		}
	}
	
	protected IAssistCompletionProposal createProposal(final RAssistInvocationContext context,
			final int offset, final String name) {
		return new RSimpleCompletionProposal(context, name, offset);
	}
	
	protected IAssistCompletionProposal createProposal(final RAssistInvocationContext context,
			final int offset, final RElementName elementName, final IRElement element,
			final int relevance) {
		return new RElementCompletionProposal(context, elementName, offset, element, relevance,
				this.labelProvider );
	}
	
	public void doComputeContextProposals(final RAssistInvocationContext context,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		if (context.getModelInfo() == null) {
			return;
		}
		
		final RAssistInvocationContext.FCallInfo fCallInfo= context.getFCallInfo();
		if (fCallInfo != null) {
			final FCallNamePattern pattern= new FCallNamePattern(fCallInfo.getAccess()) {
				
				final int infoOffset= Math.max(fCallInfo.getNode().getArgsOpenOffset() + 1, 0);
				
				@Override
				protected void handleMatch(final IRMethod element, final IRFrame frame,
						final RFrameIterator iterator) {
					proposals.add(new RElementCompletionProposal.ContextInformationProposal(context,
									element.getElementName(), this.infoOffset, element,
									iterator.getRelevance(),
									RElementsCompletionComputer.this.labelProvider ));
				}
			};
			pattern.searchFDef(fCallInfo.getSearchPath(getSearchMode(context)));
		}
	}
	
	
	protected final boolean isPackageArg(final String name) {
		return ("package".equals(name));
	}
	
	private void doComputeFCallArgumentProposals(final RAssistInvocationContext context,
			final FCallInfo fCallInfo, final int argIdx, final boolean argName, final boolean argValue,
			final AssistProposalCollector proposals) {
		final RElementName prefixName= context.getIdentifierElementName();
		final String prefixSegmentName= prefixName.getSegmentName();
		
		final int offset= context.getIdentifierOffset();
		
		if (argValue && !argName) {
			final FCall.Arg arg= fCallInfo.getArg(argIdx);
			if (arg != null && arg.hasName()) {
				if (this.inString && isPackageArg(arg.getNameChild().getText())) {
					this.pkgNamePrio= ARG_TYPE_PRIO;
				}
			}
		}
		
		class FCallHandler extends FCallNamePattern {
			
			private final RCoreFunctions coreFunction;
			
			private final HashSet<String> argNames= new HashSet<>();
			
			private int matchCount;
			
			
			FCallHandler() {
				super(fCallInfo.getAccess());
				
				if (fCallInfo.getAccess().getNextSegment() == null) {
					this.coreFunction= RCoreFunctions.DEFAULT;
				}
				else {
					this.coreFunction= null;
				}
			}
			
			
			private boolean isValidNameContext(final ArgsDefinition.Arg argDef) {
				if (RElementsCompletionComputer.this.inString) {
					return ((argDef.type & ArgsDefinition.NAME_AS_STRING) != 0);
				}
				else {
					return ((argDef.type & ArgsDefinition.NAME_AS_SYMBOL) != 0);
				}
			}
			
			private boolean checkArgsDef(final FCallArgMatch args,
					final boolean guess, final int relevance) {
				final ArgsDefinition.Arg argDef= args.getArgDef(argIdx);
				if (argDef != null) {
					final boolean typedDef= (argDef.type != 0);
					if (RElementsCompletionComputer.this.pkgNamePrio == NA_PRIO) {
						if (typedDef) {
							if ((argDef.type & ArgsDefinition.PACKAGE_NAME) != 0
									&& isValidNameContext(argDef) ) {
								RElementsCompletionComputer.this.pkgNamePrio= ARG_TYPE_PRIO + relevance;
							}
						}
						else if (guess && RElementsCompletionComputer.this.inString && isPackageArg(argDef.name)) {
							RElementsCompletionComputer.this.pkgNamePrio= ARG_TYPE_PRIO + relevance;
						}
					}
					if (RElementsCompletionComputer.this.helpTopicPrio == NA_PRIO) {
						if (typedDef) {
							if ((argDef.type & ArgsDefinition.HELP_TOPIC_NAME) != 0
									&& isValidNameContext(argDef) ) {
								RElementsCompletionComputer.this.helpTopicPrio= ARG_TYPE_PRIO + relevance;
							}
						}
					}
					return typedDef;
				}
				return false;
			}
			
			@Override
			public void searchFDef(final RFrameSearchPath searchPath) {
				super.searchFDef(searchPath);
				
				if (this.matchCount == 0
						&& this.coreFunction != null) {
					final FCall.Args callArgs= fCallInfo.getNode().getArgsChild();
					
					final ArgsDefinition coreDef= this.coreFunction.getArgs(
							getElementName().getSegmentName() );
					if (coreDef != null) {
						checkArgsDef(RAst.matchArgs(callArgs, coreDef), false, 0);
					}
				}
			}
			
			@Override
			protected void handleMatch(final IRMethod element, final IRFrame frame,
					final RFrameIterator iterator) {
				final ArgsDefinition argsDef= element.getArgsDefinition();
				if (argsDef == null) {
					return;
				}
				
				this.matchCount++;
				
				final int relevance= iterator.getRelevance();
				if (argName) {
					for (int i= 0; i < argsDef.size(); i++) {
						final ArgsDefinition.Arg arg= argsDef.get(i);
						if (arg.name != null && arg.name.length() > 0 && !arg.name.equals("...")) {
							if ((prefixSegmentName == null || arg.name.startsWith(prefixSegmentName))
									&& this.argNames.add(arg.name)) {
								final RElementName name= RElementName.create(RElementName.MAIN_DEFAULT, arg.name);
								proposals.add(new RElementCompletionProposal.ArgumentProposal(
										context, name, offset, element,
										ARG_NAME_PRIO + relevance,
										RElementsCompletionComputer.this.labelProvider ));
							}
						}
					}
				}
				if (argValue) {
					final FCall.Args callArgs= fCallInfo.getNode().getArgsChild();
					if (!checkArgsDef(RAst.matchArgs(callArgs, argsDef), true, relevance)
							&& frame.getFrameType() == IRFrame.PACKAGE
							&& this.coreFunction != null
							&& this.coreFunction.getPackageNames().contains(frame.getElementName().getSegmentName()) ) {
						final ArgsDefinition coreDef= this.coreFunction.getArgs(
								getElementName().getSegmentName() );
						if (coreDef != null) {
							checkArgsDef(RAst.matchArgs(callArgs, coreDef), false, relevance);
						}
					}
				}
			}
			
		};
		final FCallHandler search= new FCallHandler();
		search.searchFDef(fCallInfo.getSearchPath(getSearchMode(context)));
	}
	
	
	protected IRCoreAccess getRCoreAccess() {
		return this.editor.getRCoreAccess();
	}
	
	protected final IRPkgSet getRPkgSet() {
		final IRPkgManager manager= RCore.getRPkgManager(getRCoreAccess().getREnv());
		manager.getReadLock().lock();
		try {
			return manager.getRPkgSet();
		}
		finally {
			manager.getReadLock().unlock();
		}
	}
	
	protected final boolean isPackageName(final RElementName elementName) {
		return ((elementName.getType() == RElementName.MAIN_DEFAULT 
						|| RElementName.isPackageFacetScopeType(elementName.getType()) )
				&& elementName.getNextSegment() == null );
	}
	
	protected final void doComputePkgNameProposals(final RAssistInvocationContext context,
			final int prio, final AssistProposalCollector proposals) {
		final RElementName prefixName= context.getIdentifierElementName();
		final String prefixSegmentName= prefixName.getSegmentName();
		
		if (prefixName.getScope() != null
				|| !isSymbolCandidate(prefixSegmentName)) {
			return;
		}
		
		final IRPkgSet rPkgSet= getRPkgSet();
		
		final PrefixPattern pattern= new RSymbolComparator.PrefixPattern(prefixSegmentName);
		final int offset= context.getInvocationOffset() - prefixSegmentName.length();
		
		final Collection<String> envNames;
		if (rPkgSet != null) {
			final IRPkgCollection<? extends IRPkgInfo> pkgs= rPkgSet.getInstalled();
			envNames= pkgs.getNames();
			for (final String pkgName : envNames) {
				if (pattern.matches(pkgName)) {
					proposals.add(new RElementCompletionProposal.RPkgProposal(context, 
							RElementName.create(RElementName.SCOPE_PACKAGE, pkgName), offset,
							pkgs.getFirstByName(pkgName), prio ));
				}
			}
		}
		else {
			envNames= ImCollections.emptySet();
		}
		
		final Set<String> workspaceNames= RModel.getRModelManager().getPkgNames();
		for (final String pkgName : workspaceNames) {
			if (!envNames.contains(pkgName) && pattern.matches(pkgName)) {
				proposals.add(new RElementCompletionProposal.RPkgProposal(context, 
						RElementName.create(RElementName.SCOPE_PACKAGE, pkgName), offset,
						null, prio ));
			}
		}
	}
	
	private List<String> getHelpSearchPackages(final RAssistInvocationContext context,
			final boolean onlyLoaded) {
		final RElementName namespace= context.getIdentifierElementName().getScope();
		if (namespace != null && namespace.getSegmentName() != null) {
			return ImCollections.newList(namespace.getSegmentName());
		}
		if (onlyLoaded) {
			final RProcess tool= context.getTool();
			if (tool != null) {
				final RWorkspace runtimeWorkspace= tool.getWorkspaceData();
				if (runtimeWorkspace != null) {
					final List<? extends ICombinedREnvironment> list= runtimeWorkspace.getRSearchEnvironments();
					if (list != null) {
						final List<String> names= new ArrayList<>(list.size() - 1);
						for (final ICombinedREnvironment envir : list) {
							if (envir instanceof IRFrame) {
								final IRFrame frame= (IRFrame) envir;
								if (frame.getFrameType() == IRFrame.PACKAGE) {
									names.add(frame.getElementName().getSegmentName());
								}
							}
						}
						return names;
					}
				}
			}
		}
		return null;
	}
	
	protected final void doComputeHelpTopicProposals(final RAssistInvocationContext context,
			final String topicType,
			final int prio, final AssistProposalCollector proposals) {
		// (topic != null) => ?  /  (topic == null) => help()
		final RElementName prefixName= context.getIdentifierElementName();
		final String prefixSegmentName= prefixName.getSegmentName();
		
		if (topicType == null && prefixName.getScope() != null) {
			return;
		}
		
		final IREnv rEnv= getRCoreAccess().getREnv();
		if (rEnv == null) {
			return;
		}
		
		final List<String> packages= getHelpSearchPackages(context, topicType != null);
		if (packages == null && (topicType != null || prefixSegmentName.isEmpty())) {
			return;
		}
		
		final IRHelpManager rHelpManager= RCore.getRHelpManager();
		
		final IREnvHelp help= rHelpManager.getHelp(rEnv);
		if (help != null) {
			try {
				final PrefixPattern pattern= new RSymbolComparator.PrefixPattern(prefixSegmentName);
				final Map<String, Object> map= new HashMap<>();
				help.searchTopics(
						(prefixSegmentName != null && !prefixSegmentName.isEmpty()) ?
								prefixSegmentName.substring(0, 1) : null,
						(topicType != null && !topicType.isEmpty()) ?
								topicType : null,
						packages, new IREnvHelp.ITopicSearchRequestor() {
							@Override
							public void matchFound(final String topic, final String packageName) {
								if (pattern.matches(topic)) {
									final Object prev= map.put(topic, packageName);
									if (prev != null) {
										List<String> list;
										if (prev instanceof List) {
											list= (List<String>) prev;
										}
										else {
											list= new ArrayList<>(4);
											list.add((String) prev);
										}
										final int idx= Collections.binarySearch(list, packageName);
										if (idx < 0) {
											list.add(-(idx + 1), packageName);
											map.put(topic, list);
										}
									}
								}
							}
						});
				
				final int offset= (context.getInvocationContentType() != IRDocumentConstants.R_DEFAULT_CONTENT_TYPE) ?
						context.getIdentifierLastSegmentOffset() + 1 :
						context.getIdentifierLastSegmentOffset();
				for (final Map.Entry<String, Object> match : map.entrySet()) {
					proposals.add(new RSimpleCompletionProposal.RHelpTopicCompletionProposal(
							context, match.getKey(), match.getValue(), offset, prio ));
				}
			}
			finally {
				help.unlock();
			}
		}
	}
	
}
