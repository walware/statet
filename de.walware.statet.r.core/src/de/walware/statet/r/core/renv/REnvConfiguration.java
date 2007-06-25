/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.renv;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.eclipsecommons.FileUtil;
import de.walware.eclipsecommons.preferences.AbstractPreferencesModelObject;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.StringPref;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCorePreferenceNodes;
import de.walware.statet.r.internal.core.Messages;


/**
 *
 */
public class REnvConfiguration extends AbstractPreferencesModelObject {
	
	
	public static final String PROP_NAME = "name"; //$NON-NLS-1$
	private static final String PREFKEY_NAME = "name"; //$NON-NLS-1$
	
	public static final String PROP_ID = "id"; //$NON-NLS-1$
	private static final String PREFKEY_ID = "id"; //$NON-NLS-1$

	public static final String PROP_RHOME = "RHome"; //$NON-NLS-1$
	private static final String PREFKEY_RHOME = "env.r_home"; //$NON-NLS-1$

//	public static final String PREFKEY_BIN_DIRECTORY = "bin.dir";
//	public static final String PROP_BIN_DIRECTORY = "binDirectory";

	
	public static boolean isValidRHomeLocation(IFileStore loc) {
		IFileStore binDir = loc.getChild("bin"); //$NON-NLS-1$
		IFileStore exeFile = null; 
		if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
			exeFile = binDir.getChild("R.exe"); //$NON-NLS-1$
		}
		else {
			exeFile = binDir.getChild("R"); //$NON-NLS-1$
		}
		IFileInfo info = exeFile.fetchInfo();
		return (!info.isDirectory() && info.exists());
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
//	private StringPref fPrefBinDirectory;
//	private String fBinDirectory;
	
	
	/**
	 * Creates new empty configuration
	 */
	protected REnvConfiguration() {
		fIsDisposed = false;
		setId(System.getProperty("user.name")+System.currentTimeMillis()); //$NON-NLS-1$
	}
	
	protected REnvConfiguration(String id) {
		fIsDisposed = false;
		setId(id);
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
//		fPrefBinDirectory = new StringPref(fNodeQualifier, PREFKEY_BIN_DIRECTORY);
	}
	
	protected void checkExistence(IPreferenceAccess prefs) {
		IEclipsePreferences[] nodes = prefs.getPreferenceNodes(RCorePreferenceNodes.CAT_R_ENVIRONMENTS_QUALIFIER);
		if (nodes.length > 0)  {
			try {
				if (!nodes[0].nodeExists(fName)) {
					throw new IllegalArgumentException("A REnv configuration with this name does not exists."); //$NON-NLS-1$
				}
			} catch (BackingStoreException e) {
				throw new IllegalArgumentException("REnv Configuration could not be accessed."); //$NON-NLS-1$
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
		};
	}

	@Override
	public void loadDefaults() {
		setName("R"); //$NON-NLS-1$
		setRHome(""); //$NON-NLS-1$
//		setBinDirectory("bin");
	}
	
	public void load(REnvConfiguration from) {
		load(from, false);
	}
	protected void load(REnvConfiguration from, boolean copyId) {
		if (copyId) {
			setId(from.getId());
		}
		setName(from.getName());
		setRHome(from.getRHome());
	}

	@Override
	public void load(IPreferenceAccess prefs) {
		load(prefs, false);
	}
	protected void load(IPreferenceAccess prefs, boolean copyId) {
		checkPrefs();
		checkExistence(prefs);
		if (copyId) {
			setId(prefs.getPreferenceValue(fPrefId));
		}
		setName(prefs.getPreferenceValue(fPrefName));
		setRHome(prefs.getPreferenceValue(fPrefRHomeDirectory));
//		setBinDirectory(prefs.getPreferenceValue(fPrefBinDirectory));
	}

	@Override
	public Map<Preference, Object> deliverToPreferencesMap(
			Map<Preference, Object> map) {
		checkPrefs();
		map.put(fPrefId, getId());
		map.put(fPrefName, getName());
		map.put(fPrefRHomeDirectory, getRHome());
//		map.put(fPrefBinDirectory, getBinDirectory());
		return map;
	}

	
/*-- Properties --------------------------------------------------------------*/
	
	protected void setName(String label) {
		String oldValue = fName;
		fName = label;
		firePropertyChange(PROP_NAME, oldValue, fName);
	}
	public String getName() {
		return fName;
	}
	
	protected void setId(String id) {
		String oldValue = fId;
		fId = id;
		firePropertyChange(PROP_RHOME, oldValue, fId);
	}
	public String getId() {
		return fId;
	}
	
	protected void setDispose(boolean isDisposed) {
		boolean oldValue = fIsDisposed;
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
			rloc = FileUtil.expandToLocalFileStore(getRHome(), null);
		} catch (CoreException e) {
			error = e;
		}
		if (rloc == null || !isValidRHomeLocation(rloc)) {
			return new Status(Status.ERROR, RCore.PLUGIN_ID, Messages.REnvConfiguration_Validation_error_InvalidRHome_message, error);
		}
		return Status.OK_STATUS;
	}
	
	protected void setRHome(String label) {
		String oldValue = fRHomeDirectory;
		fRHomeDirectory = label;
		firePropertyChange(PROP_RHOME, oldValue, fRHomeDirectory);
	}
	public String getRHome() {
		return fRHomeDirectory;
	}

//	public void setBinDirectory(String label) {
//		
//		String oldValue = fBinDirectory;
//		fBinDirectory = label;
//		firePropertyChange(PROP_BIN_DIRECTORY, oldValue, fBinDirectory);
//	}
//	public String getBinDirectory() {
//		
//		return fBinDirectory;
//	}
	
	public Map<String, String> getEnvironmentsVariables() throws CoreException {
		Map<String, String> envp = new HashMap<String, String>();
		envp.put("R_HOME", FileUtil.expandToLocalPath(getRHome(), null).toOSString()); //$NON-NLS-1$
		return envp;
	}
}
