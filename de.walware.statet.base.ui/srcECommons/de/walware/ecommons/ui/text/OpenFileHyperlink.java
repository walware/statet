/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.internal.ui.text.EditingMessages;
import de.walware.ecommons.ui.ECommonsUI;
import de.walware.ecommons.ui.util.UIAccess;


public class OpenFileHyperlink implements IHyperlink {
	
	
	private IRegion fRegion;
	private IFileStore fStore;
	
	
	public OpenFileHyperlink(final IRegion region, final IFileStore store) {
		fRegion = region;
		fStore = store;
	}
	
	
	public String getTypeLabel() {
		return null;
	}
	
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}
	
	public String getHyperlinkText() {
		return NLS.bind(EditingMessages.OpenFileHyperlink_label, fStore.toString());
	}
	
	public void open() {
		try {
			IDE.openEditorOnFileStore(UIAccess.getActiveWorkbenchPage(true), fStore);
		}
		catch (final PartInitException e) {
			Display.getCurrent().beep();
			StatusManager.getManager().handle(new Status(IStatus.INFO, ECommonsUI.PLUGIN_ID, -1,
					NLS.bind("An error occurred when opening file under hyperlink ''{0}''", fStore.toString()), e));
		}
	}
	
}
