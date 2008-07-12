/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.util;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchSite;


/**
 * 
 */
public class WorkbenchUIUtil {
	
	
	public static ISelection getCurrentSelection(final Object context) {
		if (context instanceof IEvaluationContext) {
			final IEvaluationContext evaluationContext = (IEvaluationContext) context;
			final IWorkbenchSite site = (IWorkbenchSite) evaluationContext.getVariable(ISources.ACTIVE_SITE_NAME);
			if (site != null) {
				final ISelectionProvider selectionProvider = site.getSelectionProvider();
				if (selectionProvider != null) {
					return selectionProvider.getSelection();
				}
				return null;
			}
			else {
				return (ISelection) evaluationContext.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			}
		}
		return null;
	}
	
	
	private WorkbenchUIUtil() {}
	
}
