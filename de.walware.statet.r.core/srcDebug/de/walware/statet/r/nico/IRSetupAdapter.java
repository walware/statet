/*******************************************************************************
 * Copyright (c) 2006-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.core.filesystem.IFileStore;


/**
 * 
 * Note: the methods sets the properties at java side. The caller is
 * responsible for the synchronization of the properties with R.
 */
public interface IRSetupAdapter {
	
	
	public void setDefaultPromptText(String text);
	public void setContinuePromptText(String text);
	
	public void setLineSeparator(String newSeparator);
	public void setWorkspaceDir(IFileStore directory);
	
}
