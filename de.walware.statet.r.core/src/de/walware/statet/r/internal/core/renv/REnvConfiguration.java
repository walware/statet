/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.renv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.net.resourcemapping.IResourceMappingManager;
import de.walware.ecommons.net.resourcemapping.ResourceMappingUtils;
import de.walware.ecommons.preferences.AbstractPreferencesModelObject;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.core.Preference.IntPref;
import de.walware.ecommons.preferences.core.Preference.StringArrayPref;
import de.walware.ecommons.preferences.core.Preference.StringPref;

import de.walware.rj.rsetups.RSetup;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCorePreferenceNodes;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.core.Messages;
import de.walware.statet.r.internal.core.RCorePlugin;


public class REnvConfiguration extends AbstractPreferencesModelObject implements IREnvConfiguration {
	
	
	private static final String PREFKEY_TYPE= "type"; //$NON-NLS-1$
	
	private static final String PREFKEY_NAME= "name"; //$NON-NLS-1$
	
	private static final String PREFKEY_RHOME_DIR= "env.r_home"; //$NON-NLS-1$
	
	private static final String PREFKEY_SUBARCH= "env.sub_arch"; //$NON-NLS-1$
	
	private static final String PREFKEY_RBITS= "env.r_bits.count"; //$NON-NLS-1$
	
	private static final String PREFKEY_ROS= "env.r_os.type"; //$NON-NLS-1$
	
	private static final String PREFKEY_RLIBS_PREFIX= "env.r_libs."; //$NON-NLS-1$
	
	private static final String PREFKEY_RDOC_DIR= "env.r_doc.dir"; //$NON-NLS-1$
	private static final String PREFKEY_RSHARE_DIR= "env.r_share.dir"; //$NON-NLS-1$
	private static final String PREFKEY_RINCLUDE_DIR= "env.r_include.dir"; //$NON-NLS-1$
	
	private static final String PREFKEY_INDEX_DIR= "index.dir"; //$NON-NLS-1$
	
	
	private static final int LOCAL_WIN= 1;
	private static final int LOCAL_LINUX= 2;
	private static final int LOCAL_MAC= 3;
	private static final int LOCAL_PLATFORM;
	
	static {
		if (Platform.getOS().startsWith("win32")) { //$NON-NLS-1$
			LOCAL_PLATFORM= LOCAL_WIN;
		}
		else if (Platform.getOS().startsWith("mac")) { //$NON-NLS-1$
			LOCAL_PLATFORM= LOCAL_MAC;
		}
		else {
			LOCAL_PLATFORM= LOCAL_LINUX;
		}
	}
	
	
	public static IPath getStateLocation(final IREnv rEnv) {
		return RCorePlugin.getDefault().getStateLocation().append("renv").append(rEnv.getId()); //$NON-NLS-1$
	}
	
	public static IPath getStateLocation(final String rEnvId) {
		return RCorePlugin.getDefault().getStateLocation().append("renv").append(rEnvId); //$NON-NLS-1$
	}
	
	
	private static final ImList<IRLibraryLocation> NO_LIBS= ImCollections.emptyList();
	
	private static final String[] DEFAULT_LIBS_IDS= new String[] {
			IRLibraryGroup.R_OTHER,
			IRLibraryGroup.R_USER,
			IRLibraryGroup.R_SITE,
			IRLibraryGroup.R_DEFAULT,
	};
	
	private static IRLibraryLocation createDefaultLocation() {
		return new RLibraryLocation(IRLibraryLocation.R, IRLibraryGroup.DEFAULTLOCATION_R_DEFAULT, null);
	}
	
	private static final ImList<IRLibraryGroup> DEFAULT_LIBS_INIT;
	private static final ImList<IRLibraryGroup> DEFAULT_LIBS_DEFAULTS;
	static {
		{	final IRLibraryGroup[] groups= new IRLibraryGroup[DEFAULT_LIBS_IDS.length];
			for (int i= 0; i < DEFAULT_LIBS_IDS.length; i++) {
				final String id= DEFAULT_LIBS_IDS[i];
				final ImList<IRLibraryLocation> libs= NO_LIBS;
				groups[i]= new RLibraryGroup.Final(id, RLibraryGroup.getLabel(id), libs);
			}
			DEFAULT_LIBS_INIT= ImCollections.newList(groups);
		}
		{	final IRLibraryGroup[] groups= new IRLibraryGroup[DEFAULT_LIBS_IDS.length];
			for (int i= 0; i < DEFAULT_LIBS_IDS.length; i++) {
				final String id= DEFAULT_LIBS_IDS[i];
				final ImList<IRLibraryLocation> libs;
				if (id == IRLibraryGroup.R_DEFAULT) {
					libs= ImCollections.newList(createDefaultLocation());
				}
				else if (id == IRLibraryGroup.R_SITE) {
					libs= ImCollections.<IRLibraryLocation>newList(new RLibraryLocation(
							IRLibraryLocation.USER, IRLibraryGroup.DEFAULTLOCATION_R_SITE, null ));
				}
				else {
					libs= NO_LIBS;
				}
				groups[i]= new RLibraryGroup.Final(id, RLibraryGroup.getLabel(id), libs);
			}
			DEFAULT_LIBS_DEFAULTS= ImCollections.newList(groups);
		}
	}
	
	
	public static class Editable extends REnvConfiguration implements WorkingCopy {
		
		public Editable(final String type, final IREnv link) {
			super(type, link, (IPreferenceAccess) null, null);
			loadDefaults();
		}
		
		private Editable(final REnvConfiguration config) {
			super(config.type, config.getReference(), (IPreferenceAccess) null, null);
			load(config);
		}
		
		
		@Override
		protected ImList<IRLibraryGroup.WorkingCopy> copyLibs(final List<? extends IRLibraryGroup> source) {
			final IRLibraryGroup.WorkingCopy[] groups= new IRLibraryGroup.WorkingCopy[source.size()];
			for (int i= 0; i < groups.length; i++) {
				groups[i]= new RLibraryGroup.Editable((RLibraryGroup) source.get(i));
			}
			return ImCollections.newList(groups);
		}
		
		@Override
		public ImList<IRLibraryGroup.WorkingCopy> getRLibraryGroups() {
			return (ImList<IRLibraryGroup.WorkingCopy>) super.getRLibraryGroups();
		}
		
		@Override
		public IRLibraryGroup.WorkingCopy getRLibraryGroup(final String id) {
			return (IRLibraryGroup.WorkingCopy) super.getRLibraryGroup(id);
		}
		
	}
	
	
	private final IREnv rEnv;
	
	private String type;
	private boolean deleted;
	
	private String nodeQualifier;
	private String checkId;
	
	private StringPref prefType;
	private StringPref prefName;
	private String name;
	
	private StringPref prefRHomeDirectory;
	private String rHomeDirectory;
	private StringPref prefSubArch;
	private String subArch;
	private IntPref prefRBits;
	private int rBits;
	private StringPref prefROS;
	private String rOS;
	
	private String rVersion;
	
	private ImList<? extends IRLibraryGroup> rLibraries;
	private StringPref prefRDocDirectory;
	private String rDocDirectory;
	private IFileStore rDocDirectoryStore;
	private StringPref prefRShareDirectory;
	private String rShareDirectory;
	private IFileStore rShareDirectoryStore;
	private StringPref prefRIncludeDirectory;
	private String rIncludeDirectory;
	private IFileStore rIncludeDirectoryStore;
	
	private StringPref prefIndexDirectory;
	private String indexDirectory;
	private IFileStore indexDirectoryStore;
	
	private Properties sharedProperties;
	private final Object sharedPropertiesLock= new Object();
	
	
	REnvConfiguration(final IREnv link, final IPreferenceAccess prefs) {
		this(null, link, prefs, (String) null);
	}
	
	protected REnvConfiguration(final String type, final IREnv link,
			final IPreferenceAccess prefs, final String key) {
		assert (link != null);
		this.type= type;
		this.rEnv= link;
		
		this.nodeQualifier= IREnvManager.PREF_QUALIFIER + '/' +
				((key != null) ? key : link.getId());
		
		this.rBits= 64;
		if (prefs != null) {
			this.rLibraries= copyLibs(DEFAULT_LIBS_DEFAULTS);
			load(prefs);
		}
		else {
			this.rLibraries= DEFAULT_LIBS_INIT;
		}
	}
	
	REnvConfiguration(final String type, final IREnv link,
			final RSetup setup, final IPreferenceAccess prefs) {
		assert (link != null && setup != null);
		if (type != EPLUGIN_LOCAL_TYPE) {
			throw new IllegalArgumentException(type);
		}
		this.type= type;
		this.rEnv= link;
		
		this.nodeQualifier= IREnvManager.PREF_QUALIFIER + '/' + link.getId();
		
		setName(setup.getName());
		setROS(setup.getOS());
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			String arch= setup.getOSArch();
			if (arch == null || arch.isEmpty()) {
				arch= Platform.getOSArch();
			}
			setSubArch(arch);
		}
		this.rBits= (Platform.ARCH_X86.equals(setup.getOSArch())) ? 32 : 64;
		setRHome(setup.getRHome());
		loadDefaultInstallDir();
		this.rVersion= setup.getRVersion();
		
		final String[] ids= DEFAULT_LIBS_IDS;
		final List<IRLibraryGroup> groups= new ArrayList<>(ids.length);
		final List<IRLibraryLocation> libLocations= new ArrayList<>();
		
		final String userHome= getUserHome();
		final String eclipseHome= getInstallLocation();
		
		for (int i= 0; i < ids.length; i++) {
			libLocations.clear();
			final String id= ids[i];
			final String label= RLibraryGroup.getLabel(id);
			if (label != null) {
				final List<String> locations;
				if (id == IRLibraryGroup.R_DEFAULT) {
					libLocations.add(createDefaultLocation());
					locations= null;
				}
				else if (id == IRLibraryGroup.R_SITE) {
					locations= setup.getRLibsSite();
				}
				else if (id == IRLibraryGroup.R_USER) {
					locations= setup.getRLibsUser();
					try {
						final String path= "${workspace_loc}/.metadata/.r/" + link.getId() + "/user-library"; //$NON-NLS-1$ //$NON-NLS-2$
						final IFileStore store= checkPath(path, null, null);
						if (!store.fetchInfo().exists()) {
							store.mkdir(EFS.NONE, null);
						}
						libLocations.add(new RLibraryLocation(IRLibraryLocation.R, path, "Workspace Library"));
					}
					catch (final Exception e) {
						RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
								"An error occured when creating default R_USER 'Workspace Library'.", e));
					}
				}
				else if (id == IRLibraryGroup.R_OTHER) {
					locations= setup.getRLibs();
				}
				else {
					continue;
				}
				if (locations != null) {
					for (int j= 0; j < locations.size(); j++) {
						String path= locations.get(j);
						if (eclipseHome != null && path.startsWith(eclipseHome)) {
							path= "${eclipse_home}" + File.separatorChar + //$NON-NLS-1$
									path.substring(eclipseHome.length());
						}
						else if (userHome != null && path.startsWith(userHome)) {
							path= "${user_home}" + File.separatorChar + //$NON-NLS-1$
									path.substring(userHome.length());
						}
						libLocations.add(new RLibraryLocation(IRLibraryLocation.EPLUGIN, path, null));
					}
				}
				groups.add(new RLibraryGroup.Final(id, label,
						ImCollections.toList(libLocations)) );
			}
			else {
				// unknown group
			}
		}
		this.rLibraries= ImCollections.toList(groups);
		
		if (prefs != null) {
			load(prefs);
		}
		
		resolvePaths();
	}
	
	private String getUserHome() {
		IPath path= new Path(System.getProperty("user.home")); //$NON-NLS-1$
		path= path.addTrailingSeparator();
		return path.toOSString();
	}
	
	private String getInstallLocation() {
		final Location installLocation= Platform.getInstallLocation();
		if (installLocation == null) {
			return null;
		}
		final URL url= installLocation.getURL();
		if (url == null) {
			return null;
		}
		IPath path;
		try {
			path= URIUtil.toPath(url.toURI());
		}
		catch (final URISyntaxException e) {
			return null;
		}
		if (path == null) {
			return null;
		}
		path= path.addTrailingSeparator();
		return path.toOSString();
	}
	
	public REnvConfiguration(final IREnvConfiguration config) {
		this(config.getType(), config.getReference(), null, (String) null);
		load(config);
	}
	
	protected void checkPrefs(final IPreferenceAccess prefs) {
		final String id= this.rEnv.getId();
		if (id.equals(this.checkId)) {
			return;
		}
		this.checkId= id;
		
		this.prefType= new StringPref(this.nodeQualifier, PREFKEY_TYPE);
		
		if (this.type == null && prefs != null) {
			final String type= prefs.getPreferenceValue(this.prefType);
			if (type != null) {
				this.type= type.intern();
			}
		}
		
		this.prefName= new StringPref(this.nodeQualifier, PREFKEY_NAME);
		this.prefROS= new StringPref(this.nodeQualifier, PREFKEY_ROS);
		if (this.type == USER_LOCAL_TYPE) {
			this.prefRHomeDirectory= new StringPref(this.nodeQualifier, PREFKEY_RHOME_DIR);
			this.prefSubArch= new StringPref(this.nodeQualifier, PREFKEY_SUBARCH);
			this.prefRBits= new IntPref(this.nodeQualifier, PREFKEY_RBITS);
			this.prefRDocDirectory= new StringPref(this.nodeQualifier, PREFKEY_RDOC_DIR);
			this.prefRShareDirectory= new StringPref(this.nodeQualifier, PREFKEY_RSHARE_DIR);
			this.prefRIncludeDirectory= new StringPref(this.nodeQualifier, PREFKEY_RINCLUDE_DIR);
		}
		this.prefIndexDirectory= new StringPref(this.nodeQualifier, PREFKEY_INDEX_DIR);
	}
	
	void upgradePref() {
		this.checkId= null;
		this.nodeQualifier= IREnvManager.PREF_QUALIFIER + '/' + this.rEnv.getId();
	}
	
	protected void checkExistence(final IPreferenceAccess prefs) {
//		final IEclipsePreferences[] nodes= prefs.getPreferenceNodes(RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER);
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
		return this.rEnv;
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	
	@Override
	public boolean isDeleted() {
		return this.deleted;
	}
	
	void markDeleted() {
		this.deleted= true;
	}
	
	@Override
	public boolean isEditable() {
		return (this.type == USER_LOCAL_TYPE || this.type == USER_REMOTE_TYPE);
	}
	
	@Override
	public boolean isLocal() {
		return (this.type == USER_LOCAL_TYPE || this.type == EPLUGIN_LOCAL_TYPE);
	}
	
	@Override
	public boolean isRemote() {
		return (this.type == USER_REMOTE_TYPE);
	}
	
	
	@Override
	public String[] getNodeQualifiers() {
		checkPrefs(null);
		return new String[] { this.nodeQualifier };
	}
	
	@Override
	public void loadDefaults() {
		if (!(this instanceof WorkingCopy)) {
			throw new UnsupportedOperationException("No working copy");
		}
		setName("R"); //$NON-NLS-1$
		if (this.type == USER_LOCAL_TYPE) {
			setRHome(""); //$NON-NLS-1$
			this.rLibraries= copyLibs(DEFAULT_LIBS_DEFAULTS);
		}
		if (isLocal()) {
			loadDefaultInstallDir();
		}
		
		resolvePaths();
	}
	
	private void loadDefaultInstallDir() {
		setRDocDirectoryPath("${env_var:R_HOME}/doc"); //$NON-NLS-1$
		setRShareDirectoryPath("${env_var:R_HOME}/share"); //$NON-NLS-1$
		setRIncludeDirectoryPath("${env_var:R_HOME}/include"); //$NON-NLS-1$
	}
	
	public void load(final IREnvConfiguration from) {
		setName(from.getName());
		setROS(from.getROS());
		if (isLocal()) {
			setRHome(from.getRHome());
			setSubArch(from.getSubArch());
			this.rBits= (from instanceof REnvConfiguration) ? ((REnvConfiguration) from).rBits : 64;
			setRDocDirectoryPath(from.getRDocDirectoryPath());
			setRShareDirectoryPath(from.getRShareDirectoryPath());
			setRIncludeDirectoryPath(from.getRIncludeDirectoryPath());
			this.rLibraries= copyLibs(from.getRLibraryGroups());
		}
		setIndexDirectoryPath(from.getIndexDirectoryPath());
		
		resolvePaths();
	}
	
	@Override
	public void load(final IPreferenceAccess prefs) {
		checkPrefs(prefs);
		checkExistence(prefs);
		
		if (isEditable()) {
			setName(prefs.getPreferenceValue(this.prefName));
			setROS(prefs.getPreferenceValue(this.prefROS));
		}
		if (this.type == USER_LOCAL_TYPE) {
			setRHome(prefs.getPreferenceValue(this.prefRHomeDirectory));
			setSubArch(prefs.getPreferenceValue(this.prefSubArch));
			this.rBits= prefs.getPreferenceValue(this.prefRBits);
			setRDocDirectoryPath(prefs.getPreferenceValue(this.prefRDocDirectory));
			setRShareDirectoryPath(prefs.getPreferenceValue(this.prefRShareDirectory));
			setRIncludeDirectoryPath(prefs.getPreferenceValue(this.prefRIncludeDirectory));
		}
		if (isLocal()) {
			final String[] ids= DEFAULT_LIBS_IDS;
			final List<IRLibraryGroup> groups= new ArrayList<>(ids.length);
			for (int i= 0; i < ids.length; i++) {
				final String id= ids[i];
				final List<IRLibraryLocation> libs= new ArrayList<>();
				final IRLibraryGroup group= getRLibraryGroup(id);
				for (final IRLibraryLocation location : group.getLibraries()) {
					if (location.getSource() != IRLibraryLocation.USER) {
						libs.add(location);
					}
				}
				if (id != IRLibraryGroup.R_DEFAULT) {
					final String[] paths= prefs.getPreferenceValue(
							new StringArrayPref(this.nodeQualifier, PREFKEY_RLIBS_PREFIX+id, Preference.IS2_SEPARATOR_CHAR) );
					for (final String path : paths) {
						final RLibraryLocation location= new RLibraryLocation(IRLibraryLocation.USER, path, null);
						if (!libs.contains(location)) {
							libs.add(location);
						}
					}
				}
				groups.add(new RLibraryGroup.Final(id, RLibraryGroup.getLabel(id),
						ImCollections.toList(libs) ));
			}
			this.rLibraries= ImCollections.toList(groups);
		}
		
		setIndexDirectoryPath(prefs.getPreferenceValue(this.prefIndexDirectory));
		
		resolvePaths();
	}
	
	protected ImList<? extends IRLibraryGroup> copyLibs(final List<? extends IRLibraryGroup> source) {
		final IRLibraryGroup[] groups= new RLibraryGroup[source.size()];
		for (int i= 0; i < groups.length; i++) {
			groups[i]= new RLibraryGroup.Final((RLibraryGroup) source.get(i));
		}
		return ImCollections.newList(groups);
	}
	
	@Override
	public Map<Preference<?>, Object> deliverToPreferencesMap(final Map<Preference<?>, Object> map) {
		checkPrefs(null);
		
		map.put(this.prefType, getType());
		
		if (isEditable()) {
			map.put(this.prefName, getName());
			map.put(this.prefROS, getROS());
		}
		
		if (this.type == USER_LOCAL_TYPE) {
			map.put(this.prefRHomeDirectory, getRHome());
			map.put(this.prefSubArch, this.subArch);
			map.put(this.prefRBits, this.rBits);
			map.put(this.prefRDocDirectory, getRDocDirectoryPath());
			map.put(this.prefRShareDirectory, getRShareDirectoryPath());
			map.put(this.prefRIncludeDirectory, getRIncludeDirectoryPath());
		}
		if (isLocal()) {
			final List<? extends IRLibraryGroup> groups= this.rLibraries;
			for (final IRLibraryGroup group : groups) {
				final List<? extends IRLibraryLocation> libraries= group.getLibraries();
				final List<String> locations= new ArrayList<>(libraries.size());
				for (final IRLibraryLocation location : libraries) {
					if (location.getSource() == IRLibraryLocation.USER) {
						locations.add(location.getDirectoryPath());
					}
				}
				map.put(new StringArrayPref(this.nodeQualifier, PREFKEY_RLIBS_PREFIX+group.getId(),
						Preference.IS2_SEPARATOR_CHAR ), locations.toArray(new String[locations.size()]));
			}
		}
		
		map.put(this.prefIndexDirectory, getIndexDirectoryPath());
		
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
		return this.name;
	}
	
	public void setName(final String label) {
		final String oldValue= this.name;
		this.name= label;
		firePropertyChange(PROP_NAME, oldValue, label);
	}
	
	@Override
	public String getRVersion() {
		return this.rVersion;
	}
	
	public boolean isValidRHomeLocation(final IFileStore rHome) {
		final IFileStore binDir= rHome.getChild("bin"); //$NON-NLS-1$
		IFileStore exeFile= null;
		switch (LOCAL_PLATFORM) {
		case LOCAL_WIN:
			exeFile= binDir.getChild("R.exe"); //$NON-NLS-1$
			break;
		default:
			exeFile= binDir.getChild("R"); //$NON-NLS-1$
			break;
		}
		final IFileInfo info= exeFile.fetchInfo();
		return (!info.isDirectory() && info.exists());
	}
	
	/** {@link IREnvConfiguration.WorkingCopy#searchAvailableSubArchs(IFileStore)} */
	public List<String> searchAvailableSubArchs(final IFileStore rHome) {
		if (rHome != null && rHome.fetchInfo().exists()) {
			try {
				final IFileStore rHomeBinSub;
				final String name;
				switch (LOCAL_PLATFORM) {
				case LOCAL_WIN:
					rHomeBinSub= rHome.getChild("bin"); //$NON-NLS-1$
					name= "R.exe"; //$NON-NLS-1$
					break;
				default:
					rHomeBinSub= rHome.getChild("bin").getChild("exec"); //$NON-NLS-1$ //$NON-NLS-2$
					name= "R";  //$NON-NLS-1$
					break;
				}
				if (rHomeBinSub.fetchInfo().exists()) {
					final IFileStore[] subDirs= rHomeBinSub.childStores(EFS.NONE, null);
					final List<String> archs= new ArrayList<>();
					for (final IFileStore subDir : subDirs) {
						if (subDir.getChild(name).fetchInfo().exists()) {
							final String arch= checkArchForStatet(subDir.getName());
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
			arch= arch.substring(1);
			if (arch.isEmpty()) {
				return null;
			}
		}
		if ((arch.equals("exec"))) { //$NON-NLS-1$
			return null;
		}
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			if (arch.equals("x64")) { //$NON-NLS-1$
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
			arch= '/' + arch;
		}
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			if (arch.equals("/x86_64")) { //$NON-NLS-1$
				return "/x64"; //$NON-NLS-1$
			}
			if (arch.equals("/x86")) { //$NON-NLS-1$
				return "/i386"; //$NON-NLS-1$
			}
		}
		return arch;
	}
	
	@Override
	public IStatus validate() {
		CoreException error= null;
		if (isLocal()) {
			IFileStore rloc= null;
			try {
				rloc= FileUtil.expandToLocalFileStore(getRHome(), null, null);
			}
			catch (final CoreException e) {
				error= e;
			}
			if (rloc == null || !isValidRHomeLocation(rloc)) {
				return new Status(IStatus.ERROR, RCore.PLUGIN_ID, Messages.REnvConfiguration_Validation_error_InvalidRHome_message, error);
			}
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public String getRHome() {
		return this.rHomeDirectory;
	}
	
	public void setRHome(final String label) {
		final String oldValue= this.rHomeDirectory;
		this.rHomeDirectory= label;
		firePropertyChange(PROP_RHOME, oldValue, label);
	}
	
	@Override
	public String getSubArch() {
		return this.subArch;
	}
	
	public void setSubArch(String arch) {
		arch= checkArchForStatet(arch);
		final String oldValue= this.subArch;
		this.subArch= arch;
		firePropertyChange(PROP_SUBARCH, oldValue, arch);
	}
	
	@Override
	public String getROS() {
		return this.rOS;
	}
	
	public void setROS(final String type) {
		final String oldValue= this.rOS;
		this.rOS= type;
		firePropertyChange(PROP_RHOME, oldValue, type);
	}
	
	
	@Override
	public String getRDocDirectoryPath() {
		return this.rDocDirectory;
	}
	
	public void setRDocDirectoryPath(final String directory) {
		final String oldValue= this.rDocDirectory;
		this.rDocDirectory= directory;
		firePropertyChange(PROP_RDOC_DIRECTORY, oldValue, directory);
	}
	
	@Override
	public String getRShareDirectoryPath() {
		return this.rShareDirectory;
	}
	
	public void setRShareDirectoryPath(final String directory) {
		final String oldValue= this.rShareDirectory;
		this.rShareDirectory= directory;
		firePropertyChange(PROP_RSHARE_DIRECTORY, oldValue, directory);
	}
	
	@Override
	public String getRIncludeDirectoryPath() {
		return this.rIncludeDirectory;
	}
	
	public void setRIncludeDirectoryPath(final String directory) {
		final String oldValue= this.rIncludeDirectory;
		this.rIncludeDirectory= directory;
		firePropertyChange(PROP_RINCLUDE_DIRECTORY, oldValue, directory);
	}
	
	@Override
	public ImList<? extends IRLibraryGroup> getRLibraryGroups() {
		return this.rLibraries;
	}
	
	@Override
	public IRLibraryGroup getRLibraryGroup(final String id) {
		for (final IRLibraryGroup group : this.rLibraries) {
			if (group.getId().equals(id)) {
				return group;
			}
		}
		return null;
	}
	
	
	@Override
	public String getIndexDirectoryPath() {
		return this.indexDirectory;
	}
	
	@Override
	public IFileStore getIndexDirectoryStore() {
		return this.indexDirectoryStore;
	}
	
	public void setIndexDirectoryPath(final String directory) {
		final String oldValue= this.indexDirectory;
		this.indexDirectory= directory;
		firePropertyChange(PROP_INDEX_DIRECTORY, oldValue, directory);
	}
	
	
	@Override
	public List<String> getExecCommand(String arg1, final Set<Exec> execTypes) throws CoreException {
		final String test= (arg1 != null) ? arg1.trim().toUpperCase() : ""; //$NON-NLS-1$
		Exec type= Exec.COMMON;
		if (test.equals("CMD")) { //$NON-NLS-1$
			if (execTypes.contains(Exec.CMD)) {
				type= Exec.CMD;
				arg1= null;
			}
		}
		else {
			if (execTypes.contains(Exec.TERM)) {
				type= Exec.TERM;
			}
		}
		final List<String> commandLine= getExecCommand(type);
		if (arg1 != null) {
			commandLine.add(arg1);
		}
		return commandLine;
	}
	
	@Override
	public List<String> getExecCommand(final Exec execType) throws CoreException {
		final List<IFileStore> binDirs= getBinDirs();
		IFileStore exe= null;
		final List<String> commandLine= new ArrayList<>(2);
		switch (execType) {
		case TERM:
			if (LOCAL_PLATFORM == LOCAL_WIN) {
				exe= getExisting(binDirs, "Rterm.exe"); //$NON-NLS-1$
			}
			break;
		case CMD:
			if (LOCAL_PLATFORM == LOCAL_WIN) {
				exe= getExisting(binDirs, "Rcmd.exe"); //$NON-NLS-1$
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
				exe= getExisting(binDirs, "R.exe"); //$NON-NLS-1$
			}
			else {
				exe= getExisting(binDirs, "R"); //$NON-NLS-1$
			}
		}
		
		commandLine.add(0, URIUtil.toPath(exe.toURI()).toOSString());
		return commandLine;
	}
	
	private List<IFileStore> getBinDirs() throws CoreException {
		final IFileStore rHome= FileUtil.expandToLocalFileStore(getRHome(), null, null);
		final IFileStore rHomeBin= rHome.getChild("bin"); //$NON-NLS-1$
		final IFileStore rHomeBinSub;
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			rHomeBinSub= rHomeBin;
		}
		else { // use wrapper shell scripts
			rHomeBinSub= null; // rHomeBin.getChild("exec"); 
		}
		final List<IFileStore> dirs= new ArrayList<>(4);
		String arch= this.subArch;
		if (arch == null) {
			arch= Platform.getOSArch();
		}
		if (arch != null && rHomeBinSub != null) {
			final IFileStore rHomeBinArch;
			if (arch.equals(Platform.ARCH_X86_64)) {
				rHomeBinArch= getExistingChild(rHomeBinSub, "x86_64", "x64"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (arch.equals(Platform.ARCH_X86)) {
				rHomeBinArch= getExistingChild(rHomeBinSub, "x86", "i386", "i586", "i686"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else {
				rHomeBinArch= getExistingChild(rHomeBinSub, arch);
			}
			if (rHomeBinArch != null) {
				dirs.add(rHomeBinArch);
			}
		}
		dirs.add(rHomeBin);
		return dirs;
	}
	
	private List<IFileStore> getLibDirs() throws CoreException {
		final IFileStore rHome= FileUtil.expandToLocalFileStore(getRHome(), null, null);
		final IFileStore rHomeLib;
		if (LOCAL_PLATFORM == LOCAL_WIN) {
			rHomeLib= rHome.getChild("bin"); //$NON-NLS-1$
		}
		else {
			rHomeLib= rHome.getChild("lib"); //$NON-NLS-1$
		}
		final List<IFileStore> dirs= new ArrayList<>(4);
		String arch= this.subArch;
		if (arch == null) {
			arch= Platform.getOSArch();
		}
		if (arch != null && LOCAL_PLATFORM != LOCAL_MAC) {
			final IFileStore rHomeBinArch;
			if (arch.equals(Platform.ARCH_X86_64)) {
				rHomeBinArch= getExistingChild(rHomeLib, "x86_64", "x64"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (arch.equals(Platform.ARCH_X86)) {
				rHomeBinArch= getExistingChild(rHomeLib, "x86", "i386", "i586", "i686"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else {
				rHomeBinArch= getExistingChild(rHomeLib, arch);
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
			final IFileStore child= base.getChild(name);
			if (child.fetchInfo().exists()) {
				return child;
			}
		}
		return null;
	}
	
	private IFileStore getExisting(final List<IFileStore> dirs, final String name) {
		IFileStore file= null;
		for (final IFileStore dir : dirs) {
			file= dir.getChild(name);
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
		final Map<String, String> envp= new HashMap<>();
		final IFileStore rHomeStore= FileUtil.expandToLocalFileStore(getRHome(), null, null);
		final String rHome= URIUtil.toPath(rHomeStore.toURI()).toOSString();
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
			final String arch= this.subArch;
			final List<String> availableArchs= searchAvailableSubArchs(rHomeStore);
			if (availableArchs != null && availableArchs.contains(arch)) {
				if (arch != null) {
					envp.put("R_ARCH", checkArchForR(arch)); //$NON-NLS-1$
				}
			}
			envp.put("R_LIBS_SITE", getLibPath(getRLibraryGroup(IRLibraryGroup.R_SITE))); //$NON-NLS-1$
			envp.put("R_LIBS_USER", getLibPath(getRLibraryGroup(IRLibraryGroup.R_USER))); //$NON-NLS-1$
			envp.put("R_LIBS", getLibPath(getRLibraryGroup(IRLibraryGroup.R_OTHER))); //$NON-NLS-1$
			if (this.rDocDirectoryStore != null) {
				envp.put("R_DOC_DIR", URIUtil.toPath(this.rDocDirectoryStore.toURI()).toOSString()); //$NON-NLS-1$
			}
			if (this.rShareDirectoryStore != null) {
				envp.put("R_SHARE_DIR", URIUtil.toPath(this.rShareDirectoryStore.toURI()).toOSString()); //$NON-NLS-1$
			}
			if (this.rIncludeDirectoryStore != null) {
				envp.put("R_INCLUDE_DIR", URIUtil.toPath(this.rIncludeDirectoryStore.toURI()).toOSString()); //$NON-NLS-1$
			}
		}
		envp.put("LC_NUMERIC", "C"); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (PreferencesUtil.getInstancePrefs().getPreferenceValue(RCorePreferenceNodes.PREF_RENV_NETWORK_USE_ECLIPSE)) {
			configureNetwork(envp);
		}
		
		return envp;
	}
	
	protected void configureNetwork(final Map<String, String> envp) {
		final IProxyService proxyService= RCorePlugin.getDefault().getProxyService();
		if (proxyService != null && proxyService.isProxiesEnabled()) {
			final StringBuilder sb= new StringBuilder();
			{	final String[] nonProxiedHosts= proxyService.getNonProxiedHosts();
				if (nonProxiedHosts.length > 0) {
					sb.setLength(0);
					sb.append(nonProxiedHosts[0]);
					for (int i= 1; i < nonProxiedHosts.length; i++) {
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
				IProxyData data= proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE);
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
					envp.put("http_proxy", sb.toString()); //$NON-NLS-1$
				}
				
				data= proxyService.getProxyData("FTP"); //$NON-NLS-1$
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
			final Properties auto= getSharedProperties();
			if (auto != null) {
				final String hostname= auto.getProperty("renv.hostname"); //$NON-NLS-1$
				if (hostname != null) {
					final IResourceMappingManager rmManager= ResourceMappingUtils.getManager();
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
		if (group == null || (libs= group.getLibraries()).isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		final StringBuilder sb= new StringBuilder();
		for (final IRLibraryLocation lib : libs) {
			final IFileStore store= lib.getDirectoryStore();
			if (store != null) {
				sb.append(URIUtil.toPath(store.toURI()).toOSString());
			}
			sb.append(File.pathSeparatorChar);
		}
		return sb.substring(0, sb.length()-1);
	}
	
	public Properties getSharedProperties() {
		synchronized (this.sharedPropertiesLock) {
			if (this.sharedProperties == null) {
				loadSharedProperties();
			}
			return this.sharedProperties;
		}
	}
	
	private void loadSharedProperties() {
		synchronized (this.sharedPropertiesLock) {
			InputStream in= null;
			try {
				if (this.indexDirectoryStore != null) {
					final IFileStore store= this.indexDirectoryStore.getChild("renv.properties"); //$NON-NLS-1$
					if (store.fetchInfo().exists()) {
						final Properties prop= new Properties();
						in= store.openInputStream(EFS.NONE, null);
						prop.load(in);
						prop.setProperty("stamp", Long.toString(store.fetchInfo().getLastModified())); //$NON-NLS-1$
						this.sharedProperties= prop;
						in.close();
						in= null;
					}
				}
			}
			catch (final Exception e) {
				RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						"An error occurrend when loading shared R environment properties.", e));
				this.sharedProperties= new Properties();
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
		synchronized (this.sharedPropertiesLock) {
			this.sharedProperties= null;
			final Properties prop= new Properties();
			prop.putAll(properties);
			saveSharedProperties(prop);
			final IREnvConfiguration config= getReference().getConfig();
			if (config != null && config instanceof REnvConfiguration) {
				((REnvConfiguration) config).loadSharedProperties();
			}
		}
	}
	
	private void saveSharedProperties(final Properties prop) {
		OutputStream out= null;
		try {
			if (this.indexDirectoryStore != null) {
				final IFileStore store= this.indexDirectoryStore.getChild("renv.properties"); //$NON-NLS-1$
				out= store.openOutputStream(EFS.NONE, null);
				prop.store(out, null);
				out.close();
				out= null;
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
		String rHome= null;
		try {
			final String s= getRHome();
			if (s != null) {
				rHome= URIUtil.toPath(FileUtil.expandToLocalFileStore(s, null, null).toURI()).toOSString();
			}
		}
		catch (final CoreException e) {}
		final String userHome= System.getProperty("user.home"); //$NON-NLS-1$
		
		this.indexDirectoryStore= checkPath(this.indexDirectory, rHome, userHome);
		if (this.indexDirectoryStore == null) {
			try {
				final IPath directory= getStateLocation(this.rEnv).append("shared"); //$NON-NLS-1$
				this.indexDirectoryStore= EFS.getStore(URIUtil.toURI(directory));
			}
			catch (final CoreException e) {
			}
		}
		if (isLocal()) {
			for (final IRLibraryGroup group : this.rLibraries) {
				for (final IRLibraryLocation lib : group.getLibraries()) {
					if (lib instanceof RLibraryLocation) {
						final RLibraryLocation l= (RLibraryLocation) lib;
						l.fStore= checkPath(l.fPath, rHome, userHome);
					}
				}
			}
			this.rDocDirectoryStore= checkPath(this.rDocDirectory, rHome, userHome);
			this.rShareDirectoryStore= checkPath(this.rShareDirectory, rHome, userHome);
			this.rIncludeDirectoryStore= checkPath(this.rIncludeDirectory, rHome, userHome);
		}
	}
	
	private IFileStore checkPath(String path, final String rHome, final String userHome) {
		try {
			if (path != null && path.length() > 0) {
				if (rHome != null) {
					path= path.replace("${r_home}", rHome); //$NON-NLS-1$
					path= path.replace("${env_var:R_HOME}", rHome); //$NON-NLS-1$
				}
				else if (path.contains("${r_home}") //$NON-NLS-1$
						|| path.contains("${env_var:R_HOME}")) { //$NON-NLS-1$
					return null;
				}
				if (userHome != null) {
					if (path.startsWith("~/")) { //$NON-NLS-1$
						path= userHome + path.substring(1);
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
					NLS.bind("Could not resolve configured path ''{0}'' of " +
							"R environment configuration ''{1}''.", path, this.name ), e));
		}
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return this.rEnv.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IREnvConfiguration)) {
			return false;
		}
		final IREnvConfiguration other= (IREnvConfiguration) obj;
		return (this.rEnv.equals(other.getReference())
				&& (this.type == other.getType())
				&& ((this.name != null) ? this.name.equals(other.getName()) : null == other.getName())
				&& ((this.subArch != null) ? this.subArch.equals(other.getSubArch()) : null == other.getSubArch())
				&& ((this.rOS != null) ? this.rOS.equals(other.getROS()) : null == other.getROS())
				&& (!isLocal() || (
						   ((this.rHomeDirectory != null) ? this.rHomeDirectory.equals(other.getRHome()) : null == other.getRHome())
						&& ((this.rDocDirectory != null) ? this.rDocDirectory.equals(other.getRDocDirectoryPath()) : null == other.getRDocDirectoryPath())
						&& ((this.rShareDirectory != null) ? this.rShareDirectory.equals(other.getRShareDirectoryPath()) : null == other.getRShareDirectoryPath())
						&& ((this.rIncludeDirectory != null) ? this.rIncludeDirectory.equals(other.getRIncludeDirectoryPath()) : null == other.getRIncludeDirectoryPath())
						&& ((this.rLibraries != null) ? this.rLibraries.equals(other.getRLibraryGroups()) : null == this.rLibraries) )
						)
				&& ((this.indexDirectory != null) ? this.indexDirectory.equals(other.getIndexDirectoryPath()) : null == other.getIndexDirectoryPath())
				);
	}
	
	
	@Override
	public String toString() {
		return this.rEnv.getId() + " (" + getName() + ", " + getRHome() + ")"; //$NON-NLS-1$
	}
	
}
