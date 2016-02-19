/*=============================================================================#
 # Copyright (c) 2000-2016 IBM Corporation and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     IBM Corporation - initial API and implementation in JDT
 #     Stephan Wahlbrink - adaptations to StatET
 #=============================================================================*/

package de.walware.statet.base.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.tasklist.TaskPriority;
import de.walware.ecommons.tasklist.TaskTag;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.dialogs.groups.Layouter;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.PixelConverter;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.base.internal.ui.StatetMessages;
import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class TaskTagsConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private final IStatusChangeListener fStatusListener;
	
	private TableViewer listViewer;
	private ButtonGroup<TaskTag> listButtons;
	
	private Image taskIcon;
	private Image taskDefaultIcon;
	
	private final IObservableList list;
	private final IObservableValue defaultValue;
	
	
	public TaskTagsConfigurationBlock(final IProject project, final IStatusChangeListener statusListener) {
		super(project);
		this.fStatusListener = statusListener;
		
		this.list= new WritableList();
		this.defaultValue= new WritableValue();
	}
	
	
	final boolean isDefaultTask(final TaskTag task) {
		return (this.defaultValue.getValue() == task);
	}
	
	@Override
	protected String getHelpContext() {
		return StatetUIPlugin.PLUGIN_ID + ".task_tags_preferences"; //$NON-NLS-1$
	}
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs = new HashMap<>();
		
		prefs.put(TaskTagsPreferences.PREF_TAGS, TaskTagsPreferences.GROUP_ID);
		prefs.put(TaskTagsPreferences.PREF_PRIORITIES, TaskTagsPreferences.GROUP_ID);
		 
		setupPreferenceManager(prefs);
		
		createImages();
		
		{	// Table area
			final Composite composite = new Composite(pageComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.createCompositeGrid(2));
			
			final Composite table = createTable(composite);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			this.listButtons = new ButtonGroup<TaskTag>(composite) {
				@Override
				protected TaskTag edit1(final TaskTag item, final boolean newItem, final Object parent) {
					final TaskTagsInputDialog dialog = new TaskTagsInputDialog(getShell(), item, newItem, TaskTagsConfigurationBlock.this.list);
					if (dialog.open() == Dialog.OK) {
						return dialog.getResult();
					}
					return null;
				}
				@Override
				public void updateState() {
					super.updateState();
					saveTaskTags();
				}
			};
			this.listButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			this.listButtons.addAddButton(null);
			this.listButtons.addCopyButton(null);
			this.listButtons.addEditButton(null);
			this.listButtons.addDeleteButton(null);
			this.listButtons.addSeparator();
			this.listButtons.addDefaultButton(null);
			
			this.listButtons.connectTo(this.listViewer, this.list, this.defaultValue);
			this.listViewer.setInput(this.list);
		}
		
		updateControls();
	}
	
	protected Composite createTable(final Composite parent) {
		final TableComposite composite = new ViewerUtil.TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		this.listViewer = composite.viewer;
		composite.table.setHeaderVisible(true);
		composite.table.setLinesVisible(true);
		
		final PixelConverter conv = new PixelConverter(composite.table);
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.LEFT);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText(Messages.TaskTags_TaskColumn_name);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(final Object element) {
					final TaskTag tag = (TaskTag) element;
					return (isDefaultTask(tag)) ? TaskTagsConfigurationBlock.this.taskDefaultIcon : TaskTagsConfigurationBlock.this.taskIcon;
				}
				@Override
				public String getText(final Object element) {
					final TaskTag tag = (TaskTag) element;
					return tag.getKeyword();
				}
			});
		}
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.LEFT);
			int priorityWidth = Messages.TaskTags_PriorityColumn_name.length();
			priorityWidth = Math.max(priorityWidth, StatetMessages.TaskPriority_High.length());
			priorityWidth = Math.max(priorityWidth, StatetMessages.TaskPriority_Normal.length());
			priorityWidth = Math.max(priorityWidth, StatetMessages.TaskPriority_Low.length());
			priorityWidth = conv.convertWidthInCharsToPixels(priorityWidth) + conv.convertHorizontalDLUsToPixels(5);
			composite.layout.setColumnData(column.getColumn(), new ColumnPixelData(priorityWidth, false, true));
			
			column.getColumn().setText(Messages.TaskTags_PriorityColumn_name);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(final Object element) {
					final TaskTag task = (TaskTag) element;
					switch (task.getPriority()) {
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
		}
		
		composite.viewer.setContentProvider(new ArrayContentProvider());
		// Sorter
		composite.viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				return getComparator().compare(((TaskTag) e1).getKeyword(), ((TaskTag) e2).getKeyword());
			}
		});
		return composite;
	}
	
	private void createImages() {
		final Image baseImage = PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJS_TASK_TSK);
		this.taskIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, null, null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
		this.taskDefaultIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, SharedUIResources.getImages().getDescriptor(SharedUIResources.OVR_DEFAULT_MARKER_IMAGE_ID), null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
	}
	
	@Override
	public void dispose() {
		if (this.taskIcon != null) {
			this.taskIcon.dispose();
			this.taskIcon = null;
		}
		if (this.taskDefaultIcon != null) {
			this.taskDefaultIcon.dispose();
			this.taskDefaultIcon = null;
		}
		super.dispose();
	}
	
	@Override
	protected void updateControls() {
		loadValues();
		this.listViewer.refresh();
		this.listButtons.updateState();
		
		ViewerUtil.scheduleStandardSelection(this.listViewer);
	}
	
	private void loadValues() {
		this.list.clear();
		
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(this);
		this.list.addAll(taskPrefs.getTaskTags());
		
		if (!list.isEmpty()) {
			this.defaultValue.setValue(list.get(0));
		}
	}
	
	private void saveTaskTags() {
		ArrayList<TaskTag> taskTags= new ArrayList<>(this.list);
		final TaskTag defaultTag = (TaskTag) this.defaultValue.getValue();
		if (defaultTag != null) {
			taskTags.remove(defaultTag);
			taskTags.add(0, defaultTag);
		}
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(taskTags);
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
		if (this.list.size() == 0) {
			listStatus.setWarning(Messages.TaskTags_warning_NoTag_message);
		} 
		else if (this.defaultValue.getValue() == null) {
			listStatus.setError(Messages.TaskTags_error_DefaultTast_message);
		}
		
		final IStatus status = listStatus; // StatusUtil.getMostSevere(new IStatus[] { ... });
		this.fStatusListener.statusChanged(status);
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


class TaskTagsInputDialog extends ExtStatusDialog {
	
	
	private Text keywordControl;
	private Combo priorityControl;
	
	private String keyword;
	private TaskPriority priority;
	
	private final List<String> existingKeywords;
	
	
	public TaskTagsInputDialog(final Shell parent, final TaskTag task, final boolean newTask,
			final List<TaskTag> existingTags) {
		super(parent);
		
		if (task != null) {
			this.keyword = task.getKeyword();
			this.priority = task.getPriority();
		}
			
		this.existingKeywords = new ArrayList<>(existingTags.size());
		for (int i = 0; i < existingTags.size(); i++) {
			final TaskTag curr = existingTags.get(i);
			if (newTask || !curr.equals(task)) {
				this.existingKeywords.add(curr.getKeyword());
			}
		}
		
		setTitle((task == null) ?
				Messages.TaskTags_InputDialog_NewTag_title :
				Messages.TaskTags_InputDialog_EditTag_title );
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogUtil.getDialogSettings(StatetUIPlugin.getDefault(), "TaskTagEditDialog"); //$NON-NLS-1$
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = new Composite(parent, SWT.NONE);
		final Layouter layouter = new Layouter(area, LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.keywordControl = layouter.addLabeledTextControl(Messages.TaskTags_InputDialog_Name_label);
		((GridData) this.keywordControl.getLayoutData()).widthHint =
				new PixelConverter(this.keywordControl).convertWidthInCharsToPixels(45);
		this.keywordControl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				TaskTagsInputDialog.this.keyword = TaskTagsInputDialog.this.keywordControl.getText();
				doValidation();
			};
		});
		
		final String[] items = new String[] {
				StatetMessages.TaskPriority_High,
				StatetMessages.TaskPriority_Normal,
				StatetMessages.TaskPriority_Low,
		};
		this.priorityControl = layouter.addLabeledComboControl(Messages.TaskTags_InputDialog_Priority_label, items);
		this.priorityControl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				switch (TaskTagsInputDialog.this.priorityControl.getSelectionIndex()) {
				case 0:
					TaskTagsInputDialog.this.priority = TaskPriority.HIGH;
					break;
				case 2:
					TaskTagsInputDialog.this.priority = TaskPriority.LOW;
					break;
				default:
					TaskTagsInputDialog.this.priority = TaskPriority.NORMAL;
					break;
				}
			};
		});
		
		// Init Fields
		if (this.keyword != null) {
			this.keywordControl.setText(this.keyword);
			switch (this.priority) {
			case HIGH:
				this.priorityControl.select(0);
				break;
			case LOW:
				this.priorityControl.select(2);
				break;
			default: // NORMAL
				this.priorityControl.select(1);
				break;
			}
		} else {
			this.priorityControl.select(1);
		}
		final Display display = parent.getDisplay();
		if (display != null) {
			display.asyncExec(
				new Runnable() {
					@Override
					public void run() {
						TaskTagsInputDialog.this.keywordControl.setFocus();
					}
				}
			);
		}
		
		LayoutUtil.addSmallFiller(area, true);
		
		applyDialogFont(area);
		
		return area;
	}
	
	
	public TaskTag getResult() {
		return new TaskTag(this.keyword, this.priority);
	}
	
	private void doValidation() {
		final StatusInfo status = new StatusInfo();
		final String newText = this.keywordControl.getText();
		if (newText.isEmpty()) {
			status.setError(Messages.TaskTags_InputDialog_error_EnterName_message);
		} else {
			if (newText.indexOf(',') != -1) {
				status.setError(Messages.TaskTags_InputDialog_error_Comma_message);
			} else if (this.existingKeywords.contains(newText)) {
				status.setError(Messages.TaskTags_InputDialog_error_EntryExists_message);
			} else if (!Character.isLetterOrDigit(newText.charAt(0))) { // ||  Character.isWhitespace(newText.charAt(newText.length() - 1))) {
				status.setError(Messages.TaskTags_InputDialog_error_ShouldStartWithLetterOrDigit_message);
			}
		}
		updateStatus(status);
	}
	
}
