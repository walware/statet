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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImIdentitySet;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.FastList;
import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.preferences.core.IPreferenceSetService;
import de.walware.ecommons.preferences.core.IPreferenceSetService.IChangeEvent;
import de.walware.ecommons.preferences.core.Preference.StringPref2;
import de.walware.ecommons.preferences.core.util.PreferenceUtils;
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
import de.walware.statet.r.core.pkgmanager.IRPkgInfoAndData;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.pkgmanager.IRView;
import de.walware.statet.r.core.pkgmanager.ISelectedRepos;
import de.walware.statet.r.core.pkgmanager.RPkgAction;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.pkgmanager.RRepo;
import de.walware.statet.r.core.pkgmanager.RRepoMirror;
import de.walware.statet.r.core.pkgmanager.SelectedRepos;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.tool.AbstractStatetRRunnable;
import de.walware.statet.r.core.tool.IRConsoleService;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.renv.REnvConfiguration;


public class RPkgManager implements IRPkgManager.Ext, IPreferenceSetService.IChangeListener {
	
	
	private static final int REQUIRE_CRAN=                  0x0_1000_0000;
	private static final int REQUIRE_BIOC=                  0x0_2000_0000;
	private static final int REQUIRE_REPOS=                 0x0_8000_0000;
	
	private static final int REQUIRE_REPO_PKGS=             0x0_0100_0000;
	private static final int REQUIRE_INST_PKGS=             0x0_0800_0000;
	
	private static final ImIdentitySet<String> PREF_QUALIFIERS= ImCollections.newIdentitySet(
			PREF_QUALIFIER );
	
	private static final RRepoPref LAST_CRAN_PREF= new RRepoPref(PREF_QUALIFIER, "LastCRAN.repo"); //$NON-NLS-1$
	private static final RRepoPref LAST_BIOC_PREF= new RRepoPref(PREF_QUALIFIER, "LastBioC.repo"); //$NON-NLS-1$
	
	private static final int MIRROR_CHECK= 1000 * 60 * 60 * 6;
	private static final int PKG_CHECK= 1000 * 60 * 60 * 3;
	
	
	private final IREnv rEnv;
	private RPlatform rPlatform;
	
	private final IPreferenceAccess prefAccess;
	
	private final IFileStore rEnvDirectory;
	
	private boolean firstTime;
	
	private String bioCVersion;
	private final StringPref2 bioCVersionPref;
	
	private List<RRepo> customRepos;
	private final List<RRepo> addRepos;
	private List<RRepo> rRepos;
	private List<RRepo> allRepos;
	private List<RRepo> selectedReposInR;
	private final RRepoListPref selectedReposPref;
	
	private List<RRepo> customCRAN;
	private List<RRepoMirror> rCRANMirrors;
	private ImList<RRepo> allCRAN;
	private String selectedCRANInR;
	private final RRepoPref selectedCRANPref;
	
	private List<RRepo> customBioC;
	private List<RRepoMirror> rBioCMirrors;
	private ImList<RRepo> allBioC;
	private String selectedBioCInR;
	private final RRepoPref selectedBioCPref;
	private long mirrorsStamp;
	
	private SelectedRepos selectedRepos;
	
	private RVector<RNumericStore> libs= null;
	private REnvLibGroups rLibGroups;
	private RLibPaths rLibPaths;
	
	private RPkgSet pkgsLight;
	private FullRPkgSet pkgsExt;
	private long pkgsStamp;
	final RPkgScanner pkgScanner= new RPkgScanner();
	
	private volatile int requireLoad;
	private volatile int requireConfirm;
	
	private final FastList<Listener> listeners= new FastList<>(Listener.class);
	
	private final ReentrantReadWriteLock lock= new ReentrantReadWriteLock();
	
	private List<RView> rViews;
	private RNumVersion rViewsVersion;
//	private List<RView> bioCViews;
//	private String bioCViewsVersion;
//	private long bioCViewsStamp;
	
	private ITool rProcess;
	private int rTask;
	private Change rTaskEvent;
	
	private DB db;
	private final Cache cache;
	
	
	public RPkgManager(final IREnvConfiguration rConfig) {
		this.rEnv= rConfig.getReference();
		this.rEnvDirectory= EFS.getLocalFileSystem().getStore(REnvConfiguration.getStateLocation(this.rEnv));
		final String qualifier= ((AbstractPreferencesModelObject) rConfig).getNodeQualifiers()[0];
		this.selectedReposPref= new RRepoListPref(qualifier, "RPkg.Repos.repos"); //$NON-NLS-1$
		this.selectedCRANPref= new RRepoPref(qualifier, "RPkg.CRANMirror.repo"); //$NON-NLS-1$
		this.bioCVersionPref= new StringPref2(qualifier, "RPkg.BioCVersion.ver"); //$NON-NLS-1$
		this.selectedBioCPref= new RRepoPref(qualifier, "RPkg.BioCMirror.repo"); //$NON-NLS-1$
		
		this.prefAccess= PreferenceUtils.getInstancePrefs();
		this.addRepos= new ArrayList<>();
		if (rConfig.getType() == IREnvConfiguration.USER_LOCAL_TYPE) {
			final String rjVersion= "" + ServerUtil.RJ_VERSION[0] + '.' + ServerUtil.RJ_VERSION[1]; //$NON-NLS-1$
			this.addRepos.add(new RRepo(RRepo.SPECIAL_PREFIX+"rj", "RJ", "http://download.walware.de/rj-" + rjVersion, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		this.selectedRepos= new SelectedRepos(
				this.prefAccess.getPreferenceValue(this.selectedReposPref),
				this.prefAccess.getPreferenceValue(this.selectedCRANPref),
				this.prefAccess.getPreferenceValue(this.bioCVersionPref),
				this.prefAccess.getPreferenceValue(this.selectedBioCPref) );
		
		this.db= DB.create(this.rEnv, this.rEnvDirectory);
		this.cache= new Cache(this.rEnvDirectory);
		resetPkgs(rConfig);
		
		this.firstTime= true;
		this.mirrorsStamp= this.pkgsStamp= System.currentTimeMillis();
		this.requireLoad |= (REQUIRE_CRAN | REQUIRE_BIOC | REQUIRE_REPOS);
		this.requireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
		
		this.prefAccess.addPreferenceSetListener(this, PREF_QUALIFIERS);
		
		getWriteLock().lock();
		try {
			loadPrefs(true);
		}
		finally {
			getWriteLock().unlock();
		}
	}
	
	private void resetPkgs(final IREnvConfiguration config) {
		this.pkgsExt= null;
		if (this.db != null && config != null) {
			this.pkgsLight= this.db.loadPkgs(config.getRLibraryGroups());
		}
		if (this.pkgsLight == null) {
			this.db= null;
			this.pkgsLight= new RPkgSet(0);
		}
	}
	
	
	@Override
	public IREnv getREnv() {
		return this.rEnv;
	}
	
	Cache getCache() {
		return this.cache;
	}
	
	@Override
	public RPlatform getRPlatform() {
		return this.rPlatform;
	}
	
	@Override
	public Lock getReadLock() {
		return this.lock.readLock();
	}
	
	@Override
	public Lock getWriteLock() {
		return this.lock.writeLock();
	}
	
	
	@Override
	public void preferenceChanged(final IChangeEvent event) {
		if (event.contains(PREF_QUALIFIER)
				&& (event.contains(CUSTOM_REPO_PREF)
						|| event.contains(CUSTOM_CRAN_MIRROR_PREF)
						|| event.contains(CUSTOM_BIOC_MIRROR_PREF) )) {
			loadPrefs(true);
		}
	}
	
	@Override
	public void clear() {
		getWriteLock().lock();
		try {
			this.selectedRepos= new SelectedRepos(Collections.<RRepo> emptyList(),
					null, null, null);
			savePrefs(this.selectedRepos);
			
			this.requireLoad |= (REQUIRE_CRAN | REQUIRE_BIOC);
			this.requireLoad |= (REQUIRE_REPOS);
			
			final Change change= new Change(this.rEnv);
			change.fRepos= 1;
			checkRepos(change);
			
			this.requireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
			resetPkgs(this.rEnv.getConfig());
			this.firstTime= true;
		}
		finally {
			getWriteLock().unlock();
		}
	}
	
	public void dispose() {
		this.prefAccess.removePreferenceSetListener(this);
	}
	
	
	@Override
	public void check(final int flags, final RService r, final IProgressMonitor monitor) throws CoreException {
		checkInit(flags, r, monitor);
		check(r, monitor);
	}
	
	private void checkInit(final int flags,
			final RService r, final IProgressMonitor monitor) throws CoreException {
		if ((flags & INITIAL) == INITIAL || this.rPlatform == null) {
			checkRVersion(r.getPlatform());
			
			final IREnvConfiguration config= this.rEnv.getConfig();
			if (config != null && config.isRemote()) {
				this.rLibGroups= REnvLibGroups.loadFromR(r, monitor);
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
			
			if (this.rTaskEvent != null) {
				fireUpdate(this.rTaskEvent);
			}
		}
		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when checking for new and updated R packages.", e));
		}
		finally {
			this.rTaskEvent= null;
			endRTask();
		}
	}
	
	private void checkRVersion(final RPlatform rPlatform) {
		if (this.rPlatform != null && !this.rPlatform.getRVersion().equals(rPlatform.getRVersion())) {
			getWriteLock().lock();
			try {
				this.requireLoad |= (REQUIRE_REPOS | REQUIRE_CRAN | REQUIRE_BIOC);
				this.requireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
			}
			finally {
				getWriteLock().unlock();
			}
			refreshPkgs();
		}
		this.rPlatform= rPlatform;
	}
	
	
	@Override
	public boolean requiresUpdate() {
		final long stamp= System.currentTimeMillis();
		if (Math.abs(this.mirrorsStamp - stamp) > MIRROR_CHECK) {
			getWriteLock().lock();
			try {
				this.requireLoad |= (REQUIRE_CRAN | REQUIRE_BIOC);
				return true;
			}
			finally {
				getWriteLock().unlock();
			}
		}
		if ((this.requireLoad & (REQUIRE_REPOS | REQUIRE_CRAN | REQUIRE_BIOC)) != 0) {
			return true;
		}
		final IStatus status= getReposStatus(null);
		if (!status.isOK()) {
			return false;
		}
		
		if (Math.abs(this.pkgsStamp - stamp) > MIRROR_CHECK) {
			getWriteLock().lock();
			try {
				this.requireLoad |= (REQUIRE_REPO_PKGS);
				return true;
			}
			finally {
				getWriteLock().unlock();
			}
		}
		if ((this.requireLoad & (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS)) != 0) {
			return true;
		}
		return false;
	}
	
	
	@Override
	public IStatus getReposStatus(final ISelectedRepos repos) {
		final ISelectedRepos current= this.selectedRepos;
		final int confirm= this.requireConfirm;
		return getReposStatus((repos != null) ? repos : current, current, confirm);
	}
	
	private IStatus getReposStatus(final ISelectedRepos repos, final ISelectedRepos current, final int confirm) {
		if (repos.getRepos().isEmpty()) {
			return createStatus(IStatus.ERROR, "No repository is selected. Select the repositories where to install R packages from.");
		}
		
		final boolean requireCRAN= RVarRepo.requireCRANMirror(repos.getRepos());
		if (requireCRAN && repos.getCRANMirror() == null) {
			return createStatus(IStatus.ERROR, "No CRAN mirror is selected. Selected a mirror for CRAN.");
		}
		final boolean requireBioC= RVarRepo.requireBioCMirror(repos.getRepos());
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
			
			this.rTaskEvent= new Change(this.rEnv);
			
			final ISelectedRepos settings= runLoadRepos(r, monitor);
			if (settings != null) {
				runApplyRepo(settings, r, monitor);
				runLoadPkgs(settings, r, monitor);
			}
			fireUpdate(this.rTaskEvent);
		}
		finally {
			this.rTaskEvent= null;
			endRTask();
		}
	}
	
	
	private void checkMirrors(final Change event) {
		if ((this.requireLoad & (REQUIRE_CRAN | REQUIRE_BIOC)) != 0) {
			return;
		}
		
		SelectedRepos selected= this.selectedRepos;
		this.allCRAN= ImCollections.concatList(this.customCRAN, this.rCRANMirrors);
		
		RRepo selectedCRAN= selected.getCRANMirror();
		if (selected.getCRANMirror() != null) {
			selectedCRAN= Util.findRepo(this.allCRAN, selectedCRAN);
		}
		else if (this.firstTime && this.selectedCRANInR != null) {
			selectedCRAN= Util.getRepoByURL(this.allCRAN, this.selectedCRANInR);
		}
		if (selectedCRAN == null) {
			this.requireConfirm |= REQUIRE_CRAN;
			selectedCRAN= this.prefAccess.getPreferenceValue(LAST_CRAN_PREF);
			if (selectedCRAN != null) {
				selectedCRAN= Util.findRepo(this.allCRAN, selectedCRAN);
			}
			if (!this.customCRAN.isEmpty()
					&& (selectedCRAN == null || !selectedCRAN.getId().startsWith(RRepo.CUSTOM_PREFIX)) ) {
				selectedCRAN= this.customCRAN.get(0);
			}
			if (this.firstTime && selectedCRAN == null && !this.rCRANMirrors.isEmpty()) {
				selectedCRAN= getRegionMirror(this.rCRANMirrors);
			}
		}
		
		
		RRepo selectedBioC= selected.getBioCMirror();
		this.allBioC= ImCollections.concatList(this.customBioC, this.rBioCMirrors);
		if (selectedBioC != null) {
			selectedBioC= Util.findRepo(this.allBioC, selectedBioC);
		}
		else if (this.firstTime && this.selectedBioCInR != null) {
			selectedBioC= RPkgUtil.getRepoByURL(this.allBioC, this.selectedBioCInR);
		}
		if (selectedBioC == null) {
			this.requireConfirm |= REQUIRE_BIOC;
			selectedBioC= this.prefAccess.getPreferenceValue(LAST_BIOC_PREF);
			if (!this.customBioC.isEmpty()
					&& (selectedBioC == null || !selectedBioC.getId().startsWith(RRepo.CUSTOM_PREFIX)) ) {
				selectedBioC= this.customBioC.get(0);
			}
			if (this.firstTime && selectedBioC == null && !this.rBioCMirrors.isEmpty()) {
				selectedBioC= getRegionMirror(this.rBioCMirrors);
				if (selectedBioC == null) {
					selectedBioC= this.rBioCMirrors.get(0);
				}
			}
		}
		
		selected= new SelectedRepos(
				selected.getRepos(),
				selectedCRAN,
				this.bioCVersion,
				selectedBioC );
		if ((this.requireLoad & (REQUIRE_REPOS)) == 0) {
			for (final RRepo repo : this.allRepos) {
				if (repo instanceof RVarRepo) {
					((RVarRepo) repo).updateURL(selected);
				}
			}
		}
		this.selectedRepos= selected;
		
		event.fPkgs= 1;
	}
	
	private void checkRepos(final Change event) {
		if ((this.requireLoad & (REQUIRE_CRAN | REQUIRE_BIOC | REQUIRE_REPOS)) != 0) {
			return;
		}
		
		SelectedRepos selected= this.selectedRepos;
		
		this.allRepos= new ArrayList<>(this.customRepos.size() + this.addRepos.size() + this.rRepos.size());
		this.allRepos.addAll(this.customRepos);
		this.allRepos.addAll(this.addRepos);
		for (final RRepo repo : this.allRepos) {
			if (repo instanceof RVarRepo) {
				((RVarRepo) repo).updateURL(selected);
			}
		}
		for (final RRepo repo : this.rRepos) {
			if (repo instanceof RVarRepo) {
				((RVarRepo) repo).updateURL(selected);
			}
		}
		for (final RRepo repo : this.rRepos) {
			if (!repo.getId().isEmpty()) {
				if (RPkgUtil.getRepoById(this.allRepos, repo.getId()) == null) {
					this.allRepos.add(repo);
				}
			}
			else {
				if (Util.getRepoByURL(this.allRepos, repo) == null) {
					this.allRepos.add(RVarRepo.create(RRepo.R_PREFIX + repo.getURL(), repo.getName(),
							repo.getURL(), null ));
				}
			}
		}
		
		{	final Collection<RRepo> selectedRepos= selected.getRepos();
			final Collection<RRepo> previous= (this.firstTime && selectedRepos.isEmpty()) ?
					this.selectedReposInR : selectedRepos;
			final List<RRepo> repos= new ArrayList<>(previous.size());
			for (RRepo repo : previous) {
				repo= Util.findRepo(this.allRepos, repo);
				if (repo != null) {
					repos.add(repo);
				}
			}
			selected= new SelectedRepos(
					repos,
					selected.getCRANMirror(),
					selected.getBioCVersion(),
					selected.getBioCMirror() );
			this.selectedRepos= selected;
		}
		
		this.requireLoad |= REQUIRE_REPO_PKGS;
		
		event.fRepos= 1;
	}
	
	
	private void loadPrefs(final boolean custom) {
		final Change event= new Change(this.rEnv);
		final IPreferenceAccess prefs= PreferenceUtils.getInstancePrefs();
		getWriteLock().lock();
		try {
			if (custom) {
				this.customRepos= prefs.getPreferenceValue(CUSTOM_REPO_PREF);
				this.customCRAN= prefs.getPreferenceValue(CUSTOM_CRAN_MIRROR_PREF);
				this.customBioC= prefs.getPreferenceValue(CUSTOM_BIOC_MIRROR_PREF);
				
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
		this.listeners.add(listener);
	}
	
	@Override
	public void removeListener(final Listener listener) {
		this.listeners.remove(listener);
	}
	
	private void fireUpdate(final Event event) {
		if (event.reposChanged() == 0 && event.pkgsChanged() == 0 && event.viewsChanged() == 0) {
			return;
		}
		final Listener[] listeners= this.listeners.toArray();
		for (int i= 0; i < listeners.length; i++) {
			listeners[i].handleChange(event);
		}
	}
	
	
	@Override
	public List<RRepo> getAvailableRepos() {
		return this.allRepos;
	}
	
	@Override
	public ISelectedRepos getSelectedRepos() {
		return this.selectedRepos;
	}
	
	@Override
	public void setSelectedRepos(final ISelectedRepos repos) {
		List<RRepo> selectedRepos;
		{	final Collection<RRepo> selected= repos.getRepos();
			selectedRepos= new ArrayList<>(selected.size());
			for (final RRepo repo : this.allRepos) {
				if (selected.contains(repo)) {
					selectedRepos.add(repo);
				}
			}
		}
		RRepo selectedCRAN;
		{	final RRepo repo= repos.getCRANMirror();
			selectedCRAN= (repo != null) ? Util.findRepo(this.allCRAN, repo) : null;
			this.requireConfirm &= ~REQUIRE_CRAN;
		}
		RRepo selectedBioC;
		{	final RRepo repo= repos.getBioCMirror();
			selectedBioC= (repo != null) ? Util.findRepo(this.allBioC, repo) : null;
			this.requireConfirm &= ~REQUIRE_BIOC;
		}
		
		final SelectedRepos previousSettings= this.selectedRepos;
		final SelectedRepos newSettings= new SelectedRepos(
				selectedRepos,
				selectedCRAN,
				previousSettings.getBioCVersion(), selectedBioC );
		for (final RRepo repo : this.allRepos) {
			if (repo instanceof RVarRepo) {
				((RVarRepo) repo).updateURL(newSettings);
			}
		}
		this.selectedRepos= newSettings;
		savePrefs(newSettings);
		
		if (!newSettings.equals(previousSettings)) {
			this.requireLoad |= (REQUIRE_REPO_PKGS);
			
			final Change event= new Change(this.rEnv);
			event.fRepos= 1;
			fireUpdate(event);
		}
	}
	
	@Override
	public RRepo getRepo(final String repoId) {
		if (repoId.isEmpty()) {
			return null;
		}
		RRepo repo= this.selectedRepos.getRepo(repoId);
		if (repo == null) {
			repo= RPkgUtil.getRepoById(this.allRepos, repoId);
		}
		return repo;
	}
	
	private void savePrefs(final SelectedRepos repos) {
		if (this.rEnv.getConfig() == null) {
			return;
		}
		final IScopeContext prefs= InstanceScope.INSTANCE;
		
		final IEclipsePreferences node= prefs.getNode(this.selectedReposPref.getQualifier());
		
		PreferenceUtils.setPrefValue(node, this.selectedReposPref, repos.getRepos());
		PreferenceUtils.setPrefValue(node, this.selectedCRANPref, repos.getCRANMirror());
		PreferenceUtils.setPrefValue(node, this.bioCVersionPref, repos.getBioCVersion());
		PreferenceUtils.setPrefValue(node, this.selectedBioCPref, repos.getBioCMirror());
		
		if (repos.getCRANMirror() != null) {
			PreferenceUtils.setPrefValue(prefs, LAST_CRAN_PREF, repos.getCRANMirror());
		}
		if (repos.getBioCMirror() != null) {
			PreferenceUtils.setPrefValue(prefs, LAST_BIOC_PREF, repos.getBioCMirror());
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
		this.requireLoad |= (REQUIRE_REPO_PKGS | REQUIRE_INST_PKGS);
	}
	
	
	@Override
	public ImList<RRepo> getAvailableCRANMirrors() {
		return this.allCRAN;
	}
	
	@Override
	public List<RRepo> getAvailableBioCMirrors() {
		return this.allBioC;
	}
	
	@Override
	public IRLibPaths getRLibPaths() {
		return this.rLibPaths;
	}
	
	public REnvLibGroups getRLibGroups() {
		if (this.rLibGroups != null) {
			return this.rLibGroups;
		}
		final IREnvConfiguration config= this.rEnv.getConfig();
		if (config != null) {
			return new REnvLibGroups(config);
		}
		return null;
	}
	
	@Override
	public IRPkgSet getRPkgSet() {
		return (this.pkgsExt != null) ? this.pkgsExt : this.pkgsLight;
	}
	
	@Override
	public IRPkgSet.Ext getExtRPkgSet() {
		return this.pkgsExt;
	}
	
	@Override
	public List<? extends IRView> getRViews() {
		return this.rViews;
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
			if (this.rProcess != null) {
				return false;
			}
			this.rProcess= r.getTool();
			this.rTask= 1;
			return true;
		}
	}
	
	private void beginRTask(final IToolService r,
			final IProgressMonitor monitor) throws CoreException {
		synchronized (this) {
			while (this.rProcess != null) {
				if (this.rTask == 1) {
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
					final Status status= new Status(IStatus.ERROR, RCore.PLUGIN_ID,
							NLS.bind("Another package manager task for ''{0}'' is already running in ''{0}''", 
									this.rEnv.getName(), this.rProcess.getLabel(ITool.DEFAULT_LABEL) ));
	//				r.handleStatus(status, monitor);
					throw new CoreException(status);
				}
			}
			this.rProcess= r.getTool();
			this.rTask= 2;
		}
	}
	
	private void endRTask() {
		synchronized (this) {
			this.rProcess= null;
			this.rTask= 0;
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
				selectedRepos= this.selectedRepos;
			}
			finally {
				getReadLock().unlock();
			}
			
			if (getReposStatus(selectedRepos).getSeverity() != IStatus.ERROR) {
				this.rTaskEvent= new Change(this.rEnv);
				
				runApplyRepo(selectedRepos, r, monitor);
				runLoadPkgs(selectedRepos, r, monitor);
				
				fireUpdate(this.rTaskEvent);
			}
		}
		finally {
			this.rTaskEvent= null;
			endRTask();
		}
	}
	
	private ISelectedRepos runLoadRepos(final RService r,
			final IProgressMonitor monitor) throws CoreException {
		try {
			String bioCVersion= null;
			try {
				final RObject data= r.evalData("as.character(rj:::.renv.getBioCVersion())", monitor);
				bioCVersion= RDataUtil.checkSingleCharValue(data);
			}
			catch (final CoreException e) {
				try {
					final RObject data= r.evalData("as.character(tools:::.BioC_version_associated_with_R_version)", monitor);
					bioCVersion= RDataUtil.checkSingleCharValue(data);
				}
				catch (final CoreException ignore) {
					final Status status= new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
							"Failed to get the version of BioC.", e );
					RCorePlugin.log(status);
				}
			}
			
			final boolean loadRMirrors= ((this.requireLoad & (REQUIRE_CRAN | REQUIRE_BIOC)) != 0);
			final boolean loadRRepos= ((this.requireLoad & (REQUIRE_REPOS)) != 0);
			
			List<RRepoMirror> rCRANMirrors= null;
			List<RRepoMirror> rBioCMirrors= null;
			String selectedCRAN= null;
			String selectedBioC= null;
			if (loadRMirrors) {
				monitor.subTask("Fetching available mirrors...");
				final String mirrorArgs= "all= FALSE, local.only= FALSE"; //$NON-NLS-1$
				
				rCRANMirrors= fetchMirrors("getCRANmirrors(" + mirrorArgs + ')', r, monitor); //$NON-NLS-1$
				
				try {
					rBioCMirrors= fetchMirrors("utils:::.getMirrors('https://bioconductor.org/BioC_mirrors.csv', file.path(R.home('doc'), 'BioC_mirrors.csv'), " + mirrorArgs + ')', r, monitor); //$NON-NLS-1$
				}
				catch (final Exception e) {
				}
				if (rBioCMirrors == null || rBioCMirrors.isEmpty()) {
					final String[][] s= new String[][] {
							{ "United States (Seattle)", "http://www.bioconductor.org", "us" },
							{ "United States (Rockville)", "http://watson.nci.nih.gov/bioc_mirror", "us" },
							{ "Germany (Dortmund)", "http://bioconductor.statistik.tu-dortmund.de", "de" },
							{ "China (Anhui)", "http://mirrors.ustc.edu.cn/bioc/", "cn" },
							{ "United Kingdom (Hinxton)", "http://mirrors.ebi.ac.uk/bioconductor/", "uk" },
							{ "Riken, Kobe (Japan)", "http://bioconductor.jp/", "jp" },
							{ "Australia (Sydney)", "http://mirror.aarnet.edu.au/pub/bioconductor/", "au" },
							{ "Brazil (Ribeirão Preto)", "http://bioconductor.fmrp.usp.br/", "br" },
					};
					rBioCMirrors= new ArrayList<>(s.length);
					for (int i= 0; i < s.length; i++) {
						final String url= Util.checkURL(s[i][1]);
						if (!url.isEmpty()) {
							rBioCMirrors.add(new RRepoMirror(RRepo.R_PREFIX + url, s[i][0], url, s[i][2]));
						}
					}
				}
			}
			
			List<RRepo> rrepos= null;
			List<RRepo> selected= null;
			if (loadRRepos) {
				monitor.subTask("Fetching available repositories...");
				{	final RObject data= r.evalData("options('repos')[[1L]]", monitor); //$NON-NLS-1$
					if (data.getRObjectType() != RObject.TYPE_NULL) {
						final RCharacterStore urls= RDataUtil.checkRCharVector(data).getData();
						final RStore<?> ids= ((RVector<?>) data).getNames();
						
						final int l= RDataUtil.checkIntLength(urls);
						selected= new ArrayList<>(l);
						for (int i= 0; i < l; i++) {
							final String id= (ids != null) ? ids.getChar(i) : null;
							final String url= urls.getChar(i);
							
							final RRepo repo= Util.createRepoFromR(id, null, url);
							if (repo != null) {
								selected.add(repo);
							}
						}
					}
					else {
						selected= new ArrayList<>(4);
					}
				}
				
				final RObject data= r.evalData("local({" + //$NON-NLS-1$
						"p <- file.path(Sys.getenv('HOME'), '.R', 'repositories')\n" + //$NON-NLS-1$
						"if (!file.exists(p)) p <- file.path(R.home('etc'), 'repositories')\n" + //$NON-NLS-1$
						"r <- utils::read.delim(p, header= TRUE, comment.char= '#', colClasses= c(rep.int('character', 3L), rep.int('logical', 4L)))\n" + //$NON-NLS-1$
						"r[c(names(r)[1L], 'URL', 'default')]\n" + //$NON-NLS-1$
				"})", monitor); //$NON-NLS-1$
				final RDataFrame df= RDataUtil.checkRDataFrame(data);
				final RStore<?> ids= df.getRowNames();
				final RCharacterStore labels= RDataUtil.checkRCharVector(df.get(0)).getData();
				final RCharacterStore urls= RDataUtil.checkRCharVector(df.get("URL")).getData(); //$NON-NLS-1$
				final RLogicalStore isDefault= (selected.isEmpty()) ?
						RDataUtil.checkRLogiVector(df.get("default")).getData() : null; //$NON-NLS-1$
				
				{	final int l= RDataUtil.checkIntLength(labels);
					rrepos= new ArrayList<>(l + 4);
					for (int i= 0; i < l; i++) {
						final String id= (ids != null) ? ids.getChar(i) : null;
						final String url= urls.getChar(i);
						
						final RRepo repo= Util.createRepoFromR(id, labels.getChar(i), url);
						if (repo != null) {
							rrepos.add(repo);
							if (isDefault != null && isDefault.getLogi(i)) {
								selected.add(repo);
							}
						}
					}
				}
				
				for (int i= 0; i < selected.size(); i++) {
					final RRepo repo= selected.get(i);
					RRepo rrepo= null;
					if (!repo.getURL().isEmpty()) {
						rrepo= RPkgUtil.getRepoByURL(rrepos, repo.getURL());
					}
					if (rrepo != null) {
						selected.set(i, rrepo);
						continue;
					}
					if (!repo.getId().isEmpty()) {
						final int j= rrepos.indexOf(repo); // by id
						if (j >= 0) {
							rrepo= rrepos.get(j);
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
					final RRepo repo= RPkgUtil.getRepoById(rrepos, RRepo.CRAN_ID);
					if (repo != null && !repo.getURL().isEmpty()
							&& !RVarRepo.hasVars(repo.getURL()) ) {
						selectedCRAN= repo.getURL();
					}
				}
				else {
					final RObject data= r.evalData("options('repos')[[1L]]['CRAN']", monitor); //$NON-NLS-1$
					if (data.getRObjectType() != RObject.TYPE_NULL) {
						final String url= Util.checkURL(RDataUtil.checkSingleChar(data));
						if (!url.isEmpty() && !RVarRepo.hasVars(url)) {
							selectedCRAN= url;
						}
					}
				}
				{	final RObject data= r.evalData("options('BioC_mirror')[[1L]]", monitor); //$NON-NLS-1$
					if (data.getRObjectType() != RObject.TYPE_NULL) {
						selectedBioC= RDataUtil.checkSingleChar(data);
					}
				}
			}
			
			getWriteLock().lock();
			try {
				this.bioCVersion= bioCVersion;
				
				if (loadRMirrors) {
					this.requireLoad &= ~(REQUIRE_CRAN | REQUIRE_BIOC);
					this.rCRANMirrors= rCRANMirrors;
					this.selectedCRANInR= selectedCRAN;
					this.rBioCMirrors= rBioCMirrors;
					this.selectedBioCInR= selectedBioC;
					this.mirrorsStamp= this.rTaskEvent.fStamp;
					
					checkMirrors(this.rTaskEvent);
				}
				if (loadRRepos) {
					this.requireLoad &= ~(REQUIRE_REPOS);
					this.rRepos= rrepos;
					this.selectedReposInR= selected;
					
					checkRepos(this.rTaskEvent);
				}
				this.firstTime= false;
				
				if (getReposStatus(this.selectedRepos, this.selectedRepos, this.requireConfirm).isOK()) {
					return this.selectedRepos;
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
	
	private List<RRepoMirror> fetchMirrors(final String rExpr, final RService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RObject data= r.evalData(rExpr + "[c('Name', 'URL', 'CountryCode')]", monitor); //$NON-NLS-1$
		final RDataFrame df= RDataUtil.checkRDataFrame(data);
		final RCharacterStore names= RDataUtil.checkRCharVector(df.get("Name")).getData(); //$NON-NLS-1$
		final RCharacterStore urls= RDataUtil.checkRCharVector(df.get("URL")).getData(); //$NON-NLS-1$
		final RCharacterStore countryCodes= RDataUtil.checkRCharVector(df.get("CountryCode")).getData(); //$NON-NLS-1$
		
		final int l= RDataUtil.checkIntLength(names);
		final List<RRepoMirror> mirrors= new ArrayList<>(l);
		for (int i= 0; i < l; i++) {
			final String url= Util.checkURL(urls.getChar(i));
			if (!url.isEmpty()) {
				mirrors.add(new RRepoMirror(RRepo.R_PREFIX + url, names.getChar(i), url,
						countryCodes.getChar(i) ));
			}
		}
		return mirrors;
	}
	
	private RRepoMirror getRegionMirror(final List<RRepoMirror> mirrors) {
		final String countryCode= Locale.getDefault().getCountry().toLowerCase();
		RRepoMirror http= null;
		for (final RRepoMirror repo : mirrors) {
			if (countryCode.equals(repo.getCountryCode())) {
				if (repo.getURL().startsWith("https:")) { //$NON-NLS-1$
					return repo;
				}
				else if (http == null) {
					http= repo;
				}
			}
		}
		return http;
	}
	
	private void runApplyRepo(final ISelectedRepos repos, final RService r,
			final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Setting repository configuration...");
		try {
			if (repos.getBioCMirror() != null) {
				final FunctionCall call= r.createFunctionCall("options");
				call.addChar("BioC_mirror", repos.getBioCMirror().getURL());
				call.evalVoid(monitor);
			}
			{	final List<RRepo> selectedRepos= (List<RRepo>) repos.getRepos();
				final String[] ids= new String[selectedRepos.size()];
				final String[] urls= new String[selectedRepos.size()];
				for (int i= 0; i < urls.length; i++) {
					final RRepo repo= selectedRepos.get(i);
					ids[i]= repo.getId();
					urls[i]= repo.getURL();
				}
				final RVector<RCharacterStore> data= new RVectorImpl<RCharacterStore>(
						new RCharacterDataImpl(urls), RObject.CLASSNAME_CHARACTER, ids);
				
				final FunctionCall call= r.createFunctionCall("options");
				call.add("repos", data);
				call.evalVoid(monitor);
			}
		}
		catch (final CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when setting repository configuration in R.",
					e ));
		}
	}
	
	private void runLoadPkgs(final ISelectedRepos repoSettings, final RService r,
			final IProgressMonitor monitor) throws CoreException {
		final boolean loadRepoPkgs= (this.requireLoad & (REQUIRE_REPO_PKGS)) != 0
				&& getReposStatus(repoSettings).isOK();
		final boolean loadInstPkgs= ((this.requireLoad & (REQUIRE_INST_PKGS)) != 0);
		
		FullRPkgSet pkgs= null;
		if (loadRepoPkgs) {
			this.rTaskEvent.fOldPkgs= getRPkgSet();
			this.rTaskEvent.fNewPkgs= pkgs= this.pkgScanner.loadAvailable(repoSettings, r, monitor);
		}
		
		if (loadInstPkgs) {
			if (pkgs == null) {
				if (this.pkgsExt != null) {
					pkgs= this.pkgsExt.cloneAvailable();
				}
				else {
					pkgs= new FullRPkgSet(0);
				}
			}
			checkInstalled(pkgs, r, monitor);
		}
		
		if (pkgs != null) {
			getWriteLock().lock();
			try {
				setPkgs();
				
				if (loadRepoPkgs) {
					this.requireLoad &= ~REQUIRE_REPO_PKGS;
					this.pkgsStamp= this.rTaskEvent.fStamp;
					this.rTaskEvent.fPkgs |= AVAILABLE;
				}
				if (loadInstPkgs) {
					this.requireLoad &= ~REQUIRE_INST_PKGS;
				}
				
				if (this.pkgsExt != null) {
					checkRViews(this.pkgsExt, r, monitor);
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
		RVector<RNumericStore> libs= null;
		boolean[] update= null;
		
		try {
			libs= RDataUtil.checkRNumVector(r.evalData(
					"rj:::.renv.checkLibs()", monitor )); //$NON-NLS-1$
			
			final int l= RDataUtil.checkIntLength(libs.getData());
			ITER_LIBS: for (int idxLib= 0; idxLib < l; idxLib++) {
				final String libPath= libs.getNames().getChar(idxLib);
				if (this.libs != null) {
					final int idx= (int) this.libs.getNames().indexOf(libPath);
					if (idx >= 0) {
						if (this.libs.getData().getNum(idx) == libs.getData().getNum(idxLib)) {
							continue ITER_LIBS;
						}
					}
				}
				if (update == null) {
					update= new boolean[l];
				}
				update[idxLib]= true;
			}
			
			this.libs= libs;
		}
		catch (final UnexpectedRDataException | CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when checking for changed R libraries.",
					e ));
		}
		
		if (update != null || pkgs != null) {
			if (this.rTaskEvent == null) {
				this.rTaskEvent= new Change(this.rEnv);
			}
			if (this.rTaskEvent.fOldPkgs == null) {
				this.rTaskEvent.fOldPkgs= getRPkgSet();
			}
			if (this.pkgsExt != null || pkgs != null) {
				final FullRPkgSet newPkgs= (pkgs != null) ? pkgs : this.pkgsExt.cloneAvailable();
				this.rTaskEvent.fNewPkgs= newPkgs;
				this.rLibPaths= RLibPaths.create(getRLibGroups(), libs, r, monitor);
				this.pkgScanner.updateInstFull(this.rLibPaths, update, newPkgs, this.rTaskEvent, r, monitor);
			}
			else {
				final RPkgSet newPkgs= new RPkgSet((int) libs.getLength());
				this.rTaskEvent.fNewPkgs= newPkgs;
				this.rLibPaths= RLibPaths.createLight(getRLibGroups(), libs);
				this.pkgScanner.updateInstLight(this.rLibPaths, update, newPkgs, this.rTaskEvent, r, monitor);
			}
			
			if (this.rTaskEvent.fInstalledPkgs != null && this.rTaskEvent.fInstalledPkgs.names.isEmpty()) {
				this.rTaskEvent.fInstalledPkgs= null;
			}
			if (pkgs == null) {
				setPkgs();
			}
			if (this.rTaskEvent.fInstalledPkgs != null && this.db != null) {
				this.db.updatePkgs(this.rTaskEvent);
			}
			
			if (pkgs == null && this.pkgsExt != null) {
				checkRViews(this.pkgsExt, r, monitor);
			}
		}
	}
	
	private void setPkgs() {
		final Change event= this.rTaskEvent;
		if (event.fNewPkgs instanceof FullRPkgSet) {
			this.pkgsExt= (FullRPkgSet) event.fNewPkgs;
			this.pkgsLight= null;
		}
		else if (event.fNewPkgs instanceof RPkgSet) {
			this.pkgsExt= null;
			this.pkgsLight= (RPkgSet) event.fNewPkgs;
		}
		if (event.fInstalledPkgs != null) {
			event.fPkgs |= INSTALLED;
		}
	}
	
	private void checkRViews(final FullRPkgSet pkgs,
			final RService r, final IProgressMonitor monitor) {
		final RPkgInfoAndData pkg= pkgs.getInstalled().getFirstByName("ctv"); //$NON-NLS-1$
		if (pkg == null || pkg.getVersion().equals(this.rViewsVersion)) {
			return;
		}
		final List<RView> rViews= RViewTasks.loadRViews(r, monitor);
		if (rViews != null) {
			this.rViews= rViews;
			this.rViewsVersion= pkg.getVersion();
			this.rTaskEvent.fViews= 1;
		}
	}
	
	
	@Override
	public IRPkgData addToCache(final IFileStore store, final IProgressMonitor monitor) throws CoreException {
		final IRPkg pkg= RPkgUtil.checkPkgFileName(store.getName());
		final RPkgType type= RPkgUtil.checkPkgType(store.getName(), this.rPlatform);
		this.cache.add(pkg.getName(), type, store, monitor);
		return new RPkgData(pkg.getName(), RNumVersion.NONE, RRepo.WS_CACHE_PREFIX + type.name().toLowerCase());
	}
	
	
	@Override
	public void perform(final ITool rTool, final List<? extends RPkgAction> actions) {
		if (actions.isEmpty()) {
			return;
		}
		final String label= (actions.get(0).getAction() == RPkgAction.UNINSTALL) ?
				"Uninstall R Packages" : "Install/Update R Packages";
		final RPkgOperator op= new RPkgOperator(this);
		rTool.getQueue().add(new AbstractStatetRRunnable("r/renv/pkgs.inst", label) { //$NON-NLS-1$
			@Override
			protected void run(final IRConsoleService r, final IProgressMonitor monitor) throws CoreException {
				beginRTask(r, monitor);
				try {
					checkNewCommand(r, monitor);
					op.runActions(actions, r, monitor);
				}
				catch (final UnexpectedRDataException | CoreException e) {
					throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
							"An error occurred when installing and updating R packages.",
							e ));
				}
				finally {
					endRTask();
					
					r.briefAboutChange(0x10); // packages
				}
			}
		});
	}
	
	@Override
	public void loadPkgs(final ITool rTool, final List<? extends IRPkgInfoAndData> pkgs,
			final boolean expliciteLocation) {
		final RPkgOperator op= new RPkgOperator(this);
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
