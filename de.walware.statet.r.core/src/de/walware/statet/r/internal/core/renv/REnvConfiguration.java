/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.renv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.preferences.Preference.StringArrayPref;
import de.walware.ecommons.preferences.Preference.StringPref;

import de.walware.statet.nico.core.NicoCore;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCorePreferenceNodes;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.core.Messages;
import de.walware.statet.r.internal.core.RCorePlugin;


public class REnvConfiguration extends AbstractPreferencesModelObject implements IREnvConfiguration {
	
	
	private static final String PREFKEY_TYPE = "type"; //$NON-NLS-1$
	
	private static final String PREFKEY_NAME = "name"; //$NON-NLS-1$
	
	private static final String PREFKEY_RHOME_DIR = "env.r_home"; //$NON-NLS-1$
	
	private static final String PREFKEY_RBITS = "env.r_bits.count"; //$NON-NLS-1$
	
	private static final String PREFKEY_ROS = "env.r_os.type"; //$NON-NLS-1$
	
	private static final String PREFKEY_RLIBS_PREFIX = "env.r_libs."; //$NON-NLS-1$
	
	private static final String PREFKEY_RDOC_DIR = "env.r_doc.dir"; //$NON-NLS-1$
	private static final String PREFKEY_RSHARE_DIR = "env.r_share.dir"; //$NON-NLS-1$
	private static final String PREFKEY_RINCLUDE_DIR = "env.r_include.dir"; //$NON-NLS-1$
	
	private static final String PREFKEY_INDEX_DIR = "index.dir"; //$NON-NLS-1$
	
	public static final String E_INTERNAL_TYPE = "e-internal";
	
	
	public static File getIndexRootDirectory() {
		final IPath location = RCorePlugin.getDefault().getStateLocation();
		final File file = location.append("indices").toFile();
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}
	
	
	private static String getLibGroupLabel(final String id) {
		if (id.equals(IRLibraryGroup.R_DEFAULT)) {
			return Messages.REnvConfiguration_DefaultLib_label;
		}
		if (id.equals(IRLibraryGroup.R_SITE)) {
			return Messages.REnvConfiguration_SiteLibs_label;
		}
		if (id.equals(IRLibraryGroup.R_OTHER)) {
			return Messages.REnvConfiguration_OtherLibs_label;
		}
		if (id.equals(IRLibraryGroup.R_USER)) {
			return Messages.REnvConfiguration_UserLibs_label;
		}
		return null;
	}
	
	private static final List<IRLibraryLocation> NO_LIBS = Collections.emptyList();
	
	private static final String[] DEFAULT_LIBS_IDS = new String[] {
			IRLibraryGroup.R_DEFAULT, IRLibraryGroup.R_SITE,
			IRLibraryGroup.R_OTHER, IRLibraryGroup.R_USER,
	};
	
	private static final List<IRLibraryGroup> DEFAULT_LIBS_INIT;
	static {
		final RLibraryGroup[] groups = new RLibraryGroup[DEFAULT_LIBS_IDS.length];
		for (int i = 0; i < DEFAULT_LIBS_IDS.length; i++) {
			groups[i] = new RLibraryGroup(DEFAULT_LIBS_IDS[i], getLibGroupLabel(DEFAULT_LIBS_IDS[i]), NO_LIBS);
		}
		DEFAULT_LIBS_INIT = new ConstList<IRLibraryGroup>(groups);
	}
	
	private static final List<IRLibraryGroup> DEFAULT_LIBS_DEFAULTS = new ConstList<IRLibraryGroup>(
			new RLibraryGroup(IRLibraryGroup.R_DEFAULT, getLibGroupLabel(IRLibraryGroup.R_DEFAULT),
				new ConstList<IRLibraryLocation>(new RLibraryLocation(IRLibraryGroup.DEFAULTLOCATION_R_DEFAULT)) ),
			new RLibraryGroup(IRLibraryGroup.R_SITE, getLibGroupLabel(IRLibraryGroup.R_SITE),
				new ConstList<IRLibraryLocation>(new RLibraryLocation(IRLibraryGroup.DEFAULTLOCATION_R_SITE)) ),
			new RLibraryGroup(IRLibraryGroup.R_OTHER, getLibGroupLabel(IRLibraryGroup.R_OTHER), NO_LIBS),
			new RLibraryGroup(IRLibraryGroup.R_USER, getLibGroupLabel(IRLibraryGroup.R_USER), NO_LIBS) );
	
	
	public static class Editable extends REnvConfiguration implements WorkingCopy {
		
		public Editable(final String type, final IREnv link) {
			super(type, link, null, null);
			loadDefaults();
		}
		
		private Editable(final REnvConfiguration config) {
			super(config.fType, config.getReference(), null, null);
			load(config);
		}
		
		
		@Override
		protected List copyLibs(final List<? extends IRLibraryGroup> source) {
			final List<IRLibraryGroup.WorkingCopy> list = new ArrayList<IRLibraryGroup.WorkingCopy>(source.size());
			for (final IRLibraryGroup group : source) {
				list.add(new RLibraryGroup.Editable((RLibraryGroup) group));
			}
			return list;
		}
		
		@Override
		public IRLibraryGroup.WorkingCopy getRLibraryGroup(final String id) {
			return (IRLibraryGroup.WorkingCopy) super.getRLibraryGroup(id);
		}
		
	}
	
	
	private final IREnv fREnv;
	
	public String fType;
	
	private String fNodeQualifier;
	private String fCheckId;
	
	private StringPref fPrefType;
	private StringPref fPrefName;
	private String fName;
	
	private StringPref fPrefRHomeDirectory;
	private String fRHomeDirectory;
	private IntPref fPrefRBits;
	private int fRBits;
	private StringPref fPrefROS;
	private String fROS;
	
	private List<? extends IRLibraryGroup> fRLibraries;
	private StringPref fPrefRDocDirectory;
	private String fRDocDirectory;
	private IFileStore fRDocDirectoryStore;
	private StringPref fPrefRShareDirectory;
	private String fRShareDirectory;
	private IFileStore fRShareDirectoryStore;
	private StringPref fPrefRIncludeDirectory;
	private String fRIncludeDirectory;
	private IFileStore fRIncludeDirectoryStore;
	
	private StringPref fPrefIndexDirectory;
	private String fIndexDirectory;
	private IFileStore fIndexDirectoryStore;
	
	private Properties fSharedProperties;
	private final Object fSharedPropertiesLock = new Object();
	
	
	protected REnvConfiguration(final String type, final IREnv link,
			final IPreferenceAccess prefs, final String key) {
		assert (link != null);
		fType = type;
		fREnv = link;
		fRBits = 32;
		
		fNodeQualifier = RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER + '/' +
				((key != null) ? key : link.getId());
		fRLibraries = DEFAULT_LIBS_INIT;
		if (prefs != null) {
			load(prefs);
		}
	}
	
	public REnvConfiguration(final IREnvConfiguration config) {
		this(config.getType(), config.getReference(), null, null);
		load(config);
	}
	
	protected void checkPrefs() {
		final String id = fREnv.getId();
		if (id.equals(fCheckId)) {
			return;
		}
		fCheckId = id;
		fPrefType = new StringPref(fNodeQualifier, PREFKEY_TYPE);
		fPrefName = new StringPref(fNodeQualifier, PREFKEY_NAME);
		fPrefRBits = new IntPref(fNodeQualifier, PREFKEY_RBITS);
		fPrefROS = new StringPref(fNodeQualifier, PREFKEY_ROS);
		if (fType == USER_LOCAL_TYPE) {
			fPrefRHomeDirectory = new StringPref(fNodeQualifier, PREFKEY_RHOME_DIR);
			fPrefRDocDirectory = new StringPref(fNodeQualifier, PREFKEY_RDOC_DIR);
			fPrefRShareDirectory = new StringPref(fNodeQualifier, PREFKEY_RSHARE_DIR);
			fPrefRIncludeDirectory = new StringPref(fNodeQualifier, PREFKEY_RINCLUDE_DIR);
		}
		fPrefIndexDirectory = new StringPref(fNodeQualifier, PREFKEY_INDEX_DIR);
	}
	
	void upgradePref() {
		fCheckId = null;
		fNodeQualifier = RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER + '/' + fREnv.getId();
	}
	
	protected void checkExistence(final IPreferenceAccess prefs) {
//		final IEclipsePreferences[] nodes = prefs.getPreferenceNodes(RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER);
//		if (nodes.length > 0)  {
//			try {
//				if (!nodes[0].nodeExists(fCheckId)) {
//					throw new IllegalArgumentException("A REnv configuration with this name does not exists."); 
//				}
//			}
//			catch (final BackingStoreException e) {
//				throw new IllegalArgumentException("REnv Configuration could not be accessed."); 
//			}
//		}
	}
	
	
	public IREnv getReference() {
		return fREnv;
	}
	
	public String getType() {
		return fType;
	}
	
	public boolean isEditable() {
		return (fType == USER_LOCAL_TYPE || fType == USER_REMOTE_TYPE);
	}
	
	public boolean isLocal() {
		return (fType == USER_LOCAL_TYPE || fType == E_INTERNAL_TYPE);
	}
	
	public boolean isRemote() {
		return (fType == USER_REMOTE_TYPE);
	}
	
	
	@Override
	public String[] getNodeQualifiers() {
		checkPrefs();
		return new String[] { fNodeQualifier };
	}
	
	@Override
	public void loadDefaults() {
		setName("R"); //$NON-NLS-1$
		if (fType == USER_LOCAL_TYPE) {
			setRHome(""); //$NON-NLS-1$
			fRLibraries = copyLibs(DEFAULT_LIBS_DEFAULTS);
		}
		
		resolvePaths();
	}
	
	public void load(final IREnvConfiguration from) {
		setName(from.getName());
		setRBits(from.getRBits());
		setROS(from.getROS());
		if (fType == USER_LOCAL_TYPE) {
			setRHome(from.getRHome());
			setRDocDirectory(from.getRDocDirectoryPath());
			setRShareDirectory(from.getRShareDirectoryPath());
			setRIncludeDirectory(from.getRIncludeDirectoryPath());
			fRLibraries = copyLibs(from.getRLibraryGroups());
		}
		setIndexDirectoryPath(from.getIndexDirectoryPath());
		
		resolvePaths();
	}
	
	@Override
	public void load(final IPreferenceAccess prefs) {
		checkPrefs();
		checkExistence(prefs);
		final String type = prefs.getPreferenceValue(fPrefType);
		if (USER_REMOTE_TYPE.equals(type)) {
			fType = USER_REMOTE_TYPE;
		}
		else if (E_INTERNAL_TYPE.equals(type)) {
			fType = E_INTERNAL_TYPE;
		}
		else {
			fType = USER_LOCAL_TYPE;
		}
		setName(prefs.getPreferenceValue(fPrefName));
		setRBits(prefs.getPreferenceValue(fPrefRBits));
		setROS(prefs.getPreferenceValue(fPrefROS));
		
		if (fType == USER_LOCAL_TYPE) {
			setRHome(prefs.getPreferenceValue(fPrefRHomeDirectory));
			setRDocDirectory(prefs.getPreferenceValue(fPrefRDocDirectory));
			setRShareDirectory(prefs.getPreferenceValue(fPrefRShareDirectory));
			setRIncludeDirectory(prefs.getPreferenceValue(fPrefRIncludeDirectory));
			
			final String[] ids = DEFAULT_LIBS_IDS;
			final List<IRLibraryGroup> groups = new ArrayList<IRLibraryGroup>(ids.length);
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
					groups.add(new RLibraryGroup(id, label, new ConstList<IRLibraryLocation>(libs)));
				}
				else {
					// unknown group
				}
			}
			fRLibraries = Collections.unmodifiableList(groups);
		}
		
		setIndexDirectoryPath(prefs.getPreferenceValue(fPrefIndexDirectory));
		
		resolvePaths();
	}
	
	protected List copyLibs(final List<? extends IRLibraryGroup> source) {
		final RLibraryGroup[] groups = new RLibraryGroup[source.size()];
		for (int i = 0; i < groups.length; i++) {
			groups[i] = new RLibraryGroup(source.get(i));
		}
		return new ConstList<IRLibraryGroup>(groups);
	}
	
	@Override
	public Map<Preference, Object> deliverToPreferencesMap(
			final Map<Preference, Object> map) {
		checkPrefs();
		map.put(fPrefType, getType());
		map.put(fPrefName, getName());
		map.put(fPrefRBits, getRBits());
		map.put(fPrefROS, getROS());
		
		if (fType == USER_LOCAL_TYPE) {
			map.put(fPrefRHomeDirectory, getRHome());
			map.put(fPrefRDocDirectory, getRDocDirectoryPath());
			map.put(fPrefRShareDirectory, getRShareDirectoryPath());
			map.put(fPrefRIncludeDirectory, getRIncludeDirectoryPath());
			
			final List<? extends IRLibraryGroup> groups = fRLibraries;
			for (final IRLibraryGroup group : groups) {
				final List<? extends IRLibraryLocation> libraries = group.getLibraries();
				final String[] locations = new String[libraries.size()];
				for (int i = 0; i < libraries.size(); i++) {
					locations[i] = libraries.get(i).getDirectoryPath();
				}
				map.put(new StringArrayPref(fNodeQualifier, PREFKEY_RLIBS_PREFIX+group.getId(), Preference.IS2_SEPARATOR_CHAR),
						locations);
			}
			
		}
		
		map.put(fPrefIndexDirectory, getIndexDirectoryPath());
		
		return map;
	}
	
	public Editable createWorkingCopy() {
		return new Editable(this);
	}
	
	public REnvConfiguration getBaseConfiguration() {
		return this;
	}
	
	
/*-- Properties --------------------------------------------------------------*/
	
	public String getName() {
		return fName;
	}
	
	public void setName(final String label) {
		final String oldValue = fName;
		fName = label;
		firePropertyChange(PROP_NAME, oldValue, label);
	}
	
	public boolean isValidRHomeLocation(final IFileStore loc) {
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
	
	public IStatus validate() {
		CoreException error = null;
		if (isLocal()) {
			IFileStore rloc = null;
			try {
				rloc = FileUtil.expandToLocalFileStore(getRHome(), null, null);
			}
			catch (final CoreException e) {
				error = e;
			}
			if (rloc == null || !isValidRHomeLocation(rloc)) {
				return new Status(IStatus.ERROR, RCore.PLUGIN_ID, Messages.REnvConfiguration_Validation_error_InvalidRHome_message, error);
			}
		}
		return Status.OK_STATUS;
	}
	
	public String getRHome() {
		return fRHomeDirectory;
	}
	
	public void setRHome(final String label) {
		final String oldValue = fRHomeDirectory;
		fRHomeDirectory = label;
		firePropertyChange(PROP_RHOME, oldValue, label);
	}
	
	public int getRBits() {
		return fRBits;
	}
	
	public void setRBits(int bits) {
		if (bits != 32 && bits != 64) {
			bits = 32;
		}
		final int oldValue = fRBits;
		fRBits = bits;
		firePropertyChange(PROP_RBITS, oldValue, bits);
	}
	
	public String getROS() {
		return fROS;
	}
	
	public void setROS(final String type) {
		final String oldValue = fROS;
		fROS = type;
		firePropertyChange(PROP_RHOME, oldValue, type);
	}
	
	
	public String getRDocDirectoryPath() {
		return fRDocDirectory;
	}
	
	public void setRDocDirectory(final String directory) {
		final String oldValue = fRDocDirectory;
		fRDocDirectory = directory;
		firePropertyChange(PROP_RDOC_DIRECTORY, oldValue, directory);
	}
	
	public String getRShareDirectoryPath() {
		return fRShareDirectory;
	}
	
	public void setRShareDirectory(final String directory) {
		final String oldValue = fRShareDirectory;
		fRShareDirectory = directory;
		firePropertyChange(PROP_RSHARE_DIRECTORY, oldValue, directory);
	}
	
	public String getRIncludeDirectoryPath() {
		return fRIncludeDirectory;
	}
	
	public void setRIncludeDirectory(final String directory) {
		final String oldValue = fRIncludeDirectory;
		fRIncludeDirectory = directory;
		firePropertyChange(PROP_RINCLUDE_DIRECTORY, oldValue, directory);
	}
	
	public List getRLibraryGroups() {
		return fRLibraries;
	}
	
	public IRLibraryGroup getRLibraryGroup(final String id) {
		for (final IRLibraryGroup group : fRLibraries) {
			if (group.getId().equals(id)) {
				return group;
			}
		}
		return null;
	}
	
	
	public String getIndexDirectoryPath() {
		return fIndexDirectory;
	}
	
	public IFileStore getIndexDirectoryStore() {
		return fIndexDirectoryStore;
	}
	
	public void setIndexDirectoryPath(final String directory) {
		final String oldValue = fIndexDirectory;
		fIndexDirectory = directory;
		firePropertyChange(PROP_INDEX_DIRECTORY, oldValue, directory);
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
			envp.put("R_LIBS_SITE", getLibPath(getRLibraryGroup(IRLibraryGroup.R_SITE))); //$NON-NLS-1$
			envp.put("R_LIBS", getLibPath(getRLibraryGroup(IRLibraryGroup.R_OTHER))); //$NON-NLS-1$
			envp.put("R_LIBS_USER", getLibPath(getRLibraryGroup(IRLibraryGroup.R_USER))); //$NON-NLS-1$
			if (fRDocDirectoryStore != null) {
				envp.put("R_DOC_DIR", URIUtil.toPath(fRDocDirectoryStore.toURI()).toOSString());
			}
			if (fRShareDirectoryStore != null) {
				envp.put("R_SHARE_DIR", URIUtil.toPath(fRShareDirectoryStore.toURI()).toOSString());
			}
			if (fRIncludeDirectoryStore != null) {
				envp.put("R_INCLUDE_DIR", URIUtil.toPath(fRIncludeDirectoryStore.toURI()).toOSString());
			}
		}
		envp.put("LC_NUMERIC", "C"); //$NON-NLS-1$ //$NON-NLS-2$
		return envp;
	}
	
	
	public IFileStore resolvePath(final String path) {
		if (isRemote()) {
			final Properties auto = getSharedProperties();
			if (auto != null) {
				final String hostname = auto.getProperty("renv.hostname");
				if (hostname != null) {
					return NicoCore.mapRemoteResourceToFileStore(hostname, new Path(path), null);
				}
			}
		}
		try {
			return FileUtil.getLocalFileStore(path);
		}
		catch (final CoreException e) {
			return null;
		}
	}
	
	
	private String getLibPath(final IRLibraryGroup group) {
		List<? extends IRLibraryLocation> libs;
		if (group == null || (libs = group.getLibraries()).isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		final StringBuilder sb = new StringBuilder();
		for (final IRLibraryLocation lib : libs) {
			final IFileStore store = lib.getDirectoryStore();
			if (store != null) {
				sb.append(URIUtil.toPath(store.toURI()).toOSString());
			}
			sb.append(File.pathSeparatorChar);
		}
		return sb.substring(0, sb.length()-1);
	}
	
	public Properties getSharedProperties() {
		synchronized (fSharedPropertiesLock) {
			if (fSharedProperties == null) {
				loadSharedProperties();
			}
			return fSharedProperties;
		}
	}
	
	private void loadSharedProperties() {
		synchronized (fSharedPropertiesLock) {
			InputStream in = null;
			try {
				if (fIndexDirectoryStore != null) {
					final IFileStore store = fIndexDirectoryStore.getChild("renv.properties");
					if (store.fetchInfo().exists()) {
						final Properties prop = new Properties();
						in = store.openInputStream(EFS.NONE, null);
						prop.load(in);
						prop.setProperty("stamp", Long.toString(store.fetchInfo().getLastModified()));
						fSharedProperties = prop;
						in.close();
						in = null;
					}
				}
			}
			catch (final Exception e) {
				RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						"An error occurrend when loading shared R environment properties.", e));
				fSharedProperties = new Properties();
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (final IOException e) {}
				}
			}
		}
	}
	
	public void updateSharedProperties(final Map<String, String> properties) {
		if (properties == null) {
			return;
		}
		synchronized (fSharedPropertiesLock) {
			fSharedProperties = null;
			final Properties prop = new Properties();
			prop.putAll(properties);
			saveSharedProperties(prop);
			final IREnvConfiguration config = getReference().getConfig();
			if (config != null && config instanceof REnvConfiguration) {
				((REnvConfiguration) config).loadSharedProperties();
			}
		}
	}
	
	private void saveSharedProperties(final Properties prop) {
		OutputStream out = null;
		try {
			if (fIndexDirectoryStore != null) {
				final IFileStore store = fIndexDirectoryStore.getChild("renv.properties");
				out = store.openOutputStream(EFS.NONE, null);
				prop.store(out, null);
				out.close();
				out = null;
			}
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
					"An error occurrend when saving shared R environment properties.", e));
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (final IOException e) {}
			}
		}
	}
	
	private void resolvePaths() {
		if (!isLocal() && fIndexDirectory == null) {
			return;
		}
		String rHome = null;
		try {
			final String s = getRHome();
			if (s != null) {
				rHome = URIUtil.toPath(FileUtil.expandToLocalFileStore(s, null, null).toURI()).toOSString();
			}
		}
		catch (final CoreException e) {}
		final String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		
		fIndexDirectoryStore = checkPath(fIndexDirectory, rHome, userHome);
		if (fIndexDirectoryStore == null) {
			try {
				final File directory = new File(getIndexRootDirectory(), getReference().getId());
				fIndexDirectoryStore = EFS.getStore(directory.toURI());
			}
			catch (final CoreException e) {
			}
		}
		if (isLocal()) {
			for (final IRLibraryGroup group : fRLibraries) {
				for (final IRLibraryLocation lib : group.getLibraries()) {
					if (lib instanceof RLibraryLocation) {
						final RLibraryLocation l = (RLibraryLocation) lib;
						l.fStore = checkPath(l.fPath, rHome, userHome);
					}
				}
			}
			fRDocDirectoryStore = checkPath(fRDocDirectory, rHome, userHome);
			fRShareDirectoryStore = checkPath(fRShareDirectory, rHome, userHome);
			fRIncludeDirectoryStore = checkPath(fRIncludeDirectory, rHome, userHome);
		}
	}
	
	private IFileStore checkPath(String path, final String rHome, final String userHome) {
		try {
			if (path != null && path.length() > 0) {
				if (rHome != null) {
					path = path.replace("${env_var:R_HOME}", rHome); //$NON-NLS-1$
				}
				else if (path.contains("${env_var:R_HOME}")) { //$NON-NLS-1$
					return null;
				}
				if (userHome != null) {
					if (path.startsWith("~")) {
						path = userHome + path.substring(1);
					}
				}
				else {
					if (path.startsWith("~")) {
						return null;
					}
				}
				return FileUtil.expandToLocalFileStore(path, null, null);
			}
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID, 0,
					NLS.bind("Could not resolve configured path ''{0}}'' of " +
							"R environment configuration ''{1}''.", path, fName ), e));
		}
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return fREnv.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IREnvConfiguration)) {
			return false;
		}
		final IREnvConfiguration other = (IREnvConfiguration) obj;
		return (fREnv.equals(other.getReference())
				&& (fType == other.getType())
				&& ((fName != null) ? fName.equals(other.getName()) : null == other.getName())
				&& (fRBits == other.getRBits())
				&& ((fROS != null) ? fROS.equals(other.getROS()) : null == other.getROS())
				&& ((fType == USER_LOCAL_TYPE
						&& ((fRHomeDirectory != null) ? fRHomeDirectory.equals(other.getRHome()) : null == other.getRHome())
						&& ((fRDocDirectory != null) ? fRDocDirectory.equals(other.getRDocDirectoryPath()) : null == other.getRDocDirectoryPath())
						&& ((fRShareDirectory != null) ? fRShareDirectory.equals(other.getRShareDirectoryPath()) : null == other.getRShareDirectoryPath())
						&& ((fRIncludeDirectory != null) ? fRIncludeDirectory.equals(other.getRIncludeDirectoryPath()) : null == other.getRIncludeDirectoryPath())
						&& ((fRLibraries != null) ? fRLibraries.equals(other.getRLibraryGroups()) : null == fRLibraries)
				) || (fType == USER_REMOTE_TYPE) || (fType == E_INTERNAL_TYPE) )
				&& ((fIndexDirectory != null) ? fIndexDirectory.equals(other.getIndexDirectoryPath()) : null == other.getIndexDirectoryPath())
				);
	}
	
}
