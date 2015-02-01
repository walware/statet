/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.pkgmanager;

import de.walware.rj.renv.IRPkg;

import de.walware.statet.r.core.renv.IRLibraryLocation;


public abstract class RPkgAction {
	
	
	public static final int UNINSTALL=                      1;
	public static final int INSTALL=                        2;
	
	
	public static class Install extends RPkgAction {
		
		
		private final IRPkgData pkg;
		
		private IRLibraryLocation target;
		
		private final IRPkgInfoAndData reference;
		
		
		public Install(final IRPkgData pkg, final IRLibraryLocation target, final IRPkgInfoAndData reference) {
			this.pkg= pkg;
			this.target= target;
			this.reference= reference;
		}
		
		
		@Override
		public int getAction() {
			return INSTALL;
		}
		
		@Override
		public IRLibraryLocation getLibraryLocation() {
			return this.target;
		}
		
		public void setLibraryLocation(final IRLibraryLocation location) {
			this.target= location;
		}
		
		@Override
		public IRPkgData getPkg() {
			return this.pkg;
		}
		
		@Override
		public String getRepoId() {
			return this.pkg.getRepoId();
		}
		
		public IRPkgInfoAndData getReferencePkg() {
			return this.reference;
		}
		
		
		@Override
		public int hashCode() {
			return 9251 + this.pkg.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Install)) {
				return false;
			}
			final Install other= (Install) obj;
			return this.pkg.equals(other.pkg);
		}
		
		
		@Override
		public String toString() {
			return "RPkgAction INSTALL " + getPkg(); //$NON-NLS-1$
		}
		
	}
	
	
	public static class Uninstall extends RPkgAction {
		
		
		private final IRPkgInfo pkg;
		
		
		public Uninstall(final IRPkgInfo pkg) {
			this.pkg= pkg;
		}
		
		
		@Override
		public int getAction() {
			return UNINSTALL;
		}
		
		@Override
		public IRPkg getPkg() {
			return this.pkg;
		}
		
		@Override
		public String getRepoId() {
			return null;
		}
		
		@Override
		public IRLibraryLocation getLibraryLocation() {
			return this.pkg.getLibraryLocation();
		}
		
		
		@Override
		public int hashCode() {
			return 1269275 + this.pkg.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Install)) {
				return false;
			}
			final Install other= (Install) obj;
			return this.pkg.equals(other.pkg);
		}
		
		
		@Override
		public String toString() {
			return "RPkgAction UNINSTALL " + getPkg(); //$NON-NLS-1$
		}
		
	}
	
	
	private RPkgAction() {
	}
	
	
	public abstract int getAction();
	
	public abstract IRPkg getPkg();
	
	public abstract String getRepoId();
	public abstract IRLibraryLocation getLibraryLocation();
	
}
