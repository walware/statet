/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.REnvUtil;
import de.walware.statet.r.debug.core.sourcelookup.REnvLibraryPathSourceContainer;


public class REnvLibraryPathSourceContainerTypeDelegate extends AbstractSourceContainerTypeDelegate {
	
	
	/** Created via extension point */
	public REnvLibraryPathSourceContainerTypeDelegate() {
	}
	
	
	@Override
	public ISourceContainer createSourceContainer(final String memento)
			throws CoreException {
		final Node node= parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			final Element element= (Element) node;
			if (REnvLibraryPathSourceContainer.TYPE_ID.equals(element.getNodeName())) {
				final String s= element.getAttribute("rEnv"); //$NON-NLS-1$
				if (s == null || s.isEmpty()) {
					abort(Messages.REnvLibraryPathSourceContainer_error_InvalidConfiguration_message, null); 
				}
				final IREnv rEnv= REnvUtil.decode(s, RCore.getREnvManager());
				if (rEnv != null) {
					return new REnvLibraryPathSourceContainer(rEnv);
				}
				abort(Messages.REnvLibraryPathSourceContainer_error_REnvNotAvailable_message, null); 
			}
			abort(Messages.REnvLibraryPathSourceContainer_error_InvalidConfiguration_message, null); 
		}
		abort(Messages.REnvLibraryPathSourceContainer_error_InvalidConfiguration_message, null); 
		return null;
	}
	
	@Override
	public String getMemento(final ISourceContainer container) throws CoreException {
		final REnvLibraryPathSourceContainer rEnvContainer= (REnvLibraryPathSourceContainer) container;
		final Document document= newDocument();
		final Element element= document.createElement(REnvLibraryPathSourceContainer.TYPE_ID);
		element.setAttribute("rEnv", REnvUtil.encode(rEnvContainer.getREnv())); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}
	
}
