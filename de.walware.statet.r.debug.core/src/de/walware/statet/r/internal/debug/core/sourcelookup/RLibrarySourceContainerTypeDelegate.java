/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.statet.r.debug.core.sourcelookup.RLibrarySourceContainer;


public class RLibrarySourceContainerTypeDelegate extends AbstractSourceContainerTypeDelegate {
	
	
	/** Created via extension point */
	public RLibrarySourceContainerTypeDelegate() {
	}
	
	
	@Override
	public ISourceContainer createSourceContainer(final String memento)
			throws CoreException {
		final Node node= parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			final Element element= (Element) node;
			if (RLibrarySourceContainer.TYPE_ID.equals(element.getNodeName())) {
				final String path= element.getAttribute("path"); //$NON-NLS-1$
				if (path == null || path.isEmpty()) {
					abort(Messages.RLibrarySourceContainer_error_InvalidConfiguration_message, null); 
				}
				return new RLibrarySourceContainer(path);
			}
			abort(Messages.RLibrarySourceContainer_error_InvalidConfiguration_message, null); 
		}
		abort(Messages.RLibrarySourceContainer_error_InvalidConfiguration_message, null); 
		return null;
	}
	
	@Override
	public String getMemento(final ISourceContainer container) throws CoreException {
		final RLibrarySourceContainer rLibraryContainer= (RLibrarySourceContainer) container;
		final Document document= newDocument();
		final Element element= document.createElement(RLibrarySourceContainer.TYPE_ID);
		element.setAttribute("path", rLibraryContainer.getLocationPath()); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}
	
}
