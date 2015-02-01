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

package de.walware.statet.nico.core.runtime;

import org.eclipse.core.filesystem.IFileStore;


/**
 * A tool session track.
 */
public interface ITrack {
	
	
	/**
	 * A name of the track (usually the name from the configuration).
	 * For the identification by the user.
	 * 
	 * @return the name
	 */
	String getName();
	
	/**
	 * Flushes the buffer of the track, if available.
	 */
	void flush();
	
	/**
	 * Returns the file the track is written to (if configured).
	 * 
	 * @return the current file or <code>null</code>
	 */
	IFileStore getFile();
	
}
