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

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.io.FileUtil;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.renv.RLibraryGroup;
import de.walware.statet.r.internal.core.renv.RLibraryLocation;


public class REnvLibGroups {
	
	
	static REnvLibGroups loadFromR(final RService r,
			final IProgressMonitor monitor) throws CoreException {
		try {
			final List<IRLibraryLocation>[] groups = new List[4];
			for (int i = 0; i < groups.length; i++) {
				groups[i] = new ArrayList<>(4);
			}
			
			final String rHome = RDataUtil.checkSingleCharValue(r.evalData("R.home()", monitor)); //$NON-NLS-1$
			final Pattern varPattern = Pattern.compile(Pattern.quote(r.getPlatform().getPathSep()));
			
			final Set<String> added = new HashSet<>();
			{	final String userVar = RDataUtil.checkSingleCharValue(
						r.evalData("Sys.getenv('R_LIBS_USER')", monitor) ); //$NON-NLS-1$
				final String[] paths = varPattern.split(userVar);
				for (final String path : paths) {
					if (path != null && !path.isEmpty()) {
						groups[1].add(new RLibraryLocation(IRLibraryLocation.R, path, null));
						added.add(path);
					}
				}
			}
			{	final RCharacterStore paths = RDataUtil.checkRCharVector(
						r.evalData(".Library.site", monitor) ).getData(); //$NON-NLS-1$
				final int l = RDataUtil.checkIntLength(paths);
				for (int i = 0; i < l; i++) {
					final String path = paths.get(i);
					if (path != null && !path.isEmpty()) {
						groups[2].add(new RLibraryLocation(IRLibraryLocation.R, path, null));
						added.add(path);
					}
				}
			}
			{	final RCharacterStore paths = RDataUtil.checkRCharVector(
						r.evalData(".Library", monitor) ).getData(); //$NON-NLS-1$
				final int l = RDataUtil.checkIntLength(paths);
				for (int i = 0; i < l; i++) {
					final String path = paths.get(i);
					if (path != null && !path.isEmpty()) {
						groups[3].add(new RLibraryLocation(IRLibraryLocation.R, path, null));
						added.add(path);
					}
				}
			}
			{	final RCharacterStore paths = RDataUtil.checkRCharVector(
						r.evalData(".libPaths()", monitor) ).getData(); //$NON-NLS-1$
				final int l = RDataUtil.checkIntLength(paths);
				for (int i = 0; i < l; i++) {
					final String path = paths.get(i);
					if (path != null && !path.isEmpty() && added.add(path)) {
						groups[0].add(new RLibraryLocation(IRLibraryLocation.R, path, null));
					}
				}
			}
			
			return new REnvLibGroups(rHome, groups);
		}
		catch (final UnexpectedRDataException | CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when detecting the R library path.",
					e ));
		}
	}
	
	
	private final String fRHome;
	private final List<? extends IRLibraryGroup> fGroups;
	
	
	private REnvLibGroups(String rHome, final List<IRLibraryLocation>[] groups) {
		if (rHome.endsWith("/") || rHome.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			rHome = rHome.substring(0, rHome.length() - 1);
		}
		fRHome = rHome;
		
		for (int i = 0; i < 4; i++) {
			final List<IRLibraryLocation> locations = groups[i];
			for (int j = 0; j < locations.size(); j++) {
				final IRLibraryLocation location = locations.get(j);
				final String path = encode(location.getDirectoryPath());
				if (path != location.getDirectoryPath()) {
					locations.set(j, new RLibraryLocation(IRLibraryLocation.R, path, null));
				}
			}
		}
		fGroups = new ConstArrayList<IRLibraryGroup>(
				new RLibraryGroup.Final(RLibraryGroup.R_OTHER,
						RLibraryGroup.getLabel(RLibraryGroup.R_OTHER),
						new ConstArrayList<>(groups[0]) ),
				new RLibraryGroup.Final(RLibraryGroup.R_USER,
						RLibraryGroup.getLabel(RLibraryGroup.R_USER),
						new ConstArrayList<>(groups[1]) ),
				new RLibraryGroup.Final(RLibraryGroup.R_SITE,
						RLibraryGroup.getLabel(RLibraryGroup.R_SITE),
						new ConstArrayList<>(groups[2]) ),
				new RLibraryGroup.Final(RLibraryGroup.R_DEFAULT,
						RLibraryGroup.getLabel(RLibraryGroup.R_DEFAULT),
						new ConstArrayList<>(groups[3]) ));
	}
	
	public REnvLibGroups(final IREnvConfiguration config) {
		fRHome = null;
		fGroups = config.getRLibraryGroups();
	}
	
	
	public List<? extends IRLibraryGroup> getGroups() {
		return fGroups;
	}
	
	public IRLibraryLocation getFirstUserLibLocation() {
		for (final IRLibraryGroup group : fGroups) {
			if (group.getId().equals(IRLibraryGroup.R_USER)) {
				final List<? extends IRLibraryLocation> libraries = group.getLibraries();
				return (!libraries.isEmpty()) ? libraries.get(0) : null;
			}
		}
		return null;
	}
	
	public IRLibraryLocation getLibLocation(final String path) {
		if (fRHome != null) {
			final String encodedPath = encode(path);
			for (final IRLibraryGroup group : fGroups) {
				for (final IRLibraryLocation location : group.getLibraries()) {
					if (encodedPath.equals(location.getDirectoryPath())) {
						return location;
					}
				}
			}
			return null;
		}
		else {
			try {
				final IFileStore store = FileUtil.getLocalFileStore(path);
				for (final IRLibraryGroup group : fGroups) {
					for (final IRLibraryLocation location : group.getLibraries()) {
						if (store.equals(location.getDirectoryStore())) {
							return location;
						}
					}
				}
			}
			catch (final CoreException e) {
				RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
						"An error occurred when detecting R library location.", e));
			}
			return null;
		}
	}
	
	
	private String encode(String path) {
		if (fRHome != null && path.startsWith(fRHome)) {
			path = "${r_home}" + path.substring(fRHome.length()); //$NON-NLS-1$
		}
		return path;
	}
	
	public String resolve(String path) {
		if (fRHome != null) {
			path = path.replace("${r_home}", fRHome); //$NON-NLS-1$
			path = path.replace("${env_var:R_HOME}", fRHome); //$NON-NLS-1$
		}
		return path;
	}
	
}
