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

package de.walware.statet.r.debug.core.sourcelookup;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.io.FileUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.sourcelookup.Messages;


public class RLibrarySourceContainer extends AbstractSourceContainer {
	
	
	public static final String TYPE_ID = "de.walware.statet.r.sourceContainers.RLibraryType"; //$NON-NLS-1$
	
	
	private static IFileStore resolve(final String path) {
		if (path != null) {
			try {
				return FileUtil.expandToLocalFileStore(path, null, null);
			}
			catch (final Exception e) {
				RDebugCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID, 0,
						NLS.bind("Could not resolve configured R library path ''{0}}'' of " +
								"a source lookup entry.", path ), e));
			}
		}
		return null;
	}
	
	
	private final String fLocationPath;
	private final IFileStore fLocationStore;
	
	
	public RLibrarySourceContainer(final String locationPath) {
		this(locationPath, resolve(locationPath));
	}
	
	public RLibrarySourceContainer(final String locationPath, final IFileStore locationStore) {
		if (locationPath == null) {
			throw new NullPointerException("location"); //$NON-NLS-1$
		}
		fLocationPath = locationPath;
		fLocationStore = locationStore;
	}
	
	
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	@Override
	public String getName() {
		final String s = fLocationPath.toString();
		if (fLocationStore == null) {
			return s + Messages.RLibrarySourceContainer_name_UnresolvablePath_message;
		}
		return s;
	}
	
	/**
	 * @return the location
	 */
	public String getLocationPath() {
		return fLocationPath;
	}
	
	/**
	 * @return the store, if resolved
	 */
	public IFileStore getLocationStore() {
		return fLocationStore;
	}
	
	@Override
	public Object[] findSourceElements(final String name) throws CoreException {
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return fLocationPath.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof RLibrarySourceContainer
				&& fLocationPath.equals(((RLibrarySourceContainer) obj).fLocationPath) );
	}
	
}
