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

package de.walware.statet.r.debug.core.sourcelookup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;

import de.walware.statet.r.internal.debug.core.sourcelookup.RSourceLookupParticipant;


public class RSourceLookupDirector extends AbstractSourceLookupDirector {
	
	
	private static final Set<String> gFilteredSourceContainerTypes;
	static {
		gFilteredSourceContainerTypes= new HashSet<>();
	}
	
	
	@Override
	public void initializeDefaults(final ILaunchConfiguration configuration) throws CoreException {
		dispose();
		setLaunchConfiguration(configuration);
		setSourceContainers(new ISourceContainer[] {
				new AllRProjectsSourceContainer(),
				new DefaultSourceContainer(),
		});
		initializeParticipants();
	}
	
	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {
				new RSourceLookupParticipant(),
		});
	}
	
	@Override
	public boolean supportsSourceContainerType(final ISourceContainerType type) {
		return !gFilteredSourceContainerTypes.contains(type.getId());
	}
	
	@Override
	protected void cacheResolvedElement(final List duplicates, final Object sourceElement) {
		if (sourceElement instanceof IRSourceLookupMatch) {
			((IRSourceLookupMatch) sourceElement).select();
		}
		super.cacheResolvedElement(duplicates, sourceElement);
	}
	
}
