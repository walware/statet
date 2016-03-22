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

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRVariable;
import de.walware.statet.r.ui.editors.IRSourceEditor;


public class WatchHandler extends AbstractDebugHandler
		implements IWatchExpressionFactoryAdapter, IWatchExpressionFactoryAdapterExtension {
	
	
	public WatchHandler() {
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(evaluationContext);
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(evaluationContext);
		if (part != null && selection != null) {
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				if (structSelection.isEmpty()) {
					setBaseEnabled(false);
					return;
				}
				for (final Object obj : structSelection.toList()) {
					if (obj instanceof IRVariable && canCreateWatchExpression((IRVariable) obj)) {
						continue;
					}
					setBaseEnabled(false);
					return;
				}
				setBaseEnabled(true);
				return;
			}
			else if (selection instanceof ITextSelection && part instanceof IRSourceEditor) {
//				final ISourceEditor sourceEditor= (ISourceEditor) part;
//				final ITextSelection textSelection= (ITextSelection) selection;
				setBaseEnabled(true);
				return;
			}
		}
		setBaseEnabled(false);
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		if (part != null && selection != null) {
			int count= 0;
			final IDebugElement contextElement= getContextElement(getViewInput(part), part);
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				for (final Object obj : structSelection.toList()) {
					if (obj instanceof IRVariable) {
						final String expression= createWatchExpression((IRVariable) obj);
						if (expression != null) {
							installWatchExpression(expression, contextElement);
							count++;
						}
					}
				}
			}
			else if (selection instanceof ITextSelection && part instanceof IRSourceEditor) {
				final ISourceEditor sourceEditor= (ISourceEditor) part;
				final ITextSelection textSelection= (ITextSelection) selection;
				final String expression= getExpressionText(textSelection, sourceEditor);
				if (expression != null) {
					installWatchExpression(expression, contextElement);
					count++;
				}
			}
			if (count > 0) {
				showView(part, IDebugUIConstants.ID_EXPRESSION_VIEW);
			}
		}
		return null;
	}
	
	
	private boolean canCreateWatchExpression(final IRVariable rVariable) {
		final IRElementVariable elementVariable= getElementVariable(rVariable);
		return (elementVariable != null && elementVariable.getFQElementName() != null);
	}
	
	private String createWatchExpression(final IRVariable rVariable) {
		final IRElementVariable elementVariable= getElementVariable(rVariable);
		if (elementVariable != null) {
			final RElementName elementName= elementVariable.getFQElementName();
			if (elementName != null) {
				return addIndex(
						elementName.getDisplayName(RElementName.DISPLAY_EXACT),
						getVariableItemIndex(rVariable) );
			}
		}
		return null;
	}
	
	
	@Override
	public boolean canCreateWatchExpression(final IVariable variable) {
		if (variable instanceof IRVariable) {
			return canCreateWatchExpression((IRVariable) variable);
		}
		return false;
	}
	
	@Override
	public String createWatchExpression(final IVariable variable) throws CoreException {
		if (variable instanceof IRVariable) {
			return createWatchExpression((IRVariable) variable);
		}
		return null;
	}
	
	
	private void installWatchExpression(final String snippet, final IDebugElement contextElement) {
		final IExpressionManager expressionManager= DebugPlugin.getDefault().getExpressionManager();
		final IWatchExpression expression= expressionManager.newWatchExpression(snippet);
		expressionManager.addExpression(expression);
		
		if (contextElement != null) {
			expression.setExpressionContext(contextElement);
		}
	}
	
}
