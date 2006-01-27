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
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
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
import de.walware.statet.nico.runtime.ToolController;
import de.walware.statet.nico.runtime.ToolProcess;
import de.walware.statet.nico.runtime.ToolStreamProxy;
import de.walware.statet.nico.ui.NicoMessages;


/**
 * A console to interact with controller using command-line-based interface.
 */
public class NIConsole extends IOConsole {
	
	
	public static final String NICONSOLE_TYPE = "de.walware.statet.nico.console";
	

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
	
	
	private IOConsoleOutputStream fOutputCommands;
	private IOConsoleOutputStream fOutputResults;
	private IOConsoleOutputStream fOutputError;
	
	private ToolController fController;
	private ToolProcess fProcess;
	
	private IDebugEventSetListener fDebugListener;
	
	
	/**
	 * Constructs a new console.
	 * 
	 * @param name console name
	 */
	public NIConsole(ToolProcess process) {
		
		super("<>", NICONSOLE_TYPE, null, true);
		
		fProcess = process;
		fController = process.getController();
		
		setImageDescriptor(computeImageDescriptor());
		
		connectStreams(fController.getStreams());
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
	protected void init() {
		
		super.init();
		
		setName(computeName(NicoMessages.Status_Starting_description));
		
		fDebugListener = new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				
				EVENTS: for (DebugEvent event : events) {
					if (event.getSource() == fProcess) {
						switch (event.getKind()) {
						case DebugEvent.MODEL_SPECIFIC:
							switch (event.getDetail()) {
								case ToolProcess.STATUS_CALCULATE:
									runSetName(NicoMessages.Status_StartedCalculating_description);
									continue EVENTS;
								case ToolProcess.STATUS_IDLE:
									runSetName(NicoMessages.Status_StartedIdle_description);
									continue EVENTS;
								case ToolProcess.STATUS_QUEUE_PAUSE:
									runSetName(NicoMessages.Status_StartedPaused_description);
									continue EVENTS;
							}
							break;
						case DebugEvent.TERMINATE:
							runSetName(NicoMessages.Status_Terminated_description);
							continue EVENTS;
						}
					}
				}
			}
			
			private void runSetName(final String statusDescription) {
		            Runnable r = new Runnable() {
	                public void run() {
	                	setName(computeName(statusDescription));
	//                	ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(NIConsole.this);
	                	ConsolePlugin.getDefault().getConsoleManager().refresh(NIConsole.this);
	                }
	            };
	            StatetPlugin.getDisplay(null).asyncExec(r);
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(fDebugListener);

		JFaceResources.getFontRegistry().addListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JFaceResources.TEXT_FONT.equals(event.getProperty()) )
					setFont(null);
			};
		});
	}
	
	@Override
	protected void dispose() {
		
		super.dispose();
		
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin != null) {
			debugPlugin.removeDebugEventListener(fDebugListener);
		}
		fDebugListener = null;
	}
	
	@Override
    public IPageBookViewPage createPage(IConsoleView view) {
		
        return new NIConsolePage(this, view);
    }


	public ToolProcess getProcess() {
		
		return fProcess;
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

		String name = fProcess.getLabel();
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
	
	protected String computeName(String statusDescription) {
		
		StringBuilder name = new StringBuilder();
		name.append(fProcess.getLabel());
		name.append(" New Console <");
		name.append(statusDescription);
		name.append('>');
		
		return name.toString();
	}
	
    /**
     * Computes and returns the image descriptor for this console.
     * 
     * @return an image descriptor for this console or <code>null</code>
     */
    protected ImageDescriptor computeImageDescriptor() {
    	
        ILaunchConfiguration configuration = fProcess.getLaunch().getLaunchConfiguration();
        if (configuration != null) {
            ILaunchConfigurationType type;
            try {
                type = configuration.getType();
                return DebugUITools.getImageDescriptor(type.getIdentifier());
            } catch (CoreException e) {
                StatetPlugin.logUnexpectedError(e);
            }
        }
        return null;
    }

}
