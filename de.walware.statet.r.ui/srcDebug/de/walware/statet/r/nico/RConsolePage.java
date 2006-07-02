/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.ui.console.IConsoleView;

import de.walware.statet.ext.ui.editors.IEditorConfiguration;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.ui.console.InputGroup;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsolePage;
import de.walware.statet.r.nico.AbstractRController.IncompleteInputPrompt;
import de.walware.statet.r.ui.IRDocumentPartitions;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.text.r.RFastPartitionScanner;


public class RConsolePage extends NIConsolePage {

	public RConsolePage(NIConsole console, IConsoleView view) {
		
		super(console, view);
	}

	
	@Override
	protected IEditorConfiguration getInputEditorConfiguration() {
		
		return new RInputConfiguration();
	}
	
	@Override
	protected InputGroup createInputGroup() {
		
		return new InputGroup(this) {
			
			RFastPartitionScanner fTempScanner = new RFastPartitionScanner();
			
			@Override
			protected void onPromptUpdate(Prompt prompt) {
				
				IDocumentPartitioner partitioner = fDocument.getDocumentPartitioner(IRDocumentPartitions.R_DOCUMENT_PARTITIONING);
				if (partitioner != null && partitioner instanceof RDocumentSetupParticipant.ExtFastPartitioner) {
					RFastPartitionScanner scanner = (RFastPartitionScanner) ((RDocumentSetupParticipant.ExtFastPartitioner) partitioner).getPartitionTokenScanner();
					if ((prompt.meta & IRRunnableControllerAdapter.META_PROMPT_INCOMPLETE_INPUT) != 0) {
						IncompleteInputPrompt p = (IncompleteInputPrompt) prompt;
						String prevPart = searchLastPartition(p.input);
						scanner.setStartPartitionType(prevPart);
					}
					else {
						scanner.setStartPartitionType(IRDocumentPartitions.R_DEFAULT);
					}
					fDocument.updatePartitioning();
				}
			}
			
			private String searchLastPartition(String code) {
				
				fTempScanner.setRange(new Document(code), 0, code.length());
				IToken token = null;
				while (true) {
					IToken t =  fTempScanner.nextToken();
					if (t != null && t != Token.EOF) {
						token = t;
					}
					else {
						break;
					}
				}
				if (token != null) {
					return (String) token.getData();
				}
				return null;
			}
		};
	}
}
