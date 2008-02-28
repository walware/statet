/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IWorkbenchPart;


public interface IEditorAdapter extends IAdaptable {
	
	/**
	 * Allows access to the SourceViewer.
	 * 
	 * @return the source viewer of the editor.
	 */
	public SourceViewer getSourceViewer();
	
	public void install(IEditorInstallable installable);
	
	/**
	 * Shows the message to the status line, if possible.
	 * 
	 * @param message message to show.
	 */
	public void setStatusLineErrorMessage(String message);
	
	/**
	 * Returns the part the editor belongs to.
	 * 
	 * @return the part
	 */
	public IWorkbenchPart getWorkbenchPart();
	
	/**
	 * Returns whether the text in this text editor (SourceViewer) can be changed by the user.
	 *
	 * @return <code>true</code> if it can be edited, and <code>false</code> if it is read-only
	 */
	public boolean isEditable(boolean validate);
	
}
