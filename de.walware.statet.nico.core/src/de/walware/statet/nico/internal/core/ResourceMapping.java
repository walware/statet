/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.net.resourcemapping.IResourceMapping;


public final class ResourceMapping implements IResourceMapping {
	
	
	private static String cleanDirectory(final String path) {
		int idx= path.length() - 1;
		while (idx > 0) {
			final char c= path.charAt(idx);
			if (c == '/' || c == '\\') {
				idx--;
			}
			else {
				break;
			}
		}
		return path.substring(0, idx + 1);
	}
	
	
	private String id;
	
	private final String localText;
	private final IFileStore fileStore;
	private final IPath remotePath;
	
	private final String hostName;
	private InetAddress[] hostAddress;
	
	
	/**
	 * 
	 * @param id internal id
	 * @param localPath local path used to create IFileStore
	 * @param hostname hostname
	 * @param remotePath remote path
	 * @throws CoreException if a path is invalid
	 */
	public ResourceMapping(final String id, final String localPath, final String hostname, final String remotePath) throws CoreException {
		this.id= id;
		this.localText= localPath;
		this.fileStore= FileUtil.getFileStore(this.localText);
		this.hostName= hostname;
		this.remotePath= new Path(cleanDirectory(remotePath));
	}
	
	
	public String getId() {
		return this.id;
	}
	
	public void setId(final String id) {
		assert (this.id == null);
		this.id= id;
	}
	
	public InetAddress[] getHostAddresses() {
		return this.hostAddress;
	}
	
	public void resolve() throws UnknownHostException {
		this.hostAddress= InetAddress.getAllByName(this.hostName);
	}
	
	public String getLocalText() {
		return this.localText;
	}
	
	@Override
	public IFileStore getFileStore() {
		return this.fileStore;
	}
	
	@Override
	public String getHost() {
		return this.hostName;
	}
	
	@Override
	public IPath getRemotePath() {
		return this.remotePath;
	}
	
}
