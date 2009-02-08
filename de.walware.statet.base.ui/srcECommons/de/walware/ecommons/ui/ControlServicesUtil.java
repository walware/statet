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

package de.walware.ecommons.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.internal.expressions.AndExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.swt.IFocusService;


/**
 * Helper to use services for one or multiple specified SWT controls. 
 * 
 * DEV: At moment only {@link IHandlerService}, but other services can be added later if required
 */
public class ControlServicesUtil {
	
	
	private class FocusExpression extends Expression {
		
		String getFocusControlId() {
			return fId;
		}
		
		@Override
		public EvaluationResult evaluate(final IEvaluationContext context) throws CoreException {
			final Object id = context.getVariable(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
			return (fId == id) ? EvaluationResult.TRUE : EvaluationResult.FALSE;
		}
		
		@Override
		public void collectExpressionInfo(final ExpressionInfo info) {
			info.addVariableNameAccess(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
		}
		
		@Override
		protected int computeHashCode() {
			return FocusExpression.class.hashCode() * HASH_FACTOR +
					hashCode(fId) * HASH_FACTOR;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof FocusExpression)) {
				return false;
			}
			final FocusExpression other = (FocusExpression) obj;
			return fId.equals(other.getFocusControlId());
		}
	}
	
	
	private final String fId;
	private final IServiceLocator fServiceLocator;
	private Expression fDefaultExpression;
	
	private boolean fRequireDeactivation;
	private IHandlerService fHandlerService;
	private List<IHandlerActivation> fActivatedHandlers;
	
	
	/**
	 * @param serviceLocator the servicelocator to be used
	 * @param id for control (group)
	 * @param parentControl used to get shell and listen for dispose event
	 */
	public ControlServicesUtil(final IServiceLocator serviceLocator, final String id, final Control parentControl) {
		assert (serviceLocator != null);
		assert (id != null);
		assert (parentControl != null);
		
		fId = id;
		fServiceLocator = serviceLocator;
		init(parentControl);
	}
	
	
	private void init(final Control control) {
		fDefaultExpression = new FocusExpression();
		if (fServiceLocator instanceof IWorkbench) {
			final AndExpression and = new AndExpression();
			and.add(new ActiveShellExpression(control.getShell()));
			and.add(fDefaultExpression);
			fDefaultExpression = and;
			fRequireDeactivation = true;
		}
		else if (fServiceLocator instanceof IWorkbenchPage && 
				!control.getShell().equals(((IWorkbenchPage) fServiceLocator).getWorkbenchWindow().getShell())) {
			final AndExpression and = new AndExpression();
			and.add(new ActiveShellExpression(control.getShell()));
			and.add(fDefaultExpression);
			fDefaultExpression = and;
			fRequireDeactivation = true;
		}
		else {
			fRequireDeactivation = false;
		}
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				dispose();
			}
		});
	}
	
	public void addControl(final Control control) {
		final IFocusService focusService = (IFocusService) fServiceLocator.getService(IFocusService.class);
		focusService.addFocusTracker(control, fId);
	}
	
	public Expression getExpression() {
		return fDefaultExpression;
	}
	
	public void activateHandler(final String commandId, final IHandler handler) {
		if (fHandlerService == null) {
			fHandlerService = (IHandlerService) fServiceLocator.getService(IHandlerService.class);
			if (fRequireDeactivation) {
				fActivatedHandlers = new ArrayList<IHandlerActivation>();
			}
		}
		final IHandlerActivation handlerActivation = fHandlerService.activateHandler(commandId, handler, fDefaultExpression);
		if (fActivatedHandlers != null) {
			fActivatedHandlers.add(handlerActivation);
		}
	}
	
	
	protected void dispose() {
		if (fHandlerService != null) {
			if (fActivatedHandlers != null) {
				fHandlerService.deactivateHandlers(fActivatedHandlers);
				fActivatedHandlers = null;
			}
			fHandlerService = null;
		}
	}
	
}
