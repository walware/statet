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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.ibm.icu.text.Collator;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
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
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.ProcessOutputCollector;
import de.walware.ecommons.ui.dialogs.ButtonGroup;
import de.walware.ecommons.ui.dialogs.DatabindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.dialogs.ExtensibleTextCellEditor;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TreeComposite;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;
import de.walware.ecommons.variables.core.VariableFilter;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.RLibraryGroup;
import de.walware.statet.r.core.renv.RLibraryLocation;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.ui.RUI;


/**
 * Dialog for an {@link REnvConfiguration}
 */
public class REnvConfigDialog extends ExtStatusDialog {
	
	
	private static final Integer T_64 = Integer.valueOf(64);
	private static final Integer T_32 = Integer.valueOf(32);
	
	private static final String DETECT_START = "_R-Path-And-Library-Configuration_"; //$NON-NLS-1$
	private static final String DETECT_COMMAND = "cat('"+DETECT_START+"', "
			+ "Sys.getenv(\'R_HOME\'),"
			+ "paste(.Library, collapse=.Platform$path.sep),"
			+ "paste(.Library.site, collapse=.Platform$path.sep),"
			+ "Sys.getenv('R_LIBS'),"
			+ "Sys.getenv('R_LIBS_USER'),"
			+ "R.version$arch, "
			+ "sep='\\n');"; //$NON-NLS-1$ //$NON-NLS-2$
	// R.version$arch
	private static final int DETECT_LENGTH = 7;
	private static final int DETECT_R_HOME = 1;
	private static final int DETECT_R_DEFAULT = 2;
	private static final int DETECT_R_SITE = 3;
	private static final int DETECT_R_OTHER = 4;
	private static final int DETECT_R_USER = 5;
	private static final int DETECT_R_ARCH = 6;
	private static final Pattern DETECT_ITEM_PATTERN = RUtil.LINE_SEPARATOR_PATTERN;
	private static final Pattern DETECT_PATH_PATTERN = Pattern.compile(Pattern.quote(File.pathSeparator));
	
	
	private class RHomeComposite extends ResourceInputComposite {
		
		public RHomeComposite(final Composite parent) {
			super (parent, 
					ResourceInputComposite.STYLE_TEXT,
					ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN, 
					Messages.REnv_Detail_Location_label);
			setShowInsertVariable(true, new ConstList<VariableFilter>(
					VariableFilter.EXCLUDE_BUILD_FILTER,
					VariableFilter.EXCLUDE_INTERACTIVE_FILTER,
					VariableFilter.EXCLUDE_JAVA_FILTER ), null);
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
						setText(rhome[0], true);
						fRBitViewer.setSelection(new StructuredSelection(
								rhome[0].contains("64") ? T_64 : T_32)); //$NON-NLS-1$
						final String current = fNameControl.getText().trim();
						if ((current.length() == 0 || current.equals("R")) && rhome[1] != null) { //$NON-NLS-1$
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
		
		RLibraryGroup parent;
		RLibraryLocation library;
		
		RLibraryContainer(final RLibraryGroup parent, final RLibraryLocation library) {
			this.parent = parent;
			this.library = library;
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
	
	
	private final REnvConfiguration.WorkingCopy fConfigModel;
	private final boolean fIsNewConfig;
	private final Set<String> fExistingNames;
	
	private Text fNameControl;
	private ResourceInputComposite fRHomeControl;
	private ComboViewer fRBitViewer;
	private TreeViewer fRLibrariesViewer;
	private ButtonGroup<RLibraryLocation> fRLibrariesButtons;
	private Button fLoadButton;
	
	
	public REnvConfigDialog(final Shell parent, 
			final REnvConfiguration.WorkingCopy config, final boolean isNewConfig, 
			final Collection<REnvConfiguration> existingConfigs) {
		super(parent, true);
		
		fConfigModel = config;
		fIsNewConfig = isNewConfig;
		fExistingNames = new HashSet<String>();
		for (final REnvConfiguration ec : existingConfigs) {
			fExistingNames.add(ec.getName());
		}
		setTitle(fIsNewConfig ?
				Messages.REnv_Detail_AddDialog_title : 
				Messages.REnv_Detail_Edit_Dialog_title );
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogArea.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		
		{	// Name:
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Name_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fNameControl = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fNameControl, 60);
			fNameControl.setLayoutData(gd);
		}
		{	// Location:
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Location_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fRHomeControl = new RHomeComposite(dialogArea);
			fRHomeControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		
		LayoutUtil.addSmallFiller(dialogArea, false);
		
		{	// Type (Bits):
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Bits_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final Composite composite = new Composite(dialogArea, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			fRBitViewer = new ComboViewer(composite);
			fRBitViewer.setContentProvider(new ArrayContentProvider());
			fRBitViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(final Object element) {
					return ((Integer) element).toString() + "-bit";  //$NON-NLS-1$
				}
			});
			fRBitViewer.setInput(new Integer[] { T_32, T_64 });
			
			fLoadButton = new Button(composite, SWT.PUSH);
			fLoadButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
			fLoadButton.setText(Messages.REnv_Detail_DetectSettings_label);
			fLoadButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					detectSettings();
				}
			});
		}
		
		{	// Libraries:
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Libraries_label+":"); //$NON-NLS-1$
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			
			final Composite composite = new Composite(dialogArea, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final TreeComposite treeComposite = new ViewerUtil.TreeComposite(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.widthHint = LayoutUtil.hintWidth(fNameControl, 80);
			gd.heightHint = LayoutUtil.hintHeight(treeComposite.tree, 10);
			treeComposite.setLayoutData(gd);
			fRLibrariesViewer = treeComposite.viewer;
			treeComposite.viewer.setContentProvider(new ITreeContentProvider() {
				public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
				}
				public void dispose() {
				}
				public Object[] getElements(final Object inputElement) {
					return fConfigModel.getRLibraryGroups().toArray();
				}
				public Object getParent(final Object element) {
					if (element instanceof RLibraryContainer) {
						return ((RLibraryContainer) element).parent;
					}
					return null;
				}
				public boolean hasChildren(final Object element) {
					if (element instanceof RLibraryGroup) {
						return !((RLibraryGroup) element).getLibraries().isEmpty();
					}
					return false;
				}
				public Object[] getChildren(final Object parentElement) {
					if (parentElement instanceof RLibraryGroup) {
						final RLibraryGroup group = (RLibraryGroup) parentElement;
						final List<RLibraryLocation> libs = group.getLibraries();
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
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final Object element = cell.getElement();
					if (element instanceof RLibraryGroup) {
						final RLibraryGroup group = (RLibraryGroup) element;
						cell.setImage(RUI.getImage(RUIPlugin.IMG_OBJ_LIBRARY_GROUP));
						cell.setText(group.getLabel());
					}
					else if (element instanceof RLibraryContainer) {
						final RLibraryLocation lib = ((RLibraryContainer) element).library;
						cell.setImage(RUI.getImage(RUIPlugin.IMG_OBJ_LIBRARY_LOCATION));
						cell.setText(lib.getDirectoryPath());
					}
					else throw new UnsupportedOperationException();
				}
			});
			column.setEditingSupport(new EditingSupport(treeComposite.viewer) {
				@Override
				protected boolean canEdit(final Object element) {
					if (element instanceof RLibraryContainer) {
						final RLibraryGroup group = ((RLibraryContainer) element).parent;
						return !group.getId().equals(RLibraryGroup.R_DEFAULT);
					}
					return false;
				}
				@Override
				protected void setValue(final Object element, final Object value) {
					final RLibraryContainer container = (RLibraryContainer) element;
					final RLibraryLocation oldLib = container.library;
					final RLibraryLocation newLib = new RLibraryLocation((String) value);
					
					final List<RLibraryLocation> list = container.parent.getLibraries();
					list.set(list.indexOf(oldLib), newLib);
					container.library = newLib;
					getViewer().refresh(container, true);
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
							chooseResourceComposite.setShowInsertVariable(true, new ConstList<VariableFilter>(
									VariableFilter.EXCLUDE_BUILD_FILTER,
									VariableFilter.EXCLUDE_INTERACTIVE_FILTER,
									VariableFilter.EXCLUDE_JAVA_FILTER ), null);
							fText = (Text) chooseResourceComposite.getTextControl();
							return chooseResourceComposite;
						}
					};
				}
			});
			treeComposite.viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
			treeComposite.viewer.setInput(fConfigModel);
			ViewerUtil.installDefaultEditBehaviour(treeComposite.viewer);
			
			fRLibrariesButtons = new ButtonGroup<RLibraryLocation>(composite) {
				private RLibraryGroup getGroup(final Object element) {
					if (element instanceof RLibraryGroup) {
						return (RLibraryGroup) element;
					}
					else {
						return ((RLibraryContainer) element).parent;
					}
				}
				@Override
				protected RLibraryLocation getModelItem(final Object element) {
					if (element instanceof RLibraryContainer) {
						return ((RLibraryContainer) element).library;
					}
					return (RLibraryLocation) element;
				}
				@Override
				protected Object getViewerElement(final RLibraryLocation item, final Object parent) {
					return new RLibraryContainer((RLibraryGroup) parent, item);
				}
				@Override
				protected boolean isAddAllowed(final Object element) {
					return !getGroup(element).getId().equals(RLibraryGroup.R_DEFAULT);
				}
				@Override
				protected boolean isModifyAllowed(final Object element) {
					return ( element instanceof RLibraryContainer
							&& !getGroup(element).getId().equals(RLibraryGroup.R_DEFAULT) );
				}
				@Override
				protected Object getAddParent(final Object element) {
					return getGroup(element);
				}
				@Override
				protected List<? super RLibraryLocation> getChildContainer(final Object element) {
					if (element instanceof RLibraryGroup) {
						return ((RLibraryGroup) element).getLibraries();
					}
					else {
						return ((RLibraryContainer) element).parent.getLibraries();
					}
				}
				@Override
				protected RLibraryLocation edit1(RLibraryLocation item, final boolean newItem) {
					if (newItem) {
						item = new RLibraryLocation(""); //$NON-NLS-1$
					}
					return item;
				}
			};
			fRLibrariesButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			fRLibrariesButtons.addAddButton();
			fRLibrariesButtons.addDeleteButton();
//			fRLibrariesButtons.addSeparator();
//			fRLibrariesButtons.addUpButton();
//			fRLibrariesButtons.addDownButton();
			
			fRLibrariesButtons.connectTo(fRLibrariesViewer, null, null);
		}
		
		LayoutUtil.addSmallFiller(dialogArea, true);
		applyDialogFont(dialogArea);
		
		final DatabindingSupport databinding = new DatabindingSupport(dialogArea);
		addBindings(databinding, databinding.getRealm());
		databinding.installStatusListener(new StatusUpdater());
		fRLibrariesButtons.updateState();
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), IRUIHelpContextIds.R_ENV);
		
		return dialogArea;
	}
	
	protected void addBindings(final DatabindingSupport db, final Realm realm) {
		db.getContext().bindValue(SWTObservables.observeText(fNameControl, SWT.Modify), 
				BeansObservables.observeValue(fConfigModel, REnvConfiguration.PROP_NAME), 
				new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
					public IStatus validate(final Object value) {
						String s = (String) value;
						s = s.trim();
						if (s.length() == 0) {
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
		final Binding rHomeBinding = db.getContext().bindValue(fRHomeControl.getObservable(), 
				BeansObservables.observeValue(fConfigModel, REnvConfiguration.PROP_RHOME), 
				new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
					public IStatus validate(final Object value) {
						final IStatus status = fRHomeControl.getValidator().validate(value);
						if (!status.isOK()) {
							return status;
						}
						if (!REnvConfiguration.isValidRHomeLocation(fRHomeControl.getResourceAsFileStore())) {
							return ValidationStatus.error(Messages.REnv_Detail_Location_error_NoRHome_message);
						}
						return ValidationStatus.ok();
					}
				}), null);
		rHomeBinding.getValidationStatus().addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				final IStatus status = (IStatus) event.diff.getNewValue();
				fLoadButton.setEnabled(status.isOK());
			}
		});
		rHomeBinding.validateTargetToModel();
		db.getContext().bindValue(ViewersObservables.observeSingleSelection(fRBitViewer), 
				BeansObservables.observeValue(fConfigModel, REnvConfiguration.PROP_RBITS), 
				null, null);
	}
	
	private String[] searchRHOME() {
		try {
			final IStringVariableManager variables = VariablesPlugin.getDefault().getStringVariableManager();
			
			String loc = variables.performStringSubstitution("${env_var:R_HOME}", false); //$NON-NLS-1$
			if (loc != null && loc.length() > 0) {
				if (EFS.getLocalFileSystem().getStore(
						new Path(loc)).fetchInfo().exists()) {
					return new String[] { loc, Messages.REnv_SystemRHome_name };
				}
			}
			if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
				loc = "${env_var:PROGRAMFILES}\\R";  //$NON-NLS-1$
				final IFileStore res = EFS.getLocalFileSystem().getStore(
						new Path(variables.performStringSubstitution(loc)));
				if (!res.fetchInfo().exists()) {
					return null;
				}
				final String[] childNames = res.childNames(EFS.NONE, null);
				Arrays.sort(childNames, 0, childNames.length, Collator.getInstance());
				for (int i = childNames.length-1; i >= 0; i--) {
					if (REnvConfiguration.isValidRHomeLocation(res.getChild(childNames[i]))) {
						return new String[] { loc + '\\' + childNames[i], childNames[i] };
					}
				}
			}
			else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
				loc = "/Library/Frameworks/R.framework/Resources";  //$NON-NLS-1$
				if (REnvConfiguration.isValidRHomeLocation(EFS.getLocalFileSystem().getStore(new Path(loc)))) {
					return new String[] { loc, null };
				}
			}
			else {
				final String[] defLocations = new String[] {
						"/usr/lib/R", //$NON-NLS-1$
						"/usr/lib64/R", //$NON-NLS-1$
				};
				for (int i = 0; i < defLocations.length; i++) {
					loc = defLocations[i];
					if (REnvConfiguration.isValidRHomeLocation(EFS.getLocalFileSystem().getStore(new Path(loc)))) {
						return new String[] { loc, null };
					}
				}
			}
		}
		catch (final Exception e) {
			RUIPlugin.logError(-1, "Error when searching R_HOME location", e); //$NON-NLS-1$
		}
		return null;
	}
	
	private void detectSettings() {
		try {
			run(true, true, new IRunnableWithProgress() {
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
		LaunchConfigUtil.configureEnvironment(envp, null, fConfigModel.getEnvironmentsVariables(false));
		
		monitor.worked(1);
		
		final ProcessOutputCollector reader = new ProcessOutputCollector(processBuilder, "'Detect R settings'", monitor); //$NON-NLS-1$
		final String output = reader.collect();
		final int start = output.indexOf(DETECT_START);
		if (start >= 0) {
			final String[] lines = DETECT_ITEM_PATTERN.split(output.substring(start));
			if (lines.length == DETECT_LENGTH) {
				updateLibraries(fConfigModel.getRLibraryGroup(RLibraryGroup.R_DEFAULT).getLibraries(),
						lines[DETECT_R_DEFAULT], lines[DETECT_R_HOME]);
				final List<RLibraryLocation> siteLibs = fConfigModel.getRLibraryGroup(RLibraryGroup.R_SITE).getLibraries();
				updateLibraries(siteLibs, lines[DETECT_R_SITE], lines[DETECT_R_HOME]);
				if (siteLibs.isEmpty()) {
					siteLibs.add(new RLibraryLocation(RLibraryGroup.DEFAULTLOCATION_R_SITE));
				}
				updateLibraries(fConfigModel.getRLibraryGroup(RLibraryGroup.R_OTHER).getLibraries(),
						lines[DETECT_R_OTHER], lines[DETECT_R_HOME]);
				updateLibraries(fConfigModel.getRLibraryGroup(RLibraryGroup.R_USER).getLibraries(),
						lines[DETECT_R_USER], lines[DETECT_R_HOME]);
				if (lines[DETECT_R_ARCH].endsWith("86")) {
					fConfigModel.setRBits(32);
				}
				else if (lines[DETECT_R_ARCH].endsWith("64")) {
					fConfigModel.setRBits(64);
				}
				return;
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
				"Unexpected output:\n" + output, null)); //$NON-NLS-1$
	}
	
	private void updateLibraries(final List<RLibraryLocation> libs, final String var, final String rHome) {
		libs.clear();
		final String[] locations = DETECT_PATH_PATTERN.split(var);
		final IPath rHomePath = new Path(rHome);
		final IPath userHomePath = new Path(System.getProperty("user.home")); //$NON-NLS-1$
		for (final String location : locations) {
			if (location.length() == 0) {
				continue;
			}
			String s;
			final IPath path = new Path(location);
			if (rHomePath.isPrefixOf(path)) {
				s = "${env_var:R_HOME}/" + path.makeRelativeTo(rHomePath).toString(); //$NON-NLS-1$
			}
			else if (userHomePath.isPrefixOf(path)) {
				s = "${system_property:user.home}/" + path.makeRelativeTo(userHomePath).toString(); //$NON-NLS-1$
			}
			else {
				s = path.toString();
			}
			libs.add(new RLibraryLocation(s));
		}
	}
	
}
