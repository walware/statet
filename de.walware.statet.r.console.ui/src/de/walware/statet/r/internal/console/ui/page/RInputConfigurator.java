/*=============================================================================#
 # Copyright (c) 2006-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.page;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistProcessor;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.nico.ui.console.NIConsolePage;

import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.sourceediting.RContentAssistProcessor;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;


public class RInputConfigurator extends RSourceViewerConfigurator {
	
	
	private static class ThisConfiguration extends RSourceViewerConfiguration {
		
		public ThisConfiguration(final ISourceEditor sourceEditor) {
			super(sourceEditor, null, null, SharedUIResources.getColors() );
		}
		
		@Override
		public void initDefaultContentAssist(final ContentAssist assistant) {
			final ContentAssistComputerRegistry registry = RUIPlugin.getDefault().getRConsoleContentAssistRegistry();
			
			final ContentAssistProcessor codeProcessor = new RContentAssistProcessor(assistant,
					IRDocumentPartitions.R_DEFAULT_EXPL, registry, getSourceEditor());
			codeProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '$', '@' });
			assistant.setContentAssistProcessor(codeProcessor, IRDocumentPartitions.R_DEFAULT_EXPL);
			assistant.setContentAssistProcessor(codeProcessor, IRDocumentPartitions.R_DEFAULT);
			
			final ContentAssistProcessor symbolProcessor = new RContentAssistProcessor(assistant,
					IRDocumentPartitions.R_QUOTED_SYMBOL, registry, getSourceEditor());
			assistant.setContentAssistProcessor(symbolProcessor, IRDocumentPartitions.R_QUOTED_SYMBOL);
			
			final ContentAssistProcessor stringProcessor = new RContentAssistProcessor(assistant,
					IRDocumentPartitions.R_STRING, registry, getSourceEditor());
			assistant.setContentAssistProcessor(stringProcessor, IRDocumentPartitions.R_STRING);
		}
		
	}
	
	
	private final NIConsolePage fPage;
	
	
	public RInputConfigurator(final NIConsolePage page, final ISourceEditor inputEditor) {
		super((RConsole) page.getConsole(), new ThisConfiguration(inputEditor));
		fPage = page;
	}
	
}
