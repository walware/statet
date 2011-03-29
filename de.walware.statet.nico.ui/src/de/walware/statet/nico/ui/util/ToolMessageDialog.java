/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.core.runtime.ToolProcess;


public class ToolMessageDialog extends MessageDialog {
	
	
	public static boolean openConfirm(final ToolProcess tool, final Shell parent, final String title, final String message) {
		final ToolMessageDialog dialog = new ToolMessageDialog(tool,
				parent, title, null, 
				message, QUESTION, 
				new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
		return dialog.open() == 0;
	}
	
	
	private final ToolProcess fTool;
	
	
	/**
	 * @see MessageDialog
	 */
	public ToolMessageDialog(final ToolProcess tool, final Shell parentShell, 
			final String dialogTitle, final Image dialogTitleImage,
			final String dialogMessage, final int dialogImageType,
			final String[] dialogButtonLabels, final int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		fTool = tool;
	}
	
	
	protected ToolProcess getTool() {
		return fTool;
	}
	
	@Override
	protected Control createCustomArea(final Composite parent) {
		LayoutUtil.addSmallFiller(parent, true);
		
		final ToolInfoGroup info = new ToolInfoGroup(parent, fTool);
		info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		applyDialogFont(parent);
		return parent;
	}
	
}
