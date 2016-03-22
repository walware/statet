/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.objectbrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import de.walware.jcommons.collections.CollectionUtils;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.models.core.util.IElementPartition;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.DNDUtil;
import de.walware.ecommons.ui.util.UIAccess;


class CopyElementNameHandler extends AbstractHandler {
	
	
	private final ObjectBrowserView view;
	
	
	CopyElementNameHandler(final ObjectBrowserView objectBrowserView) {
		this.view = objectBrowserView;
	}
	
	
	private boolean isValidSelection(final ITreeSelection selection) {
		if (selection == null || selection.isEmpty()) {
			return false;
		}
		for (final Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
			final Object element = iter.next();
			if (element instanceof IElementPartition) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		setBaseEnabled(isValidSelection(this.view.getSelection()));
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (!UIAccess.isOkToUse(this.view.getViewer())) {
			return null;
		}
		final ITreeSelection selection = this.view.getSelection();
		if (!isValidSelection(selection)) {
			return null;
		}
		final TreePath[] treePaths = selection.getPaths();
		if (treePaths.length == 0) {
			return null;
		}
		final List<String> names= new ArrayList<>();
		int failed = 0;
		for (int i = 0; i < treePaths.length; i++) {
			final IElementName elementName = this.view.getFQElementName(treePaths[i]);
			final String name = (elementName != null) ? elementName.getDisplayName() : null;
			if (name != null) {
				names.add(name);
			}
			else {
				failed++;
			}
		}
		
		if (names.size() == 1) {
			copy(names.get(0));
		}
		else if (names.size() > 1) {
			copy(CollectionUtils.toString(names, ", ")); //$NON-NLS-1$
		}
		else {
			copy(""); //$NON-NLS-1$
		}
		if (failed > 0) {
			this.view.getStatusLine().setMessage(new StatusInfo(IStatus.WARNING, "Could not copy element name for " + failed + " of " + treePaths.length + " objects."));
		}
		
		return null;
	}
	
	private void copy(final String text) {
		DNDUtil.setContent(this.view.getClipboard(), 
				new String[] { text }, 
				new Transfer[] { TextTransfer.getInstance() } );
	}
	
}
