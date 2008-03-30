/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching;

import java.net.URI;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.preferences.PreferencesUtil;
import de.walware.eclipsecommons.preferences.Preference.StringPref;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants;
import de.walware.statet.r.internal.debug.ui.launcher.DefaultCodeLaunchHandler;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.nico.ui.RControllerCodeLaunchConnector;
import de.walware.statet.r.ui.RUI;


public class RCodeLaunchRegistry {
	
	
	public static final StringPref PREF_R_CONNECTOR = new StringPref(RDebugPreferenceConstants.CAT_RCONNECTOR_QUALIFIER, "rconnector.id"); //$NON-NLS-1$
	
	private static final IStatus STATUS_PROMPTER = new Status(IStatus.INFO, IDebugUIConstants.PLUGIN_ID, 200, "", null); //$NON-NLS-1$
	private static final IStatus STATUS_SAVE = new Status(IStatus.INFO, DebugPlugin.getUniqueIdentifier(), 222, "", null); //$NON-NLS-1$
	
	private static final Pattern FILENAME_PATTERN = Pattern.compile("\\Q${resource_loc}\\E"); //$NON-NLS-1$
	
	
	public static void initializeDefaultValues(final IScopeContext context) {
		PreferencesUtil.setPrefValue(context, PREF_R_CONNECTOR, RControllerCodeLaunchConnector.ID);
	}
	
	
	public static boolean isConfigured() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUI.PLUGIN_ID, CONNECTOR_EXTENSION_POINT);
		return (elements != null && elements.length > 0);
	}
	
	/**
	 * Runs a file related command in R.
	 * <p>
	 * The pattern ${file} in command string is replaced by the path of
	 * the specified file.</p>
	 * 
	 * @param command the command, (at moment) should be single line.
	 * @param file the file.
	 * @throws CoreException if running failed.
	 */
	public static void runFileUsingCommand(final String command, final IFile file, final boolean gotoConsole) throws CoreException {
		// save before launch
		final IProject project = file.getProject();
		if (project != null) {
			final IProject[] referencedProjects = project.getReferencedProjects();
			final IProject[] allProjects = new IProject[referencedProjects.length+1];
			allProjects[0] = project;
			System.arraycopy(referencedProjects, 0, allProjects, 1, referencedProjects.length);
			if (!saveBeforeLaunch(allProjects)) {
				return;
			}
		}
		
		runFileUsingCommand(command, file.getLocation(), gotoConsole);
	}
	
	/**
	 * Runs a file related command in R.
	 * Use this method only, if you don't have an IFile object for your file
	 * (e.g. external file).
	 * <p>
	 * The pattern ${file} in command string is replaced by the path of
	 * the specified file.</p>
	 * 
	 * @param command the command, (at moment) should be single line.
	 * @param file the file.
	 * @throws CoreException if running failed.
	 */
	public static void runFileUsingCommand(final String command, final IPath filePath, final boolean gotoConsole) throws CoreException {
		final IRCodeLaunchConnector connector = getDefault().getConnector();
		
		final String fileString = RUtil.escapeCompletly(filePath.makeAbsolute().toOSString());
		final String cmd = FILENAME_PATTERN.matcher(command).replaceAll(Matcher.quoteReplacement(fileString));
		connector.submit(new String[] { cmd }, gotoConsole);
	}
	
	/**
	 * Runs a file related command in R.
	 * Use this method only, if you don't have an IFile or IPath object for your file
	 * (e.g. file on webserver).
	 * <p>
	 * The pattern ${file} in command string is replaced by the path of
	 * the specified file.</p>
	 * 
	 * @param command the command, (at moment) should be single line.
	 * @param file the file.
	 * @throws CoreException if running failed.
	 */
	public static void runFileUsingCommand(final String command, final URI filePath, final boolean gotoConsole) throws CoreException {
		final IRCodeLaunchConnector connector = getDefault().getConnector();
		
		String fileString = null;
		try {
			if (EFS.getLocalFileSystem().equals(EFS.getFileSystem(filePath.getScheme()))) {
				fileString = EFS.getLocalFileSystem().getStore(filePath).toString();
			}
		} catch (final CoreException e) {
		}
		if (fileString == null) {
			fileString = filePath.toString();
		}
		
		fileString = RUtil.escapeCompletly(fileString);
		final String cmd = FILENAME_PATTERN.matcher(command).replaceAll(Matcher.quoteReplacement(fileString));
		connector.submit(new String[] { cmd }, gotoConsole);
	}
	
	private static boolean saveBeforeLaunch(final IProject[] projects) throws CoreException {
		IStatusHandler prompter = null;
		prompter = DebugPlugin.getDefault().getStatusHandler(STATUS_PROMPTER);
		if (prompter != null) {
			return ((Boolean) prompter.handleStatus(STATUS_SAVE,
					new Object[] { null, projects } )).booleanValue();
		}
		return true;
	}
	
	public static boolean runRCodeDirect(final String[] code, final boolean gotoConsole) throws CoreException {
		final IRCodeLaunchConnector connector = getDefault().getConnector();
		
		return connector.submit(code, gotoConsole);
	}
	
	public static void gotoRConsole() throws CoreException {
		final IRCodeLaunchConnector connector = getDefault().getConnector();
		
		connector.gotoConsole();
	}
	
	public static String getPreferredFileCommand(final String contentType) {
		return getDefault().getFileCommand(contentType);
	}
	
	public static ICodeLaunchContentHandler getCodeLaunchContentHandler(final String contentType) {
		return getDefault().getContentHandler(contentType);
	}
	
	
	static final String CONNECTOR_EXTENSION_POINT = "rCodeLaunchConnector"; //$NON-NLS-1$
	static final String CONTENTHANDLER_EXTENSION_POINT = "rCodeLaunchContentHandler"; //$NON-NLS-1$
	
	private static final String CONNECTOR_ELEMENT = "connector"; //$NON-NLS-1$
	private static final String CONTENTHANDLER_ELEMENT = "contentHandler"; //$NON-NLS-1$
	private static final String CONTENT_FILECOMMAND_ELEMENT = "fileCommand"; //$NON-NLS-1$
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	private static final String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$
	private static final String ATT_CONTENT_TYPE = "contentTypeId"; //$NON-NLS-1$
	private static final String ATT_HANDLER = "handler"; //$NON-NLS-1$
	private static final String ATT_DEFAULTCOMMAND = "defaultCommand"; //$NON-NLS-1$
	
	private static RCodeLaunchRegistry fgRegistry;
	
	private static synchronized RCodeLaunchRegistry getDefault() {
		if (fgRegistry == null)
			new RCodeLaunchRegistry();
		return fgRegistry;
	}
	
	
/* Config *********************************************************************/
	
	public static class ConnectorConfig {
		
		public final String fId;
		public final String fName;
		public final String fDescription;
		
		private ConnectorConfig(final IConfigurationElement element) {
			fId = element.getAttribute(ATT_ID);;
			fName = element.getAttribute(ATT_NAME);
			fDescription = element.getAttribute(ATT_DESCRIPTION);
		}
	}
	
	public static class ContentHandler {
		
		private final String contentTypeId;
		IConfigurationElement configurationElement;
		ICodeLaunchContentHandler handler;
		private String defaultFileCommand;
		private String currentFileCommand;
		
		public ContentHandler() {
			this.contentTypeId = IRSourceUnit.R_CONTENT;
			this.handler = new DefaultCodeLaunchHandler();
			this.defaultFileCommand = currentFileCommand = "source(\"${resource_loc}\")"; //$NON-NLS-1$
		}
		
		public ContentHandler(final IConfigurationElement config) {
			this.contentTypeId = config.getAttribute(ATT_CONTENT_TYPE);
			this.configurationElement = config;
			
			final IConfigurationElement[] children = config.getChildren(CONTENT_FILECOMMAND_ELEMENT);
			if (children.length > 0) {
				this.defaultFileCommand = currentFileCommand = children[0].getAttribute(ATT_DEFAULTCOMMAND);
			}
		}
		
		public String getContentTypeId() {
			return contentTypeId;
		}
		
		public String getDefaultFileCommand() {
			return defaultFileCommand;
		}
		
		public String getCurrentFileCommand() {
			return currentFileCommand;
		}
		
		public ICodeLaunchContentHandler getHandler() {
			if (handler == null && configurationElement != null && configurationElement.getAttribute(ATT_HANDLER) != null) {
				try {
					handler = (ICodeLaunchContentHandler) configurationElement.createExecutableExtension(ATT_HANDLER);
				} catch (final CoreException e) {
					RUIPlugin.getDefault().logError(ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							"Error occurred when loading content handler", e); //$NON-NLS-1$
				}
			}
			return handler;
		}
		
	}
	
	public static ConnectorConfig[] getAvailableConnectors() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUI.PLUGIN_ID, CONNECTOR_EXTENSION_POINT);
		final ConnectorConfig[] configs = new ConnectorConfig[elements.length];
		for (int i = 0; i < elements.length; i++) {
			configs[i] = new ConnectorConfig(elements[i]);
		}
		return configs;
	}
	
	public static ContentHandler[] getAvailableContentHandler() {
		final RCodeLaunchRegistry registry = getDefault();
		final HashMap<String,ContentHandler> map = registry.fContentHandler;
		synchronized (map) {
			final ContentHandler[] array = new ContentHandler[map.size()+1];
			int i = 0;
			array[i++] = registry.fDefaultHandler;
			for (final ContentHandler contentHandler : map.values()) {
				array[i++] = contentHandler;
			}
			return array;
		}
	}
	
	
/* Instance *******************************************************************/
	
	private IRCodeLaunchConnector fConnector;
	private final HashMap<String, ContentHandler> fContentHandler = new HashMap<String, ContentHandler>();
	private final ContentHandler fDefaultHandler = new ContentHandler();
	
	
	private RCodeLaunchRegistry() {
		fgRegistry = this;
		loadConnectorExtensions();
		loadHandlerExtensions();
		
		final InstanceScope scope = new InstanceScope();
		scope.getNode(PREF_R_CONNECTOR.getQualifier()).addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(final PreferenceChangeEvent event) {
				loadConnectorExtensions();
			}
		});
		scope.getNode(RDebugPreferenceConstants.CAT_CODELAUNCH_CONTENTHANDLER_QUALIFIER).addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(final PreferenceChangeEvent event) {
				loadHandlerPreferences();
			}
		});
	}
	
	
	private void loadConnectorExtensions() {
		fConnector = null;
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUI.PLUGIN_ID, CONNECTOR_EXTENSION_POINT);
		
		final String id = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_R_CONNECTOR);
		
		for (int i = 0; i < elements.length; i++) {
			if (id.equals(elements[i].getAttribute(ATT_ID))) {
				try {
					fConnector = (IRCodeLaunchConnector) elements[i].createExecutableExtension(ATT_CLASS);
					return;
				} catch (final Exception e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							NLS.bind("Error loading R Launch Connector ''{0}''.", elements[i].getAttribute(ATT_NAME)), e)); //$NON-NLS-1$
				}
			}
		}
	}
	
	private void loadHandlerExtensions() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUI.PLUGIN_ID, CONTENTHANDLER_EXTENSION_POINT);
		
		synchronized (fContentHandler) {
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals(CONTENTHANDLER_ELEMENT)) {
					final ContentHandler handlerData = new ContentHandler(elements[i]);
					final String contentTypeId = handlerData.getContentTypeId();
					if (contentTypeId != null && contentTypeId.length() > 0) {
						fContentHandler.put(contentTypeId, handlerData);
					}
				}
			}
		}
		
		loadHandlerPreferences();
	}
	
	private void loadHandlerPreferences() {
		final IEclipsePreferences node = new InstanceScope().getNode(RDebugPreferenceConstants.CAT_CODELAUNCH_CONTENTHANDLER_QUALIFIER);
		if (node == null) {
			return;
		}
		synchronized (fContentHandler) {
			for (final ContentHandler data : fContentHandler.values()) {
				final String command = node.get(data.getContentTypeId()+":command", null); //$NON-NLS-1$
				data.currentFileCommand = (command != null) ? command : data.defaultFileCommand;
			}
		}
	}
	
	private IRCodeLaunchConnector getConnector() throws CoreException {
		if (fConnector == null)
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, IStatus.OK, "No R Launch Connector configured.", null));
		
		return fConnector;
	}
	
	public ICodeLaunchContentHandler getContentHandler(final String contentType) {
		final ContentHandler data = fContentHandler.get(contentType);
		final ICodeLaunchContentHandler handler = (data != null) ? data.getHandler() : null;
		if (handler != null) {
			return handler;
		}
		return fDefaultHandler.getHandler();
	}
	
	public String getFileCommand(final String contentType) {
		final ContentHandler data = fContentHandler.get(contentType);
		final String command = (data != null) ? data.getCurrentFileCommand() : null;
		if (command != null) {
			return command;
		}
		return fDefaultHandler.getCurrentFileCommand();
	}
	
}
