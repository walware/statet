/*******************************************************************************
 * Copyright (c) 2009-2013 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.objectbrowser;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;

import de.walware.ecommons.models.core.util.IElementPartition;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.rj.data.RObject;

import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;


class PrintHandler extends AbstractHandler {
	
	
	private final ObjectBrowserView view;
	
	
	public PrintHandler(final ObjectBrowserView objectBrowserView) {
		this.view = objectBrowserView;
	}
	
	
	private boolean isValidSelection(ITreeSelection selection) {
		if (selection == null || selection.size() != 1) {
			return false;
		}
		Object element = selection.getFirstElement();
		if (element instanceof IElementPartition) {
			final ICombinedRElement rElement = ContentProvider.getCombinedRElement(element);
			if (rElement == null || rElement.getRObjectType() != RObject.TYPE_LIST) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final ToolProcess process = this.view.getTool();
		setBaseEnabled(process != null && !process.isTerminated()
				&& isValidSelection(this.view.getSelection()) );
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (!UIAccess.isOkToUse(this.view.getViewer())) {
			return null;
		}
		final ITreeSelection selection = this.view.getSelection();
		if (!isValidSelection(selection)) {
			return null;
		}
		final TreePath treePath = selection.getPaths()[0];
		final RElementName elementName = this.view.getElementName(treePath);
		if (elementName != null) {
			String cmd = RElementName.createDisplayName(elementName, RElementName.DISPLAY_NS_PREFIX | RElementName.DISPLAY_EXACT);
			if (treePath.getLastSegment() instanceof IElementPartition) {
				IElementPartition partition = (IElementPartition) treePath.getLastSegment();
				cmd = cmd + '[' + (partition.getPartitionStart() + 1) + ':' + (partition.getPartitionStart() + partition.getPartitionLength()) + ']';
			}
			
			final ToolProcess process = this.view.getTool();
			try {
				final ToolController controller = NicoUITools.accessController(RConsoleTool.TYPE, process);
				controller.submit(cmd, SubmitType.TOOLS);
			}
			catch (final CoreException e) {
			}
		}
		
		return null;
	}
	
}
