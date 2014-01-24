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

package de.walware.statet.r.internal.ui.rhelp;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.breadcrumb.IBreadcrumb;
import de.walware.ecommons.ui.mpbv.BrowserSession;
import de.walware.ecommons.ui.mpbv.PageBookBrowserPage;
import de.walware.ecommons.ui.mpbv.PageBookBrowserView;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.ui.RUI;


public class RHelpViewPage extends PageBookBrowserPage {
	
	
	private IBreadcrumb fBreadcrumb;
	private Control fBreadcrumbControl;
	
	private Object fHelpObject;
	
	private final ILabelProvider fLabelProvider = new RHelpLabelProvider();
	
	
	public RHelpViewPage(final PageBookBrowserView view, final BrowserSession session) {
		super(view, session);
	}
	
	
	@Override
	public void setUrl(String url) {
		if (url != null && url.startsWith(IRHelpManager.PORTABLE_URL_PREFIX)) {
			try {
				URI uri = new URI(url);
				uri = RCore.getRHelpManager().toHttpUrl(uri);
				url = uri.toASCIIString();
			}
			catch (final URISyntaxException e) {
				// ?
			}
		}
		super.setUrl(url);
	}
	
	@Override
	protected Control createAddressBar(final Composite parent) {
		fBreadcrumb = new RHelpBreadcrumb(this);
		if (fBreadcrumb != null) {
			fBreadcrumbControl = fBreadcrumb.createContent(parent);
			updateBreadcrumbInput();
		}
		return fBreadcrumbControl;
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
		super.initActions(serviceLocator, handlers);
		final IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
		final IContextService contextService = (IContextService) serviceLocator.getService(IContextService.class);
		
		final IHandler2 breadcrumbHandler = new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) throws ExecutionException {
				if (fBreadcrumb != null) {
					fBreadcrumb.activate();
				}
				return null;
			}
		};
		handlers.add(IBreadcrumb.SHOW_BREADCRUMB_COMMAND_ID, breadcrumbHandler);
		handlerService.activateHandler(IBreadcrumb.SHOW_BREADCRUMB_COMMAND_ID, breadcrumbHandler);
		contextService.activateContext(IBreadcrumb.WITH_BREADCRUMB_CONTEXT_ID);
	}
	
	@Override
	public void changed(final LocationEvent event) {
		super.changed(event);
		if (event.top) {
			updateAddressBar();
		}
	}
	
	@Override
	public void changed(final TitleEvent event) {
		super.changed(event);
		if (fHelpObject == null) {
			updateBreadcrumbInput();
		}
	}
	
	private void updateAddressBar() {
		try {
			final String url = getCurrentUrl();
			final Object input = RCore.getRHelpManager().getContentOfUrl(url);
			fHelpObject = input;
			
			final Image image = fLabelProvider.getImage(input);
			if (image != null) {
				setIcon(ImageDescriptor.createFromImage(image));
			}
			updateBreadcrumbInput();
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
					"An error occurred when updating the R help view address bar/breadcrumbs.", e));
		}
	}
	
	/**
	 * Makes the breadcrumb visible.
	 */
	private void showBreadcrumb() {
		if (fBreadcrumb == null || fBreadcrumbControl.isVisible()) {
			return;
		}
//		((GridData) fBreadcrumbControl.getLayoutData()).exclude= false;
		fBreadcrumbControl.setVisible(true);
		fBreadcrumbControl.getParent().layout(true, true);
	}
	
	/**
	 * Hides the breadcrumb
	 */
	private void hideBreadcrumb() {
		if (fBreadcrumb == null || !fBreadcrumbControl.isVisible()) {
			return;
		}
//		((GridData) fBreadcrumbControl.getLayoutData()).exclude= true;
		fBreadcrumbControl.setVisible(false);
		fBreadcrumbControl.getParent().layout(true, true);
	}
	
	/**
	 * Sets the breadcrumb input to the given element.
	 * @param content the element to use as input for the breadcrumb
	 * @since 3.4
	 */
	private void updateBreadcrumbInput() {
		if (fBreadcrumb == null) {
			return;
		}
		Object input = fHelpObject;
		if (input == null) {
			if (getCurrentUrl().length() > 0) {
				input = getCurrentTitle();
			}
		}
		else if (input instanceof Object[]) {
			final Object[] array = (Object[]) input;
			if (array.length >= 2) {
				array[array.length-1] = getCurrentTitle();
			}
			else {
				input = null;
			}
		}
		fBreadcrumb.setInput(input);
	}
	
}
