/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;


public interface IEFPropertyExpressions {
	
	
	String EOBJECT_LIST_ID = "de.walware.ecommons.emf.ui.EObjectList"; //$NON-NLS-1$
	
	Expression EOBJECT_LIST_EXPRESSION = new Expression() {
		
		@Override
		public void collectExpressionInfo(final ExpressionInfo info) {
			info.addVariableNameAccess(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
		}
		
		@Override
		public EvaluationResult evaluate(final IEvaluationContext context) {
			final Object id = context.getVariable(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
			return (id == EOBJECT_LIST_ID) ?
				EvaluationResult.TRUE : EvaluationResult.FALSE;
		}
		
	};
	
	
}
