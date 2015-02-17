/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.text.core.sections.AbstractDocContentSections;
import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.ui.presentation.SingleTokenScanner;
import de.walware.ecommons.text.ui.settings.TextStyleManager;


public class ConsolePreviewSourceViewerConfiguration extends SourceEditorViewerConfiguration {
	
	
	private static final IDocContentSections CONTENT_INFO= new AbstractDocContentSections(ConsoleTextStylesPreviewPartitioner.PARTITIONING,
			ConsoleTextStylesPreviewPartitioner.PARTITIONING, ImCollections.newList(ConsoleTextStylesPreviewPartitioner.PARTITIONING)) {
		@Override
		public String getTypeByPartition(final String contentType) {
			return ConsoleTextStylesPreviewPartitioner.PARTITIONING;
		}
	};
	
	
	public ConsolePreviewSourceViewerConfiguration(
			final IPreferenceStore preferenceStore, final TextStyleManager textStyles) {
		super(CONTENT_INFO, null);
		
		setup(preferenceStore, null, null);
		setTextStyles(textStyles);
	}
	
	
	@Override
	protected void initScanners() {
		final TextStyleManager textStyles= getTextStyles();
		
		for (final String contentType : ConsoleTextStylesPreviewPartitioner.PARTITIONS) {
			addScanner(contentType,
					new SingleTokenScanner(textStyles, contentType) );
		}
	}
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return ConsoleTextStylesPreviewPartitioner.PARTITIONS;
	}
	
}
