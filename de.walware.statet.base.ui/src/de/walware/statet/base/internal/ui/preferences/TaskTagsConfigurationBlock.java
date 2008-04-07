/*******************************************************************************
 * Copyright (c) 2000-2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation in JDT
 *     Stephan Wahlbrink - adaptations to StatET
 *******************************************************************************/

package de.walware.statet.base.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.SharedMessages;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.dialogs.StatusInfo;
import de.walware.eclipsecommons.ui.dialogs.groups.TableOptionButtonsGroup;
import de.walware.eclipsecommons.ui.util.PixelConverter;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.base.core.preferences.TaskTagsPreferences.TaskPriority;
import de.walware.statet.base.internal.ui.StatetMessages;
import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;


/**
 * 
 */
public class TaskTagsConfigurationBlock extends ManagedConfigurationBlock {
	
	
	static class TaskTag {
		
		String name;
		TaskPriority priority;
		
		public TaskTag(final String name, final TaskPriority priority) {
			this.name = name;
			this.priority = priority;
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
					SharedMessages.CollectionEditing_AddItem_label, 
					SharedMessages.CollectionEditing_EditItem_label, 
					SharedMessages.CollectionEditing_RemoveItem_label, 
					null,
					SharedMessages.CollectionEditing_DefaultItem_label, 
			} );
			setDefaultButton(IDX_EDIT);
			setRemoveButton(IDX_REMOVE);
		}
		
		@Override
		protected void createTableColumns(final TableViewer viewer, final Table table, final TableLayout layout) {
			final PixelConverter conv = new PixelConverter(table);
			TableViewerColumn col;
			
			col = new TableViewerColumn(viewer, SWT.LEFT);
			col.getColumn().setText(Messages.TaskTags_TaskColumn_name);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(final Object element) {
					final TaskTag tag = (TaskTag) element;
					final Image baseImage = PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJS_TASK_TSK);
					final ImageDescriptor defaultOverlay = (isDefaultTask(tag)) ? StatetImages.getDescriptor(StatetImages.OVR_DEFAULT_MARKER) : null;
					return new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
								null, null, null, defaultOverlay, null}, 
								new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
				}
				@Override
				public String getText(final Object element) {
					final TaskTag tag = (TaskTag) element;
					return tag.name;
				}
			});
			layout.addColumnData(new ColumnWeightData(1));
			
			col = new TableViewerColumn(viewer, SWT.LEFT);
			col.getColumn().setText(Messages.TaskTags_PriorityColumn_name);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(final Object element) {
					final TaskTag task = (TaskTag) element;
					switch (task.priority) {
					case HIGH:
						return StatetMessages.TaskPriority_High; 
					case NORMAL:
						return StatetMessages.TaskPriority_Normal; 
					case LOW:
						return StatetMessages.TaskPriority_Low;
					default:
						return ""; //$NON-NLS-1$
					}
				}
			});
			int priorityWidth = Messages.TaskTags_PriorityColumn_name.length();
			priorityWidth = Math.max(priorityWidth, StatetMessages.TaskPriority_High.length());
			priorityWidth = Math.max(priorityWidth, StatetMessages.TaskPriority_Normal.length());
			priorityWidth = Math.max(priorityWidth, StatetMessages.TaskPriority_Low.length());
			priorityWidth = conv.convertWidthInCharsToPixels(priorityWidth) + conv.convertHorizontalDLUsToPixels(5);
			layout.addColumnData(new ColumnPixelData(priorityWidth, false, true));
			
			// Sorter
			viewer.setComparator(new ViewerComparator() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(final Viewer viewer, final Object e1, final Object e2) {
					return getComparator().compare(((TaskTag) e1).name, ((TaskTag) e2).name);
				}
			});
		}
		
		
		@Override
		public void handleSelection(final TaskTag item, final IStructuredSelection rawSelection) {
			fButtonGroup.enableButton(IDX_EDIT, (item != null) );
			fButtonGroup.enableButton(IDX_DEFAULT, (item != null) && !isDefaultTask(item));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void handleButtonPressed(final int buttonIdx, final TaskTag item, final IStructuredSelection rawSelection) {
			switch (buttonIdx) {
			case IDX_ADD:
				doEdit(null);
				break;
			
			case IDX_EDIT:
				if (item != null)
					doEdit(item);
				break;
				
			case IDX_REMOVE:
				if (!rawSelection.isEmpty())
					doRemove(rawSelection.toList());
				break;
				
			case IDX_DEFAULT:
				if (item != null)
					doSetDefault(item);
				break;
			}
		}
		
	}
	
	
	private TasksGroup fTasksGroup;
	
	private IStatusChangeListener fStatusListener;
	
	
	public TaskTagsConfigurationBlock(final IProject project, final IStatusChangeListener statusListener) {
		super(project);
		fStatusListener = statusListener;
		fTasksGroup = new TasksGroup();
	}
	
	
	final boolean isDefaultTask(final TaskTag task) {
		return (task == fTasksGroup.fDefaultTask);
	}
	
	@Override
	public void createContents(final Composite pageComposite, final IWorkbenchPreferenceContainer container, 
			final IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		
		prefs.put(TaskTagsPreferences.PREF_TAGS, TaskTagsPreferences.GROUP_ID);
		prefs.put(TaskTagsPreferences.PREF_PRIORITIES, TaskTagsPreferences.GROUP_ID);
		
		setupPreferenceManager(container, prefs);
		
		fTasksGroup.createGroup(pageComposite, 1);
		
		loadValues();
		fTasksGroup.initFields();
	}
	
	private void doEdit(final TaskTag item) {
		final TaskTagsInputDialog dialog = new TaskTagsInputDialog(getShell(), item, fTasksGroup.getListModel());
		if (dialog.open() == Window.OK) {
			final TaskTag newItem = dialog.getResult();
			if (item != null) {
				if (item == fTasksGroup.fDefaultTask)
					fTasksGroup.fDefaultTask = newItem;
				fTasksGroup.replaceItem(item, newItem);
			} else {
				fTasksGroup.addItem(newItem);
			}
			
			saveTaskTags();
		}
	}
	
	private void doRemove(final List<TaskTag> selection) {
		if (selection.contains(fTasksGroup.fDefaultTask)) {
			fTasksGroup.fDefaultTask = null;
		}
		fTasksGroup.removeItems(selection);
		
		saveTaskTags();
	}
	
	private void doSetDefault(final TaskTag item) {
		fTasksGroup.fDefaultTask = item;
		if (fTasksGroup.getListModel().indexOf(item) != 0) {
			fTasksGroup.getListModel().remove(item);
			fTasksGroup.getListModel().add(0, item);
		}
		fTasksGroup.getStructuredViewer().refresh();
		fTasksGroup.reselect();
		
		saveTaskTags();
	}
	
	@Override
	protected void updateControls() {
		loadValues();
		fTasksGroup.getStructuredViewer().refresh();
	}
	
	private void loadValues() {
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(this);
		final String[] tags = taskPrefs.getTags();
		final TaskPriority[] prios = taskPrefs.getPriorities();
		
		final List<TaskTag> items = new ArrayList<TaskTag>(tags.length);
		for (int i = 0; i < tags.length; i++) {
			items.add(new TaskTag(tags[i], prios[i]));
		}
		fTasksGroup.getListModel().clear();
		fTasksGroup.getListModel().addAll(items);
		
		if (!items.isEmpty()) {
			fTasksGroup.fDefaultTask = items.get(0);
		}
	}
	
	private void saveTaskTags() {
		final int n = fTasksGroup.getListModel().size();
		final String[] tags = new String[n];
		final TaskPriority[] prios = new TaskPriority[n];
		for (int i = 0; i < n; i++) {
			final TaskTag item = fTasksGroup.getListModel().get(i);
			tags[i] = item.name;
			prios[i] = item.priority;
		}
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(
				tags, prios);
		
		setPrefValues(taskPrefs.getPreferencesMap());
		
		validateSettings();
	}
	
	@Override
	public void performDefaults() {
		super.performDefaults();
		validateSettings();
	}
	
	private IStatus validateSettings() {
		final StatusInfo listStatus = new StatusInfo();
		if (fTasksGroup.getListModel().size() == 0) {
			listStatus.setWarning(Messages.TaskTags_warning_NoTag_message);
		} 
		else if (fTasksGroup.fDefaultTask == null) {
			listStatus.setError(Messages.TaskTags_error_DefaultTast_message);
		}
		
		final IStatus status = listStatus; // StatusUtil.getMostSevere(new IStatus[] { ... });
		fStatusListener.statusChanged(status);
		return status;
	}
	
	
	@Override
	protected String[] getFullBuildDialogStrings(final boolean workspaceSettings) {
		final String title = Messages.TaskTags_NeedsBuild_title;
		String message;
		if (workspaceSettings) {
			message = Messages.TaskTags_NeedsFullBuild_message; 
		} else {
			message = Messages.TaskTags_NeedsProjectBuild_message; 
		}	
		return new String[] { title, message };
	}	
	
}
