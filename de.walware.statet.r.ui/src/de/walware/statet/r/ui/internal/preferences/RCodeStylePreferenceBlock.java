/*******************************************************************************
 * Copyright (c) 2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.internal.preferences;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.dialogs.Layouter;

import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;
import de.walware.statet.r.core.RCodeStyleSettings;


/**
 * A PreferenceBlock for RCodeStyleSettings (code formatting preferences).
 */
public class RCodeStylePreferenceBlock extends ManagedConfigurationBlock {
	
	
	private static class NumberValidator implements IValidator {
		
		private int fMin;
		private int fMax;
		private String fErrorMessage;
		
		NumberValidator(int min, int max, String errorMessage) {
			
			fMin = min;
			fMax = max;
			fErrorMessage = errorMessage;
		}
		
		public IStatus validate(Object value) {
			try {
				int i = Integer.parseInt((String) value);
				if (i >= fMin && i <= fMax) {
					return ValidationStatus.ok();
				}
			}
			catch(NumberFormatException e) {
			}
			return ValidationStatus.error(fErrorMessage); 
		}
	}
	
	
	private IStatusChangeListener fStatusListener;
	private AggregateValidationStatus fAggregateStatus;
	private DataBindingContext fDbc;
	private RCodeStyleSettings fModel;
	
	private Text fTabSizeText;

	
	public RCodeStylePreferenceBlock(IProject project, IStatusChangeListener statusListener) {
		
		super();
		fStatusListener = statusListener;
	}
	
	@Override
	public void createContents(Layouter parent, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {
		
		super.createContents(parent, container, preferenceStore);
		setupPreferenceManager(container, new Preference[] {
				RCodeStyleSettings.PREF_TAB_SIZE,
				RCodeStyleSettings.Presentation.PREF_TAB_SIZE,
		});
		fModel = new RCodeStyleSettings(this);
		
		Layouter layout = new Layouter(new Composite(parent.fComposite, SWT.NONE), 2);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		layout.fComposite.setLayoutData(gd);
		
		fTabSizeText = layout.addLabeledTextControl(Messages.RCodeStyle_TabSize_label, 2);
		
		initBindings();
	}
	
	private void initBindings() {
		
		Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);

		fDbc.bindValue(SWTObservables.observeText(fTabSizeText, SWT.Modify), 
				BeansObservables.observeValue(fModel, RCodeStyleSettings.PROP_TAB_SIZE), 
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 32, Messages.RCodeStyle_TabSize_error_message)), 
				null);

		fAggregateStatus = new AggregateValidationStatus(fDbc.getBindings(),
				AggregateValidationStatus.MAX_SEVERITY);
		fAggregateStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				IStatus currentStatus = (IStatus) event.diff.getNewValue();
				fStatusListener.statusChanged(currentStatus);
			}
		});
	}
	
	@Override
	protected void updateControls() {
		
		fModel.load(this);
		fModel.resetDirty();
		fDbc.updateTargets();  // required for invalid target values
	}
	
	@Override
	protected void onBeforeSave() {
		
		if (fModel.isDirty()) {
			fModel.resetDirty();
			setPrefValues(fModel.toPreferencesMap());
		}
	}
	
	@Override
	public void dispose() {
		
		super.dispose();
		
		if (fAggregateStatus != null) {
			fAggregateStatus.dispose();
			fAggregateStatus = null;
		}
	}
}
