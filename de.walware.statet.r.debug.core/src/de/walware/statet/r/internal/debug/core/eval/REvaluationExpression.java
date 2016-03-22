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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.jdt.annotation.NonNull;

import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.internal.debug.core.model.RDebugElement;


public class REvaluationExpression extends RDebugElement implements IErrorReportingExpression {
	
	
	private static final String[] NO_MESSAGES= new String[0];
	
	
	private final REvaluationResult result;
	
	
	public REvaluationExpression(final REvaluationResult result) {
		super(result.getThread().getDebugTarget());
		this.result= result;
		
		this.result.getThread().getExpressionManager().register(this);
	}
	
	
	@Override
	public String getExpressionText() {
		return this.result.getExpressionText();
	}
	
	public REvaluationResult getResult() {
		return this.result;
	}
	
	@Override
	public boolean hasErrors() {
		return (this.result.getStatus() > IStatus.ERROR);
	}
	
	@Override
	public String[] getErrorMessages() {
		if (this.result.getStatus() >= IStatus.ERROR) {
			final ImList<@NonNull String> messages= this.result.getMessages();
			if (messages != null) {
				return messages.toArray(new String[messages.size()]);
			}
		}
		return NO_MESSAGES;
	}
	
	@Override
	public IRValue getValue() {
		return this.result.getValue();
	}
	
	@Override
	public void dispose() {
		this.result.getThread().getExpressionManager().unregister(this);
		this.result.free();
	}
	
}
