/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.help;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.ICommandLink;
import org.eclipse.help.IContext;
import org.eclipse.help.IContext3;
import org.eclipse.help.IContextProvider;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.editors.text.TextEditor;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.util.MessageUtil;

import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.rtools.RunHelpInR;
import de.walware.statet.r.internal.ui.rtools.RunHelpSearchInR;


/**
 * Context with context senitive R help.
 */
public class EnrichedRHelpContext implements IContext3 {
	
	
	public static String searchContextInfo(Object target) {
		try {
			String plaintext = null;
			if (target instanceof TextEditor) {
				final TextEditor textEditor = (TextEditor) target;
				plaintext = getPlaintextFromTextSelection(textEditor.getSelectionProvider());
				if (plaintext == null) {
					plaintext = getPlaintextFromDocument(
							textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()),
							textEditor.getSelectionProvider());
				}
			}
			else {
				if (target instanceof IAdaptable) {
					final ISourceEditor editor = (ISourceEditor) ((IAdaptable) target).getAdapter(ISourceEditor.class);
					if (editor != null) {
						target = editor.getViewer();
					}
				}
				if (target instanceof SourceViewer) {
					final SourceViewer sourceViewer = (SourceViewer) target;
					plaintext = getPlaintextFromTextSelection(sourceViewer.getSelectionProvider());
					if (plaintext == null) {
						plaintext = getPlaintextFromDocument(sourceViewer.getDocument(), sourceViewer.getSelectionProvider());
					}
				}
			}
			if (plaintext != null && 
					plaintext.length() < 50 && 
					plaintext.indexOf('\n') < 0 && plaintext.indexOf('\r') < 0) {
				return plaintext;
			}
		}
		catch (final Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error occured when dectecting R element", e); //$NON-NLS-1$
		}
		return null;
	}
	
	private static String getPlaintextFromTextSelection(final ISelectionProvider selectionProvider) {
		final ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection();
		if ( (!textSelection.isEmpty()) && textSelection.getLength() > 0) {
			return textSelection.getText();
		}
		return null;
	}
	
	private static String getPlaintextFromDocument(final IDocument document, final ISelectionProvider selectionProvider) throws BadLocationException {
		final ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection();
		final RHeuristicTokenScanner scanner = new RHeuristicTokenScanner();
		scanner.configure(document);
		final IRegion region = scanner.findRWord(textSelection.getOffset(), false, true);
		if (region != null) {
			return document.get(region.getOffset(), region.getLength());
		}
		return null;
	}
	
	
	public static class Provider implements IContextProvider {
		
		private final IWorkbenchPart3 fPart;
		private final ISourceViewer fSourceViewer;
		private final Object fTarget;
		private final String fContextId;
		
		public Provider(final IWorkbenchPart3 part, final String contextId) {
			fTarget = fPart = part;
			fContextId = contextId;
			
			fSourceViewer = null;
		}
		public Provider(final ISourceViewer sourceViewer, final String contextId) {
			fTarget = fSourceViewer = sourceViewer;
			fContextId = contextId;
			
			fPart = null;
		}
		
		@Override
		public int getContextChangeMask() {
			return SELECTION;
		}
		
		@Override
		public IContext getContext(final Object target) {
			IContext context = HelpSystem.getContext(fContextId);
			final String plaintext = searchContextInfo(fTarget);
			if (context instanceof IContext3 && plaintext != null) {
				context = new EnrichedRHelpContext((IContext3) context, plaintext);
			}
			return context;
		}
		
		@Override
		public String getSearchExpression(final Object target) {
			
			return null;
		}
	}
	
	private static class RHelpResource implements IHelpResource {
		
		private final String fLabel;
		private final String fUrl;
		
		public RHelpResource(final String label, final String url) {
			fLabel = label;
			fUrl = url;
		}
		
		@Override
		public String getLabel() {
			return fLabel;
		}
		
		@Override
		public String getHref() {
			return fUrl;
		}
		
	}	
	
	public static class RHelpCommand extends RHelpResource {
		
		public RHelpCommand(final String label, final String command) {
			super(label, "command://"+command); //$NON-NLS-1$
		}
		
	}
	
	
	private final String fTitle;
	private final String fText;
	private String fStyledText;
	private IHelpResource[] fRelatedTopics;
	private final ICommandLink[] fRelatedCommands;
	
	
	/**
	 * 
	 */
	public EnrichedRHelpContext(final IContext3 context, final String plaintext) {
		fTitle = context.getTitle();
		fText = context.getText();
		fStyledText = context.getStyledText();
		if (fStyledText == null) {
			fStyledText = fText;
		}
		fRelatedTopics = context.getRelatedTopics();
		fRelatedCommands = context.getRelatedCommands();
		
		enrich(plaintext);
	}
	
	private void enrich(final String plaintext) {
		try {
			final List<IHelpResource> resources = new ArrayList<IHelpResource>(fRelatedTopics.length + 1);
			final String urlText = URLEncoder.encode(plaintext, "UTF-8"); //$NON-NLS-1$
			
			resources.add(new RHelpCommand(NLS.bind(Messages.RHelp_Run_Help_label, plaintext), 
					MessageUtil.escapeForFormText(RunHelpInR.createCommandString(plaintext))));
			resources.add(new RHelpCommand(NLS.bind(Messages.RHelp_Run_HelpSearch_label, plaintext), 
					MessageUtil.escapeForFormText(RunHelpSearchInR.createCommandString(plaintext))));
			resources.add(new RHelpResource(NLS.bind(Messages.RHelp_Search_RSiteSearch_label, plaintext), 
					NLS.bind("http://search.r-project.org/cgi-bin/namazu.cgi?query={0}&amp;max=20&amp;result=normal&amp;sort=score&amp;idxname=functions&amp;idxname=docs", urlText) )); //$NON-NLS-1$
			resources.addAll(Arrays.asList(fRelatedTopics));
			fRelatedTopics = resources.toArray(new IHelpResource[resources.size()]);
		}
		catch (final Exception e) {
			RUIPlugin.logError(-1, "Error occured when enrich R help.", e); //$NON-NLS-1$
		}
	}
	
	
	@Override
	public String getTitle() {
		return fTitle;
	}
	
	@Override
	public String getText() {
		return fText;
	}
	
	@Override
	public String getStyledText() {
		return fStyledText;
	}
	
	@Override
	public IHelpResource[] getRelatedTopics() {
		return fRelatedTopics;
	}
	
	@Override
	public String getCategory(final IHelpResource topic) {
		if (topic instanceof RHelpResource) {
			return Messages.RHelp_category;
		}
		return null;
	}
	
	@Override
	public ICommandLink[] getRelatedCommands() {
		return fRelatedCommands;
	}
	
}
