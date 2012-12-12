/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBookView;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ts.ui.IToolRunnableDecorator;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsolePage;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.internal.console.ui.RConsoleMessages;


public class AdjustWidthHandler extends AbstractHandler {
	
	
	private static class AdjustWidthRunnable implements IToolRunnable, IToolRunnableDecorator {
		
		
		private final int fWidth;
		
		
		public AdjustWidthRunnable(final int width) {
			fWidth = width;
		}
		
		
		@Override
		public String getTypeId() {
			return "r/console/width"; //$NON-NLS-1$
		}
		
		@Override
		public Image getImage() {
			return ConsolePlugin.getImage(IConsoleConstants.IMG_VIEW_CONSOLE);
		}
		
		@Override
		public String getLabel() {
			return RConsoleMessages.AdjustWidth_task;
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool.isProvidingFeatureSet(RConsoleTool.R_BASIC_FEATURESET_ID));
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final IRBasicAdapter r = (IRBasicAdapter) service;
			r.submitToConsole("options(width = "+fWidth+"L)", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	}
	
	
	public AdjustWidthHandler() {
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		setBaseEnabled((evaluationContext instanceof IEvaluationContext)
				&& isValid((ToolProcess) ((IEvaluationContext) evaluationContext)
						.getVariable(NicoUI.TOOL_SOURCE_ID) ));
	}
	
	private boolean isValid(final ToolProcess process) {
		return (process != null && process.getMainType() == RConsoleTool.TYPE
				&& !process.isTerminated() );
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ToolSessionUIData session = NicoUI.getToolRegistry().getActiveToolSession(
				UIAccess.getActiveWorkbenchPage(true) );
		if (!isValid(session.getProcess())) {
			return null;
		}
		final NIConsole console = session.getConsole();
		if (console == null) {
			return null;
		}
		final IConsoleView consoleView = NicoUITools.getConsoleView(console, session.getPage());
		if (consoleView == null || !(consoleView instanceof PageBookView)) {
			return null;
		}
		consoleView.display(console);
		final IPage page = ((PageBookView) consoleView).getCurrentPage();
		if (page instanceof NIConsolePage && ((NIConsolePage) page).getConsole() == console) {
			int width = getWidth(((NIConsolePage) page).getOutputViewer());
			if (width >= 0) {
				if (width < 10) {
					width = 10;
				}
				console.getProcess().getQueue().add(new AdjustWidthRunnable(width));
			}
		}
		return null;
	}
	
	private int getWidth(final TextConsoleViewer viewer) {
		if (UIAccess.isOkToUse(viewer)) {
			final GC gc = new GC(Display.getCurrent());
			try {
				gc.setFont(viewer.getTextWidget().getFont());
				final FontMetrics fontMetrics = gc.getFontMetrics();
				final int charWidth = fontMetrics.getAverageCharWidth();
				final int clientWidth = viewer.getTextWidget().getClientArea().width;
				return clientWidth/charWidth;
			}
			finally {
				gc.dispose();
			}
		}
		return -1;
	}
	
}
