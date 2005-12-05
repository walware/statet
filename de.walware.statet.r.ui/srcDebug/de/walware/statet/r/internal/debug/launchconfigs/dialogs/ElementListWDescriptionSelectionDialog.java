/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.launchconfigs.dialogs;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import de.walware.eclipsecommon.ui.util.PixelConverter;
import de.walware.statet.r.internal.debug.RLaunchingMessages;


/**
 * 
 * @author Stephan Wahlbrink
 */
public class ElementListWDescriptionSelectionDialog extends ElementListSelectionDialog {

	private boolean fWithArgumentField;
	
	private Text fDescriptionText;
	private Text fArgumentText;
	private String fArgumentValue;
	
	
	protected static class Element {
		
		private String fName;
		private String fDescription;
		private boolean fHasArgument;
		
		public Element(String name, String description) {
			
			this(name, description, false);
		}
		public Element(String name, String description, boolean hasArgument) {
			
			fName = name;
			fDescription = description;
			fHasArgument = hasArgument;
		}
		
		public String toString() {
			return fName;
		}
	}


	/**
	 * @param parent
	 * @param renderer
	 */
	public ElementListWDescriptionSelectionDialog(Shell parent, boolean withArguments) {
		super(parent, new LabelProvider());

		fWithArgumentField = withArguments;
		setMultipleSelection(false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Control control = super.createDialogArea(parent);
		createDescriptionArea((Composite)control);
		return control;
	}

	/**
	 * Creates an area to display a description of the selected option
	 * 
	 * @param parent parent widget
	 */
	private void createDescriptionArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayoutData(gd);
		container.setFont(parent.getFont());
		
		if (fWithArgumentField) {
			Label label = new Label(container, SWT.NONE);
			label.setFont(parent.getFont());
			label.setText(RLaunchingMessages.SelectionDialog_Argument);
			
			fArgumentText = new Text(container, SWT.BORDER);
			fArgumentText.setFont(container.getFont());
			gd = new GridData(GridData.FILL_HORIZONTAL);
			fArgumentText.setLayoutData(gd);
		}
			
		Label desc = new Label(container, SWT.NONE);
		desc.setFont(parent.getFont());
		desc.setText(RLaunchingMessages.SelectionDialog_Description);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		desc.setLayoutData(gd);
		
		fDescriptionText = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		fDescriptionText.setFont(container.getFont());
		fDescriptionText.setEditable(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = new PixelConverter(fDescriptionText).convertHeightInCharsToPixels(4);
		fDescriptionText.setLayoutData(gd);		
	}

	/**
	 * Update variable description and argument button enablement.
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#handleSelectionChanged()
	 */
	protected void handleSelectionChanged() {
		
		super.handleSelectionChanged();
		Object[] objects = getSelectedElements();
		String text = null;
		if (objects.length == 1) {
			Element option = (Element)objects[0];
			text = option.fDescription;
			fArgumentText.setEditable(option.fHasArgument);
		}
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		fDescriptionText.setText(text);
	}

	protected void okPressed() {
		
		fArgumentValue = fArgumentText.getText().trim();
		super.okPressed();
	}
	
	/**
	 * Returns the r-option expression the user generated from this
	 * dialog, or <code>null</code> if none.
	 *  
	 * @return r-option expression the user generated from this
	 * dialog, or <code>null</code> if none
	 */
	public String getValue() {
		Object[] selected = super.getResult();
		if (selected != null && selected.length == 1) {
			Element option = (Element)selected[0];
			if (option.fHasArgument)
				return option.fName + "=" + fArgumentValue;
			return option.fName;
		}
		return null;
	}
	
}
