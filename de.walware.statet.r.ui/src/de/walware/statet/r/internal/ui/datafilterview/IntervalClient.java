/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import static de.walware.statet.r.internal.ui.datafilter.IntervalVariableFilter.MAX_IDX;
import static de.walware.statet.r.internal.ui.datafilter.IntervalVariableFilter.MIN_IDX;
import static de.walware.statet.r.internal.ui.datafilter.IntervalVariableFilter.NA_IDX;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.components.WaScale;
import de.walware.ecommons.ui.content.IntValue2Double2TextBinding;
import de.walware.ecommons.ui.content.IntValue2TextBinding;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.rj.data.RStore;

import de.walware.statet.r.internal.ui.datafilter.IntervalVariableFilter;


public class IntervalClient extends FilterClient {
	
	
	private WaScale fScaleControl;
	
	private Text fLowerBoundControl;
	private Text fUpperBoundControl;
	
	private Object fLowerUpperGroup;
	
	private final IntervalVariableFilter fFilter;
	
	private Button fNAControl;
	
	
	public IntervalClient(final VariableComposite parent, final IntervalVariableFilter filter) {
		super(parent);
		
		fFilter = filter;
		filter.setListener(this);
		
		init(5);
	}
	
	
	@Override
	public IntervalVariableFilter getFilter() {
		return fFilter;
	}
	
	@Override
	protected void addWidgets() {
		fScaleControl = new WaScale(this, SWT.HORIZONTAL);
		fScaleControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
		
		fLowerBoundControl = new Text(this, SWT.BORDER);
		fLowerBoundControl.setLayoutData(LayoutUtil.hintWidth(
				new GridData(SWT.FILL, SWT.CENTER, true, false), fLowerBoundControl, 10 ));
		fLowerBoundControl.setToolTipText(Messages.Interval_LowerBound_tooltip);
		
		LayoutUtil.addGDDummy(this, true);
		
		fNAControl = new Button(this, SWT.CHECK);
		fNAControl.setText("NA"); //$NON-NLS-1$
		fNAControl.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT));
		fNAControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		LayoutUtil.addGDDummy(this, true);
		
		fUpperBoundControl = new Text(this, SWT.BORDER);
		fUpperBoundControl.setLayoutData(LayoutUtil.hintWidth(
				new GridData(SWT.FILL, SWT.CENTER, true, false), fLowerBoundControl, 10 ));
		fUpperBoundControl.setToolTipText(Messages.Interval_UpperBound_tooltip);
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator) {
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		if (fFilter.getColumn().getDataStore().getStoreType() == RStore.NUMERIC) {
			final IConverter text2value = new Text2NumConverter();
			final IConverter value2text = new Num2TextConverter();
//			{	RDataFormatter colFormatter = fFilter.getColumn().getDefaultFormat();
//				if (colFormatter != null && colFormatter.hasNumFormat()) {
//					RDataFormatter textFormatter = new RDataFormatter();
//					textFormatter.initNumFormat(colFormatter.getMaxFractionalDigits() + 1,
//							colFormatter.getMaxExponentDigits() );
//					value2text = new RDataFormatterConverter(Double.TYPE, textFormatter);
//				}
//			}
			final IntValue2Double2TextBinding.LowerUpperGroup group = new IntValue2Double2TextBinding.LowerUpperGroup(
					fScaleControl, fLowerBoundControl, fUpperBoundControl,
					db.getRealm(), value2text, text2value );
			db.getContext().bindValue(group.getLower(),
					fFilter.getSelectedLowerValue() );
			db.getContext().bindValue(group.getUpper(),
					fFilter.getSelectedUpperValue() );
			fLowerUpperGroup = group;
		}
		else {
			final IConverter text2value = new Text2IntConverter();
			final IConverter value2text = new Int2TextConverter();
			final IntValue2TextBinding.LowerUpperGroup group = new IntValue2TextBinding.LowerUpperGroup(
					fScaleControl, fLowerBoundControl, fUpperBoundControl,
					db.getRealm(), value2text, text2value );
			db.getContext().bindValue(group.getLower(),
					fFilter.getSelectedLowerValue() );
			db.getContext().bindValue(group.getUpper(),
					fFilter.getSelectedUpperValue() );
			fLowerUpperGroup = group;
		}
		db.getContext().bindValue(SWTObservables.observeSelection(fNAControl),
				fFilter.getSelectedNA() );
	}
	
	@Override
	protected void updateInput() {
		final RStore minMaxData = fFilter.getMinMaxData();
		if (minMaxData == null) {
			setEnabled(false, false);
			return;
		}
		
		if (fLowerUpperGroup instanceof IntValue2Double2TextBinding.LowerUpperGroup) {
			final double min = minMaxData.getNum(MIN_IDX);
			final double max = minMaxData.getNum(MAX_IDX);
			setEnabled((min != max), minMaxData.getLogi(NA_IDX));
			((IntValue2Double2TextBinding.LowerUpperGroup) fLowerUpperGroup).setMinMax(
					min, max );
		}
		else {
			final int min = minMaxData.getInt(MIN_IDX);
			final int max = minMaxData.getInt(MAX_IDX);
			setEnabled((min != max), minMaxData.getLogi(NA_IDX));
			((IntValue2TextBinding.LowerUpperGroup) fLowerUpperGroup).setMinMax(
					min, max );
		}
	}
	
	public void setEnabled(final boolean scale, final boolean na) {
		if (fScaleControl.getEnabled() != scale) {
			fScaleControl.setEnabled(scale);
			fLowerBoundControl.setEnabled(scale);
			fUpperBoundControl.setEnabled(scale);
		}
		if (fNAControl.getEnabled() != na) {
			fNAControl.setEnabled(na);
			fNAControl.setVisible(na);
		}
	}
	
}
