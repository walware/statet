/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.nico.ui;

import org.eclipse.jface.text.contentassist.ContentAssistant;

import de.walware.ecommons.ui.text.sourceediting.ContentAssistComputerRegistry;
import de.walware.ecommons.ui.text.sourceediting.ContentAssistProcessor;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.nico.ui.console.NIConsolePage;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.editors.RContentAssistProcessor;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;


public class RInputConfigurator extends RSourceViewerConfigurator {
	
	
	private class RConsoleConfiguration extends RSourceViewerConfiguration {
		
		public RConsoleConfiguration(final ISourceEditor sourceEditor, final IRCoreAccess coreAccess) {
			super(sourceEditor, coreAccess,
					RInputConfigurator.this.getPreferenceStore(),
					StatetUIServices.getSharedColorManager() );
		}
		
		@Override
		public void initDefaultContentAssist(final ContentAssistant assistant) {
			final ContentAssistComputerRegistry registry = RUIPlugin.getDefault().getRConsoleContentAssistRegistry();
			
			final ContentAssistProcessor stringProcessor = new RContentAssistProcessor(assistant,
					IRDocumentPartitions.R_STRING, registry, getSourceEditor());
			stringProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '/' });
			assistant.setContentAssistProcessor(stringProcessor, IRDocumentPartitions.R_STRING);
		}
		
	}
	
	
	private NIConsolePage fPage;
	
	
	public RInputConfigurator(final NIConsolePage page, final ISourceEditor inputEditor) {
		super((RConsole) page.getConsole(), RUIPlugin.getDefault().getEditorPreferenceStore());
		fPage = page;
		setConfiguration(new RConsoleConfiguration(inputEditor, this));
	}
	
}
