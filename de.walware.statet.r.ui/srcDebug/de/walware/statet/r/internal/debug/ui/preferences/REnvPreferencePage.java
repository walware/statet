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

package de.walware.statet.r.internal.debug.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.dialogs.ButtonGroup;
import de.walware.ecommons.ui.preferences.ConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.ui.RUI;


/**
 * Preference page for R (Environment) configuration of the workbench.
 */
public class REnvPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	
	public static final String PREF_PAGE_ID = "de.walware.statet.r.preferencePages.REnvironmentPage"; //$NON-NLS-1$
	
	
	private TableViewer fListViewer;
	private ButtonGroup<REnvConfiguration.WorkingCopy> fListButtons;
	
	private Image fEnvIcon;
	private Image fEnvDefaultIcon;
	
	private IObservableList fList;
	private IObservableValue fDefault;
	
	
	public REnvPreferencePage() {
	}
	
	public void init(final IWorkbench workbench) {
		fList = new WritableList();
		fDefault = new WritableValue();
	}
	
	
	private boolean isDefaultREnv(final REnvConfiguration config) {
		return (fDefault.getValue() == config);
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		createImages();
		
		final Composite pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		final Label label = new Label(pageComposite, SWT.LEFT);
		label.setText(Messages.REnv_REnvList_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		{	// Table area
			final Composite composite = new Composite(pageComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Composite table = createTable(composite);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			fListButtons = new ButtonGroup<REnvConfiguration.WorkingCopy>(composite) {
				@Override
				protected REnvConfiguration.WorkingCopy edit1(final REnvConfiguration.WorkingCopy config, final boolean newConfig) {
					REnvConfiguration.WorkingCopy editConfig;
					final List<REnvConfiguration> existingConfigs = new ArrayList<REnvConfiguration>(fList);
					if (newConfig) {
						editConfig = new REnvConfiguration.WorkingCopy();
						if (config != null) { // copy...
							editConfig.load(config);
						}
					}
					else {
						editConfig = config.createWorkingCopy();
						existingConfigs.remove(config);
					}
					final REnvConfigDialog dialog = new REnvConfigDialog(getShell(),
							editConfig, newConfig, existingConfigs);
					if (dialog.open() == Dialog.OK && editConfig.isDirty()) {
						if (newConfig) {
							return editConfig;
						}
						else {
							config.load(editConfig);
							return config;
						}
					}
					return null;
				}
				@Override
				public void updateState() {
					super.updateState();
					REnvPreferencePage.this.updateStatus();
				}
			};
			fListButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			fListButtons.addAddButton();
			fListButtons.addCopyButton();
			fListButtons.addEditButton();
			fListButtons.addDeleteButton();
			fListButtons.addSeparator();
			fListButtons.addDefaultButton();
			
			fListButtons.connectTo(fListViewer, fList, fDefault);
			fListViewer.setInput(fList);
		}
		
		loadValues(PreferencesUtil.getInstancePrefs());
		fListButtons.refresh();
		
		applyDialogFont(pageComposite);
		return pageComposite;
	}
	
	private void createImages() {
		final Image baseImage = RUI.getImage(RUI.IMG_OBJ_R_RUNTIME_ENV);
		fEnvIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, null, null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
		fEnvDefaultIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, StatetImages.getDescriptor(StatetImages.OVR_DEFAULT_MARKER), null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
	}
	
	protected Composite createTable(final Composite parent) {
		final TableComposite composite = new ViewerUtil.TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		fListViewer = composite.viewer;
		composite.table.setHeaderVisible(true);
		composite.table.setLinesVisible(true);
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText(Messages.REnv_NameColumn_name);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(final Object element) {
					final REnvConfiguration config = (REnvConfiguration) element;
					return (fDefault.getValue() == config) ? fEnvDefaultIcon : fEnvIcon;
				}
				@Override
				public String getText(final Object element) {
					final REnvConfiguration config = (REnvConfiguration) element;
					return config.getName();
				}
			});
		}
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText(Messages.REnv_LocationColumn_name);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(final Object element) {
					return super.getImage(element);
				}
				@Override
				public String getText(final Object element) {
					final REnvConfiguration config = (REnvConfiguration) element;
					return config.getRHome();
				}
			});
		}
		
		composite.viewer.setContentProvider(new ArrayContentProvider());
		// Sorter
		composite.viewer.setComparator(new ViewerComparator() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				return getComparator().compare(((REnvConfiguration.WorkingCopy) e1).getName(), ((REnvConfiguration.WorkingCopy) e2).getName());
			}
		});
		
		return composite;
	}
	
	@Override
	public void dispose() {
		if (fEnvIcon != null) {
			fEnvIcon.dispose();
			fEnvIcon = null;
		}
		if (fEnvDefaultIcon != null) {
			fEnvDefaultIcon.dispose();
			fEnvDefaultIcon = null;
		}
		super.dispose();
	}
	
	@Override
	protected void performDefaults() {
	}
	
	@Override
	public boolean performOk() {
		if (fListButtons.isDirty()) {
			return saveValues(false);
		}
		return true;
	}
	
	@Override
	protected void performApply() {
		saveValues(true);
	}
	
	
	private void updateStatus() {
		if (fDefault.getValue() == null) {
			setMessage(Messages.REnv_warning_NoDefaultConfiguration_message, IStatus.WARNING);
			return;
		}
		setMessage(null);
	}
	
	private boolean saveValues(final boolean saveStore) {
		try {
			final REnvConfiguration.WorkingCopy defaultREnv = (REnvConfiguration.WorkingCopy) fDefault.getValue();
			final String defaultConfigName = (defaultREnv != null) ? defaultREnv.getName() : null;
			final String[] groupIds = RCore.getREnvManager().set((REnvConfiguration[]) fList.toArray(new REnvConfiguration.WorkingCopy[fList.size()]), defaultConfigName);
			if (groupIds != null) {
				ConfigurationBlock.scheduleChangeNotification(
						(IWorkbenchPreferenceContainer) getContainer(), groupIds, saveStore);
			}
			return true;
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					-1, Messages.REnv_error_Saving_message, e),
					StatusManager.LOG | StatusManager.SHOW);
			return false;
		}
	}
	
	private void loadValues(final IPreferenceAccess prefs) {
		fList.clear();
		fDefault.setValue(null);
		
		final IREnvManager manager = RCore.getREnvManager();
		final String[] names = manager.getNames();
		final REnvConfiguration defaultConfig = manager.getDefault();
		final String defaultConfigName = (defaultConfig != null) ? defaultConfig.getName() : null;
		for (final String name : names) {
			final REnvConfiguration.WorkingCopy config = manager.get(null, name).createWorkingCopy();
			fList.add(config);
			if (config.getName().equals(defaultConfigName)) {
				fDefault.setValue(config);
			}
		}
	}
	
}
