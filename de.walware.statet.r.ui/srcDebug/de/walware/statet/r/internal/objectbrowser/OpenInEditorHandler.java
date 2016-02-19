/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.objectbrowser;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.dataeditor.RLiveDataEditorInput;


public class OpenInEditorHandler extends AbstractHandler {
	
	
	public OpenInEditorHandler() {
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final IWorkbenchPart activePart = WorkbenchUIUtil.getActivePart(evaluationContext);
		if (activePart instanceof ObjectBrowserView) {
			final ObjectBrowserView browser = (ObjectBrowserView) activePart;
			
			final ToolProcess tool = browser.getTool();
			final ITreeSelection selection;
			if (tool != null && !tool.isTerminated()
					&& (selection = browser.getSelection()).size() == 1) {
				final ICombinedRElement rElement = ContentProvider.getCombinedRElement(selection.getFirstElement());
				setBaseEnabled(rElement != null
						&& RLiveDataEditorInput.isSupported(rElement) );
				return;
			}
		}
		setBaseEnabled(false);
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart activePart = WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		if (activePart instanceof ObjectBrowserView) {
			final ObjectBrowserView browser = (ObjectBrowserView) activePart;
			
			final ToolProcess tool = browser.getTool();
			final ITreeSelection selection = browser.getSelection();
			if (tool == null || selection == null || selection.size() != 1) {
				return null;
			}
			final RElementName elementName = browser.getElementName(selection.getPaths()[0]);
			
			try {
				IDE.openEditor(browser.getSite().getPage(), new RLiveDataEditorInput(tool, elementName),
						"de.walware.statet.r.editors.RData", true); //$NON-NLS-1$
			}
			catch (final PartInitException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
						"Failed to open the selected element in the data viewer."));
			}
		}
		return null;
	}
	
}
