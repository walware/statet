/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.texteditor.templates.TemplatesView;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.statet.base.ui.sourceeditors.ExtEditorTemplatesPage;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.sweave.text.Rweave;
import de.walware.statet.r.ui.editors.templates.REditorContext;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;


/**
 * Page for {@link LtxRweaveEditor} / {@link TemplatesView}
 */
public class LtxRweaveEditorTemplatesPage extends ExtEditorTemplatesPage {
	
	
	private SourceEditorViewerConfigurator fRPreviewConfigurator;
	
	
	public LtxRweaveEditorTemplatesPage(final LtxRweaveEditor editor, final ISourceViewer viewer) {
		super(editor, viewer);
	}
	
	
	@Override
	protected String getPreferencePageId() {
		return "de.walware.statet.r.preferencePages.REditorTemplates"; 
	}
	
	@Override
	protected IPreferenceStore getTemplatePreferenceStore() {
		return RUIPlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	public TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getREditorTemplateStore();
	}
	
	@Override
	protected ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getREditorTemplateContextRegistry();
	}
	
	@Override
	protected String[] getContextTypeIds(final IDocument document, final int offset) {
		try {
			final String contentType = TextUtilities.getContentType(document, Rweave.LTX_R_PARTITIONING, offset, true);
			if (Rweave.R_PARTITION_CONSTRAINT.matches(contentType)) {
				return new String[] { REditorTemplatesContextType.RCODE_CONTEXTTYPE };
			}
		}
		catch (final BadLocationException e) {
		}
		return new String[0];
	}
	
	@Override
	protected DocumentTemplateContext createContext(final IDocument document, final Template template, final int offset, final int length) {
		final TemplateContextType contextType = getContextTypeRegistry().getContextType(template.getContextTypeId());
		if (contextType != null) {
			return new REditorContext(contextType, document, offset, length, getEditor());
		}
		return null;
	}
	
	@Override
	protected SourceEditorViewerConfigurator getTemplatePreviewConfig(final Template template, final TemplateVariableProcessor templateProcessor) {
		if (fRPreviewConfigurator == null) {
			fRPreviewConfigurator = new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(), templateProcessor);
		}
		return fRPreviewConfigurator;
	}
	
	@Override
	protected SourceEditorViewerConfigurator getTemplateEditConfig(final Template template, final TemplateVariableProcessor templateProcessor) {
		return new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(), templateProcessor);
	}
	
}
