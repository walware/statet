/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.renv;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.FileUtil;
import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.preferences.Preference.StringArrayPref;
import de.walware.ecommons.preferences.Preference.StringPref;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCorePreferenceNodes;
import de.walware.statet.r.internal.core.Messages;


/**
 * Configuration of an R setup
 */
public class REnvConfiguration extends AbstractPreferencesModelObject {
	
	
	public static final String PROP_NAME = "name"; //$NON-NLS-1$
	private static final String PREFKEY_NAME = "name"; //$NON-NLS-1$
	
	public static final String PROP_ID = "id"; //$NON-NLS-1$
	private static final String PREFKEY_ID = "id"; //$NON-NLS-1$
	
	public static final String PROP_RHOME = "RHome"; //$NON-NLS-1$
	private static final String PREFKEY_RHOME = "env.r_home"; //$NON-NLS-1$
	
	public static final String PROP_RBITS = "RBits"; //$NON-NLS-1$
	private static final String PREFKEY_RBITS = "env.r_bits.count"; //$NON-NLS-1$
	
	public static final String PROP_RLIBS = "RLibraries"; //$NON-NLS-1$
	private static final String PREFKEY_RLIBS_PREFIX = "env.r_libs."; //$NON-NLS-1$
	
	
	private static String getLibGroupLabel(final String id) {
		if (id.equals(RLibraryGroup.R_DEFAULT)) {
			return Messages.REnvConfiguration_DefaultLib_label;
		}
		if (id.equals(RLibraryGroup.R_SITE)) {
			return Messages.REnvConfiguration_SiteLibs_label;
		}
		if (id.equals(RLibraryGroup.R_OTHER)) {
			return Messages.REnvConfiguration_OtherLibs_label;
		}
		if (id.equals(RLibraryGroup.R_USER)) {
			return Messages.REnvConfiguration_UserLibs_label;
		}
		return null;
	}
	
	private static final List<RLibraryLocation> NO_LIBS = Collections.emptyList();
	
	private static final String[] DEFAULT_LIBS_IDS = new String[] {
			RLibraryGroup.R_DEFAULT, RLibraryGroup.R_SITE,
			RLibraryGroup.R_OTHER, RLibraryGroup.R_USER,
	};
	
	private static final List<RLibraryGroup> DEFAULT_LIBS_INIT;
	static {
		final RLibraryGroup[] groups = new RLibraryGroup[DEFAULT_LIBS_IDS.length];
		for (int i = 0; i < DEFAULT_LIBS_IDS.length; i++) {
			groups[i] = new RLibraryGroup(DEFAULT_LIBS_IDS[i], getLibGroupLabel(DEFAULT_LIBS_IDS[i]), NO_LIBS);
		}
		DEFAULT_LIBS_INIT = new ConstList<RLibraryGroup>(groups);
	}
	
	private static final List<RLibraryGroup> DEFAULT_LIBS_DEFAULTS = new ConstList<RLibraryGroup>(
			new RLibraryGroup(RLibraryGroup.R_DEFAULT, getLibGroupLabel(RLibraryGroup.R_DEFAULT),
				new ConstList<RLibraryLocation>(new RLibraryLocation(RLibraryGroup.DEFAULTLOCATION_R_DEFAULT)) ),
			new RLibraryGroup(RLibraryGroup.R_SITE, getLibGroupLabel(RLibraryGroup.R_SITE),
				new ConstList<RLibraryLocation>(new RLibraryLocation(RLibraryGroup.DEFAULTLOCATION_R_SITE)) ),
			new RLibraryGroup(RLibraryGroup.R_OTHER, getLibGroupLabel(RLibraryGroup.R_OTHER), NO_LIBS),
			new RLibraryGroup(RLibraryGroup.R_USER, getLibGroupLabel(RLibraryGroup.R_USER), NO_LIBS) );
	
	
	public static enum Exec {
		COMMON,
		CMD,
		TERM;
	}
	
	
	public static boolean isValidRHomeLocation(final IFileStore loc) {
		final IFileStore binDir = loc.getChild("bin"); //$NON-NLS-1$
		IFileStore exeFile = null;
		if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
			exeFile = binDir.getChild("R.exe"); //$NON-NLS-1$
		}
		else {
			exeFile = binDir.getChild("R"); //$NON-NLS-1$
		}
		final IFileInfo info = exeFile.fetchInfo();
		return (!info.isDirectory() && info.exists());
	}
	
	
	public static class WorkingCopy extends REnvConfiguration {
		
		public WorkingCopy() {
			loadDefaults();
		}
		
		private WorkingCopy(final REnvConfiguration config) {
			setId(config.getId());
			load(config);
		}
		
		@Override
		protected List<RLibraryGroup> copyLibs(final List<RLibraryGroup> source) {
			final List<RLibraryGroup> list = new ArrayList<RLibraryGroup>(source.size());
			for (final RLibraryGroup group : source) {
				list.add(new RLibraryGroup(group, true));
			}
			return list;
		}
		
		@Override
		public void setName(final String label) {
			super.setName(label);
		}
		
		@Override
		public void setRHome(final String label) {
			super.setRHome(label);
		}
		
		@Override
		public void setRBits(final int bits) {
			super.setRBits(bits);
		}
		
	}
	
	
	private String fNodeQualifier;
	private String fCheckName;
	private boolean fIsDisposed;
	
	private StringPref fPrefName;
	private String fName;
	private StringPref fPrefId;
	private String fId;
	private StringPref fPrefRHomeDirectory;
	private String fRHomeDirectory;
	private IntPref fPrefRBits;
	private int fRBits;
	
	private List<RLibraryGroup> fRLibraries;
	
	
	/**
	 * Creates new empty configuration
	 */
	protected REnvConfiguration() {
		this(System.getProperty("user.name")+System.currentTimeMillis()); //$NON-NLS-1$
	}
	
	protected REnvConfiguration(final String id) {
		fIsDisposed = false;
		setId(id);
		fRBits = 32;
		fRLibraries = DEFAULT_LIBS_INIT;
	}
	
	protected void checkPrefs() {
		Assert.isNotNull(fName);
		if (fName.equals(fCheckName)) {
			return;
		}
		fCheckName = fName;
		fNodeQualifier = RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER + "/" + fName; //$NON-NLS-1$
		fPrefName = new StringPref(fNodeQualifier, PREFKEY_NAME);
		fPrefId = new StringPref(fNodeQualifier, PREFKEY_ID);
		fPrefRHomeDirectory = new StringPref(fNodeQualifier, PREFKEY_RHOME);
		fPrefRBits = new IntPref(fNodeQualifier, PREFKEY_RBITS);
	}
	
	protected void checkExistence(final IPreferenceAccess prefs) {
		final IEclipsePreferences[] nodes = prefs.getPreferenceNodes(RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER);
		if (nodes.length > 0)  {
			try {
				if (!nodes[0].nodeExists(fName)) {
					throw new IllegalArgumentException("A REnv configuration with this name does not exists."); 
				}
			}
			catch (final BackingStoreException e) {
				throw new IllegalArgumentException("REnv Configuration could not be accessed."); 
			}
		}
	}
	
	@Override
	public String[] getNodeQualifiers() {
		checkPrefs();
		return new String[] { fNodeQualifier };
	}
	
	public Preference[] getPreferenceDefinitions() {
		checkPrefs();
		return new Preference[] {
				fPrefName,
				fPrefRHomeDirectory,
//				fPrefBinDirectory,
				fPrefRBits,
		};
	}
	
	@Override
	public void loadDefaults() {
		setName("R"); //$NON-NLS-1$
		setRHome(""); //$NON-NLS-1$
		fRLibraries = copyLibs(DEFAULT_LIBS_DEFAULTS);
		
		resolveLibs();
	}
	
	public void load(final REnvConfiguration from) {
		load(from, false);
	}
	protected void load(final REnvConfiguration from, final boolean copyId) {
		if (copyId) {
			setId(from.getId());
		}
		setName(from.getName());
		setRHome(from.getRHome());
		setRBits(from.getRBits());
		fRLibraries = copyLibs(from.getRLibraryGroups());
		
		resolveLibs();
	}
	
	@Override
	public void load(final IPreferenceAccess prefs) {
		load(prefs, false);
	}
	protected void load(final IPreferenceAccess prefs, final boolean copyId) {
		checkPrefs();
		checkExistence(prefs);
		if (copyId) {
			setId(prefs.getPreferenceValue(fPrefId));
		}
		setName(prefs.getPreferenceValue(fPrefName));
		setRHome(prefs.getPreferenceValue(fPrefRHomeDirectory));
		setRBits(prefs.getPreferenceValue(fPrefRBits));
		
		final String[] ids = DEFAULT_LIBS_IDS;
		final List<RLibraryGroup> groups = new ArrayList<RLibraryGroup>(ids.length);
		for (int i = 0; i < ids.length; i++) {
			final String id = ids[i];
			final String label = getLibGroupLabel(id);
			if (label != null) {
				final String[] locations = prefs.getPreferenceValue(
						new StringArrayPref(fNodeQualifier, PREFKEY_RLIBS_PREFIX+id, Preference.IS2_SEPARATOR_CHAR));
				final RLibraryLocation[] libs = new RLibraryLocation[(locations != null) ? locations.length : 0];
				for (int j = 0; j < locations.length; j++) {
					libs[j] = new RLibraryLocation(locations[j]);
				}
				groups.add(new RLibraryGroup(id, label, new ConstList<RLibraryLocation>(libs)));
			}
			else {
				// unknown group
			}
		}
		fRLibraries = Collections.unmodifiableList(groups);
		
		resolveLibs();
	}
	
	protected List<RLibraryGroup> copyLibs(final List<RLibraryGroup> source) {
		final RLibraryGroup[] groups = new RLibraryGroup[source.size()];
		for (int i = 0; i < groups.length; i++) {
			groups[i] = new RLibraryGroup(source.get(i), false);
		}
		return new ConstList<RLibraryGroup>(groups);
	}
	
	@Override
	public Map<Preference, Object> deliverToPreferencesMap(
			final Map<Preference, Object> map) {
		checkPrefs();
		map.put(fPrefId, getId());
		map.put(fPrefName, getName());
		map.put(fPrefRHomeDirectory, getRHome());
		map.put(fPrefRBits, getRBits());
		
		final List<RLibraryGroup> groups = fRLibraries;
		for (final RLibraryGroup group : groups) {
			final List<RLibraryLocation> libraries = group.getLibraries();
			final String[] locations = new String[libraries.size()];
			for (int i = 0; i < libraries.size(); i++) {
				locations[i] = libraries.get(i).getDirectoryPath();
			}
			map.put(new StringArrayPref(fNodeQualifier, PREFKEY_RLIBS_PREFIX+group.getId(), Preference.IS2_SEPARATOR_CHAR),
					locations);
		}
		
		return map;
	}
	
	public WorkingCopy createWorkingCopy() {
		return new WorkingCopy(this);
	}
	
	
/*-- Properties --------------------------------------------------------------*/
	
	protected void setName(final String label) {
		final String oldValue = fName;
		fName = label;
		firePropertyChange(PROP_NAME, oldValue, fName);
	}
	public String getName() {
		return fName;
	}
	
	protected void setId(final String id) {
		final String oldValue = fId;
		fId = id;
		firePropertyChange(PROP_RHOME, oldValue, fId);
	}
	public String getId() {
		return fId;
	}
	
	protected void setDispose(final boolean isDisposed) {
		final boolean oldValue = fIsDisposed;
		fIsDisposed = isDisposed;
		firePropertyChange(PROP_RHOME, oldValue, fIsDisposed);
	}
	public boolean isDisposed() {
		return fIsDisposed;
	}
	
	public IStatus validate() {
		if (fIsDisposed) {
			return new Status(Status.ERROR, RCore.PLUGIN_ID, Messages.REnvConfiguration_Validation_error_Removed_message);
		}
		CoreException error = null;
		IFileStore rloc = null;
		try {
			rloc = FileUtil.expandToLocalFileStore(getRHome(), null, null);
		}
		catch (final CoreException e) {
			error = e;
		}
		if (rloc == null || !isValidRHomeLocation(rloc)) {
			return new Status(Status.ERROR, RCore.PLUGIN_ID, Messages.REnvConfiguration_Validation_error_InvalidRHome_message, error);
		}
		return Status.OK_STATUS;
	}
	
	protected void setRHome(final String label) {
		final String oldValue = fRHomeDirectory;
		fRHomeDirectory = label;
		firePropertyChange(PROP_RHOME, oldValue, label);
	}
	public String getRHome() {
		return fRHomeDirectory;
	}
	
	protected void setRBits(int bits) {
		if (bits != 32 && bits != 64) {
			bits = 32;
		}
		final int oldValue = fRBits;
		fRBits = bits;
		firePropertyChange(PROP_RBITS, oldValue, bits);
	}
	
	public int getRBits() {
		return fRBits;
	}
	
	
	public List<RLibraryGroup> getRLibraryGroups() {
		return fRLibraries;
	}
	
	public RLibraryGroup getRLibraryGroup(final String id) {
		for (final RLibraryGroup group : fRLibraries) {
			if (group.getId().equals(id)) {
				return group;
			}
		}
		return null;
	}
	
	public List<String> getExecCommand(String arg1, final Set<Exec> execTypes) throws CoreException {
		final String test = (arg1 != null) ? arg1.trim().toUpperCase() : ""; //$NON-NLS-1$
		Exec type = Exec.COMMON;
		if (test.equals("CMD")) { //$NON-NLS-1$
			if (execTypes.contains(Exec.CMD)) {
				type = Exec.CMD;
				arg1 = null;
			}
		}
		else {
			if (execTypes.contains(Exec.TERM)) {
				type = Exec.TERM;
			}
		}
		final List<String> commandLine = getExecCommand(type);
		if (arg1 != null) {
			commandLine.add(arg1);
		}
		return commandLine;
	}
	
	public List<String> getExecCommand(final Exec execType) throws CoreException {
		String child = null;
		final List<String> commandLine = new ArrayList<String>(2);
		switch (execType) {
		case TERM:
			if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
				child = "bin\\Rterm.exe"; //$NON-NLS-1$
			}
			break;
		case CMD:
			if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
				child = "bin\\Rcmd.exe"; //$NON-NLS-1$
			}
			else {
				commandLine.add("CMD"); //$NON-NLS-1$
			}
			break;
		default:
			if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
				child = "bin\\R.exe"; //$NON-NLS-1$
			}
			break;
		}
		if (child == null) {
			child = "bin/R"; //$NON-NLS-1$
		}
		final IPath exec = URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, child).toURI());
		commandLine.add(0, exec.toOSString());
		return commandLine;
	}
	
	public Map<String, String> getEnvironmentsVariables() throws CoreException {
		return getEnvironmentsVariables(true);
	}
	
	public Map<String, String> getEnvironmentsVariables(final boolean configureRLibs) throws CoreException {
		final Map<String, String> envp = new HashMap<String, String>();
		final String rHome = URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, null).toURI()).toOSString();
		envp.put("R_HOME", rHome); //$NON-NLS-1$
		envp.put("PATH", //$NON-NLS-1$
				URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, "bin").toURI()).toOSString() + //$NON-NLS-1$
						File.pathSeparatorChar + "${env_var:PATH}"); //$NON-NLS-1$
		if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
			// libs in path
		}
		else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			envp.put("DYLD_LIBRARY_PATH", //$NON-NLS-1$
					URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, "lib").toURI()).toOSString() + //$NON-NLS-1$
							File.pathSeparatorChar + "${env_var:DYLD_LIBRARY_PATH}"); //$NON-NLS-1$
		}
		else {
			envp.put("LD_LIBRARY_PATH", //$NON-NLS-1$
					URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, "lib").toURI()).toOSString() + //$NON-NLS-1$
							File.pathSeparatorChar + "${env_var:LD_LIBRARY_PATH}"); //$NON-NLS-1$
		}
		if (configureRLibs) {
			envp.put("R_LIBS_SITE", getLibPath(getRLibraryGroup(RLibraryGroup.R_SITE))); //$NON-NLS-1$
			envp.put("R_LIBS", getLibPath(getRLibraryGroup(RLibraryGroup.R_OTHER))); //$NON-NLS-1$
			envp.put("R_LIBS_USER", getLibPath(getRLibraryGroup(RLibraryGroup.R_USER))); //$NON-NLS-1$
		}
		envp.put("LC_NUMERIC", "C"); //$NON-NLS-1$ //$NON-NLS-2$
		return envp;
	}
	
	private String getLibPath(final RLibraryGroup group) {
		List<RLibraryLocation> libs;
		if (group == null || (libs = group.getLibraries()).isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		final StringBuilder sb = new StringBuilder();
		for (final RLibraryLocation lib : libs) {
			final IFileStore store = lib.getDirectoryStore();
			if (store != null) {
				sb.append(URIUtil.toPath(store.toURI()).toOSString());
			}
			sb.append(File.pathSeparatorChar);
		}
		return sb.substring(0, sb.length()-1);
	}
	
	private void resolveLibs() {
		String rHome;
		try {
			rHome = URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, null).toURI()).toOSString();
		}
		catch (final CoreException e) {
			rHome = null;
		}
		for (final RLibraryGroup group : fRLibraries) {
			for (final RLibraryLocation lib : group.getLibraries()) {
				IFileStore store = null;
				try {
					String path = lib.fPath;
					if (path != null && path.length() > 0
							&& (rHome != null || !path.contains("${env_var:R_HOME}"))) { //$NON-NLS-1$
						path = path.replace("${env_var:R_HOME}", rHome); //$NON-NLS-1$
						store = FileUtil.expandToLocalFileStore(path, null, null);
					}
				}
				catch (final Exception e) {};
				lib.fStore = store;
			}
		}
	}
	
}
