/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ConstList;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IRPackageDescription;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpKeyword.Group;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.internal.core.RCorePlugin;


public class REnvHelp implements IREnvHelp {
	
	
	private final IREnv fREnv;
	
	private final String fDocDir;
	
	private final List<IRHelpKeyword.Group> fKeywords;
	
	private final List<IRPackageHelp> fPackages;
	private volatile Map<String, IRPackageHelp> fPackageMap;
	private volatile REnvIndexReader fIndexReader;
	
	private boolean fDisposed;
	
	private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
	
	
	public REnvHelp(final IREnv rEnv, final String docDir,
			final ConstList<Group> keywords, final ConstList<IRPackageHelp> packages) {
		fREnv = rEnv;
		fDocDir = docDir;
		fKeywords = keywords;
		fPackages = packages;
	}
	
	
	public void dispose() {
		fLock.writeLock().lock();
		try {
			fDisposed = true;
			fPackageMap = null;
			if (fIndexReader != null) {
				fIndexReader.dispose();
				fIndexReader = null;
			}
		}
		finally {
			fLock.writeLock().unlock();
		}
	}
	
	public IREnv getREnv() {
		return fREnv;
	}
	
	public List<IRHelpKeyword.Group> getKeywords() {
		return fKeywords;
	}
	
	public List<IRPackageHelp> getRPackages() {
		return fPackages;
	}
	
	public IRPackageHelp getRPackage(final String packageName) {
		return getPackageMap().get(packageName);
	}
	
	private Map<String, IRPackageHelp> getPackageMap() {
		Map<String, IRPackageHelp> map = fPackageMap;
		if (map == null) {
			if (fDisposed) {
				throw new IllegalStateException("This help index is no longer valid.");
			}
			synchronized (this) {
				map = fPackageMap;
				if (map == null) {
					map = new HashMap<String, IRPackageHelp>(fPackages.size());
					for (final IRPackageHelp packageHelp : fPackages) {
						map.put(packageHelp.getName(), packageHelp);
					}
					fPackageMap = map;
				}
			}
		}
		return map;
	}
	
	private REnvIndexReader getIndex() {
		REnvIndexReader reader = fIndexReader;
		if (reader == null) {
			if (fDisposed) {
				throw new IllegalStateException("This help index is no longer valid.");
			}
			synchronized (this) {
				reader = fIndexReader;
				if (reader == null) {
					final IREnvConfiguration config = fREnv.getConfig();
					if (config == null) {
						throw new IllegalStateException("This R environment is no longer valid.");
					}
					try {
						reader = new REnvIndexReader(config);
					}
					catch (final Exception e) {
						RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
								"An error occurred when initializing searcher for the R help index.", e));
						throw new RuntimeException("An error occurred when reading R help index.");
					}
					fIndexReader = reader;
				}
			}
		}
		return reader;
	}
	
	public IRHelpPage getPage(final String packageName, final String name) {
		final IRPackageHelp packageHelp = getPackageMap().get(packageName);
		if (packageHelp != null) {
			return packageHelp.getHelpPage(name);
		}
		return null;
	}
	
	public IRHelpPage getPageForTopic(final String packageName, final String topic) {
		final IRPackageHelp packageHelp = getPackageMap().get(packageName);
		if (packageHelp != null) {
			getIndex().getPageForTopic(packageHelp, topic);
		}
		return null;
	}
	
	public IRHelpPage getPageForTopic(final IRPackageHelp packageHelp, final String topic) {
		return getIndex().getPageForTopic(packageHelp, topic);
	}
	
	public String getHtmlPage(final IRHelpPage page) {
		return getHtmlPage(page.getPackage().getName(), page.getName(), null, null, null);
	}
	
	public String getHtmlPage(final String packageName, final String pageName) {
		return getHtmlPage(packageName, pageName, null, null, null);
	}
	
	public String getHtmlPage(final String packageName, final String pageName,
			final String queryString, final String[] preTags, final String[] postTags) {
		return getIndex().getHtmlPage(packageName, pageName, queryString, preTags, postTags);
	}
	
	public List<IRHelpPage> getPagesForTopic(final String topic) {
		return getIndex().getPagesForTopic(topic, getPackageMap());
	}
	
	
	public boolean search(final RHelpSearchQuery.Compiled query, final IRHelpSearchRequestor requestor) {
		return getIndex().search(query, fPackages, getPackageMap(), requestor);
	}
	
	
	public List<RHelpTopicEntry> getPackageTopics(final IRPackageHelp packageHelp) {
		return getIndex().getPackageTopics(packageHelp);
	}
	
	public IRPackageDescription getPackageDescription(final IRPackageHelp packageHelp) {
		return getIndex().getPackageDescription(packageHelp.getName(),
				packageHelp.getTitle(), packageHelp.getVersion() );
	}
	
	public String getDocDir() {
		return fDocDir;
	}
	
	
	public void lock() {
		fLock.readLock().lock();
	}
	
	public void unlock() {
		fLock.readLock().unlock();
	}
	
}
