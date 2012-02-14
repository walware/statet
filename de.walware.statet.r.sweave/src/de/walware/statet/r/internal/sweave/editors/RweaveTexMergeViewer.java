/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ltk.ui.compare.CompareMergeTextViewer;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.sweave.SweavePlugin;


public class RweaveTexMergeViewer extends CompareMergeTextViewer {
	
	
	public RweaveTexMergeViewer(final Composite parent, final CompareConfiguration configuration) {
		super(parent, configuration);
	}
	
	@Override
	protected IDocumentSetupParticipant createDocumentSetupParticipant() {
		return new RweaveTexDocumentSetupParticipant();
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfigurator(final SourceViewer sourceViewer) {
		final RweaveTexViewerConfigurator viewerConfigurator = new RweaveTexViewerConfigurator(
				RCore.getWorkbenchAccess() );
		viewerConfigurator.setConfiguration(new RweaveTexViewerConfiguration(null, viewerConfigurator,
				SweavePlugin.getDefault().getEditorRTexPreferenceStore(), SharedUIResources.getColors() ));
		return viewerConfigurator;
	}
	
}
