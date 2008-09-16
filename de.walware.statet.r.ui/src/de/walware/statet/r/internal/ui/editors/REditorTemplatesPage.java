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


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractDocument;
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

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.ui.text.Partitioner;

import de.walware.statet.base.ui.sourceeditors.ExtEditorTemplatesPage;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.REditor;
import de.walware.statet.r.ui.editors.RTemplateSourceViewerConfigurator;
import de.walware.statet.r.ui.editors.templates.REditorContext;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;


/**
 * Page for {@link REditor} / {@link TemplatesView}
 */
public class REditorTemplatesPage extends ExtEditorTemplatesPage {
	
	
	private SourceViewerConfigurator fRPreviewConfigurator;
	
	
	public REditorTemplatesPage(final REditor editor, final ISourceViewer viewer) {
		super(editor, viewer);
		
	}
	
	
	@Override
	protected String getPreferencePageId() {
		return "de.walware.statet.r.preferencePages.REditorTemplates";  //$NON-NLS-1$
	}
	
	@Override
	protected IPreferenceStore getTemplatePreferenceStore() {
		return RUIPlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	protected TemplateStore getTemplateStore() {
		return RUIPlugin.getDefault().getREditorTemplateStore();
	}
	
	@Override
	protected ContextTypeRegistry getContextTypeRegistry() {
		return RUIPlugin.getDefault().getREditorTemplateContextRegistry();
	}
	
	@Override
	protected String[] getContextTypeIds(final IDocument document, final int offset) {
		try {
			final String partitionType = TextUtilities.getContentType(document, getEditor().getPartitioning().getPartitioning(), offset, true);
			if (partitionType == IRDocumentPartitions.R_ROXYGEN) {
				return new String[] { REditorTemplatesContextType.ROXYGEN_CONTEXTTYPE };
			}
		}
		catch (final BadLocationException e) {}
		return new String[] { REditorTemplatesContextType.RCODE_CONTEXTTYPE };
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
	protected SourceViewerConfigurator getTemplatePreviewConfig(final Template template, final TemplateVariableProcessor templateProcessor) {
		if (fRPreviewConfigurator == null) {
			fRPreviewConfigurator = new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(), templateProcessor);
		}
		return fRPreviewConfigurator;
	}
	
	@Override
	protected SourceViewerConfigurator getTemplateEditConfig(final Template template, final TemplateVariableProcessor templateProcessor) {
		return new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(), templateProcessor);
	}
	
	@Override
	protected void configureDocument(final AbstractDocument document, final TemplateContextType contextType, final SourceViewerConfigurator configurator) {
		final Partitioner partitioner = (Partitioner) document.getDocumentPartitioner(configurator.getPartitioning().getPartitioning());
		if (contextType.getId().equals(REditorTemplatesContextType.ROXYGEN_CONTEXTTYPE)) {
			partitioner.setStartPartitionType(IRDocumentPartitions.R_ROXYGEN);
		}
		else {
			partitioner.setStartPartitionType(IRDocumentPartitions.R_DEFAULT_EXPL);
		}
		partitioner.disconnect();
		partitioner.connect(document);
		document.setDocumentPartitioner(configurator.getPartitioning().getPartitioning(), partitioner);
	}

}
