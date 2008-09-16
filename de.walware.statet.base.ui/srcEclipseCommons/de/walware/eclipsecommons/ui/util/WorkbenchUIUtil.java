/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Util methods for Eclipse IDE workbench
 */
public class WorkbenchUIUtil {
	
	
	public static ISelection getCurrentSelection(final Object context) {
		if (context instanceof IEvaluationContext) {
			final IEvaluationContext evaluationContext = (IEvaluationContext) context;
			final IWorkbenchSite site = (IWorkbenchSite) evaluationContext.getVariable(ISources.ACTIVE_SITE_NAME);
			if (site != null) {
				final ISelectionProvider selectionProvider = site.getSelectionProvider();
				if (selectionProvider != null) {
					return selectionProvider.getSelection();
				}
				return null;
			}
			else {
				return (ISelection) evaluationContext.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			}
		}
		return null;
	}
	
	public static void openEditor(final IWorkbenchPage page, final IFile file, final IRegion initialSelection) {
		final Display display = page.getWorkbenchWindow().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				IMarker marker;
				try { 
					marker = file.createMarker("de.walware.statet.base.markers.InitialSelection"); //$NON-NLS-1$
					if (initialSelection != null) {
						marker.setAttribute(IMarker.CHAR_START, initialSelection.getOffset());
						marker.setAttribute(IMarker.CHAR_END, initialSelection.getOffset() + initialSelection.getLength());
					}
				}
				catch (final CoreException e) {
					marker = null;
				}
				try {
					if (marker != null) {
						IDE.openEditor(page, marker, true);
					}
					else {
						IDE.openEditor(page, file, true);
					}
				}
				catch (final PartInitException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
							NLS.bind("Could not open editor for ''{0}''", file.getName()), e));
				}
				if (marker != null) {
					try {
						marker.delete();
					}
					catch (final CoreException e) {
					}
				}
			}
		});
	}
	
	public static void indicateStatus(final IStatus status, final ExecutionEvent executionEvent) {
		if (status.isOK()) {
			return;
		}
		if (status.getMessage() != null && executionEvent != null) {
			final IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(executionEvent);
			if (workbenchPart != null) {
				final IEditorStatusLine statusLine = (IEditorStatusLine) workbenchPart.getAdapter(IEditorStatusLine.class);
				if (statusLine != null) {
					statusLine.setMessage(status.getSeverity() == IStatus.ERROR, status.getMessage(), null);
				}
			}
		}
		if (status.getSeverity() == IStatus.ERROR) {
			Display.getCurrent().beep();
		}
	}
	
	
	private WorkbenchUIUtil() {}
	
}
