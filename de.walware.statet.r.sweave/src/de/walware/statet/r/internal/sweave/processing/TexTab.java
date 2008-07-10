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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.walware.eclipsecommons.ui.databinding.LaunchConfigTabWithDbc;
import de.walware.eclipsecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.BuilderChooser;
import net.sourceforge.texlipse.builder.BuilderRegistry;

import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;


public class TexTab extends LaunchConfigTabWithDbc {
	
	
	private static class BuildChooserObservable extends AbstractObservableValue implements SelectionListener {
		
		private BuilderChooser fControl;
		private Integer fCurrentBuilder;
		
		public BuildChooserObservable(final BuilderChooser control) {
			fControl = control;
			fCurrentBuilder = fControl.getSelectedBuilder();
			fControl.addSelectionListener(this);
		}
		
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
		
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		public void widgetSelected(final SelectionEvent e) {
			final int oldValue = fCurrentBuilder;
			fCurrentBuilder = fControl.getSelectedBuilder();
			fireValueChange(Diffs.createValueDiff(oldValue, fCurrentBuilder));
		}
		
	}
	
	
	public static final String NS = "de.walware.statet.r.debug/Tex/"; //$NON-NLS-1$
	public static final String ATTR_OPENTEX_ENABLED = NS + "OpenTex.enabled"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_ENABLED = NS + "BuildTex.enabled"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_BUILDERID = NS + "BuildTex.builderId"; //$NON-NLS-1$
	public static final String ATTR_BUILDTEX_OUTPUTDIR = NS + "BuildTex.outputDir"; //$NON-NLS-1$
	
	public static final int OPEN_OFF = -1;
	public static final int OPEN_ALWAYS = 0;
	
	
	private Button fOpenTexFileControl;
	private Button fOpenTexFileOnErrorsControl;
	private Button fBuildTexFileControl;
	private BuilderChooser fBuildTexTypeChooser;
	private ChooseResourceComposite fOutputDirControl;
	
	private WritableValue fOpenTexEnabledValue;
	private WritableValue fOpenTexOnErrorsEnabledValue;
	private WritableValue fBuildTexEnabledValue;
	private WritableValue fBuildTexBuilderIdValue;
	private WritableValue fOutputDirValue;
	
	
	public TexTab() {
	}
	
	
	public String getName() {
		return Messages.Processing_TexTab_label;
	}
	
	@Override
	public Image getImage() {
		return SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_BUILDTEX);
	}
	
	
	public void createControl(final Composite parent) {
		GridData gd;
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(LayoutUtil.applyTabDefault(new GridLayout(), 1));
		
		final Label label = new Label(mainComposite, SWT.NONE);
		label.setText(Messages.TexTab_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		fOpenTexFileControl = new Button(mainComposite, SWT.CHECK);
		fOpenTexFileControl.setText(Messages.TexTab_OpenTex_label);
		fOpenTexFileControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fOpenTexFileOnErrorsControl = new Button(mainComposite, SWT.CHECK);
		fOpenTexFileOnErrorsControl.setText(Messages.TexTab_OpenTex_OnlyOnErrors_label);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		fOpenTexFileOnErrorsControl.setLayoutData(gd);
		
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		fBuildTexFileControl = new Button(mainComposite, SWT.CHECK);
		fBuildTexFileControl.setText(Messages.TexTab_BuildTex_label);
		fBuildTexFileControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		fBuildTexTypeChooser = new BuilderChooser(mainComposite);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		fBuildTexTypeChooser.getControl().setLayoutData(gd);
		
		fOutputDirControl = new ChooseResourceComposite(mainComposite, 
				ChooseResourceComposite.STYLE_TEXT | ChooseResourceComposite.STYLE_LABEL, 
				ChooseResourceComposite.MODE_DIRECTORY | ChooseResourceComposite.MODE_SAVE, 
				Messages.TexTab_OutputDir_label) {
			
			@Override
			protected void fillMenu(final Menu menu) {
				super.fillMenu(menu);
				
				MenuItem item;
				item = new MenuItem(menu, SWT.PUSH);
				item.setText(Messages.TexTab_OutputDir_InsertTexPath_label);
				item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						insertText(TexPathConfig.TEXFILE_PATH_VARIABLE);
						getTextControl().setFocus();
					}
				});
				
				item = new MenuItem(menu, SWT.PUSH);
				item.setText(Messages.TexTab_OutputDir_InsertSweavePath_label);
				item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						insertText(TexPathConfig.SOURCEFILE_PATH_VARIABLE);
						getTextControl().setFocus();
					}
				});
			}
		};
		fOutputDirControl.showInsertVariable(true);
		fOutputDirControl.getValidator().setOnEmpty(IStatus.OK);
		fOutputDirControl.getValidator().setOnExisting(IStatus.OK);
		fOutputDirControl.getValidator().setOnFile(IStatus.ERROR);
		fOutputDirControl.getValidator().setOnLateResolve(IStatus.OK);
		fOutputDirControl.getValidator().setOnNotLocal(IStatus.ERROR);
		fOutputDirControl.getValidator().setIgnoreRelative(true);
		fOutputDirControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		initBindings();
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fOpenTexEnabledValue = new WritableValue(realm, false, Boolean.class);
		fOpenTexOnErrorsEnabledValue = new WritableValue(realm, false, Boolean.class);
		fBuildTexEnabledValue = new WritableValue(realm, false, Boolean.class);
		fBuildTexBuilderIdValue = new WritableValue(realm, 0, Integer.class);
		fOutputDirValue = new WritableValue(realm, null, String.class);
		
		final ISWTObservableValue openObs = SWTObservables.observeSelection(fOpenTexFileControl);
		dbc.bindValue(openObs, fOpenTexEnabledValue, null, null);
		dbc.bindValue(SWTObservables.observeSelection(fOpenTexFileOnErrorsControl), fOpenTexOnErrorsEnabledValue, null, null);
		final ISWTObservableValue buildObs = SWTObservables.observeSelection(fBuildTexFileControl);
		dbc.bindValue(buildObs, fBuildTexEnabledValue, null, null);
		dbc.bindValue(new BuildChooserObservable(fBuildTexTypeChooser), fBuildTexBuilderIdValue, null, null);
		
		// Enablement
		dbc.bindValue(SWTObservables.observeEnabled(fOpenTexFileOnErrorsControl), openObs, null, null);
		final Composite group = fBuildTexTypeChooser.getControl();
		final Control[] controls = group.getChildren();
		for (int i = 0; i < controls.length; i++) {
			dbc.bindValue(SWTObservables.observeEnabled(controls[i]), buildObs, null, null);
		}
		
		dbc.bindValue(fOutputDirControl.createObservable(), fOutputDirValue, 
				new UpdateValueStrategy().setAfterGetValidator(fOutputDirControl.getValidator()), null);
	}
	
	
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_OPENTEX_ENABLED, OPEN_OFF);
		configuration.setAttribute(ATTR_BUILDTEX_ENABLED, true);
		configuration.setAttribute(ATTR_BUILDTEX_BUILDERID, 0);
		configuration.setAttribute(ATTR_BUILDTEX_OUTPUTDIR, ""); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		int open = OPEN_OFF;
		try {
			open = configuration.getAttribute(ATTR_OPENTEX_ENABLED, OPEN_OFF);
		} catch (final CoreException e) {
			logReadingError(e);
		}
		fOpenTexEnabledValue.setValue(open >= OPEN_ALWAYS);
		fOpenTexOnErrorsEnabledValue.setValue(open > OPEN_ALWAYS);
		
		boolean build = false;
		try {
			build = configuration.getAttribute(ATTR_BUILDTEX_ENABLED, false);
		} catch (final CoreException e) {
			logReadingError(e);
		}
		fBuildTexEnabledValue.setValue(build);
		
		int texBuilderId = 0;
		try {
			texBuilderId = configuration.getAttribute(ATTR_BUILDTEX_BUILDERID, 0);
		} catch (final CoreException e) {
			logReadingError(e);
		}
		fBuildTexBuilderIdValue.setValue(texBuilderId);
		
		String outputDir = ""; //$NON-NLS-1$
		try {
			outputDir = configuration.getAttribute(ATTR_BUILDTEX_OUTPUTDIR, ""); //$NON-NLS-1$
		} catch (final CoreException e) {
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
		
		final boolean build = (Boolean) fBuildTexEnabledValue.getValue();
		configuration.setAttribute(ATTR_BUILDTEX_ENABLED, build);
		
		final Object texBuilderId = fBuildTexBuilderIdValue.getValue();
		if (texBuilderId instanceof Integer) {
			configuration.setAttribute(ATTR_BUILDTEX_BUILDERID, ((Integer) texBuilderId).intValue());
		}
		else {
			configuration.setAttribute(ATTR_BUILDTEX_BUILDERID, 0);
		}
		
		configuration.setAttribute(ATTR_BUILDTEX_OUTPUTDIR, (String) fOutputDirValue.getValue());
	}
	
	
	public boolean addOutputFormatListener(final IChangeListener listener) {
		if (fBuildTexBuilderIdValue != null) {
			fBuildTexBuilderIdValue.addChangeListener(listener);
			return true;
		}
		return false;
	}
	
	public String getOutputFormat() {
		final Object texBuilderId = fBuildTexBuilderIdValue.getValue();
		if (texBuilderId instanceof Integer) {
			final Builder builder = BuilderRegistry.get((Integer) texBuilderId);
			if (builder != null) {
				return builder.getOutputFormat();
			}
		}
		return "-"; //$NON-NLS-1$
	}
	
}
