/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.ImList;

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


public final class REnvHelp implements IREnvHelp {
	
	
	private final IREnv rEnv;
	
	private final String docDir;
	
	private final ImList<IRHelpKeyword.Group> keywords;
	
	private final ImList<IRPkgHelp> packages;
	private volatile Map<String, IRPkgHelp> packageMap;
	private volatile REnvIndexReader indexReader;
	
	private boolean disposed;
	
	private final ReentrantReadWriteLock lock= new ReentrantReadWriteLock();
	
	
	public REnvHelp(final IREnv rEnv, final String docDir,
			final ImList<Group> keywords, final ImList<IRPkgHelp> packages) {
		this.rEnv= rEnv;
		this.docDir= docDir;
		this.keywords= keywords;
		this.packages= packages;
	}
	
	
	public void dispose() {
		this.lock.writeLock().lock();
		try {
			this.disposed= true;
			this.packageMap= null;
			if (this.indexReader != null) {
				this.indexReader.dispose();
				this.indexReader= null;
			}
		}
		finally {
			this.lock.writeLock().unlock();
		}
	}
	
	@Override
	public IREnv getREnv() {
		return this.rEnv;
	}
	
	
	public String getDocDir() {
		return this.docDir;
	}
	
	
	public void lock() {
		this.lock.readLock().lock();
	}
	
	@Override
	public void unlock() {
		this.lock.readLock().unlock();
	}
	
	
	@Override
	public ImList<IRHelpKeyword.Group> getKeywords() {
		return this.keywords;
	}
	
	@Override
	public ImList<IRPkgHelp> getPkgs() {
		return this.packages;
	}
	
	@Override
	public IRPkgHelp getPkgHelp(final String packageName) {
		return getPackageMap().get(packageName);
	}
	
	private Map<String, IRPkgHelp> getPackageMap() {
		Map<String, IRPkgHelp> map= this.packageMap;
		if (map == null) {
			if (this.disposed) {
				throw new IllegalStateException("This help index is no longer valid.");
			}
			synchronized (this) {
				map= this.packageMap;
				if (map == null) {
					map= new HashMap<>(this.packages.size());
					for (final IRPkgHelp pkgHelp : this.packages) {
						map.put(pkgHelp.getName(), pkgHelp);
					}
					this.packageMap= map;
				}
			}
		}
		return map;
	}
	
	private REnvIndexReader getIndex() {
		REnvIndexReader reader= this.indexReader;
		if (reader == null) {
			if (this.disposed) {
				throw new IllegalStateException("This help index is no longer valid.");
			}
			synchronized (this) {
				reader= this.indexReader;
				if (reader == null) {
					final IREnvConfiguration config= this.rEnv.getConfig();
					if (config == null) {
						throw new IllegalStateException("This R environment is no longer valid.");
					}
					try {
						reader= new REnvIndexReader(config);
					}
					catch (final Exception e) {
						RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
								"An error occurred when initializing searcher for the R help index.", e));
						throw new RuntimeException("An error occurred when reading R help index.");
					}
					this.indexReader= reader;
				}
			}
		}
		return reader;
	}
	
	@Override
	public IRHelpPage getPage(final String packageName, final String name) {
		final IRPkgHelp pkgHelp= getPackageMap().get(packageName);
		if (pkgHelp != null) {
			return pkgHelp.getHelpPage(name);
		}
		return null;
	}
	
	@Override
	public IRHelpPage getPageForTopic(final String packageName, final String topic) {
		final IRPkgHelp pkgHelp= getPackageMap().get(packageName);
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
		return getIndex().search(query, this.packages, getPackageMap(), requestor);
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
	
	@Override
	public boolean searchTopics(final String prefix, final String topicType,
			final List<String> packages, final ITopicSearchRequestor requestor) {
		if (requestor == null) {
			throw new NullPointerException("requestor"); //$NON-NLS-1$
		}
		return getIndex().searchTopics(prefix, topicType, packages, requestor);
	}
	
}
