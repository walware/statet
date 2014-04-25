/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.collections.ConstList;

import de.walware.rj.renv.IRPkgDescription;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpKeyword.Group;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.rhelp.index.REnvIndexReader;


public class REnvHelp implements IREnvHelp {
	
	
	private final IREnv fREnv;
	
	private final String fDocDir;
	
	private final List<IRHelpKeyword.Group> fKeywords;
	
	private final List<IRPkgHelp> fPackages;
	private volatile Map<String, IRPkgHelp> fPackageMap;
	private volatile REnvIndexReader fIndexReader;
	
	private boolean fDisposed;
	
	private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
	
	
	public REnvHelp(final IREnv rEnv, final String docDir,
			final ConstList<Group> keywords, final ConstList<IRPkgHelp> packages) {
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
	
	@Override
	public IREnv getREnv() {
		return fREnv;
	}
	
	@Override
	public List<IRHelpKeyword.Group> getKeywords() {
		return fKeywords;
	}
	
	@Override
	public List<IRPkgHelp> getRPackages() {
		return fPackages;
	}
	
	@Override
	public IRPkgHelp getRPackage(final String packageName) {
		return getPackageMap().get(packageName);
	}
	
	private Map<String, IRPkgHelp> getPackageMap() {
		Map<String, IRPkgHelp> map = fPackageMap;
		if (map == null) {
			if (fDisposed) {
				throw new IllegalStateException("This help index is no longer valid.");
			}
			synchronized (this) {
				map = fPackageMap;
				if (map == null) {
					map = new HashMap<>(fPackages.size());
					for (final IRPkgHelp pkgHelp : fPackages) {
						map.put(pkgHelp.getName(), pkgHelp);
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
	
	@Override
	public IRHelpPage getPage(final String packageName, final String name) {
		final IRPkgHelp pkgHelp = getPackageMap().get(packageName);
		if (pkgHelp != null) {
			return pkgHelp.getHelpPage(name);
		}
		return null;
	}
	
	@Override
	public IRHelpPage getPageForTopic(final String packageName, final String topic) {
		final IRPkgHelp pkgHelp = getPackageMap().get(packageName);
		if (pkgHelp != null) {
			return getIndex().getPageForTopic(pkgHelp, topic);
		}
		return null;
	}
	
	@Override
	public String getHtmlPage(final IRHelpPage page) {
		return getHtmlPage(page.getPackage().getName(), page.getName(), null, null, null);
	}
	
	@Override
	public String getHtmlPage(final String packageName, final String pageName) {
		return getHtmlPage(packageName, pageName, null, null, null);
	}
	
	@Override
	public String getHtmlPage(final String packageName, final String pageName,
			final String queryString, final String[] preTags, final String[] postTags) {
		return getIndex().getHtmlPage(packageName, pageName, queryString, preTags, postTags);
	}
	
	@Override
	public List<IRHelpPage> getPagesForTopic(final String topic) {
		return getIndex().getPagesForTopic(topic, getPackageMap());
	}
	
	
	public boolean search(final RHelpSearchQuery.Compiled query, final IRHelpSearchRequestor requestor) {
		return getIndex().search(query, fPackages, getPackageMap(), requestor);
	}
	
	
	public List<RHelpTopicEntry> getPkgTopics(final IRPkgHelp pkgHelp) {
		return getIndex().getPackageTopics(pkgHelp);
	}
	
	@Override
	public IRPkgDescription getPkgDescription(final String pkgName) {
		final IRPkgHelp pkgHelp= getPackageMap().get(pkgName);
		if (pkgHelp != null) {
			return getIndex().getPkgDescription(pkgHelp);
		}
		return null;
	}
	
	public String getDocDir() {
		return fDocDir;
	}
	
	
	public void lock() {
		fLock.readLock().lock();
	}
	
	@Override
	public void unlock() {
		fLock.readLock().unlock();
	}
	
}
