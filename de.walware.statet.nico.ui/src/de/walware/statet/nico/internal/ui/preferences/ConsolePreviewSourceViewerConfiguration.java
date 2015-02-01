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

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.text.ui.presentation.SingleTokenScanner;
import de.walware.ecommons.ui.ColorManager;


public class ConsolePreviewSourceViewerConfiguration extends SourceEditorViewerConfiguration {
	
	
	public ConsolePreviewSourceViewerConfiguration(final IPreferenceStore preferenceStore,
			final ColorManager colorManager) {
		super(null);
		
		setup(preferenceStore, colorManager, null, null);
		initScanners();
	}
	
	protected void initScanners() {
		final IPreferenceStore store= getPreferences();
		final ColorManager colorManager= getColorManager();
		
		for (final String contentType : ConsoleTextStylesPreviewPartitioner.PARTITIONS) {
			addScanner(contentType,
					new SingleTokenScanner(colorManager, store,
							ConsolePreferences.OUTPUT_TEXTSTYLE_GROUP_ID, contentType ) );
		}
	}
	
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return ConsoleTextStylesPreviewPartitioner.PARTITIONS;
	}
	
}
