/*******************************************************************************
 * Copyright (c) 2006-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.components.ShortedLabel;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUITools;


/**
 * Control group showing information about a NICO tool.
 */
public class ToolInfoGroup {
	
	
	private final ToolProcess fProcess;
	
	private ViewForm fForm;
	
	
	public ToolInfoGroup(final Composite parent, final ToolProcess process) {
		fProcess = process;
		createControls(parent);
	}
	
	
	private void createControls(final Composite parent) {
		fForm = new ViewForm(parent, SWT.BORDER | SWT.FLAT);
		final Composite info = new Composite(fForm, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 2;
		info.setLayout(layout);
		fForm.setContent(info);
		
		final Label text = new Label(info, SWT.NONE);
		final Image image = NicoUITools.getImage(fProcess);
		if (image != null) {
			text.setImage(image);
		}
		else {
			text.setText("(i)"); //$NON-NLS-1$
		}
		final GridData gd = new GridData(SWT.TOP, SWT.LEFT, false, false);
		gd.horizontalSpan = 1;
		gd.verticalSpan = 2;
		text.setLayoutData(gd);
		
		final ShortedLabel detail1 = new ShortedLabel(info, SWT.NONE);
		detail1.setText(fProcess.getLabel(ITool.LONG_LABEL));
		detail1.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final ShortedLabel detail2 = new ShortedLabel(info, SWT.NONE);
		final String wd = FileUtil.toString(fProcess.getWorkspaceData().getWorkspaceDir());
		detail2.setText((wd != null) ? wd : "");
		detail2.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	public Control getControl() {
		return fForm;
	}
	
}
