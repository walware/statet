/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
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
	
	
	public static final int UNINSTALL = 1;
	public static final int INSTALL = 2;
	
	
	public static class Install extends RPkgAction {
		
		
		private final IRPkgData fPkg;
		
		private IRLibraryLocation fTarget;
		
		private final IRPkgDescription fReference;
		
		
		public Install(final IRPkgData pkg, final IRLibraryLocation target, final IRPkgDescription reference) {
			fPkg = pkg;
			fTarget = target;
			fReference = reference;
		}
		
		
		@Override
		public int getAction() {
			return INSTALL;
		}
		
		@Override
		public IRLibraryLocation getLibraryLocation() {
			return fTarget;
		}
		
		public void setLibraryLocation(final IRLibraryLocation location) {
			fTarget = location;
		}
		
		@Override
		public IRPkgData getPkg() {
			return fPkg;
		}
		
		@Override
		public String getRepoId() {
			return fPkg.getRepoId();
		}
		
		public IRPkgDescription getReferencePkg() {
			return fReference;
		}
		
	}
	
	
	public static class Uninstall extends RPkgAction {
		
		
		private final IRPkgInfo fPkg;
		
		
		public Uninstall(final IRPkgInfo pkg) {
			fPkg = pkg;
		}
		
		
		@Override
		public int getAction() {
			return UNINSTALL;
		}
		
		@Override
		public IRPkg getPkg() {
			return fPkg;
		}
		
		@Override
		public String getRepoId() {
			return null;
		}
		
		@Override
		public IRLibraryLocation getLibraryLocation() {
			return fPkg.getLibraryLocation();
		}
		
	}
	
	
	private RPkgAction() {
	}
	
	
	public abstract int getAction();
	
	public abstract IRPkg getPkg();
	
	public abstract String getRepoId();
	public abstract IRLibraryLocation getLibraryLocation();
	
}
