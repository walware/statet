/*******************************************************************************
 * Copyright (c) 2000-2012 IBM Corporation and others.
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
import java.util.Iterator;
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
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
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
import de.walware.statet.base.core.preferences.TaskTagsPreferences.TaskPriority;
import de.walware.statet.base.internal.ui.StatetMessages;
import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * 
 */
public class TaskTagsConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private IStatusChangeListener fStatusListener;
	
	private TableViewer fListViewer;
	private ButtonGroup<TaskTag> fListButtons;
	
	private Image fTaskIcon;
	private Image fTaskDefaultIcon;
	
	private final IObservableList fList;
	private final IObservableValue fDefault;
	
	
	public TaskTagsConfigurationBlock(final IProject project, final IStatusChangeListener statusListener) {
		super(project);
		fStatusListener = statusListener;
		
		fList = new WritableList();
		fDefault = new WritableValue();
	}
	
	
	final boolean isDefaultTask(final TaskTag task) {
		return (fDefault.getValue() == task);
	}
	
	@Override
	protected String getHelpContext() {
		return StatetUIPlugin.PLUGIN_ID + ".task_tags_preferences"; //$NON-NLS-1$
	}
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs = new HashMap<Preference<?>, String>();
		
		prefs.put(TaskTagsPreferences.PREF_TAGS, TaskTagsPreferences.GROUP_ID);
		prefs.put(TaskTagsPreferences.PREF_PRIORITIES, TaskTagsPreferences.GROUP_ID);
		 
		setupPreferenceManager(prefs);
		
		createImages();
		
		{	// Table area
			final Composite composite = new Composite(pageComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Composite table = createTable(composite);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			fListButtons = new ButtonGroup<TaskTag>(composite) {
				@Override
				protected TaskTag edit1(final TaskTag item, final boolean newItem, Object parent) {
					final TaskTagsInputDialog dialog = new TaskTagsInputDialog(getShell(), item, newItem, fList);
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
			fListButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			fListButtons.addAddButton(null);
			fListButtons.addCopyButton(null);
			fListButtons.addEditButton(null);
			fListButtons.addDeleteButton(null);
			fListButtons.addSeparator();
			fListButtons.addDefaultButton(null);
			
			fListButtons.connectTo(fListViewer, fList, fDefault);
			fListViewer.setInput(fList);
		}
		
		updateControls();
	}
	
	protected Composite createTable(final Composite parent) {
		final TableComposite composite = new ViewerUtil.TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		fListViewer = composite.viewer;
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
					return (isDefaultTask(tag)) ? fTaskDefaultIcon : fTaskIcon;
				}
				@Override
				public String getText(final Object element) {
					final TaskTag tag = (TaskTag) element;
					return tag.name;
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
					switch (task.priority) {
					case HIGH:
						return StatetMessages.TaskPriority_High; 
					case NORMAL:
						return StatetMessages.TaskPriority_Normal; 
					case LOW:
						return StatetMessages.TaskPriority_Low;
					default:
						return ""; 
					}
				}
			});
		}
		
		composite.viewer.setContentProvider(new ArrayContentProvider());
		// Sorter
		composite.viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				return getComparator().compare(((TaskTag) e1).name, ((TaskTag) e2).name);
			}
		});
		return composite;
	}
	
	private void createImages() {
		final Image baseImage = PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJS_TASK_TSK);
		fTaskIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, null, null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
		fTaskDefaultIcon = new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, SharedUIResources.getImages().getDescriptor(SharedUIResources.OVR_DEFAULT_MARKER_IMAGE_ID), null},
				new Point(baseImage.getBounds().width+4, baseImage.getBounds().height)).createImage();
	}
	
	@Override
	public void dispose() {
		if (fTaskIcon != null) {
			fTaskIcon.dispose();
			fTaskIcon = null;
		}
		if (fTaskDefaultIcon != null) {
			fTaskDefaultIcon.dispose();
			fTaskDefaultIcon = null;
		}
		super.dispose();
	}
	
	@Override
	protected void updateControls() {
		loadValues();
		fListViewer.refresh();
		fListButtons.updateState();
	}
	
	private void loadValues() {
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(this);
		final String[] tags = taskPrefs.getTags();
		final TaskPriority[] prios = taskPrefs.getPriorities();
		
		final List<TaskTag> items = new ArrayList<TaskTag>(tags.length);
		for (int i = 0; i < tags.length; i++) {
			items.add(new TaskTag(tags[i], prios[i]));
		}
		fList.clear();
		fList.addAll(items);
		
		if (!items.isEmpty()) {
			fDefault.setValue(items.get(0));
		}
	}
	
	private void saveTaskTags() {
		final int n = fList.size();
		final String[] tags = new String[n];
		final TaskPriority[] prios = new TaskPriority[n];
		if (n > 0) {
			int i = 0;
			final TaskTag defaultTag = (TaskTag) fDefault.getValue();
			if (defaultTag != null) {
				tags[0] = defaultTag.name;
				prios[0] = defaultTag.priority;
				i++;
			}
			for (final Iterator<?> iter = fList.iterator(); iter.hasNext(); ) {
				final TaskTag item = (TaskTag) iter.next();
				if (item != defaultTag) {
					tags[i] = item.name;
					prios[i] = item.priority;
					i++;
				}
			}
		}
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(tags, prios);
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
		if (fList.size() == 0) {
			listStatus.setWarning(Messages.TaskTags_warning_NoTag_message);
		} 
		else if (fDefault.getValue() == null) {
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


class TaskTag {
	
	String name;
	TaskPriority priority;
	
	public TaskTag(final String name, final TaskPriority priority) {
		this.name = name;
		this.priority = priority;
	}
	
}


class TaskTagsInputDialog extends ExtStatusDialog {
	
	
	private Text fNameControl;
	private Combo fPriorityControl;
	
	private String fName;
	private TaskPriority fPriority;
	private List<String> fExistingNames;
	
	
	public TaskTagsInputDialog(final Shell parent, final TaskTag task, final boolean newTask, final List<TaskTag> existingEntries) {
		super(parent);
		
		if (task != null) {
			fName = task.name;
			fPriority = task.priority;
		}
			
		fExistingNames = new ArrayList<String>(existingEntries.size());
		for (int i = 0; i < existingEntries.size(); i++) {
			final TaskTag curr = existingEntries.get(i);
			if (newTask || !curr.equals(task)) {
				fExistingNames.add(curr.name);
			}
		}
		
		setTitle((task == null) ?
				Messages.TaskTags_InputDialog_NewTag_title :
				Messages.TaskTags_InputDialog_EditTag_title );
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return DialogUtil.getDialogSettings(StatetUIPlugin.getDefault(), "TaskTagEditDialog"); 
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = new Composite(parent, SWT.NONE);
		final Layouter layouter = new Layouter(dialogArea, LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fNameControl = layouter.addLabeledTextControl(Messages.TaskTags_InputDialog_Name_label);
		((GridData) fNameControl.getLayoutData()).widthHint =
				new PixelConverter(fNameControl).convertWidthInCharsToPixels(45);
		fNameControl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				fName = fNameControl.getText();
				doValidation();
			};
		});
		
		final String[] items = new String[] {
				StatetMessages.TaskPriority_High,
				StatetMessages.TaskPriority_Normal,
				StatetMessages.TaskPriority_Low,
		};
		fPriorityControl = layouter.addLabeledComboControl(Messages.TaskTags_InputDialog_Priority_label, items);
		fPriorityControl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
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
		final Display display = parent.getDisplay();
		if (display != null) {
			display.asyncExec(
				new Runnable() {
					@Override
					public void run() {
						fNameControl.setFocus();
					}
				}
			);
		}
		
		LayoutUtil.addSmallFiller(dialogArea, true);
		applyDialogFont(dialogArea);
		return dialogArea;
	}
	
	
	public TaskTag getResult() {
		return new TaskTag(fName, fPriority);
	}
	
	private void doValidation() {
		final StatusInfo status = new StatusInfo();
		final String newText = fNameControl.getText();
		if (newText.isEmpty()) {
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
