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

package de.walware.statet.r.internal.core.pkgmanager;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.renv.RPkgType;

import de.walware.statet.r.core.RCore;


final class Cache {
	
	private static final String BAK_POSTFIX = "-bak"; //$NON-NLS-1$
	
	private final IFileStore fBinDir;
	private final IFileStore fSrcDir;
	
	
	public Cache(final IFileStore parent) {
		fBinDir = parent.getChild("pkg-bin"); //$NON-NLS-1$
		fSrcDir = parent.getChild("pkg-src"); //$NON-NLS-1$
	}
	
	
	private void checkDir(final IProgressMonitor monitor) throws CoreException {
		if (!fBinDir.fetchInfo().exists()) {
			fBinDir.mkdir(EFS.NONE, monitor);
		}
		if (!fSrcDir.fetchInfo().exists()) {
			fSrcDir.mkdir(EFS.NONE, monitor);
		}
	}
	
	public void add(final String pkgName, final RPkgType type,
			final IFileStore store, final IProgressMonitor monitor) throws CoreException {
		checkDir(monitor);
		
		final IFileStore dir = (type == RPkgType.SOURCE) ? fSrcDir : fBinDir;
		final String[] names = dir.childNames(EFS.NONE, monitor);
		final String prefix = pkgName + '_';
		String oldName = null;
		for (final String name : names) {
			if (name.startsWith(prefix)) {
				if (name.endsWith(BAK_POSTFIX) || oldName != null) {
					removeBak(dir.getChild(name), monitor);
				}
				else {
					oldName = name;
				}
			}
		}
		if (oldName != null) {
			makeBak(dir.getChild(oldName), monitor);
		}
		store.copy(dir.getChild(store.getName()), EFS.NONE, monitor);
	}
	
	private void removeBak(final IFileStore file, final IProgressMonitor monitor) throws CoreException {
		file.delete(EFS.NONE, monitor);
	}
	
	private void makeBak(final IFileStore file, final IProgressMonitor monitor) throws CoreException {
		file.move(file.getParent().getChild(file.getName() + BAK_POSTFIX), EFS.OVERWRITE, monitor);
	}
	
	public IFileStore get(final String pkgName, final RPkgType type,
			final IProgressMonitor monitor) throws CoreException {
		final IFileStore dir = (type == RPkgType.SOURCE) ? fSrcDir : fBinDir;
		final String[] names = dir.childNames(EFS.NONE, monitor);
		final String prefix = pkgName + '_';
		for (final String name : names) {
			if (name.startsWith(prefix)) {
				if (!name.endsWith(BAK_POSTFIX)) {
					return dir.getChild(name);
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
				"R package '" + pkgName + "' not available in local package cache.", null ));
	}
	
}
