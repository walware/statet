/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.dialogs.ResourceSelectionDialog;
import de.walware.eclipsecommons.ui.util.PixelConverter;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class RConsoleMainTab extends AbstractLaunchConfigurationTab {

	public static final String FIRST_EDIT = IRConsoleConstants.ROOT + "firstedit"; //$NON-NLS-1$
	
	
	protected Text fLocationField;
	protected Button fLocationFileButton;
	protected Button fLocationWorkspaceButton;
	protected Button fLocationVariablesButton;

	protected Text fWorkDirectoryField;
	protected Button fWorkDirectoryFileButton;
	protected Button fWorkDirectoryWorkspaceButton;
	protected Button fWorkDirectoryVariablesButton;

	protected Text fArgumentField;
	protected Button fArgumentROptionsButton;
	protected Button fArgumentVariablesButton;

	protected boolean fInitializing = false;
	private boolean fUserEdited = false;
	
	
	protected WidgetListener fListener = new WidgetListener();
	
	/**
	 * A listener to update for text modification and widget selection.
	 */
	protected class WidgetListener extends SelectionAdapter implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (!fInitializing) {
				setDirty(true);
				fUserEdited = true;
				updateLaunchConfigurationDialog();
			}
		}
		public void widgetSelected(SelectionEvent e) {

			setDirty(true);
			Object source = e.getSource();

			// Executable group
			if (source == fLocationWorkspaceButton) {
				handleLocationWorkspaceButtonSelected();
			}
			else if (source == fLocationFileButton) {
				handleLocationFileButtonSelected();
			}
			else if (source == fLocationVariablesButton) {
				handleVariablesButtonSelected(fLocationField);
			}

			// Working directory group
			else if (source == fWorkDirectoryWorkspaceButton) {
				handleWorkingDirectoryWorkspaceButtonSelected();
			}
			else if (source == fWorkDirectoryFileButton) {
				handleWorkingDirectoryFileButtonSelected();
			}
			else if (source == fWorkDirectoryVariablesButton) {
				handleVariablesButtonSelected(fWorkDirectoryField);
			}
			
			// Argument group
			else if (source == fArgumentROptionsButton) {
				handleRArgumentsButtonSelected();
			}
			else if (source == fArgumentVariablesButton) {
				handleVariablesButtonSelected(fArgumentField);
			}
			
		}
	}

	
	public RConsoleMainTab() {
	}

	public String getName() {
		
		return RLaunchingMessages.MainTab_name;
	}

	@Override
	public Image getImage() {
		return StatetImages.getImage(StatetImages.LAUNCHCONFIG_MAIN);
	}
	
	public void createControl(Composite parent) {
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Layouter layouter = new Layouter(mainComposite, new GridLayout());
		createLocationComponent(layouter);
		createWorkDirectoryComponent(layouter);
		createArgumentComponent(layouter);
		layouter.addFiller();
	}

	protected void createLocationComponent(Layouter parent) {

		Group group = parent.addGroup(RLaunchingMessages.RCmdMainTab_Location);
		Layouter groupLayouter = new Layouter(group, 1);
		
		fLocationField = groupLayouter.addTextControl();
		fLocationField.addModifyListener(fListener);
		
		Layouter buttonLayouter = new Layouter(new Composite(group, SWT.NONE), 3);
		GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
		buttonLayouter.composite.setLayoutData(gd);

		fLocationWorkspaceButton = buttonLayouter.addButton(RLaunchingMessages.MainTab_Location_Browse_Workspace, fListener, 1);
		fLocationFileButton = buttonLayouter.addButton(RLaunchingMessages.MainTab_Location_Browse_FileSystem, fListener, 1);
		fLocationVariablesButton = buttonLayouter.addButton(RLaunchingMessages.MainTab_Location_Variables, fListener, 1);
	}

	/**
	 * Creates the controls needed to edit the working directory
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Layouter parent) {

		Group group = parent.addGroup(RLaunchingMessages.MainTab_WorkingDirectory);
		Layouter groupLayouter = new Layouter(group, 1);
		
		fWorkDirectoryField = groupLayouter.addTextControl();
		fWorkDirectoryField.addModifyListener(fListener);
		
		Layouter buttonLayouter = new Layouter(new Composite(group, SWT.NONE), 3);
		GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
		buttonLayouter.composite.setLayoutData(gd);

		fWorkDirectoryWorkspaceButton = buttonLayouter.addButton(RLaunchingMessages.MainTab_WorkingDirectory_Browse_Workspace, fListener, 1);
		fWorkDirectoryFileButton = buttonLayouter.addButton(RLaunchingMessages.MainTab_WorkingDirectory_Browse_FileSystem, fListener, 1);
		fWorkDirectoryVariablesButton = buttonLayouter.addButton(RLaunchingMessages.MainTab_WorkingDirectory_Variables, fListener, 1);
	}
	
	/**
	 * Creates the controls needed to edit the argument and
	 * prompt for argument attributes of an external tool
	 *
	 * @param parent the composite to create the controls in
	 */
	protected void createArgumentComponent(Layouter parent) {
		
		Group group = parent.addGroup(RLaunchingMessages.MainTab_Arguments);
		Layouter groupLayouter = new Layouter(group, 1);

		fArgumentField = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		PixelConverter conv = new PixelConverter(fArgumentField);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		gd.heightHint = conv.convertHeightInCharsToPixels(4);
		fArgumentField.setLayoutData(gd);
		fArgumentField.addModifyListener(fListener);
		
		Layouter buttonLayouter = new Layouter(new Composite(group, SWT.NONE), 2);
		gd = new GridData(SWT.END, SWT.CENTER, false, false);
		buttonLayouter.composite.setLayoutData(gd);

		fArgumentROptionsButton = buttonLayouter.addButton(RLaunchingMessages.RCmdMainTab_ROptions, fListener, 1);
		fArgumentVariablesButton = buttonLayouter.addButton(RLaunchingMessages.MainTab_Arguments_Variables, fListener, 1);

		groupLayouter.addLabel(RLaunchingMessages.MainTab_Arguments_Note);
	}
	
	
	/**
	 * Prompts the user for a workspace location within the workspace and sets
	 * the location as a String containing the workspace_loc variable or
	 * <code>null</code> if no location was obtained from the user.
	 */
	protected void handleLocationWorkspaceButtonSelected() {

		ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(),  
				RLaunchingMessages.RCmdMainTab_SelectRExecutable); //$NON-NLS-1$
		if (dialog.open() == Dialog.OK) {
			Object[] results = dialog.getResult();
			if (results == null || results.length == 0) {
				return;
			}
			IResource resource = (IResource) results[0];
			fLocationField.setText(newVariableExpression("workspace_loc", resource.getFullPath().toString())); //$NON-NLS-1$
		}
	}

	/**
	 * Prompts the user to choose a location from the filesystem and
	 * sets the location as the full path of the selected file.
	 */
	protected void handleLocationFileButtonSelected() {
		
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(fLocationField.getText());
		String text= fileDialog.open();
		if (text != null) {
			fLocationField.setText(text);
		}
	}
	
	/**
	 * Prompts the user for a working directory location within the workspace
	 * and sets the working directory as a String containing the workspace_loc
	 * variable or <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkingDirectoryWorkspaceButtonSelected() {
		
		ContainerSelectionDialog containerDialog = new ContainerSelectionDialog(
			getShell(), 
			ResourcesPlugin.getWorkspace().getRoot(),
			false,
			RLaunchingMessages.MainTab_SelectWorkingDirectory_message);
		containerDialog.open();
		Object[] resource = containerDialog.getResult();
		String text = null;
		if (resource != null && resource.length > 0) {
			text = newVariableExpression("workspace_loc", ((IPath)resource[0]).toString()); //$NON-NLS-1$
		}
		if (text != null) {
			fWorkDirectoryField.setText(text);
		}
	}

	/**
	 * Prompts the user to choose a working directory from the filesystem.
	 */
	protected void handleWorkingDirectoryFileButtonSelected() {
		
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		dialog.setMessage(RLaunchingMessages.MainTab_SelectWorkingDirectory_message); //$NON-NLS-1$
		dialog.setFilterPath(fWorkDirectoryField.getText());
		String text= dialog.open();
		if (text != null) {
			fWorkDirectoryField.setText(text);
		}
	}
	
	/**
	 * A variable entry button has been pressed for the given text
	 * field. Prompt the user for a variable and enter the result
	 * in the given field.
	 */
	private void handleVariablesButtonSelected(Text textField) {

		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		if (dialog.open() != Dialog.OK)
			return;
		String variable = dialog.getVariableExpression();
		if (variable == null)
			return;
		
		textField.insert(variable);
	}
	
	/**
	 * A variable entry button has been pressed for the given text
	 * field. Prompt the user for R arguments and enter the result
	 * in the given field.
	 */
	private void handleRArgumentsButtonSelected() {
		
		ROptionsSelectionDialog dialog = new ROptionsSelectionDialog(getShell());
		if (dialog.open() != Dialog.OK) 
			return;
		String variable = dialog.getValue();
		if (variable == null)
			return;

		int pos = fArgumentField.getCaretPosition();
		int end = pos+fArgumentField.getSelectionCount();
		StringBuffer buffer = new StringBuffer(variable);
		if ( (pos > 0) && (!" ".equals(fArgumentField.getText(pos-1, pos)) ) ) { //$NON-NLS-1$
			buffer.insert(0, ' ');
		}
		if ( (end < fArgumentField.getCharCount()) && (!" ".equals(fArgumentField.getText(end, end+1))  //$NON-NLS-1$
				&& !variable.endsWith("="))) { //$NON-NLS-1$
			buffer.append(' ');
		}
		fArgumentField.insert(buffer.toString());
	}
	
	
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
		boolean isWin = Platform.getOS().startsWith("win"); //$NON-NLS-1$
		
		String rLocation;
		if (isWin) {
			StringBuilder path = new StringBuilder();
			path.append("${env_var:PROGRAMFILES}"); //$NON-NLS-1$
			path.append("\\R\\R-2.x.x\\bin\\"); //$NON-NLS-1$
			path.append("Rterm.exe"); //$NON-NLS-1$
			rLocation = path.toString();
		}
		else {
			rLocation = "/usr/local/bin/R"; //$NON-NLS-1$
		}
		
		String workingDirectory = "${project_loc}"; //$NON-NLS-1$
		IResource selectedResource = DebugUITools.getSelectedResource();
		if (selectedResource != null) {
			IProject project = selectedResource.getProject();
			if (project != null) {
				workingDirectory = "${project_loc:"+project.getName()+"}"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		configuration.setAttribute(IRConsoleConstants.ATTR_R_LOCATION, rLocation);
		configuration.setAttribute(IRConsoleConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
		configuration.setAttribute(IRConsoleConstants.ATTR_R_CMD, "TERM"); //$NON-NLS-1$
		configuration.setAttribute(IRConsoleConstants.ATTR_CMD_ARGUMENTS, isWin? "--ess" : ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		configuration.setAttribute(FIRST_EDIT, true);
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		
		fInitializing = true;
		updateControls(configuration);
		fInitializing = false;
		setDirty(false);
	}
	
	protected void updateControls(ILaunchConfiguration configuration) {

		String location= ""; //$NON-NLS-1$
		try {
			location = configuration.getAttribute(IRConsoleConstants.ATTR_R_LOCATION, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			logError(RLaunchingMessages.Tab_error_ReadingConfiguration_message, ce);
		}
		fLocationField.setText(location);

		String workingDir = ""; //$NON-NLS-1$
		try {
			workingDir = configuration.getAttribute(IRConsoleConstants.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			logError(RLaunchingMessages.Tab_error_ReadingConfiguration_message, ce);
		}
		fWorkDirectoryField.setText(workingDir);

		String arguments= ""; //$NON-NLS-1$
		try {
			arguments = configuration.getAttribute(IRConsoleConstants.ATTR_CMD_ARGUMENTS, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			logError(RLaunchingMessages.Tab_error_ReadingConfiguration_message, ce);
		}
		fArgumentField.setText(arguments);
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		String location = fLocationField.getText().trim();
		if (location.length() == 0) {
			configuration.setAttribute(IRConsoleConstants.ATTR_R_LOCATION, (String)null);
		} else {
			configuration.setAttribute(IRConsoleConstants.ATTR_R_LOCATION, location);
		}
		
		String workingDirectory = fWorkDirectoryField.getText().trim();
		if (workingDirectory.length() == 0) {
			configuration.setAttribute(IRConsoleConstants.ATTR_WORKING_DIRECTORY, (String)null);
		} else {
			configuration.setAttribute(IRConsoleConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
		}

		String arguments = fArgumentField.getText().trim();
		if (arguments.length() == 0) {
			configuration.setAttribute(IRConsoleConstants.ATTR_CMD_ARGUMENTS, (String)null);
		} else {
			configuration.setAttribute(IRConsoleConstants.ATTR_CMD_ARGUMENTS, arguments);
		}
		
		if(fUserEdited) {
			configuration.setAttribute(FIRST_EDIT, false);
		}

	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {

		setErrorMessage(null);
		setMessage(null);
		boolean newConfig = false;
		try {
			newConfig = launchConfig.getAttribute(FIRST_EDIT, false);
		} catch (CoreException e) {
			//assume false is correct
		}
		return validateLocation(newConfig) && validateWorkDirectory();
	}
	
	/**
	 * Validates the content of the location field.
	 */
	protected boolean validateLocation(boolean newConfig) {
		
		String location = fLocationField.getText().trim();
		if (location.length() < 1) {
			if (newConfig) {
				setErrorMessage(null);
				setMessage(RLaunchingMessages.RCmdMainTab_info_SpecifyLocation_message);
			} else {
				setErrorMessage(RLaunchingMessages.MainTab_error_LocationCannotBeEmpty_message);
				setMessage(null);
			}
			return false;
		}
		
		String expandedLocation = null;
		try {
			expandedLocation = resolveExpression(location);
			if (expandedLocation == null) { //a variable that needs to be resolved at runtime
				return true;
			}
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
			return false;
		}
		
		File file = new File(expandedLocation);
		if (!file.exists()) { // The file does not exist.
			if (!newConfig) {
				setErrorMessage(RLaunchingMessages.MainTab_error_LocationDoesNotExist_message); //$NON-NLS-1$
			}
			return false;
		}
		if (!file.isFile()) {
			if (!newConfig) {
				setErrorMessage(RLaunchingMessages.MainTab_error_LocationSpecifiedIsNotAFile_message); //$NON-NLS-1$
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Validates the content of the working directory field.
	 */
	protected boolean validateWorkDirectory() {
		String dir = fWorkDirectoryField.getText().trim();
		if (dir.length() <= 0) {
			return true;
		}

		String expandedDir = null;
		try {
			expandedDir = resolveExpression(dir);
			if (expandedDir == null) { //a variable that needs to be resolved at runtime
				return true;
			}
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
			return false;
		}
			
		File file = new File(expandedDir);
		if (!file.exists()) { // The directory does not exist.
			setErrorMessage(RLaunchingMessages.MainTab_error_WorkingDirectoryDoesNotExistOrIsInvalid_message);
			return false;
		}
		if (!file.isDirectory()) {
			setErrorMessage(RLaunchingMessages.MainTab_error_WorkingDirectoryNotADirectory);
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a new variable expression with the given variable and the given argument.
	 * @see IStringVariableManager#generateVariableExpression(String, String)
	 */
	protected String newVariableExpression(String varName, String arg) {
	
		return VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(varName, arg);
	}

	private String resolveExpression(String expression) throws CoreException {

		String expanded = null;
		try {
			IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
			expanded = manager.performStringSubstitution(expression);
		} catch (CoreException e) { //possibly just a variable that needs to be resolved at runtime
			validateVaribles(expression);
			return null;
		}
		return expanded;
	}
	
	/**
	 * Validates the variables of the given string to determine if all variables are valid
	 * 
	 * @param expression expression with variables
	 * @exception CoreException if a variable is specified that does not exist
	 */
	private void validateVaribles(String expression) throws CoreException {
		
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		manager.validateStringVariables(expression);
	}

	
	protected void logError(String msg, CoreException ce) {
		
		RUIPlugin.logError(IStatetStatusConstants.LAUNCHCONFIG_ERROR, msg, ce);
	}
}
