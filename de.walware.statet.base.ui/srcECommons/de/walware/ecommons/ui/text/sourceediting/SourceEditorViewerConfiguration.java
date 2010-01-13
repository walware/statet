/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import de.walware.ecommons.FastList;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.templates.WordFinder;
import de.walware.ecommons.ui.util.ColorManager;
import de.walware.ecommons.ui.util.ISettingsChangedHandler;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.sourceeditors.ContentAssistPreference;


/**
 * Configuration for SourceViewer...
 */
public abstract class SourceEditorViewerConfiguration extends TextSourceViewerConfiguration
		implements ISettingsChangedHandler {
	
	
	private static IInformationControlCreator ASSIST_INFO_CREATOR;
	
	
	private static class AssistInformationControlCreator extends AbstractReusableInformationControlCreator {
		
		@Override
		protected IInformationControl doCreateInformationControl(final Shell parent) {
			if (BrowserInformationControl.isAvailable(parent)) {
				return new BrowserInformationControl(parent, JFaceResources.DIALOG_FONT, false) {
					
					@Override
					public void setInformation(String content) {
						if (content.startsWith("...<br")) { // spell correction change proposal //$NON-NLS-1$
							content = content.replace("\\t", "    "); //$NON-NLS-1$ //$NON-NLS-2$
							final StringBuffer s = new StringBuffer(content.length()+1000);
							s.append("<pre>"); //$NON-NLS-1$
							s.append(content);
							s.append("</pre>"); //$NON-NLS-1$
							setInput(new DefaultBrowserInformationInput(null, "", s.toString(),  //$NON-NLS-1$
									DefaultBrowserInformationInput.FORMAT_HTMLBODY_INPUT));
						}
						else {
							setInput(new DefaultBrowserInformationInput(null, "", content, //$NON-NLS-1$
									DefaultBrowserInformationInput.FORMAT_TEXT_INPUT));
						}
					}
				};
			}
			else {
				return new DefaultInformationControl(parent, new HTMLTextPresenter(false));
			}
		}
		
	};
	
	
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
	
	
	private final ISourceEditor fSourceEditor;
	
	private ColorManager fColorManager;
	private FastList<ISettingsChangedHandler> fSettingsHandler = new FastList<ISettingsChangedHandler>(ISettingsChangedHandler.class);
	private ContentAssistant fContentAssistant;
	private IQuickAssistAssistant fQuickAssistant;
	
	
	public SourceEditorViewerConfiguration(final ISourceEditor sourceEditor) {
		fSourceEditor = sourceEditor;
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
	
	protected ISourceEditor getSourceEditor() {
		return fSourceEditor;
	}
	
	public IPreferenceStore getPreferences() {
		return fPreferenceStore;
	}
	
	protected ColorManager getColorManager() {
		return fColorManager;
	}
	
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		if (groupIds.contains(ContentAssistPreference.GROUP_ID)) {
			if (fContentAssistant != null) {
				ContentAssistPreference.configure(fContentAssistant);
			}
			if (fQuickAssistant != null) {
				ContentAssistPreference.configure(fQuickAssistant);
			}
		}
		for (final ISettingsChangedHandler handler : fSettingsHandler.toArray()) {
			handler.handleSettingsChanged(groupIds, options);
		}
	}
	public List<ISourceEditorAddon> getAddOns() {
		return new ArrayList<ISourceEditorAddon>();
	}
	
	
	@Override
	public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
		if (fContentAssistant == null) {
			fContentAssistant = createContentAssistant(sourceViewer);
			if (fContentAssistant != null) {
				ContentAssistPreference.configure(fContentAssistant);
				fContentAssistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				fContentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
				fContentAssistant.setInformationControlCreator(getAssistInformationControlCreator(sourceViewer));
			}
		}
		return fContentAssistant;
	}
	
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		return null;
	}
	
	
	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(final ISourceViewer sourceViewer) {
		if (fQuickAssistant == null) {
			fQuickAssistant = createQuickAssistant(sourceViewer);
			if (fQuickAssistant != null) {
				ContentAssistPreference.configure(fQuickAssistant);
				fQuickAssistant.setInformationControlCreator(getAssistInformationControlCreator(sourceViewer));
			}
		}
		return fQuickAssistant;
	}
	
	protected IQuickAssistAssistant createQuickAssistant(final ISourceViewer sourceViewer) {
		return super.getQuickAssistAssistant(sourceViewer);
	}
	
	protected IInformationControlCreator getAssistInformationControlCreator(final ISourceViewer sourceViewer) {
		if (ASSIST_INFO_CREATOR == null) {
			ASSIST_INFO_CREATOR = new AssistInformationControlCreator();
		}
		return ASSIST_INFO_CREATOR;
	}
	
	@Override
	public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType) {
		return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}
	
	@Override
	public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType, final int stateMask) {
		return null;
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
				if (offset >= 2 && "${".equals(doc.get(offset-2, 2))) {  //$NON-NLS-1$
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
	
	protected ContentAssistant createTemplateVariableContentAssistant(final ISourceViewer sourceViewer, final TemplateVariableProcessor processor) {
		final ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		for (final String contentType : getConfiguredContentTypes(sourceViewer)) {
			assistant.setContentAssistProcessor(processor, contentType);
		}
		return assistant;
	}
	
}
