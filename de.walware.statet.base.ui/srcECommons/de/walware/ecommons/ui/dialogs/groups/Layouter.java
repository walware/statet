/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs.groups;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.PixelConverter;


/**
 * use {@link LayoutUtil}, if possible.
 */
public class Layouter {
	
	
	public static final int DEFAULT_INDENTION = 20;
	
	
	public Composite composite;
	public int fNumColumns;
	
	
	/**
	 * Creates a new Layout-util
	 * <p>
	 * Warning: Does not create a new composite.
	 * 
	 * @param composite
	 * @param numColums
	 */
	public Layouter(final Composite composite, final int numColums) {
		this.composite = composite;
		fNumColumns = numColums;
		
		final GridLayout layout = new GridLayout();
		if (!(composite instanceof Group)) {
			layout.marginHeight = 0;
			layout.marginWidth = 0;
		}
		layout.numColumns = fNumColumns;
		composite.setLayout(layout);
	}
	
	public Layouter(final Composite composite, final GridLayout layout) {
		this.composite = composite;
		fNumColumns = layout.numColumns;
		
		composite.setLayout(layout);
	}
	
	
	public void add(final Control composite) {
		add(composite, fNumColumns);
	}
	
	public void add(final Control composite, final int horizontalSpan) {
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = horizontalSpan;
		composite.setLayoutData(gd);
	}
	
	public void addFiller() {
		Dialog.applyDialogFont(composite);
		final PixelConverter pixelConverter = new PixelConverter(composite);
		
		final Label filler = new Label(composite, SWT.LEFT );
		final GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = fNumColumns;
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}
	
	public void addSpaceGrabber() {
		final Label filler = new Label(composite, SWT.NONE);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = fNumColumns;
		filler.setLayoutData(gd);
	}
	
	/**
	 * Label (spans all cols)
	 * @param text
	 * @return the created label
	 */
	public Label addLabel(final String text) {
		return addLabel(text, 0, fNumColumns);
	}
	
	public Label addLabel(final String text, final int indentation, final int hSpan) {
		return addLabel(text, indentation, hSpan, false);
	}
	
	public Label addLabel(final String text, final int indentation, final int hSpan, final boolean vAlignTop) {
		final Label label = new Label(composite, SWT.LEFT);
		label.setText(text);
		final GridData gd = new GridData(SWT.FILL, vAlignTop ? SWT.TOP : SWT.CENTER, false, false);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = hSpan;
		label.setLayoutData(gd);
		
		return label;
	}
	
	public Button addCheckBox(final String label) {
		return addCheckBox(label, 0, fNumColumns);
	}
	public Button addCheckBox(final String label, final int indentation) {
		return addCheckBox(label, indentation, fNumColumns);
	}
	public Button addCheckBox(final String label, final int indentation, final int horizontalSpan) {
		final Button checkBox = new Button(composite, SWT.CHECK);
		checkBox.setText(label);
		
		final GridData gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = horizontalSpan;
		checkBox.setLayoutData(gd);
		//makeScrollableCompositeAware(checkBox);
		
		return checkBox;
	}
	
	public Text addTextControl() {
		return addTextControl(0, fNumColumns, true, -1);
	}
	
	public Text addTextControl(final int hIndent, final int horizontalSpan, final boolean hGrab, final int widthHint) {
		final Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		
		final GridData gd = new GridData(hGrab ? SWT.FILL : SWT.LEFT, SWT.CENTER, hGrab, false);
		gd.horizontalIndent = hIndent;
		gd.horizontalSpan = horizontalSpan;
		if (widthHint > 0) {
			gd.widthHint = new PixelConverter(text).convertWidthInCharsToPixels(widthHint);
		}
		text.setLayoutData(gd);
		
		return text;
	}
	
	public Text addLabeledTextControl(final String label) {
		addLabel(label, 0, 1);
		return addTextControl(0, fNumColumns - 1, true, -1);
	}
	
	public Combo addLabeledComboControl(final String label, final String[] items) {
		addLabel(label, 0, 1);
		return addComboControl(items, true, 0, fNumColumns - 1, false);
	}
	
	public Combo addComboControl(final String[] items, final int numColumns) {
		return addComboControl(items, true, 0, numColumns, true);
	}
	
	public Combo addComboControl(final String[] items, final boolean readOnly, final int hIndent, final int hSpan, final boolean hGrab) {
		int style = SWT.DROP_DOWN;
		if (readOnly) {
			style |= SWT.READ_ONLY;
		}
		final Combo combo = new Combo(composite, style);
		if (items != null) {
			combo.setItems(items);
		}
		
		final GridData gd = new GridData(hGrab ? SWT.FILL : SWT.LEFT, SWT.CENTER, hGrab, false);
		gd.horizontalIndent = hIndent;
		gd.horizontalSpan = hSpan;
		gd.widthHint = LayoutUtil.hintWidth(combo, items);
//		PixelConverter conv = new PixelConverter(combo);
//		gd.widthHint = conv.convertWidthInCharsToPixels(charWidth);
		combo.setLayoutData(gd);
		
		return combo;
	}
	
	public Button addButton(final String label, final SelectionListener listener) {
		return addButton(label, listener, fNumColumns);
	}
	
	public Button addButton(final String label, final SelectionListener listener, final int horizontalSpan) {
		final Button button = new Button(composite, SWT.PUSH);
		button.setText(label);
		final GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		gd.horizontalSpan = horizontalSpan;
		gd.minimumWidth = LayoutUtil.hintWidth(button);
		button.setLayoutData(gd);
		
		if (listener != null)
			button.addSelectionListener(listener);
		
		return button;
	}
	
	
	public void addGroup(final OptionsGroup group) {
		group.createGroup(composite, fNumColumns);
	}
	
	
	public Group addGroup(final String label) {
		return addGroup(label, false);
	}
	public Group addGroup(final String label, final boolean grabVerticalSpace) {
		final Group group = new Group(composite, SWT.NONE);
		group.setText(label);
		
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, grabVerticalSpace);
		gd.horizontalSpan = fNumColumns;
		group.setLayoutData(gd);
		
		return group;
	}
	
}
