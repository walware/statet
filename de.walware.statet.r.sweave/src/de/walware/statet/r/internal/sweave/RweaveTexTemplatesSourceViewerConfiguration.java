/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.ColorManager;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.internal.sweave.editors.RweaveTexViewerConfiguration;


class RweaveTexTemplatesSourceViewerConfiguration extends RweaveTexViewerConfiguration {
	
	
	private final TemplateVariableProcessor fProcessor;
	
	
	public RweaveTexTemplatesSourceViewerConfiguration(final TemplateVariableProcessor processor,
			final IRCoreAccess rCoreAccess, final IPreferenceStore store, final ColorManager colorManager) {
		super(null, rCoreAccess, store, colorManager);
		fProcessor = processor;
	}
	
	@Override
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		return createTemplateVariableContentAssistant(sourceViewer, fProcessor);
	}
	
	@Override
	public int[] getConfiguredTextHoverStateMasks(final ISourceViewer sourceViewer, final String contentType) {
		return new int[] { ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK };
	}
	
	@Override
	public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType, final int stateMask) {
		return new TemplateVariableTextHover(fProcessor);
	}
	
}
