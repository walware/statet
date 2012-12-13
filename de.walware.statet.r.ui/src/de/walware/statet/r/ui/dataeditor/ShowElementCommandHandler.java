/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.dataeditor;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.rj.eclient.AbstractRToolCommandHandler;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.RUI;


public class ShowElementCommandHandler extends AbstractRToolCommandHandler {
	
	
	public static final String SHOW_ELEMENT_COMMAND_ID = "showElement"; //$NON-NLS-1$
	
	
	public ShowElementCommandHandler() {
	}
	
	
	@Override
	protected IStatus execute(final String id, final IRToolService r, final Map<String, Object> data,
			final IProgressMonitor monitor) throws CoreException {
		if (id.equals(SHOW_ELEMENT_COMMAND_ID)) {
			final ToolProcess tool = (ToolProcess) r.getTool();
			final String elementName = ToolCommandHandlerUtil.getCheckedData(data, "elementName", String.class, true); //$NON-NLS-1$
			final RElementName rElementName = RElementName.parseDefault(elementName);
			if (rElementName != null) {
				UIAccess.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
						if (page == null) {
							return;
						}
						try {
							IDE.openEditor(page, new RLiveDataEditorInput(tool, rElementName),
									"de.walware.statet.r.editors.RData", true); //$NON-NLS-1$
						}
						catch (final PartInitException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
									"Failed to open the selected element in the data viewer."));
						}
					}
				});
			}
		}
		return null;
	}
	
}
