/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.pkgmanager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.PixelConverter;


public class StatusPage extends WizardPage {
	
	
	public static final String PAGE_NAME = "StatusPage"; //$NON-NLS-1$
	
	
	private final boolean fAllowIgnore;
	
	private IStatus fStatus;
	
	private TableViewer fChildViewer;
	
	private Button fIgnoreControl;
	
	
	public StatusPage(final String title, final boolean allowIgnore) {
		super(PAGE_NAME);
		fAllowIgnore = allowIgnore;
		fStatus = Status.OK_STATUS;
		
		setTitle(title);
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createContentGrid(1));
		
		{	final Label label = new Label(composite, SWT.NONE);
			label.setText("&Issues:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		{	final Control detail = createDetailArea(composite);
			detail.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		if (fAllowIgnore) {
			final Button button = new Button(composite, SWT.CHECK);
			fIgnoreControl = button;
			button.setText("Ignore shown issues and continue.");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					updateState();
				}
			});
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		
		Dialog.applyDialogFont(composite);
		setControl(composite);
		
		setStatus(fStatus);
	}
	
	protected Control createDetailArea(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createCompositeGrid(1));
		
		fChildViewer = new TableViewer(composite);
		fChildViewer.setContentProvider(ArrayContentProvider.getInstance());
		fChildViewer.setLabelProvider(new StatusLabelProvider());
		fChildViewer.getTable().setToolTipText(null);
		new ColumnViewerToolTipSupport(fChildViewer, ColumnViewerToolTipSupport.NO_RECREATE, false) {
			@Override
			protected Composite createViewerToolTipContentArea(final Event event,
					final ViewerCell cell, final Composite parent) {
				final Image image = getImage(event);
				final String text = getText(event);
				
				final Composite composite = new Composite(parent, SWT.NONE);
				composite.setLayout(LayoutUtil.createCompositeGrid((image != null) ? 2 : 1));
				composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
				{	final Color color = getBackgroundColor(event);
					if (color != null) {
						composite.setBackground(color);
					}
				}
				
				if (image != null) {
					final Label label = new Label(composite, SWT.LEFT | SWT.TOP);
					label.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
					label.setImage(image);
				}
				{	final Label label = new Label(composite, SWT.LEFT | SWT.WRAP | SWT.TRANSPARENT);
					final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
					gd.widthHint = new PixelConverter(label).convertWidthInCharsToPixels(80);
					label.setLayoutData(gd);
					label.setText(text);
					label.setForeground(getForegroundColor(event));
				}
				
				return composite;
			}
		};
		
		{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = LayoutUtil.hintHeight(fChildViewer.getTable(), 8);
			gd.widthHint = 100;
			fChildViewer.getControl().setLayoutData(gd);
		}
		
		return composite;
	}
	
	
	public void setStatus(final IStatus status) {
		if (status == null) {
			throw new NullPointerException("status"); //$NON-NLS-1$
		}
		fStatus = status;
		if (isControlCreated()) {
			StatusInfo.applyToStatusLine(this, status);
			fChildViewer.setInput(status.isMultiStatus() ? status.getChildren() : new Object[0]);
			if (fIgnoreControl != null) {
				fIgnoreControl.setEnabled(fStatus.getSeverity() >= IStatus.ERROR);
			}
		}
		updateState();
	}
	
	private void updateState() {
		setPageComplete(fStatus.getSeverity() < IStatus.ERROR
				|| (fIgnoreControl != null && fIgnoreControl.getSelection() ));
	}
	
}
