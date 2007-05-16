/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.eclipsecommons.templates.TemplateVariableProcessor;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;


class RTemplateSourceViewerConfiguration extends RSourceViewerConfiguration {

	
	private final TemplateVariableProcessor fProcessor;

	
	public RTemplateSourceViewerConfiguration(
			TemplateVariableProcessor processor, StatetProject project) {
		
		super(RCore.getWorkbenchAccess(), 
				StatetUIServices.getSharedColorManager(), 
				RSourceViewerConfiguration.createCombinedPreferenceStore(
						RUIPlugin.getDefault().getPreferenceStore(), project));
		fProcessor = processor;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		
		return getTemplateVariableContentAssistant(sourceViewer, fProcessor);
	}

	@Override
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		
		return new int[] { ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK };
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		
		return new TemplateVariableTextHover(fProcessor);
	}
	
}