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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.jcommons.collections.CollectionUtils;

import de.walware.ecommons.ui.util.DNDUtil;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRVariable;


public class CopyQualifiedNameHandler extends AbstractDebugHandler {
	
	
	public CopyQualifiedNameHandler() {
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(evaluationContext);
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(evaluationContext);
		if (part != null && selection != null && !selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				for (final Object obj : structSelection.toList()) {
					if (obj instanceof IRVariable) {
						final IRVariable rVariable= (IRVariable) obj;
						final IRElementVariable elementVariable= getElementVariable(rVariable);
						if (elementVariable != null && elementVariable.getFQElementName() != null) {
							continue;
						}
					}
					setBaseEnabled(false);
					return;
				}
				setBaseEnabled(true);
				return;
			}
		}
		setBaseEnabled(false);
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		if (part != null && selection != null && !selection.isEmpty()) {
			final List<String> names= new ArrayList<>();
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				for (final Object obj : structSelection.toList()) {
					final String name= getName(obj);
					if (name != null) {
						names.add(name);
					}
				}
			}
			
			if (names.size() == 1) {
				copy(names.get(0), part);
			}
			else if (names.size() > 1) {
				copy(CollectionUtils.toString(names, ", "), part); //$NON-NLS-1$
			}
			else {
				copy("", part); //$NON-NLS-1$
			}
		}
		return null;
	}
	
	private String getName(final Object obj) {
		if (obj instanceof IRVariable) {
			final IRVariable rVariable= (IRVariable) obj;
			final IRElementVariable elementVariable= getElementVariable(rVariable);
			if (elementVariable != null) {
				final RElementName elementName= elementVariable.getFQElementName();
				if (elementName != null) {
					return addIndex(
							elementName.getDisplayName(RElementName.DISPLAY_EXACT | RElementName.DISPLAY_FQN),
							getVariableItemIndex(rVariable) );
				}
			}
		}
		return null;
	}
	
	private void copy(final String text, final IWorkbenchPart part) {
		final Clipboard clipboard= new Clipboard(part.getSite().getShell().getDisplay());
		try {
			DNDUtil.setContent(clipboard,
					new String[] { text },
					new Transfer[] { TextTransfer.getInstance() } );
		}
		finally {
			clipboard.dispose();
		}
	}
	
}
