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

package de.walware.eclipsecommons.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import de.walware.eclipsecommons.FileValidator;
import de.walware.eclipsecommons.internal.ui.Messages;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.MessageUtil;


/**
 * Composite with text/combo and browse buttons.
 * 
 * Configurable for files and directories, new or existing resources.
 * 
 * Note: Not yet all combinations are tested! 
 */
public class ChooseResourceComposite extends Composite {

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
	private boolean fDoOpen;
	private boolean fControlledChange;
	private FileValidator fValidator;
	
	private Text fLocationTextField;
	private Combo fLocationComboField;
	
	private Label fLabel;
	private Button fLocationWorkspaceButton;
	private Button fLocationFilesystemButton;
	private HashMap<Button,String> fButtons;
	
	
	public ChooseResourceComposite(Composite parent, int style,
			int mode, String resourceLabel) {
		
		super(parent, SWT.NONE);
		
		fValidator = new FileValidator();
		setMode(mode);
		
		fStyle = style;
		fAsCombo = (fStyle & STYLE_COMBO) == STYLE_COMBO;
		fControlledChange = false;
		setResourceLabel(resourceLabel);
		createContent();
	}

	public void setHistory(String[] history) {
		
		if (history != null && fAsCombo) {
			fLocationComboField.setItems(history);
		}
	}
	
	public void setMode(int mode) {
		
		Assert.isTrue((mode & (MODE_DIRECTORY | MODE_FILE)) != 0);
		if ((mode & MODE_DIRECTORY) == MODE_DIRECTORY) {
			fForDirectory = true;
			fValidator.setOnDirectory(IStatus.OK);
		}
		else {
			fForDirectory = false;
			fValidator.setOnDirectory(IStatus.ERROR);
		}
		if ((mode & MODE_FILE) == MODE_FILE) {
			fValidator.setOnFile(IStatus.OK);
		}
		else {
			fValidator.setOnFile(IStatus.ERROR);
		}
		
		fDoOpen = (mode & MODE_OPEN) == MODE_OPEN;
		fValidator.setDefaultMode(fDoOpen);
	}

	public void setResourceLabel(String label) {
		
		fResourceLabel = label;
		if (fLabel != null) {
			fLabel.setText(fResourceLabel + ":"); //$NON-NLS-1$
		}
		fValidator.setResourceLabel(MessageUtil.removeMnemonics(label));
	}
	
	protected String getTaskLabel() {
		
		return NLS.bind(Messages.ChooseResource_Task_description, fResourceLabel);
	}
	
	public Control getTextControl() {
		
		if (fAsCombo) {
			return fLocationComboField;
		}
		else {
			return fLocationTextField;
		}
	}
	
	protected void setText(String s) {
		
		fControlledChange = true;
		if (fAsCombo) {
			fLocationComboField.setText(s);
		}
		else {
			fLocationTextField.setText(s);
		}
		fControlledChange = false;
	}
	
	protected void insertText(String s) {
		
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
		GridLayout layout = new GridLayout();
		if ((fStyle & STYLE_GROUP) == STYLE_GROUP) {
			this.setLayout(new FillLayout());
			Group group = new Group(this, SWT.NONE);
			group.setText(fResourceLabel + ":"); //$NON-NLS-1$
			content = group;
			LayoutUtil.applyGroupDefaults(layout, 1);
		}
		else {
			content = this;
			LayoutUtil.applyCompositeDefaults(layout, 1);
		}
		content.setLayout(layout);

		if ((fStyle & STYLE_LABEL) != 0) {
			fLabel = new Label(content, SWT.LEFT);
			fLabel.setText(fResourceLabel + ":"); //$NON-NLS-1$
			fLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}

		if (fAsCombo) {
			fLocationComboField = new Combo(content, SWT.DROP_DOWN);
		}
		else {
			fLocationTextField = new Text(content, SWT.BORDER | SWT.SINGLE);
		}
		Control inputField = getTextControl();
		inputField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!fControlledChange) {
					fValidator.setExplicit(getText());
				}
			}
		});
		
		Composite buttonComposite = new Composite(content, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		List<Button> buttons = new ArrayList<Button>(4);
		fLocationWorkspaceButton = new Button(buttonComposite, SWT.PUSH);
		fLocationWorkspaceButton.setText(Messages.BrowseWorkspace_label);
		fLocationWorkspaceButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						handleBrowseWorkspaceButton();
						getTextControl().setFocus();
					}
				});
		buttons.add(fLocationWorkspaceButton);
		
		fLocationFilesystemButton = new Button(buttonComposite, SWT.PUSH);
		fLocationFilesystemButton.setText(Messages.BrowseFilesystem_label);
		fLocationFilesystemButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						handleBrowseFilesystemButton();
						getTextControl().setFocus();
					}
				});
		buttons.add(fLocationFilesystemButton);

		buttons.addAll(Arrays.asList(createCustomButtons(buttonComposite)));
		
		layout = new GridLayout(buttons.size(), true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonComposite.setLayout(layout);
		
		GridDataFactory buttonsGD = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);
		fButtons = new HashMap<Button, String>();
		for (Button button : buttons) {
			buttonsGD.minSize(LayoutUtil.hintWidth(button), SWT.DEFAULT).applyTo(button);
			fButtons.put(button, button.getText());
		}
		
		inputField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				updateLabels(true);
			}
			public void focusLost(FocusEvent e) {
				updateLabels(false);
			}
		});
		updateLabels(false);
	}

	protected void updateLabels(boolean hasFocus) {
		
		for (Button button : fButtons.keySet()) {
			String label = fButtons.get(button);
			if (!hasFocus) {
				label = MessageUtil.removeMnemonics(label);
			}
			button.setText(label);
		}
	}
	
	protected Button[] createCustomButtons(Composite composite) {
		
		return new Button[0];
	}
	
	protected void handleBrowseWorkspaceButton() {
		
		IResource res = fValidator.getWorkspaceResource();
		if (res == null) {
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		
		Object[] results = null;
		String wsPath;
		String appendPath;
		IResource resource = null;
		if (fForDirectory) { 
			ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), 
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
			ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(), getTaskLabel());
			dialog.setInitialSelections(new IResource[] { res });
			dialog.setAllowNewResources(fValidator.getOnNotExisting() != IStatus.ERROR);
			dialog.open();
			results = dialog.getResult();
			if (results == null || results.length < 1) {
				return;
			}
			resource = (IFile) results[0];
			res = resource.getParent();
			StringBuilder path = new StringBuilder('/'+resource.getName());
			while (!res.exists()) {
				res = res.getParent();
				path.insert(0, '/'+res.getName());
			}
			wsPath = res.getFullPath().toString();
			appendPath = path.toString();
		}
		
		fValidator.setExplicit(resource);
		setText(newVariableExpression("workspace_loc", wsPath) + appendPath); //$NON-NLS-1$
	}

	protected void handleBrowseFilesystemButton() {

		String path = null;
		try {
			if (fValidator.isLocalFile()) {
				path = URIUtil.toPath(fValidator.getFileStore().toURI()).toOSString();
			}
		}
		catch (Exception e) {
		}
		if (fForDirectory) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setText(MessageUtil.removeMnemonics(getTaskLabel()));
			dialog.setFilterPath(path);
			path = dialog.open();
		}
		else {
			FileDialog dialog = new FileDialog(getShell(), (fDoOpen) ? SWT.OPEN: SWT.SAVE);
			dialog.setText(MessageUtil.removeMnemonics(getTaskLabel()));
			dialog.setFilterPath(path);
			path = dialog.open();
		}
		if (path == null) {
			return;
		}
		fValidator.setExplicit(path);
		setText(path);
	}
	

	public String getResourceString() {
		
		return getText();
	}
	
	
	public void addModifyListener(ModifyListener listener) {
		
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
		
	public IObservableValue createObservable() {
		
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
	protected String newVariableExpression(String varName, String arg) {
	
		return VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(varName, arg);
	}

}
