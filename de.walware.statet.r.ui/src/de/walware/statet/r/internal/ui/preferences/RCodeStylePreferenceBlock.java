/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;


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
	
	private Text fTabSize;
	private ComboViewer fIndentPolicy;
	private Label fIndentSpaceCountLabel;
	private Text fIndentSpaceCount;

	
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
		
		Composite mainComposite = new Composite(parent.composite, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		mainComposite.setLayout((LayoutUtil.applyCompositeDefaults(new GridLayout(), 2)));
		
		Label label = new Label(mainComposite, SWT.NONE);
		label.setText(Messages.RCodeStyle_TabSize_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fTabSize = new Text(mainComposite, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fTabSize, 2);
		fTabSize.setLayoutData(gd);
		
		Group group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		group.setText(Messages.RCodeStyle_Indentation_group);
		createIndentControls(group);
		
		initBindings();
		fDbc.updateTargets();
	}
	
	private void createIndentControls(Composite group) {

		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		GridData gd;
		Label label;
		
		label = new Label(group, SWT.NONE);
		label.setText(Messages.RCodeStyle_Indentation_DefaultPolicy_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fIndentPolicy = new ComboViewer(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		fIndentPolicy.setContentProvider(new ArrayContentProvider());
		IndentationType[] items = new IndentationType[] { IndentationType.TAB, IndentationType.SPACES };
		final String[] itemLabels = new String[] { Messages.RCodeStyle_IndentationType_UseTabs_name, Messages.RCodeStyle_IndentationType_UseSpaces_name };
		fIndentPolicy.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				IndentationType t = (IndentationType) element;
				switch (t) {
				case TAB:
					return itemLabels[0];
				case SPACES:
					return itemLabels[1];
				}
				return null;
			}
		});
		fIndentPolicy.setInput(items);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fIndentPolicy.getCombo(), itemLabels);
		fIndentPolicy.getCombo().setLayoutData(gd);
		fIndentPolicy.setSelection(new StructuredSelection(IndentationType.TAB));
		
		label = fIndentSpaceCountLabel = new Label(group, SWT.NONE);
		label.setText(Messages.RCodeStyle_Indentation_NumOfSpaces_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fIndentSpaceCount = new Text(group, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fIndentSpaceCount, 2);
		fIndentSpaceCount.setLayoutData(gd);
	}

	private void initBindings() {
		
		Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);

		fDbc.bindValue(SWTObservables.observeText(fTabSize, SWT.Modify), 
				BeansObservables.observeValue(fModel, RCodeStyleSettings.PROP_TAB_SIZE), 
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 32, Messages.RCodeStyle_TabSize_error_message)), 
				null);
		
		IObservableValue indentObservable = ViewersObservables.observeSingleSelection(fIndentPolicy);
		indentObservable.setValue(null);
		indentObservable.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				IndentationType t = (IndentationType) event.diff.getNewValue();
				fIndentSpaceCountLabel.setEnabled(t == IndentationType.SPACES);
				fIndentSpaceCount.setEnabled(t == IndentationType.SPACES);
			}
		});
		fDbc.bindValue(indentObservable, BeansObservables.observeValue(fModel, RCodeStyleSettings.PROP_INDENT_DEFAULT_TYPE),
				null, null);
		fDbc.bindValue(SWTObservables.observeText(fIndentSpaceCount, SWT.Modify), 
				BeansObservables.observeValue(fModel, RCodeStyleSettings.PROP_INDENT_SPACES_COUNT), 
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 32, Messages.RCodeStyle_Indentation_NumOfSpaces_error_message)), 
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
