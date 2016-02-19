/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.launching;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.databinding.core.util.UpdateableErrorValidator;
import de.walware.ecommons.debug.core.util.LaunchUtils;
import de.walware.ecommons.debug.ui.HelpRequestor;
import de.walware.ecommons.debug.ui.config.InputArgumentsComposite;
import de.walware.ecommons.debug.ui.config.LaunchConfigTabWithDbc;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.r.console.ui.IRConsoleHelpContextIds;
import de.walware.statet.r.console.ui.launching.RConsoleLaunching;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvConfiguration.Exec;
import de.walware.statet.r.internal.console.ui.Messages;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.launching.core.RLaunching;
import de.walware.statet.r.launching.ui.REnvTab;


/**
 * Main tab for R Console launch config.
 */
public class RConsoleMainTab extends LaunchConfigTabWithDbc {
	
	
	private class RArgumentsComposite extends InputArgumentsComposite {
		
		public RArgumentsComposite(final Composite parent) {
			super(parent);
		}
		
		@Override
		protected void fillToolMenu(final Menu menu) {
			super.fillToolMenu(menu);
			
			if (fWithHelp) {
				fHelpItem = new MenuItem(menu, SWT.PUSH);
				fHelpItem.setText(Messages.RConsole_MainTab_RunHelp_label);
				fHelpItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						queryHelp();
						getTextControl().setFocus();
					}
				});
				checkHelp(fConfigCache);
			}
		}
	}
	
	private final RConsoleType[] fTypes;
	private final RConsoleType fDefaultType;
	
	private ComboViewer fTypesCombo;
	
	private ResourceInputComposite fWorkingDirectoryControl;
	private RArgumentsComposite fArgumentsControl;
	
	private WritableValue fTypeValue;
	private WritableValue fWorkingDirectoryValue;
	protected WritableValue fArgumentsValue;
	
	boolean fWithHelp = false;
	private MenuItem fHelpItem;
	private ILaunchConfigurationTab fREnvTab;
	private ILaunchConfiguration fConfigCache;
	
	
	public RConsoleMainTab() {
		super();
		fTypes = loadTypes();
		fDefaultType = fTypes[0];
	}
	
	
	protected RConsoleType[] loadTypes() {
		final List<RConsoleType> types = new ArrayList<>();
		types.add(new RConsoleType("RJ (default)", RConsoleLaunching.LOCAL_RJS, true, true, true)); //$NON-NLS-1$
		types.add(new RConsoleType("Rterm", RConsoleLaunching.LOCAL_RTERM, false, false, false)); //$NON-NLS-1$
		return types.toArray(new RConsoleType[types.size()]);
	}
	
	RConsoleType getSelectedType() {
		if (fTypeValue != null) {
			return (RConsoleType) fTypeValue.getValue();
		}
		return null;
	}
	
	@Override
	public String getName() {
		return Messages.RConsole_MainTab_name;
	}
	
	@Override
	public Image getImage() {
		return SharedUIResources.getImages().get(SharedUIResources.OBJ_MAIN_TAB_ID);
	}
	
	@Override
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());
		
		{	// Type
			final Composite composite = new Composite(mainComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			composite.setLayout(LayoutUtil.createCompositeGrid(2));
			
			final Label label = new Label(composite, SWT.LEFT);
			label.setText(Messages.RConsole_MainTab_LaunchType_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final String[] names = new String[fTypes.length];
			for (int i = 0; i < fTypes.length; i++) {
				names[i] = fTypes[i].getName();
			}
			fTypesCombo = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			fTypesCombo.setContentProvider(new ArrayContentProvider());
			fTypesCombo.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(final Object element) {
					final RConsoleType type = (RConsoleType) element;
					return type.getName();
				}
			});
			fTypesCombo.setInput(fTypes);
			fTypesCombo.getCombo().setVisibleItemCount(names.length);
			fTypesCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fTypesCombo.getControl().setEnabled(fTypes.length > 1);
		}
		
		final Composite detailGroup = createTypeDetailGroup(mainComposite);
		if (detailGroup != null) {
			detailGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		final Composite commandGroup = createROptionsGroup(mainComposite);
		commandGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		
		LayoutUtil.addSmallFiller(mainComposite, true);
		createFooter(mainComposite);
		
		Dialog.applyDialogFont(parent);
		
		initBindings();
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IRConsoleHelpContextIds.R_CONSOLE_LAUNCH );
	}
	
	private Composite createROptionsGroup(final Composite parent) {
		for (final ILaunchConfigurationTab tab : getLaunchConfigurationDialog().getTabs()) {
			if (tab instanceof REnvTab) {
				fREnvTab = tab;
				break;
			}
		}
		fWithHelp = (fREnvTab != null) && (getLaunchConfigurationDialog() instanceof TrayDialog);
		
		final Group group = new Group(parent, SWT.NONE);
		group.setLayout(LayoutUtil.createGroupGrid(3));
		group.setText("R options:");
		
		fWorkingDirectoryControl = new ResourceInputComposite(group,
				ResourceInputComposite.STYLE_LABEL | ResourceInputComposite.STYLE_TEXT,
				ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN,
				Messages.RConsole_MainTab_WorkingDir_label );
		fWorkingDirectoryControl.setShowInsertVariable(true,
				DialogUtil.DEFAULT_INTERACTIVE_FILTERS, null);
		fWorkingDirectoryControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		fArgumentsControl = new RArgumentsComposite(group);
		fArgumentsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		return group;
	}
	
	protected Composite createTypeDetailGroup(final Composite parent) {
		return null;
	}
	
	protected Composite getArgumentComposite() {
		return fArgumentsControl;
	}
	
	protected void createFooter(final Composite composite) {
		final Label note = new Label(composite, SWT.WRAP);
		note.setText(SharedMessages.Note_label + ": " + fArgumentsControl.getNoteText()); //$NON-NLS-1$
		note.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	}
	
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fTypeValue = new WritableValue(realm, RConsoleType.class);
		fWorkingDirectoryValue = new WritableValue(realm, null, String.class);
		fArgumentsValue = new WritableValue(realm, String.class);
		
		IValidator typeValidator= null;
		if (getLaunchConfigurationDialog().getMode().equals(ILaunchManager.DEBUG_MODE)) {
			typeValidator= new UpdateableErrorValidator(new IValidator() {
				@Override
				public IStatus validate(Object value) {
					RConsoleType type= (RConsoleType) value;
					if (!type.isDebugSupported()) {
						return ValidationStatus.error(NLS.bind(
								"R launch type ''{0}'' does not support debug mode.",
								type.getName() ));
					}
					return ValidationStatus.ok();
				}
			});
		}
		dbc.bindValue(ViewersObservables.observeSingleSelection(fTypesCombo),
				fTypeValue,
				(typeValidator != null) ? 
						new UpdateValueStrategy().setAfterGetValidator(typeValidator) :
						null,
				null );
		
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(fArgumentsControl.getTextControl()),
				fArgumentsValue,
				null, null );
		
		fWorkingDirectoryControl.getValidator().setOnEmpty(IStatus.OK);
		dbc.bindValue(fWorkingDirectoryControl.getObservable(), fWorkingDirectoryValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new UpdateableErrorValidator(fWorkingDirectoryControl.getValidator()) ),
				null );
		
		fTypeValue.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				final Object newValue = event.diff.getNewValue();
				updateType((RConsoleType) newValue);
			}
		});
	}
	
	public RConsoleType getType() {
		return (RConsoleType) fTypeValue.getValue();
	}
	
	protected IObservableValue getTypeValue() {
		return fTypeValue;
	}
	
	/**
	 * @param typeId
	 */
	protected void updateType(final RConsoleType type) {
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RConsoleLaunching.ATTR_TYPE, fDefaultType.getId()); //s
		configuration.setAttribute(RConsoleLaunching.ATTR_OPTIONS, ""); //$NON-NLS-1$
		configuration.setAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false); 
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		String type = null;
		try {
			type = configuration.getAttribute(RConsoleLaunching.ATTR_TYPE, ""); //$NON-NLS-1$
			// convert old rterm to new rj
			if (type.equals("rterm")) { //$NON-NLS-1$
				type = RConsoleLaunching.LOCAL_RTERM;
			}
		} catch (final CoreException e) {
			type = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		int i = 0;
		for (; i < fTypes.length; i++) {
			if (fTypes[i].getId().equals(type)) {
				fTypeValue.setValue(fTypes[i]);
				break;
			}
		}
		if (i >= fTypes.length) {
			fTypeValue.setValue(fDefaultType);
		}
		
		String wd = null;
		try {
			wd = REnvTab.readWorkingDirectory(configuration);
		}
		catch (final CoreException e) {
			wd = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fWorkingDirectoryValue.setValue(wd);
		
		String options = null;
		try {
			options = configuration.getAttribute(RConsoleLaunching.ATTR_OPTIONS, ""); //$NON-NLS-1$
		} catch (final CoreException e) {
			options = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fArgumentsValue.setValue(options);
		
		checkHelp(configuration);
	}
	
	@Override
	public void activated(final ILaunchConfigurationWorkingCopy workingCopy) {
		checkHelp(workingCopy);
		super.activated(workingCopy);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RConsoleLaunching.ATTR_TYPE, ((RConsoleType) fTypeValue.getValue()).getId());
		if (fArgumentsControl.isEnabled()) {
			configuration.setAttribute(RConsoleLaunching.ATTR_OPTIONS, (String) fArgumentsValue.getValue());
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_OPTIONS);
		}
		
		REnvTab.setWorkingDirectory(configuration, (String) fWorkingDirectoryValue.getValue());
	}
	
	private void checkHelp(final ILaunchConfiguration configuration) {
		fConfigCache = configuration;
		if (fWithHelp && fHelpItem != null) {
			fHelpItem.setEnabled(fREnvTab.isValid(fConfigCache));
		}
	}
	
	private void queryHelp() {
		if (!fWithHelp) {
			return;
		}
		try {
			final List<String> cmdLine = new ArrayList<>();
			final ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
			
			// r env
			final IREnvConfiguration renv = RLaunching.getREnvConfig(fConfigCache, true);
			
			cmdLine.addAll(0, renv.getExecCommand(Exec.TERM));
			
			cmdLine.add("--help"); //$NON-NLS-1$
			final ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
			final HelpRequestor helper = new HelpRequestor(processBuilder, (TrayDialog) dialog);
			
			final Map<String, String> envp = processBuilder.environment();
			LaunchUtils.configureEnvironment(envp, fConfigCache, renv.getEnvironmentsVariables());
			
			dialog.run(true, true, helper);
			updateLaunchConfigurationDialog();
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID, -1,
					Messages.RConsole_MainTab_error_CannotRunHelp_message, e ),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID, -1,
					Messages.RConsole_MainTab_error_WhileRunningHelp_message, e.getTargetException() ),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InterruptedException e) {
			// canceled
		}
	}
	
	@Override
	public void dispose() {
		if (fWithHelp) {
			HelpRequestor.closeHelpTray((TrayDialog) getLaunchConfigurationDialog());
		}
		super.dispose();
	}
	
}
