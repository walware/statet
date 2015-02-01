/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.sourcemodel.SyntaxProblemReporter;


public class RProblemReporter {
	
	
	private final SyntaxProblemReporter fSyntaxProblemReporter = new SyntaxProblemReporter();
	
	
	public RProblemReporter() {
	}
	
	
	public void run(final IRSourceUnit su, final SourceContent content,
			final RAstNode node, final IProblemRequestor problemRequestor) {
		fSyntaxProblemReporter.run(su, content, node, problemRequestor);
	}
	
	public void run(final IRSourceUnit su, final SourceContent content,
			final IAstNode node, final IProblemRequestor problemRequestor) {
		if (node instanceof RAstNode) {
			run(su, content, (RAstNode) node, problemRequestor);
		}
		else {
			final int n = node.getChildCount();
			for (int i = 0; i < n; i++) {
				final IAstNode child = node.getChild(i);
				if (child instanceof RAstNode) {
					run(su, content, (RAstNode) child, problemRequestor);
				}
			}
		}
	}
	
}
