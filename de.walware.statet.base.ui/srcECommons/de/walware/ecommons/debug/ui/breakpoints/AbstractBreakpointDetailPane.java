/*******************************************************************************
 *  Copyright (c) 2009-2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.debug.ui.breakpoints;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.ui.IDetailPane3;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ui.util.LayoutUtil;


/**
 * Common detail pane function.
 */
public abstract class AbstractBreakpointDetailPane implements IDetailPane3 {
	
	
	private final String fId;
	private final String fName;
	private final String fDescription;
	
	private AbstractBreakpointDetailEditor fEditor;
	private IWorkbenchPartSite fSite;
	
	// property listeners
	private final FastList<IPropertyListener> fListeners = new FastList<IPropertyListener>(IPropertyListener.class);
	
	private Composite fComposite;
	
	
	/**
	 * Constructs a detail pane.
	 * 
	 * @param id detail pane ID
	 * @param name detail pane name
	 * @param description detail pane description
	 */
	public AbstractBreakpointDetailPane(final String id, final String name, final String description) {
		fId = id;
		fName = name;
		fDescription = description;
	}
	
	@Override
	public void init(final IWorkbenchPartSite partSite) {
		fSite = partSite;
	}
	
	@Override
	public void dispose() {
		fEditor = null;
		fSite = null;
		fListeners.clear();
		fComposite.dispose();
	}
	
	
	@Override
	public String getID() {
		return fId;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public String getDescription() {
		return fDescription;
	}
	
	
	@Override
	public void addPropertyListener(final IPropertyListener listener) {
		fListeners.add(listener);
	}
	
	@Override
	public void removePropertyListener(final IPropertyListener listener) {
		fListeners.remove(listener);
	}
	
	protected FastList<IPropertyListener> getPropertyListeners() {
		return fListeners;
	}
	
	/**
	 * Fires a property change to all listeners.
	 * 
	 * @param property the property
	 */
	protected void firePropertyChange(final int property) {
		for (final IPropertyListener listener : fListeners.toArray()) {
			listener.propertyChanged(this, property);
		}
	}
	
	@Override
	public Control createControl(final Composite parent) {
		fComposite = new Composite(parent, SWT.NONE);
		fComposite.setLayout(LayoutUtil.createContentGrid(1));
		fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fEditor = createEditor(fComposite);
		final Control editorControl = fEditor.createControl(fComposite);
		editorControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return fComposite;
	}
	
	/**
	 * Creates the detail pane specific editor.
	 * 
	 * @param parent parent composite
	 * @return editor
	 */
	protected abstract AbstractBreakpointDetailEditor createEditor(Composite parent);
	
	/**
	 * Returns the editor associated with this detail pane.
	 * 
	 * @return editor
	 */
	protected AbstractBreakpointDetailEditor getEditor() {
		return fEditor;
	}
	
	@Override
	public boolean setFocus() {
		return fComposite.setFocus();
	}
	
	
	@Override
	public boolean isDirty() {
		return (fEditor != null && fEditor.isDirty());
	}
	
	@Override
	public void doSaveAs() {
		// do nothing
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public boolean isSaveOnCloseNeeded() {
		return isDirty() && fEditor.getStatus().isOK();
	}
	
	@Override
	public void doSave(final IProgressMonitor monitor) {
		final IStatusLineManager statusLine = getStatusLine();
		if (statusLine != null) {
			statusLine.setErrorMessage(null);
		}
		final IStatus status = fEditor.save();
		if (status != null && !status.isOK()) {
			if (statusLine != null) {
				statusLine.setErrorMessage(status.getMessage());
				Display.getCurrent().beep();
			}
		}
	}
	
	private IStatusLineManager getStatusLine() {
		// we want to show messages globally hence we
		// have to go through the active part
		if (fSite instanceof IViewSite) {
			final IViewSite site = (IViewSite) fSite;
//			IWorkbenchPage page = site.getPage();
//			IWorkbenchPart activePart = page.getActivePart();
//			
//			if (activePart instanceof IViewPart) {
//				IViewPart activeViewPart = (IViewPart) activePart;
//				IViewSite activeViewSite = activeViewPart.getViewSite();
//				return activeViewSite.getActionBars().getStatusLineManager();
//			}
//			
//			if (activePart instanceof IEditorPart) {
//				IEditorPart activeEditorPart = (IEditorPart) activePart;
//				IEditorActionBarContributor contributor = activeEditorPart.getEditorSite().getActionBarContributor();
//				if (contributor instanceof EditorActionBarContributor)
//					return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
//			}
			// no active part
			return site.getActionBars().getStatusLineManager();
		}
		return null;
	}
	
	@Override
	public void display(final IStructuredSelection selection) {
		// clear status line
		final IStatusLineManager statusLine = getStatusLine();
		if (statusLine != null) {
			statusLine.setErrorMessage(null);
		}
		final AbstractBreakpointDetailEditor editor = getEditor();
		Object input = null;
		if (selection != null && selection.size() == 1) {
			input = selection.getFirstElement();
			// update even if the same in case attributes have changed
		}
		
		editor.setInput(input);
	}
	
}
