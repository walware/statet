/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.processing;

import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.BUILDTEX_TYPE_RCONSOLE;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.databinding.jface.RadioGroupObservable;
import de.walware.ecommons.databinding.jface.SWTMultiEnabledObservable;
import de.walware.ecommons.debug.ui.LaunchConfigTabWithDbc;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditor;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.components.CustomizableVariableSelectionDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.BuilderChooser;
import net.sourceforge.texlipse.builder.BuilderRegistry;

import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;


public class TexTab extends LaunchConfigTabWithDbc {
	
	
	private static class BuildChooserObservable extends AbstractObservableValue implements SelectionListener {
		
		private final BuilderChooser fControl;
		private Integer fCurrentBuilder;
		
		public BuildChooserObservable(final BuilderChooser control) {
			fControl = control;
			fCurrentBuilder = fControl.getSelectedBuilder();
			fControl.addSelectionListener(this);
		}
		
		@Override
		public Object getValueType() {
			return Integer.class;
		}
		
		@Override
		protected Object doGetValue() {
			return fCurrentBuilder;
		}
		
		@Override
		protected void doSetValue(final Object value) {
			if (value instanceof Integer) {
				fCurrentBuilder = (Integer) value;
				fControl.setSelectedBuilder(fCurrentBuilder);
				return;
			}
		}
		
		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final int oldValue = fCurrentBuilder;
			fCurrentBuilder = fControl.getSelectedBuilder();
			fireValueChange(Diffs.createValueDiff(oldValue, fCurrentBuilder));
		}
		
	}
	
	
	public static final String NS = "de.walware.statet.r.debug/Tex/"; //$NON-NLS-1$
	
	public static final String ATTR_OPENTEX_ENABLED = NS + "OpenTex.enabled"; //$NON-NLS-1$
	/** @Deprecated replaced by {@link #ATTR_BUILDTEX_TYPE} */
	public static final String ATTR_BUILDTEX_ENABLED = NS + "BuildTex.enabled"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_TYPE = NS + "BuildTex.type"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_ECLIPSE_BUILDERID = NS + "BuildTex.builderId"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_R_COMMANDS = NS + "BuildTex.rCommands"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_FORMAT = NS + "BuildTex.format"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_OUTPUTDIR = NS + "BuildTex.outputDir"; //$NON-NLS-1$
	
	public static final int OPEN_OFF = -1;
	public static final int OPEN_ALWAYS = 0;
	
	private Button fOpenTexFileControl;
	private Button fOpenTexFileOnErrorsControl;
	
	private ResourceInputComposite fOutputDirControl;
	
	private Button fBuildTexFileDisabledControl;
	private Button fBuildTexFileEclipseControl;
	private BuilderChooser fBuildTexTypeChooser;
	private Button fBuildTexFileRControl;
	private SnippetEditor fConsoleCommandEditor;
	private Combo fOutputFormatControl;
	
	private WritableValue fOutputDirValue;
	private WritableValue fOutputFormatValue;
	private WritableValue fOpenTexEnabledValue;
	private WritableValue fOpenTexOnErrorsEnabledValue;
	private WritableValue fBuildTexTypeValue;
	private WritableValue fBuildTexBuilderIdValue;
	private WritableValue fBuildTexRCommandsValue;
	
	
	public TexTab() {
	}
	
	
	@Override
	public String getName() {
		return Messages.Processing_TexTab_label;
	}
	
	@Override
	public Image getImage() {
		return SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_BUILDTEX);
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());
		
		final Group group = new Group(mainComposite, SWT.NONE);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		group.setText(Messages.TexTab_label);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fOpenTexFileControl = new Button(group, SWT.CHECK);
		fOpenTexFileControl.setText(Messages.TexTab_OpenTex_label);
		fOpenTexFileControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fOpenTexFileOnErrorsControl = new Button(group, SWT.CHECK);
		fOpenTexFileOnErrorsControl.setText(Messages.TexTab_OpenTex_OnlyOnErrors_label);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		fOpenTexFileOnErrorsControl.setLayoutData(gd);
		
		LayoutUtil.addSmallFiller(group, false);
		
		createOutputOptions(group);
		
		LayoutUtil.addSmallFiller(group, false);
		
		createBuildOptions(group);
		
		initBindings();
	}
	
	private void createOutputOptions(final Group composite) {
		{	final Label label = new Label(composite, SWT.NONE);
			label.setText(Messages.TexTab_OutputDir_longlabel);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		fOutputDirControl = new ResourceInputComposite(composite, 
				ResourceInputComposite.STYLE_TEXT, 
				ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_SAVE, 
				Messages.TexTab_OutputDir_label) {
			
			@Override
			protected void fillMenu(final Menu menu) {
				super.fillMenu(menu);
				{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(Messages.Insert_SweaveDirVariable_label);
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							insertText("${container_loc:${"+RweaveTexLaunchDelegate.VARNAME_SWEAVE_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
							getTextControl().setFocus();
						}
					});
				}
				{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(Messages.Insert_LatexDirVariable_label);
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							insertText("${container_loc:${"+RweaveTexLaunchDelegate.VARNAME_LATEX_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
							getTextControl().setFocus();
						}
					});
				}
			}
		};
		fOutputDirControl.setShowInsertVariable(true,
				DialogUtil.DEFAULT_INTERACTIVE_FILTERS,
				new ConstList<IStringVariable>(
						RweaveTexLaunchDelegate.VARIABLE_SWEAVE_FILE,
						RweaveTexLaunchDelegate.VARIABLE_LATEX_FILE ));
		fOutputDirControl.getValidator().setOnEmpty(IStatus.OK);
		fOutputDirControl.getValidator().setOnExisting(IStatus.OK);
		fOutputDirControl.getValidator().setOnFile(IStatus.ERROR);
		fOutputDirControl.getValidator().setOnLateResolve(IStatus.OK);
		fOutputDirControl.getValidator().setOnNotLocal(IStatus.ERROR);
		fOutputDirControl.getValidator().setIgnoreRelative(true);
		fOutputDirControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		{	final Composite lineComposite = new Composite(composite, SWT.NONE);
			lineComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			lineComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			{	final Label label = new Label(lineComposite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.TexTab_OutputFormat_label);
			}
			{	fOutputFormatControl = new Combo(lineComposite, SWT.BORDER | SWT.DROP_DOWN);
				final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
				gd.widthHint = LayoutUtil.hintWidth(fOutputFormatControl, 3);
				fOutputFormatControl.setLayoutData(gd);
				fOutputFormatControl.setItems(new String[] { "dvi", "pdf" }); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	private void createBuildOptions(final Composite composite) {
		// Disabled
		fBuildTexFileDisabledControl = new Button(composite, SWT.RADIO);
		fBuildTexFileDisabledControl.setText(Messages.TexTab_BuildDisabled_label);
		fBuildTexFileDisabledControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fBuildTexFileDisabledControl.setSelection(true);
		
		// Eclipse/TeXlipse
		fBuildTexFileEclipseControl = new Button(composite, SWT.RADIO);
		fBuildTexFileEclipseControl.setText(Messages.TexTab_BuildEclipse_label);
		fBuildTexFileEclipseControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		{	fBuildTexTypeChooser = new BuilderChooser(composite);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			fBuildTexTypeChooser.getControl().setLayoutData(gd);
		}
		
		// R Console
		{	fBuildTexFileRControl = new Button(composite, SWT.RADIO);
			fBuildTexFileRControl.setText(Messages.TexTab_BuildRConsole_label);
			fBuildTexFileRControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		{	final TemplateVariableProcessor templateVariableProcessor = new TemplateVariableProcessor();
			final RSourceViewerConfigurator configurator = new RTemplateSourceViewerConfigurator(
					null, templateVariableProcessor );
			fConsoleCommandEditor = new SnippetEditor(configurator, null, null, true) {
				@Override
				protected void fillToolMenu(final Menu menu) {
					{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText(SharedMessages.InsertVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								final CustomizableVariableSelectionDialog dialog = new CustomizableVariableSelectionDialog(getTextControl().getShell());
								dialog.addVariableFilter(DialogUtil.EXCLUDE_JAVA_FILTER);
								dialog.addAdditional(RweaveTexLaunchDelegate.VARIABLE_SWEAVE_FILE);
								dialog.addAdditional(RweaveTexLaunchDelegate.VARIABLE_LATEX_FILE);
								dialog.addAdditional(RweaveTexLaunchDelegate.VARIABLE_OUTPUT_FILE);
								if (dialog.open() != Dialog.OK) {
									return;
								}
								final String variable = dialog.getVariableExpression();
								if (variable == null) {
									return;
								}
								getTextControl().insert(variable);
								getTextControl().setFocus();
							}
						});
					}
					{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText(Messages.Insert_LatexFileVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								getTextControl().insert("${resource_loc:${"+RweaveTexLaunchDelegate.VARNAME_LATEX_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
								getTextControl().setFocus();
							}
						});
					}
					{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText(Messages.Insert_OutputDirVariable_label);
						item.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								getTextControl().insert("${container_loc:${"+RweaveTexLaunchDelegate.VARNAME_OUTPUT_FILE+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
								getTextControl().setFocus();
							}
						});
					}
				}
			};
			fConsoleCommandEditor.create(composite, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd.heightHint = LayoutUtil.hintHeight(fConsoleCommandEditor.getSourceViewer().getTextWidget(), 5);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			fConsoleCommandEditor.getControl().setLayoutData(gd);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fOpenTexEnabledValue = new WritableValue(realm, false, Boolean.class);
		fOpenTexOnErrorsEnabledValue = new WritableValue(realm, false, Boolean.class);
		fOutputDirValue = new WritableValue(realm, null, String.class);
		fBuildTexTypeValue = new WritableValue(realm, 0, Integer.class);
		fBuildTexBuilderIdValue = new WritableValue(realm, 0, Integer.class);
		fBuildTexRCommandsValue = new WritableValue(realm, "", String.class); //$NON-NLS-1$
		fOutputFormatValue = new WritableValue(realm, "", String.class); //$NON-NLS-1$
		
		final ISWTObservableValue openObs = SWTObservables.observeSelection(fOpenTexFileControl);
		dbc.bindValue(openObs, fOpenTexEnabledValue, null, null);
		dbc.bindValue(SWTObservables.observeSelection(fOpenTexFileOnErrorsControl), fOpenTexOnErrorsEnabledValue, null, null);
		dbc.bindValue(new RadioGroupObservable(realm, new Button[] {
				fBuildTexFileDisabledControl, fBuildTexFileEclipseControl, fBuildTexFileRControl
		}), fBuildTexTypeValue, null, null);
		dbc.bindValue(new BuildChooserObservable(fBuildTexTypeChooser), fBuildTexBuilderIdValue, null, null);
		dbc.bindValue(SWTObservables.observeText(fConsoleCommandEditor.getTextControl(), SWT.Modify), fBuildTexRCommandsValue, null, null);
		dbc.bindValue(SWTObservables.observeText(fOutputFormatControl), fOutputFormatValue);
		
		fBuildTexBuilderIdValue.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				updateFormat();
			}
		});
		fBuildTexTypeValue.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				final Object newValue = event.diff.getNewValue();
				final int typeId = (newValue instanceof Integer) ? ((Integer) newValue).intValue() : -1;
				switch (typeId) {
				case BUILDTEX_TYPE_ECLIPSE:
					updateFormat();
					break;
				case BUILDTEX_TYPE_RCONSOLE:
					if (RweaveTexLaunchDelegate.DEFAULT_BUILDTEX_R_COMMANDS.equals(fBuildTexRCommandsValue.getValue())) {
						fOutputFormatValue.setValue(RweaveTexLaunchDelegate.DEFAULT_BUILDTEX_FORMAT);
					}
					break;
				}
				
			}
		});
		
		// Enablement
		dbc.bindValue(SWTObservables.observeEnabled(fOpenTexFileOnErrorsControl), openObs, null, null);
		final Composite group = fBuildTexTypeChooser.getControl();
		dbc.bindValue(new SWTMultiEnabledObservable(realm, group.getChildren(), null), 
				new ComputedValue(realm, Boolean.class) {
					@Override
					protected Object calculate() {
						return (((Integer) fBuildTexTypeValue.getValue()) == RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE);
					}
				}, null, null);
		dbc.bindValue(new SWTMultiEnabledObservable(realm, new Control[] { fConsoleCommandEditor.getControl() }, null),
				new ComputedValue(realm, Boolean.class) {
					@Override
					protected Object calculate() {
						return (((Integer) fBuildTexTypeValue.getValue()) == RweaveTexLaunchDelegate.BUILDTEX_TYPE_RCONSOLE);
					}
				}, null, null);
		dbc.bindValue(new SWTMultiEnabledObservable(realm, new Control[] { fOutputFormatControl }, null),
				new ComputedValue(realm, Boolean.class) {
			@Override
			protected Object calculate() {
				return (((Integer) fBuildTexTypeValue.getValue()) != RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE);
			}
		}, null, null);
		
		dbc.bindValue(fOutputDirControl.getObservable(), fOutputDirValue, 
				new UpdateValueStrategy().setAfterGetValidator(fOutputDirControl.getValidator()), null);
	}
	
	private void updateFormat() {
		final Object texBuilderId = fBuildTexBuilderIdValue.getValue();
		if (texBuilderId instanceof Integer) {
			final Builder builder = BuilderRegistry.get((Integer) texBuilderId);
			if (builder != null) {
				fOutputFormatValue.setValue(builder.getOutputFormat());
			}
		}
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_OPENTEX_ENABLED, OPEN_OFF);
		configuration.setAttribute(ATTR_BUILDTEX_TYPE, RweaveTexLaunchDelegate.DEFAULT_BUILDTEX_TYPE);
		configuration.setAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID, 0);
		configuration.setAttribute(ATTR_BUILDTEX_OUTPUTDIR, ""); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		int open = OPEN_OFF;
		try {
			open = configuration.getAttribute(ATTR_OPENTEX_ENABLED, open);
		} catch (final CoreException e) {
			logReadingError(e);
		}
		fOpenTexEnabledValue.setValue(open >= OPEN_ALWAYS);
		fOpenTexOnErrorsEnabledValue.setValue(open > OPEN_ALWAYS);
		
		int buildType = RweaveTexLaunchDelegate.DEFAULT_BUILDTEX_TYPE;
		try {
			buildType = configuration.getAttribute(ATTR_BUILDTEX_TYPE, -2);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		if (buildType == -2) {
			try {
				buildType = configuration.getAttribute(ATTR_BUILDTEX_ENABLED, false) ?
						RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE :
						RweaveTexLaunchDelegate.BUILDTEX_TYPE_DISABLED;
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
		}
		fBuildTexTypeValue.setValue(buildType);
		
		int texBuilderId = 0;
		try {
			texBuilderId = configuration.getAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID, texBuilderId);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		fBuildTexBuilderIdValue.setValue(texBuilderId);
		
		String rCommands = RweaveTexLaunchDelegate.DEFAULT_BUILDTEX_R_COMMANDS;
		try {
			rCommands = configuration.getAttribute(ATTR_BUILDTEX_R_COMMANDS, rCommands);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		fBuildTexRCommandsValue.setValue(rCommands);
		
		if (buildType == BUILDTEX_TYPE_ECLIPSE) {
			updateFormat();
		}
		else {
			String format = RweaveTexLaunchDelegate.DEFAULT_BUILDTEX_FORMAT;
			try {
				format = configuration.getAttribute(ATTR_BUILDTEX_FORMAT, format);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fOutputFormatValue.setValue(format);
		}
		
		String outputDir = ""; //$NON-NLS-1$
		try {
			outputDir = configuration.getAttribute(ATTR_BUILDTEX_OUTPUTDIR, outputDir);
		}
		catch (final CoreException e) {
			logReadingError(e);
		}
		fOutputDirValue.setValue(outputDir);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		int open = OPEN_OFF;
		if ((Boolean) fOpenTexEnabledValue.getValue()) {
			open = ((Boolean) fOpenTexOnErrorsEnabledValue.getValue()) ? IMarker.SEVERITY_ERROR : OPEN_ALWAYS;
		}
		configuration.setAttribute(ATTR_OPENTEX_ENABLED, open);
		
		final int buildType = (Integer) fBuildTexTypeValue.getValue();
		configuration.setAttribute(ATTR_BUILDTEX_TYPE, buildType);
		
		final Integer texBuilderId = (Integer) fBuildTexBuilderIdValue.getValue();
		if (texBuilderId != null
				&& (buildType == RweaveTexLaunchDelegate.BUILDTEX_TYPE_ECLIPSE || texBuilderId.intValue() != 0) ) {
			configuration.setAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID, texBuilderId.intValue());
		}
		else {
			configuration.removeAttribute(ATTR_BUILDTEX_ECLIPSE_BUILDERID);
		}
		
		final String rCommands = (String) fBuildTexRCommandsValue.getValue();
		if (buildType == RweaveTexLaunchDelegate.BUILDTEX_TYPE_RCONSOLE || !rCommands.equals(RweaveTexLaunchDelegate.DEFAULT_BUILDTEX_R_COMMANDS)) {
			configuration.setAttribute(ATTR_BUILDTEX_R_COMMANDS, rCommands);
		}
		else {
			configuration.removeAttribute(ATTR_BUILDTEX_R_COMMANDS);
		}
		
		final String format = (String) fOutputFormatValue.getValue();
		configuration.setAttribute(ATTR_BUILDTEX_FORMAT, format);
		
		configuration.setAttribute(ATTR_BUILDTEX_OUTPUTDIR, (String) fOutputDirValue.getValue());
	}
	
	
	public boolean addOutputFormatListener(final IChangeListener listener) {
		if (fOutputFormatValue != null) {
			fOutputFormatValue.addChangeListener(listener);
			return true;
		}
		return false;
	}
	
	public String getOutputFormat() {
		return (String) fOutputFormatValue.getValue();
	}
	
}
