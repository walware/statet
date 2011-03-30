/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.net.resourcemapping.IResourceMapping;


public class ResourceMapping implements IResourceMapping {
	
	
	private static String cleanDirectory(final String path) {
		int idx = path.length() - 1;
		while (idx > 0) {
			final char c = path.charAt(idx);
			if (c == '/' || c == '\\') {
				idx--;
			}
			else {
				break;
			}
		}
		return path.substring(0, idx + 1);
	}
	
	
	private String fId;
	
	private final String fLocalText;
	private final IFileStore fFileStore;
	private final IPath fRemotePath;
	
	private final String fHostName;
	private InetAddress[] fHostAddress;
	
	
	/**
	 * 
	 * @param id internal id
	 * @param localPath local path used to create IFileStore
	 * @param hostname hostname
	 * @param remotePath remote path
	 * @throws CoreException if a path is invalid
	 */
	public ResourceMapping(final String id, final String localPath, final String hostname, final String remotePath) throws CoreException {
		fId = id;
		fLocalText = localPath;
		fFileStore = FileUtil.getFileStore(fLocalText);
		fHostName = hostname;
		fRemotePath = new Path(cleanDirectory(remotePath));
	}
	
	
	public String getId() {
		return fId;
	}
	
	public void setId(final String id) {
		assert (fId == null);
		fId = id;
	}
	
	public InetAddress[] getHostAddresses() {
		return fHostAddress;
	}
	
	public void resolve() throws UnknownHostException {
		fHostAddress = InetAddress.getAllByName(fHostName);
	}
	
	public String getLocalText() {
		return fLocalText;
	}
	
	public IFileStore getFileStore() {
		return fFileStore;
	}
	
	public String getHost() {
		return fHostName;
	}
	
	public IPath getRemotePath() {
		return fRemotePath;
	}
	
}
