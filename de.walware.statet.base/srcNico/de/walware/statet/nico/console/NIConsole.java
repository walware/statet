/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPageBookViewPage;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.nico.Messages;
import de.walware.statet.nico.runtime.IStatusListener;
import de.walware.statet.nico.runtime.ToolController;
import de.walware.statet.nico.runtime.ToolStreamProxy;
import de.walware.statet.nico.runtime.ToolController.ToolStatus;


/**
 * A console to interact with controller using command-line-based interface.
 */
public class NIConsole extends IOConsole {
	
	
	public static final String NICONSOLE_TYPE = "de.walware.statet.nico.console";
	
	
	private IOConsoleOutputStream fOutputCommands;
	private IOConsoleOutputStream fOutputResults;
	private IOConsoleOutputStream fOutputError;
	
	private ToolController fController;
	
	
	/**
	 * Constructs a new console.
	 * 
	 * @param name console name
	 * @param autoLifecycle whether lifecycle methods should be called automatically
	 *  when added and removed from the console manager
	 */
	public NIConsole(ToolController controller, boolean autoLifecycle) {
		
		super(computeName(controller, ToolStatus.STARTING), 
				NICONSOLE_TYPE, null, 
				autoLifecycle);
		
		fController = controller;
		
		connectStreams(fController.getStreams());
		fController.addStatusListener(new IStatusListener() {
			
			public void statusChanged(ToolStatus oldStatus, ToolStatus newStatus) {
				
				final ToolStatus status = newStatus;
	            Runnable r = new Runnable() {
	                public void run() {
	                	setName(computeName(fController, status));
//	                	ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(NIConsole.this);
	                	ConsolePlugin.getDefault().getConsoleManager().refresh(NIConsole.this);
	                }
	            };
	            StatetPlugin.getDisplay(null).asyncExec(r);
			}
		});
	}
	
	protected void connectStreams(ToolStreamProxy streams) {
		// Connect input, output, error stream
		
		fOutputCommands = newOutputStream();
		streams.getInputStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
			    try {
					fOutputCommands.write("\n> " + text + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		fOutputResults = newOutputStream();
		streams.getOutputStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
			    try {
					fOutputResults.write(text);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		fOutputError = newOutputStream();
		streams.getErrorStreamMonitor().addListener(new IStreamListener() {
			public void streamAppended(String text, IStreamMonitor monitor) {
			    try {
					fOutputError.write(text);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
    public IPageBookViewPage createPage(IConsoleView view) {
		
		JFaceResources.getFontRegistry().addListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JFaceResources.TEXT_FONT.equals(event.getProperty()) )
					setFont(null);
			};
		});
		
        return new NIConsolePage(this, view);
    }

	
	public ToolController getController() {
		
		return fController;
	}
	
	/**
	 * More intelligent and flexible method to show this console.
	 * 
	 * @param activate <code>true</code> to activate the console, 
	 * 		or <code>false</code> to show the console without changing the focus.
	 * 
	 * @throws CoreException
	 */
	public void show(boolean activate) throws CoreException {
		
		IWorkbenchPage page = StatetPlugin.getActivePage();
		if (page == null) {
			return;
		}

		String name = fController.getName();
		IConsoleView choosenView = null;
		
		List<IConsoleView> consoleViews = getConsoleViews(page);
		IConsoleView fullMatchView = null;
		for (IConsoleView view : consoleViews) {
			if (name.equals(view.getViewSite().getSecondaryId())) {
				fullMatchView = view;
				break;
			}
		}
		
		// Does a console view already show the console
		if (fullMatchView != null && fullMatchView.getConsole() == this) {
			choosenView = fullMatchView;
		}
		else {
			for (IConsoleView view : consoleViews) {
				if (view.getConsole() == this) {
					choosenView = view;
					break;
				}
			}
		}
		if (choosenView != null) {
			if (activate) {
				page.activate(choosenView);
			}
			else {
				page.bringToTop(choosenView);
			}
			return;
		}
		
		// Search a view, which is not pinned
		if (fullMatchView != null && !fullMatchView.isPinned()) {
			choosenView = fullMatchView;
		}
		else {
			for (IConsoleView view : consoleViews) {
				if (!view.isPinned()) {
					choosenView = view;
					break;
				}
			}
		}
		if (choosenView != null) {
			choosenView.display(this);
			if (activate) {
				page.activate(choosenView);
			}
			else {
				page.bringToTop(choosenView);
			}
			return;
		}
		
		// Create a new console view
		page.showView(
				IConsoleConstants.ID_CONSOLE_VIEW, name, 
				activate ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_VISIBLE);
	}
	
	public static List<IConsoleView> getConsoleViews(IWorkbenchPage page) {
		
		List<IConsoleView> consoleViews = new ArrayList<IConsoleView>();
		
		IViewReference[] allReferences = page.getViewReferences();
		for (IViewReference reference : allReferences) {
			if (reference.getId().equals(IConsoleConstants.ID_CONSOLE_VIEW)) {
				IViewPart view = reference.getView(false);
				if (view != null) {
					consoleViews.add((IConsoleView) view);
				}
			}
		}
		return consoleViews;
	}
	
	protected static String computeName(ToolController controller, ToolStatus status) {
		
		StringBuilder name = new StringBuilder();
		name.append(controller.getName());
		name.append(" New Console <");
		name.append(Messages.getDefaultStatusDescription(status));
		name.append('>');
		
		return name.toString();
	}
}
