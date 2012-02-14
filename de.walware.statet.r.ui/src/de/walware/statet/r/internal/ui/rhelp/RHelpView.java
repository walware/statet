/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ui.ISelectionWithElementInfoListener;
import de.walware.ecommons.ltk.ui.LTKInputData;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.SimpleContributionItem;
import de.walware.ecommons.ui.mpbv.BookmarkCollection;
import de.walware.ecommons.ui.mpbv.BrowserBookmark;
import de.walware.ecommons.ui.mpbv.BrowserSession;
import de.walware.ecommons.ui.mpbv.PageBookBrowserPage;
import de.walware.ecommons.ui.mpbv.PageBookBrowserView;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;


public class RHelpView extends PageBookBrowserView
		implements ISelectionWithElementInfoListener, IShowInTarget {
	
	
	public class RunCode extends AbstractHandler {
		
		private final boolean fGotoConsole;
		
		public RunCode(final boolean gotoConsole) {
			fGotoConsole = gotoConsole;
		}
		
		@Override
		public void setEnabled(final Object evaluationContext) {
			setBaseEnabled(getCurrentBrowserPage() != null);
		}
		
		@Override
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
	
	private class LinkEditorHandler extends SimpleContributionItem {
		
		public LinkEditorHandler() {
			super(SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SYNCHRONIZED_IMAGE_ID), null,
					"Link with Editor", null, STYLE_CHECK);
		}
		
		@Override
		protected void execute() throws ExecutionException {
			setLinkingWithEditor(!fIsLinkingWithEditor);
		}
		
	}
	
	
	private boolean fIsLinkingWithEditor;
	private final LinkEditorHandler fLinkingWithEditorHandler = new LinkEditorHandler();
	private SourceEditor1 fLinkedEditor;
	
	
	public RHelpView() {
		super();
	}
	
	
	@Override
	public void dispose() {
		if (fLinkedEditor != null) {
			fLinkedEditor.removePostSelectionWithElementInfoListener(this);
			fLinkedEditor = null;
		}
		
		super.dispose();
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
							@Override
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
		
		getSite().getPage().addPartListener(new IPartListener2() {
			@Override
			public void partOpened(final IWorkbenchPartReference partRef) {
			}
			@Override
			public void partClosed(final IWorkbenchPartReference partRef) {
				if (fLinkedEditor != null && partRef.getPart(false) == fLinkedEditor) {
					clear();
				}
			}
			@Override
			public void partVisible(final IWorkbenchPartReference partRef) {
			}
			@Override
			public void partHidden(final IWorkbenchPartReference partRef) {
			}
			@Override
			public void partInputChanged(final IWorkbenchPartReference partRef) {
			}
			@Override
			public void partActivated(final IWorkbenchPartReference partRef) {
				final IWorkbenchPart part = partRef.getPart(false);
				if (part instanceof SourceEditor1) {
					fLinkedEditor = (SourceEditor1) part;
					fLinkedEditor.addPostSelectionWithElementInfoListener(RHelpView.this);
				}
				else if (part instanceof IEditorPart) {
					clear();
				}
			}
			@Override
			public void partDeactivated(final IWorkbenchPartReference partRef) {
			}
			@Override
			public void partBroughtToTop(final IWorkbenchPartReference partRef) {
			}
			private void clear() {
				if (fLinkedEditor != null) {
					fLinkedEditor.removePostSelectionWithElementInfoListener(RHelpView.this);
					fLinkedEditor = null;
				}
			}
		});
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
		toolBarManager.add(fLinkingWithEditorHandler);
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
	
	
	public void setLinkingWithEditor(final boolean enable) {
		fIsLinkingWithEditor = enable;
		fLinkingWithEditorHandler.setChecked(enable);
		if (enable && fLinkedEditor != null) {
			final ISelection selection = fLinkedEditor.getShowInContext().getSelection();
			if (selection instanceof LTKInputData) {
				stateChanged((LTKInputData) selection);
			}
		}
	}
	
	@Override
	public void inputChanged() {
	}
	
	@Override
	public void stateChanged(final LTKInputData state) {
		if (!fIsLinkingWithEditor) {
			return;
		}
		show(state, false);
	}
	
	private boolean show(final LTKInputData state, final boolean explicite) {
		if (state.getInputElement().getModelTypeId() == RModel.TYPE_ID) {
			final AstSelection astSelection = state.getAstSelection();
			final ISelection selection = state.getSelection();
			if (astSelection != null && selection instanceof ITextSelection) {
				final ITextSelection textSelection = (ITextSelection) selection;
				if (!(astSelection.getCovering() instanceof RAstNode) || textSelection.getLength() > 0) {
					return false;
				}
				final RAstNode rNode = (RAstNode) astSelection.getCovering();
				RElementName name = null;
				if (!rNode.hasChildren()) {
					name = RHelpHover.searchName(rNode, rNode, false);
				}
				if (name == null) {
					name = RHelpHover.searchNameOfFunction(rNode,
							new Region(textSelection.getOffset(), textSelection.getLength()) );
				}
				if (name == null) {
					return false;
				}
				final IREnv rEnv = RCore.getREnvManager().getDefault();
				if (rEnv == null) {
					return false;
				}
				final IRHelpManager rHelpManager = RCore.getRHelpManager();
				final IREnvHelp help = rHelpManager.getHelp(rEnv);
				if (help != null) {
					final String url;
					try {
						if (name.getType() == RElementName.MAIN_PACKAGE) {
							final IRPackageHelp packageHelp = help.getRPackage(name.getSegmentName());
							if (packageHelp != null) {
								url = rHelpManager.getPackageHttpUrl(packageHelp, RHelpUIServlet.BROWSE_TARGET);
							}
							else {
								url = null;
							}
						}
						else {
							final List<IRHelpPage> topics = help.getPagesForTopic(name.getSegmentName());
							if (topics.size() == 1) {
								url = rHelpManager.getPageHttpUrl(topics.get(0), RHelpUIServlet.BROWSE_TARGET);
							}
							else if (topics.size() > 1) {
								url = rHelpManager.toHttpUrl("rhelp:///topic/"+name.getSegmentName(), rEnv,
										RHelpUIServlet.BROWSE_TARGET);
							}
							else {
								url = null;
							}
						}
					}
					finally {
						help.unlock();
					}
					if (url != null) {
						UIAccess.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								if ((explicite || fLinkedEditor != null
										&& fLinkedEditor == fLinkedEditor.getSite().getPage().getActiveEditor())
										&& getPageBook() != null && !getPageBook().isDisposed()) {
									final BrowserSession session = getCurrentSession();
									if (session == null || !url.equals(session.getUrl())) {
										openUrl(url, session);
									}
								}
							}
						});
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IShowInTarget.class.equals(required)) {
			return this;
		}
		return super.getAdapter(required);
	}
	
	@Override
	public boolean show(final ShowInContext context) {
		final ISelection selection = context.getSelection();
		if (selection instanceof LTKInputData) {
			return show((LTKInputData) selection, true);
		}
		return false;
	}
	
}
