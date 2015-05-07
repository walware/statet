/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.templates.TemplateContextType;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.templates.AbstractTemplatePreferencePage;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.text.core.treepartitioner.TreePartitioner;

import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;
import de.walware.statet.r.ui.text.r.RPartitionNodeType;


public class REditorTemplatePreferencePage extends AbstractTemplatePreferencePage {
	
	
	public REditorTemplatePreferencePage() {
		setPreferenceStore(RUIPlugin.getDefault().getPreferenceStore());
		setTemplateStore(RUIPlugin.getDefault().getREditorTemplateStore());
		setContextTypeRegistry(RUIPlugin.getDefault().getREditorTemplateContextRegistry());
	}
	
	@Override
	protected SourceEditorViewerConfigurator createSourceViewerConfigurator(
			final TemplateVariableProcessor templateProcessor) {
		return new RTemplateSourceViewerConfigurator(null, templateProcessor);
	}
	
	
	@Override
	protected void configureContext(final AbstractDocument document,
			final TemplateContextType contextType, final SourceEditorViewerConfigurator configurator) {
		final String partitioning= configurator.getDocumentContentInfo().getPartitioning();
		final TreePartitioner partitioner= (TreePartitioner) document.getDocumentPartitioner(partitioning);
		if (contextType.getId().equals(REditorTemplatesContextType.ROXYGEN_CONTEXTTYPE)) {
			partitioner.setStartType(RPartitionNodeType.ROXYGEN);
		}
		else {
			partitioner.setStartType(RPartitionNodeType.DEFAULT_ROOT);
		}
		partitioner.disconnect();
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioning, partitioner);
	}
	
}
