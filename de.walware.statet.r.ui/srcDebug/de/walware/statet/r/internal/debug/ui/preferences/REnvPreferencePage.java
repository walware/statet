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

package de.walware.statet.r.internal.debug.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.PreferencesUtil;
import de.walware.eclipsecommons.ui.SharedMessages;
import de.walware.eclipsecommons.ui.dialogs.groups.TableOptionButtonsGroup;
import de.walware.eclipsecommons.ui.preferences.AbstractConfigurationBlock;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.base.ui.util.ExceptionHandler;
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
		
		public REnvConfig(REnvConfiguration config) {
			setId(config.getId());
			load(config);
		}
		
		@Override
		public void setName(String label) {
			super.setName(label);
		}
		
		@Override
		public void setRHome(String label) {
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
		protected void createTableColumns(TableViewer viewer, Table table, TableLayout layout) {
//			PixelConverter conv = new PixelConverter(table);
			TableViewerColumn col;
			
			col = new TableViewerColumn(viewer, SWT.LEFT);
			col.getColumn().setText(Messages.REnv_NameColumn_name);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(Object element) {
					REnvConfiguration config = (REnvConfiguration) element;
					Image baseImage = RUI.getImage(RUI.IMG_OBJ_R_ENVIRONMENT);
					ImageDescriptor defaultOverlay = (isDefaultREnv(config)) ? StatetImages.getDescriptor(StatetImages.OVR_DEFAULT_MARKER) : null;
					return new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
								null, null, null, defaultOverlay, null},
								new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
				}
				@Override
				public String getText(Object element) {
					REnvConfiguration config = (REnvConfiguration) element;
					return config.getName();
				}
			});
			layout.addColumnData(new ColumnWeightData(1));

			col = new TableViewerColumn(viewer, SWT.LEFT);
			col.getColumn().setText(Messages.REnv_LocationColumn_name);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(Object element) {
					return super.getImage(element);
				}
				@Override
				public String getText(Object element) {
					REnvConfiguration config = (REnvConfiguration) element;
					return config.getRHome();
				}
			});
			layout.addColumnData(new ColumnWeightData(1));
			
			// Sorter
			viewer.setComparator(new ViewerComparator() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(Viewer viewer, Object e1, Object e2) {
					return getComparator().compare(((REnvConfig) e1).getName(), ((REnvConfig) e2).getName());
				}
			});
		}
		

		@Override
		protected void handleSelection(REnvConfig config, IStructuredSelection rawSelection) {
			fButtonGroup.enableButton(IDX_DUBLICATE, (config != null) );
			fButtonGroup.enableButton(IDX_EDIT, (config != null) );
			fButtonGroup.enableButton(IDX_DEFAULT, (config != null) && !isDefaultREnv(config));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void handleButtonPressed(int buttonIdx, REnvConfig config, IStructuredSelection rawSelection) {
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
	
	
	/**
	 * 
	 */
	public REnvPreferencePage() {
	}
	
	public void init(IWorkbench workbench) {
		fEnvTableGroup = new REnvTableGroup();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout());
		Label label = new Label(page, SWT.LEFT);
		label.setText(Messages.REnv_REnvList_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fEnvTableGroup.createGroup(page, 1);
		
		UIAccess.getDisplay(getShell()).asyncExec(new Runnable() {
			public void run() {
				Button defaultButton = getDefaultsButton();
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
	
	
	private boolean isDefaultREnv(REnvConfiguration config) {
		return (fDefaultREnv == config);
	}

	private void doSetDefault(REnvConfig config) {
		fIsDirty = true;
		fDefaultREnv = config;

		fEnvTableGroup.refresh();
	}
	
	private void doEdit(boolean newConfig, REnvConfig config) {
		REnvConfig editConfig;
		List<REnvConfiguration> existingConfigs = new ArrayList<REnvConfiguration>(fEnvTableGroup.getListModel());
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
		REnvConfigDialog dialog = new REnvConfigDialog(getShell(),
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

	private void doRemove(List<REnvConfig> configs) {
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
	
	private boolean saveValues(boolean saveStore) {
		try {
			List<REnvConfig> configs = fEnvTableGroup.getListModel();
			String defaultConfigName = (fDefaultREnv != null) ? fDefaultREnv.getName() : null;
			String[] contexts = RCore.getREnvManager().set(configs.toArray(new REnvConfig[configs.size()]), defaultConfigName);
			if (contexts != null) {
				AbstractConfigurationBlock.scheduleChangeNotification(
						(IWorkbenchPreferenceContainer) getContainer(), contexts, saveStore);
			}
			return true;
		}
		catch (CoreException e) {
			ExceptionHandler.handle(e.getStatus());
			return false;
		}
	}
	
	private void loadValues(IPreferenceAccess prefs) {
		fEnvTableGroup.getListModel().clear();
		fDefaultREnv = null;
		
		IREnvManager manager = RCore.getREnvManager();
		String[] names = manager.getNames();
		REnvConfiguration defaultConfig = manager.getDefault();
		String defaultConfigName = (defaultConfig != null) ? defaultConfig.getName() : null;
		for (String name : names) {
			REnvConfig config = new REnvConfig(manager.get(null, name));
			fEnvTableGroup.getListModel().add(config);
			if (config.getName().equals(defaultConfigName)) {
				fDefaultREnv = config;
			}
		}
	}
}
