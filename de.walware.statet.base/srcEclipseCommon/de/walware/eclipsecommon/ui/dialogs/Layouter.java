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

package de.walware.eclipsecommon.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommon.ui.dialogs.groups.OptionsGroup;
import de.walware.eclipsecommon.ui.util.PixelConverter;



public class Layouter {
	
	
	public static final int DEFAULT_INDENTION = 20;
	
	
	public Composite fComposite;
	public int fNumColumns;
	
	/**
	 * Creates a new Layout-util
	 * <p>
	 * Warning: Does not create a new composite.
	 * 
	 * @param composite
	 * @param numColums
	 */
	public Layouter(Composite composite, int numColums) {

		fComposite = composite;
		fNumColumns = numColums;

		GridLayout layout = new GridLayout();
		if (!(composite instanceof Group)) {
			layout.marginHeight = 0;
			layout.marginWidth = 0;
		}
		layout.numColumns = fNumColumns;
		fComposite.setLayout(layout);
	}

	public Layouter(Composite composite, GridLayout layout) {

		fComposite = composite;
		fNumColumns = layout.numColumns;

		fComposite.setLayout(layout);
	}

	public void addFiller() {
		
		PixelConverter pixelConverter = new PixelConverter(fComposite);
		
		Label filler = new Label(fComposite, SWT.LEFT );
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = fNumColumns;
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	public void addSmallFiller() {
		
		PixelConverter pixelConverter = new PixelConverter(fComposite);
		
		Label filler = new Label(fComposite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = fNumColumns;
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 8;
		filler.setLayoutData(gd);
	}

	public void addHorizontalLine() {
		
		Label horizontalLine = new Label(fComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = fNumColumns;
		horizontalLine.setLayoutData(gd);
//		horizontalLine.setFont(composite.getFont());
	}
	
	
	public Label addLabel(String text) {
		
		return addLabel(text, 0, fNumColumns);
	}
		
	public Label addLabel(String text, int indentation, int horizontalSpan) {
		
		Label label = new Label(fComposite, SWT.LEFT);
		label.setText(text);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = horizontalSpan;
		label.setLayoutData(gd);
		
		return label;
	}

	public Button addCheckBox(String label) {
		return addCheckBox(label, 0, fNumColumns);
	}
	public Button addCheckBox(String label, int indentation) {
		return addCheckBox(label, indentation, fNumColumns);
	}
	public Button addCheckBox(String label, int indentation, int horizontalSpan) {		

		Button checkBox = new Button(fComposite, SWT.CHECK);
		checkBox.setText(label);
		
		GridData gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = horizontalSpan;
		checkBox.setLayoutData(gd);
		//makeScrollableCompositeAware(checkBox);

		return checkBox;
	}
	
	public Text addTextControl() {
		
		return addTextControl(fNumColumns);
	}
	
	public Text addTextControl(int horizontalSpan) {
		
		Text text = new Text(fComposite, SWT.SINGLE | SWT.BORDER);
		
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = horizontalSpan;
		text.setLayoutData(gd);
		
		return text;
	}

	public Text addLabeledTextControl(String label) {
		
		addLabel(label, 0, 1);
		Text text = addTextControl(fNumColumns - 1);
		
		return text;
	}

	public Combo addLabeledComboControl(String label, String[] items) {

		addLabel(label, 0, 1);
		return addComboControl(items, fNumColumns - 1);
	}

	public Combo addComboControl(String[] items, int numColumns) {

		Combo combo = new Combo(fComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(items);

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.horizontalSpan = numColumns;
		combo.setLayoutData(gd);
		
		return combo;
	}

	public Button addButton(String label, SelectionListener listener) {
		
		return addButton(label, listener, fNumColumns);
	}
	public Button addButton(String label, SelectionListener listener, int horizontalSpan) {
		
		Button button = new Button(fComposite, SWT.PUSH);
		button.setText(label);
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		gd.horizontalSpan = horizontalSpan;
		gd.widthHint = getButtonWidthHint(button);
		button.setLayoutData(gd);
		
		if (listener != null)
			button.addSelectionListener(listener);
		
		return button;
	}
	
	private int getButtonWidthHint(Button button) {

		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
	
	
	public void addGroup(OptionsGroup group) {
		
		group.createGroup(this);
	}
	
	
	public Group addGroup(String label) {
		
		return addGroup(label, false);
	}
	public Group addGroup(String label, boolean grabVerticalSpace) {
		
		Group group = new Group(fComposite, SWT.NONE);
		group.setText(label);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, grabVerticalSpace);
		gd.horizontalSpan = fNumColumns;
		group.setLayoutData(gd);

		return group;
	}

	/**
	 * Tests is the control is not <code>null</code> and not disposed.
	*/
	public static final boolean isOkToUse(Control control) {
		return (control != null) && (Display.getCurrent() != null) && !control.isDisposed();
	}

}