/*=============================================================================#
 # Copyright (c) 2011-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.net.resourcemapping;

import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;


public interface IResourceMappingManager {
	
	
	/**
	 * Returns all configured resource mappings for the given host.
	 * 
	 * @param hostAddress the address of the remote system
	 * @param order the order of the mappings in the returned list
	 * @return a list of resource mappings, an empty list if no mappings exists
	 */
	List<IResourceMapping> getResourceMappingsFor(String hostAddress, ResourceMappingOrder order);
	
	/**
	 * Maps a remote resource path to a local file store.
	 * 
	 * @param hostAddress the address of the remote system the remote resource path belongs to
	 * @param remotePath the remote resource path
	 * @param relativeBasePath optional relative path
	 * @return a file store or <code>null</code> if no mapping was found
	 */
	IFileStore mapRemoteResourceToFileStore(String hostAddress, IPath remotePath, IPath relativeBasePath);
	
	/**
	 * Maps a local file store to a remote resource path.
	 * 
	 * @param hostAddress the address of the remote system the remote resource path shall belong to
	 * @param fileStore the file store
	 * @return a path or <code>null</code> if no mapping was found
	 */
	IPath mapFileStoreToRemoteResource(String hostAddress, IFileStore fileStore);
	
}
