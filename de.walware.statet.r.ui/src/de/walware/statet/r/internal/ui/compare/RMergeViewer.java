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

import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.workbench.CompareMergeTextViewer;

import de.walware.statet.base.ui.StatetUIServices;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;


public class RMergeViewer extends CompareMergeTextViewer {
	
	
	public RMergeViewer(final Composite parent, final CompareConfiguration configuration) {
		super(parent, configuration);
	}
	
	@Override
	protected IDocumentSetupParticipant createDocumentSetupParticipant() {
		return new RDocumentSetupParticipant();
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfigurator(final SourceViewer sourceViewer) {
		final RSourceViewerConfigurator viewerConfigurator = new RSourceViewerConfigurator(
				RCore.getWorkbenchAccess(), RUIPlugin.getDefault().getEditorPreferenceStore());
		viewerConfigurator.setConfiguration(new RSourceViewerConfiguration(
				viewerConfigurator, viewerConfigurator.getPreferenceStore(), StatetUIServices.getSharedColorManager()));
		return viewerConfigurator;
	}
	
}
