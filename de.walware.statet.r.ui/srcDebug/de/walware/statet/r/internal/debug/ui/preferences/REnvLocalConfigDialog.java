/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.preferences;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.ibm.icu.text.Collator;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.debug.core.util.LaunchUtils;
import de.walware.ecommons.debug.ui.ProcessOutputCollector;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.DataAdapter;
import de.walware.ecommons.ui.components.ExtensibleTextCellEditor;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TreeComposite;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvConfiguration.Exec;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.core.renv.IRLibraryLocation.WorkingCopy;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.ui.REnvLabelProvider;
import de.walware.statet.r.ui.RUI;


/**
 * Dialog for a local standard {@link IREnvConfiguration} (<code>user-local</code>)
 */
public class REnvLocalConfigDialog extends ExtStatusDialog {
	
	
	private static final String DETECT_START = "_R-Path-And-Library-Configuration_"; //$NON-NLS-1$
	private static final String DETECT_COMMAND = "cat('"+DETECT_START+"'," //$NON-NLS-1$ //$NON-NLS-2$
			+ "Sys.getenv(\'R_HOME\')," //$NON-NLS-1$
			+ "Sys.getenv(\'R_ARCH\')," //$NON-NLS-1$
			+ "paste(.Library,collapse=.Platform$path.sep)," //$NON-NLS-1$
			+ "paste(.Library.site,collapse=.Platform$path.sep)," //$NON-NLS-1$
			+ "Sys.getenv('R_LIBS')," //$NON-NLS-1$
			+ "Sys.getenv('R_LIBS_USER')," //$NON-NLS-1$
			+ "R.home('doc')," //$NON-NLS-1$
			+ "R.home('share')," //$NON-NLS-1$
			+ "R.home('include')," //$NON-NLS-1$
			+ "R.version$arch," //$NON-NLS-1$
			+ ".Platform$OS.type," //$NON-NLS-1$
			+ "sep=intToUtf8(0x0AL));"; //$NON-NLS-1$
	
	private static final int DETECT_LENGTH = 12;
	private static final int DETECT_R_HOME = 1;
	private static final int DETECT_R_ARCHVAR = 2;
	private static final int DETECT_R_DEFAULT = 3;
	private static final int DETECT_R_SITE = 4;
	private static final int DETECT_R_OTHER = 5;
	private static final int DETECT_R_USER = 6;
	private static final int DETECT_R_DOC_DIR = 7;
	private static final int DETECT_R_SHARE_DIR = 8;
	private static final int DETECT_R_INCLUDE_DIR = 9;
	private static final int DETECT_R_ARCH = 10;
	private static final int DETECT_R_OS = 11;
	private static final Pattern DETECT_ITEM_PATTERN = RUtil.LINE_SEPARATOR_PATTERN;
	private static final Pattern DETECT_PATH_PATTERN = Pattern.compile(Pattern.quote(File.pathSeparator));
	
	
	private class RHomeComposite extends ResourceInputComposite {
		
		public RHomeComposite(final Composite parent) {
			super (parent, 
					ResourceInputComposite.STYLE_TEXT,
					ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN, 
					Messages.REnv_Detail_Location_label);
			setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
		}
		
		@Override
		protected void fillMenu(final Menu menu) {
			super.fillMenu(menu);
			
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.REnv_Detail_Location_FindAuto_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					final String[] rhome = searchRHOME();
					if (rhome != null) {
						setText(rhome[0]);
						updateArchs(false);
						final String current = fNameControl.getText().trim();
						if ((current.isEmpty() || current.equals("R")) && rhome[1] != null) { //$NON-NLS-1$
							fNameControl.setText(rhome[1]);
						}
					}
					else {
						final String name = Messages.REnv_Detail_Location_label;
						MessageDialog.openInformation(getShell(), 
								MessageUtil.removeMnemonics(name), 
								NLS.bind(Messages.REnv_Detail_Location_FindAuto_Failed_message, name));
					}
					getTextControl().setFocus();
				}
			});
			
		}
		
	}
	
	private static class RLibraryContainer {
		
		
		private IRLibraryGroup.WorkingCopy parent;
		
		private IRLibraryLocation.WorkingCopy library;
		
		
		RLibraryContainer(final IRLibraryGroup.WorkingCopy parent,
				final IRLibraryLocation.WorkingCopy library) {
			this.parent= parent;
			this.library= library;
		}
		
		
		@Override
		public int hashCode() {
			return this.library.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof RLibraryContainer)) {
				return false;
			}
			final RLibraryContainer other = (RLibraryContainer) obj;
			return (this.library == other.library);
		}
	}
	
	
	private final IREnvConfiguration.WorkingCopy fConfigModel;
	private final boolean fIsNewConfig;
	private final Set<String> fExistingNames;
	
	private Text fNameControl;
	private ResourceInputComposite fRHomeControl;
	
	private Button fLoadButton;
	
	private Combo fRArchControl;
	
	private TreeViewer fRLibrariesViewer;
	private ButtonGroup<IRLibraryLocation.WorkingCopy> fRLibrariesButtons;
	
	private ResourceInputComposite fRDocDirectoryControl;
	private ResourceInputComposite fRShareDirectoryControl;
	private ResourceInputComposite fRIncludeDirectoryControl;
	
	
	public REnvLocalConfigDialog(final Shell parent, 
			final IREnvConfiguration.WorkingCopy config, final boolean isNewConfig, 
			final Collection<IREnvConfiguration> existingConfigs) {
		super(parent, WITH_RUNNABLE_CONTEXT | ((isNewConfig) ? WITH_DATABINDING_CONTEXT :
			(WITH_DATABINDING_CONTEXT | SHOW_INITIAL_STATUS)) );
		
		fConfigModel = config;
		fIsNewConfig = isNewConfig;
		fExistingNames = new HashSet<>();
		for (final IREnvConfiguration ec : existingConfigs) {
			fExistingNames.add(ec.getName());
		}
		setTitle(fIsNewConfig ?
				Messages.REnv_Detail_AddDialog_title : 
				Messages.REnv_Detail_Edit_Dialog_title );
	}
	
	
	@Override
	public void create() {
		super.create();
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), IRUIHelpContextIds.R_ENV);
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(LayoutUtil.createDialogGrid(2));
		
		{	// Name:
			final Label label = new Label(area, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Name_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fNameControl = new Text(area, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fNameControl, 60);
			fNameControl.setLayoutData(gd);
			fNameControl.setEditable(fConfigModel.isEditable());
		}
		
		if (fConfigModel.isEditable()) {
			// Location:
			final Label label = new Label(area, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Location_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fRHomeControl = new RHomeComposite(area);
			fRHomeControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		
		LayoutUtil.addSmallFiller(area, false);
		
		{	// Architecture / Bits:
			final Label label = new Label(area, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Arch_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final Composite composite = new Composite(area, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			composite.setLayout(LayoutUtil.createCompositeGrid(3));
			
			{	fRArchControl = new Combo(composite, SWT.DROP_DOWN);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
				gd.widthHint = LayoutUtil.hintWidth(fRArchControl, 8);
				fRArchControl.setLayoutData(gd);
				fRArchControl.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						final int selectionIdx;
						if (!fRArchControl.getListVisible()
								&& (selectionIdx = fRArchControl.getSelectionIndex()) >= 0) {
							final String item = fRArchControl.getItem(selectionIdx);
						}
					}
				});
				fRArchControl.setEnabled(fConfigModel.isEditable());
			}
			
			if (fConfigModel.isEditable()) {
				fLoadButton = new Button(composite, SWT.PUSH);
				fLoadButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 2, 1));
				fLoadButton.setText(Messages.REnv_Detail_DetectSettings_label);
				fLoadButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						detectSettings();
					}
				});
			}
			else {
				LayoutUtil.addGDDummy(composite, true, 2);
			}
		}
		
		{	// Libraries:
			final Label label = new Label(area, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Libraries_label+":"); //$NON-NLS-1$
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			
			final Composite composite = new Composite(area, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.createCompositeGrid(2));
			
			final TreeComposite treeComposite = new ViewerUtil.TreeComposite(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
			fRLibrariesViewer = treeComposite.viewer;
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.widthHint = LayoutUtil.hintWidth(fNameControl, 80);
			gd.heightHint = LayoutUtil.hintHeight(treeComposite.tree, 10);
			treeComposite.setLayoutData(gd);
			treeComposite.viewer.setContentProvider(new ITreeContentProvider() {
				@Override
				public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
				}
				@Override
				public void dispose() {
				}
				@Override
				public Object[] getElements(final Object inputElement) {
					return fConfigModel.getRLibraryGroups().toArray();
				}
				@Override
				public Object getParent(final Object element) {
					if (element instanceof RLibraryContainer) {
						return ((RLibraryContainer) element).parent;
					}
					return null;
				}
				@Override
				public boolean hasChildren(final Object element) {
					if (element instanceof IRLibraryGroup.WorkingCopy) {
						return !((IRLibraryGroup.WorkingCopy) element).getLibraries().isEmpty();
					}
					return false;
				}
				@Override
				public Object[] getChildren(final Object parentElement) {
					if (parentElement instanceof IRLibraryGroup.WorkingCopy) {
						final IRLibraryGroup.WorkingCopy group = (IRLibraryGroup.WorkingCopy) parentElement;
						final List<? extends IRLibraryLocation.WorkingCopy> libs = group.getLibraries();
						final RLibraryContainer[] array = new RLibraryContainer[libs.size()];
						for (int i = 0; i < libs.size(); i++) {
							array[i] = new RLibraryContainer(group, libs.get(i));
						}
						return array;
					}
					return null;
				}
			});
			final TreeViewerColumn column = treeComposite.addColumn(SWT.LEFT, new ColumnWeightData(100));
			column.setLabelProvider(new REnvLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final Object element = cell.getElement();
					if (element instanceof RLibraryContainer) {
						final IRLibraryLocation lib = ((RLibraryContainer) element).library;
						cell.setImage(RUI.getImage(RUI.IMG_OBJ_LIBRARY_LOCATION));
						if (lib.getSource() != IRLibraryLocation.USER && lib.getLabel() != null) {
							cell.setText(lib.getLabel());
						}
						else {
							cell.setText(lib.getDirectoryPath());
						}
						finishUpdate(cell);
						return;
					}
					super.update(cell);
				}
			});
			column.setEditingSupport(new EditingSupport(treeComposite.viewer) {
				@Override
				protected boolean canEdit(final Object element) {
					if (element instanceof RLibraryContainer) {
						final RLibraryContainer container = ((RLibraryContainer) element);
						return (container.library.getSource() == IRLibraryLocation.USER);
					}
					return false;
				}
				@Override
				protected void setValue(final Object element, final Object value) {
					final RLibraryContainer container = (RLibraryContainer) element;
					container.library.setDirectoryPath((String) value);
					
					getViewer().refresh(container, true);
					getDataBinding().updateStatus();
				}
				@Override
				protected Object getValue(final Object element) {
					final RLibraryContainer container = (RLibraryContainer) element;
					return container.library.getDirectoryPath();
				}
				@Override
				protected CellEditor getCellEditor(final Object element) {
					return new ExtensibleTextCellEditor(treeComposite.tree) {
						@Override
						protected Control createCustomControl(final Composite parent) {
							final ResourceInputComposite chooseResourceComposite = new ResourceInputComposite(parent,
									ResourceInputComposite.STYLE_TEXT,
									ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN,
									Messages.REnv_Detail_LibraryLocation_label) {
								@Override
								protected void beforeMenuAction() {
									getFocusGroup().discontinueTracking();
								}
								@Override
								protected void afterMenuAction() {
									getFocusGroup().continueTracking();
								}
							};
							chooseResourceComposite.setShowInsertVariable(true,
									DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
							fText = (Text) chooseResourceComposite.getTextControl();
							return chooseResourceComposite;
						}
					};
				}
			});
			treeComposite.viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
			treeComposite.viewer.setInput(fConfigModel);
			ViewerUtil.installDefaultEditBehaviour(treeComposite.viewer);
			ViewerUtil.scheduleStandardSelection(treeComposite.viewer);
			
			fRLibrariesButtons = new ButtonGroup<IRLibraryLocation.WorkingCopy>(composite) {
				@Override
				protected IRLibraryLocation.WorkingCopy edit1(final IRLibraryLocation.WorkingCopy item, final boolean newItem, final Object parent) {
					if (newItem) {
						return ((IRLibraryGroup.WorkingCopy) parent).newLibrary(""); //$NON-NLS-1$
					}
					return item;
				}
				@Override
				public void updateState() {
					super.updateState();
					if (getDataAdapter().isDirty()) {
						getDataBinding().updateStatus();
					}
				}
			};
			fRLibrariesButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			fRLibrariesButtons.addAddButton(null);
			fRLibrariesButtons.addDeleteButton(null);
//			fRLibrariesButtons.addSeparator();
//			fRLibrariesButtons.addUpButton();
//			fRLibrariesButtons.addDownButton();
			
			final DataAdapter<IRLibraryLocation.WorkingCopy> adapter = new DataAdapter.ListAdapter<IRLibraryLocation.WorkingCopy>(
					(ITreeContentProvider) fRLibrariesViewer.getContentProvider(),
					null, null ) {
				private IRLibraryGroup.WorkingCopy getGroup(final Object element) {
					if (element instanceof IRLibraryGroup.WorkingCopy) {
						return (IRLibraryGroup.WorkingCopy) element;
					}
					else {
						return ((RLibraryContainer) element).parent;
					}
				}
				@Override
				public IRLibraryLocation.WorkingCopy getModelItem(final Object element) {
					if (element instanceof RLibraryContainer) {
						return ((RLibraryContainer) element).library;
					}
					return (IRLibraryLocation.WorkingCopy) element;
				}
				@Override
				public Object getViewerElement(final IRLibraryLocation.WorkingCopy item, final Object parent) {
					return new RLibraryContainer((IRLibraryGroup.WorkingCopy.WorkingCopy) parent, item);
				}
				@Override
				public boolean isAddAllowed(final Object element) {
					return !getGroup(element).getId().equals(IRLibraryGroup.R_DEFAULT);
				}
				@Override
				public boolean isModifyAllowed(final Object element) {
					return ( element instanceof RLibraryContainer
							&& ((RLibraryContainer) element).library.getSource() == IRLibraryLocation.USER );
				}
				@Override
				public Object getAddParent(final Object element) {
					return getGroup(element);
				}
				@Override
				public List<? super IRLibraryLocation.WorkingCopy> getContainerFor(final Object element) {
					if (element instanceof IRLibraryGroup.WorkingCopy) {
						return ((IRLibraryGroup.WorkingCopy) element).getLibraries();
					}
					else {
						return ((RLibraryContainer) element).parent.getLibraries();
					}
				}
			};
			fRLibrariesButtons.connectTo(fRLibrariesViewer, adapter);
		}
		
		if (fConfigModel.isEditable()) {
			final Composite group = createInstallDirGroup(area);
			if (group != null) {
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			}
		}
		
		LayoutUtil.addSmallFiller(area, true);
		
		applyDialogFont(area);
		
		return area;
	}
	
	private Composite createInstallDirGroup(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createGroupGrid(2));
		composite.setText("Advanced - Installation locations:");
		{	final Label label = new Label(composite, SWT.NONE);
			label.setText("Documentation ('R_DOC_DIR'):");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final ResourceInputComposite text = new ResourceInputComposite(composite, ResourceInputComposite.STYLE_TEXT,
					(ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN), "R_DOC_DIR"); //$NON-NLS-1$
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			text.setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
			fRDocDirectoryControl = text;
		}
		{	final Label label = new Label(composite, SWT.NONE);
			label.setText("Shared files ('R_SHARE_DIR'):");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final ResourceInputComposite text = new ResourceInputComposite(composite, ResourceInputComposite.STYLE_TEXT,
					(ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN), "R_SHARE_DIR"); //$NON-NLS-1$
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			text.setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
			fRShareDirectoryControl = text;
		}
		{	final Label label = new Label(composite, SWT.NONE);
			label.setText("Include files ('R_INCLUDE_DIR'):");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final ResourceInputComposite text = new ResourceInputComposite(composite, ResourceInputComposite.STYLE_TEXT,
					(ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN), "R_INCLUDE_DIR"); //$NON-NLS-1$
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			text.setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
			fRIncludeDirectoryControl = text;
		}
		return composite;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(SWTObservables.observeText(fNameControl, SWT.Modify), 
				BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_NAME), 
				new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
					@Override
					public IStatus validate(final Object value) {
						String s = (String) value;
						s = s.trim();
						if (s.isEmpty()) {
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_Missing_message);
						}
						if (fExistingNames.contains(s)) {
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_Duplicate_message);
						}
						if (s.contains("/")) {  //$NON-NLS-1$
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_InvalidChar_message);
						}
						return ValidationStatus.ok();
					}
				}), null);
		if (fRHomeControl != null) {
			final Binding rHomeBinding = db.getContext().bindValue(fRHomeControl.getObservable(), 
					BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_RHOME), 
					new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
						@Override
						public IStatus validate(final Object value) {
							final IStatus status = fRHomeControl.getValidator().validate(value);
							if (!status.isOK()) {
								return status;
							}
							if (!fConfigModel.isValidRHomeLocation(fRHomeControl.getResourceAsFileStore())) {
								return ValidationStatus.error(Messages.REnv_Detail_Location_error_NoRHome_message);
							}
							updateArchs(!fIsNewConfig);
							return ValidationStatus.ok();
						}
					}), null);
			rHomeBinding.getValidationStatus().addValueChangeListener(new IValueChangeListener() {
				@Override
				public void handleValueChange(final ValueChangeEvent event) {
					final IStatus status = (IStatus) event.diff.getNewValue();
					fLoadButton.setEnabled(status.isOK());
				}
			});
			rHomeBinding.validateTargetToModel();
		}
		db.getContext().bindValue(SWTObservables.observeText(fRArchControl),
				BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_SUBARCH) );
		
		if (fRDocDirectoryControl != null) {
			db.getContext().bindValue(fRDocDirectoryControl.getObservable(),
					BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_RDOC_DIRECTORY) );
			db.getContext().bindValue(fRShareDirectoryControl.getObservable(),
					BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_RSHARE_DIRECTORY) );
			db.getContext().bindValue(fRIncludeDirectoryControl.getObservable(),
					BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_RINCLUDE_DIRECTORY) );
		}
	}
	
	private String[] searchRHOME() {
		try {
			final IStringVariableManager variables = VariablesPlugin.getDefault().getStringVariableManager();
			
			{	String loc= variables.performStringSubstitution("${env_var:R_HOME}", false); //$NON-NLS-1$
				if (loc != null && loc.length() > 0) {
					loc= resolve(loc);
					if (loc != null) {
						final IFileStore locStore= EFS.getLocalFileSystem().getStore(new Path(loc));
						if (locStore.fetchInfo().exists()) {
							return new String[] { loc, Messages.REnv_SystemRHome_name };
						}
					}
				}
			}
			
			final ImList<String> locCandidates;
			String prefixPattern= null;
			String prefixReplacement= null;
			if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
				String baseLoc= "${env_var:PROGRAMFILES}\\R";  //$NON-NLS-1$
				final IFileStore baseStore= EFS.getLocalFileSystem().getStore(
						new Path(variables.performStringSubstitution(baseLoc)));
				if (baseStore.fetchInfo().exists()) {
					prefixReplacement= baseLoc;
					prefixPattern= baseLoc= FileUtil.toString(baseStore);
					
					final String[] names= baseStore.childNames(EFS.NONE, null);
					Arrays.sort(names, 0, names.length,
							Collections.reverseOrder(Collator.getInstance()) );
					for (int i= 0; i < names.length; i++) {
						names[i]= baseLoc + '\\' + names[i];
					}
					locCandidates= ImCollections.newList(names);
				}
				else {
					locCandidates= ImCollections.newList();
				}
			}
			else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
				locCandidates= ImCollections.newList(
						"/Library/Frameworks/R.framework/Resources" //$NON-NLS-1$
				);
			}
			else {
				locCandidates= ImCollections.newList(
						"/usr/local/lib64/R", //$NON-NLS-1$
						"/usr/lib64/R", //$NON-NLS-1$
						"/usr/local/lib/R", //$NON-NLS-1$
						"/usr/lib/R" //$NON-NLS-1$
				);
			};
			for (String loc : locCandidates) {
				loc= resolve(loc);
				if (loc != null) {
					final IFileStore locStore= EFS.getLocalFileSystem().getStore(new Path(loc));
					if (fConfigModel.isValidRHomeLocation(locStore)) {
						if (prefixPattern != null && loc.startsWith(prefixPattern)) {
							loc= prefixReplacement + loc.substring(prefixPattern.length());
						}
						String name= locStore.getName();
						if (name.equals("Resources")) { //$NON-NLS-1$
							final IFileStore parent= locStore.getParent();
							name= (parent != null) ? parent.getName() : null;
						}
						if (name != null) {
							if (name.isEmpty() || name.equals("R")) { //$NON-NLS-1$
								name= null;
							}
							else if (Character.isDigit(name.charAt(0))) {
								name= "R " + name; //$NON-NLS-1$
							}
						}
						return new String[] { loc, name };
					}
				}
			}
			return null;
		}
		catch (final Exception e) {
			RUIPlugin.logError(-1, "Error when searching R_HOME location", e); //$NON-NLS-1$
			return null;
		}
	}
	
	private String resolve(final String loc) {
		try {
			java.nio.file.Path path= Paths.get(loc);
			path= path.toRealPath();
			return path.toString();
		}
		catch (final IOException e2) {
			return null;
		}
	}
	
	private void updateArchs(final boolean conservative) {
		if (fRHomeControl == null) {
			return;
		}
		try {
			final IFileStore rHome = fRHomeControl.getResourceAsFileStore();
			final List<String> availableArchs = fConfigModel.searchAvailableSubArchs(rHome);
			if (availableArchs == null) {
				fRArchControl.setItems(new String[0]);
				return;
			}
			final String oldArch = fRArchControl.getText();
			fRArchControl.setItems(availableArchs.toArray(new String[availableArchs.size()]));
			int idx = (oldArch.length() > 0) ? availableArchs.indexOf(oldArch) : -1;
			if (idx >= 0) {
				fRArchControl.select(idx);
			}
			
			if (conservative && fRArchControl.getText().length() > 0) {
				return;
			}
			idx = availableArchs.indexOf(Platform.getOSArch());
			if (idx < 0) {
				if (Platform.getOSArch().equals(Platform.ARCH_X86)) {
					idx = availableArchs.indexOf("i386"); //$NON-NLS-1$
					if (idx < 0) {
						idx = availableArchs.indexOf("i586"); //$NON-NLS-1$
						if (idx < 0) {
							idx = availableArchs.indexOf("i686"); //$NON-NLS-1$
						}
					}
				}
			}
			if (idx < 0) {
				idx = 0;
			}
			fRArchControl.select(idx);
		}
		catch (final Exception e) {
			fRArchControl.setItems(new String[0]);
		}
	}
	
	private void detectSettings() {
		try {
			run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						detectSettings(monitor);
					}
					catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {
			final String message = (e.getCause() instanceof CoreException) ?
					Messages.REnv_Detail_DetectSettings_error_message :
					Messages.REnv_Detail_DetectSettings_error_Unexpected_message;
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					message, e), StatusManager.LOG);
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					message, e.getCause()), StatusManager.SHOW);
		}
		catch (final InterruptedException e) {
		}
		fRLibrariesButtons.refresh();
		fRLibrariesViewer.expandAll();
	}
	
	private void detectSettings(final IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.REnv_Detail_DetectSettings_task, 10);
		
		final ProcessBuilder processBuilder = new ProcessBuilder(fConfigModel.getExecCommand(Exec.TERM));
		processBuilder.command().add("--no-save"); //$NON-NLS-1$
		processBuilder.command().add("--slave"); //$NON-NLS-1$
		processBuilder.command().add("-e"); //$NON-NLS-1$
		processBuilder.command().add(DETECT_COMMAND);
		
		final Map<String, String> envp = processBuilder.environment();
		LaunchUtils.configureEnvironment(envp, null, fConfigModel.getEnvironmentsVariables(false));
		
		monitor.worked(1);
		
		final ProcessOutputCollector reader = new ProcessOutputCollector(processBuilder, "'Detect R settings'", monitor); //$NON-NLS-1$
		final String output = reader.collect();
		final int start = output.indexOf(DETECT_START);
		if (start >= 0) {
			final String[] lines = DETECT_ITEM_PATTERN.split(output.substring(start));
			if (lines.length == DETECT_LENGTH) {
				updateLibraries(fConfigModel.getRLibraryGroup(IRLibraryGroup.R_DEFAULT),
						lines[DETECT_R_DEFAULT], lines[DETECT_R_HOME]);
				
				final IRLibraryGroup.WorkingCopy.WorkingCopy group = fConfigModel.getRLibraryGroup(IRLibraryGroup.R_SITE);
				updateLibraries(group, lines[DETECT_R_SITE], lines[DETECT_R_HOME]);
				if (group.getLibraries().isEmpty()) {
					group.getLibraries().add(group.newLibrary(IRLibraryGroup.DEFAULTLOCATION_R_SITE));
				}
				updateLibraries(fConfigModel.getRLibraryGroup(IRLibraryGroup.R_OTHER),
						lines[DETECT_R_OTHER], lines[DETECT_R_HOME]);
				updateLibraries(fConfigModel.getRLibraryGroup(IRLibraryGroup.R_USER),
						lines[DETECT_R_USER], lines[DETECT_R_HOME]);
				
				fConfigModel.setRDocDirectoryPath(checkDir(lines[DETECT_R_DOC_DIR], lines[DETECT_R_HOME]));
				fConfigModel.setRShareDirectoryPath(checkDir(lines[DETECT_R_SHARE_DIR], lines[DETECT_R_HOME]));
				fConfigModel.setRIncludeDirectoryPath(checkDir(lines[DETECT_R_INCLUDE_DIR], lines[DETECT_R_HOME]));
				
				if (lines[DETECT_R_ARCHVAR].length() > 0) {
					fConfigModel.setSubArch(lines[DETECT_R_ARCHVAR]);
				}
				else if (lines[DETECT_R_ARCH].length() > 0 && fConfigModel.getSubArch() == null) {
					fConfigModel.setSubArch(lines[DETECT_R_ARCH]);
				}
				fConfigModel.setROS(lines[DETECT_R_OS]);
				return;
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
				"Unexpected output:\n" + output, null)); //$NON-NLS-1$
	}
	
	private void updateLibraries(final IRLibraryGroup.WorkingCopy.WorkingCopy group, final String var, final String rHome) {
		final List<WorkingCopy> libraries = group.getLibraries();
		libraries.clear();
		final String[] locations = DETECT_PATH_PATTERN.split(var);
		final IPath rHomePath = new Path(rHome);
		final IPath userHomePath = new Path(System.getProperty("user.home")); //$NON-NLS-1$
		for (final String location : locations) {
			if (location.isEmpty()) {
				continue;
			}
			String s;
			final IPath path;
			if (location.startsWith("~/")) { //$NON-NLS-1$
				path = userHomePath.append(location.substring(2));
			}
			else {
				path = new Path(location);
			}
			if (rHomePath.isPrefixOf(path)) {
				s = "${env_var:R_HOME}/" + path.makeRelativeTo(rHomePath).toString(); //$NON-NLS-1$
			}
			else if (userHomePath.isPrefixOf(path)) {
				s = "${user_home}/" + path.makeRelativeTo(userHomePath).toString(); //$NON-NLS-1$
			}
			else {
				s = path.toString();
			}
			libraries.add(group.newLibrary(s));
		}
	}
	
	private String checkDir(String dir, final String rHome) {
		if (dir != null && dir.length() > 0) {
			final IPath rHomePath = new Path(rHome);
			final IPath path = new Path(dir);
			if (rHomePath.isPrefixOf(path)) {
				dir = "${env_var:R_HOME}/" + path.makeRelativeTo(rHomePath).toString(); //$NON-NLS-1$
			}
			return dir;
		}
		return null;
	}
	
}
