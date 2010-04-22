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

package de.walware.statet.r.internal.ui.rhelp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.mpbv.BookmarkCollection;
import de.walware.ecommons.ui.mpbv.BrowserBookmark;
import de.walware.ecommons.ui.mpbv.BrowserSession;
import de.walware.ecommons.ui.mpbv.PageBookBrowserPage;
import de.walware.ecommons.ui.mpbv.PageBookBrowserView;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;


public class RHelpView extends PageBookBrowserView {
	
	
	public class RunCode extends AbstractHandler {
		
		private final boolean fGotoConsole;
		
		public RunCode(final boolean gotoConsole) {
			fGotoConsole = gotoConsole;
		}
		
		@Override
		public void setEnabled(final Object evaluationContext) {
			setBaseEnabled(getCurrentBrowserPage() != null);
		}
		
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			final PageBookBrowserPage browserPage = getCurrentBrowserPage();
			if (browserPage != null) {
				final String selection = browserPage.getSelection();
				if (selection != null && selection.length() > 0) {
					try {
						RCodeLaunching.runRCodeDirect(selection, fGotoConsole);
					}
					catch (final CoreException e) {
						final IStatus causeStatus = e.getStatus();
						final Status status = new Status(causeStatus.getSeverity(), RUI.PLUGIN_ID,
								ICommonStatusConstants.LAUNCHING, RLaunchingMessages.RSelectionLaunch_error_message, e);
						StatusManager.getManager().handle(status);
						final IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
						if (manager != null) {
							if (causeStatus.getSeverity() == IStatus.ERROR) {
								manager.setErrorMessage(causeStatus.getMessage());
							}
							else {
								manager.setMessage(causeStatus.getMessage());
							}
						}
					}
				}
			}
			return null;
		}
		
	}
	
	
	public RHelpView() {
		super();
	}
	
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		
		if (!PlatformUI.getWorkbench().isStarting()) {
			final Job job = new Job("Initial R Help Page") { //$NON-NLS-1$
				{	setSystem(true);
					setUser(false);
					setPriority(Job.SHORT);
				}
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					RCore.getRHelpManager().ensureIsRunning();
					final Display display = UIAccess.getDisplay();
					if (getCurrentBrowserPage() == null
							&& display != null && !display.isDisposed()) {
						display.asyncExec(new Runnable() {
							public void run() {
								if (getCurrentBrowserPage() == null
										&& UIAccess.isOkToUse(getPageBook())) {
									newPage(null, true);
								}
							}
						});
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule(50);
		}
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
		super.initActions(serviceLocator, handlers);
		final IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
		
		{	final IHandler2 handler = new RunCode(false);
			handlers.add(RCodeLaunching.RUN_SELECTION_COMMAND_ID, handler);
			handlerService.activateHandler(RCodeLaunching.RUN_SELECTION_COMMAND_ID, handler);
		}
		{	final IHandler2 handler = new RunCode(true);
			handlers.add(RCodeLaunching.RUN_FILEVIACOMMAND_GOTOCONSOLE_COMMAND_ID, handler);
			handlerService.activateHandler(RCodeLaunching.RUN_FILEVIACOMMAND_GOTOCONSOLE_COMMAND_ID, handler);
		}
	}
	
	@Override
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final IActionBars actionBars, final HandlerCollection handlers) {
		super.contributeToActionBars(serviceLocator, actionBars, handlers);
		
		final IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.appendToGroup("bookmarks", //$NON-NLS-1$
				new CommandContributionItem(new CommandContributionItemParameter(
				serviceLocator, null, "de.walware.ecommons.ide.commands.OpenSearchDialog", //$NON-NLS-1$
				Collections.singletonMap("pageId", "de.walware.statet.r.searchPages.RHelpPage"), //$NON-NLS-1$ //$NON-NLS-2$ 
				null, null, null,
				null, null, null,
				CommandContributionItem.STYLE_PUSH, null, false)));
	}
	
	@Override
	protected PageBookBrowserPage doCreatePage(final BrowserSession session) {
		return new RHelpViewPage(this, session);
	}
	
	@Override
	protected void updateTitle() {
		final BrowserSession session = getCurrentSession();
		if (session == null) {
			setContentDescription(getNoPageTitle());
		}
		else {
			setContentDescription(""); //$NON-NLS-1$
		}
	}
	
	
	@Override
	protected BookmarkCollection initBookmarkCollection() {
		final BookmarkCollection collection = BookmarkCollection.getCollection(RHelpPreferences.RHELP_QUALIFIER);
		final List<BrowserBookmark> bookmarks = collection.getBookmarks();
		synchronized (collection) {
			if (bookmarks.isEmpty()) {
				bookmarks.add(new BrowserBookmark("R Homepage - The R Project for Statistical Computing", "http://www.r-project.org")); //$NON-NLS-1$ //$NON-NLS-2$
				bookmarks.add(new BrowserBookmark("CRAN - The Comprehensive R Archive Network", "http://cran.r-project.org/")); //$NON-NLS-1$ //$NON-NLS-2$
				bookmarks.add(new BrowserBookmark("RSeek.org - R-project Search Engine", "http://rseek.org/")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return collection;
	}
	
	@Override
	protected BrowserBookmark createBookmark() {
		final PageBookBrowserPage page = getCurrentBrowserPage();
		if (page != null) {
			String url = page.getCurrentUrl();
			try {
				URI uri = new URI(url);
				uri = RCore.getRHelpManager().toPortableUrl(uri);
				url = uri.toString();
			}
			catch (final URISyntaxException e) {
				// ?
			}
			return new BrowserBookmark(page.getCurrentTitle(), url);
		}
		return null;
	}
	
	@Override
	public String getHomePageUrl() {
		return PreferencesUtil.getInstancePrefs().getPreferenceValue(RHelpPreferences.HOMEPAGE_URL_PREF);
	}
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		pageIds.add("de.walware.statet.r.preferencePages.RHelpPage"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REnvironmentPage"); //$NON-NLS-1$
	}
	
}
