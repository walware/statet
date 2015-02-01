/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.databinding.ComputedOnChangeValue;
import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.databinding.NotEmptyValidator;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.util.TrackingConfiguration;
import de.walware.statet.nico.internal.ui.Messages;


/**
 * Composite to configure a track configuration.
 */
public class TrackingConfigurationComposite extends Composite {
	
	
	public static class SaveTemplate {
		
		private final String fLabel;
		private final String fFilePath;
		
		public SaveTemplate(final String label, final String path) {
			fLabel = label;
			fFilePath = path;
		}
		
		public String getLabel() {
			return fLabel;
		}
		
		public String getFilePath() {
			return fFilePath;
		}
		
	}
	
	
	private Text fNameControl;
	
	private Button fStreamInfoControl;
	private Button fStreamInputControl;
	private Button fStreamInputHistoryOnlyControl;
	private Button fStreamOutputErrorControl;
	private Button fStreamOutputErrorTruncateControl;
	private Text fStreamOutputErrorTruncateLinesControl;
	
	private SubmitTypeSelectionComposite fSubmitTypeControl;
	
	private ResourceInputComposite fFilePathControl;
	private Button fFileAppendControl;
	private Button fFileOverwriteControl;
	
	private Button fPrependTimestampControl;
	
	private final List<SaveTemplate> fSaveTemplates = new ArrayList<SaveTemplate>();
	
	private TrackingConfiguration fInput;
	
	
	public TrackingConfigurationComposite(final Composite parent) {
		super(parent, SWT.NONE);
		
		configure();
		create();
	}
	
	
	protected boolean enableFullMode() {
		return true;
	}
	protected boolean enableFilePathAsCombo() {
		return false;
	}
	
	protected void configure() {
	}
	
	protected void addSaveTemplate(final SaveTemplate template) {
		fSaveTemplates.add(template);
	}
	
	protected void create() {
		setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		
		if (enableFullMode()) {
			final Composite topComposite = createTopOptions(this);
			if (topComposite != null) {
				topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			}
		}
		
		final Composite contentComposite = createContentOptions(this);
		if (contentComposite != null) {
			contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		final Composite saveComposite = createSaveOptions(this);
		if (saveComposite != null) {
			saveComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		final Composite additionalComposite = createAdditionalOptions(this);
		if (additionalComposite != null) {
			additionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}
	
	private Composite createTopOptions(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
		
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText(Messages.Tracking_Name_label);
		}
		{	final Text text = new Text(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(text, 80);
			text.setLayoutData(gd);
			fNameControl = text;
		}
		
		return composite;
	}
	
	private Composite createContentOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setText(Messages.Tracking_Content_label+':');
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		{	fStreamInfoControl = new Button(composite, SWT.CHECK);
			fStreamInfoControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fStreamInfoControl.setText(Messages.Tracking_InfoStream_label);
		}	
		{	fStreamInputControl = new Button(composite, SWT.CHECK);
			fStreamInputControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fStreamInputControl.setText(Messages.Tracking_InputStream_label);
		}
		if (enableFullMode()) {
			fStreamInputHistoryOnlyControl = new Button(composite, SWT.CHECK);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			fStreamInputHistoryOnlyControl.setLayoutData(gd);
			fStreamInputHistoryOnlyControl.setText(Messages.Tracking_InputStream_OnlyHistory_label);
			fStreamInputHistoryOnlyControl.setEnabled(false);
		}
		{	fStreamOutputErrorControl = new Button(composite, SWT.CHECK);
			fStreamOutputErrorControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fStreamOutputErrorControl.setText(Messages.Tracking_OutputStream_label);
		}
		{	final Composite truncateRow;
			{	truncateRow = new Composite(composite, SWT.NONE);
				final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
				gd.horizontalIndent = LayoutUtil.defaultIndent();
				truncateRow.setLayoutData(gd);
				truncateRow.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			}
			{	fStreamOutputErrorTruncateControl = new Button(truncateRow, SWT.CHECK);
				fStreamOutputErrorTruncateControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				fStreamOutputErrorTruncateControl.setText(Messages.Tracking_OutputStream_TruncateLines_label);
				fStreamOutputErrorTruncateControl.setEnabled(false);
			}
			{	fStreamOutputErrorTruncateLinesControl = new Text(truncateRow, SWT.RIGHT | SWT.BORDER);
				final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
				gd.widthHint = LayoutUtil.hintWidth(fStreamOutputErrorTruncateLinesControl, 10);
				fStreamOutputErrorTruncateLinesControl.setLayoutData(gd);
				fStreamOutputErrorTruncateLinesControl.setEnabled(false);
			}
		}
		
		if (enableFullMode()) {
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				label.setText(Messages.Tracking_Sources_label);
			}
			{	fSubmitTypeControl = new SubmitTypeSelectionComposite(composite);
				fSubmitTypeControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			}
		}
		return composite;
	}
	
	private Composite createSaveOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setText(Messages.Tracking_File_label+':');
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		fFilePathControl = new ResourceInputComposite(composite,
				enableFilePathAsCombo() ? ResourceInputComposite.STYLE_COMBO : ResourceInputComposite.STYLE_TEXT, 
				ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_SAVE,
				Messages.Tracking_File_label) {
			@Override
			protected void fillMenu(final Menu menu) {
				super.fillMenu(menu);
				
				if (!fSaveTemplates.isEmpty()) {
					new MenuItem(menu, SWT.SEPARATOR);
					
					for (final SaveTemplate template : fSaveTemplates) {
						final MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText(template.getLabel());
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								setText(template.getFilePath(), true);
								getTextControl().setFocus();
							}
						});
					}
				}
			}
		};
		fFilePathControl.setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
		fFilePathControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fFileAppendControl = new Button(composite, SWT.CHECK);
		fFileAppendControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fFileAppendControl.setText(Messages.Tracking_File_Append_label);
		
		fFileOverwriteControl = new Button(composite, SWT.CHECK);
		fFileOverwriteControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fFileOverwriteControl.setText(Messages.Tracking_File_Overwrite_label);
		
		return composite;
	}
	
	protected ResourceInputComposite getPathInput() {
		return fFilePathControl;
	}
	
	/**
	 * Extended or overwritten this method to add additional options.
	 * 
	 * @return the composite containing the additional options
	 */
	protected Composite createAdditionalOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setText(Messages.Tracking_Actions_label+':');
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		addDefaultAdditionalOptions(composite);
		
		return composite;
	}
	
	protected Control addDefaultAdditionalOptions(final Composite composite) {
		final int columns = ((GridLayout) composite.getLayout()).numColumns;
		fPrependTimestampControl = new Button(composite, SWT.CHECK);
		fPrependTimestampControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, columns, 1));
		fPrependTimestampControl.setText(Messages.Tracking_Actions_PrependTimestamp_label);
		return fPrependTimestampControl;
	}
	
	protected void addBindings(final DataBindingSupport db) {
		if (fNameControl != null) {
			db.getContext().bindValue(SWTObservables.observeText(fNameControl, SWT.Modify), BeansObservables.observeValue(fInput, "name"), //$NON-NLS-1$
					new UpdateValueStrategy().setAfterGetValidator(new NotEmptyValidator(Messages.Tracking_Name_error_Missing_message)), null);
		}
		
		final IObservableValue infoUIObs = SWTObservables.observeSelection(fStreamInfoControl);
		db.getContext().bindValue(infoUIObs, BeansObservables.observeValue(fInput, "trackStreamInfo")); //$NON-NLS-1$
		
		final IObservableValue inputUIObs = SWTObservables.observeSelection(fStreamInputControl);
		db.getContext().bindValue(inputUIObs, BeansObservables.observeValue(fInput, "trackStreamInput")); //$NON-NLS-1$
		if (fStreamInputHistoryOnlyControl != null) {
			db.getContext().bindValue(SWTObservables.observeEnabled(fStreamInputHistoryOnlyControl), inputUIObs);
			db.getContext().bindValue(SWTObservables.observeSelection(fStreamInputHistoryOnlyControl), BeansObservables.observeValue(fInput, "trackStreamInputHistoryOnly")); //$NON-NLS-1$
		}
		
		final IObservableValue outputUIObs = SWTObservables.observeSelection(fStreamOutputErrorControl);
		final IObservableValue outputModelObs = BeansObservables.observeValue(fInput, "trackStreamOutput"); //$NON-NLS-1$
		db.getContext().bindValue(outputUIObs, outputModelObs);
		if (fStreamOutputErrorTruncateControl != null) {
			final IObservableValue outputTruncateUIObs = SWTObservables.observeSelection(fStreamOutputErrorTruncateControl);
			final IObservableValue outputTruncateModelObs = BeansObservables.observeValue(fInput, "trackStreamOutputTruncate"); //$NON-NLS-1$
			db.getContext().bindValue(SWTObservables.observeEnabled(fStreamOutputErrorTruncateControl), outputUIObs);
			db.getContext().bindValue(outputTruncateUIObs, outputTruncateModelObs);
			db.getContext().bindValue(SWTObservables.observeEnabled(fStreamOutputErrorTruncateLinesControl), new ComputedOnChangeValue(
					Boolean.class, outputModelObs, outputTruncateModelObs) {
				@Override
				protected Object calculate() {
					final Boolean one = (Boolean) outputModelObs.getValue();
					final Boolean two = (Boolean) outputTruncateModelObs.getValue();
					return Boolean.valueOf(one.booleanValue() && two.booleanValue());
				}
			});
			db.getContext().bindValue(SWTObservables.observeText(fStreamOutputErrorTruncateLinesControl, SWT.Modify), BeansObservables.observeValue(fInput, "trackStreamOutputTruncateLines"), //$NON-NLS-1$
					new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(2, 1000000, Messages.Tracking_OutputStream_TruncateLines_error_Invalid_message)), null);
		}
		
		if (fSubmitTypeControl != null) {
			db.getContext().bindValue(fSubmitTypeControl.getObservable(), BeansObservables.observeValue(fInput, "submitTypes")); //$NON-NLS-1$
		}
		
		db.getContext().bindValue(fFilePathControl.getObservable(), BeansObservables.observeValue(fInput, "filePath"), //$NON-NLS-1$
				new UpdateValueStrategy().setAfterGetValidator(fFilePathControl.getValidator()), null);
		final IObservableValue fileModeModelObs = BeansObservables.observeValue(fInput, "fileMode"); //$NON-NLS-1$
		db.getContext().bindValue(SWTObservables.observeSelection(fFileAppendControl), new ComputedOnChangeValue(Boolean.class, fileModeModelObs) {
			@Override
			protected Object calculate() {
				final Integer mode = (Integer) fileModeModelObs.getValue();
				return Boolean.valueOf((mode.intValue() & EFS.APPEND) == EFS.APPEND);
			}
			@Override
			protected void extractAndSet(final Object value) {
				final Boolean selected = (Boolean) value;
				fileModeModelObs.setValue(selected.booleanValue() ? EFS.APPEND : EFS.NONE);
			}
		});
		db.getContext().bindValue(SWTObservables.observeSelection(fFileOverwriteControl), new ComputedOnChangeValue(Boolean.class, fileModeModelObs) {
			@Override
			protected Object calculate() {
				final Integer mode = (Integer) fileModeModelObs.getValue();
				return Boolean.valueOf((mode.intValue() & EFS.OVERWRITE) == EFS.OVERWRITE);
			}
			@Override
			protected void extractAndSet(final Object value) {
				final Boolean selected = (Boolean) value;
				fileModeModelObs.setValue(selected.booleanValue() ? EFS.OVERWRITE : EFS.NONE);
			}
		});
		
		if (fPrependTimestampControl != null) {
			db.getContext().bindValue(SWTObservables.observeSelection(fPrependTimestampControl), BeansObservables.observeValue(fInput, "prependTimestamp")); //$NON-NLS-1$
		}
	}
	
	
	public void setLabelEnabled(final boolean enabled) {
		fNameControl.setEnabled(enabled);
	}
	
	public void setStreamsEnabled(final boolean enabled) {
		fStreamInfoControl.setEnabled(enabled);
		fStreamInputControl.setEnabled(enabled);
		if (fStreamInputHistoryOnlyControl != null) {
			fStreamInputHistoryOnlyControl.setEnabled(enabled);
		}
		fStreamOutputErrorControl.setEnabled(enabled);
	}
	
	public boolean getStreamsEnabled() {
		return fStreamInfoControl.getEnabled();
	}
	
	public void setSubmitTypesEnabled(final boolean enabled) {
		fSubmitTypeControl.setEnabled(enabled);
	}
	
	public boolean getSubmitTypesEnabled() {
		return fSubmitTypeControl.getEnabled();
	}
	
	
	public void setInput(final TrackingConfiguration config) {
		fInput = config;
	}
	
	public TrackingConfiguration getInput() {
		return fInput;
	}
	
}
