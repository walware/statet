/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License 
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve.internal.launchconfigs;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.base.StatetPlugin;
import de.walware.statet.r.rserve.RServePlugin;


public class ConnectionConfig {

	public static final String PROP_SERVERADDRESS = "serverAddress";
	private static final String DEFAULT_SERVERADDRESS = "127.0.0.1";
	private String fServerAddress;

	public static final String PROP_SERVERPORT = "serverPort";
	private static final int DEFAULT_SERVERPORT = 6311;
	private int fServerPort;

	
	public ConnectionConfig() {
		
		this(true);
	}
	
	private ConnectionConfig(boolean activateBeanSupport) {
		
		fServerAddress = DEFAULT_SERVERADDRESS;
		fServerPort = DEFAULT_SERVERPORT;
		
		if (activateBeanSupport) {
			fPropertyChangeSupport = new PropertyChangeSupport(this);
		}
	}
	
	
	
	public void setServerAddress(String address) {
		
		String oldValue = fServerAddress;
		fServerAddress = address;
		fPropertyChangeSupport.firePropertyChange(PROP_SERVERADDRESS, 
				oldValue, address);
	}

	public String getServerAddress() {
		
		return fServerAddress;
	}
	
	
	public void setServerPort(int port) {
		
		int oldValue = fServerPort;
		fServerPort = port;
		fPropertyChangeSupport.firePropertyChange(PROP_SERVERPORT, 
				oldValue, port);
	}
	
	public int getServerPort() {
		
		return fServerPort;
	}


//-- ILaunchConfigurationAdapter
	public static void writeDefaultsTo(ILaunchConfigurationWorkingCopy configuration) {
		
		new ConnectionConfig(false).writeTo(configuration);
	}
	
	public void readFrom(ILaunchConfiguration configuration) {
		
		String address = read(configuration, 
				IRServeConstants.CONFIG_CONNECTION_SERVERADDRESS, DEFAULT_SERVERADDRESS);
		setServerAddress(address);
		
		int port = read(configuration, 
				IRServeConstants.CONFIG_CONNECTION_SERVERPORT, DEFAULT_SERVERPORT);
		setServerPort(port);
	}
	
	public void writeTo(ILaunchConfigurationWorkingCopy configuration) {
		
		String address = getServerAddress();
		configuration.setAttribute(IRServeConstants.CONFIG_CONNECTION_SERVERADDRESS, address);
		
		int port = getServerPort();
		configuration.setAttribute(IRServeConstants.CONFIG_CONNECTION_SERVERPORT, port);
	}

	
//-- Bean-Support
	private PropertyChangeSupport fPropertyChangeSupport;
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		
		fPropertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		
		fPropertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		
		fPropertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		
		fPropertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
	
	
	protected String read(ILaunchConfiguration config, String attributeName, String defaultValue) {
		
		String s = defaultValue;
		try {
			s = config.getAttribute(attributeName, defaultValue);
		} catch (CoreException ce) {
			logError("Error reading configuration (attribute: " + attributeName + ").", ce);
		}
		return s;
	}
	
	protected int read(ILaunchConfiguration config, String attributeName, int defaultValue) {
		
		int i = defaultValue;
		try {
			i = config.getAttribute(attributeName, i);
		} catch (CoreException ce) {
			logError("Error reading configuration (attribute: " + attributeName + ").", ce);
		}
		return i;
	}
	
	protected void logError(String msg, CoreException ce) {
		
		StatetPlugin.log(new Status(
				IStatus.ERROR, RServePlugin.PLUGIN_ID, IStatetStatusConstants.LAUNCHCONFIG_ERROR,
				msg, ce));
	}
	
}
