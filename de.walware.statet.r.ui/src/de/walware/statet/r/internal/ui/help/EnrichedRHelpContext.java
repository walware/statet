/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
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

import de.walware.eclipsecommons.ui.util.MessageUtil;

import de.walware.statet.ext.ui.editors.IEditorAdapter;
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
				TextEditor textEditor = (TextEditor) target;
				plaintext = getPlaintextFromTextSelection(textEditor.getSelectionProvider());
				if (plaintext == null) {
					plaintext = getPlaintextFromDocument(
							textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()),
							textEditor.getSelectionProvider());
				}
			}
			else {
				if (target instanceof IAdaptable) {
					IEditorAdapter editor = (IEditorAdapter) ((IAdaptable) target).getAdapter(IEditorAdapter.class);
					if (editor != null) {
						target = editor.getSourceViewer();
					}
				}
				if (target instanceof SourceViewer) {
					SourceViewer sourceViewer = (SourceViewer) target;
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
		catch (Exception e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error occured when dectecting R element", e); //$NON-NLS-1$
		}
		return null;
	}

	private static String getPlaintextFromTextSelection(ISelectionProvider selectionProvider) {
		ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection();
		if ( (!textSelection.isEmpty()) && textSelection.getLength() > 0) {
			return textSelection.getText();
		}
		return null;
	}
	
	private static String getPlaintextFromDocument(IDocument document, ISelectionProvider selectionProvider) throws BadLocationException {
		ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection();
		RHeuristicTokenScanner scanner = new RHeuristicTokenScanner();
		scanner.configure(document, null);
		IRegion region = scanner.findRWord(textSelection.getOffset(), false, true);
		if (region != null) {
			return document.get(region.getOffset(), region.getLength());
		}
		return null;
	}

	
	public static class Provider implements IContextProvider {
		
		private IWorkbenchPart3 fPart;
		private ISourceViewer fSourceViewer;
		private Object fTarget;
		private String fContextId;
		
		public Provider(IWorkbenchPart3 part, String contextId) {
			fTarget = fPart = part;
			fContextId = contextId;
		}
		public Provider(ISourceViewer sourceViewer, String contextId) {
			fTarget = fSourceViewer = sourceViewer;
			fContextId = contextId;
		}
		
		public int getContextChangeMask() {
			return SELECTION;
		}
		
		public IContext getContext(Object target) {
			IContext context = HelpSystem.getContext(fContextId);
			String plaintext = searchContextInfo(fTarget);
			if (context instanceof IContext3 & plaintext != null) {
				context = new EnrichedRHelpContext((IContext3) context, plaintext);
			}
			return context;
		}
		
		public String getSearchExpression(Object target) {

			return null;
		}
	}
	
	private static class RHelpResource implements IHelpResource {

		private String fLabel;
		private String fUrl;

		public RHelpResource(String label, String url) {
			fLabel = label;
			fUrl = url;
		}

		public String getLabel() {
			return fLabel;
		}

		public String getHref() {
			return fUrl;
		}
		
	}	

	public static class RHelpCommand extends RHelpResource {

		public RHelpCommand(String label, String command) {
			super(label, "command://"+command); //$NON-NLS-1$
		}
		
	}
	
	
	private String fTitle;
	private String fText;
	private String fStyledText;
	private IHelpResource[] fRelatedTopics;
	private ICommandLink[] fRelatedCommands;
	
	
	/**
	 * 
	 */
	public EnrichedRHelpContext(IContext3 context, String plaintext) {
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
	
	private void enrich(String plaintext) {
		try {
			List<IHelpResource> resources = new ArrayList<IHelpResource>(fRelatedTopics.length + 1);
			String urlText = URLEncoder.encode(plaintext, "UTF-8"); //$NON-NLS-1$

			resources.add(new RHelpCommand(NLS.bind(Messages.RHelp_Run_Help_label, plaintext), 
					MessageUtil.escapeForFormText(RunHelpInR.createCommandString(plaintext))));
			resources.add(new RHelpCommand(NLS.bind(Messages.RHelp_Run_HelpSearch_label, plaintext), 
					MessageUtil.escapeForFormText(RunHelpSearchInR.createCommandString(plaintext))));
			resources.add(new RHelpResource(NLS.bind(Messages.RHelp_Search_RSiteSearch_label, plaintext), 
					NLS.bind("http://search.r-project.org/cgi-bin/namazu.cgi?query={0}&amp;max=20&amp;result=normal&amp;sort=score&amp;idxname=functions&amp;idxname=docs", urlText) )); //$NON-NLS-1$
			resources.addAll(Arrays.asList(fRelatedTopics));
			fRelatedTopics = resources.toArray(new IHelpResource[resources.size()]);
		}
		catch (Exception e) {
			RUIPlugin.logError(-1, "Error occured when enrich R help.", e); //$NON-NLS-1$
		}
	}
	

	public String getTitle() {
		return fTitle;
	}
	
	public String getText() {
		return fText;
	}

	public String getStyledText() {
		return fStyledText;
	}
	
	public IHelpResource[] getRelatedTopics() {
		return fRelatedTopics;
	}

	public String getCategory(IHelpResource topic) {
		if (topic instanceof RHelpResource) {
			return Messages.RHelp_category;
		}
		return null;
	}

	public ICommandLink[] getRelatedCommands() {
		return fRelatedCommands;
	}
		
}
