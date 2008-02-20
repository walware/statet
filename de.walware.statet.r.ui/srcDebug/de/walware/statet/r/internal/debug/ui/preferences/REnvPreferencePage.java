/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.PreferencesUtil;
import de.walware.eclipsecommons.ui.SharedMessages;
import de.walware.eclipsecommons.ui.dialogs.groups.TableOptionButtonsGroup;
import de.walware.eclipsecommons.ui.preferences.AbstractConfigurationBlock;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.ui.RUI;


/**
 * 
 */
public class REnvPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	
	public static final String PREF_PAGE_ID = "de.walware.statet.r.preferencePages.RInteractionPreferencePage"; //$NON-NLS-1$
	
	
	static class REnvConfig extends REnvConfiguration {
		
		public REnvConfig() {
			loadDefaults();
		}
		
		public REnvConfig(final REnvConfiguration config) {
			setId(config.getId());
			load(config);
		}
		
		@Override
		public void setName(final String label) {
			super.setName(label);
		}
		
		@Override
		public void setRHome(final String label) {
			super.setRHome(label);
		}
	}
	
	private class REnvTableGroup extends TableOptionButtonsGroup<REnvConfig> {
		
		private static final int IDX_ADD = 0;
		private static final int IDX_DUBLICATE = 1;
		private static final int IDX_EDIT = 2;
		private static final int IDX_REMOVE = 3;
		private static final int IDX_DEFAULT = 5;
		
		REnvTableGroup() {
			super(new String[] {
					SharedMessages.CollectionEditing_AddItem_label,
					Messages.REnv_Copy_label,
					SharedMessages.CollectionEditing_EditItem_label,
					SharedMessages.CollectionEditing_RemoveItem_label,
					null,
					SharedMessages.CollectionEditing_DefaultItem_label,
			} );
			setDefaultButton(IDX_EDIT);
			setRemoveButton(IDX_REMOVE);
		}
		
		@Override
		protected void createTableColumns(final TableViewer viewer, final Table table, final TableLayout layout) {
//			PixelConverter conv = new PixelConverter(table);
			TableViewerColumn col;
			
			col = new TableViewerColumn(viewer, SWT.LEFT);
			col.getColumn().setText(Messages.REnv_NameColumn_name);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(final Object element) {
					final REnvConfiguration config = (REnvConfiguration) element;
					return (config == fDefaultREnv) ? fEnvDefaultIcon : fEnvIcon;
				}
				@Override
				public String getText(final Object element) {
					final REnvConfiguration config = (REnvConfiguration) element;
					return config.getName();
				}
			});
			layout.addColumnData(new ColumnWeightData(1));
			
			col = new TableViewerColumn(viewer, SWT.LEFT);
			col.getColumn().setText(Messages.REnv_LocationColumn_name);
			col.setLabelProvider(new ColumnLabelProvider() {
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
			layout.addColumnData(new ColumnWeightData(1));
			
			// Sorter
			viewer.setComparator(new ViewerComparator() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(final Viewer viewer, final Object e1, final Object e2) {
					return getComparator().compare(((REnvConfig) e1).getName(), ((REnvConfig) e2).getName());
				}
			});
		}
		
		
		@Override
		protected void handleSelection(final REnvConfig config, final IStructuredSelection rawSelection) {
			fButtonGroup.enableButton(IDX_DUBLICATE, (config != null) );
			fButtonGroup.enableButton(IDX_EDIT, (config != null) );
			fButtonGroup.enableButton(IDX_DEFAULT, (config != null) && !isDefaultREnv(config));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void handleButtonPressed(final int buttonIdx, final REnvConfig config, final IStructuredSelection rawSelection) {
			switch (buttonIdx) {
			case IDX_ADD:
				doEdit(true, null);
				break;
			
			case IDX_DUBLICATE:
				if (config != null) {
					doEdit(true, config);
				}
				break;
				
			case IDX_EDIT:
				if (config != null) {
					doEdit(false, config);
				}
				break;
				
			case IDX_REMOVE:
				if (!rawSelection.isEmpty())
					doRemove(rawSelection.toList());
				break;
				
			case IDX_DEFAULT:
				if (config != null) {
					doSetDefault(config);
				}
				break;
			}
			updateStatus();
		}
		
	}
	
	
	private REnvTableGroup fEnvTableGroup;
	
	private boolean fIsDirty;
	private REnvConfiguration fDefaultREnv;
	
	private Image fEnvIcon;
	private Image fEnvDefaultIcon;
	
	
	/**
	 * 
	 */
	public REnvPreferencePage() {
	}
	
	public void init(final IWorkbench workbench) {
		fEnvTableGroup = new REnvTableGroup();
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		createImages();
		
		final Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout());
		final Label label = new Label(page, SWT.LEFT);
		label.setText(Messages.REnv_REnvList_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fEnvTableGroup.createGroup(page, 1);
		
		UIAccess.getDisplay(getShell()).asyncExec(new Runnable() {
			public void run() {
				final Button defaultButton = getDefaultsButton();
				if (UIAccess.isOkToUse(defaultButton)) {
					defaultButton.setEnabled(false);
				}
			}
		});
		
		loadValues(PreferencesUtil.getInstancePrefs());
		fEnvTableGroup.initFields();
		
		applyDialogFont(page);
		return page;
	}
	
	private void createImages() {
		final Image baseImage = RUI.getImage(RUI.IMG_OBJ_R_ENVIRONMENT);
		fEnvIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, null, null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
		fEnvDefaultIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, StatetImages.getDescriptor(StatetImages.OVR_DEFAULT_MARKER), null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
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
		if (fIsDirty) {
			return saveValues(false);
		}
		return true;
	}
	
	@Override
	protected void performApply() {
		saveValues(true);
	}
	
	
	private boolean isDefaultREnv(final REnvConfiguration config) {
		return (fDefaultREnv == config);
	}
	
	private void doSetDefault(final REnvConfig config) {
		fIsDirty = true;
		fDefaultREnv = config;
		
		fEnvTableGroup.refresh();
	}
	
	private void doEdit(final boolean newConfig, final REnvConfig config) {
		REnvConfig editConfig;
		final List<REnvConfiguration> existingConfigs = new ArrayList<REnvConfiguration>(fEnvTableGroup.getListModel());
		if (newConfig) {
			editConfig = new REnvConfig();
			if (config != null) { // copy...
				editConfig.load(config);
			}
		}
		else {
			editConfig = new REnvConfig(config);
			existingConfigs.remove(config);
		}
		final REnvConfigDialog dialog = new REnvConfigDialog(getShell(),
				editConfig, newConfig, existingConfigs);
		if (dialog.open() == Dialog.OK && editConfig.isDirty()) {
			fIsDirty = true;
			if (newConfig) {
				if (fEnvTableGroup.getListModel().size() == 0) {
					fDefaultREnv = editConfig;
				}
				fEnvTableGroup.addItem(editConfig);
			}
			else {
				config.load(editConfig);
				fEnvTableGroup.refresh();
			}
		}
	}
	
	private void doRemove(final List<REnvConfig> configs) {
		fIsDirty = true;
		if (fDefaultREnv != null && configs.contains(fDefaultREnv)) {
			fDefaultREnv = null;
		}
		fEnvTableGroup.removeItems(configs);
	}
	
	private void updateStatus() {
		if (fDefaultREnv == null) {
			setMessage(Messages.REnv_warning_NoDefaultConfiguration_message, IStatus.WARNING);
			return;
		}
		setMessage(null);
	}
	
	private boolean saveValues(final boolean saveStore) {
		try {
			final List<REnvConfig> configs = fEnvTableGroup.getListModel();
			final String defaultConfigName = (fDefaultREnv != null) ? fDefaultREnv.getName() : null;
			final String[] contexts = RCore.getREnvManager().set(configs.toArray(new REnvConfig[configs.size()]), defaultConfigName);
			if (contexts != null) {
				AbstractConfigurationBlock.scheduleChangeNotification(
						(IWorkbenchPreferenceContainer) getContainer(), contexts, saveStore);
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
		fEnvTableGroup.getListModel().clear();
		fDefaultREnv = null;
		
		final IREnvManager manager = RCore.getREnvManager();
		final String[] names = manager.getNames();
		final REnvConfiguration defaultConfig = manager.getDefault();
		final String defaultConfigName = (defaultConfig != null) ? defaultConfig.getName() : null;
		for (final String name : names) {
			final REnvConfig config = new REnvConfig(manager.get(null, name));
			fEnvTableGroup.getListModel().add(config);
			if (config.getName().equals(defaultConfigName)) {
				fDefaultREnv = config;
			}
		}
	}
	
}
