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
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.RDataEditor;
import de.walware.statet.r.ui.dataeditor.RLiveDataEditorInput;


public class OpenInEditorHandler extends AbstractHandler {
	
	
	public OpenInEditorHandler() {
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final IWorkbenchPart activePart= WorkbenchUIUtil.getActivePart(evaluationContext);
		if (activePart instanceof ObjectBrowserView) {
			final ObjectBrowserView browser= (ObjectBrowserView) activePart;
			
			final ToolProcess tool= browser.getTool();
			final ITreeSelection selection= browser.getSelection();
			if (tool != null && !tool.isTerminated()
					&& selection.size() == 1) {
				final ICombinedRElement rElement= ContentProvider.getCombinedRElement(selection.getFirstElement());
				setBaseEnabled(rElement != null
						&& RLiveDataEditorInput.isSupported(rElement) );
				return;
			}
		}
		setBaseEnabled(false);
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart activePart= WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		if (activePart instanceof ObjectBrowserView) {
			final ObjectBrowserView browser= (ObjectBrowserView) activePart;
			
			final ToolProcess tool= browser.getTool();
			final ITreeSelection selection= browser.getSelection();
			if (tool != null && selection.size() == 1) {
				final RElementName elementName= browser.getFQElementName(selection.getPaths()[0]);
				
				RDataEditor.open(browser.getSite().getPage(), tool, elementName, null);
				return null;
			}
		}
		return null;
	}
	
}
