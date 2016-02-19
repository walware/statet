/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.sourcelookup;

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.swt.widgets.Shell;

import de.walware.statet.r.debug.core.sourcelookup.AllRProjectsSourceContainer;


public class AllRProjectsSourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	
	/** Created via extension point */
	public AllRProjectsSourceContainerBrowser() {
	}
	
	
	@Override
	public boolean canAddSourceContainers(final ISourceLookupDirector director) {
		final ISourceContainer[] containers= director.getSourceContainers();
		for (final ISourceContainer container : containers) {
			if (container.getType().getId().equals(AllRProjectsSourceContainer.TYPE_ID)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public ISourceContainer[] addSourceContainers(final Shell shell,
			final ISourceLookupDirector director) {
		final AllRProjectsSourceContainer container= new AllRProjectsSourceContainer();
		container.init(director);
		return new ISourceContainer[] { container };
	}
	
}
