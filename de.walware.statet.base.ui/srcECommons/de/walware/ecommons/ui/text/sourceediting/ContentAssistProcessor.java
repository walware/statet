/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - adapted API and improvements
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ibm.icu.text.Collator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.ecommons.internal.ui.text.EditingMessages;


/**
 * A content assist processor that aggregates the proposals of the
 * {@link IContentAssistComputer}s contributed via a extension point
 * (depends on editor).
 * <p>
 * Subclasses may extend:
 * <ul>
 * <li><code>createContext</code> to provide the context object passed to the computers</li>
 * <li><code>createProgressMonitor</code> to change the way progress is reported</li>
 * <li><code>filterAndSort</code> to add sorting and filtering</li>
 * <li><code>getContextInformationValidator</code> to add context validation (needed if any
 * contexts are provided)</li>
 * <li><code>getErrorMessage</code> to change error reporting</li>
 * </ul>
 * </p>
 * 
 */
public class ContentAssistProcessor implements IContentAssistProcessor {
	
	
	private static final boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("de.walware.ecommons.ui/debug/ResultCollector")); //$NON-NLS-1$ //$NON-NLS-2$
	
	private static final Collator NAME_COLLATOR = Collator.getInstance();
	
	private static final Comparator<IAssistCompletionProposal> PROPOSAL_COMPARATOR = new Comparator<IAssistCompletionProposal>() {
		
		public int compare(final IAssistCompletionProposal proposal1, final IAssistCompletionProposal proposal2) {
			final int diff = proposal2.getRelevance() - proposal1.getRelevance();
			if (diff != 0) {
				return diff; // reverse
			}
			return NAME_COLLATOR.compare(proposal1.getSortingString(), proposal2.getSortingString());
		}
		
	};
	
	
	/**
	 * The completion listener class for this processor.
	 */
	private final class CompletionListener implements ICompletionListener, ICompletionListenerExtension {
		
		public void assistSessionStarted(final ContentAssistEvent event) {
			if (event.processor != ContentAssistProcessor.this || event.assistant != fAssistant) {
				return;
			}
			
			final KeySequence binding = getIterationBinding();
			fAvailableCategories = fComputerRegistry.getCategories();
			fIterationGesture = createIterationGesture(binding);
			fCategoryIteration = createCategoryIteration();
			
			for (final ContentAssistCategory category : fAvailableCategories) {
				final List<IContentAssistComputer> computers = category.getComputers(fPartition);
				for (final IContentAssistComputer computer : computers) {
					computer.sessionStarted(fEditor);
				}
			}
			
			fRepetition = 0;
			
			if (fCategoryIteration.size() == 1) {
				fAssistant.setRepeatedInvocationMode(false);
				fAssistant.setShowEmptyList(false);
			}
			else {
				fAssistant.setRepeatedInvocationMode(true);
				fAssistant.setStatusLineVisible(true);
				fAssistant.setStatusMessage(createIterationMessage(0));
				fAssistant.setShowEmptyList(true);
				if (fAssistant instanceof IContentAssistantExtension3) {
					final IContentAssistantExtension3 ext3= (IContentAssistantExtension3) fAssistant;
					((ContentAssistant) ext3).setRepeatedInvocationTrigger(binding);
				}
			}
		}
		
		public void assistSessionEnded(final ContentAssistEvent event) {
			if (event.processor != ContentAssistProcessor.this || event.assistant != fAssistant) {
				return;
			}
			
			if (fAvailableCategories != null) {
				for (final ContentAssistCategory category : fAvailableCategories) {
					final List<IContentAssistComputer> computers = category.getComputers(fPartition);
					for (final IContentAssistComputer computer : computers) {
						computer.sessionEnded();
					}
				}
			}
			fAvailableCategories = null;
			fCategoryIteration = null;
			fRepetition = -1;
			fIterationGesture = null;
			
			fAssistant.setShowEmptyList(false);
			fAssistant.setRepeatedInvocationMode(false);
			fAssistant.setStatusLineVisible(false);
			if (fAssistant instanceof IContentAssistantExtension3) {
				final IContentAssistantExtension3 ext3 = (IContentAssistantExtension3) fAssistant;
				((ContentAssistant) ext3).setRepeatedInvocationTrigger(null);
			}
		}
		
		public void selectionChanged(final ICompletionProposal proposal, final boolean smartToggle) {
		}
		
		public void assistSessionRestarted(final ContentAssistEvent event) {
			fRepetition = 0;
		}
		
	}
	
	
	/**
	 * The completion proposal registry.
	 */
	private ContentAssistComputerRegistry fComputerRegistry;
	
	private final String fPartition;
	private final ContentAssist fAssistant;
	private final ISourceEditor fEditor;
	
	private char[] fCompletionAutoActivationCharacters;
	
	/* cycling stuff */
	private int fRepetition = -1;
	private List<ContentAssistCategory> fAvailableCategories;
	private List<List<ContentAssistCategory>> fCategoryIteration;
	private String fIterationGesture = null;
	private int fNumberOfComputedResults = 0;
	private String fErrorMessage;
	
	private IContextInformationValidator fContextInformationValidator;
	
	/* for detection if information mode is valid */
	private long fInformationModeTimestamp;
	private long fInformationModeModificationStamp;
	private int fInformationModeOffset;
	
	
	public ContentAssistProcessor(final ContentAssist assistant, final String partition, final ContentAssistComputerRegistry registry, final ISourceEditor editor) {
		assert(assistant != null);
		assert(partition != null);
		assert(registry != null);
		assert(editor != null);
		
		fPartition = partition;
		fComputerRegistry = registry;
		fEditor = editor;
		fAssistant = assistant;
		fAssistant.enableColoredLabels(true);
		fAssistant.addCompletionListener(new CompletionListener());
	}
	
	
	public String getPartition() {
		return fPartition;
	}
	
	protected ISourceEditor getEditor() {
		return fEditor;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		final long start = System.currentTimeMillis();
		
		clearState();
		
		final AssistInvocationContext context = createCompletionProposalContext(offset);
		final long setup = DEBUG ? System.currentTimeMillis() : 0L;
		
		final long modificationStamp = ((AbstractDocument) context.getSourceViewer().getDocument()).getModificationStamp();
		final int mode;
		if (fComputerRegistry.isInSpecificMode()) {
			mode = IContentAssistComputer.SPECIFIC_MODE;
		}
		else if (!fAssistant.isProposalPopupActive1()
				&& (   start-fInformationModeTimestamp > 3000 
					|| offset != fInformationModeOffset
					|| !fAssistant.isContextInfoPopupActive1()
					|| modificationStamp != fInformationModeModificationStamp)
				&& forceContextInformation(context)) {
			mode = IContentAssistComputer.INFORMATION_MODE;
			fAssistant.setShowEmptyList(false);
			fAssistant.setStatusLineVisible(true);
//			fAssistant.setStatusMessage(createIterationMessage(-1));
			fAssistant.setStatusMessage(EditingMessages.ContentAssistProcessor_ContextSelection_label);
		}
		else if (fRepetition > 0) {
				mode = IContentAssistComputer.SPECIFIC_MODE;
		}
		else {
			mode = IContentAssistComputer.COMBINED_MODE;
		}
		
		final List<ContentAssistCategory> categories = (mode == IContentAssistComputer.INFORMATION_MODE) ?
				fAvailableCategories : getCurrentCategories();
		
		final SubMonitor progress = SubMonitor.convert(createProgressMonitor());
		progress.beginTask(EditingMessages.ContentAssistProcessor_ComputingProposals_task, categories.size() + 1);
		progress.subTask((mode != IContentAssistComputer.INFORMATION_MODE) ?
				EditingMessages.ContentAssistProcessor_ComputingProposals_Collecting_task :
				EditingMessages.ContentAssistProcessor_ComputingContexts_Collecting_task);
		final List<IAssistCompletionProposal> proposals = collectProposals(context, mode, categories, progress);
		final long collect = DEBUG ? System.currentTimeMillis() : 0L;
		
		progress.subTask((mode != IContentAssistComputer.INFORMATION_MODE) ?
				EditingMessages.ContentAssistProcessor_ComputingProposals_Sorting_task :
				EditingMessages.ContentAssistProcessor_ComputingContexts_Sorting_task);
		final List<IAssistCompletionProposal> filtered = filterAndSortProposals(proposals, context, progress);
		final long filter = DEBUG ? System.currentTimeMillis() : 0L;
		
		fNumberOfComputedResults = filtered.size();
		final ICompletionProposal[] result = filtered.toArray(new ICompletionProposal[fNumberOfComputedResults]);
		progress.done();
		
		if (mode == IContentAssistComputer.INFORMATION_MODE) {
			if (result.length > 1 && fAssistant.isContextInfoPopupActive1()
					&& !fAssistant.isProposalPopupActive1()) {
				fAssistant.hide1();
			}
			fInformationModeOffset = offset;
			fInformationModeTimestamp = System.currentTimeMillis();
			fInformationModeModificationStamp = modificationStamp;
		}
		
		if (DEBUG) {
			System.err.println("Code Assist Stats (" + result.length + " proposals)"); //$NON-NLS-1$ 
			System.err.println("Code Assist (setup):\t" + (setup - start) ); 
			System.err.println("Code Assist (collect):\t" + (collect - setup) ); 
			System.err.println("Code Assist (sort):\t" + (filter - collect) ); 
		}
		
		return result;
	}
	
	private void clearState() {
		fErrorMessage = null;
		fNumberOfComputedResults = 0;
	}
	
	/**
	 * Collects the proposals.
	 * 
	 * @param viewer the text viewer
	 * @param offset the offset
	 * @param monitor the progress monitor
	 * @param context the code assist invocation context
	 * @return the list of proposals
	 */
	private List<IAssistCompletionProposal> collectProposals(final AssistInvocationContext context, final int mode,
			final List<ContentAssistCategory> categories, final SubMonitor progress) {
		final List<IAssistCompletionProposal> proposals = new ArrayList<IAssistCompletionProposal>();
		for (final ContentAssistCategory category : categories) {
			final List<IContentAssistComputer> computers = category.getComputers(fPartition);
			final SubMonitor computorsProgress = progress.newChild(1);
			for (final IContentAssistComputer computer : computers) {
				computer.computeCompletionProposals(context, mode, proposals, computorsProgress);
			}
		}
		return proposals;
	}
	
	/**
	 * Filters and sorts the proposals. The passed list may be modified
	 * and returned, or a new list may be created and returned.
	 * 
	 * @param proposals the list of collected proposals
	 * @param monitor a progress monitor
	 * @param context 
	 * @return the list of filtered and sorted proposals, ready for display
	 */
	protected List<IAssistCompletionProposal> filterAndSortProposals(final List<IAssistCompletionProposal> proposals, final AssistInvocationContext context, final IProgressMonitor monitor) {
		Collections.sort(proposals, PROPOSAL_COMPARATOR);
		return proposals;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int offset) {
		
		clearState();
		
		final AssistInvocationContext context = createContextInformationContext(offset);
		
		final List<ContentAssistCategory> categories = fComputerRegistry.getCategories();
		
		final SubMonitor progress = SubMonitor.convert(createProgressMonitor());
		progress.beginTask(EditingMessages.ContentAssistProcessor_ComputingContexts_task, categories.size() + 1);
		progress.subTask(EditingMessages.ContentAssistProcessor_ComputingContexts_Collecting_task);
		final IContextInformation info = collectContextInformation(context, categories, progress);
		
		final IContextInformation[] result;
		if (info != null) {
			fNumberOfComputedResults = 1;
			return new IContextInformation[] { info };
		}
		else {
			fNumberOfComputedResults = 0;
			result = null;
		}
		progress.done();
		
		return result;
	}
	
	private IContextInformation collectContextInformation(final AssistInvocationContext context, final List<ContentAssistCategory> categories, final SubMonitor progress) {
		final List<IAssistInformationProposal> infos = new ArrayList<IAssistInformationProposal>();
		for (final ContentAssistCategory category : categories) {
			final List<IContentAssistComputer> computers = category.getComputers(fPartition);
			final SubMonitor computersProgress = progress.newChild(1);
			for (final IContentAssistComputer computer : computers) {
				final IStatus status = computer.computeContextInformation(context, infos, computersProgress);
				if ((status != null && status.getSeverity() > IStatus.WARNING)
						|| infos.size() > 1) {
					return null;
				}
				if (infos.size() == 1) {
					return infos.get(0);
				}
			}
		}
		return null;
	}
	
	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 *
	 * @param activationSet the activation set
	 */
	public final void setCompletionProposalAutoActivationCharacters(final char[] activationSet) {
		fCompletionAutoActivationCharacters = activationSet;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final char[] getCompletionProposalAutoActivationCharacters() {
		return fCompletionAutoActivationCharacters;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getErrorMessage() {
		return fErrorMessage;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This implementation returns the validator created by
	 * {@link #createContextInformationValidator()}
	 */
	public final IContextInformationValidator getContextInformationValidator() {
		if (fContextInformationValidator == null) {
			fContextInformationValidator = createContextInformationValidator();
		}
		return fContextInformationValidator;
	}
	
	protected IContextInformationValidator createContextInformationValidator() {
		return null;
	}
	
	/**
	 * Creates a progress monitor.
	 * <p>
	 * The default implementation creates a
	 * <code>NullProgressMonitor</code>.
	 * </p>
	 * 
	 * @return a progress monitor
	 */
	protected IProgressMonitor createProgressMonitor() {
		return new NullProgressMonitor();
	}
	
	/**
	 * Creates the context that is passed to the completion proposal
	 * computers.
	 * 
	 * @param offset the content assist offset
	 * @return the context to be passed to the computers
	 */
	protected AssistInvocationContext createCompletionProposalContext(final int offset) {
		return new AssistInvocationContext(getEditor(), offset, 0);
	}
	
	/**
	 * Creates the context that is passed to the completion proposal
	 * computers.
	 * 
	 * @param offset the content assist offset
	 * @return the context to be passed to the computers
	 */
	protected AssistInvocationContext createContextInformationContext(final int offset) {
		return new AssistInvocationContext(getEditor(), offset, 0);
	}
	
	protected boolean forceContextInformation(final AssistInvocationContext context) {
		return false;
	}
	
	private List<ContentAssistCategory> getCurrentCategories() {
		if (fCategoryIteration == null)
			return fAvailableCategories;
		
		final int iteration = fRepetition % fCategoryIteration.size();
		fAssistant.setStatusMessage(createIterationMessage(fRepetition));
		fAssistant.setEmptyMessage(createEmptyMessage());
		fRepetition++;
		
		return fCategoryIteration.get(iteration);
	}
	
	private List<List<ContentAssistCategory>> createCategoryIteration() {
		final List<List<ContentAssistCategory>> sequence = new ArrayList<List<ContentAssistCategory>>(fAvailableCategories.size());
		sequence.add(createDefaultCategories());
		for (final ContentAssistCategory category : createSeparateCategories()) {
			sequence.add(Collections.singletonList(category));
		}
		return sequence;
	}
	
	private List<ContentAssistCategory> createDefaultCategories() {
		final List<ContentAssistCategory> included = new ArrayList<ContentAssistCategory>(fAvailableCategories.size());
		for (final ContentAssistCategory category : fAvailableCategories) {
			if (category.isEnabledInDefault() && category.hasComputers(fPartition)) {
				included.add(category);
			}
		}
		return included;
	}
	
	private List<ContentAssistCategory> createSeparateCategories() {
		final ArrayList<ContentAssistCategory> sorted = new ArrayList<ContentAssistCategory>(fAvailableCategories.size());
		for (final ContentAssistCategory category : fAvailableCategories) {
			if (category.isEnabledInCircling() && category.hasComputers(fPartition)) {
				sorted.add(category);
			}
		}
		return sorted;
	}
	
	protected String createEmptyMessage() {
		return NLS.bind(EditingMessages.ContentAssistProcessor_Empty_message, new String[] { 
				getCategoryName(fRepetition)});
	}
	
	protected String createIterationMessage(final int repetition) {
		return NLS.bind(EditingMessages.ContentAssistProcessor_ToggleAffordance_message, new String[] { 
				getCategoryName(repetition), fIterationGesture, getCategoryName(repetition + 1) });
	}
	
	protected String getCategoryName(final int repetition) {
		if (repetition < 0) {
			return EditingMessages.ContentAssistProcessor_ContextSelection_label;
		}
		final int iteration = repetition % fCategoryIteration.size();
		if (iteration == 0) {
			return EditingMessages.ContentAssistProcessor_DefaultProposalCategory;
		}
		return fCategoryIteration.get(iteration).get(0).getDisplayName();
	}
	
	private String createIterationGesture(final KeySequence binding) {
		return (binding != null) ? 
				NLS.bind(EditingMessages.ContentAssistProcessor_ToggleAffordance_PressGesture_message, new String[] { 
						binding.format() }) :
				EditingMessages.ContentAssistProcessor_ToggleAffordance_ClickGesture_message;
	}
	
	private KeySequence getIterationBinding() {
		final IBindingService bindingSvc = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		final TriggerSequence binding = bindingSvc.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		if (binding instanceof KeySequence) {
			return (KeySequence) binding;
		}
		return null;
	}
	
}
