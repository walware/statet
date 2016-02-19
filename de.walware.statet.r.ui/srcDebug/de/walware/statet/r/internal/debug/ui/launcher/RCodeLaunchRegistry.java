/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.debug.ui.launcher.RCodeLaunchRegistry.ContentHandler.FileCommand;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.launching.ICodeSubmitContentHandler;
import de.walware.statet.r.launching.IRCodeSubmitConnector;
import de.walware.statet.r.launching.RRunDebugPreferenceConstants;
import de.walware.statet.r.ui.RUI;


public class RCodeLaunchRegistry {
	
	
	public static boolean isConfigured() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUI.PLUGIN_ID, CONNECTOR_EXTENSION_POINT);
		return (elements != null && elements.length > 0);
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
	
	public static synchronized RCodeLaunchRegistry getDefault() {
		if (fgRegistry == null) {
			new RCodeLaunchRegistry();
		}
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
		
		public static class FileCommand {
			
			private final String fId;
			private final String fLabel;
			private final String fDefaultFileCommand;
			private String fCurrentFileCommand;
			
			
			public FileCommand(final String id, final String label, final String defaultCommand) {
				fId = id;
				fLabel = label;
				fDefaultFileCommand = fCurrentFileCommand = defaultCommand;
			}
			
			
			void setCurrentCommand(final String command) {
				fCurrentFileCommand = (command != null) ? command : fDefaultFileCommand;
			}
			
			
			public String getId() {
				return fId;
			}
			
			public String getLabel() {
				return fLabel;
			}
			
			public String getDefaultCommand() {
				return fDefaultFileCommand;
			}
			
			public String getCurrentCommand() {
				return fCurrentFileCommand;
			}
			
		}
		
		private final String fContentTypeId;
		IConfigurationElement fConfigurationElement;
		ICodeSubmitContentHandler fHandler;
		private final FileCommand[] fFileCommands;
		
		public ContentHandler(final IConfigurationElement config) {
			fContentTypeId = config.getAttribute(ATT_CONTENT_TYPE);
			fConfigurationElement = config;
			
			final IConfigurationElement[] fileCommandConfigs = config.getChildren(CONTENT_FILECOMMAND_ELEMENT);
			fFileCommands = new FileCommand[fileCommandConfigs.length];
			for (int i = 0; i < fileCommandConfigs.length; i++) {
				fFileCommands[i] = new FileCommand(
						fileCommandConfigs[i].getAttribute(ATT_ID),
						fileCommandConfigs[i].getAttribute(ATT_NAME),
						fileCommandConfigs[i].getAttribute(ATT_DEFAULTCOMMAND) );
			}
		}
		
		public String getContentTypeId() {
			return fContentTypeId;
		}
		
		public ICodeSubmitContentHandler getHandler() {
			if (fHandler == null && fConfigurationElement != null && fConfigurationElement.getAttribute(ATT_HANDLER) != null) {
				try {
					fHandler = (ICodeSubmitContentHandler) fConfigurationElement.createExecutableExtension(ATT_HANDLER);
				} catch (final CoreException e) {
					RUIPlugin.logError(ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							"Error occurred when loading content handler", e); //$NON-NLS-1$
				}
			}
			return fHandler;
		}
		
		public FileCommand getContentFileCommand() {
			if (fFileCommands.length > 0) {
				return fFileCommands[0];
			}
			return null;
		}
		
		public FileCommand[] getFileCommands() {
			return fFileCommands;
		}
		
		public FileCommand getFileCommand(final String id) {
			for (int i = 0; i < fFileCommands.length; i++) {
				if (fFileCommands[i].fId.equals(id)) {
					return fFileCommands[i];
				}
			}
			return null;
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
	
	public static FileCommand[] getAvailableFileCommands() {
		return getDefault().getFileCommands();
	}
	
	
/* Instance *******************************************************************/
	
	private IRCodeSubmitConnector fConnector;
	private final HashMap<String, ContentHandler> fContentHandler= new HashMap<>();
	private ContentHandler fDefaultHandler;
	
	
	private RCodeLaunchRegistry() {
		fgRegistry = this;
		loadConnectorExtensions();
		loadHandlerExtensions();
		
		final IScopeContext scope= InstanceScope.INSTANCE;
		scope.getNode(RRunDebugPreferenceConstants.PREF_R_CONNECTOR.getQualifier())
				.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent event) {
				loadConnectorExtensions();
			}
		});
		scope.getNode(RRunDebugPreferenceConstants.CAT_CODELAUNCH_CONTENTHANDLER_QUALIFIER)
				.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent event) {
				loadHandlerPreferences();
			}
		});
	}
	
	
	private void loadConnectorExtensions() {
		fConnector = null;
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(RUI.PLUGIN_ID, CONNECTOR_EXTENSION_POINT);
		
		final String id = PreferencesUtil.getInstancePrefs().getPreferenceValue(
				RRunDebugPreferenceConstants.PREF_R_CONNECTOR );
		
		for (int i = 0; i < elements.length; i++) {
			if (id.equals(elements[i].getAttribute(ATT_ID))) {
				try {
					fConnector = (IRCodeSubmitConnector) elements[i].createExecutableExtension(ATT_CLASS);
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
			fDefaultHandler = fContentHandler.get(RCore.R_CONTENT_ID);
			if (fDefaultHandler == null) {
				throw new IllegalStateException();
			}
		}
		
		loadHandlerPreferences();
	}
	
	private void loadHandlerPreferences() {
		final IEclipsePreferences node= InstanceScope.INSTANCE.getNode(
				RRunDebugPreferenceConstants.CAT_CODELAUNCH_CONTENTHANDLER_QUALIFIER );
		if (node == null) {
			return;
		}
		synchronized (fContentHandler) {
			for (final ContentHandler data : fContentHandler.values()) {
				for (final FileCommand fileCommand : data.getFileCommands()) {
					fileCommand.setCurrentCommand(node.get(fileCommand.fId+":command", null)); //$NON-NLS-1$
				}
			}
		}
	}
	
	public IRCodeSubmitConnector getConnector() throws CoreException {
		if (fConnector == null) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, IStatus.OK, RLaunchingMessages.SubmitCode_error_NoConnector_message, null));
		}
		
		return fConnector;
	}
	
	public ICodeSubmitContentHandler getContentHandler(final String contentType) {
		final ContentHandler data;
		synchronized (fContentHandler) {
			data = fContentHandler.get(contentType);
		}
		final ICodeSubmitContentHandler handler = (data != null) ? data.getHandler() : null;
		if (handler != null) {
			return handler;
		}
		return fDefaultHandler.getHandler();
	}
	
	public FileCommand getContentFileCommand(final String contentType) {
		final ContentHandler data;
		synchronized (fContentHandler) {
			data = fContentHandler.get(contentType);
		}
		final FileCommand fileCommand = (data != null) ? data.getContentFileCommand() : null;
		if (fileCommand != null) {
			return fileCommand;
		}
		return fDefaultHandler.getContentFileCommand();
	}
	
	public FileCommand[] getFileCommands() {
		synchronized (fContentHandler) {
			final List<FileCommand> list= new ArrayList<>(fContentHandler.size()*2);
			for (final ContentHandler data : fContentHandler.values()) {
				for (final FileCommand fileCommand : data.getFileCommands()) {
					list.add(fileCommand);
				}
			}
			return list.toArray(new FileCommand[list.size()]);
		}
	}
	
	public FileCommand getFileCommand(final String id) {
		synchronized (fContentHandler) {
			for (final ContentHandler data : fContentHandler.values()) {
				for (final FileCommand fileCommand : data.getFileCommands()) {
					if (fileCommand.getId().equals(id)) {
						return fileCommand;
					}
				}
			}
		}
		return null;
	}
	
}
