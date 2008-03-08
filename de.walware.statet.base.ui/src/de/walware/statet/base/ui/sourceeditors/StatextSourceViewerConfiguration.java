/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
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
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;
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
	
	
	private static final Pattern TAB_PATTERN = Pattern.compile("\\\t"); //$NON-NLS-1$
	
	private static IInformationControlCreator ASSIST_INFO_CREATOR;
	
	
	private static class AssistInformationControlCreator extends AbstractReusableInformationControlCreator 
			implements IPropertyChangeListener {
		
		private String INFO_STYLE_SHEET;
		
		public AssistInformationControlCreator() {
			JFaceResources.getFontRegistry().addListener(this);
			updateStyleSheet();
		}
		
		public void propertyChange(final PropertyChangeEvent event) {
			if (event.getProperty().equals(JFaceResources.DIALOG_FONT)) {
				updateStyleSheet();
			}
		}
		
		private void updateStyleSheet() {
			String style =
				// Font definitions
				"html         { font-family: sans-serif; font-size: 9pt; font-style: normal; font-weight: normal; }\n"+
				"body, h1, h2, h3, h4, h5, h6, p, table, td, caption, th, ul, ol, dl, li, dd, dt { font-size: 1em; }\n"+
				"pre          { font-family: monospace; }\n"+
				// Margins
				"html         { margin: 0px; padding: 0px }"+
				"body         { overflow: auto; margin-top: 0.25em; margin-bottom: 0.5em; margin-left: 0.25em; margin-right: 0.25em; }\n"+
				"h1           { margin-top: 0.3em; margin-bottom: 0.04em; }\n"+
				"h2           { margin-top: 2em; margin-bottom: 0.25em; }\n"+
				"h3           { margin-top: 1.7em; margin-bottom: 0.25em; }\n"+
				"h4           { margin-top: 2em; margin-bottom: 0.3em; }\n"+
				"h5           { margin-top: 0px; margin-bottom: 0px; }\n"+
				"p            { margin-top: 1em; margin-bottom: 1em; }\n"+
//				"pre          { margin-left: 0.6em; }\n"+
				"ul           { margin-top: 0px; margin-bottom: 1em; }\n"+
				"li           { margin-top: 0px; margin-bottom: 0px; }\n"+
				"li p         { margin-top: 0px; margin-bottom: 0px; }\n"+
				"ol           { margin-top: 0px; margin-bottom: 1em; }\n"+
				"dl           { margin-top: 0px; margin-bottom: 1em; }\n"+
				"dt           { margin-top: 0px; margin-bottom: 0px; font-weight: bold; }\n"+
				"dd           { margin-top: 0px; margin-bottom: 0px; }\n"+
				// Styles and colors
				"a:link       { color: #0000FF; }\n"+
				"a:hover      { color: #000080; }\n"+
				"a:visited    { text-decoration: underline; }\n"+
				"h4           { font-style: italic; }\n"+
				"strong       { font-weight: bold; }\n"+
				"em           { font-style: italic; }\n"+
				"var          { font-style: italic; }\n"+
				"th           { font-weight: bold; }\n";
			try {
				final FontData[] fontData = JFaceResources.getFontRegistry().getFontData(JFaceResources.DIALOG_FONT);
				if (fontData != null && fontData.length > 0) {
					style = style.replace("9pt", fontData[0].getHeight()+"pt");
					style = style.replace("sans-serif", "sans-serif, '"+fontData[0].getName()+"'");
				}
			}
			catch (final Throwable e) {
			}
			INFO_STYLE_SHEET = style;
		}
		
		@Override
		protected IInformationControl doCreateInformationControl(final Shell parent) {
			final int shellStyle = SWT.NO_TRIM | SWT.TOOL;
			final int style = SWT.NONE;
			if (BrowserInformationControl.isAvailable(parent)) {
				return new BrowserInformationControl(parent, shellStyle, style) {
					@Override
					public void setInformation(String content) {
						if (content.lastIndexOf("<html>", 100) < 0) {
							if (!content.startsWith("...<br")) {
								content = HTMLPrinter.convertToHTMLContent(content);
								final Matcher matcher = TAB_PATTERN.matcher(content);
								if (matcher.find()) {
									content = matcher.replaceAll("    ");
								}
								content = "<pre>"+content+"</pre>";
							}
							final StringBuffer s = new StringBuffer(content);
							HTMLPrinter.insertPageProlog(s, 0, INFO_STYLE_SHEET);
							HTMLPrinter.addPageEpilog(s);
							content = s.toString();
						}
						final String html = content;
						super.setInformation(html);
					}
				};
			}
			else {
				return new DefaultInformationControl(parent, shellStyle, style, new HTMLTextPresenter(false));
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
	
	
	private final IEditorAdapter fEditorAdapter;
	
	private ColorManager fColorManager;
	private FastList<ISettingsChangedHandler> fSettingsHandler = new FastList<ISettingsChangedHandler>(ISettingsChangedHandler.class);
	private ContentAssistant fContentAssistant;
	private IQuickAssistAssistant fQuickAssistant;
	
	
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
	
	protected IEditorAdapter getEditorAdapter() {
		return fEditorAdapter;
	}
	
	public IPreferenceStore getPreferences() {
		return fPreferenceStore;
	}
	
	protected ColorManager getColorManager() {
		return fColorManager;
	}
	
	public boolean handleSettingsChanged(final Set<String> groupIds, final Object options) {
		boolean affectsPresentation = false;
		if (groupIds.contains(ContentAssistPreference.GROUP_ID)) {
			if (fContentAssistant != null) {
				ContentAssistPreference.configure(fContentAssistant);
			}
			if (fQuickAssistant != null) {
				ContentAssistPreference.configure(fQuickAssistant);
			}
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
				fContentAssistant.setInformationControlCreator(getAssistInformationControlCreator(sourceViewer));
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
	
}
