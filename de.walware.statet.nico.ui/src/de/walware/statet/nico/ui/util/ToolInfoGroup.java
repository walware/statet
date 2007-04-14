/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.walware.eclipsecommons.ui.dialogs.ShortedLabel;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.ui.StatetImages;


/**
 * Control group showing information about a NICO tool.
 */
public class ToolInfoGroup {

	
	private ToolProcess fProcess;
	private ViewForm fForm;

	
	public ToolInfoGroup(Composite parent, ToolProcess process) {
		
		fProcess = process;
		createControls(parent);
	}
	
	private void createControls(Composite parent) {
		
		fForm = new ViewForm(parent, SWT.BORDER | SWT.FLAT);
		Composite info = new Composite(fForm, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 2;
		info.setLayout(layout);
		fForm.setContent(info);

		Label text = new Label(info, SWT.NONE);
		ImageDescriptor imageDescr = NicoUITools.getImageDescriptor(fProcess);
		Image image = null;
		if (imageDescr != null) {
			image = StatetImages.getCachedImage(imageDescr);
			text.setImage(image);
		}
		else {
			text.setText("(i)"); //$NON-NLS-1$
		}
		GridData gd = new GridData(SWT.TOP, SWT.LEFT, false, false);
		gd.horizontalSpan = 1;
		gd.verticalSpan = 2;
		text.setLayoutData(gd);
		
		ShortedLabel detail;
		detail = new ShortedLabel(info, SWT.NONE);
		detail.setText(fProcess.getToolLabel(false));
		detail.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		detail = new ShortedLabel(info, SWT.NONE);
		detail.setText(fProcess.getLabel());
		detail.getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	public Control getControl() {
		
		return fForm;
	}
}
