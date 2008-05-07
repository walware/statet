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

package de.walware.eclipsecommons.ui;

import org.eclipse.core.expressions.Expression;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.handlers.NestableHandlerService;
import org.eclipse.ui.services.IServiceLocator;


/**
 * Creates services for a SWT control. It allows to active handler for only this control
 * and therefore use of different handlers for different controls in a dialog or form.
 * 
 * At moment only {@link IHandlerService}, but other services can be added later if required
 */
public class ControlNestedServices {
	
	
	private class Listener implements FocusListener, DisposeListener {
		public void focusGained(final FocusEvent e) {
			if (fHandlerService != null) {
				fHandlerService.activate();
			}
		}
		public void focusLost(final FocusEvent e) {
			if (fHandlerService != null) {
				fHandlerService.deactivate();
			}
		}
		public void widgetDisposed(final DisposeEvent e) {
			ControlNestedServices.this.dispose();
		}
	}
	
	
	private Control fControl;
	private IServiceLocator fParent;
	private Expression fDefaultExpression;
	
	private boolean fIsInitialized;
	
	private NestableHandlerService fHandlerService;
	
	
	public ControlNestedServices(final Control control, final IServiceLocator parent) {
		fControl = control;
		fParent = parent;
	}
	
	
	private void checkInit() {
		if (!fIsInitialized) {
			final Listener listener = new Listener();
			fControl.addFocusListener(listener);
			fControl.addDisposeListener(listener);
			if (fParent instanceof IWorkbench || fParent instanceof IWorkbenchPage) {
				fDefaultExpression = new ActiveShellExpression(fControl.getShell());
			}
			// add other expression here if required
			fIsInitialized = true;
		}
	}
	
	protected void dispose() {
		if (fHandlerService != null) {
			fHandlerService.dispose();
			fHandlerService = null;
		}
		fControl = null;
	}
	
	
	public IHandlerService getHandlerService() {
		if (fHandlerService == null) {
			checkInit();
			fHandlerService = new NestableHandlerService((IHandlerService) fParent.getService(IHandlerService.class), fDefaultExpression);
		}
		return fHandlerService;
	}
	
}
