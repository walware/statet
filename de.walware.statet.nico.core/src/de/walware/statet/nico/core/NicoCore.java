/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.core.runtime.IResourceMapping;
import de.walware.statet.nico.internal.core.NicoPlugin;


/**
 * 
 */
public class NicoCore {
	
	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.nico.core"; //$NON-NLS-1$
	
	public static final int STATUS_CATEGORY = (3 << 16);
	
	/** Status Code for errors when handle Threads/Runnables */
	public static final int STATUSCODE_RUNTIME_ERROR = STATUS_CATEGORY | (2 << 8);
	
	public static final int EXITVALUE_CORE_EXCEPTION = STATUSCODE_RUNTIME_ERROR | 1;
	public static final int EXITVALUE_RUNTIME_EXCEPTION = STATUSCODE_RUNTIME_ERROR | 2;
	
	
	private static IPreferenceAccess CONSOLE_PREFS = PreferencesUtil.createAccess(new IScopeContext[] {
			new ConsoleInstanceScope(), new ConsoleDefaultScope(), new InstanceScope(), new DefaultScope() });
	
	/**
	 * The instance preferences for consoles with the scope search path:
	 * <ol>
	 *     <li>ConsoleInstance</li>
	 *     <li>ConsoleDefault</li>
	 *     <li>Instance</li>
	 *     <li>Default</li>
	 * @return shared preference access to the preferences
	 */
	public static IPreferenceAccess getInstanceConsolePreferences() {
		return CONSOLE_PREFS;
	}
	
	public static void addToolLifeListener(final IToolLifeListener listener) {
		NicoPlugin.getDefault().getToolLifecycle().addToolLifeListener(listener);
	}
	
	public static void removeToolLifeListener(final IToolLifeListener listener) {
		NicoPlugin.getDefault().getToolLifecycle().removeToolLifeListener(listener);
	}
	
	/**
	 * Returns all configured resource mappings for the given host
	 * 
	 * @param hostAddress
	 * @return list of resource mappings
	 */
	public static Set<IResourceMapping> getResourceMappingsFor(final String hostAddress) {
		final Set<IResourceMapping> mappings = NicoPlugin.getDefault().getMappingManager().getMappingsFor(hostAddress);
		if (mappings != null) {
			return Collections.unmodifiableSet(mappings);
		}
		return Collections.EMPTY_SET;
	}
	
	/**
	 * Maps a remote resource path to local file store
	 * 
	 * @param hostAddress the remote resource path belongs to
	 * @param remotePath the remote path
	 * @param relativeBasePath optional relative path
	 * @return a file store or <code>null</code> if no mapping was found
	 */
	public static IFileStore mapRemoteResourceToFileStore(final String hostAddress, IPath remotePath, final IPath relativeBasePath) {
		if (!remotePath.isAbsolute()) {
			if (relativeBasePath == null) {
				return null;
			}
			remotePath = relativeBasePath.append(remotePath);
		}
		final Set<IResourceMapping> mappings = NicoCore.getResourceMappingsFor(hostAddress);
		for (final IResourceMapping mapping : mappings) {
			final IPath remoteBase = mapping.getRemotePath();
			if (remoteBase.isPrefixOf(remotePath)) {
				final IPath subPath = remotePath.removeFirstSegments(remoteBase.segmentCount());
				final IFileStore localBaseStore = mapping.getFileStore();
				return localBaseStore.getFileStore(subPath);
			}
		}
		return null;
	}
	
	/**
	 * Maps a local file store to a remote resource path
	 * 
	 * @param hostAddress the remote resource path belongs to
	 * @param fileStore the file store
	 * @return a path or <code>null</code> if no mapping was found
	 */
	public static IPath mapFileStoreToRemoteResource(final String hostAddress, final IFileStore fileStore) {
		final Set<IResourceMapping> mappings = NicoCore.getResourceMappingsFor(hostAddress);
		for (final IResourceMapping mapping : mappings) {
			final IFileStore localBaseStore = mapping.getFileStore();
			if (localBaseStore.equals(fileStore)) {
				return mapping.getRemotePath();
			}
			if (localBaseStore.isParentOf(fileStore)) {
				final IPath localBasePath = new Path(localBaseStore.toURI().getPath());
				final IPath fileStorePath = new Path(fileStore.toURI().getPath());
				if (localBasePath.isPrefixOf(fileStorePath)) {
					final IPath subPath = fileStorePath.removeFirstSegments(localBasePath.segmentCount());
					final IPath remotePath = mapping.getRemotePath();
					return remotePath.append(subPath);
				}
			}
		}
		return null;
	}
	
	
	private NicoCore() {}
	
}
