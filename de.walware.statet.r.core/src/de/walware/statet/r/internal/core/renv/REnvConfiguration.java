/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.net.resourcemapping.IResourceMappingManager;
import de.walware.ecommons.net.resourcemapping.ResourceMappingUtils;
import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.IntPref;
import de.walware.ecommons.preferences.Preference.StringArrayPref;
import de.walware.ecommons.preferences.Preference.StringPref;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.rj.rsetups.RSetup;

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
	
	private static final String PREFKEY_SUBARCH = "env.sub_arch"; //$NON-NLS-1$
	
	private static final String PREFKEY_RBITS = "env.r_bits.count"; //$NON-NLS-1$
	
	private static final String PREFKEY_ROS = "env.r_os.type"; //$NON-NLS-1$
	
	private static final String PREFKEY_RLIBS_PREFIX = "env.r_libs."; //$NON-NLS-1$
	
	private static final String PREFKEY_RDOC_DIR = "env.r_doc.dir"; //$NON-NLS-1$
	private static final String PREFKEY_RSHARE_DIR = "env.r_share.dir"; //$NON-NLS-1$
	private static final String PREFKEY_RINCLUDE_DIR = "env.r_include.dir"; //$NON-NLS-1$
	
	private static final String PREFKEY_INDEX_DIR = "index.dir"; //$NON-NLS-1$
	
	
	private static final int LOCAL_WIN = 1;
	private static final int LOCAL_LINUX = 2;
	private static final int LOCAL_MAC = 3;
	private static final int LOCAL_PLATFORM;
	
	static {
		if (Platform.getOS().startsWith("win32")) { //$NON-NLS-1$
			LOCAL_PLATFORM = LOCAL_WIN;
		}
		else if (Platform.getOS().startsWith("mac")) { //$NON-NLS-1$
			LOCAL_PLATFORM = LOCAL_MAC;
		}
		else {
			LOCAL_PLATFORM = LOCAL_LINUX;
		}
	}
	
	
	public static File getIndexRootDirectory() {
		final IPath location = RCorePlugin.getDefault().getStateLocation();
		final File file = location.append("indices").toFile(); //$NON-NLS-1$
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
		if (id.equals(IRLibraryGroup.R_USER)) {
			return Messages.REnvConfiguration_UserLibs_label;
		}
		if (id.equals(IRLibraryGroup.R_OTHER)) {
			return Messages.REnvConfiguration_OtherLibs_label;
		}
		return null;
	}
	
	private static final List<RLibraryLocation> NO_LIBS = Collections.emptyList();
	
	private static final String[] DEFAULT_LIBS_IDS = new String[] {
			IRLibraryGroup.R_DEFAULT, IRLibraryGroup.R_SITE,
			IRLibraryGroup.R_USER, IRLibraryGroup.R_OTHER
	};
	
	private static final List<IRLibraryGroup> DEFAULT_LIBS_INIT;
	static {
		final IRLibraryGroup[] groups = new IRLibraryGroup[DEFAULT_LIBS_IDS.length];
		for (int i = 0; i < DEFAULT_LIBS_IDS.length; i++) {
			groups[i] = new RLibraryGroup.Final(DEFAULT_LIBS_IDS[i], getLibGroupLabel(DEFAULT_LIBS_IDS[i]), NO_LIBS);
		}
		DEFAULT_LIBS_INIT = new ConstList<IRLibraryGroup>(groups);
	}
	
	private static final List<IRLibraryGroup> DEFAULT_LIBS_DEFAULTS = new ConstList<IRLibraryGroup>(
			new RLibraryGroup.Final(IRLibraryGroup.R_DEFAULT, getLibGroupLabel(IRLibraryGroup.R_DEFAULT),
				new ConstList<RLibraryLocation>(new RLibraryLocation(IRLibraryGroup.DEFAULTLOCATION_R_DEFAULT)) ),
			new RLibraryGroup.Final(IRLibraryGroup.R_SITE, getLibGroupLabel(IRLibraryGroup.R_SITE),
				new ConstList<RLibraryLocation>(new RLibraryLocation(IRLibraryGroup.DEFAULTLOCATION_R_SITE)) ),
			new RLibraryGroup.Final(IRLibraryGroup.R_USER, getLibGroupLabel(IRLibraryGroup.R_USER), NO_LIBS),
			new RLibraryGroup.Final(IRLibraryGroup.R_OTHER, getLibGroupLabel(IRLibraryGroup.R_OTHER), NO_LIBS) );
	
	
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
		protected List<IRLibraryGroup.WorkingCopy> copyLibs(final List<? extends IRLibraryGroup> source) {
			final List<IRLibraryGroup.WorkingCopy> list = new ArrayList<IRLibraryGroup.WorkingCopy>(source.size());
			for (final IRLibraryGroup group : source) {
				list.add(new RLibraryGroup.Editable((RLibraryGroup) group));
			}
			return list;
		}
		
		@Override
		public List<IRLibraryGroup.WorkingCopy> getRLibraryGroups() {
			return (List<IRLibraryGroup.WorkingCopy>) fRLibraries;
		}
		
		@Override
		public IRLibraryGroup.WorkingCopy getRLibraryGroup(final String id) {
			return (IRLibraryGroup.WorkingCopy) super.getRLibraryGroup(id);
		}
		
	}
	
	
	private final IREnv fREnv;
	
	private String fType;
	
	private String fNodeQualifier;
	private String fCheckId;
	
	private StringPref fPrefType;
	private StringPref fPrefName;
	private String fName;
	
	private StringPref fPrefRHomeDirectory;
	private String fRHomeDirectory;
	private StringPref fPrefSubArch;
	private String fSubArch;
	private IntPref fPrefRBits;
	private int fRBits;
	private StringPref fPrefROS;
	private String fROS;
	
	protected List<? extends IRLibraryGroup> fRLibraries;
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
		fRBits = 64;
		
		fNodeQualifier = RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER + '/' +
				((key != null) ? key : link.getId());
		fRLibraries = DEFAULT_LIBS_INIT;
		if (prefs != null) {
			load(prefs);
		}
	}
	
	REnvConfiguration(final IREnv link, final RSetup setup) {
		assert (link != null && setup != null);
		fType = EPLUGIN_LOCAL_TYPE;
		fREnv = link;
		
		setName(setup.getName());
		setROS(setup.getOS());
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			String arch = setup.getOSArch();
			if (arch == null || arch.isEmpty()) {
				arch = Platform.getOSArch();
			}
			setSubArch(arch);
		}
		fRBits = (Platform.ARCH_X86.equals(setup.getOSArch())) ? 32 : 64;
		setRHome(setup.getRHome());
		setRDocDirectoryPath("${env_var:R_HOME}/doc"); //$NON-NLS-1$
		setRShareDirectoryPath("${env_var:R_HOME}/share"); //$NON-NLS-1$
		setRIncludeDirectoryPath("${env_var:R_HOME}/include"); //$NON-NLS-1$
		
		final String[] ids = DEFAULT_LIBS_IDS;
		final List<IRLibraryGroup> groups = new ArrayList<IRLibraryGroup>(ids.length);
		for (int i = 0; i < ids.length; i++) {
			final String id = ids[i];
			final String label = getLibGroupLabel(id);
			if (label != null) {
				final List<String> locations;
				if (id.equals(IRLibraryGroup.R_SITE)) {
					locations = setup.getRLibsSite();
				}
				else if (id.equals(IRLibraryGroup.R_USER)) {
					locations = setup.getRLibsUser();
				}
				else if (id.equals(IRLibraryGroup.R_OTHER)) {
					locations = setup.getRLibs();
				}
				else {
					continue;
				}
				final RLibraryLocation[] libs = new RLibraryLocation[(locations != null) ? locations.size() : 0];
				for (int j = 0; j < locations.size(); j++) {
					libs[j] = new RLibraryLocation(locations.get(j));
				}
				groups.add(new RLibraryGroup.Final(id, label, new ConstList<RLibraryLocation>(libs)));
			}
			else {
				// unknown group
			}
		}
		fRLibraries = Collections.unmodifiableList(groups);
		
		resolvePaths();
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
		fPrefROS = new StringPref(fNodeQualifier, PREFKEY_ROS);
		if (fType == USER_LOCAL_TYPE) {
			fPrefRHomeDirectory = new StringPref(fNodeQualifier, PREFKEY_RHOME_DIR);
			fPrefSubArch = new StringPref(fNodeQualifier, PREFKEY_SUBARCH);
			fPrefRBits = new IntPref(fNodeQualifier, PREFKEY_RBITS);
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
	
	
	@Override
	public IREnv getReference() {
		return fREnv;
	}
	
	@Override
	public String getType() {
		return fType;
	}
	
	@Override
	public boolean isEditable() {
		return (fType == USER_LOCAL_TYPE || fType == USER_REMOTE_TYPE);
	}
	
	@Override
	public boolean isLocal() {
		return (fType == USER_LOCAL_TYPE || fType == EPLUGIN_LOCAL_TYPE);
	}
	
	@Override
	public boolean isRemote() {
		return (fType == USER_REMOTE_TYPE);
	}
	
	
	@Override
	public String[] getNodeQualifiers() {
		if (fType == EPLUGIN_LOCAL_TYPE) {
			return new String[0];
		}
		
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
		setROS(from.getROS());
		if (isLocal()) {
			setRHome(from.getRHome());
			setSubArch(from.getSubArch());
			fRBits = (from instanceof REnvConfiguration) ? ((REnvConfiguration) from).fRBits : 64;
			setRDocDirectoryPath(from.getRDocDirectoryPath());
			setRShareDirectoryPath(from.getRShareDirectoryPath());
			setRIncludeDirectoryPath(from.getRIncludeDirectoryPath());
			fRLibraries = copyLibs(from.getRLibraryGroups());
		}
		setIndexDirectoryPath(from.getIndexDirectoryPath());
		
		resolvePaths();
	}
	
	@Override
	public void load(final IPreferenceAccess prefs) {
		if (fType == EPLUGIN_LOCAL_TYPE) {
			return;
		}
		
		checkPrefs();
		checkExistence(prefs);
		final String type = prefs.getPreferenceValue(fPrefType);
		if (USER_REMOTE_TYPE.equals(type)) {
			fType = USER_REMOTE_TYPE;
		}
		else if (EPLUGIN_LOCAL_TYPE.equals(type)) {
			fType = EPLUGIN_LOCAL_TYPE;
		}
		else {
			fType = USER_LOCAL_TYPE;
		}
		setName(prefs.getPreferenceValue(fPrefName));
		setROS(prefs.getPreferenceValue(fPrefROS));
		
		if (fType == USER_LOCAL_TYPE) {
			setRHome(prefs.getPreferenceValue(fPrefRHomeDirectory));
			setSubArch(prefs.getPreferenceValue(fPrefSubArch));
			fRBits = prefs.getPreferenceValue(fPrefRBits);
			setRDocDirectoryPath(prefs.getPreferenceValue(fPrefRDocDirectory));
			setRShareDirectoryPath(prefs.getPreferenceValue(fPrefRShareDirectory));
			setRIncludeDirectoryPath(prefs.getPreferenceValue(fPrefRIncludeDirectory));
			
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
					groups.add(new RLibraryGroup.Final(id, label, new ConstList<RLibraryLocation>(libs)));
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
	
	protected List<? extends IRLibraryGroup> copyLibs(final List<? extends IRLibraryGroup> source) {
		final IRLibraryGroup[] groups = new RLibraryGroup[source.size()];
		for (int i = 0; i < groups.length; i++) {
			groups[i] = new RLibraryGroup.Final((RLibraryGroup) source.get(i));
		}
		return new ConstList<IRLibraryGroup>(groups);
	}
	
	@Override
	public Map<Preference<?>, Object> deliverToPreferencesMap(final Map<Preference<?>, Object> map) {
		if (fType == EPLUGIN_LOCAL_TYPE) {
			return map;
		}
		
		checkPrefs();
		map.put(fPrefType, getType());
		map.put(fPrefName, getName());
		map.put(fPrefROS, getROS());
		
		if (fType == USER_LOCAL_TYPE) {
			map.put(fPrefRHomeDirectory, getRHome());
			map.put(fPrefSubArch, fSubArch);
			map.put(fPrefRBits, fRBits);
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
	
	@Override
	public Editable createWorkingCopy() {
		return new Editable(this);
	}
	
	public REnvConfiguration getBaseConfiguration() {
		return this;
	}
	
	
/*-- Properties --------------------------------------------------------------*/
	
	@Override
	public String getName() {
		return fName;
	}
	
	public void setName(final String label) {
		final String oldValue = fName;
		fName = label;
		firePropertyChange(PROP_NAME, oldValue, label);
	}
	
	public boolean isValidRHomeLocation(final IFileStore rHome) {
		final IFileStore binDir = rHome.getChild("bin"); //$NON-NLS-1$
		IFileStore exeFile = null;
		switch (LOCAL_PLATFORM) {
		case LOCAL_WIN:
			exeFile = binDir.getChild("R.exe"); //$NON-NLS-1$
			break;
		default:
			exeFile = binDir.getChild("R"); //$NON-NLS-1$
			break;
		}
		final IFileInfo info = exeFile.fetchInfo();
		return (!info.isDirectory() && info.exists());
	}
	
	public List<String> searchAvailableSubArchs(final IFileStore rHome) {
		if (rHome != null && rHome.fetchInfo().exists()) {
			try {
				final IFileStore rHomeBinSub;
				final String name;
				switch (LOCAL_PLATFORM) {
				case LOCAL_WIN:
					rHomeBinSub = rHome.getChild("bin"); //$NON-NLS-1$
					name = "R.exe"; //$NON-NLS-1$
					break;
				default:
					rHomeBinSub = rHome.getChild("bin").getChild("exec"); //$NON-NLS-1$ //$NON-NLS-2$
					name = "R";  //$NON-NLS-1$
					break;
				}
				if (rHomeBinSub.fetchInfo().exists()) {
					final IFileStore[] subDirs = rHomeBinSub.childStores(EFS.NONE, null);
					final List<String> archs = new ArrayList<String>();
					for (final IFileStore subDir : subDirs) {
						if (subDir.getChild(name).fetchInfo().exists()) {
							final String arch = checkArchForStatet(subDir.getName());
							if (arch != null) {
								archs.add(arch);
							}
						}
					}
					return archs;
				}
			}
			catch (final CoreException e) {}
		}
		return null;
	}
	
	private String checkArchForStatet(String arch) {
		if (arch == null || arch.isEmpty()) {
			return null;
		}
		if (arch.charAt(0) == '/') {
			arch = arch.substring(1);
			if (arch.isEmpty()) {
				return null;
			}
		}
		if ((arch.equals("exec"))) { //$NON-NLS-1$
			return null;
		}
		if (LOCAL_PLATFORM == LOCAL_WIN) { //$NON-NLS-1$
			if (arch.equals("x64")) {
				return "x86_64"; //$NON-NLS-1$
			}
			if (arch.equals("i386")) { //$NON-NLS-1$
				return "x86"; //$NON-NLS-1$
			}
		}
		return arch;
	}
	
	private String checkArchForR(String arch) {
		if (arch == null) {
			return null;
		}
		if (arch.charAt(0) != '/') {
			arch = '/' + arch;
		}
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			if (arch.equals("/x86_64")) { //$NON-NLS-1$
				return "/x64"; //$NON-NLS-1$
			}
			if (arch.equals("/x86")) {
				return "/i386"; //$NON-NLS-1$
			}
		}
		return arch;
	}
	
	@Override
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
	
	@Override
	public String getRHome() {
		return fRHomeDirectory;
	}
	
	public void setRHome(final String label) {
		final String oldValue = fRHomeDirectory;
		fRHomeDirectory = label;
		firePropertyChange(PROP_RHOME, oldValue, label);
	}
	
	@Override
	public String getSubArch() {
		return fSubArch;
	}
	
	public void setSubArch(String arch) {
		arch = checkArchForStatet(arch);
		final String oldValue = fSubArch;
		fSubArch = arch;
		firePropertyChange(PROP_SUBARCH, oldValue, arch);
	}
	
	@Override
	public String getROS() {
		return fROS;
	}
	
	public void setROS(final String type) {
		final String oldValue = fROS;
		fROS = type;
		firePropertyChange(PROP_RHOME, oldValue, type);
	}
	
	
	@Override
	public String getRDocDirectoryPath() {
		return fRDocDirectory;
	}
	
	public void setRDocDirectoryPath(final String directory) {
		final String oldValue = fRDocDirectory;
		fRDocDirectory = directory;
		firePropertyChange(PROP_RDOC_DIRECTORY, oldValue, directory);
	}
	
	@Override
	public String getRShareDirectoryPath() {
		return fRShareDirectory;
	}
	
	public void setRShareDirectoryPath(final String directory) {
		final String oldValue = fRShareDirectory;
		fRShareDirectory = directory;
		firePropertyChange(PROP_RSHARE_DIRECTORY, oldValue, directory);
	}
	
	@Override
	public String getRIncludeDirectoryPath() {
		return fRIncludeDirectory;
	}
	
	public void setRIncludeDirectoryPath(final String directory) {
		final String oldValue = fRIncludeDirectory;
		fRIncludeDirectory = directory;
		firePropertyChange(PROP_RINCLUDE_DIRECTORY, oldValue, directory);
	}
	
	@Override
	public List<? extends IRLibraryGroup> getRLibraryGroups() {
		return fRLibraries;
	}
	
	@Override
	public IRLibraryGroup getRLibraryGroup(final String id) {
		for (final IRLibraryGroup group : fRLibraries) {
			if (group.getId().equals(id)) {
				return group;
			}
		}
		return null;
	}
	
	
	@Override
	public String getIndexDirectoryPath() {
		return fIndexDirectory;
	}
	
	@Override
	public IFileStore getIndexDirectoryStore() {
		return fIndexDirectoryStore;
	}
	
	public void setIndexDirectoryPath(final String directory) {
		final String oldValue = fIndexDirectory;
		fIndexDirectory = directory;
		firePropertyChange(PROP_INDEX_DIRECTORY, oldValue, directory);
	}
	
	
	@Override
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
	
	@Override
	public List<String> getExecCommand(final Exec execType) throws CoreException {
		final List<IFileStore> binDirs = getBinDirs();
		IFileStore exe = null;
		final List<String> commandLine = new ArrayList<String>(2);
		switch (execType) {
		case TERM:
			if (LOCAL_PLATFORM == LOCAL_WIN) {
				exe = getExisting(binDirs, "Rterm.exe"); //$NON-NLS-1$
			}
			break;
		case CMD:
			if (LOCAL_PLATFORM == LOCAL_WIN) {
				exe = getExisting(binDirs, "Rcmd.exe"); //$NON-NLS-1$
			}
			if (exe == null) {
				commandLine.add("CMD"); //$NON-NLS-1$
			}
			break;
		default:
			break;
		}
		if (exe == null) {
			if (LOCAL_PLATFORM == LOCAL_WIN) {
				exe = getExisting(binDirs, "R.exe"); //$NON-NLS-1$
			}
			else {
				exe = getExisting(binDirs, "R"); //$NON-NLS-1$
			}
		}
		
		commandLine.add(0, URIUtil.toPath(exe.toURI()).toOSString());
		return commandLine;
	}
	
	private List<IFileStore> getBinDirs() throws CoreException {
		final IFileStore rHome = FileUtil.expandToLocalFileStore(getRHome(), null, null);
		final IFileStore rHomeBin = rHome.getChild("bin"); //$NON-NLS-1$
		final IFileStore rHomeBinSub;
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			rHomeBinSub = rHomeBin;
		}
		else { // use wrapper shell scripts
			rHomeBinSub = null; // rHomeBin.getChild("exec"); //$NON-NLS-1$
		}
		final List<IFileStore> dirs = new ArrayList<IFileStore>(4);
		String arch = fSubArch;
		if (arch == null) {
			arch = Platform.getOSArch();
		}
		if (arch != null && rHomeBinSub != null) {
			final IFileStore rHomeBinArch;
			if (arch.equals(Platform.ARCH_X86_64)) {
				rHomeBinArch = getExistingChild(rHomeBinSub, "x86_64", "x64"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (arch.equals(Platform.ARCH_X86)) {
				rHomeBinArch = getExistingChild(rHomeBinSub, "x86", "i386", "i586", "i686"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else {
				rHomeBinArch = getExistingChild(rHomeBinSub, arch);
			}
			if (rHomeBinArch != null) {
				dirs.add(rHomeBinArch);
			}
		}
		dirs.add(rHomeBin);
		return dirs;
	}
	
	private List<IFileStore> getLibDirs() throws CoreException {
		final IFileStore rHome = FileUtil.expandToLocalFileStore(getRHome(), null, null);
		final IFileStore rHomeLib;
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			rHomeLib = rHome.getChild("bin"); //$NON-NLS-1$
		}
		else {
			rHomeLib = rHome.getChild("lib"); //$NON-NLS-1$
		}
		final List<IFileStore> dirs = new ArrayList<IFileStore>(4);
		String arch = fSubArch;
		if (arch == null) {
			arch = Platform.getOSArch();
		}
		if (arch != null && LOCAL_PLATFORM != LOCAL_MAC) {
			final IFileStore rHomeBinArch;
			if (arch.equals(Platform.ARCH_X86_64)) {
				rHomeBinArch = getExistingChild(rHomeLib, "x86_64", "x64"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (arch.equals(Platform.ARCH_X86)) {
				rHomeBinArch = getExistingChild(rHomeLib, "x86", "i386", "i586", "i686"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else {
				rHomeBinArch = getExistingChild(rHomeLib, arch);
			}
			if (rHomeBinArch != null) {
				dirs.add(rHomeBinArch);
			}
		}
		dirs.add(rHomeLib);
		return dirs;
	}
	
	private IFileStore getExistingChild(final IFileStore base, final String... names) {
		for (final String name : names) {
			final IFileStore child = base.getChild(name);
			if (child.fetchInfo().exists()) {
				return child;
			}
		}
		return null;
	}
	
	private IFileStore getExisting(final List<IFileStore> dirs, final String name) {
		IFileStore file = null;
		for (final IFileStore dir : dirs) {
			file = dir.getChild(name);
			if (file.fetchInfo().exists()) {
				break;
			}
		}
		return file;
	}
	
	@Override
	public Map<String, String> getEnvironmentsVariables() throws CoreException {
		return getEnvironmentsVariables(true);
	}
	
	@Override
	public Map<String, String> getEnvironmentsVariables(final boolean configureRLibs) throws CoreException {
		final Map<String, String> envp = new HashMap<String, String>();
		final IFileStore rHomeStore = FileUtil.expandToLocalFileStore(getRHome(), null, null);
		final String rHome = URIUtil.toPath(rHomeStore.toURI()).toOSString();
		envp.put("R_HOME", rHome); //$NON-NLS-1$
		switch (LOCAL_PLATFORM) {
		case LOCAL_WIN:
			envp.put("PATH", //$NON-NLS-1$
					URIUtil.toPath(getExisting(getBinDirs(), "R.dll").getParent().toURI()).toOSString() + //$NON-NLS-1$
							File.pathSeparatorChar + "${env_var:PATH}"); //$NON-NLS-1$
			// libs in path
			break;
		case LOCAL_MAC:
			envp.put("PATH", //$NON-NLS-1$
					URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, "bin").toURI()).toOSString() + //$NON-NLS-1$
							File.pathSeparatorChar + "${env_var:PATH}"); //$NON-NLS-1$
			envp.put("DYLD_LIBRARY_PATH", //$NON-NLS-1$
					URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, "lib").toURI()).toOSString() + //$NON-NLS-1$
							File.pathSeparatorChar + "${env_var:DYLD_LIBRARY_PATH}"); //$NON-NLS-1$
			break;
		default:
			envp.put("PATH", //$NON-NLS-1$
					URIUtil.toPath(FileUtil.expandToLocalFileStore(getRHome(), null, "bin").toURI()).toOSString() + //$NON-NLS-1$
							File.pathSeparatorChar + "${env_var:PATH}"); //$NON-NLS-1$
			envp.put("LD_LIBRARY_PATH", //$NON-NLS-1$
					URIUtil.toPath(getExisting(getLibDirs(), "libR.so").getParent().toURI()).toOSString() + //$NON-NLS-1$
							File.pathSeparatorChar + "${env_var:LD_LIBRARY_PATH}"); //$NON-NLS-1$
			break;
		}
		if (configureRLibs) {
			final String arch = fSubArch;
			final List<String> availableArchs = searchAvailableSubArchs(rHomeStore);
			if (availableArchs != null && availableArchs.contains(arch)) {
				if (arch != null) {
					envp.put("R_ARCH", checkArchForR(arch)); //$NON-NLS-1$
				}
			}
			envp.put("R_LIBS_SITE", getLibPath(getRLibraryGroup(IRLibraryGroup.R_SITE))); //$NON-NLS-1$
			envp.put("R_LIBS_USER", getLibPath(getRLibraryGroup(IRLibraryGroup.R_USER))); //$NON-NLS-1$
			envp.put("R_LIBS", getLibPath(getRLibraryGroup(IRLibraryGroup.R_OTHER))); //$NON-NLS-1$
			if (fRDocDirectoryStore != null) {
				envp.put("R_DOC_DIR", URIUtil.toPath(fRDocDirectoryStore.toURI()).toOSString()); //$NON-NLS-1$
			}
			if (fRShareDirectoryStore != null) {
				envp.put("R_SHARE_DIR", URIUtil.toPath(fRShareDirectoryStore.toURI()).toOSString()); //$NON-NLS-1$
			}
			if (fRIncludeDirectoryStore != null) {
				envp.put("R_INCLUDE_DIR", URIUtil.toPath(fRIncludeDirectoryStore.toURI()).toOSString()); //$NON-NLS-1$
			}
		}
		envp.put("LC_NUMERIC", "C"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (PreferencesUtil.getInstancePrefs().getPreferenceValue(RCorePreferenceNodes.PREF_RENV_NETWORK_USE_ECLIPSE)) {
			configureNetwork(envp);
		}
		
		return envp;
	}
	
	protected void configureNetwork(final Map<String, String> envp) {
		final IProxyService proxyService = RCorePlugin.getDefault().getProxyService();
		if (proxyService != null && proxyService.isProxiesEnabled()) {
			final StringBuilder sb = new StringBuilder();
			{	final String[] nonProxiedHosts = proxyService.getNonProxiedHosts();
				if (nonProxiedHosts.length > 0) {
					sb.setLength(0);
					sb.append(nonProxiedHosts[0]);
					for (int i = 1; i < nonProxiedHosts.length; i++) {
						sb.append(',');
						sb.append(nonProxiedHosts[i]);
					}
					envp.put("no_proxy", sb.toString()); //$NON-NLS-1$
				}
			}
			if (LOCAL_PLATFORM == LOCAL_WIN && proxyService.isSystemProxiesEnabled()) {
				envp.put("R_NETWORK", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				IProxyData data = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE);
				if (data != null && data.getHost() != null) {
					sb.setLength(0);
					sb.append("http://"); //$NON-NLS-1$
					if (data.isRequiresAuthentication()) {
						if (data.getPassword() == null || data.getPassword().isEmpty()) {
							envp.put("http_proxy_user", "ask"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							sb.append((data.getUserId() != null) ? data.getUserId() : "") //$NON-NLS-1$
									.append(':').append(data.getPassword());
							sb.append('@');
						}
					}
					sb.append(data.getHost());
					if (data.getPort() > 0) {
						sb.append(':').append(data.getPort());
					}
					sb.append('/');
					envp.put("http_proxy", sb.toString());
				}
				
				data = proxyService.getProxyData("FTP"); //$NON-NLS-1$
				if (data != null && data.getHost() != null) {
					sb.setLength(0);
					sb.append("ftp://"); //$NON-NLS-1$
					sb.append(data.getHost());
					if (data.getPort() > 0) {
						sb.append(':').append(data.getPort());
					}
					sb.append('/');
					envp.put("ftp_proxy", sb.toString()); //$NON-NLS-1$
					
					if (data.isRequiresAuthentication()) {
						if (data.getUserId() != null) {
							envp.put("ftp_proxy_user", data.getUserId()); //$NON-NLS-1$
						}
						if (data.getPassword() != null) {
							envp.put("ftp_proxy_password", data.getPassword()); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}
	
	
	public IFileStore resolvePath(final String path) {
		if (isRemote()) {
			final Properties auto = getSharedProperties();
			if (auto != null) {
				final String hostname = auto.getProperty("renv.hostname"); //$NON-NLS-1$
				if (hostname != null) {
					final IResourceMappingManager rmManager = ResourceMappingUtils.getManager();
					if (rmManager != null) {
						return rmManager.mapRemoteResourceToFileStore(hostname, new Path(path), null);
					}
					return null;
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
					final IFileStore store = fIndexDirectoryStore.getChild("renv.properties"); //$NON-NLS-1$
					if (store.fetchInfo().exists()) {
						final Properties prop = new Properties();
						in = store.openInputStream(EFS.NONE, null);
						prop.load(in);
						prop.setProperty("stamp", Long.toString(store.fetchInfo().getLastModified())); //$NON-NLS-1$
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
				final IFileStore store = fIndexDirectoryStore.getChild("renv.properties"); //$NON-NLS-1$
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
					if (path.startsWith("~/")) { //$NON-NLS-1$
						path = userHome + path.substring(1);
					}
				}
				else {
					if (path.startsWith("~/")) { //$NON-NLS-1$
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
				&& ((fSubArch != null) ? fSubArch.equals(other.getSubArch()) : null == other.getSubArch())
				&& ((fROS != null) ? fROS.equals(other.getROS()) : null == other.getROS())
				&& ((fType == USER_LOCAL_TYPE
						&& ((fRHomeDirectory != null) ? fRHomeDirectory.equals(other.getRHome()) : null == other.getRHome())
						&& ((fRDocDirectory != null) ? fRDocDirectory.equals(other.getRDocDirectoryPath()) : null == other.getRDocDirectoryPath())
						&& ((fRShareDirectory != null) ? fRShareDirectory.equals(other.getRShareDirectoryPath()) : null == other.getRShareDirectoryPath())
						&& ((fRIncludeDirectory != null) ? fRIncludeDirectory.equals(other.getRIncludeDirectoryPath()) : null == other.getRIncludeDirectoryPath())
						&& ((fRLibraries != null) ? fRLibraries.equals(other.getRLibraryGroups()) : null == fRLibraries)
				) || (fType == USER_REMOTE_TYPE) || (fType == EPLUGIN_LOCAL_TYPE) )
				&& ((fIndexDirectory != null) ? fIndexDirectory.equals(other.getIndexDirectoryPath()) : null == other.getIndexDirectoryPath())
				);
	}
	
}
