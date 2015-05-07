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

import de.walware.statet.nico.ui.console.NIConsolePage;

import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RDocumentContentInfo;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.sourceediting.RContentAssistProcessor;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;


public class RInputConfigurator extends RSourceViewerConfigurator {
	
	
	private static class ThisConfiguration extends RSourceViewerConfiguration {
		
		public ThisConfiguration(final ISourceEditor sourceEditor) {
			super(RDocumentContentInfo.INSTANCE, sourceEditor, null, null, null);
		}
		
		@Override
		public void initContentAssist(final ContentAssist assistant) {
			final ContentAssistComputerRegistry registry = RUIPlugin.getDefault().getRConsoleContentAssistRegistry();
			
			final ContentAssistProcessor codeProcessor = new RContentAssistProcessor(assistant,
					IRDocumentConstants.R_DEFAULT_CONTENT_TYPE, registry, getSourceEditor());
			codeProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '$', '@' });
			assistant.setContentAssistProcessor(codeProcessor, IRDocumentConstants.R_DEFAULT_CONTENT_TYPE);
			
			final ContentAssistProcessor symbolProcessor = new RContentAssistProcessor(assistant,
					IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE, registry, getSourceEditor());
			assistant.setContentAssistProcessor(symbolProcessor, IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE);
			
			final ContentAssistProcessor stringProcessor = new RContentAssistProcessor(assistant,
					IRDocumentConstants.R_STRING_CONTENT_TYPE, registry, getSourceEditor());
			assistant.setContentAssistProcessor(stringProcessor, IRDocumentConstants.R_STRING_CONTENT_TYPE);
		}
		
	}
	
	
	private final NIConsolePage fPage;
	
	
	public RInputConfigurator(final NIConsolePage page, final ISourceEditor inputEditor) {
		super((RConsole) page.getConsole(), new ThisConfiguration(inputEditor));
		fPage = page;
	}
	
}
