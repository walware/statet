/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;

import de.walware.eclipsecommons.ltk.ast.IAstNode;


/**
 * 
 */
public interface ISourceUnit extends IModelElement, IAdaptable {
	
	
	public WorkingContext getWorkingContext();
	public ISourceUnit getUnderlyingUnit();
	
	public String getTypeId();
	public String getId();
	public IPath getPath();
	public IResource getResource();
	
	public AbstractDocument getDocument();
	public SourceContent getContent();
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException;
	
	public AstInfo<? extends IAstNode> getAstInfo(String type, boolean ensureSync, IProgressMonitor monitor);
	
	public IProblemRequestor getProblemRequestor();
	
	public ISourceUnitModelInfo getModelInfo(String type, int syncLevel, IProgressMonitor monitor);
	
	public void connect();
	public void disconnect();
	
}
