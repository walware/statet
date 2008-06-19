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

package de.walware.statet.ext.ui.dialogs;

import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.base.ui.sourceeditors.IEditorInstallable;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;


/**
 * Simple {@link IEditorAdapter} for snippet editors or previewers.
 */
public class ViewerEditorAdapter implements IEditorAdapter {
	
	
	private SourceViewer fSourceViewer;
	private SourceViewerConfigurator fConfigurator;
	
	
	/**
	 * Creates adapter for the viewer
	 * 
	 * @param viewer the viewer
	 * @param configurator a configurator used for {@link IEditorInstallable}, may be <code>null</code> (disables modules)
	 */
	public ViewerEditorAdapter(final SourceViewer viewer, final SourceViewerConfigurator configurator) {
		fSourceViewer = viewer;
		fConfigurator = configurator;
	}
	
	
	public SourceViewer getSourceViewer() {
		return fSourceViewer;
	}
	
	public IWorkbenchPart getWorkbenchPart() {
		return null;
	}
	
	public void install(final IEditorInstallable installable) {
		if (fConfigurator != null) {
			fConfigurator.installModul(installable);
		}
	}
	
	public boolean isEditable(final boolean validate) {
		return fSourceViewer.isEditable();
	}
	
	public Object getAdapter(final Class adapter) {
		return null;
	}
	
}
