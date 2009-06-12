/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.debug.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.internal.core.LaunchConfiguration;


/**
 * Launch configuration which allows to adds temporary additional or overwrite existing
 * attributes to a launch configuration.
 */
public class OverlayLaunchConfiguration extends LaunchConfiguration {
	
	
	private final ILaunchConfiguration fLaunchConfiguration;
	
	private final Map<String, Object> fAdditionalAttributes;
	
	
	public OverlayLaunchConfiguration(final ILaunchConfiguration orginal, final Map<String, Object> additional) {
		super(orginal.getLocation());
		fLaunchConfiguration = orginal;
		fAdditionalAttributes = additional;
	}
	
	
	public ILaunchConfiguration getOriginal() {
		return fLaunchConfiguration;
	}
	
	
	@Override
	public boolean contentsEqual(final ILaunchConfiguration configuration) {
		return fLaunchConfiguration.contentsEqual(configuration);
	}
	
	@Override
	public ILaunchConfigurationWorkingCopy copy(final String name) throws CoreException {
		return fLaunchConfiguration.copy(name);
	}
	
	@Override
	public void delete() throws CoreException {
		fLaunchConfiguration.delete();
	}
	
	@Override
	public boolean exists() {
		return fLaunchConfiguration.exists();
	}
	
	@Override
	public boolean hasAttribute(final String attributeName) throws CoreException {
		if (fAdditionalAttributes.containsKey(attributeName)) {
			return true;
		}
		return fLaunchConfiguration.hasAttribute(attributeName);
	}
	
	@Override
	public boolean getAttribute(final String attributeName, final boolean defaultValue) throws CoreException {
		final Object obj = fAdditionalAttributes.get(attributeName);
		if (obj instanceof Boolean) {
			return ((Boolean) obj).booleanValue();
		}
		return fLaunchConfiguration.getAttribute(attributeName, defaultValue);
	}
	
	@Override
	public int getAttribute(final String attributeName, final int defaultValue) throws CoreException {
		final Object obj = fAdditionalAttributes.get(attributeName);
		if (obj instanceof Integer) {
			return ((Integer) obj).intValue();
		}
		return fLaunchConfiguration.getAttribute(attributeName, defaultValue);
	}
	
	@Override
	public List getAttribute(final String attributeName, final List defaultValue) throws CoreException {
		final Object obj = fAdditionalAttributes.get(attributeName);
		if (obj instanceof List) {
			return (List) obj;
		}
		return fLaunchConfiguration.getAttribute(attributeName, defaultValue);
	}
	
	@Override
	public Set getAttribute(final String attributeName, final Set defaultValue) throws CoreException {
		final Object obj = fAdditionalAttributes.get(attributeName);
		if (obj instanceof Set) {
			return ((Set) obj);
		}
		return fLaunchConfiguration.getAttribute(attributeName, defaultValue);
	}
	
	@Override
	public Map getAttribute(final String attributeName, final Map defaultValue) throws CoreException {
		final Object obj = fAdditionalAttributes.get(attributeName);
		if (obj instanceof Map) {
			return (Map) obj;
		}
		return fLaunchConfiguration.getAttribute(attributeName, defaultValue);
	}
	
	@Override
	public String getAttribute(final String attributeName, final String defaultValue) throws CoreException {
		final Object obj = fAdditionalAttributes.get(attributeName);
		if (obj instanceof String) {
			return (String) obj;
		}
		return fLaunchConfiguration.getAttribute(attributeName, defaultValue);
	}
	
	@Override
	public Map getAttributes() throws CoreException {
		return fLaunchConfiguration.getAttributes();
	}
	
	@Override
	public String getCategory() throws CoreException {
		return fLaunchConfiguration.getCategory();
	}
	
	@Override
	public IFile getFile() {
		return fLaunchConfiguration.getFile();
	}
	
	@Override
	public IPath getLocation() {
		return fLaunchConfiguration.getLocation();
	}
	
	@Override
	public IResource[] getMappedResources() throws CoreException {
		return fLaunchConfiguration.getMappedResources();
	}
	
	@Override
	public String getMemento() throws CoreException {
		return fLaunchConfiguration.getMemento();
	}
	
	@Override
	public Set getModes() throws CoreException {
		return fLaunchConfiguration.getModes();
	}
	
	@Override
	public String getName() {
		return fLaunchConfiguration.getName();
	}
	
	@Override
	public ILaunchDelegate getPreferredDelegate(final Set modes) throws CoreException {
		return fLaunchConfiguration.getPreferredDelegate(modes);
	}
	
	@Override
	public ILaunchConfigurationType getType() throws CoreException {
		return fLaunchConfiguration.getType();
	}
	
	@Override
	public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
		return fLaunchConfiguration.getWorkingCopy();
	}
	
	@Override
	public boolean isLocal() {
		return fLaunchConfiguration.isLocal();
	}
	
	@Override
	public boolean isMigrationCandidate() throws CoreException {
		return fLaunchConfiguration.isMigrationCandidate();
	}
	
	@Override
	public boolean isReadOnly() {
		return fLaunchConfiguration.isReadOnly();
	}
	
	@Override
	public boolean isWorkingCopy() {
		return false;
	}
	
	
	
	@Override
	public void migrate() throws CoreException {
		fLaunchConfiguration.migrate();
	}
	
	@Override
	public boolean supportsMode(final String mode) throws CoreException {
		return fLaunchConfiguration.supportsMode(mode);
	}
	
	@Override
	public Object getAdapter(final Class adapter) {
		return fLaunchConfiguration.getAdapter(adapter);
	}
	
}
