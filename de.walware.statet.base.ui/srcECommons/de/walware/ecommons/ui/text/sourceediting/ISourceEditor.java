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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.text.PartitioningConfiguration;


/**
 * A interface for source editors independent of IEditorPart
 */
public interface ISourceEditor extends IAdaptable {
	
	/**
	 * Returns the source unit of editor input, if exists
	 * 
	 * @return model element or <code>null</code>
	 */
	public ISourceUnit getSourceUnit();
	
	/**
	 * Returns the part the editor belongs to
	 * 
	 * @return the part or <code>null</code>, if not in part
	 */
	public IWorkbenchPart getWorkbenchPart();
	
	/**
	 * Returns the service locator for the editor
	 * 
	 * @return service locator responsible for editor
	 */
	public IServiceLocator getServiceLocator();
	
	/**
	 * Allows access to the SourceViewer
	 * 
	 * @return the source viewer of the editor.
	 */
	public SourceViewer getViewer();
	
	/**
	 * The partitioning the viewer is configured to.
	 * 
	 * @return the partitioning configuration
	 */
	public PartitioningConfiguration getPartitioning();
	
	/**
	 * Returns whether the text in this text editor (SourceViewer) can be changed by the user
	 * 
	 * @param validate causes final validation if editor input is editable
	 * @return <code>true</code> if it can be edited, and <code>false</code> if it is read-only
	 */
	public boolean isEditable(boolean validate);
	
	/**
	 * Selects and reveals the specified range in this text editor
	 *
	 * @param offset the offset of the selection
	 * @param length the length of the selection
	 */
	public void selectAndReveal(int offset, int length);
	
	public ITextEditToolSynchronizer getTextEditToolSynchronizer();
	
}
