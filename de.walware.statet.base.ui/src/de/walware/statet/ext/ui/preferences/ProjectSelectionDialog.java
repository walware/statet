/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import java.util.Set;

import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

import de.walware.eclipsecommons.ui.dialogs.StatusInfo;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class ProjectSelectionDialog<ProjectNature extends IProjectNature> extends SelectionStatusDialog {
	
	
	private static class ContentProvider implements IStructuredContentProvider {
		
		public Object[] getElements(final Object inputElement) {
			return ((Set) inputElement).toArray();
		}
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}
		public void dispose() {
		}
	}
	
	private static class ProjectLabelProvider extends LabelProvider {
		
		@Override
		public String getText(final Object element) {
			if (element instanceof IProjectNature)
				return ((IProjectNature) element).getProject().getName();
			return super.getText(element);
		}
	}
	
	private static class ProjectComparator extends ViewerComparator {
		
		@SuppressWarnings("unchecked")
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			
			return getComparator().compare( ((IProjectNature) e1).getProject().getName(), ((IProjectNature) e2).getProject().getName() );
		}
	}
	
	
	private Set<ProjectNature> fAllItems;
	private Set<ProjectNature> fFilteredItems;
	
	// the visual selection widget group
	private TableViewer fTableViewer;
	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
	
	private ViewerFilter fFilter;
	private final static String DIALOG_SETTINGS_SHOW_ALL = "ProjectSelectionDialog.show_all"; //$NON-NLS-1$
	
	
	public ProjectSelectionDialog(final Shell parentShell, final Set<ProjectNature> all, final Set<ProjectNature> filtered) {
		super(parentShell);
		setTitle(Messages.ProjectSelectionDialog_title);  
		setMessage(Messages.ProjectSelectionDialog_desciption);
		
		fAllItems = all;
		fFilteredItems = filtered;
		
		final int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
		
		fFilter = new ViewerFilter() {
			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
				for (final Object object : fFilteredItems) {
					if (element == object)
						return true;
				}
				return false;
			}
		};
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		// page group
		final Composite composite = (Composite) super.createDialogArea(parent);
		
		createMessageArea(composite);
		
		fTableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				doSelectionChanged(((IStructuredSelection) event.getSelection()).toArray());
			}
		});
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				okPressed();
			}
		});
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		fTableViewer.getTable().setLayoutData(data);
		
		fTableViewer.setContentProvider(new ContentProvider());
		fTableViewer.setLabelProvider(new ProjectLabelProvider());
		fTableViewer.setComparator((new ProjectComparator()));
		
		final Button checkbox = new Button(composite, SWT.CHECK);
		checkbox.setText(Messages.ProjectSelectionDialog_filter); 
		checkbox.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		checkbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(final SelectionEvent e) {
				updateFilter(((Button) e.widget).getSelection());
			}
			public void widgetDefaultSelected(final SelectionEvent e) {
				updateFilter(((Button) e.widget).getSelection());
			}
		});
		final IDialogSettings dialogSettings = StatetUIPlugin.getDefault().getDialogSettings();
		final boolean doFilter = !dialogSettings.getBoolean(DIALOG_SETTINGS_SHOW_ALL) && !fFilteredItems.isEmpty();
		checkbox.setSelection(doFilter);
		updateFilter(doFilter);
		
		fTableViewer.setInput(fAllItems);
		
		doSelectionChanged(new Object[0]);
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	protected void updateFilter(final boolean selected) {
		if (selected) {
			fTableViewer.addFilter(fFilter);
		} else {
			fTableViewer.removeFilter(fFilter);
		}
		StatetUIPlugin.getDefault().getDialogSettings().put(DIALOG_SETTINGS_SHOW_ALL, !selected);
	}
	
	private void doSelectionChanged(final Object[] objects) {
		if (objects.length != 1) {
			updateStatus(new StatusInfo(IStatus.ERROR, "")); //$NON-NLS-1$
			setSelectionResult(null);
		} else {
			updateStatus(new StatusInfo()); 
			setSelectionResult(objects);
		}
	}
	
	@Override
	protected void computeResult() {
	}
	
}
