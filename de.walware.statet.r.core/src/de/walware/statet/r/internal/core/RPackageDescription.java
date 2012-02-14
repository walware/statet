/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import de.walware.statet.r.core.renv.IRPackageDescription;


public class RPackageDescription implements IRPackageDescription {
	
	
	private final String fName;
	private final String fTitle;
	private final String fDescription;
	private final String fVersion;
	private final String fPriority;
	private final String fAuthor;
	private final String fMaintainer;
	private final String fUrl;
	
	
	public RPackageDescription(final String name, final String title,
			final String desription, final String version, final String priority,
			final String author, final String maintainer, final String url) {
		fName = name.intern();
		fTitle = title;
		fDescription = desription;
		fVersion = version;
		fPriority = (priority != null) ? priority.intern() : null;
		fAuthor = author;
		fMaintainer = maintainer;
		fUrl = url;
	}
	
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public String getTitle() {
		return fTitle;
	}
	
	@Override
	public String getDescription() {
		return fDescription;
	}
	
	@Override
	public String getPriority() {
		return fPriority;
	}
	
	@Override
	public String getVersion() {
		return fVersion;
	}
	
	@Override
	public String getAuthor() {
		return fAuthor;
	}
	
	@Override
	public String getMaintainer() {
		return fMaintainer;
	}
	
	@Override
	public String getUrl() {
		return fUrl;
	}
	
	
	@Override
	public int hashCode() {
		return fName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RPackageDescription)) {
			return false;
		}
		final IRPackageDescription other = (IRPackageDescription) obj;
		return (this == other || (
				fName == other.getName() && fVersion.equals(other.getVersion()) ));
	}
	
	@Override
	public String toString() {
		return fName + " [Version: " + fVersion + "]";
	}
	
}
