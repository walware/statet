/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import java.util.List;

import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.viewer.TexLaunchConfigurationDelegate;
import net.sourceforge.texlipse.viewer.TexLaunchConfigurationTab;
import net.sourceforge.texlipse.viewer.ViewerConfiguration;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.ui.databinding.LaunchConfigTabWithDbc;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;


public class PreviewTab extends LaunchConfigTabWithDbc {
	
	
	public static final String NS = "de.walware.statet.r.debug/DocPreview/"; //$NON-NLS-1$
	public static final String ATTR_VIEWER_CODE = NS + "Viewer.code"; //$NON-NLS-1$
	
	
	private class SelectionObservable extends AbstractObservableValue implements SelectionListener, ISelectionChangedListener, IDocumentListener, IValidator {
		
		private String fEncodedValue;
		private IStatus fCurrentStatus;
		
		
		public SelectionObservable() {
			fCurrentStatus = ValidationStatus.ok();
		}
		
		
		public Object getValueType() {
			return String.class;
		}
		
		@Override
		protected void doSetValue(final Object value) {
			if (value instanceof String) {
				final String s = (String) value;
				fEncodedValue = s;
				if (s.startsWith(RweaveTexCreationDelegate.PREVIEW_IDE)) {
					updateEnablement(RweaveTexCreationDelegate.PREVIEW_IDE);
					fCurrentStatus = ValidationStatus.ok();
					return;
				}
				else if (s.startsWith(RweaveTexCreationDelegate.PREVIEW_SPECIAL)) {
					updateEnablement(RweaveTexCreationDelegate.PREVIEW_SPECIAL);
					
					final String[] split = s.split(":", 2); //$NON-NLS-1$
					if (split.length == 2 && split[1].length() > 0) {
						final List<ViewerConfiguration> configs = fAvailablePreviewConfigs;
						for (final ViewerConfiguration config : configs) {
							if (config.getName().equals(split[1])) {
								fLaunchConfigTable.setSelection(new StructuredSelection(config));
								fCurrentStatus = ValidationStatus.ok();
								return;
							}
						}
					}
					fLaunchConfigTable.setSelection(new StructuredSelection());
					fCurrentStatus = ValidationStatus.warning(Messages.PreviewTab_LaunchConfig_error_NoConfigSelected_message);
					return;
				}
			}
			
			fCurrentStatus = ValidationStatus.ok();
			updateEnablement(null);
		}
		
		@Override
		protected Object doGetValue() {
			return fEncodedValue;
		}
		
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		public void widgetSelected(final SelectionEvent e) {
			if (!isInitializing()) {
				updateValue();
			}
		}
		
		public void selectionChanged(final SelectionChangedEvent event) {
			if (!isInitializing()) {
				updateValue();
			}
		}
		
		public void documentAboutToBeChanged(final DocumentEvent event) {
		}
		
		public void documentChanged(final DocumentEvent event) {
			if (!isInitializing()) {
				updateValue();
			}
		}
		
		private void updateValue() {
			String value;
			if (fSystemSelectControl.getSelection()) {
				value = RweaveTexCreationDelegate.PREVIEW_IDE;
				fCurrentStatus = ValidationStatus.ok();
				updateEnablement(RweaveTexCreationDelegate.PREVIEW_IDE);
			}
			else if (fLaunchConfigSelectControl.getSelection()) {
				final Object selectedLaunch = ((StructuredSelection) fLaunchConfigTable.getSelection()).getFirstElement();
				value = RweaveTexCreationDelegate.PREVIEW_SPECIAL;
				if (selectedLaunch instanceof ViewerConfiguration) {
					value += ':'+((ViewerConfiguration) selectedLaunch).getName();
					fCurrentStatus = ValidationStatus.ok();
				}
				else {
					fCurrentStatus = ValidationStatus.warning(Messages.PreviewTab_LaunchConfig_error_NoConfigSelected_message);
				}
				updateEnablement(RweaveTexCreationDelegate.PREVIEW_SPECIAL);
			}
			else {
				value = ""; //$NON-NLS-1$
				fCurrentStatus = ValidationStatus.ok();
				updateEnablement(null);
			}
			if (!value.equals(fEncodedValue)) {
				final String oldValue = fEncodedValue;
				fEncodedValue = value;
				fireValueChange(Diffs.createValueDiff(oldValue, value));
			}
		}
		
		public IStatus validate(final Object value) {
			return fCurrentStatus;
		}
		
		public void updateEnablement(final String selection) {
			fDisableSelectControl.setSelection(selection == null);
			fSystemSelectControl.setSelection(selection == RweaveTexCreationDelegate.PREVIEW_IDE);
			fLaunchConfigSelectControl.setSelection(selection == RweaveTexCreationDelegate.PREVIEW_SPECIAL);
			
			fLaunchConfigTable.getControl().setEnabled(selection == RweaveTexCreationDelegate.PREVIEW_SPECIAL);
			fLaunchConfigNewButton.setEnabled(selection == RweaveTexCreationDelegate.PREVIEW_SPECIAL);
		}
		
	}
	
	
	private List<ViewerConfiguration> fAvailablePreviewConfigs;
	private WritableValue fSelectionValue;
	
	private ILaunchConfigurationListener fLaunchConfigurationListener;
	private Button fDisableSelectControl;
	private Button fSystemSelectControl;
	private Button fLaunchConfigSelectControl;
	private TableViewer fLaunchConfigTable;
	private Button fLaunchConfigNewButton;
	
	private String fOutputFormat;
	private TexTab fTexTab;
	private SelectionObservable fSelectionObservable;
	
	
	public String getName() {
		return Messages.Creation_PreviewTab_label;
	}
	
	@Override
	public Image getImage() {
		return SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_PREVIEW);
	}
	
	
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(LayoutUtil.applyTabDefault(new GridLayout(), 1));
		
		final Label label = new Label(mainComposite, SWT.NONE);
		label.setText(Messages.PreviewTab_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		Composite composite;
		composite = new Composite(mainComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createOptions(composite);
		
		fLaunchConfigurationListener = new ILaunchConfigurationListener() {
			public void launchConfigurationAdded(final ILaunchConfiguration configuration) {
				updateAvailableConfigs();
			}
			public void launchConfigurationChanged(final ILaunchConfiguration configuration) {
				updateAvailableConfigs();
			}
			public void launchConfigurationRemoved(final ILaunchConfiguration configuration) {
				updateAvailableConfigs();
			}
		};
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(fLaunchConfigurationListener);
		
		initBindings();
	}
	
	private void createOptions(final Composite group) {
		GridData gd;
		group.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
		
		fDisableSelectControl = new Button(group, SWT.RADIO);
		fDisableSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fDisableSelectControl.setText(Messages.PreviewTab_Disable_label);
		
		LayoutUtil.addSmallFiller(group, false);
		
		fSystemSelectControl = new Button(group, SWT.RADIO);
		fSystemSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fSystemSelectControl.setText(Messages.PreviewTab_SystemEditor_label);
		
		LayoutUtil.addSmallFiller(group, false);
		
		fLaunchConfigSelectControl = new Button(group, SWT.RADIO);
		fLaunchConfigSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fLaunchConfigSelectControl.setText(Messages.PreviewTab_LaunchConfig_label);
		
		fLaunchConfigTable = new TableViewer(group, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		gd.heightHint = LayoutUtil.hintHeight(fLaunchConfigTable.getTable(), 6);
		fLaunchConfigTable.getControl().setLayoutData(gd);
		fLaunchConfigTable.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ViewerConfiguration) {
					return ((ViewerConfiguration) element).getName();
				}
				return super.getText(element);
			}
		});
		fLaunchConfigTable.setContentProvider(new ArrayContentProvider());
		fLaunchConfigTable.setInput(new Object());
		
		fLaunchConfigNewButton = new Button(group, SWT.PUSH);
		fLaunchConfigNewButton.setText(Messages.PreviewTab_LaunchConfig_NewConfig_label);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fLaunchConfigNewButton);
		fLaunchConfigNewButton.setLayoutData(gd);
		fLaunchConfigNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				createNewPreviewLaunchConfig();
			}
			
		});
	}
	
	
	private void updateAvailableConfigs() {
		if (fTexTab == null) {
			final ILaunchConfigurationTab[] tabs = getLaunchConfigurationDialog().getTabs();
			for (int i = 0; i < tabs.length; i++) {
				if (tabs[i] instanceof TexTab) {
					fTexTab = (TexTab) tabs[i];
				}
			}
			if (fTexTab == null) {
				return;
			}
			if (!fTexTab.addOutputFormatListener(new IChangeListener() {
				public void handleChange(final ChangeEvent event) {
					updateAvailableConfigs();
				}
			})) {
				fTexTab = null;
				return;
			}
		}
		fOutputFormat = fTexTab.getOutputFormat();
		fAvailablePreviewConfigs = Texlipse.getViewerManager().getAvailableConfigurations(fOutputFormat);
		if (UIAccess.isOkToUse(fLaunchConfigTable)) {
			fLaunchConfigTable.setInput(fAvailablePreviewConfigs);
			if (fSelectionObservable != null) {
				fSelectionObservable.updateValue();
			}
		}
	}
	
	private void createNewPreviewLaunchConfig() {
		try {
			final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			final ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(TexLaunchConfigurationDelegate.CONFIGURATION_ID);
			final String name = getLaunchConfigurationDialog().generateName(NLS.bind(Messages.PreviewTab_LaunchConfig_NewConfig_seed, fOutputFormat.toUpperCase()));
			final ILaunchConfigurationWorkingCopy config = type.newInstance(null, name);
			new EnvironmentTab().setDefaults(config);
			new TexLaunchConfigurationTab().setDefaults(config);
			
			fSelectionValue.setValue(RweaveTexCreationDelegate.PREVIEW_SPECIAL+':'+name);
			setDirty(true);
			
			config.doSave();
		} catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					Messages.PreviewTab_LaunchConfig_NewConfig_error_Creating_message, e), StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fSelectionValue = new WritableValue(realm, String.class);
		
		fSelectionObservable = new SelectionObservable();
		fDisableSelectControl.addSelectionListener(fSelectionObservable);
		fSystemSelectControl.addSelectionListener(fSelectionObservable);
		fLaunchConfigSelectControl.addSelectionListener(fSelectionObservable);
		fLaunchConfigTable.addSelectionChangedListener(fSelectionObservable);
		fSelectionValue.setValue("init"); //$NON-NLS-1$
		
		dbc.bindValue(fSelectionObservable, fSelectionValue, new UpdateValueStrategy().setAfterGetValidator(fSelectionObservable), null);
	}
	
	
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_VIEWER_CODE, RweaveTexCreationDelegate.PREVIEW_IDE);
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		updateAvailableConfigs();
		
		String value = null;
		try {
			value = configuration.getAttribute(ATTR_VIEWER_CODE, "");
		} catch (final CoreException e) {
			logReadingError(e);
		}
		final Object firstConfig = fLaunchConfigTable.getElementAt(0);
		fLaunchConfigTable.setSelection((firstConfig != null) ? new StructuredSelection(firstConfig) : new StructuredSelection());
		fSelectionValue.setValue(value);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_VIEWER_CODE, (String) fSelectionValue.getValue());
	}
	
	@Override
	public void dispose() {
		if (fLaunchConfigurationListener != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(fLaunchConfigurationListener);
			fLaunchConfigurationListener = null;
		}
		super.dispose();
	}
	
}
