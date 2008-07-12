/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - adapted API and improvements
 *******************************************************************************/

package de.walware.eclipsecommons.ui.text.sourceediting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ibm.icu.text.Collator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.eclipsecommons.internal.ui.text.EditingMessages;


/**
 * A content assist processor that aggregates the proposals of the
 * {@link IJavaCompletionProposalComputer}s contributed via the
 * <code>org.eclipse.jdt.ui.javaCompletionProposalComputer</code> extension point.
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
	
	
	private static final boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("de.walware.eclipsecommons.ui/debug/ResultCollector")); //$NON-NLS-1$ //$NON-NLS-2$
	
	private static final Comparator<ContentAssistCategory> CATEGORY_COMPARATOR = new Comparator<ContentAssistCategory>() {
		
		public int compare(final ContentAssistCategory c1, final ContentAssistCategory c2) {
			return c1.getSortOrder() - c2.getSortOrder();
		}
		
	};
	
	private static final Collator NAME_COLLATOR = Collator.getInstance();
	
	private static final Comparator<ICompletionProposal> PROPOSAL_COMPARATOR = new Comparator<ICompletionProposal>() {
		
		public int compare(final ICompletionProposal proposal1, final ICompletionProposal proposal2) {
			final int p1 = (proposal1 instanceof IRatedProposal) ? ((IRatedProposal) proposal1).getRelevance() : 30;
			final int p2 = (proposal2 instanceof IRatedProposal) ? ((IRatedProposal) proposal2).getRelevance() : 30;
			if (p1 != p2) {
				return p2 - p1; // reverse
			}
			return NAME_COLLATOR.compare(proposal1.getDisplayString(), proposal2.getDisplayString());
		}
		
	};
	
	
	/**
	 * The completion listener class for this processor.
	 * 
	 * @since 3.4
	 */
	private final class CompletionListener implements ICompletionListener, ICompletionListenerExtension {
		
		public void assistSessionStarted(final ContentAssistEvent event) {
			if (event.processor != ContentAssistProcessor.this)
				return;
			
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
			if (event.assistant instanceof IContentAssistantExtension2) {
				final IContentAssistantExtension2 extension = (IContentAssistantExtension2) event.assistant;
				
				if (fCategoryIteration.size() == 1) {
					extension.setRepeatedInvocationMode(false);
					extension.setShowEmptyList(false);
				}
				else {
					extension.setRepeatedInvocationMode(true);
					extension.setStatusLineVisible(true);
					extension.setStatusMessage(createIterationMessage());
					extension.setShowEmptyList(true);
					if (extension instanceof IContentAssistantExtension3) {
						final IContentAssistantExtension3 ext3= (IContentAssistantExtension3) extension;
						((ContentAssistant) ext3).setRepeatedInvocationTrigger(binding);
					}
				}
			}
		}
		
		public void assistSessionEnded(final ContentAssistEvent event) {
			if (event.processor != ContentAssistProcessor.this)
				return;
			
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
			if (event.assistant instanceof IContentAssistantExtension2) {
				final IContentAssistantExtension2 extension = (IContentAssistantExtension2) event.assistant;
				extension.setShowEmptyList(false);
				extension.setRepeatedInvocationMode(false);
				extension.setStatusLineVisible(false);
				if (extension instanceof IContentAssistantExtension3) {
					final IContentAssistantExtension3 ext3 = (IContentAssistantExtension3) extension;
					((ContentAssistant) ext3).setRepeatedInvocationTrigger(null);
				}
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
	private final ContentAssistant fAssistant;
	private final ISourceEditor fEditor;
	
	private char[] fCompletionAutoActivationCharacters;
	
	/* cycling stuff */
	private int fRepetition = -1;
	private List<ContentAssistCategory> fAvailableCategories;
	private List<List<ContentAssistCategory>> fCategoryIteration;
	private String fIterationGesture = null;
	private int fNumberOfComputedResults = 0;
	private String fErrorMessage;
	
	
	public ContentAssistProcessor(final ContentAssistant assistant, final String partition, final ContentAssistComputerRegistry registry, final ISourceEditor editor) {
		assert(assistant != null);
		assert(partition != null);
		assert(registry != null);
		assert(editor != null);
		
		fPartition = partition;
		fComputerRegistry = registry;
		fEditor = editor;
		fAssistant = assistant;
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
		final long start = DEBUG ? System.currentTimeMillis() : 0;
		
		clearState();
		final SubMonitor progress = SubMonitor.convert(createProgressMonitor());
		progress.beginTask(EditingMessages.ContentAssistProcessor_ComputingProposals_task, fAvailableCategories.size() + 1);
		
		final AssistInvocationContext context = createContext(offset);
		final long setup = DEBUG ? System.currentTimeMillis() : 0;
		
		progress.subTask(EditingMessages.ContentAssistProcessor_ComputingProposals_Collecting_task);
		final List<ICompletionProposal> proposals = collectProposals(context, progress);
		final long collect = DEBUG ? System.currentTimeMillis() : 0;
		
		progress.subTask(EditingMessages.ContentAssistProcessor_ComputingProposals_Sorting_task);
		final List<ICompletionProposal> filtered = filterAndSortProposals(proposals, context, progress);
		fNumberOfComputedResults = filtered.size();
		final long filter = DEBUG ? System.currentTimeMillis() : 0;
		
		final ICompletionProposal[] result = filtered.toArray(new ICompletionProposal[filtered.size()]);
		progress.done();
		
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
	private List<ICompletionProposal> collectProposals(final AssistInvocationContext context, final SubMonitor progress) {
		final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (final ContentAssistCategory category : getCurrentCategories()) {
			final List<IContentAssistComputer> computers = category.getComputers(fPartition);
			final SubMonitor computorsProgress = progress.newChild(1);
			for (final IContentAssistComputer computer : computers) {
				computer.computeCompletionProposals(context, proposals, computorsProgress);
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
	protected List<ICompletionProposal> filterAndSortProposals(final List<ICompletionProposal> proposals, final AssistInvocationContext context, final IProgressMonitor monitor) {
		Collections.sort(proposals, PROPOSAL_COMPARATOR);
		return proposals;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int offset) {
		clearState();
		
		final SubMonitor progress = SubMonitor.convert(createProgressMonitor());
		progress.beginTask(EditingMessages.ContentAssistProcessor_ComputingContexts_task, getCurrentCategories().size() + 1);
		final AssistInvocationContext context = createContext(offset);
		
		progress.subTask(EditingMessages.ContentAssistProcessor_ComputingContexts_Collecting_task);
		final List<IContextInformation> proposals = collectContextInformation(context, progress);
		
		progress.subTask(EditingMessages.ContentAssistProcessor_ComputingContexts_Sorting_task);
		final List<IContextInformation> filtered = filterAndSortContextInformation(proposals, context, progress);
		fNumberOfComputedResults= filtered.size();
		
		final IContextInformation[] result= filtered.toArray(new IContextInformation[filtered.size()]);
		progress.done();
		return result;
	}
	
	private List<IContextInformation> collectContextInformation(final AssistInvocationContext context, final SubMonitor progress) {
		final List<IContextInformation> proposals = new ArrayList<IContextInformation>();
		for (final ContentAssistCategory category : getCurrentCategories()) {
			final List<IContentAssistComputer> computers = category.getComputers(fPartition);
			final SubMonitor computersProgress = progress.newChild(1);
			for (final IContentAssistComputer computer : computers) {
				computer.computeContextInformation(context, proposals, computersProgress);
			}
		}
		return proposals;
	}
	
	/**
	 * Filters and sorts the context information objects. The passed
	 * list may be modified and returned, or a new list may be created
	 * and returned.
	 * 
	 * @param contexts the list of collected proposals
	 * @param monitor a progress monitor
	 * @return the list of filtered and sorted proposals, ready for display
	 */
	protected List<IContextInformation> filterAndSortContextInformation(final List<IContextInformation> contexts, final AssistInvocationContext context, final IProgressMonitor monitor) {
		return contexts;
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
	 */
	public IContextInformationValidator getContextInformationValidator() {
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
	protected AssistInvocationContext createContext(final int offset) {
		return new AssistInvocationContext(getEditor(), offset);
	}
	
	private List<ContentAssistCategory> getCurrentCategories() {
		if (fCategoryIteration == null)
			return fAvailableCategories;
		
		final int iteration = fRepetition % fCategoryIteration.size();
		fAssistant.setStatusMessage(createIterationMessage());
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
			if (category.isEnabledAsSeparate() && category.hasComputers(fPartition)) {
				sorted.add(category);
			}
		}
		Collections.sort(sorted, CATEGORY_COMPARATOR);
		return sorted;
	}
	
	protected String createEmptyMessage() {
		return NLS.bind(EditingMessages.ContentAssistProcessor_Empty_message, new String[] { 
				getCategoryName(fRepetition)});
	}
	
	protected String createIterationMessage() {
		return NLS.bind(EditingMessages.ContentAssistProcessor_ToggleAffordance_message, new String[] { 
				getCategoryName(fRepetition), fIterationGesture, getCategoryName(fRepetition + 1) });
	}
	
	protected String getCategoryName(final int repetition) {
		final int iteration = repetition % fCategoryIteration.size();
		if (iteration == 0) {
			return EditingMessages.ContentAssistProcessor_defaultProposalCategory;
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
