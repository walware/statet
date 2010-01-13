/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.IResourceMapping;


public class ResourceMappingManager {
	
	
	private static final String QUALIFIER = NicoCore.PLUGIN_ID + "/resoursemappings"; //$NON-NLS-1$
	
	private static final String LOCAL_KEY = "local.path"; //$NON-NLS-1$
	private static final String HOST_KEY = "host.name"; //$NON-NLS-1$
	private static final String REMOTE_KEY = "remote.path"; //$NON-NLS-1$
	
	
	private class UpdateJob extends Job {
		
		
		UpdateJob() {
			super("Update Resource Mappings");
			setPriority(BUILD);
		}
		
		public void schedule(final List<ResourceMapping> list) {
			schedule();
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final List<ResourceMapping> list = fList;
			if (list == null) {
				return Status.OK_STATUS;
			}
			final Map<String, Set<IResourceMapping>> mappingsByHost = new HashMap<String, Set<IResourceMapping>>();
			
			final SubMonitor progress = SubMonitor.convert(monitor, list.size() +1);
			final MultiStatus status = new MultiStatus(NicoCore.PLUGIN_ID, 0, "Update Resource Mapping", null);
			for (int i = 0; i < list.size(); i++) {
				progress.setWorkRemaining(list.size()-i +1);
				final ResourceMapping mapping = list.get(i);
				try {
					mapping.resolve();
					
					final InetAddress[] addresses = mapping.getHostAddresses();
					for (final InetAddress inetAddress : addresses) {
						final String host = inetAddress.getHostAddress();
						Set<IResourceMapping> mappings = mappingsByHost.get(host);
						if (mappings == null) {
							mappings = new HashSet<IResourceMapping>();
							mappingsByHost.put(host, mappings);
						}
						mappings.add(mapping);
					}
				}
				catch (final UnknownHostException e) {
					status.add(new Status(IStatus.INFO, NicoCore.PLUGIN_ID, "Unknown host: " + e.getMessage(), e));
				}
			}
			
			synchronized(ResourceMappingManager.this) {
				if (fList == list) {
					fMappingsByHost = mappingsByHost;
				}
			}
			return status;
		}
		
	}
	
	
	private List<ResourceMapping> fList;
	private Map<String, Set<IResourceMapping>> fMappingsByHost;
	
	private final UpdateJob fUpdateJob;
	
	
	public ResourceMappingManager() {
		fUpdateJob = new UpdateJob();
		load();
	}
	
	
	public void dispose() {
		synchronized (this) {
			fList = null;
			fUpdateJob.cancel();
		}
	}
	
	
	public List<ResourceMapping> getList() {
		return fList;
	}
	
	public Set<IResourceMapping> getMappingsFor(final String hostAddress) {
		final Map<String, Set<IResourceMapping>> byHost = fMappingsByHost;
		if (byHost != null) {
			return byHost.get(hostAddress);
		}
		return null;
	}
	
	public void load() {
		try {
			final List<ResourceMapping> list = new ArrayList<ResourceMapping>();
			
			final IEclipsePreferences rootNode = new InstanceScope().getNode(QUALIFIER);
			final String[] names = rootNode.childrenNames();
			for (final String name : names) {
				final ResourceMapping mapping = read(rootNode.node(name));
				if (mapping != null) {
					list.add(mapping);
				}
			}
			
			synchronized (this) {
				fList = list;
				fUpdateJob.cancel();
				fUpdateJob.schedule();
			}
		}
		catch (final BackingStoreException e) {
			NicoPlugin.logError(-1, "Failed to load resource mappings.", e);
		}
	}
	
	public void setPreferences(final List<ResourceMapping> list) {
		try {
			final IEclipsePreferences rootNode = new InstanceScope().getNode(QUALIFIER);
			
			final List<String> names = new LinkedList<String>(Arrays.asList(rootNode.childrenNames()));
			final List<ResourceMapping> mappings = new LinkedList<ResourceMapping>(list);
			
			int maxIdx = 0;
			for (final Iterator<ResourceMapping> iter = mappings.iterator(); iter.hasNext(); ) {
				final ResourceMapping mapping = iter.next();
				final String id = mapping.getId();
				if (id != null) {
					try {
						final int idx = Integer.parseInt(id);
						if (idx > maxIdx) {
							maxIdx = idx;
						}
					}
					catch (final NumberFormatException e) {
					}
					iter.remove();
					names.remove(id);
					
					write(rootNode.node(id), mapping);
				}
			}
			for (final Iterator<ResourceMapping> iter = mappings.iterator(); iter.hasNext(); ) {
				final ResourceMapping mapping = iter.next();
				final String id = Integer.toString(++maxIdx);
				mapping.setId(id);
				names.remove(id);
				write(rootNode.node(id), mapping);
			}
			for (final String name : names) {
				if (rootNode.nodeExists(name)) {
					final Preferences node = rootNode.node(name);
					node.removeNode();
				}
			}
			rootNode.flush();
			
			synchronized (this) {
				fList = list;
				fUpdateJob.cancel();
				fUpdateJob.schedule();
			}
		}
		catch (final BackingStoreException e) {
			NicoPlugin.logError(-1, "Failed to save resource mappings.", e);
		}
	}
	
	
	protected ResourceMapping read(final Preferences node) {
		final String id = node.name();
		final String local = node.get(LOCAL_KEY, null);
		final String host = node.get(HOST_KEY, null);
		final String remote = node.get(REMOTE_KEY, null);
		if (local != null && host != null && remote != null) {
			try {
				return new ResourceMapping(id, local, host, remote);
			}
			catch (final CoreException e) {
				NicoPlugin.logError(-1, NLS.bind("Failed to load resource mapping: ''{0}''.", id), e);
			}
		}
		return null;
	}
	
	protected void write(final Preferences node, final ResourceMapping mapping) {
		node.put(LOCAL_KEY, mapping.getLocalText());
		node.put(HOST_KEY, mapping.getHost());
		node.put(REMOTE_KEY, mapping.getRemotePath().toString());
	}
	
}
