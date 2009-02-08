/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.text.PartitioningConfiguration;
import de.walware.ecommons.ui.util.UIAccess;


/**
 * Simple {@link ISourceEditor} for snippet editors or previewers.
 */
public class ViewerSourceEditorAdapter implements ISourceEditor {
	
	
	private SourceViewer fSourceViewer;
	private SourceEditorViewerConfigurator fConfigurator;
	
	
	/**
	 * Creates adapter for the viewer
	 * 
	 * @param viewer the viewer
	 * @param configurator a configurator used for {@link ISourceEditorAddon}, may be <code>null</code> (disables modules)
	 */
	public ViewerSourceEditorAdapter(final SourceViewer viewer, final SourceEditorViewerConfigurator configurator) {
		fSourceViewer = viewer;
		fConfigurator = configurator;
	}
	
	
	public ISourceUnit getSourceUnit() {
		return null;
	}
	
	public IWorkbenchPart getWorkbenchPart() {
		return null;
	}
	
	public IServiceLocator getServiceLocator() {
		return null;
	}
	
	public SourceViewer getViewer() {
		return fSourceViewer;
	}
	
	public PartitioningConfiguration getPartitioning() {
		return fConfigurator.getPartitioning();
	}
	
	
	public ITextEditToolSynchronizer getTextEditToolSynchronizer() {
		return null;
	}
	
	public boolean isEditable(final boolean validate) {
		return fSourceViewer.isEditable();
	}
	
	public void selectAndReveal(final int offset, final int length) {
		if (UIAccess.isOkToUse(fSourceViewer)) {
			fSourceViewer.setSelectedRange(offset, length);
			fSourceViewer.revealRange(offset, length);
		}
	}
	
	
	public Object getAdapter(final Class adapter) {
		return null;
	}
	
}
