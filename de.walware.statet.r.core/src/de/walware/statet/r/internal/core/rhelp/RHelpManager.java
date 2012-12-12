/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.renv.REnvConfiguration;
import de.walware.statet.r.internal.core.renv.REnvManager;
import de.walware.statet.r.internal.core.rhelp.RHelpWebapp.ContentInfo;


public class RHelpManager implements IRHelpManager, SettingsChangeNotifier.ChangeListener, IDisposable {
// Compatible to dynamic R help
// 1) With dynamic = TRUE from tools:::httpd()
//    Here generated links are of the forms
//    ../../pkg/help/topic
//    file.html
//    ../../pkg/html/file.html
//    and links are never missing: topics are always linked as
//    ../../pkg/help/topic for the current packages, and this means
//    'search this package then all the others, and show all matches
//    if we need to go outside this packages'
	
	
	private static class EnvItem {
		
		final String id;
		
		int state;
		
		REnvHelp help;
		String indexDir;
		
		final Object helpLock = new Object();
		final Object indexLock = new Object();
		
		public EnvItem(final String id) {
			this.id = id;
			this.state = 0;
			this.help = null;
			this.indexDir = null;
		}
		
	}
	
	private class InitJob extends Job {
		
		public InitJob() {
			super("Prepare R help");
			setSystem(true);
			setPriority(Job.DECORATE);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			ensureIsRunning();
			
			final IREnv rEnv = fREnvManager.getDefault().resolve();
			if (rEnv != null) {
				final REnvHelp help = getHelp(rEnv);
				if (help != null) {
					help.unlock();
				}
			}
			final Set<String> rEnvIds = SaveUtil.getExistingRHelpEnvId();
			rEnvIds.removeAll(Arrays.asList(fREnvManager.getIds()));
			for (final String rEnvId : rEnvIds) {
				delete(rEnvId);
			}
			
			return Status.OK_STATUS;
		}
		
	}
	
	private static final int HELP_LOADED = 1;
	private static final int HELP_MISSING = -1;
	private static final int RENV_DELETED = -2;
	
	/**
	 * Searches topic in library
	 */
	private static final String RHELP_TOPIC_PATH = "/topic"; //$NON-NLS-1$
	
	/**
	 * Shows page (package, package/name or package/topic)
	 */
	private static final String RHELP_PAGE_PATH = "/page"; //$NON-NLS-1$
	
	
	private final REnvManager fREnvManager = RCorePlugin.getDefault().getREnvManager();
	private boolean fRunning;
	
	private boolean fHttpdStarted;
	private JettyServer fHttpd;
	
	private final Object fIndexLock = new Object();
	
	private final SaveUtil fSaveUtil = new SaveUtil();
	
	private final Map<String, EnvItem> fHelpIndexes = new HashMap<String, EnvItem>();
	
	
	public RHelpManager() {
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(this);
		new InitJob().schedule(1000);
	}
	
	
	@Override
	public String getPageHttpUrl(final IRHelpPage page, final String target) {
		final IRPackageHelp packageHelp = page.getPackage();
		return getPageHttpUrl(packageHelp.getName(), page.getName(), packageHelp.getREnv(), target);
	}
	
	@Override
	public String getPageHttpUrl(final String packageName, final String pageName,
			final IREnv rEnv, final String target) {
		checkRunning();
		final StringBuilder sb = new StringBuilder(64);
		sb.append(RHelpWebapp.CONTEXT_PATH);
		sb.append('/');
		sb.append(target);
		sb.append('/');
		sb.append(rEnv.getId());
		sb.append('/');
		sb.append(RHelpWebapp.CAT_LIBRARY);
		sb.append('/');
		sb.append(packageName);
		sb.append('/');
		if (pageName != null) {
			sb.append(RHelpWebapp.COMMAND_HTML_PAGE);
			sb.append('/');
			sb.append(pageName);
			sb.append(".html"); //$NON-NLS-1$
		}
		return createUrl(sb.toString());
	}
	
	@Override
	public String getTopicHttpUrl(final String topic, final String packageName,
			final IREnv rEnv, final String target) {
		checkRunning();
		final StringBuilder sb = new StringBuilder(64);
		sb.append(RHelpWebapp.CONTEXT_PATH);
		sb.append('/');
		sb.append(target);
		sb.append('/');
		sb.append(rEnv.getId());
		sb.append('/');
		sb.append(RHelpWebapp.CAT_LIBRARY);
		sb.append('/');
		sb.append(packageName);
		sb.append('/');
		sb.append(RHelpWebapp.COMMAND_HELP_TOPIC);
		sb.append('/');
		sb.append(topic);
		return createUrl(sb.toString());
	}
	
	@Override
	public String getREnvHttpUrl(final IREnv rEnv, final String target) {
		checkRunning();
		final StringBuilder sb = new StringBuilder(64);
		sb.append(RHelpWebapp.CONTEXT_PATH);
		sb.append('/');
		sb.append(target);
		sb.append('/');
		sb.append(rEnv.getId());
		sb.append('/');
		return createUrl(sb.toString());
	}
	
	@Override
	public String getPackageHttpUrl(final IRPackageHelp packageHelp, final String target) {
		return getPageHttpUrl(packageHelp.getName(), null, packageHelp.getREnv(), target);
	}
	
	@Override
	public String toHttpUrl(final String url, final IREnv rEnv, final String target) {
		checkRunning();
		if (url.startsWith("rhelp:///")) { //$NON-NLS-1$
			final StringBuilder sb = new StringBuilder(64);
			sb.append(RHelpWebapp.CONTEXT_PATH);
			sb.append('/');
			sb.append(target);
			
			final String path = url.substring(8);
			final int idx1 = (path.length() > 0) ? path.indexOf('/', 1) : -1;
			if (idx1 > 0) {
				final String command = path.substring(0, idx1);
				if (command.equals(RHELP_PAGE_PATH)) {
					sb.append('/');
					sb.append(rEnv.getId());
					sb.append('/');
					sb.append(RHelpWebapp.CAT_LIBRARY);
					final int idx2 = path.indexOf('/', idx1+1);
					if (idx2 > idx1+1 && idx2 < path.length()-1) {
						sb.append('/');
						sb.append(path.substring(idx1+1, idx2));
						sb.append('/');
						sb.append(RHelpWebapp.COMMAND_HTML_PAGE);
						sb.append('/');
						sb.append(path.substring(idx2+1));
						sb.append(".html");
					}
					else {
						sb.append('/');
						sb.append(path.substring(idx1+1, (idx2 > 0) ? idx2 : path.length()));
						sb.append('/');
					}
					return createUrl(sb.toString());
				}
				else if (command.equals(RHELP_TOPIC_PATH)) {
					sb.append('/');
					sb.append(rEnv.getId());
					sb.append('/');
					sb.append(RHelpWebapp.CAT_LIBRARY);
					sb.append('/');
					sb.append('-');
					sb.append('/');
					sb.append(RHelpWebapp.COMMAND_HELP_TOPIC);
					sb.append('/');
					sb.append(path.substring(idx1+1));
					return createUrl(sb.toString());
				}
			}
			else if (path.length() == 1) { // start
				sb.append('/');
				sb.append(rEnv.getId());
				sb.append('/');
				return createUrl(sb.toString());
			}
			return null;
		}
		if (url.startsWith("http://") && (rEnv != null || target != null)) { //$NON-NLS-1$
			try {
				final URI uri = new URI(url);
				if (isDynamic(uri)) {
					final String path = uri.getPath();
					if (path != null && path.startsWith(RHelpWebapp.CONTEXT_PATH)) {
						final int idx2 = path.indexOf('/', RHelpWebapp.CONTEXT_PATH.length()+1);
						if (idx2 >= 0) {
							final StringBuilder sb = new StringBuilder(path.length()+16);
							sb.append(RHelpWebapp.CONTEXT_PATH);
							sb.append('/');
							sb.append((target != null) ? target :
									path.substring(RHelpWebapp.CONTEXT_PATH.length()+1, idx2));
							final String info = path.substring(idx2+1);
							if (rEnv != null) {
								final int idx3 = info.indexOf('/');
								if (idx3 < 0) {
									return null;
								}
								sb.append('/');
								sb.append(rEnv.getId());
								sb.append(info.substring(idx3));
							}
							else {
								sb.append('/');
								sb.append(info);
							}
							return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
									sb.toString(), uri.getQuery(), uri.getFragment()).toString();
						}
					}
					return null;
				}
			}
			catch (final Exception e) {}
		}
		return url;
	}
	
	@Override
	public String toHttpUrl(final Object object, final String target) {
		if (object == this) {
			return "about:blank"; //$NON-NLS-1$
		}
		if (object instanceof IREnv) {
			return getREnvHttpUrl((IREnv) object, target);
		}
		if (object instanceof IREnvConfiguration) {
			return getREnvHttpUrl(((IREnvConfiguration) object).getReference(), target);
		}
		if (object instanceof IRPackageHelp) {
			return getPackageHttpUrl((IRPackageHelp) object, target);
		}
		if (object instanceof IRHelpPage) {
			return getPageHttpUrl((IRHelpPage) object, target);
		}
		if (object instanceof String) {
			final String s = (String) object;
			if (s.startsWith("http://")) {
				return s;
			}
		}
		return null;
	}
	
	private String createUrl(final String path) {
		try {
			return new URI("http", null, fHttpd.getHost(), fHttpd.getPort(), path, null, null) //$NON-NLS-1$
					.toASCIIString();
		}
		catch (final URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public Object getContentOfUrl(final String url) {
		try {
			final URI uri = new URI(url);
			final String path = uri.getPath();
			if (uri.getScheme() != null && uri.getScheme().equals("http") //$NON-NLS-1$
					&& path != null && path.startsWith(RHelpWebapp.CONTEXT_PATH)) {
				final int idx2 = path.indexOf('/', RHelpWebapp.CONTEXT_PATH.length()+1);
				if (idx2 >= 0) {
					final ContentInfo info = RHelpWebapp.extractContent(path.substring(idx2));
					if (info != null) {
						final IREnv rEnv = fREnvManager.get(info.rEnvId, null);
						if (rEnv != null && info.cat == RHelpWebapp.CAT_LIBRARY) {
							final REnvHelp help = getHelp(rEnv);
							if (help != null) {
								try {
									final IRPackageHelp packageHelp = help.getRPackage(info.packageName);
									if (packageHelp != null && info.command == RHelpWebapp.COMMAND_HTML_PAGE) {
										final IRHelpPage page = packageHelp.getHelpPage(info.detail);
										if (page != null) {
											return page;
										}
									}
									return packageHelp;
								}
								finally {
									help.unlock();
								}
							}
							return null;
						}
						if (rEnv != null && info.cat == RHelpWebapp.CAT_DOC) {
							return new Object[] { rEnv, null };
						}
						return rEnv;
					}
				}
			}
		} catch (final URISyntaxException e) {}
		return null;
	}
	
	@Override
	public boolean ensureIsRunning() {
		if (!fHttpdStarted) {
			startServer();
		}
		return fRunning;
	}
	
	@Override
	public boolean isDynamic(final URI url) {
		checkRunning();
		return ((fHttpd.getHost().equals(url.getHost()) && fHttpd.getPort() == url.getPort()
				|| PORTABLE_URL_SCHEME.equals(url.getScheme()) ));
	}
	
	@Override
	public URI toHttpUrl(final URI url) throws URISyntaxException {
		if (isDynamic(url)) {
			String path = url.getPath();
			if (path != null && !path.startsWith("/rhelp")) {
				path = "/rhelp" + path;
			}
			return new URI("http", null, fHttpd.getHost(), fHttpd.getPort(),
					path, url.getQuery(), url.getFragment());
		}
		return url;
	}
	
	@Override
	public URI toPortableUrl(final URI url) throws URISyntaxException {
		if (isDynamic(url)) {
			String path = url.getPath();
			if (path != null && path.startsWith("/rhelp")) {
				path = path.substring(6);
			}
			return new URI(PORTABLE_URL_SCHEME, null, null, -1,
					path, url.getQuery(), url.getFragment());
		}
		return url;
	}
	
	private void checkRunning() {
		if (!ensureIsRunning()) {
			throw new UnsupportedOperationException("Help is not available.");
		}
	}
	
	private synchronized void startServer() {
		if (fHttpdStarted) {
			return;
		}
		final JettyServer httpd = new JettyServer();
		try {
			httpd.startServer();
			fHttpd = httpd;
			fRunning = true;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
						"An error occured when starting webserver for R help.", e));
			try {
				httpd.stopServer();
			}
			catch (final Exception ignore) {}
		}
		fHttpdStarted = true;
	}
	
	private synchronized void stopServer() {
		if (fHttpd == null) {
			return;
		}
		fRunning = false;
		try {
			fHttpd.stopServer();
			fHttpd = null;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occured when stopping webserver for R help.", e));
		}
	}
	
	
	@Override
	public void search(final RHelpSearchQuery query, final IRHelpSearchRequestor requestor,
			final IProgressMonitor monitor) throws CoreException {
		if (query == null) {
			throw new NullPointerException("query"); //$NON-NLS-1$
		}
		if (requestor == null) {
			throw new NullPointerException("requestor"); //$NON-NLS-1$
		}
		final RHelpSearchQuery.Compiled compiledQuery = query.compile();
		final REnvHelp help = getHelp(compiledQuery.getREnv());
		if (help != null) {
			try {
				if (!help.search(compiledQuery, requestor)) {
					throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
							"An internal error occurend when performing R help search.", null));
				}
			}
			finally {
				help.unlock();
			}
		}
		else {
			final IREnv rEnv = query.getREnv();
			if (rEnv == null || rEnv.getConfig() == null) {
				throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
						"The selected R environment doesn't exists.", null));
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
						"The R library of the selected R environment <code>" + compiledQuery.getREnv().getName() +
						"</code> is not yet indexed. Please run the indexer first to enable R help support.", null));
			}
		}
	}
	
	
	@Override
	public void settingsChanged(final Set<String> groupIds) {
		if (groupIds.contains(IREnvManager.SETTINGS_GROUP_ID)) {
			final List<IREnvConfiguration> configurations = fREnvManager.getConfigurations();
			final EnvItem[] items = new EnvItem[configurations.size()]; 
			synchronized (fIndexLock) {
				for (int i = 0; i < configurations.size(); i++) {
					items[i] = fHelpIndexes.get(configurations.get(i).getReference().getId());
				}
			}
			for (int i = 0; i < configurations.size(); i++) {
				if (items[i] != null) {
					final EnvItem item = items[i];
					final IREnvConfiguration config = configurations.get(i);
					REnvHelp oldHelp = null;
					synchronized (item.helpLock) {
						if (!((item.indexDir != null) ? 
								item.indexDir.equals(config.getIndexDirectoryPath()) :
								null == config.getIndexDirectoryPath() )) {
							item.state = 0;
							oldHelp = item.help;
							item.help = null;
						}
					}
					if (oldHelp != null) {
						oldHelp.dispose();
					}
				}
			}
		}
	}
	
	public boolean updateHelp(final IREnvConfiguration rEnvConfig, 
			final Map<String, String> rEnvSharedProperties, final REnvHelp help) {
		final IREnv rEnv = help.getREnv();
		final String rEnvId = rEnv.getId();
		EnvItem item;
		synchronized (fIndexLock) {
			item = fHelpIndexes.get(rEnvId);
			if (item == null) {
				item = new EnvItem(rEnvId);
				fHelpIndexes.put(rEnvId, item);
			}
		}
		REnvHelp oldHelp = null;
		try {
			synchronized (item.helpLock) {
				if (item.state == RENV_DELETED) {
					oldHelp = help;
					fSaveUtil.delete(rEnvId);
					return false;
				}
				item.state = HELP_LOADED;
				item.indexDir = rEnvConfig.getIndexDirectoryPath();
				oldHelp = item.help;
				item.help = help;
				fSaveUtil.save(rEnvConfig, help);
				if (rEnvConfig instanceof REnvConfiguration) {
					((REnvConfiguration) rEnvConfig).updateSharedProperties(rEnvSharedProperties);
				}
				return true;
			}
		}
		finally {
			if (oldHelp != null) {
				oldHelp.dispose();
			}
		}
	}
	
	public void delete(final String id) {
		if (id != null && id.length() > 0) {
			EnvItem item;
			synchronized (fIndexLock) {
				item = fHelpIndexes.get(id);
				if (item == null) {
					item = new EnvItem(id);
					fHelpIndexes.put(id, item);
				}
			}
			REnvHelp oldHelp = null;
			synchronized (item.helpLock) {
				item.state = RENV_DELETED;
				oldHelp = item.help;
				item.help = null;
			}
			if (oldHelp != null) {
				oldHelp.dispose();
			}
			synchronized (item.helpLock) {
				fSaveUtil.delete(id);
			}
		}
	}
	
	@Override
	public List<IREnv> getREnvWithHelp() {
		final List<IREnvConfiguration> configurations = fREnvManager.getConfigurations();
		final EnvItem[] items = new EnvItem[configurations.size()];
		synchronized (fIndexLock) {
			for (int i = 0; i < configurations.size(); i++) {
				final String id = configurations.get(i).getReference().getId();
				EnvItem item = fHelpIndexes.get(id);
				if (item == null) {
					item = new EnvItem(id);
					fHelpIndexes.put(id, item);
				}
				items[i] = item;
			}
		}
		final List<IREnv> withHelp = new ArrayList<IREnv>(configurations.size());
		for (int i = 0; i < items.length; i++) {
			final EnvItem item = items[i];
			final IREnvConfiguration rEnvConfig = configurations.get(i);
			if (rEnvConfig.isDeleted()) {
				continue;
			}
			synchronized (item.helpLock) {
				switch (item.state) {
				case HELP_LOADED:
					withHelp.add(rEnvConfig.getReference());
					continue;
				case 0:
					if (fSaveUtil.hasIndex(rEnvConfig)) {
						withHelp.add(rEnvConfig.getReference());
					}
					else {
						item.state = HELP_MISSING;
					}
					continue;
				default:
					continue;
				}
			}
		}
		return withHelp;
	}
	
	@Override
	public boolean hasHelp(IREnv rEnv) {
		if (rEnv != null) {
			rEnv = rEnv.resolve();
		}
		if (rEnv != null) {
			final String id = rEnv.getId();
			EnvItem item;
			synchronized (fIndexLock) {
				item = fHelpIndexes.get(id);
				if (item == null) {
					item = new EnvItem(id);
					fHelpIndexes.put(id, item);
				}
			}
			synchronized (item.helpLock) {
				switch (item.state) {
				case HELP_LOADED:
					return true;
				case 0:
					if (fSaveUtil.hasIndex(rEnv.getConfig())) {
						return true;
					}
					else {
						item.state = HELP_MISSING;
						return false;
					}
				default:
					return false;
				}
			}
		}
		return false;
	}
	
	@Override
	public REnvHelp getHelp(IREnv rEnv) {
		if (rEnv != null) {
			rEnv = rEnv.resolve();
		}
		if (rEnv != null) {
			final String rEnvId = rEnv.getId();
			EnvItem item;
			synchronized (fIndexLock) {
				item = fHelpIndexes.get(rEnvId);
				if (item == null) {
					item = new EnvItem(rEnvId);
					fHelpIndexes.put(rEnvId, item);
				}
			}
			synchronized (item.helpLock) {
				switch (item.state) {
				case HELP_LOADED:
					item.help.lock();
					return item.help;
				case 0:
					final IREnvConfiguration rEnvConfig = rEnv.getConfig();
					if (rEnvConfig != null) {
						item.indexDir = rEnvConfig.getIndexDirectoryPath();
						item.help = fSaveUtil.load(rEnvConfig);
					}
					if (item.help != null) {
						item.state = HELP_LOADED;
						item.help.lock();
						return item.help;
					}
					else {
						item.state = HELP_MISSING;
						return null;
					}
				default:
					return null;
				}
			}
		}
		return null;
	}
	
	public Object getIndexLock(final IREnv rEnv) {
		final String id = rEnv.getId();
		EnvItem item;
		synchronized (fIndexLock) {
			item = fHelpIndexes.get(id);
			if (item == null) {
				item = new EnvItem(id);
				fHelpIndexes.put(id, item);
			}
		}
		return item.indexLock;
	}
	
	@Override
	public void dispose() {
		final SettingsChangeNotifier changeNotifier = PreferencesUtil.getSettingsChangeNotifier();
		if (changeNotifier != null) {
			changeNotifier.removeChangeListener(this);
		}
		stopServer();
	}
	
}
