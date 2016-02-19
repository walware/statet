/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.rtool;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.ui.editors.text.IEncodingSupport;

import de.walware.ecommons.debug.core.variables.ResourceVariableResolver;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.ResourceVariableUtil;


public class RResourceEncodingVariableResolver extends ResourceVariableResolver {
	
	
	public RResourceEncodingVariableResolver(final ResourceVariableUtil util) {
		super(util);
	}
	
	
	@Override
	public String resolveValue(final IDynamicVariable variable, final String argument) throws CoreException {
		final String encoding = getEncoding(variable, getResource(variable, RESOURCE, argument));
		return (encoding != null) ? encoding : "unknown";
	}
	
	
	protected String getEncoding(final IDynamicVariable variable, final IResource resource)
			throws CoreException {
		final IResolveContext context= getContext();
		final IAdaptable adaptable= (context instanceof ResourceVariableUtil) ?
				((ResourceVariableUtil) context).getWorkbenchPart() :
				UIAccess.getActiveWorkbenchPart(false);
		if (adaptable != null) {
			final IEncodingSupport encodingSupport = (IEncodingSupport) adaptable
					.getAdapter(IEncodingSupport.class);
			if (encodingSupport != null) {
				return encodingSupport.getEncoding();
			}
		}
		if (resource instanceof IFile) {
			return ((IFile) resource).getCharset(true); 
		}
		if (resource instanceof IContainer) {
			return ((IContainer) resource).getDefaultCharset(true);
		}
		return null;
	}
	
}
