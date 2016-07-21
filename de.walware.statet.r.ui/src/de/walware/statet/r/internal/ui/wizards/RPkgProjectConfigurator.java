/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.io.FileUtil.ReadTextFileOperation;
import de.walware.ecommons.io.FileUtil.ReaderAction;

import de.walware.eutils.autonature.core.IProjectConfigurator;

import de.walware.statet.r.core.RBuildpaths;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.ui.RUI;


public class RPkgProjectConfigurator implements ProjectConfigurator, IProjectConfigurator {
	
	
	private static class AbortException extends RuntimeException {
		
		private static final long serialVersionUID= 1L;
		
		public AbortException() {
		}
		
	}
	
	private static class ParseResult {
		
		private String pkgName;
		
		public ParseResult() {
		}
		
	}
	
	
	private static final Path PKG_DESCRIPTION_FILE_JPATH= Paths.get(RBuildpaths.PKG_DESCRIPTION_FILE_NAME);
	private static final Path PKG_NAMESPACE_FILE_JPATH= Paths.get(RBuildpaths.PKG_NAMESPACE_FILE_NAME);
	
	private static final Path E_PROJECT_FILE_JPATH= Paths.get(".project"); //$NON-NLS-1$
	private static final Path E_METADATA_DIR_JPATH= Paths.get(".metadata"); //$NON-NLS-1$
	
	private final static int MAX_DEPTH= 3;
	
	private static final String PACKAGE_FIELD= "Package:"; //$NON-NLS-1$
	
	
	private static boolean isFileExists(final IContainer parent, final IPath path) {
		final IResource member= parent.findMember(path);
		return (member != null && member.getType() == IResource.FILE
				&& member.exists() );
	}
	
	private static boolean isFileExists(final Path parent, final Path path) {
		return Files.isRegularFile(parent.resolve(path));
	}
	
	private static boolean isRPkgRoot(final IContainer container) {
		return (isFileExists(container, RBuildpaths.PKG_DESCRIPTION_FILE_PATH)
				&& isFileExists(container, RBuildpaths.PKG_NAMESPACE_FILE_PATH) );
	}
	
	private static boolean isRPkgRoot(final Path container) {
		return (isFileExists(container, PKG_DESCRIPTION_FILE_JPATH)
				&& isFileExists(container, PKG_NAMESPACE_FILE_JPATH) );
	}
	
	private static void addExistingFolder(final IContainer parent, final IPath path,
			final Set<IFolder> set) {
		final IResource member= parent.findMember(path);
		if (member != null && member.getType() == IResource.FOLDER) {
			set.add((IFolder) member);
		}
	}
	
	
	private static class RPkgRootsJFinder {
		
		private final static int ROOT= 10000;
		
		private final Path root;
		
		private List<File> pkgRoots;
		private int lastPkgRootDepth;
		
		private final IProgressMonitor monitor;
		
		
		public RPkgRootsJFinder(final Path root, final IProgressMonitor monitor) {
			this.root= root;
			this.monitor= monitor;
		}
		
		
		public int visit(final Path dir) throws IOException {
			if (this.monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			if (dir != this.root && isFileExists(dir, E_PROJECT_FILE_JPATH)) {
				return ROOT;
			}
			
			if (isRPkgRoot(dir)) {
				if (this.pkgRoots == null) {
					this.pkgRoots= new ArrayList<>();
				}
				this.pkgRoots.add(dir.toFile());
				this.lastPkgRootDepth= 0;
				
				return 1;
			}
			
			int count= 0;
			for (final Path path : Files.newDirectoryStream(dir)) {
				if (Files.isDirectory(path)) {
					if (path.endsWith(E_METADATA_DIR_JPATH)) {
						count+= ROOT;
					}
					else {
						count+= visit(path);
					}
				}
			}
			if (count == 1 && this.lastPkgRootDepth < MAX_DEPTH) {
				this.pkgRoots.set(this.pkgRoots.size() - 1, dir.toFile());
				this.lastPkgRootDepth++;
			}
			return count;
		}
		
		public Set<File> getPkgRoots() {
			return (this.pkgRoots != null) ? new HashSet<>(this.pkgRoots) : null;
		}
		
	}
	
	private static class RPkgRootResourceFinder implements IResourceVisitor {
		
		private final IContainer root;
		private final Set<IPath> ignoredPaths;
		
		private IContainer pkgRoot;
		
		private final IProgressMonitor monitor;
		
		
		public RPkgRootResourceFinder(final IContainer root, final Set<IPath> ignoredPaths,
				final IProgressMonitor monitor) {
			this.root= root;
			this.ignoredPaths= ignoredPaths;
			this.monitor= monitor;
		}
		
		@Override
		public boolean visit(final IResource resource) throws CoreException {
			switch (resource.getType()) {
			case IResource.PROJECT:
			case IResource.FOLDER:
				if (this.monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				break;
			default:
				return false;
			}
			
			if (resource != this.root && this.ignoredPaths != null) {
				for (final IPath ignoredDirectory : this.ignoredPaths) {
					if (ignoredDirectory.equals(resource.getLocation())) {
						return false;
					}
				}
			}
			
			if (isRPkgRoot((IContainer) resource)) {
				if (this.pkgRoot == null) {
					this.pkgRoot= (IContainer) resource;
				}
				else {
					this.pkgRoot= null;
					throw new AbortException();
				}
				return false;
			}
			
			return true;
		}
		
		public boolean hasPkgRoot() {
			return (this.pkgRoot != null);
		}
		
		public boolean hasPkgRootInLimit() {
			int depth= 0;
			IContainer folder= this.pkgRoot;
			while (folder != null && depth <= MAX_DEPTH) {
				if (folder.equals(this.root)) {
					return true;
				}
				folder= folder.getParent();
				depth++;
			}
			return false;
		}
		
		public IContainer getPkgRoot() {
			return this.pkgRoot;
		}
		
	}
	
	
	public RPkgProjectConfigurator() {
	}
	
	
	@Override
	public Set<File> findConfigurableLocations(final File root,
			final IProgressMonitor monitor) {
		final Path rootPath= root.toPath();
		final RPkgRootsJFinder finder= new RPkgRootsJFinder(rootPath, monitor);
		try {
			finder.visit(rootPath);
		}
		catch (final OperationCanceledException e) {
			return null;
		}
		catch (final IOException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
					NLS.bind("An error occurred when searching for R packages in ''{0}''.",
							root.getAbsolutePath() ),
					e ));
		}
		return finder.getPkgRoots();
	}
	
	@Override
	public boolean shouldBeAnEclipseProject(final IContainer container,
			final IProgressMonitor monitor) {
		final RPkgRootResourceFinder finder= searchPkgRoot(container, null, monitor);
		return (finder != null && finder.hasPkgRootInLimit());
	}
	
	@Override
	public Set<IFolder> getFoldersToIgnore(final IProject project,
			final IProgressMonitor monitor) {
		final Set<IFolder> toIgnore= new HashSet<>();
		
		addExistingFolder(project, RBuildpaths.PKG_R_FOLDER_PATH, toIgnore);
		addExistingFolder(project, RBuildpaths.PKG_DATA_FOLDER_PATH, toIgnore);
		addExistingFolder(project, RBuildpaths.PKG_DEMO_FOLDER_PATH, toIgnore);
		addExistingFolder(project, RBuildpaths.PKG_MAN_FOLDER_PATH, toIgnore);
		addExistingFolder(project, RBuildpaths.PKG_PO_FOLDER_PATH, toIgnore);
		addExistingFolder(project, RBuildpaths.PKG_INST_FOLDER_PATH, toIgnore);
		addExistingFolder(project, RBuildpaths.PKG_RCHECK_FOLDER_PATH, toIgnore);
		
		return toIgnore;
	}
	
	
	@Override
	public boolean canConfigure(final IProject project, final Set<IPath> ignoredPaths,
			final IProgressMonitor monitor) {
		try {
			if (project.hasNature(RProjects.R_PKG_NATURE_ID)) {
				return false;
			}
		}
		catch (final CoreException e) {}
		
		final RPkgRootResourceFinder finder= searchPkgRoot(project, ignoredPaths, monitor);
		return (finder != null && finder.hasPkgRoot());
	}
	
	@Override
	public byte check(final IProject project, final IProgressMonitor monitor) {
//		try {
//			if (project.hasNature(RProjects.R_PKG_NATURE_ID)) {
//				return ALREADY_CONFIGURED;
//			}
//		}
//		catch (final CoreException e) {}
		
		final RPkgRootResourceFinder finder= searchPkgRoot(project, null, monitor);
		return (finder != null && finder.hasPkgRoot()) ? CONFIGURABLE : NOT_CONFIGURABLE;
	}
	
	@Override
	public void configure(final IProject project, final Set<IPath> ignoredPaths,
			final IProgressMonitor monitor) {
		final SubMonitor m= SubMonitor.convert(monitor, 1 + 3 + 3);
		
		final IContainer pkgRoot;
		final RPkgRootResourceFinder rootFinder= searchPkgRoot(project, ignoredPaths, m.newChild(1));
		if (rootFinder == null || (pkgRoot= rootFinder.getPkgRoot()) == null) {
			return;
		}
		
		final IFile descriptionFile= pkgRoot.getFile(RBuildpaths.PKG_DESCRIPTION_FILE_PATH);
		final ParseResult parseResult= parsePkgName(descriptionFile, m.newChild(3));
		
		try {
			RProjects.setupRPkgProject(project, pkgRoot, parseResult.pkgName, m.newChild(3));
		}
		catch (final CoreException e) {
			if (e.getStatus().getSeverity() != IStatus.CANCEL) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
						NLS.bind("An error occurred when configuring R project ''{0}''.",
								project.getFullPath().toString() ),
						e ));
			}
		}
	}
	
	@Override
	public void configure(final IProject project, final IProgressMonitor monitor) {
		configure(project, null, monitor);
	}
	
	private RPkgRootResourceFinder searchPkgRoot(final IContainer container, final Set<IPath> ignoredPaths,
			final IProgressMonitor m) {
		final RPkgRootResourceFinder finder= new RPkgRootResourceFinder(container, ignoredPaths, m);
		try {
			container.accept(finder);
		}
		catch (final AbortException e) {}
		catch (final OperationCanceledException e) {
			return null;
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
					NLS.bind("An error occurred when searching for R packages in ''{0}''.",
							container.getFullPath().toString() ),
					e ));
			return null;
		}
		return finder;
	}
	
	private ParseResult parsePkgName(final IFile descriptionFile,
			final SubMonitor m) {
		final ParseResult result= new ParseResult();
		try {
			final ReadTextFileOperation fileOp= FileUtil.getFileUtil(descriptionFile).createReadTextFileOp(
					new ReaderAction() {
				@Override
				public void run(final BufferedReader reader,
						final IProgressMonitor monitor) throws IOException, CoreException {
					String line;
					while ((line= reader.readLine()) != null) {
						if (line.startsWith(PACKAGE_FIELD)) {
							final String value= line.substring(PACKAGE_FIELD.length()).trim();
							if (!value.isEmpty()) {
								result.pkgName= value;
							}
							return;
						}
					}
				}
			});
			fileOp.doOperation(m);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
					NLS.bind("An error occurred when parsing R package description file ''{0}''.",
							descriptionFile.getFullPath().toString() ),
					e ));
		}
		return result;
	}
	
}
