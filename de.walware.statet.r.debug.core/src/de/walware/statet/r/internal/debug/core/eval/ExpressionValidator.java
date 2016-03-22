/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.eval;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.statet.r.core.model.RProblemReporter;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.debug.core.Messages;


@NonNullByDefault
public class ExpressionValidator implements IProblemRequestor {
	
	
	private static class StopReporterException extends RuntimeException {
		
		private static final long serialVersionUID= 1L;
		
		public StopReporterException() {
		}
		
	}
	
	
	private final RScanner rParser= new RScanner(AstInfo.LEVEL_MODEL_DEFAULT);
	
	private final RProblemReporter rProblemReporter= new RProblemReporter();
	
	private @Nullable String errorMessage;
	
	
	public ExpressionValidator() {
	}
	
	
	public @Nullable String checkExpression(final String expression) {
		try {
			final RAstNode node= this.rParser.scanExpr(new StringParserInput(expression).init());
			if (node == null) {
				this.errorMessage= Messages.Expression_Validate_Detail_SingleExpression_message;
			}
			else if (RAst.hasErrors(node)) {
				final SourceContent content= new SourceContent(0, expression);
				try {
					this.rProblemReporter.run(null, content, node, this);
				}
				catch (final StopReporterException e) {}
				if (this.errorMessage == null) {
					this.errorMessage= Messages.Expression_Validate_Detail_DetailMissing_message;
				}
			}
			if (this.errorMessage != null) {
				return NLS.bind(Messages.Expression_Validate_Invalid_message, this.errorMessage);
			}
			return null;
		}
		finally {
			this.errorMessage= null;
		}
	}
	
	
	@Override
	public void acceptProblems(final IProblem problem) {
		this.errorMessage= problem.getMessage();
		throw new StopReporterException();
	}
	
	@Override
	public void acceptProblems(final String categoryId, final List<IProblem> problems) {
		for (final IProblem problem : problems) {
			acceptProblems(problem);
		}
	}
	
	@Override
	public void finish() {
	}
	
}
