/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.walware.ecommons.ui.util.LayoutUtil;


public class ExtStatusDialog extends StatusDialog implements IRunnableContext {
	
	
	public class StatusUpdater implements IStatusChangeListener {
		
		public void statusChanged(final IStatus status) {
			updateStatus(status);
		}
		
	}
	
	
	private final boolean fWithRunnableContext;
	
	private Composite fProgressComposite;
	private ProgressMonitorPart fProgressMonitorPart;
	private Button fProgressMonitorCancelButton;
	
	private int fActiveRunningOperations;
	
	private Control fProgressLastFocusControl;
	private ControlEnableState fProgressLastContentEnableState;
	private Control[] fProgressLastButtonControls;
	private boolean[] fProgressLastButtonEnableStates;
	
	
	/**
	 * @see StatusDialog#StatusDialog(Shell)
	 */
	public ExtStatusDialog(final Shell parent) {
		this(parent, false);
	}
	
	/**
	 * @see StatusDialog#StatusDialog(Shell)
	 * 
	 * @param withRunnableContext create elements to provide {@link IRunnableContext}
	 */
	public ExtStatusDialog(final Shell parent, final boolean withRunnableContext) {
		super(parent);
		fWithRunnableContext = withRunnableContext;
	}
	
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createButtonBar(final Composite parent) {
		final Composite composite = (Composite) super.createButtonBar(parent);
		final GridLayout layout = (GridLayout) composite.getLayout();
		layout.verticalSpacing = 0;
		
		if (fWithRunnableContext) {
			final Composite monitorComposite = createMonitorComposite(composite);
			final Control[] children = composite.getChildren();
			layout.numColumns = 3;
			((GridData) children[0].getLayoutData()).horizontalSpan++;
			monitorComposite.moveBelow(children[1]);
			monitorComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		
		return composite;
	}
	
	private Composite createMonitorComposite(final Composite parent) {
		fProgressComposite = new Composite(parent, SWT.NULL);
		final GridLayout layout = LayoutUtil.applyCompositeDefaults(new GridLayout(), 2);
		layout.marginLeft = LayoutUtil.defaultHMargin();
		fProgressComposite.setLayout(layout);
		
		fProgressMonitorPart = new ProgressMonitorPart(fProgressComposite, null);
		fProgressMonitorPart.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fProgressMonitorCancelButton = createButton(fProgressComposite, 1000, IDialogConstants.CANCEL_LABEL, true);
		
		Dialog.applyDialogFont(fProgressComposite);
		fProgressComposite.setVisible(false);
		return fProgressComposite;
	}
	public void run(final boolean fork, final boolean cancelable, final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (!fWithRunnableContext) {
			throw new UnsupportedOperationException();
		}
		if (getShell() != null && getShell().isVisible()) {
			if (fActiveRunningOperations == 0) {
				// Save control state
				fProgressLastFocusControl = getShell().getDisplay().getFocusControl();
				if (fProgressLastFocusControl != null && fProgressLastFocusControl.getShell() != getShell()) {
					fProgressLastFocusControl = null;
				}
				
				fProgressLastContentEnableState = ControlEnableState.disable(getDialogArea());
				final List<Control> buttons = new ArrayList<Control>();
				for (final Control child : getButton(IDialogConstants.OK_ID).getParent().getChildren()) {
					if (child instanceof Button) {
						buttons.add(child);
					}
				}
				fProgressLastButtonControls = buttons.toArray(new Control[buttons.size()]);
				fProgressLastButtonEnableStates = new boolean[fProgressLastButtonControls.length];
				for (int i = 0; i < fProgressLastButtonControls.length; i++) {
					fProgressLastButtonEnableStates[i] = fProgressLastButtonControls[i].getEnabled();
					fProgressLastButtonControls[i].setEnabled(false);
				}
				
				// Enable monitor
				fProgressMonitorCancelButton.setEnabled(cancelable);
				fProgressMonitorPart.attachToCancelComponent(fProgressMonitorCancelButton);
				fProgressComposite.setVisible(true);
				fProgressMonitorCancelButton.setFocus();
				
			}
			
			fActiveRunningOperations++;
			try {
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			} 
			finally {
				fActiveRunningOperations--;
				
				if (fActiveRunningOperations == 0 && getShell() != null) {
					fProgressComposite.setVisible(false);
					fProgressLastContentEnableState.restore();
					for (int i = 0; i < fProgressLastButtonControls.length; i++) {
						fProgressLastButtonControls[i].setEnabled(fProgressLastButtonEnableStates[i]);
					}
					
					fProgressMonitorPart.removeFromCancelComponent(fProgressMonitorCancelButton);
					if (fProgressLastFocusControl != null) {
						fProgressLastFocusControl.setFocus();
					}
				}
			}
		} 
		else {
			PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
		}
	}
	
	@Override
	protected void updateButtonsEnableState(final IStatus status) {
		super.updateButtonsEnableState(status);
		if (fActiveRunningOperations > 0) {
			final Button okButton = getButton(IDialogConstants.OK_ID);
			for (int i = 0; i < fProgressLastButtonControls.length; i++) {
				if (fProgressLastButtonControls[i] == okButton) {
					fProgressLastButtonEnableStates[i] = okButton.isEnabled();
				}
			}
		}
	}
	
}
