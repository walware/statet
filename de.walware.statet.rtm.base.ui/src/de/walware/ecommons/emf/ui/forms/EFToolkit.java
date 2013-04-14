/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;


public class EFToolkit extends FormToolkit {
	
	
	public EFToolkit(final FormColors colors) {
		super(colors);
	}
	
	
	public Label createPropLabel(final Composite parent,
			final String text, final String tooltip) {
		return createPropLabel(parent, text, tooltip, 1);
	}
	
	public Label createPropLabelFullLine(final Composite parent,
			final String text, final String tooltip) {
		final int colSpan = ((GridLayout) parent.getLayout()).numColumns;
		return createPropLabel(parent, text, tooltip, colSpan);
	}
	
	public Label createPropLabel(final Composite parent,
			final String text, final String tooltip, final int colSpan) {
		final Label label = createLabel(parent, text);
		
		final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
		
		label.setToolTipText(tooltip);
		label.setForeground(getColors().getColor(IFormColors.TITLE));
		
		return label;
	}
	
	public Text createPropTextField(final Composite parent, final int numChars) {
		final Text text = createText(parent, null);
		
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.widthHint = LayoutUtil.hintWidth(text, numChars);
		text.setLayoutData(gd);
		
		return text;
	}
	
	public TableComposite createPropSingleColumnTable(final Composite parent, final int numRows, final int numChars) {
		final ViewerUtil.TableComposite tableComposite = new ViewerUtil.TableComposite(
				parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL );
		adapt(tableComposite.table, false, false);
		
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = LayoutUtil.hintHeight(tableComposite.table, numRows, false);
		gd.widthHint = LayoutUtil.hintWidth(tableComposite.table, numChars);
		gd.minimumWidth = gd.widthHint / 2;
		
		tableComposite.setLayoutData(gd);
		tableComposite.addColumn(null, SWT.LEFT, new ColumnWeightData(100));
		
		return tableComposite;
	}
	
}
