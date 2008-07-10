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

package de.walware.statet.r.internal.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.base.ui.sourceeditors.CompareMergeTextViewer;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RdDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.RdSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RdSourceViewerConfigurator;


public class RdMergeViewer extends CompareMergeTextViewer {
	
	
	public RdMergeViewer(final Composite parent, final CompareConfiguration configuration) {
		super(parent, configuration);
	}
	
	@Override
	protected IDocumentSetupParticipant createDocumentSetupParticipant() {
		return new RdDocumentSetupParticipant();
	}
	
	@Override
	protected SourceViewerConfigurator createConfigurator(final SourceViewer sourceViewer) {
		final RdSourceViewerConfigurator viewerConfigurator = new RdSourceViewerConfigurator(
				RCore.getWorkbenchAccess(), RUIPlugin.getDefault().getEditorPreferenceStore());
		viewerConfigurator.setConfiguration(new RdSourceViewerConfiguration(
				viewerConfigurator, viewerConfigurator.getPreferenceStore(), StatetUIServices.getSharedColorManager()));
		return viewerConfigurator;
	}
	
}
