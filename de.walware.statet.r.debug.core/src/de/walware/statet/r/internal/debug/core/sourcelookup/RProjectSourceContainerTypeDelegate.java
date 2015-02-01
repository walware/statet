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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.walware.statet.r.debug.core.sourcelookup.RProjectSourceContainer;


public class RProjectSourceContainerTypeDelegate extends AbstractSourceContainerTypeDelegate {
	
	
	/** Created via extension point */
	public RProjectSourceContainerTypeDelegate() {
	}
	
	
	@Override
	public ISourceContainer createSourceContainer(final String memento)
			throws CoreException {
		final Node node= parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			final Element element= (Element) node;
			if (RProjectSourceContainer.TYPE_ID.equals(element.getNodeName())) {
				final IProject project;
				{	final String s= element.getAttribute("name"); //$NON-NLS-1$
					if (s == null || s.isEmpty()) {
						abort(Messages.RProjectSourceContainer_error_InvalidConfiguration_message, null); 
					}
					project= ResourcesPlugin.getWorkspace().getRoot().getProject(s);
				}
				return new RProjectSourceContainer(project, false);
			}
			abort(Messages.RProjectSourceContainer_error_InvalidConfiguration_message, null); 
		}
		abort(Messages.RProjectSourceContainer_error_InvalidConfiguration_message, null); 
		return null;
	}
	
	@Override
	public String getMemento(final ISourceContainer container) throws CoreException {
		final RProjectSourceContainer rProjectContainer= (RProjectSourceContainer) container;
		final Document document= newDocument();
		final Element element= document.createElement(RProjectSourceContainer.TYPE_ID);
		element.setAttribute("name", rProjectContainer.getProject().getName()); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}
	
}
