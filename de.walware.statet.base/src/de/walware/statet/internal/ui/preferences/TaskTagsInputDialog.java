/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.dialogs.StatusInfo;
import de.walware.eclipsecommons.ui.util.PixelConverter;
import de.walware.statet.base.core.preferences.TaskTagsPreferences.TaskPriority;
import de.walware.statet.internal.ui.StatetMessages;
import de.walware.statet.internal.ui.preferences.TaskTagsConfigurationBlock.TaskTag;


/**
 * Dialog to enter a na new task tag
 */
public class TaskTagsInputDialog extends StatusDialog {
	
	
	private Text fNameControl;
	private Combo fPriorityControl;
	
	private String fName;
	private TaskPriority fPriority;
	private List<String> fExistingNames;
	
	
	public TaskTagsInputDialog(Shell parent, TaskTag task, List<TaskTag> existingEntries) {
		
		super(parent);
		
		if (task != null) {
			fName = task.fName;
			fPriority = task.fPriority;
		}
			
		fExistingNames = new ArrayList<String>(existingEntries.size());
		for (int i = 0; i < existingEntries.size(); i++) {
			TaskTag curr = (TaskTag) existingEntries.get(i);
			if (!curr.equals(task)) {
				fExistingNames.add(curr.fName);
			}
		}
		
		if (task == null) {
			setTitle(Messages.TaskTags_InputDialog_NewTag_title); 
		} else {
			setTitle(Messages.TaskTags_InputDialog_EditTag_title); 
		}

	}
	
	@Override
	protected void configureShell(Shell newShell) {
		
		super.configureShell(newShell);
		// ADDHELP
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaHelpContextIds.TODO_TASK_INPUT_DIALOG);
	}

	protected Control createDialogArea(Composite parent) {
		
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Layouter layouter = new Layouter(new Composite(composite, SWT.NONE), 2);
		
		fNameControl = layouter.addLabeledTextControl(Messages.TaskTags_InputDialog_Name_label);
		((GridData) fNameControl.getLayoutData()).widthHint = 
				new PixelConverter(fNameControl).convertWidthInCharsToPixels(45);
		fNameControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fName = fNameControl.getText();
				doValidation();
			};
		});

		String[] items = new String[] {
				StatetMessages.TaskPriority_High, 
				StatetMessages.TaskPriority_Normal, 
				StatetMessages.TaskPriority_Low, 
		};
		fPriorityControl = layouter.addLabeledComboControl(Messages.TaskTags_InputDialog_Priority_label, items);
		fPriorityControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				switch (fPriorityControl.getSelectionIndex()) {
				case 0:
					fPriority = TaskPriority.HIGH;
					break;
				case 2:
					fPriority = TaskPriority.LOW;
					break;
				default:
					fPriority = TaskPriority.NORMAL;
					break;				
				}
			};
		});

		// Init Fields
		if (fName != null) {
			fNameControl.setText(fName);
			switch (fPriority) {
			case HIGH:
				fPriorityControl.select(0);
				break;
			case LOW:
				fPriorityControl.select(2);
				break;
			default: // NORMAL
				fPriorityControl.select(1);
				break;
			}
		} else {
			fPriorityControl.select(1);
		}
		Display display = parent.getDisplay();
		if (display != null) {
			display.asyncExec(
				new Runnable() {
					public void run() {
						fNameControl.setFocus();
					}
				}
			);
		}
		
		return composite;
	}

	public TaskTag getResult() {
		
		return new TaskTag(fName, fPriority);
	}
	
		
	private void doValidation() {
		
		StatusInfo status = new StatusInfo();
		String newText = fNameControl.getText();
		if (newText.length() == 0) {
			status.setError(Messages.TaskTags_InputDialog_error_EnterName_message); 
		} else {
			if (newText.indexOf(',') != -1) {
				status.setError(Messages.TaskTags_InputDialog_error_Comma_message); 
			} else if (fExistingNames.contains(newText)) {
				status.setError(Messages.TaskTags_InputDialog_error_EntryExists_message); 
			} else if (!Character.isLetterOrDigit(newText.charAt(0))) { // ||  Character.isWhitespace(newText.charAt(newText.length() - 1))) {
				status.setError(Messages.TaskTags_InputDialog_error_ShouldStartWithLetterOrDigit_message); 
			}
		}
		updateStatus(status);
	}

}
