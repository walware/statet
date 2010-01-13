/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/RJ-Project (www.walware.de/opensource).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.services;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.osgi.framework.Version;


/**
 * Information about the platform R is running on
 * and the running R version.
 * 
 * The properties usually doesn't change for a single RService
 * instance.
 */
public final class RPlatform implements Externalizable {
	
	
	public static final String OS_WINDOWS = "windows";
	public static final String OS_UNIX = "unix";
	
	
	private String osType;
	
	private String fileSep;
	private String pathSep;
	
	private String versionString;
	private transient Version version;
	
	
	public RPlatform() {
	}
	
	public RPlatform(final String osType, final String fileSep, final String pathSep, String version) {
		this.osType = osType;
		this.fileSep = fileSep;
		this.pathSep = pathSep;
		this.versionString = version;
	}
	
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.osType = in.readUTF();
		this.fileSep = in.readUTF();
		this.pathSep = in.readUTF();
		this.versionString = in.readUTF();
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(this.osType);
		out.writeUTF(this.fileSep);
		out.writeUTF(this.pathSep);
		out.writeUTF(this.versionString);
	}
	
	
	public String getOsType() {
		return this.osType;
	}
	
	public String getFileSep() {
		return this.fileSep;
	}
	
	public String getPathSep() {
		return this.pathSep;
	}
	
	public Version getRVersion() {
		if (this.version == null) {
			this.version = new Version(this.versionString);
		}
		return this.version;
	}
	
}
