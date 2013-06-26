/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.ecommons.FastList;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference.StringPref2;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RLogicalStore;
import de.walware.rj.data.RNumericStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RVectorImpl;
import de.walware.rj.eclient.AbstractRToolRunnable;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkgType;
import de.walware.rj.server.srvext.ServerUtil;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RPlatform;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgDescription;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.pkgmanager.IRView;
import de.walware.statet.r.core.pkgmanager.ISelectedRepos;
import de.walware.statet.r.core.pkgmanager.RPkgAction;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.core.pkgmanager.SelectedRepos;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.tool.AbstractStatetRRunnable;
import de.walware.statet.r.core.tool.IRConsoleService;
import de.walware.statet.r.internal.core.RCorePlugin;


public class RPkgManager implements IRPkgManager.Ext, SettingsChangeNotifier.ManageListener {
	
	
	private final static int REQUIRE_CRAN =                 0x10000000;
	private final static int REQUIRE_BIOC =                 0x20000000;
	private final static int REQUIRE_REPOS =                0x80000000;
	
	private final static int REQUIRE_REPO_PKGS =            0x01000000;
	private final static int REQUIRE_INST_PKGS =            0x08000000;
	
	private final static RRepoPref LAST_CRAN_PREF = new RRepoPref(PREF_QUALIFIER, "LastCRAN.repo"); //$NON-NLS-1$
	private final static RRepoPref LAST_BIOC_PREF = new RRepoPref(PREF_QUALIFIER, "LastBioC.repo"); //$NON-NLS-1$
	
	private final static int MIRROR_CHECK = 1000 * 60 * 60 * 6;
	private final static int PKG_CHECK = 1000 * 60 * 60 * 3;
	
	
	private final IREnv fREnv;
	private RPlatform fRPlatform;
	
	private final IFileStore fREnvDirectory;
	
	private boolean fFirstTime;
	
	private String fBioCVersion;
	private final StringPref2 fBioCVersionPref;
	
	private List<RRepo> fCustomRepos;
	private final List<RRepo> fAddRepos;
	private List<RRepo> fRRepos;
	private List<RRepo> fAllRepos;
	private List<RRepo> fSelectedReposInR;
	private final RRepoListPref fSelectedReposPref;
	
	private List<RRepo> fCustomCRAN;
	private List<RRepo> fRCRAN;
	private RRepo fRCRANByCountry;
	private List<RRepo> fAllCRAN;
	private String fSelectedCRANInR;
	private final RRepoPref fSelectedCRANPref;
	
	private List<RRepo> fCustomBioC;
	private List<RRepo> fRBioC;
	private List<RRepo> fAllBioC;
	private String fSelectedBioCInR;
	private final RRepoPref fSelectedBioCPref;
	private long fMirrorsStamp;
	
	private SelectedRepos fSelectedRepos;
	
	private RVector<RNumericStore> fLibs = null;
	private REnvLibGroups fRLibGroups;
	private RLibPaths fRLibPaths;
	
	private RPkgSet fPkgsLight;
	private FullRPkgSet fPkgsExt;
	private long fPkgsStamp;
	final RPkgScanner fPkgScanner = new RPkgScanner();
	
	private volatile int fRequireLoad;
	private volatile int fRequireConfirm;
	
	private final FastList<Listener> fListeners = new FastList<Listener>(Listener.class);
	
	private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
	
	private List<RView> fRViews;
	private RNumVersion fRViewsVersion;
//	private List<RView> fBioCViews;
//	private String fBioCViewsVersion;
//	private long fBioCViewsStamp;
	
	private ITool fRProcess;
	private int fRTask;
	private Change fRTaskEvent;
	
	private DB fDB;
	private final Cache fCache;
	
	
	public RPkgManager(final IREnvConfiguration rConfig) {
		fREnv = rConfig.getReference();
		final IPath path = RCorePlugin.getDefault().getStateLocation().append("renv").append(fREnv.getId()); //$NON-NLS-1$
		fREnvDirectory = EFS.getLocalFileSystem().getStore(path);
		final String qualifier = ((AbstractPreferencesModelObject) rConfig).getNodeQualifiers()[0];
		fSelectedReposPref = new RRepoListPref(qualifier, "RPkg.Repos.repos"); //$NON-NLS-1$
		fSelectedCRANPref = new RRepoPref(qualifier, "RPkg.CRANMirror.repo"); //$NON-NLS-1$
		fBioCVersionPref = new StringPref2(qualifier, "RPkg.BioCVersion.ver"); //$NON-NLS-1$
		fSelectedBioCPref = new RRepoPref(qualifier, "RPkg.BioCMirror.repo"); //$NON-NLS-1$
		
		final IPreferenceAccess prefs = PreferencesUtil.getInstancePrefs();
		fAddRepos = new ArrayList<RRepo>();
		if (rConfig.getType() == IREnvConfiguration.USER_LOCAL_TYPE) {
			final String rjVersion = "" + ServerUtil.RJ_VERSION[0] + '.' + ServerUtil.RJ_VERSION[1]; //$NON-NLS-1$
			fAddRepos.add(new RRepo(RRepo.SPECIAL_PREFIX+"rj", "RJ", "http://download.walware.de/rj-" + rjVersion, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		fSelectedRepos = new SelectedRepos(
				prefs.getPreferenceValue(fSelectedReposPref),
				prefs.getPreferenceValue(fSelectedCRANPref),
				prefs.getPreferenceValue(fBioCVersionPref),
				prefs.getPreferenceValue(fSelectedBioCPref) );
		
		fDB = DB.create(fREnv, fREnvDirectory);
		fCache = new Cache(fREnvDirectory);
		resetPkgs(rConfig);
		
		fFirstTime = true;
		fMirrorsStamp = fPkgsStamp = System.currentTimeMillis();
		fRequireLoad |= (REQUIRE_CRAN | REQUIRE_BIOC | REQUIRE_REPOS);
		fRequireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
		
		PreferencesUtil.getSettingsChangeNotifier().addManageListener(this);
		
		getWriteLock().lock();
		try {
			loadPrefs(true);
		}
		finally {
			getWriteLock().unlock();
		}
	}
	
	private void resetPkgs(final IREnvConfiguration config) {
		fPkgsExt = null;
		if (fDB != null && config != null) {
			fPkgsLight = fDB.loadPkgs(config.getRLibraryGroups());
		}
		if (fPkgsLight == null) {
			fDB = null;
			fPkgsLight = new RPkgSet(0);
		}
	}
	
	
	@Override
	public IREnv getREnv() {
		return fREnv;
	}
	
	Cache getCache() {
		return fCache;
	}
	
	@Override
	public RPlatform getRPlatform() {
		return fRPlatform;
	}
	
	@Override
	public Lock getReadLock() {
		return fLock.readLock();
	}
	
	@Override
	public Lock getWriteLock() {
		return fLock.writeLock();
	}
	
	
	@Override
	public void clear() {
		getWriteLock().lock();
		try {
			fSelectedRepos = new SelectedRepos(Collections.<RRepo> emptyList(),
					null, null, null);
			savePrefs(fSelectedRepos);
			
			fRequireLoad |= (REQUIRE_CRAN | REQUIRE_BIOC);
			fRequireLoad |= (REQUIRE_REPOS);
			
			final Change change = new Change(fREnv);
			change.fRepos = 1;
			checkRepos(change);
			
			fRequireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
			resetPkgs(fREnv.getConfig());
			fFirstTime = true;
		}
		finally {
			getWriteLock().unlock();
		}
	}
	
	public void dispose() {
		PreferencesUtil.getSettingsChangeNotifier().removeManageListener(this);
	}
	
	@Override
	public void beforeSettingsChangeNotification(final Set<String> groupIds) {
		if (groupIds.contains(CUSTOM_GROUP_ID)) {
			loadPrefs(groupIds.contains(CUSTOM_GROUP_ID));
		}
	}
	
	@Override
	public void afterSettingsChangeNotification(final Set<String> groupIds) {
	}
	
	
	@Override
	public void check(final int flags, final RService r, final IProgressMonitor monitor) throws CoreException {
		checkInit(flags, r, monitor);
		check(r, monitor);
	}
	
	private void checkInit(final int flags,
			final RService r, final IProgressMonitor monitor) throws CoreException {
		if ((flags & INITIAL) == INITIAL || fRPlatform == null) {
			checkRVersion(r.getPlatform());
			
			final IREnvConfiguration config = fREnv.getConfig();
			if (config != null && config.isRemote()) {
				fRLibGroups = REnvLibGroups.loadFromR(r, monitor);
			}
		}
	}
	
	private void check(
			final RService r, final IProgressMonitor monitor) throws CoreException {
		if (!beginRTaskSilent((IToolService) r, monitor)) {
			return;
		}
		try {
			checkInstalled(null, r, monitor);
			
			if (fRTaskEvent != null) {
				fireUpdate(fRTaskEvent);
			}
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when checking for new and updated R packages.", e));
		}
		finally {
			fRTaskEvent = null;
			endRTask();
		}
	}
	
	private void checkRVersion(final RPlatform rPlatform) {
		if (fRPlatform != null && !fRPlatform.getRVersion().equals(rPlatform.getRVersion())) {
			getWriteLock().lock();
			try {
				fRequireLoad |= (REQUIRE_REPOS | REQUIRE_CRAN | REQUIRE_BIOC);
				fRequireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
			}
			finally {
				getWriteLock().unlock();
			}
			refreshPkgs();
		}
		fRPlatform = rPlatform;
	}
	
	
	@Override
	public boolean requiresUpdate() {
		final long stamp = System.currentTimeMillis();
		if (Math.abs(fMirrorsStamp - stamp) > MIRROR_CHECK) {
			getWriteLock().lock();
			try {
				fRequireLoad |= (REQUIRE_CRAN | REQUIRE_BIOC);
				return true;
			}
			finally {
				getWriteLock().unlock();
			}
		}
		if ((fRequireLoad & (REQUIRE_REPOS | REQUIRE_CRAN | REQUIRE_BIOC)) != 0) {
			return true;
		}
		final IStatus status = getReposStatus(null);
		if (!status.isOK()) {
			return false;
		}
		
		if (Math.abs(fPkgsStamp - stamp) > MIRROR_CHECK) {
			getWriteLock().lock();
			try {
				fRequireLoad |= (REQUIRE_REPO_PKGS);
				return true;
			}
			finally {
				getWriteLock().unlock();
			}
		}
		if ((fRequireLoad & (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS)) != 0) {
			return true;
		}
		return false;
	}
	
	
	@Override
	public IStatus getReposStatus(final ISelectedRepos repos) {
		final ISelectedRepos current = fSelectedRepos;
		final int confirm = fRequireConfirm;
		return getReposStatus((repos != null) ? repos : current, current, confirm);
	}
	
	private IStatus getReposStatus(final ISelectedRepos repos, final ISelectedRepos current, final int confirm) {
		if (repos.getRepos().isEmpty()) {
			return createStatus(IStatus.ERROR, "No repository is selected. Select the repositories where to install R packages from.");
		}
		
		final boolean requireCRAN = RVarRepo.requireCRANMirror(repos.getRepos());
		if (requireCRAN && repos.getCRANMirror() == null) {
			return createStatus(IStatus.ERROR, "No CRAN mirror is selected. Selected a mirror for CRAN.");
		}
		final boolean requireBioC = RVarRepo.requireBioCMirror(repos.getRepos());
		if (requireBioC && repos.getBioCMirror() == null) {
			return createStatus(IStatus.ERROR, "No BioC mirror is selected. Selected a mirror for Bioconductor.");
		}
		
		if ((requireCRAN && (confirm & REQUIRE_CRAN) != 0)
				|| (requireBioC && (confirm & REQUIRE_BIOC) != 0)
				|| (repos != current && !repos.equals(current) )) {
			return createStatus(IStatus.INFO, "Check the repository settings and confirm with 'Apply' to show the available R packages.");
		}
		
		return Status.OK_STATUS;
	}
	
	private static IStatus createStatus(final int severity, final String message) {
		return new Status(severity, RCore.PLUGIN_ID, message);
	}
	
	@Override
	public void update(final RService r, final IProgressMonitor monitor) throws CoreException {
		beginRTask((IToolService) r, monitor);
		try {
			checkInit(0, r, monitor);
			
			fRTaskEvent = new Change(fREnv);
			
			final ISelectedRepos settings = runLoadRepos(r, monitor);
			if (settings != null) {
				runApplyRepo(settings, r, monitor);
				runLoadPkgs(settings, r, monitor);
			}
			fireUpdate(fRTaskEvent);
		}
		finally {
			fRTaskEvent = null;
			endRTask();
		}
	}
	
	
	private void checkMirrors(final Change event) {
		if ((fRequireLoad & (REQUIRE_CRAN | REQUIRE_BIOC)) != 0) {
			return;
		}
		
		SelectedRepos selected = fSelectedRepos;
		fAllCRAN = ConstList.concat(fCustomCRAN, fRCRAN);
		
		RRepo selectedCRAN = selected.getCRANMirror();
		if (selected.getCRANMirror() != null) {
			selectedCRAN = Util.findRepo(fAllCRAN, selectedCRAN);
		}
		else if (fFirstTime && fSelectedCRANInR != null) {
			selectedCRAN = Util.getRepoByURL(fAllCRAN, fSelectedCRANInR);
		}
		if (selectedCRAN == null) {
			fRequireConfirm |= REQUIRE_CRAN;
			selectedCRAN = PreferencesUtil.getInstancePrefs().getPreferenceValue(LAST_CRAN_PREF);
			if (selectedCRAN != null) {
				selectedCRAN = Util.findRepo(fAllCRAN, selectedCRAN);
			}
			if (!fCustomCRAN.isEmpty()
					&& (selectedCRAN == null || !selectedCRAN.getId().startsWith(RRepo.CUSTOM_PREFIX)) ) {
				selectedCRAN = fCustomCRAN.get(0);
			}
			if (fFirstTime && selectedCRAN == null) {
				selectedCRAN = fRCRANByCountry;
			}
		}
		
		
		RRepo selectedBioC = selected.getBioCMirror();
		fAllBioC = ConstList.concat(fCustomBioC, fRBioC);
		if (selectedBioC != null) {
			selectedBioC = Util.findRepo(fAllBioC, selectedBioC);
		}
		else if (fFirstTime && fSelectedBioCInR != null) {
			selectedBioC = RPkgUtil.getRepoByURL(fAllBioC, fSelectedBioCInR);
		}
		if (selectedBioC == null) {
			fRequireConfirm |= REQUIRE_BIOC;
			selectedBioC = PreferencesUtil.getInstancePrefs().getPreferenceValue(LAST_BIOC_PREF);
			if (!fCustomBioC.isEmpty()
					&& (selectedBioC == null || !selectedBioC.getId().startsWith(RRepo.CUSTOM_PREFIX)) ) {
				selectedBioC = fCustomBioC.get(0);
			}
			if (fFirstTime && selectedBioC == null) {
				selectedBioC = Util.getRepoByURL(fAllBioC, "http://www.bioconductor.org"); //$NON-NLS-1$
			}
		}
		
		selected = new SelectedRepos(
				selected.getRepos(),
				selectedCRAN,
				fBioCVersion,
				selectedBioC );
		if ((fRequireLoad & (REQUIRE_REPOS)) == 0) {
			for (final RRepo repo : fAllRepos) {
				if (repo instanceof RVarRepo) {
					((RVarRepo) repo).updateURL(selected);
				}
			}
		}
		fSelectedRepos = selected;
		
		event.fPkgs = 1;
	}
	
	private void checkRepos(final Change event) {
		if ((fRequireLoad & (REQUIRE_CRAN | REQUIRE_BIOC | REQUIRE_REPOS)) != 0) {
			return;
		}
		
		SelectedRepos selected = fSelectedRepos;
		
		fAllRepos = new ArrayList<RRepo>(fCustomRepos.size() + fAddRepos.size() + fRRepos.size());
		fAllRepos.addAll(fCustomRepos);
		fAllRepos.addAll(fAddRepos);
		for (final RRepo repo : fAllRepos) {
			if (repo instanceof RVarRepo) {
				((RVarRepo) repo).updateURL(selected);
			}
		}
		for (final RRepo repo : fRRepos) {
			if (repo instanceof RVarRepo) {
				((RVarRepo) repo).updateURL(selected);
			}
		}
		for (final RRepo repo : fRRepos) {
			if (!repo.getId().isEmpty()) {
				if (RPkgUtil.getRepoById(fAllRepos, repo.getId()) == null) {
					fAllRepos.add(repo);
				}
			}
			else {
				if (Util.getRepoByURL(fAllRepos, repo) == null) {
					fAllRepos.add(RVarRepo.create(RRepo.R_PREFIX + repo.getURL(), repo.getName(),
							repo.getURL(), null ));
				}
			}
		}
		
		{	final Collection<RRepo> selectedRepos = selected.getRepos();
			final Collection<RRepo> previous = (fFirstTime && selectedRepos.isEmpty()) ?
					fSelectedReposInR : selectedRepos;
			final List<RRepo> repos = new ArrayList<RRepo>(previous.size());
			for (RRepo repo : previous) {
				repo = Util.findRepo(fAllRepos, repo);
				if (repo != null) {
					repos.add(repo);
				}
			}
			selected = new SelectedRepos(
					repos,
					selected.getCRANMirror(),
					selected.getBioCVersion(),
					selected.getBioCMirror() );
			fSelectedRepos = selected;
		}
		
		fRequireLoad |= REQUIRE_REPO_PKGS;
		
		event.fRepos = 1;
	}
	
	
	private void loadPrefs(final boolean custom) {
		final Change event = new Change(fREnv);
		final IPreferenceAccess prefs = PreferencesUtil.getInstancePrefs();
		getWriteLock().lock();
		try {
			if (custom) {
				fCustomRepos = prefs.getPreferenceValue(CUSTOM_REPO_PREF);
				fCustomCRAN = prefs.getPreferenceValue(CUSTOM_CRAN_MIRROR_PREF);
				fCustomBioC = prefs.getPreferenceValue(CUSTOM_BIOC_MIRROR_PREF);
				
				checkRepos(event);
			}
		}
		finally {
			getWriteLock().unlock();
		}
		fireUpdate(event);
	}
	
	
	@Override
	public void addListener(final Listener listener) {
		fListeners.add(listener);
	}
	
	@Override
	public void removeListener(final Listener listener) {
		fListeners.remove(listener);
	}
	
	private void fireUpdate(final Event event) {
		if (event.reposChanged() == 0 && event.pkgsChanged() == 0 && event.viewsChanged() == 0) {
			return;
		}
		final Listener[] listeners = fListeners.toArray();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleChange(event);
		}
	}
	
	
	@Override
	public List<RRepo> getAvailableRepos() {
		return fAllRepos;
	}
	
	@Override
	public ISelectedRepos getSelectedRepos() {
		return fSelectedRepos;
	}
	
	@Override
	public void setSelectedRepos(final ISelectedRepos repos) {
		List<RRepo> selectedRepos;
		{	final Collection<RRepo> selected = repos.getRepos();
			selectedRepos = new ArrayList<RRepo>(selected.size());
			for (final RRepo repo : fAllRepos) {
				if (selected.contains(repo)) {
					selectedRepos.add(repo);
				}
			}
		}
		RRepo selectedCRAN;
		{	final RRepo repo = repos.getCRANMirror();
			selectedCRAN = (repo != null) ? Util.findRepo(fAllCRAN, repo) : null;
			fRequireConfirm &= ~REQUIRE_CRAN;
		}
		RRepo selectedBioC;
		{	final RRepo repo = repos.getBioCMirror();
			selectedBioC = (repo != null) ? Util.findRepo(fAllBioC, repo) : null;
			fRequireConfirm &= ~REQUIRE_BIOC;
		}
		
		final SelectedRepos previousSettings = fSelectedRepos;
		final SelectedRepos newSettings = new SelectedRepos(
				selectedRepos,
				selectedCRAN,
				previousSettings.getBioCVersion(), selectedBioC );
		for (final RRepo repo : fAllRepos) {
			if (repo instanceof RVarRepo) {
				((RVarRepo) repo).updateURL(newSettings);
			}
		}
		fSelectedRepos = newSettings;
		savePrefs(newSettings);
		
		if (!newSettings.equals(previousSettings)) {
			fRequireLoad |= (REQUIRE_REPO_PKGS);
			
			final Change event = new Change(fREnv);
			event.fRepos = 1;
			fireUpdate(event);
		}
	}
	
	@Override
	public RRepo getRepo(final String repoId) {
		if (repoId.isEmpty()) {
			return null;
		}
		RRepo repo = fSelectedRepos.getRepo(repoId);
		if (repo == null) {
			repo = RPkgUtil.getRepoById(fAllRepos, repoId);
		}
		return repo;
	}
	
	private void savePrefs(final SelectedRepos repos) {
		if (fREnv.getConfig() == null) {
			return;
		}
		final IScopeContext prefs = InstanceScope.INSTANCE;
		
		final IEclipsePreferences node = prefs.getNode(fSelectedReposPref.getQualifier());
		
		PreferencesUtil.setPrefValue(node, fSelectedReposPref, repos.getRepos());
		PreferencesUtil.setPrefValue(node, fSelectedCRANPref, repos.getCRANMirror());
		PreferencesUtil.setPrefValue(node, fBioCVersionPref, repos.getBioCVersion());
		PreferencesUtil.setPrefValue(node, fSelectedBioCPref, repos.getBioCMirror());
		
		if (repos.getCRANMirror() != null) {
			PreferencesUtil.setPrefValue(prefs, LAST_CRAN_PREF, repos.getCRANMirror());
		}
		if (repos.getBioCMirror() != null) {
			PreferencesUtil.setPrefValue(prefs, LAST_BIOC_PREF, repos.getBioCMirror());
		}
		
		try {
			node.flush();
		}
		catch (final BackingStoreException e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when saving the R package manager preferences.", e ));
		}
	}
	
	@Override
	public void refreshPkgs() {
		fRequireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
	}
	
	
	@Override
	public List<RRepo> getAvailableCRANMirrors() {
		return fAllCRAN;
	}
	
	@Override
	public List<RRepo> getAvailableBioCMirrors() {
		return fAllBioC;
	}
	
	@Override
	public IRLibPaths getRLibPaths() {
		return fRLibPaths;
	}
	
	public REnvLibGroups getRLibGroups() {
		if (fRLibGroups != null) {
			return fRLibGroups;
		}
		final IREnvConfiguration config = fREnv.getConfig();
		if (config != null) {
			return new REnvLibGroups(config);
		}
		return null;
	}
	
	@Override
	public IRPkgSet getRPkgSet() {
		return (fPkgsExt != null) ? fPkgsExt : fPkgsLight;
	}
	
	@Override
	public IRPkgSet.Ext getExtRPkgSet() {
		return fPkgsExt;
	}
	
	@Override
	public List<? extends IRView> getRViews() {
		return fRViews;
	}
	
//	@Override
//	public List<? extends IRView> getBioCViews() {
//		return fBioCViews;
//	}
	
	
	@Override
	public void apply(final ITool process) {
		process.getQueue().add(new AbstractRToolRunnable("r/renv/rpkg.apply", //$NON-NLS-1$
				"Perform Package Manager Operations") {
			@Override
			protected void run(final IRToolService r, final IProgressMonitor monitor) throws CoreException {
				runApply(r, monitor);
			}
		});
	}
	
	
	private boolean beginRTaskSilent(final IToolService r,
			final IProgressMonitor monitor) {
		synchronized (this) {
			if (fRProcess != null) {
				return false;
			}
			fRProcess = r.getTool();
			fRTask = 1;
			return true;
		}
	}
	
	private void beginRTask(final IToolService r,
			final IProgressMonitor monitor) throws CoreException {
		synchronized (this) {
			while (fRProcess != null) {
				if (fRTask == 1) {
					monitor.subTask("Waiting for package check...");
					try {
						wait();
					}
					catch (final InterruptedException e) {
						if (monitor.isCanceled()) {
							throw new CoreException(Status.CANCEL_STATUS);
						}
					}
				}
				else {
					final Status status = new Status(IStatus.ERROR, RCore.PLUGIN_ID,
							NLS.bind("Another package manager task for ''{0}'' is already running in ''{0}''", 
									fREnv.getName(), fRProcess.getLabel(ITool.DEFAULT_LABEL) ));
	//				r.handleStatus(status, monitor);
					throw new CoreException(status);
				}
			}
			fRProcess = r.getTool();
			fRTask = 2;
		}
	}
	
	private void endRTask() {
		synchronized (this) {
			fRProcess = null;
			fRTask = 0;
			notifyAll();
		}
	}
	
	protected void runApply(final RService r, final IProgressMonitor monitor)
			throws CoreException {
		beginRTask((IToolService) r, monitor);
		try {
			final ISelectedRepos selectedRepos;
			getReadLock().lock();
			try {
				selectedRepos = fSelectedRepos;
			}
			finally {
				getReadLock().unlock();
			}
			
			if (getReposStatus(selectedRepos).getSeverity() != IStatus.ERROR) {
				fRTaskEvent = new Change(fREnv);
				
				runApplyRepo(selectedRepos, r, monitor);
				runLoadPkgs(selectedRepos, r, monitor);
				
				fireUpdate(fRTaskEvent);
			}
		}
		finally {
			fRTaskEvent = null;
			endRTask();
		}
	}
	
	private ISelectedRepos runLoadRepos(final RService r,
			final IProgressMonitor monitor) throws CoreException {
		try {
			String bioCVersion;
			{	final RObject data = r.evalData("as.character(tools:::.BioC_version_associated_with_R_version)", monitor);
				bioCVersion = RDataUtil.checkSingleCharValue(data);
			}
			
			final boolean loadRMirrors = ((fRequireLoad & (REQUIRE_CRAN | REQUIRE_BIOC)) != 0);
			final boolean loadRRepos = ((fRequireLoad & (REQUIRE_REPOS)) != 0);
			
			List<RRepo> rCRAN = null;
			List<RRepo> rBioC = null;
			String selectedCRAN = null;
			RRepo rCRANByCountry = null;
			String selectedBioC = null;
			if (loadRMirrors) {
				monitor.subTask("Fetching available mirrors...");
				final String region = Locale.getDefault().getCountry().toLowerCase();
				{	final RObject data = r.evalData("getCRANmirrors()[c('Name', 'URL', 'CountryCode')]", monitor); //$NON-NLS-1$
					final RDataFrame df = RDataUtil.checkRDataFrame(data);
					final RCharacterStore names = RDataUtil.checkRCharVector(df.get("Name")).getData(); //$NON-NLS-1$
					final RCharacterStore urls = RDataUtil.checkRCharVector(df.get("URL")).getData(); //$NON-NLS-1$
					final RCharacterStore regions = RDataUtil.checkRCharVector(df.get("CountryCode")).getData(); //$NON-NLS-1$
					
					final int l = RDataUtil.checkIntLength(names);
					rCRAN = new ArrayList<RRepo>(l);
					for (int i = 0; i < l; i++) {
						final String url = Util.checkURL(urls.getChar(i));
						if (!url.isEmpty()) {
							final RRepo repo = new RRepo(RRepo.R_PREFIX + url, names.getChar(i),
									url, null );
							rCRAN.add(repo);
							if (rCRANByCountry == null && !region.isEmpty()
									&& region.equals(regions.getChar(i))) {
								rCRANByCountry = repo;
							}
						}
					}
				}
				
				{	final String[][] fix = new String[][] {
							{ "Seattle (USA)", "http://www.bioconductor.org" },
							{ "Bethesda (USA)", "http://watson.nci.nih.gov/bioc_mirror" },
							{ "Dortmund (Germany)", "http://bioconductor.statistik.tu-dortmund.de" },
							{ "Bergen (Norway)", "http://bioconductor.uib.no" },
							{ "Cambridge (UK)", "http://mirrors.ebi.ac.uk/bioconductor" }
					};
					rBioC = new ArrayList<RRepo>(fix.length);
					for (int i = 0; i < fix.length; i++) {
						final String url = Util.checkURL(fix[i][1]);
						if (!url.isEmpty()) {
							rBioC.add(new RRepo(RRepo.R_PREFIX + url, fix[i][0], url, null));
						}
					}
				}
			}
			
			List<RRepo> rrepos = null;
			List<RRepo> selected = null;
			if (loadRRepos) {
				monitor.subTask("Fetching available repositories...");
				{	final RObject data = r.evalData("options('repos')[[1L]]", monitor); //$NON-NLS-1$
					if (data.getRObjectType() != RObject.TYPE_NULL) {
						final RCharacterStore urls = RDataUtil.checkRCharVector(data).getData();
						final RStore ids = ((RVector<?>) data).getNames();
						
						final int l = RDataUtil.checkIntLength(urls);
						selected = new ArrayList<RRepo>(l);
						for (int i = 0; i < l; i++) {
							final String id = (ids != null) ? ids.getChar(i) : null;
							final String url = urls.getChar(i);
							
							final RRepo repo = Util.createRepoFromR(id, null, url);
							if (repo != null) {
								selected.add(repo);
							}
						}
					}
					else {
						selected = new ArrayList<RRepo>(4);
					}
				}
				
				final RObject data = r.evalData("local({" + //$NON-NLS-1$
						"p <- file.path(Sys.getenv('HOME'), '.R', 'repositories')\n" + //$NON-NLS-1$
						"if (!file.exists(p)) p <- file.path(R.home('etc'), 'repositories')\n" + //$NON-NLS-1$
						"r <- utils::read.delim(p, header = TRUE, comment.char = '#', colClasses = c(rep.int('character', 3L), rep.int('logical', 4L)))\n" + //$NON-NLS-1$
						"r[c(names(r)[1L], 'URL', 'default')]\n" + //$NON-NLS-1$
				"})", monitor); //$NON-NLS-1$
				final RDataFrame df = RDataUtil.checkRDataFrame(data);
				final RStore ids = df.getRowNames();
				final RCharacterStore labels = RDataUtil.checkRCharVector(df.get(0)).getData();
				final RCharacterStore urls = RDataUtil.checkRCharVector(df.get("URL")).getData(); //$NON-NLS-1$
				final RLogicalStore isDefault = (selected.isEmpty()) ?
						RDataUtil.checkRLogiVector(df.get("default")).getData() : null; //$NON-NLS-1$
				
				{	final int l = RDataUtil.checkIntLength(labels);
					rrepos = new ArrayList<RRepo>(l + 4);
					for (int i = 0; i < l; i++) {
						final String id = (ids != null) ? ids.getChar(i) : null;
						final String url = urls.getChar(i);
						
						final RRepo repo = Util.createRepoFromR(id, labels.getChar(i), url);
						if (repo != null) {
							rrepos.add(repo);
							if (isDefault != null && isDefault.getLogi(i)) {
								selected.add(repo);
							}
						}
					}
				}
				
				for (int i = 0; i < selected.size(); i++) {
					final RRepo repo = selected.get(i);
					RRepo rrepo = null;
					if (!repo.getURL().isEmpty()) {
						rrepo = RPkgUtil.getRepoByURL(rrepos, repo.getURL());
					}
					if (rrepo != null) {
						selected.set(i, rrepo);
						continue;
					}
					if (!repo.getId().isEmpty()) {
						final int j = rrepos.indexOf(repo); // by id
						if (j >= 0) {
							rrepo = rrepos.get(j);
							if (!RVarRepo.hasVars(rrepo.getURL())) {
								rrepo.setURL(repo.getURL());
							}
							selected.set(i, rrepo);
							continue;
						}
					}
					repo.setName(RRepo.hintName(repo));
					continue;
				}
			}
			
			if (loadRMirrors) {
				if (loadRRepos) {
					final RRepo repo = RPkgUtil.getRepoById(rrepos, RRepo.CRAN_ID);
					if (repo != null && !repo.getURL().isEmpty()
							&& !RVarRepo.hasVars(repo.getURL()) ) {
						selectedCRAN = repo.getURL();
					}
				}
				else {
					final RObject data = r.evalData("options('repos')[[1L]]['CRAN']", monitor); //$NON-NLS-1$
					if (data.getRObjectType() != RObject.TYPE_NULL) {
						final String url = Util.checkURL(RDataUtil.checkSingleChar(data));
						if (!url.isEmpty() && !RVarRepo.hasVars(url)) {
							selectedCRAN = url;
						}
					}
				}
				{	final RObject data = r.evalData("options('BioC_mirror')[[1L]]", monitor); //$NON-NLS-1$
					if (data.getRObjectType() != RObject.TYPE_NULL) {
						selectedBioC = RDataUtil.checkSingleChar(data);
					}
				}
			}
			
			getWriteLock().lock();
			try {
				fBioCVersion = bioCVersion;
				
				if (loadRMirrors) {
					fRequireLoad &= ~(REQUIRE_CRAN | REQUIRE_BIOC);
					fRCRAN = rCRAN;
					fRCRANByCountry = rCRANByCountry;
					fSelectedCRANInR = selectedCRAN;
					fRBioC = rBioC;
					fSelectedBioCInR = selectedBioC;
					fMirrorsStamp = fRTaskEvent.fStamp;
					
					checkMirrors(fRTaskEvent);
				}
				if (loadRRepos) {
					fRequireLoad &= ~(REQUIRE_REPOS);
					fRRepos = rrepos;
					fSelectedReposInR = selected;
					
					checkRepos(fRTaskEvent);
				}
				fFirstTime = false;
				
				if (getReposStatus(fSelectedRepos, fSelectedRepos, fRequireConfirm).isOK()) {
					return fSelectedRepos;
				}
				return null;
			}
			finally {
				getWriteLock().unlock();
			}
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
					"An error occurred when loading data for package manager.", e));
		}
	}
	
	private void runApplyRepo(final ISelectedRepos repos, final RService r,
			final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Setting repository configuration...");
		Exception error = null;
		
		try {
			if (repos.getBioCMirror() != null) {
				final FunctionCall call = r.createFunctionCall("options");
				call.addChar("BioC_mirror", repos.getBioCMirror().getURL());
				call.evalVoid(monitor);
			}
			{	final List<RRepo> selectedRepos = (List<RRepo>) repos.getRepos();
				final String[] ids = new String[selectedRepos.size()];
				final String[] urls = new String[selectedRepos.size()];
				for (int i = 0; i < urls.length; i++) {
					final RRepo repo = selectedRepos.get(i);
					ids[i] = repo.getId();
					urls[i] = repo.getURL();
				}
				final RVector<RCharacterStore> data = new RVectorImpl<RCharacterStore>(
						new RCharacterDataImpl(urls), RObject.CLASSNAME_CHARACTER, ids);
				
				final FunctionCall call = r.createFunctionCall("options");
				call.add("repos", data);
				call.evalVoid(monitor);
			}
		}
		catch (final CoreException e) {
			error = e;
		}
		if (error != null) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when setting repository configuration in R.", error));
		}
	}
	
	private void runLoadPkgs(final ISelectedRepos repoSettings, final RService r,
			final IProgressMonitor monitor) throws CoreException {
		final boolean loadRepoPkgs = (fRequireLoad & (REQUIRE_REPO_PKGS)) != 0
				&& getReposStatus(repoSettings).isOK();
		final boolean loadInstPkgs = ((fRequireLoad & (REQUIRE_INST_PKGS)) != 0);
		
		FullRPkgSet pkgs = null;
		if (loadRepoPkgs) {
			fRTaskEvent.fOldPkgs = getRPkgSet();
			fRTaskEvent.fNewPkgs = pkgs = fPkgScanner.loadAvailable(repoSettings, r, monitor);
		}
		
		if (loadInstPkgs) {
			if (pkgs == null) {
				if (fPkgsExt != null) {
					pkgs = fPkgsExt.cloneAvailable();
				}
				else {
					pkgs = new FullRPkgSet(0);
				}
			}
			checkInstalled(pkgs, r, monitor);
			
			updateRViews(repoSettings, pkgs, r, monitor);
		}
		
		if (pkgs != null) {
			getWriteLock().lock();
			try {
				setPkgs();
				
				if (loadRepoPkgs) {
					fRequireLoad &= ~REQUIRE_REPO_PKGS;
					fPkgsStamp = fRTaskEvent.fStamp;
					fRTaskEvent.fPkgs |= AVAILABLE;
				}
				if (loadInstPkgs) {
					fRequireLoad &= ~REQUIRE_INST_PKGS;
				}
			}
			finally {
				getWriteLock().unlock();
			}
		}
	}
	
	
	private void checkInstalled(final FullRPkgSet pkgs,
			final RService r, final IProgressMonitor monitor)
			throws CoreException {
		RVector<RNumericStore> libs = null;
		boolean[] update = null;
		
		Exception error = null;
		try {
			libs = RDataUtil.checkRNumVector(r.evalData(
					"rj:::.renv.checkLibs()", monitor )); //$NON-NLS-1$
			
			final int l = RDataUtil.checkIntLength(libs.getData());
			ITER_LIBS: for (int idxLib = 0; idxLib < l; idxLib++) {
				final String libPath = libs.getNames().getChar(idxLib);
				if (fLibs != null) {
					final int idx = (int) fLibs.getNames().indexOf(libPath);
					if (idx >= 0) {
						if (fLibs.getData().getNum(idx) == libs.getData().getNum(idxLib)) {
							continue ITER_LIBS;
						}
					}
				}
				if (update == null) {
					update = new boolean[l];
				}
				update[idxLib] = true;
			}
			
			fLibs = libs;
		}
		catch (final UnexpectedRDataException e) {
			error = e;
		}
		catch (final CoreException e) {
			error = e;
		}
		if (error != null) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when checking for changed R libraries.", error ));
		}
		
		if (update != null || pkgs != null) {
			if (fRTaskEvent == null) {
				fRTaskEvent = new Change(fREnv);
			}
			if (fRTaskEvent.fOldPkgs == null) {
				fRTaskEvent.fOldPkgs = getRPkgSet();
			}
			if (fPkgsExt != null || pkgs != null) {
				final FullRPkgSet newPkgs = (pkgs != null) ? pkgs : fPkgsExt.cloneAvailable();
				fRTaskEvent.fNewPkgs = newPkgs;
				fRLibPaths = RLibPaths.create(getRLibGroups(), libs, r, monitor);
				fPkgScanner.updateInstFull(fRLibPaths, update, newPkgs, fRTaskEvent, r, monitor);
			}
			else {
				final RPkgSet newPkgs = new RPkgSet((int) libs.getLength());
				fRTaskEvent.fNewPkgs = newPkgs;
				fPkgScanner.updateInstLight(libs, update, newPkgs, fRTaskEvent, getRLibGroups(),
						r, monitor);
			}
			
			if (fRTaskEvent.fInstalledPkgs != null && fRTaskEvent.fInstalledPkgs.fNames.isEmpty()) {
				fRTaskEvent.fInstalledPkgs = null;
			}
			if (pkgs == null) {
				setPkgs();
			}
			if (fRTaskEvent.fInstalledPkgs != null && fDB != null) {
				fDB.updatePkgs(fRTaskEvent);
			}
		}
	}
	
	private void setPkgs() {
		final Change event = fRTaskEvent;
		if (event.fNewPkgs instanceof FullRPkgSet) {
			fPkgsExt = (FullRPkgSet) event.fNewPkgs;
			fPkgsLight = null;
		}
		else if (event.fNewPkgs instanceof RPkgSet) {
			fPkgsExt = null;
			fPkgsLight = (RPkgSet) event.fNewPkgs;
		}
		if (event.fInstalledPkgs != null) {
			event.fPkgs |= INSTALLED;
		}
	}
	
	private void updateRViews(final ISelectedRepos repoSettings, final FullRPkgSet pkgs,
			final RService r, final IProgressMonitor monitor) {
		final RPkgDescription pkg = pkgs.getInstalled().getFirstByName("ctv"); //$NON-NLS-1$
		if (pkg == null || pkg.getVersion().equals(fRViewsVersion)) {
			return;
		}
		final List<RView> rViews = RViewTasks.loadRViews(r, monitor);
		if (rViews != null) {
			fRViews = rViews;
			fRViewsVersion = pkg.getVersion();
			fRTaskEvent.fViews = 1;
		}
	}
	
	
	@Override
	public IRPkgData addToCache(final IFileStore store, final IProgressMonitor monitor) throws CoreException {
		final IRPkg pkg = RPkgUtil.checkPkgFileName(store.getName());
		final RPkgType type = RPkgUtil.checkPkgType(store.getName(), fRPlatform);
		fCache.add(pkg.getName(), type, store, monitor);
		return new RPkgData(pkg.getName(), RNumVersion.NONE, RRepo.WS_CACHE_PREFIX + type.name().toLowerCase());
	}
	
	
	@Override
	public void perform(final ITool rTool, final List<? extends RPkgAction> actions) {
		if (actions.isEmpty()) {
			return;
		}
		final String label = (actions.get(0).getAction() == RPkgAction.UNINSTALL) ?
				"Uninstall R Packages" : "Install/Update R Packages";
		final RPkgOperator op = new RPkgOperator(this);
		rTool.getQueue().add(new AbstractStatetRRunnable("r/renv/pkgs.inst", label) { //$NON-NLS-1$
			@Override
			protected void run(final IRConsoleService r, final IProgressMonitor monitor) throws CoreException {
				Exception error = null;
				beginRTask(r, monitor);
				try {
					checkNewCommand(r, monitor);
					op.runActions(actions, r, monitor);
				}
				catch (final UnexpectedRDataException e) {
					error = e;
				}
				catch (final CoreException e) {
					error = e;
				}
				finally {
					endRTask();
					
					r.briefAboutChange(0x10); // packages
				}
				if (error != null) {
					throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
							"An error occurred when installing and updating R packages.", error ));
				}
			}
		});
	}
	
	@Override
	public void loadPkgs(final ITool rTool, final List<? extends IRPkgDescription> pkgs,
			final boolean expliciteLocation) {
		final RPkgOperator op = new RPkgOperator(this);
		rTool.getQueue().add(new AbstractStatetRRunnable("r/renv/pkgs.load", //$NON-NLS-1$
				"Load R Packages") {
			@Override
			protected void run(final IRConsoleService r, final IProgressMonitor monitor) throws CoreException {
				checkNewCommand(r, monitor);
				op.loadPkgs(pkgs, expliciteLocation, r, monitor);
			}
		});
	}
	
}
