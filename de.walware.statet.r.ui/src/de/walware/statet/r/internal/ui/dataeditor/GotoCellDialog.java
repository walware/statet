/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.dataeditor;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;
import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.nebula.widgets.nattable.coordinate.Orientation;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.viewport.IViewportDim;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.databinding.LongValidator;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.ui.dataeditor.RDataTableComposite;


public class GotoCellDialog extends ExtStatusDialog {
	
	
	private final RDataTableComposite table;
	
	private final Text[] indexControls = new Text[2];
	
	private final Range[] indexRanges = new Range[2]; // 0-based
	private final WritableValue[] indexValues = new WritableValue[2]; // 1-based
	
	
	
	public GotoCellDialog(final RDataTableComposite table) {
		super(table.getShell(), WITH_DATABINDING_CONTEXT);
		
		this.table = table;
		
		setTitle("Go to Cell");
		setBlockOnOpen(true);
		setStatusLineAboveButtons(true);
		setHelpAvailable(false);
		
		for (final Orientation orientation : Orientation.values()) {
			final IViewportDim viewportDim = this.table.getViewport(orientation);
			this.indexRanges[orientation.ordinal()] = new Range(
					viewportDim.getMinimumOriginPosition(),
					viewportDim.getScrollable().getPositionCount() );
			this.indexValues[orientation.ordinal()] = new WritableValue(
					Long.valueOf(viewportDim.getOriginPosition() + 1), Long.TYPE );
		}
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createDialogGrid(2));
		
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("&Row index:");
			
			final Text text = new Text(composite, SWT.BORDER | SWT.SINGLE);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			indexControls[VERTICAL.ordinal()] = text;
		}
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Co&lumn index:");
			
			final Text text = new Text(composite, SWT.BORDER | SWT.SINGLE);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(text, 25);
			text.setLayoutData(gd);
			indexControls[HORIZONTAL.ordinal()] = text;
		}
		
		applyDialogFont(composite);
		return composite;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		for (final Orientation orientation : Orientation.values()) {
			final Range range = this.indexRanges[orientation.ordinal()];
			db.getContext().bindValue(
					SWTObservables.observeText(this.indexControls[orientation.ordinal()], SWT.Modify),
					this.indexValues[orientation.ordinal()],
					new UpdateValueStrategy().setAfterGetValidator(new LongValidator(
							(range.start + 1), range.end,
							"Invalid " + getLabel(orientation) + " index (" + (range.start + 1) + "\u2013" + range.end + ").")),
							null );
			this.indexControls[orientation.ordinal()].selectAll();
		}
	}
	
	protected String getLabel(final Orientation orientation) {
		return (orientation == HORIZONTAL) ? "column" : "row";
	}
	
	
	public void set(final Orientation orientation, final long index) {
		if (this.indexRanges[orientation.ordinal()].contains(index)) {
			this.indexValues[orientation.ordinal()].setValue(Long.valueOf(index + 1));
		}
	}
	
	public long get(final Orientation orientation) {
		return ((Long) this.indexValues[orientation.ordinal()].getValue()).longValue() - 1;
	}
	
}
