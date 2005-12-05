/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation in JDT
 *     Stephan Wahlbrink - adaptations to StatET
 *******************************************************************************/

package de.walware.statet.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommon.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommon.ui.dialogs.Layouter;
import de.walware.eclipsecommon.ui.dialogs.StatusInfo;
import de.walware.eclipsecommon.ui.dialogs.groups.SelectionItem;
import de.walware.eclipsecommon.ui.dialogs.groups.TableOptionButtonsGroup;
import de.walware.eclipsecommon.ui.util.PixelConverter;
import de.walware.statet.base.StatetPreferenceConstants;
import de.walware.statet.base.StatetCore.TaskPriority;
import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;
import de.walware.statet.internal.ui.StatetMessages;


/**
  */
public class TaskTagsConfigurationBlock extends ManagedConfigurationBlock {

	
	private static final Key PREF_TASK_TAGS = createStatetCoreKey(StatetPreferenceConstants.TASK_TAGS);
	private static final Key PREF_TASK_TAGS_PRIORITIES = createStatetCoreKey(StatetPreferenceConstants.TASK_TAGS_PRIORITIES);
	

	public static class TaskTag extends SelectionItem {
		
		public TaskPriority fPriority;
		
		public TaskTag(String name, TaskPriority priority) {
			
			super(name);
			
			fPriority = priority;
		}
	}
	
	
/* Table-Viewer support *******************************************************/
	
	private class TaskTagLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

		public TaskTagLabelProvider() {
		}
		
		public Image getImage(Object element) {
			
			return null;
		}

		public String getText(Object element) {
			
			return getColumnText(element, 0);
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			
			TaskTag task = (TaskTag) element;
			
			switch (columnIndex) {
			case 0:
				String name = task.fName;
				if (isDefaultTask(task)) {
					name = NLS.bind(Messages.TaskTags_DefaultTask, name); 
				}
				return name;

			case 1:
				switch (task.fPriority) {
				case HIGH:
					return StatetMessages.TaskPriority_High; 
				case NORMAL:
					return StatetMessages.TaskPriority_Normal; 
				case LOW:
					return StatetMessages.TaskPriority_Low; 
				}
				break;
				
			default:
				break;
			}
			
			return ""; //$NON-NLS-1$
		}

		public Font getFont(Object element) {
			
			if (isDefaultTask((TaskTag) element)) {
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			}
			return null;
		}
	}

	
/* GUI ************************************************************************/
	
	private static final int IDX_ADD = 0;
	private static final int IDX_EDIT = 1;
	private static final int IDX_REMOVE = 2;
	private static final int IDX_DEFAULT = 4;
	
	private class TasksGroup extends TableOptionButtonsGroup<TaskTag> {
		
		TaskTag fDefaultTask = null;
		
		TasksGroup() {
			
			super(new String[] {
					Messages.TaskTags_AddButton_label, 
					Messages.TaskTags_EditButton_label, 
					Messages.TaskTags_RemoveButton_label, 
					null,
					Messages.TaskTags_DefaultButton_label, 
			} );
			setRemoveButtonIndex(IDX_REMOVE);
			
			setTableColumns(new String[] {
					Messages.TaskTags_TaskColumn_name, 
					Messages.TaskTags_PriorityColumn_name, 
			} );
		}
		
		@Override
		protected ColumnLayoutData[] createColumnLayoutData(Table table) {
			
			PixelConverter conv = new PixelConverter(table);
			int priorityWidth = conv.convertHeightInCharsToPixels(Messages.TaskTags_PriorityColumn_name.length());
			priorityWidth = Math.max(priorityWidth, conv.convertWidthInCharsToPixels(StatetMessages.TaskPriority_High.length()));
			priorityWidth = Math.max(priorityWidth, conv.convertWidthInCharsToPixels(StatetMessages.TaskPriority_Normal.length()));
			priorityWidth = Math.max(priorityWidth, conv.convertWidthInCharsToPixels(StatetMessages.TaskPriority_Low.length()));
			return new ColumnLayoutData[] {
					new ColumnWeightData(1),
					new ColumnPixelData(priorityWidth + 5)
			};
		}
		
		@Override
		protected ITableLabelProvider createTableLabelProvider() {

			return new TaskTagLabelProvider();
		}

		@Override
		public void handleListSelection() {
			
			TaskTag single = getSingleSelectedItem();
			
			fButtonGroup.enableButton(IDX_EDIT, (single != null) );
			fButtonGroup.enableButton(IDX_DEFAULT, (single != null) && !isDefaultTask(single));
		}
		
		@Override
		protected void handleDoubleClick(TaskTag item) {
			
			doEdit(item);
		}

		@Override
		public void handleButtonPressed(int buttonIndex) {
			
			switch (buttonIndex) {
			case IDX_ADD:
				doEdit(null);
				break;

			case IDX_EDIT:
				TaskTag item = getSingleSelectedItem();
				if (item != null)
					doEdit(item);
				break;
				
			case IDX_REMOVE:
				IStructuredSelection selection = getSelectedItems();
				if (!selection.isEmpty())
					doRemove(selection.toList());
				break;
				
			case IDX_DEFAULT:
				item = getSingleSelectedItem();
				if (item != null)
					doSetDefault(item);
				break;
			}
		}
		
	}
	

	private TasksGroup fTasksGroup;

	private IStatusChangeListener fStatusListener;

	
	public TaskTagsConfigurationBlock(IProject project, IStatusChangeListener statusListener) {

		super(project);
		fStatusListener = statusListener;
		
		fTasksGroup = new TasksGroup();
	}
	
	final boolean isDefaultTask(TaskTag task) {
		return (task == fTasksGroup.fDefaultTask);
	}
	
	@Override
	public void createContents(Layouter layouter, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {

		super.createContents(layouter, container, preferenceStore);
		
		setupPreferenceManager(container, new Key[] {
				PREF_TASK_TAGS,
				PREF_TASK_TAGS_PRIORITIES,
		} );

		layouter.addGroup(fTasksGroup);
		
		loadValues();
		fTasksGroup.initFields();
	}
	
	private void doEdit(TaskTag item) {
		
		TaskTagsInputDialog dialog = new TaskTagsInputDialog(getShell(), item, fTasksGroup.fSelectionModel);
		if (dialog.open() == Window.OK) {
			if (item != null) {
				fTasksGroup.replaceItem(item, dialog.getResult());
			} else {
				fTasksGroup.addItem(dialog.getResult());
			}
			saveValues(PREF_TASK_TAGS);
		}
		
	}
	
	private void doRemove(List<TaskTag> selection) {
		
		if (selection.contains(fTasksGroup.fDefaultTask))
			fTasksGroup.fDefaultTask = null;
		fTasksGroup.removeItems(selection);
		
		saveValues(PREF_TASK_TAGS);
	}
	
	private void doSetDefault(TaskTag item) {
		
		fTasksGroup.fDefaultTask = item;
		if (fTasksGroup.fSelectionModel.indexOf(item) != 0) {
			fTasksGroup.fSelectionModel.remove(item);
			fTasksGroup.fSelectionModel.add(0, item);
		}
		fTasksGroup.fSelectionViewer.refresh();
		fTasksGroup.handleListSelection();
		
		saveValues(PREF_TASK_TAGS);
	}

	@Override
	protected void updateControls() {
		
		loadValues();
		fTasksGroup.fSelectionViewer.refresh();
	}
		
	private void loadValues() {

		String tagValue = getValue(PREF_TASK_TAGS);
		String priosValue = getValue(PREF_TASK_TAGS_PRIORITIES);
		String[] tags = tagValue.split(",");
		String[] prios = priosValue.split(",");
		Assert.isTrue(tags.length == prios.length);
		
		List<TaskTag> items = new ArrayList<TaskTag>(tags.length);
		for (int i = 0; i < tags.length; i++) {
			String tagName = tags[i]; //.trim();
			if (tagName.length() > 0)
				items.add(new TaskTag(tagName, TaskPriority.valueOf(prios[i].trim()) ));
		}
		fTasksGroup.fSelectionModel.clear();
		fTasksGroup.fSelectionModel.addAll(items);
		
		if (!items.isEmpty())
			fTasksGroup.fDefaultTask = items.get(0);
	}

	private void saveValues(Key key) {
		
		if (key == PREF_TASK_TAGS) {
			StringBuffer tags = new StringBuffer();
			StringBuffer prios = new StringBuffer();
			for (TaskTag tag : fTasksGroup.fSelectionModel) {
				tags.append(tag.fName);
				tags.append(',');
				prios.append(tag.fPriority.toString());
				prios.append(',');
			}
				
			setValue(PREF_TASK_TAGS, tags.toString());
			setValue(PREF_TASK_TAGS_PRIORITIES, prios.toString());
		}
		
		validateSettings();
	}
	
	private IStatus validateSettings() {
		
		StatusInfo listStatus = new StatusInfo();
		if (fTasksGroup.fSelectionModel.size() == 0) {
			listStatus.setWarning(Messages.TaskTags_warning_NoTag_message);
		} 
		else if (fTasksGroup.fDefaultTask == null) {
			listStatus.setError(Messages.TaskTags_error_DefaultTast_message);
		}

		IStatus status = listStatus; 		// StatusUtil.getMostSevere(new IStatus[] { ... });
		fStatusListener.statusChanged(status);
		return status;
	}
	
	
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		
		String title = Messages.TaskTags_NeedsBuild_title; 
		String message;
		if (workspaceSettings) {
			message = Messages.TaskTags_NeedsFullBuild_message; 
		} else {
			message = Messages.TaskTags_NeedsProjectBuild_message; 
		}	
		return new String[] { title, message };
	}	

}
