/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Diffs;
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.LaunchConfigTabWithDbc;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditor;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.components.CustomizableVariableSelectionDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;
import de.walware.statet.r.ui.editors.RTemplateSourceViewerConfigurator;


public class RweaveTab extends LaunchConfigTabWithDbc {
	
	
	public static final String NS = "de.walware.statet.r.debug/Rweave/"; //$NON-NLS-1$
	public static final String ATTR_SWEAVE_ID = NS + "SweaveProcessing"; //$NON-NLS-1$
	
	
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
				if (s.startsWith(RweaveTexLaunchDelegate.SWEAVE_CONSOLE)) {
					updateEnablement(RweaveTexLaunchDelegate.SWEAVE_CONSOLE);
					
					final String[] split = s.split(":", 2); //$NON-NLS-1$
					final String command = (split.length == 2 && split[1].length() > 0) ? split[1] : RweaveTexLaunchDelegate.DEFAULT_SWEAVE_R_COMMANDS;
					if (!command.equals(fConsoleCommandEditor.getDocument().get())) {
						fConsoleCommandEditor.getDocument().set(command);
					}
					
					fCurrentStatus = ValidationStatus.ok();
					return;
				}
				else if (s.startsWith(RweaveTexLaunchDelegate.SWEAVE_LAUNCH)) {
					updateEnablement(RweaveTexLaunchDelegate.SWEAVE_LAUNCH);
					
					final String[] split = s.split(":", 2); //$NON-NLS-1$
					if (split.length == 2 && split[1].length() > 0) {
						final ILaunchConfiguration[] configs = fAvailableConfigs;
						for (final ILaunchConfiguration config : configs) {
							if (config.getName().equals(split[1])) {
								fCmdLaunchTable.setSelection(new StructuredSelection(config));
								fCurrentStatus = ValidationStatus.ok();
								return;
							}
						}
					}
					fCmdLaunchTable.setSelection(new StructuredSelection());
					fCurrentStatus = ValidationStatus.warning(Messages.RweaveTab_RCmd_error_NoConfigSelected_message);
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
			if (fConsoleSelectControl.getSelection()) {
				value = RweaveTexLaunchDelegate.SWEAVE_CONSOLE + ':' + fConsoleCommandEditor.getDocument().get();
				fCurrentStatus = ValidationStatus.ok();
				updateEnablement(RweaveTexLaunchDelegate.SWEAVE_CONSOLE);
			}
			else if (fCmdLaunchSelectControl.getSelection()) {
				final Object selectedLaunch = ((StructuredSelection) fCmdLaunchTable.getSelection()).getFirstElement();
				value = RweaveTexLaunchDelegate.SWEAVE_LAUNCH;
				if (selectedLaunch instanceof ILaunchConfiguration) {
					value += ':'+((ILaunchConfiguration) selectedLaunch).getName();
					fCurrentStatus = ValidationStatus.ok();
				}
				else {
					fCurrentStatus = ValidationStatus.warning(Messages.RweaveTab_RCmd_error_NoConfigSelected_message);
				}
				updateEnablement(RweaveTexLaunchDelegate.SWEAVE_LAUNCH);
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
			fSkipSelectControl.setSelection(selection == null);
			fConsoleSelectControl.setSelection(selection == RweaveTexLaunchDelegate.SWEAVE_CONSOLE);
			fCmdLaunchSelectControl.setSelection(selection == RweaveTexLaunchDelegate.SWEAVE_LAUNCH);
			
			fConsoleCommandEditor.getTextControl().setEnabled(selection == RweaveTexLaunchDelegate.SWEAVE_CONSOLE);
			fConsoleCommandInsertButton.setEnabled(selection == RweaveTexLaunchDelegate.SWEAVE_CONSOLE);
			fCmdLaunchTable.getControl().setEnabled(selection == RweaveTexLaunchDelegate.SWEAVE_LAUNCH);
			fCmdLaunchNewButton.setEnabled(selection == RweaveTexLaunchDelegate.SWEAVE_LAUNCH);
		}
		
	}
	
	
	private ILaunchConfiguration[] fAvailableConfigs;
	private WritableValue fSelectionValue;
	
	private ILaunchConfigurationListener fLaunchConfigurationListener;
	private Button fSkipSelectControl;
	private Button fConsoleSelectControl;
	private SnippetEditor fConsoleCommandEditor;
	
	private Button fConsoleCommandInsertButton;
	private Button fCmdLaunchSelectControl;
	private TableViewer fCmdLaunchTable;
	private Button fCmdLaunchNewButton;
	
	
	public String getName() {
		return Messages.Processing_SweaveTab_label;
	}
	
	@Override
	public Image getImage() {
		return SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_RWEAVE);
	}
	
	
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(LayoutUtil.applyTabDefaults(new GridLayout(), 1));
		
		final Label label = new Label(mainComposite, SWT.NONE);
		label.setText(Messages.RweaveTab_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		Composite composite;
		composite = new Composite(mainComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createSweaveCommandGroup(composite);
		
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
		updateAvailableConfigs();
		
		initBindings();
	}
	
	private void createSweaveCommandGroup(final Composite group) {
		GridData gd;
		group.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
		
		fSkipSelectControl = new Button(group, SWT.RADIO);
		fSkipSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fSkipSelectControl.setText(Messages.RweaveTab_Skip_label);
		
		LayoutUtil.addSmallFiller(group, false);
		
		fConsoleSelectControl = new Button(group, SWT.RADIO);
		fConsoleSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fConsoleSelectControl.setText(Messages.RweaveTab_InConsole_label);
		
		final TemplateVariableProcessor templateVariableProcessor = new TemplateVariableProcessor();
		final RSourceViewerConfigurator configurator = new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(), templateVariableProcessor);
		fConsoleCommandEditor = new SnippetEditor(configurator);
		fConsoleCommandEditor.create(group, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd.heightHint = LayoutUtil.hintHeight(fConsoleCommandEditor.getSourceViewer().getTextWidget(), 5);
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		fConsoleCommandEditor.getControl().setLayoutData(gd);
		
		fConsoleCommandInsertButton = new Button(group, SWT.PUSH);
		fConsoleCommandInsertButton.setText(Messages.RweaveTab_InConsole_InserVar_label);
		fConsoleCommandInsertButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		fConsoleCommandInsertButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final CustomizableVariableSelectionDialog dialog = new CustomizableVariableSelectionDialog(getShell());
				dialog.setFilters(DialogUtil.DEFAULT_INTERACTIVE_FILTERS);
				dialog.addAdditional(RweaveTexLaunchDelegate.VARIABLE_SWEAVE_FILE);
				dialog.addAdditional(RweaveTexLaunchDelegate.VARIABLE_LATEX_FILE);
				if (dialog.open() != Dialog.OK) {
					return;
				}
				final String variable = dialog.getVariableExpression();
				if (variable == null) {
					return;
				}
				fConsoleCommandEditor.getSourceViewer().getTextWidget().insert(variable);
				fConsoleCommandEditor.getControl().setFocus();
			}
		});
		
		LayoutUtil.addSmallFiller(group, false);
		
		fCmdLaunchSelectControl = new Button(group, SWT.RADIO);
		fCmdLaunchSelectControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fCmdLaunchSelectControl.setText(Messages.RweaveTab_RCmd_label);
		
		fCmdLaunchTable = new TableViewer(group, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		gd.heightHint = LayoutUtil.hintHeight(fCmdLaunchTable.getTable(), 5);
		fCmdLaunchTable.getControl().setLayoutData(gd);
		fCmdLaunchTable.setLabelProvider(DebugUITools.newDebugModelPresentation());
		fCmdLaunchTable.setContentProvider(new ArrayContentProvider());
		fCmdLaunchTable.setInput(new Object());
		
		fCmdLaunchNewButton = new Button(group, SWT.PUSH);
		fCmdLaunchNewButton.setText(Messages.RweaveTab_RCmd_NewConfig_label);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fCmdLaunchNewButton);
		fCmdLaunchNewButton.setLayoutData(gd);
		fCmdLaunchNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				createNewRCmdSweaveLaunchConfig();
			}
			
		});
	}
	
	
	private void updateAvailableConfigs() {
		try {
			final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			final ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(RLaunchConfigurations.ID_R_CMD_CONFIGURATION_TYPE);
			final ILaunchConfiguration[] allConfigs = launchManager.getLaunchConfigurations(type);
			final ArrayList<Object> filteredConfigs = new ArrayList<Object>(allConfigs.length+1);
			for (final ILaunchConfiguration config : allConfigs) {
				if (config.getAttribute(RLaunchConfigurations.ATTR_R_CMD_COMMAND, "").equals("CMD Sweave")) { //$NON-NLS-1$ //$NON-NLS-2$
					filteredConfigs.add(config);
				}
			}
			fAvailableConfigs = filteredConfigs.toArray(new ILaunchConfiguration[filteredConfigs.size()]);
			if (UIAccess.isOkToUse(fCmdLaunchTable)) {
				fCmdLaunchTable.setInput(fAvailableConfigs);
			}
		}
		catch (final CoreException e) {
			SweavePlugin.logError(ICommonStatusConstants.LAUNCHCONFIG_ERROR, "An error occurred when updating R CMD list.", e);
		}
	}
	
	private void createNewRCmdSweaveLaunchConfig() {
		try {
			final String name = getLaunchConfigurationDialog().generateName(Messages.RweaveTab_RCmd_NewConfig_seed);
			final ILaunchConfigurationWorkingCopy config = RLaunchConfigurations.createNewRCmdConfig(name, "CMD Sweave"); //$NON-NLS-1$
			
			fSelectionValue.setValue(RweaveTexLaunchDelegate.SWEAVE_LAUNCH+':'+name);
			setDirty(true);
			
			config.doSave();
		} catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					Messages.RweaveTab_RCmd_NewConfig_error_Creating_message, e), StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fSelectionValue = new WritableValue(realm, String.class);
		
		final SelectionObservable obs = new SelectionObservable();
		fSkipSelectControl.addSelectionListener(obs);
		fConsoleSelectControl.addSelectionListener(obs);
		fCmdLaunchSelectControl.addSelectionListener(obs);
		fCmdLaunchTable.addSelectionChangedListener(obs);
		fConsoleCommandEditor.getDocument().addDocumentListener(obs);
		
		fSelectionValue.setValue("init"); //$NON-NLS-1$
		
		dbc.bindValue(obs, fSelectionValue, new UpdateValueStrategy().setAfterGetValidator(obs), null);
	}
	
	
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_SWEAVE_ID, RweaveTexLaunchDelegate.SWEAVE_CONSOLE+':');
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		String value = null;
		try {
			value = configuration.getAttribute(ATTR_SWEAVE_ID, ""); //$NON-NLS-1$
		} catch (final CoreException e) {
			logReadingError(e);
		}
		fConsoleCommandEditor.getDocument().set(RweaveTexLaunchDelegate.DEFAULT_SWEAVE_R_COMMANDS);
		final Object firstConfig = fCmdLaunchTable.getElementAt(0);
		fCmdLaunchTable.setSelection((firstConfig != null) ? new StructuredSelection(firstConfig) : new StructuredSelection());
		fSelectionValue.setValue(value);
		fConsoleCommandEditor.reset();
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_SWEAVE_ID, (String) fSelectionValue.getValue());
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
