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

import org.eclipse.jdt.annotation.NonNullByDefault;

import de.walware.statet.r.internal.debug.core.model.RMainThread;
import de.walware.statet.r.internal.debug.core.model.RStackFrame;


@NonNullByDefault
public final class REvalExpressionTask {
	
	
	public static class Key {
		
		private final Long handle;
		
		private final String expression;
		
		
		public Key(final Long handle, final String expression) {
			this.handle= handle;
			this.expression= expression;
		}
		
		@Override
		public int hashCode() {
			return this.handle.hashCode() + this.expression.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Key) {
				final Key other= (Key) obj;
				return (this.handle.equals(other.handle)
						&& this.expression.equals(other.expression) );
			}
			return false;
		}
		
	}
	
	
	private final String rExpression;
	
	private final RStackFrame frame;
	
	private final Key key;
	
	
	public REvalExpressionTask(final String expressionText, final RStackFrame frame) {
		this.rExpression= expressionText;
		this.frame= frame;
		
		this.key= new Key(frame.getDbgFrame().getHandle(), expressionText);
	}
	
	
	public String getRExpression() {
		return this.rExpression;
	}
	
	public RStackFrame getStackFrame() {
		return this.frame;
	}
	
	public RMainThread getThread() {
		return this.frame.getThread();
	}
	
	public Key getKey() {
		return this.key;
	}
	
}
