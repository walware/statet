/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.eclipsecommons.FastList;
import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.templates.WordFinder;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.util.ISettingsChangedHandler;


/**
 * Configuration for SourceViewer...
 */
public abstract class StatextSourceViewerConfiguration extends TextSourceViewerConfiguration
		implements ISettingsChangedHandler {
	
	
	public static IPreferenceStore createCombinedPreferenceStore(final IPreferenceStore store) {
		return createCombinedPreferenceStore(new IPreferenceStore[] { store });
	}
	
	public static IPreferenceStore createCombinedPreferenceStore(final IPreferenceStore[] stores) {
		final IPreferenceStore[] all = new IPreferenceStore[stores.length+2];
		int i = stores.length;
		System.arraycopy(stores, 0, all, 0, i);
		all[i++] = StatetUIPlugin.getDefault().getPreferenceStore();
		all[i++] = EditorsUI.getPreferenceStore();
		return new ChainedPreferenceStore(all);
	}
	
	
	protected final IEditorAdapter fEditorAdapter;
	
	private ColorManager fColorManager;
	private FastList<ISettingsChangedHandler> fSettingsHandler = new FastList<ISettingsChangedHandler>(ISettingsChangedHandler.class);
	private ContentAssistant fContentAssistant;
	
	
	public StatextSourceViewerConfiguration(final IEditorAdapter editorAdapter) {
		fEditorAdapter = editorAdapter;
	}
	
	protected void setup(final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		fPreferenceStore = preferenceStore;
		fColorManager = colorManager;
	}
	
	/**
	 * Initializes the scanners.
	 */
	protected void setScanners(final org.eclipse.jface.text.rules.ITokenScanner[] scanners) {
		for (int i = 0; i < scanners.length; i++) {
			if (scanners[i] instanceof ISettingsChangedHandler) {
				fSettingsHandler.add((ISettingsChangedHandler) scanners[i]);
			}
		}
	}
	
	public IPreferenceStore getPreferences() {
		return fPreferenceStore;
	}
	
	protected ColorManager getColorManager() {
		return fColorManager;
	}
	
	public boolean handleSettingsChanged(final Set<String> groupIds, final Object options) {
		boolean affectsPresentation = false;
		if (groupIds.contains(ContentAssistPreference.GROUP_ID) && fContentAssistant != null) {
			ContentAssistPreference.configure(fContentAssistant);
		}
		for (final ISettingsChangedHandler handler : fSettingsHandler.toArray()) {
			affectsPresentation |= handler.handleSettingsChanged(groupIds, options);
		}
		return affectsPresentation;
	}
	
/* For TemplateEditors ********************************************************/
	
	protected static class TemplateVariableTextHover implements ITextHover {
		
		private TemplateVariableProcessor fProcessor;
		
		/**
		 * @param processor the template variable processor
		 */
		public TemplateVariableTextHover(final TemplateVariableProcessor processor) {
			fProcessor = processor;
		}
		
		public String getHoverInfo(final ITextViewer textViewer, final IRegion subject) {
			try {
				final IDocument doc= textViewer.getDocument();
				final int offset= subject.getOffset();
				if (offset >= 2 && "${".equals(doc.get(offset-2, 2))) { //$NON-NLS-1$
					final String varName= doc.get(offset, subject.getLength());
					final TemplateContextType contextType= fProcessor.getContextType();
					if (contextType != null) {
						final Iterator iter= contextType.resolvers();
						while (iter.hasNext()) {
							final TemplateVariableResolver var= (TemplateVariableResolver) iter.next();
							if (varName.equals(var.getType())) {
								return var.getDescription();
							}
						}
					}
				}
			} catch (final BadLocationException e) {
			}
			return null;
		}
		
		public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
			if (textViewer != null) {
				return WordFinder.findWord(textViewer.getDocument(), offset);
			}
			return null;
		}
		
	}
	
	
	@Override
	public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
		if (fContentAssistant == null) {
			fContentAssistant = createContentAssistant(sourceViewer);
			if (fContentAssistant != null) {
				ContentAssistPreference.configure(fContentAssistant);
				fContentAssistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				fContentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
				fContentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
			}
		}
		return fContentAssistant;
	}
	
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		return null;
	}
	
	protected ContentAssistant createTemplateVariableContentAssistant(final ISourceViewer sourceViewer, final TemplateVariableProcessor processor) {
		final ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		for (final String contentType : getConfiguredContentTypes(sourceViewer)) {
			assistant.setContentAssistProcessor(processor, contentType);
		}
		return assistant;
	}
	
}
