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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkg;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.pkgmanager.IRPkgList;
import de.walware.statet.r.core.pkgmanager.ISelectedRepos;
import de.walware.statet.r.core.pkgmanager.RPkgInfo;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.core.renv.IRLibraryLocation;


final class RPkgScanner {
	
	private static final String AVAIL_LIST_FNAME = "rj:::.renv.getAvailPkgs"; //$NON-NLS-1$
	private static final int AVAIL_LIST_COUNT1 = 9;
	private static final int AVAIL_LIST_IDX1_NAME = 0;
	private static final int AVAIL_LIST_IDX1_VERSION = 1;
	private static final int AVAIL_LIST_IDX1_PRIORITY = 2;
	private static final int AVAIL_LIST_IDX1_LICENSE = 3;
	private static final int AVAIL_LIST_IDX1_DEPENDS = 4;
	private static final int AVAIL_LIST_IDX1_IMPORTS = 5;
	private static final int AVAIL_LIST_IDX1_LINKINGTO = 6;
	private static final int AVAIL_LIST_IDX1_SUGGESTS = 7;
	private static final int AVAIL_LIST_IDX1_ENHANCES = 8;
	
	private static final String INST_LIST_FNAME = "rj:::.renv.getInstPkgs"; //$NON-NLS-1$
	private static final int INST_LIST_COUNT1 = 4;
	private static final int INST_LIST_IDX1_NAME = 0;
	private static final int INST_LIST_IDX1_VERSION = 1;
	private static final int INST_LIST_IDX1_TITLE = 2;
	private static final int INST_LIST_IDX1_BUILT = 3;
	
	private static final String INST_DETAIL_FNAME = "rj:::.renv.getInstPkgDetail"; //$NON-NLS-1$
	private static final int INST_DETAIL_LENGTH = 7;
	private static final int INST_DETAIL_IDX_PRIORITY = 0;
	private static final int INST_DETAIL_IDX_LICENSE = 1;
	private static final int INST_DETAIL_IDX_DEPENDS = 2;
	private static final int INST_DETAIL_IDX_IMPORTS = 3;
	private static final int INST_DETAIL_IDX_LINKINGTO = 4;
	private static final int INST_DETAIL_IDX_SUGGESTS = 5;
	private static final int INST_DETAIL_IDX_ENHANCES = 6;
	
	
	private static List<IRPkg> parsePkgRefs(final String s) {
		if (s == null || s.isEmpty()) {
			return Collections.emptyList();
		}
		final List<IRPkg> list = new ArrayList<>(4);
		String name = null;
		String version = null;
		boolean ws;
		final StringBuilder sb = new StringBuilder();
		ITER_S: for (int i = 0; i <= s.length();) {
			int c = (i < s.length()) ? s.charAt(i) : -1;
			
			if (c == -1 || c == ',') {
				if (name != null) {
					list.add(new RPkg(name, RNumVersion.create(version)));
				}
				name = null;
				version = null;
				i++;
				continue ITER_S;
			}
			if (Character.isLetterOrDigit(c)) {
				if (name != null) {
					// ?
				}
				name = null;
				version = null;
				int j = i+1;
				while (true) {
					c = (j < s.length()) ? s.charAt(j) : -1;
					if (c < 0 || Character.isWhitespace(c) || c == '(' || c == ',') {
						name = s.substring(i, j).intern();
						i = j;
						continue ITER_S;
					}
					j++;
				}
			}
			if (c == '(') {
				if (name == null || version != null) {
					// ?
					name = null;
					version = null;
				}
				sb.setLength(0);
				ws = false;
				int j = ++i;
				while (true) {
					c = (j < s.length()) ? s.charAt(j) : -1;
					if (c < 0 || c == ')') {
						version = sb.toString();
						i = j+1;
						continue ITER_S;
					}
					if (Character.isWhitespace(c)) {
						ws = true;
					}
					else {
						if (ws) {
							sb.append(' ');
							ws = false;
						}
						sb.append((char) c);
					}
					j++;
				}
			}
			
			i++;
			continue ITER_S;
		}
		return list;
	}
	
	
	private final RPkgCollection<IRPkgData> fExpectedPkgs = new RPkgCollection<>(4);
	
	
	public RPkgScanner() {
	}
	
	
	void addExpectedPkg(final IRLibraryLocation location, final IRPkgData pkg) {
		final String path = location.getDirectoryPath();
		final RPkgList<IRPkgData> list = fExpectedPkgs.getOrAdd(path);
		list.set(pkg);
	}
	
	private void clearExpected() {
		final List<RPkgList<IRPkgData>> all = fExpectedPkgs.getAll();
		for (final RPkgList<IRPkgData> list : all) {
			list.clear();
		}
	}
	
	
	FullRPkgSet loadAvailable(final ISelectedRepos repoSettings, final RService r, final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Loading available R packages...");
		try {
			final RCharacterStore repos = RDataUtil.checkRCharVector(r.evalData(
					"options('repos')[[1L]]", monitor)).getData(); //$NON-NLS-1$
			
			final int l = RDataUtil.checkIntLength(repos);
			final FullRPkgSet pkgs = new FullRPkgSet(l);
			for (int idxRepos = 0; idxRepos < l; idxRepos++) {
				final String repoURL = repos.getChar(idxRepos);
				if (repoURL == null || repoURL.isEmpty()) {
					continue;
				}
				final RRepo repo = Util.getRepoByURL(repoSettings.getRepos(), repoURL);
				monitor.subTask(NLS.bind("Loading available R packages from {0}...", repoURL));
				
				RArray<RCharacterStore> data;
				{	final FunctionCall call = r.createFunctionCall(AVAIL_LIST_FNAME);
					call.addChar("repo", repoURL); //$NON-NLS-1$
					if (repo.getPkgType() != null) {
						final String key = RPkgUtil.getPkgTypeInstallKey(r.getPlatform(), repo.getPkgType());
						if (key == null) {
							continue;
						}
					}
					data = RDataUtil.checkRCharArray(call.evalData(monitor), 2);
					RDataUtil.checkColumnCountEqual(data, AVAIL_LIST_COUNT1);
				}
				
				final RCharacterStore store = data.getData();
				final int nPkgs = data.getDim().getInt(0);
				final RPkgList<RPkgData> list = new RPkgList<>(nPkgs);
				pkgs.getAvailable().add(repo.getId(), list);
				
				for (int idxPkgs = 0; idxPkgs < nPkgs; idxPkgs++) {
					String name = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_NAME));
					final String version = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_VERSION));
					if (name != null && !name.isEmpty() && version != null) {
						name = name.intern();
						final RPkgData pkg = new RPkgData(name, RNumVersion.create(version), repo.getId());
						
						pkg.setLicense(store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_LICENSE)));
						
						pkg.setDepends(RPkgScanner.parsePkgRefs(store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_DEPENDS))));
						pkg.setImports(RPkgScanner.parsePkgRefs(store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_IMPORTS))));
						pkg.setLinkingTo(RPkgScanner.parsePkgRefs(store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_LINKINGTO))));
						pkg.setSuggests(RPkgScanner.parsePkgRefs(store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_SUGGESTS))));
						pkg.setEnhances(RPkgScanner.parsePkgRefs(store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_ENHANCES))));
						
						pkg.setPriority(store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkgs, AVAIL_LIST_IDX1_PRIORITY)));
						
						list.add(pkg);
					}
				}
			}
			return pkgs;
		}
		catch (final UnexpectedRDataException | CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when loading list of available R packages.",
					e ));
		}
	}
	
	void updateInstLight(final RLibPaths rLibPaths, final boolean[] update,
			final RPkgSet newPkgs, final Change event,
			final RService r, final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Updating installed R packages...");
		try {
			final RPkgSet oldPkgs = (RPkgSet) event.fOldPkgs;
			final RPkgChangeSet changeSet = new RPkgChangeSet();
			event.fInstalledPkgs = changeSet;
			for (final RLibPaths.EntryImpl libPath : rLibPaths.getEntries()) {
				final IRLibraryLocation location= libPath.getLocation();
				if (libPath.getRIndex() < 0) {
					continue;
				}
				if (newPkgs.getInstalled().getBySource(location.getDirectoryPath()) != null ) {
					continue;
				}
				if (update == null || update[libPath.getRIndex()] || oldPkgs == null) {
					final RArray<RCharacterStore> data;
					{	final FunctionCall call = r.createFunctionCall(INST_LIST_FNAME);
						call.addChar("lib", libPath.getRPath()); //$NON-NLS-1$
						data = RDataUtil.checkRCharArray(call.evalData(monitor), 2);
						RDataUtil.checkColumnCountEqual(data, INST_LIST_COUNT1);
					}
					final RCharacterStore store = data.getData();
					final int nPkgs = data.getDim().getInt(0);
					
					final RPkgList<RPkgInfo> oldList = (oldPkgs != null) ?
							oldPkgs.getInstalled().getBySource(location.getDirectoryPath()) : null;
					final RPkgList<RPkgInfo> newList = new RPkgList<>(nPkgs);
					final RPkgList<IRPkgData> expectedList = fExpectedPkgs.getBySource(location.getDirectoryPath());
					for (int idxPkg = 0; idxPkg < nPkgs; idxPkg++) {
						String name = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_NAME));
						final String version = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_VERSION));
						if (name != null && !name.isEmpty() && version != null && !version.isEmpty()) {
							name = name.intern();
							String built = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_BUILT));
							if (built == null) {
								built = ""; //$NON-NLS-1$
							}
							final RPkgInfo oldPkg = (oldList != null) ? oldList.get(name) : null;
							final RPkgInfo newPkg;
							final boolean changed = (oldPkg == null
									|| !oldPkg.getVersion().toString().equals(version)
									|| !oldPkg.getBuilt().equals(built) );
							
							if (!changed) {
								newPkg = oldPkg;
							}
							else {
								final IRPkgData expectedData =
										(expectedList != null) ? expectedList.get(name) : null;
								newPkg = new RPkgInfo(name, RNumVersion.create(version), built,
										store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_TITLE)),
										location, 
										0,
										event.fStamp,
										(expectedData != null) ? expectedData.getRepoId() : null );
								
								changeSet.names.add(name);
								if (oldPkg == null) {
									changeSet.added.add(newPkg);
								}
								else {
									changeSet.changed.add(oldPkg);
								}
							}
							
							newList.add(newPkg);
						}
					}
					if (oldList != null) {
						int i = 0, j = 0;
						final int in = oldList.size(), jn = newList.size();
						while (i < in) {
							final RPkgInfo oldPkg = oldList.get(i++);
							final String name = oldPkg.getName();
							if (j < jn) {
								if (newList.get(j).getName() == name) {
									j++;
									continue;
								}
								final int idx = newList.indexOf(name, j + 1);
								if (idx >= 0) {
									j = idx + 1;
									continue;
								}
							}
							changeSet.names.add(name);
							changeSet.deleted.add(oldPkg);
						}
					}
					
					newPkgs.getInstalled().add(location.getDirectoryPath(), newList);
				}
				else {
					newPkgs.getInstalled().add(location.getDirectoryPath(),
							oldPkgs.getInstalled().getBySource(location.getDirectoryPath()) );
				}
			}
			
			clearExpected();
			return;
		}
		catch (final UnexpectedRDataException | CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when loading list of installed R packages.",
					e ));
		}
	}
	
	void updateInstFull(final RLibPaths rLibPaths, final boolean[] update,
			final FullRPkgSet newPkgs, final Change event,
			final RService r, final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Updating installed R packages...");
		try {
			final FullRPkgSet oldFullPkgs = (event.fOldPkgs instanceof FullRPkgSet) ?
					(FullRPkgSet) event.fOldPkgs : null;
			final RPkgChangeSet changeSet = new RPkgChangeSet();
			event.fInstalledPkgs = changeSet;
			for (final RLibPaths.EntryImpl libPath : rLibPaths.getEntries()) {
				final IRLibraryLocation location= libPath.getLocation();
				if (libPath.getRIndex() < 0) {
					continue;
				}
				if (update == null || update[libPath.getRIndex()] || oldFullPkgs == null) {
					final RArray<RCharacterStore> data;
					{	final FunctionCall call = r.createFunctionCall(INST_LIST_FNAME);
						call.addChar("lib", libPath.getRPath()); //$NON-NLS-1$
						data = RDataUtil.checkRCharArray(call.evalData(monitor), 2);
						RDataUtil.checkColumnCountEqual(data, INST_LIST_COUNT1);
					}
					final RCharacterStore store = data.getData();
					final int nPkgs = data.getDim().getInt(0);
					
					final IRPkgList<? extends IRPkgInfo> oldList = (event.fOldPkgs != null) ?
							event.fOldPkgs.getInstalled().getBySource(location.getDirectoryPath()) : null;
					final RPkgList<RPkgInfoAndData> newList = new RPkgList<>(nPkgs);
					final RPkgList<IRPkgData> expectedList = fExpectedPkgs.getBySource(location.getDirectoryPath());
					for (int idxPkg = 0; idxPkg < nPkgs; idxPkg++) {
						String name = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_NAME));
						final String version = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_VERSION));
						if (name != null && !name.isEmpty() && version != null && !version.isEmpty()) {
							name = name.intern();
							String built = store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_BUILT));
							if (built == null) {
								built = ""; //$NON-NLS-1$
							}
							final IRPkgInfo oldPkg = (oldList != null) ? oldList.get(name) : null;
							final RPkgInfoAndData newPkg;
							final boolean changed = (oldPkg == null
									|| !oldPkg.getVersion().toString().equals(version)
									|| !oldPkg.getBuilt().equals(built) );
							
							if (!changed && (oldPkg instanceof RPkgInfoAndData)) {
								newPkg = (RPkgInfoAndData) oldPkg;
							}
							else {
								final IRPkgData expectedData = (changed && expectedList != null) ?
										expectedList.get(name) : null;
								newPkg = new RPkgInfoAndData(name,
										(!changed) ? oldPkg.getVersion() : RNumVersion.create(version),
										built,
										store.getChar(RDataUtil.getDataIdx(nPkgs, idxPkg, INST_LIST_IDX1_TITLE)),
										location,
										(!changed) ? oldPkg.getFlags() : 0,
										(!changed) ? oldPkg.getInstallStamp() : event.fStamp,
										(!changed) ? oldPkg.getRepoId() : ((expectedData != null) ? expectedData.getRepoId() : null) );
								
								RCharacterStore detail;
								{	final FunctionCall call = r.createFunctionCall(INST_DETAIL_FNAME);
									call.addChar("lib", libPath.getRPath()); //$NON-NLS-1$
									call.addChar("name", name); //$NON-NLS-1$
									detail = RDataUtil.checkRCharVector(call.evalData(monitor)).getData();
									RDataUtil.checkLengthEqual(detail, INST_DETAIL_LENGTH);
								}
								
								newPkg.setPriority(detail.getChar(INST_DETAIL_IDX_PRIORITY));
								newPkg.setLicense(detail.getChar(INST_DETAIL_IDX_LICENSE));
								
								newPkg.setDepends(RPkgScanner.parsePkgRefs(detail.getChar(INST_DETAIL_IDX_DEPENDS)));
								newPkg.setImports(RPkgScanner.parsePkgRefs(detail.getChar(INST_DETAIL_IDX_IMPORTS)));
								newPkg.setLinkingTo(RPkgScanner.parsePkgRefs(detail.getChar(INST_DETAIL_IDX_LINKINGTO)));
								newPkg.setSuggests(RPkgScanner.parsePkgRefs(detail.getChar(INST_DETAIL_IDX_SUGGESTS)));
								newPkg.setEnhances(RPkgScanner.parsePkgRefs(detail.getChar(INST_DETAIL_IDX_ENHANCES)));
								
								if (changed) {
									changeSet.names.add(name);
									if (oldPkg == null) {
										changeSet.added.add(newPkg);
									}
									else {
										changeSet.changed.add(oldPkg);
									}
								}
							}
							
							newList.add(newPkg);
						}
					}
					if (oldList != null) {
						int i = 0, j = 0;
						final int in = oldList.size(), jn = newList.size();
						while (i < in) {
							final IRPkgInfo oldPkg = oldList.get(i++);
							final String name = oldPkg.getName();
							if (j < jn) {
								if (newList.get(j).getName() == name) {
									j++;
									continue;
								}
								final int idx = newList.indexOf(name, j + 1);
								if (idx >= 0) {
									j = idx + 1;
									continue;
								}
							}
							changeSet.names.add(name);
							changeSet.deleted.add(oldPkg);
						}
					}
					
					newPkgs.getInstalled().add(location.getDirectoryPath(), newList);
				}
				else {
					newPkgs.getInstalled().add(location.getDirectoryPath(),
							oldFullPkgs.getInstalled().getBySource(location.getDirectoryPath()) );
				}
			}
			return;
		}
		catch (final UnexpectedRDataException | CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when loading list of installed R packages.",
					e ));
		}
	}
	
}
