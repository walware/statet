/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.databinding.NotEmptyValidator;
import de.walware.ecommons.ui.dialogs.ButtonGroup;
import de.walware.ecommons.ui.dialogs.DatabindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.preferences.ConfigurationBlock;
import de.walware.ecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;
import de.walware.ecommons.variables.core.VariableFilter;

import de.walware.statet.nico.core.runtime.IResourceMapping;
import de.walware.statet.nico.internal.core.NicoPlugin;
import de.walware.statet.nico.internal.core.ResourceMapping;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;


public class ResourceMappingPreferencePage extends ConfigurationBlockPreferencePage<ResourceMappingConfigurationBlock> {
	
	
	public ResourceMappingPreferencePage() {
	}
	
	
	@Override
	protected ResourceMappingConfigurationBlock createConfigurationBlock() throws CoreException {
		return new ResourceMappingConfigurationBlock();
	}
	
	
}


class ResourceMappingConfigurationBlock extends ConfigurationBlock {
	
	
	// TODO Add up/down buttons
	
	
	private TableViewer fListViewer;
	private ButtonGroup<ResourceMapping> fListButtons;
	
	private final WritableList fList;
	
	
	public ResourceMappingConfigurationBlock() {
		fList = new WritableList();
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Realm realm = Realm.getDefault();
		final DataBindingContext dbc = new DataBindingContext();
		
		{	// Table area
			final Composite composite = new Composite(pageComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Composite table = createTable(composite);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			fListButtons = new ButtonGroup<ResourceMapping>(composite) {
				@Override
				protected ResourceMapping edit1(final ResourceMapping item, final boolean newItem) {
					final EditMappingDialog dialog = new EditMappingDialog(getShell(), item, newItem);
					if (dialog.open() == Dialog.OK) {
						return dialog.getResult();
					}
					return null;
				}
			};
			fListButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			fListButtons.addAddButton();
			fListButtons.addCopyButton();
			fListButtons.addEditButton();
			fListButtons.addDeleteButton();
			
			fListButtons.connectTo(fListViewer, fList, null);
			fListViewer.setInput(fList);
		}
		
		fList.addAll(NicoPlugin.getDefault().getMappingManager().getList());
		
		updateControls();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(pageComposite, "de.walware.statet.nico.ui.resourcemapping"); //$NON-NLS-1$
	}
	
	protected Composite createTable(final Composite parent) {
		final TableComposite composite = new ViewerUtil.TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		fListViewer = composite.viewer;
		composite.table.setHeaderVisible(true);
		composite.table.setLinesVisible(true);
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText("Local");
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final IResourceMapping mapping = (IResourceMapping) cell.getElement();
					final IFileStore fileStore = mapping.getFileStore();
					cell.setText((fileStore != null) ? fileStore.toString() : "<invalid>");
				}
			});
		}
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText("Host");
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final IResourceMapping mapping = (IResourceMapping) cell.getElement();
					cell.setText(mapping.getHost());
				}
			});
		}
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText("Remote");
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final IResourceMapping mapping = (IResourceMapping) cell.getElement();
					final IPath path = mapping.getRemotePath();
					cell.setText((path != null) ? path.toString() : "<invalid>");
				}
			});
		}
		
		composite.viewer.setContentProvider(new ArrayContentProvider());
		
		return composite;
	}
	
	protected void updateControls() {
		fListButtons.refresh();
	}
	
	@Override
	public void performDefaults() {
		if (fList.isEmpty()) {
			return;
		}
		final boolean deleteAll = MessageDialog.openQuestion(getShell(), 
				"Load Defaults", "Delete all mappings?");
		if (deleteAll) {
			fList.clear();
			updateControls();
		}
	}
	
	@Override
	public boolean performOk() {
		NicoPlugin.getDefault().getMappingManager().setPreferences(new ArrayList<ResourceMapping>(fList));
		return true;
	}
	
}

class EditMappingDialog extends ExtStatusDialog {
	
	
	private ResourceInputComposite fLocalControl;
	private Text fHostControl;
	private Text fRemoteControl;
	
	private final String fMappingId;
	private final WritableValue fLocalValue;
	private final WritableValue fHostValue;
	private final WritableValue fRemoteValue;
	
	
	public EditMappingDialog(final Shell shell, final ResourceMapping mapping, final boolean newMapping) {
		super(shell);
		setTitle("Edit Resource Mapping");
		
		fMappingId = (!newMapping) ? mapping.getId() : null;
		fLocalValue = new WritableValue(mapping != null ? mapping.getLocalText() : null, String.class);
		fHostValue = new WritableValue(mapping != null ? mapping.getHost() : null, String.class);
		fRemoteValue = new WritableValue(mapping != null ? mapping.getRemotePath().toString() : null, String.class);
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), "ResourceMappingEditDialog"); //$NON-NLS-1$
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		dialogArea.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		
		final Composite composite = dialogArea;
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Path on &Local:");
			
			fLocalControl = new ResourceInputComposite(composite, ResourceInputComposite.STYLE_TEXT,
					ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN,
					"local directory");
			fLocalControl.getValidator().setOnNotExisting(IStatus.WARNING);
			fLocalControl.setShowInsertVariable(false, new ConstList<VariableFilter>(
					VariableFilter.EXCLUDE_JAVA_FILTER,
					VariableFilter.EXCLUDE_BUILD_FILTER,
					VariableFilter.EXCLUDE_INTERACTIVE_FILTER ), null);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth((Text) fLocalControl.getTextControl(), 50);
			fLocalControl.setLayoutData(gd);
		}
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Remote &Host:");
			
			fHostControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
			fHostControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
		}
		{	final Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Path on &Remote:");
			
			fRemoteControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
			fRemoteControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		
		final DatabindingSupport databinding = new DatabindingSupport(parent);
		addBindings(databinding);
		databinding.installStatusListener(new StatusUpdater());
		
		return dialogArea;
	}
	
	protected void addBindings(final DatabindingSupport db) {
		db.getContext().bindValue(fLocalControl.getObservable(), fLocalValue,
				new UpdateValueStrategy().setAfterGetValidator(fLocalControl.getValidator()), null);
		db.getContext().bindValue(SWTObservables.observeText(fHostControl, SWT.Modify), fHostValue,
				new UpdateValueStrategy().setAfterGetValidator(new NotEmptyValidator(
						"Missing host must be specified by its hostname or IP number.")), null);
		db.getContext().bindValue(SWTObservables.observeText(fRemoteControl, SWT.Modify), fRemoteValue,
				new UpdateValueStrategy().setAfterGetValidator(new NotEmptyValidator(
						"Missing remote path.")), null);
	}
	
	public ResourceMapping getResult() {
		try {
			return new ResourceMapping(fMappingId, 
					(String) fLocalValue.getValue(),
					(String) fHostValue.getValue(),
					(String) fRemoteValue.getValue());
		}
		catch (final CoreException e) {
			return null;
		}
	}
	
}
