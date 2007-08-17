/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.AbstractDocument;


/**
 *
 */
public interface ISourceUnit extends IModelElement {
	
	public ISourceUnit getWorkingCopy(WorkingContext context, boolean create);
	public WorkingContext getWorkingContext();
	public ISourceUnit getUnderlyingUnit();
	
	public String getId();
	public IPath getPath();
	public IResource getResource();
	
	public AbstractDocument getDocument();
	public SourceContent getContent();

	public void connect();
	public void disconnect();
	
}
