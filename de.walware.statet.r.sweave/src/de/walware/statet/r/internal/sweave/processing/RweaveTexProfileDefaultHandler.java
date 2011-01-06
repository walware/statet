/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_PREVIEW;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_TEX;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_WEAVE;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.sweave.processing.SweaveProcessing.IProcessingListener;


/**
 * Handlers for Sweave (LaTeX/R) output creation toolchain running with the active profile.
 */
public abstract class RweaveTexProfileDefaultHandler extends AbstractHandler implements IElementUpdater, IProcessingListener {
	
	public static class ProcessWeave extends RweaveTexProfileDefaultHandler {
		
		public static final String COMMAND_ID = "de.walware.statet.sweave.commands.ProcessWeaveDefault"; //$NON-NLS-1$
		
		public ProcessWeave() {
			super(COMMAND_ID, STEP_WEAVE);
		}
		
	}
	
	public static class ProcessTex extends RweaveTexProfileDefaultHandler {
		
		public static final String COMMAND_ID = "de.walware.statet.sweave.commands.ProcessTexDefault"; //$NON-NLS-1$
		
		public ProcessTex() {
			super(COMMAND_ID, STEP_TEX);
		}
		
	}
	
	public static class ProcessDoc extends RweaveTexProfileDefaultHandler {
		
		public static final String COMMAND_ID = "de.walware.statet.doc.commands.ProcessDocDefault"; //$NON-NLS-1$
		
		public ProcessDoc() {
			super(COMMAND_ID, STEP_WEAVE | STEP_TEX);
		}
		
	}
	
	public static class ProcessAndPreview extends RweaveTexProfileDefaultHandler {
		
		public static final String COMMAND_ID = "de.walware.statet.doc.commands.ProcessAndPreviewDefault"; //$NON-NLS-1$
		
		public ProcessAndPreview() {
			super(COMMAND_ID, 0);
		}
		
	}
	
	
	public static class PreviewDoc extends RweaveTexProfileDefaultHandler {
		
		public static final String COMMAND_ID = "de.walware.statet.doc.commands.PreviewDocDefault"; //$NON-NLS-1$
		
		public PreviewDoc() {
			super(COMMAND_ID, STEP_PREVIEW);
		}
	
	}
	
	
	private String fCommandId;
	private int fLaunchFlags;
	private SweaveProcessing fSweaveManager;
	private String fTooltip;
	
	
	public RweaveTexProfileDefaultHandler(final String commandId, final int flags) {
		fCommandId = commandId;
		fLaunchFlags = flags;
		fSweaveManager = SweavePlugin.getDefault().getRweaveTexProcessingManager();
		fSweaveManager.addProcessingListener(this);
		activeProfileChanged(fSweaveManager.getActiveProfile());
	}
	
	@Override
	public void dispose() {
		if (fSweaveManager != null) {
			fSweaveManager.removeProcessingListener(this);
			fSweaveManager = null;
		}
	}
	
	public SweaveProcessing getSweaveManager() {
		return fSweaveManager;
	}
	
	protected String getLabel() {
		return fTooltip;
	}
	
	
	public void availableProfileChanged(final ILaunchConfiguration[] configs) {
	}
	
	public void activeProfileChanged(final ILaunchConfiguration config) {
		fTooltip = MessageUtil.escapeForTooltip(fSweaveManager.getLabelForLaunch(config, fLaunchFlags, true));
		final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(fCommandId, null);
	}
	
	
	public void updateElement(final UIElement element, final Map parameters) {
		element.setTooltip(fTooltip);
	}
	
	public Object execute(final ExecutionEvent arg0) throws ExecutionException {
		final ILaunchConfiguration config = fSweaveManager.getActiveProfile();
		if (config != null) {
			fSweaveManager.launch(config, fLaunchFlags);
		}
		else {
			final Runnable runnable = new Runnable() {
				public void run() {
					final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
					fSweaveManager.openConfigurationDialog(page.getWorkbenchWindow().getShell(), null);
				};
			};
			UIAccess.getDisplay().asyncExec(runnable);
		}
		return null;
	}
	
}
