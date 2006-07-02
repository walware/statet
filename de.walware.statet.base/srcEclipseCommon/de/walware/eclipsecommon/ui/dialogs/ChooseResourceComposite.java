/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommon.ui.dialogs;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import de.walware.eclipsecommon.internal.ui.Messages;

import de.walware.statet.base.StatetPlugin;


/**
 * 
 */
public class ChooseResourceComposite extends Composite {

	private static final int SHIFT_EXISTING = 3;
	private static final int SHIFT_NEW = 6;
	public static final int MODE_EXISTING_INFO = 1<<SHIFT_EXISTING;
	public static final int MODE_EXISTING_WARNING = 3<<SHIFT_EXISTING;
	public static final int MODE_EXISTING_ERROR = 7<<SHIFT_EXISTING;
	public static final int MODE_NEW_ERROR = 1<<SHIFT_NEW;
	
	private String fUserTask;
	
	private boolean fForDirectory = false;
	private int fMode = 0;
	
	private IFile fLocationWSResourceCache;
	private IFileStore fLocationEFSCache;
	private Combo fLocationField;
	private ListenerList fModificationListeners = new ListenerList();
	
	private Button fLocationWorkspaceButton;
	private Button fLocationFilesystemButton;
	private boolean fIgnoreTextChanges = false;
	
	
	public ChooseResourceComposite(Composite parent, 
			int mode, String userTaskLabel) {
		
		super(parent, SWT.NONE);
		
		fMode = mode;
		fUserTask = userTaskLabel;
		createContent();
	}

	public void setHistory(String[] history) {
		
		if (history != null) {
			fLocationField.setItems(history);
		}
	}
	
	public int getMode() {
		
		return fMode;
	}
	public void setMode(int mode) {
		
		fMode = mode;
	}
	
	private void createContent() {
		
		Layouter layouter = new Layouter(this, 1);
		fLocationField = layouter.addComboControl(null, false, 1);
		
		Layouter buttonLayouter = new Layouter(new Composite(layouter.fComposite, SWT.NONE), 2);
		GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
		buttonLayouter.fComposite.setLayoutData(gd);
		
		fLocationWorkspaceButton = buttonLayouter.addButton(Messages.BrowseWorkspace_button_name, 
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						fIgnoreTextChanges = true;
						handleBrowseWorkspaceButton();
						fIgnoreTextChanges = false;
					}
				}, 1);
		fLocationFilesystemButton = buttonLayouter.addButton(Messages.BrowseFilesystem_button_name, 
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						fIgnoreTextChanges = true;
						handleBrowseFilesystemButton();
						fIgnoreTextChanges = false;
					}
				}, 1);
		fLocationField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!fIgnoreTextChanges) {
						fLocationWSResourceCache = null;
						fLocationEFSCache = null;
					}
					Object[] listener = fModificationListeners.getListeners();
					for (Object obj : listener) {
						((ModifyListener) obj).modifyText(e);
					}
				}
		});
	}
	
	protected void handleBrowseWorkspaceButton() {
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource res = fLocationWSResourceCache;
		if (res == null) {
			try {
				String current = resolveExpression(fLocationField.getText());
				IResource[] found = null;
				if (!fForDirectory) {
					found = root.findFilesForLocation(new Path(current));
				}
				if (found == null || found.length == 0) {
					found = root.findContainersForLocation(new Path(current));
				}
				if (found != null && found.length > 0) {
					res = found[0];
				}
			}
			catch (Exception e) {
			}
		}
		if (res == null) {
			res = root;
		}
		
		Object[] results = null;
		if (fForDirectory) { 
			ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), (IContainer) res, 
					(fMode & MODE_NEW_ERROR) == 0, fUserTask+":"); //$NON-NLS-1$
			if (dialog.open() == Dialog.OK) {
				results = dialog.getResult();
			}
		}
		else {
			ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(), fUserTask+":"); //$NON-NLS-1$
			dialog.setInitialSelections(new IResource[] { res });
			dialog.setAllowNewResources((fMode & MODE_NEW_ERROR) == 0);
			dialog.open();
			results = dialog.getResult();
		}
		if (results == null || results.length < 1) {
			return;
		}
		fLocationWSResourceCache = (IFile) results[0];
		fLocationEFSCache = null;
		
		res = fLocationWSResourceCache.getParent();
		StringBuilder path = new StringBuilder('/'+fLocationWSResourceCache.getName());
		while (!res.exists()) {
			res = res.getParent();
			path.insert(0, '/'+res.getName());
		}
		path.insert(0, newVariableExpression("workspace_loc",  //$NON-NLS-1$ 
				res.getFullPath().toString()));
		fLocationField.setText(path.toString());
	}

	protected void handleBrowseFilesystemButton() {

		String path = null;
		try {
			path = resolveExpression(fLocationField.getText());
		}
		catch (Exception e) {
		}
		if (fForDirectory) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setFilterPath(path);
			path = dialog.open();
		}
		else {
			FileDialog dialog = new FileDialog(getShell(), (fMode & MODE_NEW_ERROR) == 0 ? SWT.SAVE : SWT.OPEN);
			dialog.setText(fUserTask);
			dialog.setFilterPath(path);
			path = dialog.open();
		}
		if (path == null) {
			return;
		}
		fLocationWSResourceCache = null;
		fLocationEFSCache = null;
		fLocationField.setText(path);
	}
	
	public String getResourceString() {
		
		return fLocationField.getText();
	}
	
	
	public void addModifyListener(ModifyListener listener) {
		
		fModificationListeners.add(listener);
	}
	
	public IFile getResourceAsWorkspaceResource() {
		
		return (IFile) fLocationWSResourceCache;
	}
	
	/**
	 * You must call validate, before this method can return a file
	 * @return a file handler or null.
	 */
	public IFileStore getResourceAsFileStore() {
		
		return fLocationEFSCache;
	}
		
	private IFileStore detectEFSStore() throws CoreException {
		
		String loc = resolveExpression(fLocationField.getText());
		if (loc.trim().length() == 0) {
			return null;
		}
	
		if (fLocationWSResourceCache != null) {
			return EFS.getStore(fLocationWSResourceCache.getRawLocationURI());
		}
		try {
			Path path = new Path(loc);
			if (path.isValidPath(loc)) {
				IFileStore store = EFS.getLocalFileSystem().getStore(path);
				if (store != null) {
					return store;
				}
			}
		}
		catch (Exception e) {
		}
		try {
			URI uri = URI.create(loc);
			IFileStore store = EFS.getStore(uri);
			if (store != null) {
				return store;
			}
		}
		catch (IllegalArgumentException e) {
		}
		throw new CoreException(new Status(IStatus.ERROR, StatetPlugin.ID, 0, Messages.Location_error_UnknownFormat_messageLocation_error_UnknownFormat_message, null));
	}
	
	public IStatus validate() {
		
		IStatus status = null;
		if (fLocationWSResourceCache != null && fLocationWSResourceCache instanceof IFile) {
			fLocationEFSCache = null;
			status = validate((IFile) fLocationWSResourceCache);
		}
		else {
			try {
				fLocationEFSCache = detectEFSStore();
				if (fLocationEFSCache == null) {
					return new StatusInfo(IStatus.ERROR, Messages.File_error_NoValidFile_message);
				}
				status = validate(fLocationEFSCache);
			} catch (CoreException e) {
				status = e.getStatus();
//				status = new StatusInfo(IStatus.ERROR, Messages.File_error_NoValidFile_message);
			}
		}
		if (status == null) {
			status = new StatusInfo();
		}
		return status;
	}

	protected IStatus validate(IFile file) {
		
		int check = fMode >> SHIFT_NEW;
		if ((check & 1) != 0 && !file.exists()) {
			return new StatusInfo(IStatus.ERROR, Messages.File_error_DoesNotExists_message);
		}
		check = fMode >> SHIFT_EXISTING;
		if ((check & 1) != 0) {
			if (file.exists()) {
				return new StatusInfo(getSeverity(check), Messages.File_error_AlreadyExists_message); 
			}
		}
		return null;
	}

	protected IStatus validate(IFileStore file) {
		
		int check = fMode >> SHIFT_NEW;
		if ((check & 1) != 0 && !file.fetchInfo().exists()) {
			return new StatusInfo(IStatus.ERROR, Messages.File_error_DoesNotExists_message);
		}
		check = fMode >> SHIFT_EXISTING;
		if ((check & 1) != 0) {
			if (file.fetchInfo().exists()) {
				return new StatusInfo(getSeverity(check), Messages.File_error_AlreadyExists_message); 
			}
		}
		return null;
	}

	protected int getSeverity(int mode) {
		
		if ((mode & 4) != 0) {
			return IStatus.ERROR;
		}
		if ((mode & 2) != 0) {
			return IStatus.WARNING;
		}
		if ((mode & 1) != 0) {
			return IStatus.INFO;
		}
		return IStatus.OK;
	}
	
	/**
	 * Returns a new variable expression with the given variable and the given
	 * argument.
	 * 
	 * @see IStringVariableManager#generateVariableExpression(String, String)
	 */
	protected String newVariableExpression(String varName, String arg) {
	
		return VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(varName, arg);
	}

	protected String resolveExpression(String expression) throws CoreException {

		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		return manager.performStringSubstitution(expression);
	}

}
