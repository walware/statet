/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.UnexpectedRDataException;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRLibPaths.Entry;
import de.walware.statet.r.core.pkgmanager.IRPkgInfoAndData;
import de.walware.statet.r.core.pkgmanager.ISelectedRepos;
import de.walware.statet.r.core.pkgmanager.RPkgAction;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.tool.IRConsoleService;


public class RPkgOperator {
	
	
	private final RPkgManager fManager;
	private final IREnvConfiguration fEnvConfig;
	private final ISelectedRepos fRepos;
	
	private IRConsoleService fR;
	
	private String fTempDir;
	private Set<String> fCreatedDirs;
	
	
	public RPkgOperator(final RPkgManager manager, final IREnvConfiguration config,
			final ISelectedRepos repos) {
		fManager = manager;
		fEnvConfig = config;
		fRepos = repos;
	}
	
	public RPkgOperator(final RPkgManager manager) {
		this(manager, manager.getREnv().getConfig(), manager.getSelectedRepos());
	}
	
	
	private String requireTempDir(final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		if (fTempDir == null) {
			fTempDir = RDataUtil.checkSingleCharValue(fR.evalData("tempdir()", monitor)); //$NON-NLS-1$
		}
		return fTempDir;
	}
	
	private void requireLibDir(final String dir,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		if (fCreatedDirs == null) {
			fCreatedDirs= new HashSet<>();
		}
		if (fCreatedDirs.contains(dir)) {
			return;
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		sb.append("dir.create("); //$NON-NLS-1$
		sb.append('"').append(RUtil.escapeCompletely(dir)).append('"');
		sb.append(", showWarnings= FALSE"); //$NON-NLS-1$
		sb.append(", recursive= TRUE"); //$NON-NLS-1$
		sb.append(')');
		fR.evalVoid(sb.toString(), monitor);
		
		sb.setLength(0);
		sb.append("file.access("); //$NON-NLS-1$
		sb.append('"').append(RUtil.escapeCompletely(dir)).append('"');
		sb.append(", 3L"); //$NON-NLS-1$
		sb.append(')');
		if (RDataUtil.checkSingleIntValue(fR.evalData(sb.toString(), monitor)) != 0) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
					NLS.bind("An error occurred when creating the R library location ''{0}''.", dir), null ));
		}
		
		sb.setLength(0);
		sb.append(".libPaths("); //$NON-NLS-1$
		sb.append("c("); //$NON-NLS-1$
		sb.append('"').append(RUtil.escapeCompletely(dir)).append('"');
		sb.append(", .libPaths())"); //$NON-NLS-1$
		sb.append(')');
		fR.evalVoid(sb.toString(), monitor);
		
		fCreatedDirs.add(dir);
	}
	
	void runActions(final List<? extends RPkgAction> actions,
			final IRConsoleService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final SubMonitor progress = SubMonitor.convert(monitor, actions.size());
		fR = r;
		try {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < actions.size(); i++) {
				final RPkgAction action = actions.get(i);
				if (action.getAction() == RPkgAction.INSTALL) {
					installPkg((RPkgAction.Install) action, sb, progress.newChild(1));
				}
				else if (action.getAction() == RPkgAction.UNINSTALL) {
					uninstallPkg((RPkgAction.Uninstall) action, sb, progress.newChild(1));
				}
				progress.setWorkRemaining(actions.size() - (i+1));
			}
		}
		finally {
			fR = null;
		}
	}
	
	private void installPkg(final RPkgAction.Install action, final StringBuilder sb,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String name = action.getPkg().getName();
		monitor.subTask(NLS.bind("Installing R package ''{0}''...", name));
		
		sb.setLength(0);
		sb.append("install.packages("); //$NON-NLS-1$
		final RRepo repo = fRepos.getRepo(action.getRepoId());
		if (repo == null) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					NLS.bind("Repository ({0}) not found.", action.getRepoId()) ));
		}
		if (repo.getId().startsWith(RRepo.WS_CACHE_PREFIX)) {
			final IFileStore store = fManager.getCache().get(name, repo.getPkgType(),
					monitor );
			if (RPkgUtil.checkPkgType(store.getName(), fManager.getRPlatform()) != repo.getPkgType()) {
				throw new IllegalStateException();
			}
			String fileName;
			if (fEnvConfig.isRemote()) {
				final InputStream in = store.openInputStream(EFS.NONE, monitor);
				try {
					fileName = requireTempDir(monitor) + fR.getPlatform().getFileSep() + store.getName();
					fR.uploadFile(in, store.fetchInfo().getLength(), fileName, 0, monitor);
				}
				finally {
					try {
						in.close();
					}
					catch (final IOException e) {}
				}
			}
			else {
				fileName = store.toLocalFile(EFS.NONE, monitor).getPath();
			}
			sb.append('"').append(RUtil.escapeCompletely(fileName)).append('"');
			sb.append(", repos= NULL"); //$NON-NLS-1$
		}
		else {
			sb.append('"').append(name).append('"');
			{	sb.append(", repos= "); //$NON-NLS-1$
				sb.append('"').append(RUtil.escapeCompletely(repo.getURL())).append('"');
			}
		}
		if (repo.getPkgType() != null) {
			sb.append(", type= "); //$NON-NLS-1$
			sb.append('"').append(RPkgUtil.getPkgTypeInstallKey(fR.getPlatform(), repo.getPkgType())).append('"');
		}
		{	sb.append(", lib= "); //$NON-NLS-1$
			final Entry entry = fManager.getRLibPaths().getEntryByLocation(
					action.getLibraryLocation() );
			if ((entry.getAccess() & IRLibPaths.WRITABLE) == 0) {
				return;
			}
			if ((entry.getAccess() & IRLibPaths.EXISTS) == 0) {
				requireLibDir(entry.getRPath(), monitor);
			}
			sb.append('"').append(RUtil.escapeCompletely(entry.getRPath())).append('"');
		}
		sb.append(", dependencies= FALSE"); //$NON-NLS-1$
		sb.append(')');
		fR.submitToConsole(sb.toString(), monitor);
		
		fManager.pkgScanner.addExpectedPkg(action.getLibraryLocation(), action.getPkg());
	}
	
	private void uninstallPkg(final RPkgAction.Uninstall action, final StringBuilder sb,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final String name = action.getPkg().getName();
		monitor.subTask(NLS.bind("Uninstalling R package ''{0}''...", name));
		
		sb.setLength(0);
		sb.append("remove.packages("); //$NON-NLS-1$
		sb.append('"').append(name).append('"');
		{	sb.append(", lib= "); //$NON-NLS-1$
			final Entry entry = fManager.getRLibPaths().getEntryByLocation(
					action.getLibraryLocation() );
			if ((entry.getAccess() & IRLibPaths.WRITABLE) == 0) {
				return;
			}
			sb.append('"').append(RUtil.escapeCompletely(entry.getRPath())).append('"');
		}
		sb.append(')');
		fR.submitToConsole(sb.toString(), monitor);
	}
	
	void loadPkgs(final List<? extends IRPkgInfoAndData> pkgs, final boolean expliciteLocation,
			final IRConsoleService r, final IProgressMonitor monitor) throws CoreException {
		fR = r;
		try {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < pkgs.size(); i++) {
				final IRPkgInfoAndData pkgData = pkgs.get(i);
				monitor.subTask(NLS.bind("Loading R package ''{0}''...", pkgData.getName()));
				
				sb.setLength(0);
				sb.append("library("); //$NON-NLS-1$
				sb.append('"').append(pkgData.getName()).append('"');
				if (expliciteLocation) {
					sb.append(", lib.loc= "); //$NON-NLS-1$
					final Entry entry = fManager.getRLibPaths().getEntryByLocation(
							pkgData.getLibraryLocation() );
					sb.append('"').append(RUtil.escapeCompletely(entry.getRPath())).append('"');
				}
				sb.append(')');
				fR.submitToConsole(sb.toString(), monitor);
			};
		}
		finally {
			fR.briefAboutChange(0x1); // auto
			fR = null;
		}
	}
	
}
