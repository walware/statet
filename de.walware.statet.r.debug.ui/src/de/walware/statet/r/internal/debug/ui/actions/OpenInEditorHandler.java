/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRVariable;
import de.walware.statet.r.ui.dataeditor.RDataEditor;
import de.walware.statet.r.ui.dataeditor.RLiveDataEditorInput;


public class OpenInEditorHandler extends AbstractDebugHandler {
	
	
	public OpenInEditorHandler() {
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(evaluationContext);
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(evaluationContext);
		if (part != null && selection != null && !selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				if (structSelection.size() == 1) {
					final Object obj= structSelection.getFirstElement();
					if (obj instanceof IRVariable) {
						final IRVariable rVariable= (IRVariable) obj;
						final IRElementVariable elementVariable= getElementVariable(rVariable);
						if (elementVariable != null) {
							final ICombinedRElement element= elementVariable.getElement();
							setBaseEnabled(element != null
									&& elementVariable.getFQElementName() != null
									&& RLiveDataEditorInput.isSupported(element) );
							return;
						}
					}
				}
			}
		}
		setBaseEnabled(false);
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		if (part instanceof IDebugView && selection != null && !selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				if (structSelection.size() == 1) {
					final Object obj= structSelection.getFirstElement();
					if (obj instanceof IRVariable) {
						final IRVariable rVariable= (IRVariable) obj;
						final IRElementVariable elementVariable= getElementVariable(rVariable);
						if (elementVariable != null) {
							final IRDebugTarget debugTarget= elementVariable.getDebugTarget();
							final RElementName elementName= elementVariable.getFQElementName();
							if (elementName != null) {
								RDataEditor.open(part.getSite().getPage(), debugTarget.getProcess(),
										elementName, getVariableItemIndex(rVariable) );
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
}
