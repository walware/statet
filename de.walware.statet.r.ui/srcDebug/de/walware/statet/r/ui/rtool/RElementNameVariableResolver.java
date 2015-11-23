/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.rtool;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.ui.IElementNameProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.LTKSelectionUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.ui.rtools.Messages;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.IRSourceEditor;
import de.walware.statet.r.ui.sourceediting.RAssistInvocationContext;


public class RElementNameVariableResolver implements IDynamicVariableResolver {
	
	
	public static final String R_OBJECT_NAME_NAME = "r_object_name"; //$NON-NLS-1$
	
	
	public RElementNameVariableResolver() {
	}
	
	
	@Override
	public String resolveValue(final IDynamicVariable variable, final String argument)
			throws CoreException {
		final IWorkbenchPart part = UIAccess.getActiveWorkbenchPart(false);
		if (part != null) {
			final IWorkbenchPartSite site = part.getSite();
			final ISelectionProvider selectionProvider = site.getSelectionProvider();
			if (selectionProvider != null) {
				final ISelection selection = selectionProvider.getSelection();
				
				if (selection instanceof IStructuredSelection) {
					final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					if (selection.isEmpty()) {
						throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
								NLS.bind(Messages.Variable_error_EmptySelection_message,
										R_OBJECT_NAME_NAME )));
					}
					if (structuredSelection.size() == 1) {
						final Object element = structuredSelection.getFirstElement();
						if (element instanceof IRElement) { 
							final IElementName elementName;
							if (selection instanceof IElementNameProvider) {
								elementName = ((IElementNameProvider) selection).getElementName(
										(selection instanceof ITreeSelection) ?
												((ITreeSelection) selection).getPaths()[0] :
												element );
							}
							else {
								elementName = ((IRElement) element).getElementName();
							}
							return checkName(elementName);
						}
					}
					final IModelElement[] elements = LTKSelectionUtil.getSelectedElements(
							(IStructuredSelection) selection );
					if (elements != null && elements.length == 1
							&& elements[0].getModelTypeId() == RModel.TYPE_ID) {
						return checkName(elements[0].getElementName());
					}
					throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
							NLS.bind(Messages.Variable_error_NoSingleRElement_message,
									R_OBJECT_NAME_NAME )));
				}
			}
			
			final ISourceEditor editor = (ISourceEditor) part.getAdapter(ISourceEditor.class);
			if (editor instanceof IRSourceEditor) {
				final Point range = editor.getViewer().getSelectedRange();
				final RAssistInvocationContext context = new RAssistInvocationContext(editor,
						new Region(range.x, range.y), null);
				return checkName(context.getNameSelection());
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
				NLS.bind(Messages.Variable_error_NoSingleRElement_message,
						R_OBJECT_NAME_NAME )));
	}
	
	private String checkName(final IElementName elementName) throws CoreException {
		if (elementName instanceof RElementName) {
			final String name = ((RElementName) elementName).getDisplayName(
					(RElementName.DISPLAY_FQN | RElementName.DISPLAY_EXACT) );
			if (name != null) {
				return name;
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
				NLS.bind(Messages.Variable_error_InvalidObject_QualifiedName_message,
						R_OBJECT_NAME_NAME )));
	}
	
}
