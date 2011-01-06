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

package de.walware.statet.nico.core.runtime;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;


/**
 * Specifies a mapping of a file path from a remote system
 * accessible at the local system
 */
public interface IResourceMapping {
	
	enum Order {
		
		LOCAL,
		REMOTE,
		
	}
	
	/**
	 * Path from viewpoint of the local system
	 * 
	 * @return EFS resource
	 */
	IFileStore getFileStore();
	
	/**
	 * Hostname (name or IP) of the remote system
	 * 
	 * @return the hostname
	 */
	String getHost();
	
	/**
	 * Path from viewpoint of the remote system
	 * 
	 * @return path object
	 */
	IPath getRemotePath();
	
}
