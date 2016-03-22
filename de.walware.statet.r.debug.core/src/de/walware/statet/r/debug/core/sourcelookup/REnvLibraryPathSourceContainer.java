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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.debug.core.sourcelookup.Messages;


public class REnvLibraryPathSourceContainer extends CompositeSourceContainer {
	
	
	public static final String TYPE_ID= "de.walware.statet.r.sourceContainers.REnvLibraryPathType"; //$NON-NLS-1$
	
	
	private final IREnv rEnv;
	
	
	public REnvLibraryPathSourceContainer(final IREnv rEnv) {
		if (rEnv == null) {
			throw new NullPointerException("rEnv"); //$NON-NLS-1$
		}
		this.rEnv= rEnv;
	}
	
	
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	@Override
	public String getName() {
		return NLS.bind(Messages.REnvLibraryPathSourceContainer_name, this.rEnv.getName());
	}
	
	public IREnv getREnv() {
		return this.rEnv;
	}
	
	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		final List<ISourceContainer> list= new ArrayList<>();
		final IREnvConfiguration config= this.rEnv.getConfig();
		if (config == null) {
			abort(Messages.REnvLibraryPathSourceContainer_error_REnvNotAvailable_message, null);
		}
		final List<? extends IRLibraryGroup> libraryGroups= config.getRLibraryGroups();
		for (final IRLibraryGroup group : libraryGroups) {
			final List<? extends IRLibraryLocation> libraries= group.getLibraries();
			for (final IRLibraryLocation lib : libraries) {
				final IFileStore store= lib.getDirectoryStore();
				if (store != null) {
					final RLibrarySourceContainer container= new RLibrarySourceContainer(
							store.toString(), lib.getDirectoryStore() );
					container.init(getDirector());
					list.add(container);
				}
			}
		}
		return list.toArray(new ISourceContainer[list.size()]);
	}
	
	
	@Override
	public int hashCode() {
		return this.rEnv.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof REnvLibraryPathSourceContainer
				&& this.rEnv.equals(obj));
	}
	
}
