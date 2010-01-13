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

package de.walware.ecommons.ui.workbench;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.FileValidator;
import de.walware.ecommons.debug.ui.CustomizableVariableSelectionDialog;
import de.walware.ecommons.internal.ui.Messages;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.dialogs.WidgetToolsButton;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.variables.core.VariableFilter;


/**
 * Composite with text/combo and browse buttons.
 * 
 * Configurable for files and directories, new or existing resources.
 * 
 * XXX: Not yet all combinations are tested!
 */
public class ResourceInputComposite extends Composite {
	
	
	private static final String VAR_WORKSPACE_LOC = "workspace_loc"; //$NON-NLS-1$
	
	
	private static class SearchResourceDialog extends FilteredResourcesSelectionDialog {
		
		public SearchResourceDialog(final Shell shell, final boolean multi,
				final IContainer container, final int typesMask) {
			super(shell, multi, container, typesMask);
			setTitle(Messages.ResourceSelectionDialog_title);
		}
	}
	
	
	public static final int MODE_FILE = 1;
	public static final int MODE_DIRECTORY = 2;
	public static final int MODE_SAVE = 4;
	public static final int MODE_OPEN = 8;
	
	public static final int STYLE_TEXT = 0;
	public static final int STYLE_COMBO = 1;
	public static final int STYLE_GROUP = 1<<1;
	public static final int STYLE_LABEL = 2<<1;
	
	
	private String fResourceLabel;
	
	private int fStyle;
	private boolean fAsCombo;
	
	private boolean fForDirectory;
	private boolean fForFile;
	private boolean fDoOpen;
	private boolean fControlledChange;
	private FileValidator fValidator;
	
	private Text fLocationTextField;
	private Combo fLocationComboField;
	
	private Label fLabel;
	private WidgetToolsButton fTools;
	private boolean fShowInsertVariable;
	private List<VariableFilter> fShowInsertVariableFilters;
	private List<IStringVariable> fShowInsertVariableAdditionals;
	
	
	public ResourceInputComposite(final Composite parent, final int style,
			final int mode, final String resourceLabel) {
		super(parent, SWT.NONE);
		
		fValidator = new FileValidator();
		setMode(mode);
		
		fStyle = style;
		fAsCombo = (fStyle & STYLE_COMBO) == STYLE_COMBO;
		fControlledChange = false;
		setResourceLabel(resourceLabel);
		createContent();
	}
	
	
	public void setHistory(final String[] history) {
		if (history != null && fAsCombo) {
			fLocationComboField.setItems(history);
		}
	}
	
	public void setMode(final int mode) {
		assert ((mode & (MODE_DIRECTORY | MODE_FILE)) != 0);
		if ((mode & MODE_DIRECTORY) == MODE_DIRECTORY) {
			fForDirectory = true;
			fValidator.setOnDirectory(IStatus.OK);
		}
		else {
			fForDirectory = false;
			fValidator.setOnDirectory(IStatus.ERROR);
		}
		if ((mode & MODE_FILE) == MODE_FILE) {
			fForFile = true;
			fValidator.setOnFile(IStatus.OK);
		}
		else {
			fForFile = false;
			fValidator.setOnFile(IStatus.ERROR);
		}
		
		fDoOpen = (mode & MODE_OPEN) == MODE_OPEN;
		fValidator.setDefaultMode(fDoOpen);
		if (fTools != null) {
			fTools.resetMenu();
		}
	}
	
	public void setResourceLabel(final String label) {
		fResourceLabel = label;
		if (fLabel != null) {
			fLabel.setText(fResourceLabel + ':');
		}
		fValidator.setResourceLabel(MessageUtil.removeMnemonics(label));
	}
	
	protected String getTaskLabel() {
		return NLS.bind(Messages.ChooseResource_Task_description, fResourceLabel);
	}
	
	public void setShowInsertVariable(final boolean enable,
			final List<VariableFilter> filters, final List<? extends IStringVariable> additionals) {
		fShowInsertVariable = enable;
		fShowInsertVariableFilters = (filters != null) ? new ConstList<VariableFilter>(filters) : null;
		if (fShowInsertVariableAdditionals != null) {
			for (final IStringVariable variable : fShowInsertVariableAdditionals) {
				final String name = variable.getName();
				final Pattern pattern = Pattern.compile("\\Q${"+name+"\\E[\\}\\:]");
				fValidator.setOnPattern(pattern, -1);
			}
		}
		fShowInsertVariableAdditionals = (additionals != null) ? new ConstList<IStringVariable>(additionals) : null;
		if (fShowInsertVariableAdditionals != null) {
			for (final IStringVariable variable : fShowInsertVariableAdditionals) {
				final String name = variable.getName();
				final Pattern pattern = Pattern.compile("\\Q${"+name+"\\E[\\}\\:]");
				fValidator.setOnPattern(pattern, IStatus.OK);
			}
		}
		if (fTools != null) {
			fTools.resetMenu();
		}
	}
	
	
	public Control getTextControl() {
		if (fAsCombo) {
			return fLocationComboField;
		}
		else {
			return fLocationTextField;
		}
	}
	
	protected void setText(final String s, final boolean validate) {
		if (!validate) {
			fControlledChange = true;
		}
		if (fAsCombo) {
			fLocationComboField.setText(s);
		}
		else {
			fLocationTextField.setText(s);
		}
		if (!validate) {
			fControlledChange = false;
		}
	}
	
	protected void insertText(final String s) {
		if (fAsCombo) {
			//
		}
		else {
			fLocationTextField.insert(s);
		}
	}
	
	protected String getText() {
		if (fAsCombo) {
			return fLocationComboField.getText();
		}
		else {
			return fLocationTextField.getText();
		}
	}
	
	private void createContent() {
		Composite content;
		final GridLayout layout = new GridLayout();
		if ((fStyle & STYLE_GROUP) == STYLE_GROUP) {
			this.setLayout(new FillLayout());
			final Group group = new Group(this, SWT.NONE);
			group.setText(fResourceLabel + ':');
			content = group;
			LayoutUtil.applyGroupDefaults(layout, 2);
		}
		else {
			content = this;
			LayoutUtil.applyCompositeDefaults(layout, 2);
		}
		layout.horizontalSpacing = 0;
		content.setLayout(layout);
		
		if ((fStyle & STYLE_LABEL) != 0) {
			fLabel = new Label(content, SWT.LEFT);
			fLabel.setText(fResourceLabel + ':');
			fLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		}
		
		if (fAsCombo) {
			fLocationComboField = new Combo(content, SWT.DROP_DOWN);
		}
		else {
			fLocationTextField = new Text(content, SWT.BORDER | SWT.SINGLE);
		}
		final Control inputField = getTextControl();
		inputField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				if (!fControlledChange) {
					fValidator.setExplicit(getText());
				}
			}
		});
		
		fTools = new WidgetToolsButton(inputField) {
			@Override
			protected void fillMenu(final Menu menu) {
				ResourceInputComposite.this.fillMenu(menu);
			}
		};
		fTools.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}
	
	protected void fillMenu(final Menu menu) {
		final boolean both = (fForFile && fForDirectory);
		{
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.SearchWorkspace_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					beforeMenuAction();
					handleSearchWorkspaceButton();
					getTextControl().setFocus();
					afterMenuAction();
				}
			});
		}
		if (fForFile) {
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(both ? Messages.BrowseWorkspace_ForFile_label : Messages.BrowseWorkspace_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					beforeMenuAction();
					handleBrowseWorkspaceButton(MODE_FILE);
					getTextControl().setFocus();
					afterMenuAction();
				}
			});
		}
		if (fForDirectory) {
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(both ? Messages.BrowseWorkspace_ForDir_label : Messages.BrowseWorkspace_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					beforeMenuAction();
					handleBrowseWorkspaceButton(MODE_DIRECTORY);
					getTextControl().setFocus();
					afterMenuAction();
				}
			});
		}	
		if (fForFile) {
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(both ? Messages.BrowseFilesystem_ForFile_label : Messages.BrowseFilesystem_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					beforeMenuAction();
					handleBrowseFilesystemButton(MODE_FILE);
					getTextControl().setFocus();
					afterMenuAction();
				}
			});
		}
		if (fForDirectory) {
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(both ? Messages.BrowseFilesystem_ForDir_label : Messages.BrowseFilesystem_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					beforeMenuAction();
					handleBrowseFilesystemButton(MODE_DIRECTORY);
					getTextControl().setFocus();
					afterMenuAction();
				}
			});
		}
		
		if (fShowInsertVariable) {
			new MenuItem(menu, SWT.SEPARATOR);
		}
		
		if (fShowInsertVariable) {
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(SharedMessages.InsertVariable_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					beforeMenuAction();
					handleVariablesButton();
					getTextControl().setFocus();
					afterMenuAction();
				}
			});
		}
		
	}
	
	protected void handleSearchWorkspaceButton() {
		int resourceMode = 0;
		if (fForFile) {
			resourceMode |= IResource.FILE;
		}
		if (fForDirectory ) {
			resourceMode |= IResource.FOLDER;
		}
		final IWorkspaceRoot container = ResourcesPlugin.getWorkspace().getRoot();
		final SearchResourceDialog dialog = new SearchResourceDialog(getShell(), false, container, resourceMode);
		String initial = ""; //$NON-NLS-1$
		final IFileStore store = fValidator.getFileStore();
		if (store != null) {
			initial = store.getName();
		}
		else {
			final String current = getText();
			final int idx = current.lastIndexOf('/');
			if (idx >= 0) {
				initial = current.substring(idx+1);
			}
			else {
				initial = current;
			}
		}
		dialog.setInitialPattern(initial);
		dialog.open();
		final Object[] results = dialog.getResult();
		if (results == null || results.length < 1) {
			return;
		}
		IResource resource = (IResource) results[0];
		if (!fForFile && resource.getType() == IResource.FILE) {
			resource = resource.getParent();
		}
		final String wsPath = resource.getFullPath().toString();
		
		fValidator.setExplicit(resource);
		setText(newVariableExpression(VAR_WORKSPACE_LOC, wsPath), false); 
	}
	
	protected void handleBrowseWorkspaceButton(final int mode) {
		IResource res = fValidator.getWorkspaceResource();
		if (res == null) {
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		
		Object[] results = null;
		String wsPath;
		String appendPath;
		IResource resource = null;
		if (mode == MODE_DIRECTORY) {
			if (res instanceof IFile) {
				res = res.getParent();
			}
			final ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), 
					(IContainer) res, (fValidator.getOnNotExisting() != IStatus.ERROR), getTaskLabel());
			dialog.open();
			results = dialog.getResult();
			if (results == null || results.length < 1) {
				return;
			}
			wsPath = ((Path) results[0]).toString();
			resource = ResourcesPlugin.getWorkspace().getRoot().findMember(wsPath);
			appendPath = ""; //$NON-NLS-1$
		}
		else {
			final ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(), getTaskLabel());
			dialog.setInitialSelections(new IResource[] { res });
			dialog.setAllowNewResources(fValidator.getOnNotExisting() != IStatus.ERROR);
			dialog.open();
			results = dialog.getResult();
			if (results == null || results.length < 1) {
				return;
			}
			resource = (IFile) results[0];
			res = resource.getParent();
			final StringBuilder path = new StringBuilder('/'+resource.getName());
			while (!res.exists()) {
				res = res.getParent();
				path.insert(0, '/'+res.getName());
			}
			wsPath = res.getFullPath().toString();
			appendPath = path.toString();
		}
		
		fValidator.setExplicit(resource);
		setText(newVariableExpression(VAR_WORKSPACE_LOC, wsPath) + appendPath, false); 
	}
	
	protected void handleBrowseFilesystemButton(final int mode) {
		String path = null;
		try {
			if (fValidator.isLocalFile()) {
				path = URIUtil.toPath(fValidator.getFileStore().toURI()).toOSString();
			}
		}
		catch (final Exception e) {
		}
		if (mode == MODE_DIRECTORY) {
			final DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setText(MessageUtil.removeMnemonics(getTaskLabel()));
			dialog.setFilterPath(path);
			path = dialog.open();
		}
		else {
			final FileDialog dialog = new FileDialog(getShell(), (fDoOpen) ? SWT.OPEN: SWT.SAVE);
			dialog.setText(MessageUtil.removeMnemonics(getTaskLabel()));
			dialog.setFilterPath(path);
			path = dialog.open();
		}
		if (path == null) {
			return;
		}
		fValidator.setExplicit(path);
		setText(path, false);
	}
	
	protected void handleVariablesButton() {
		final CustomizableVariableSelectionDialog dialog = new CustomizableVariableSelectionDialog(getShell());
		if (fShowInsertVariableFilters != null) {
			dialog.addFilters(fShowInsertVariableFilters);
		}
		if (fShowInsertVariableAdditionals != null) {
			dialog.addAdditionals(fShowInsertVariableAdditionals);
		}
		if (dialog.open() != Dialog.OK) {
			return;
		}
		final String variable = dialog.getVariableExpression();
		if (variable == null) {
			return;
		}
		insertText(variable);
	}
	
	
	public String getResourceString() {
		return getText();
	}
	
	
	public void addModifyListener(final ModifyListener listener) {
		if (fAsCombo) {
			fLocationComboField.addModifyListener(listener);
		}
		else
			fLocationTextField.addModifyListener(listener);
	}
	
	public IResource getResourceAsWorkspaceResource() {
		return fValidator.getWorkspaceResource();
	}
	
	/**
	 * You must call validate, before this method can return a file
	 * @return a file handler or null.
	 */
	public IFileStore getResourceAsFileStore() {
		return fValidator.getFileStore();
	}
	
	public IObservableValue getObservable() {
		if (fAsCombo) {
			return SWTObservables.observeText(fLocationComboField);
		}
		else {
			return SWTObservables.observeText(fLocationTextField, SWT.Modify);
		}
	}
	
	public FileValidator getValidator() {
		return fValidator;
	}
	
	/**
	 * Returns a new variable expression with the given variable and the given
	 * argument.
	 * 
	 * @see IStringVariableManager#generateVariableExpression(String, String)
	 */
	protected String newVariableExpression(final String varName, final String arg) {
		return VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(varName, arg);
	}
	
	/**
	 * Is called before a menu action is executed.
	 */
	protected void beforeMenuAction() {
	}
	
	/**
	 * Is called after a menu action is finish.
	 */
	protected void afterMenuAction() {
	}
	
}
