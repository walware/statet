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
public abstract class GenericSourceUnitWorkingCopy implements ISourceUnit {
	
	
	protected final ISourceUnit fFrom;
	private IWorkingBuffer fBuffer;
	
	private int fCounter = 0;
	
	
	public GenericSourceUnitWorkingCopy(final ISourceUnit from) {
		fFrom = from;
	}
	
	public ISourceUnit getUnderlyingUnit() {
		return fFrom;
	}
	
	public ISourceUnit getSourceUnit() {
		return this;
	}
	public String getElementName() {
		return fFrom.getElementName();
	}
	public String getId() {
		return fFrom.getId();
	}
	public String getTypeId() {
		return fFrom.getTypeId();
	}
	public IPath getPath() {
		return fFrom.getPath();
	}
	public IResource getResource() {
		return fFrom.getResource();
	}
	
	public AbstractDocument getDocument() {
		return fBuffer.getDocument();
	}
	
	public SourceContent getContent() {
		return fBuffer.getContent();
	}
	
	public Object getAdapter(final Class adapter) {
		return fFrom.getAdapter(adapter);
	}
	
	public IModelElement getParent() {
		return null; // directory
	}
	
	public boolean hasChildren(final Object filter) {
		return false;
	}
	
	public IModelElement[] getChildren(final Object filter) {
		return new IModelElement[0];
	}
	
	public synchronized final void connect() {
		fCounter++;
		if (fCounter == 1) {
			if (fBuffer == null) {
				fBuffer = createWorkingBuffer();
			}
			register();
			fFrom.connect();
		}
	}
	
	protected abstract IWorkingBuffer createWorkingBuffer();
	protected abstract void register();
	
	public synchronized final void disconnect() {
		fCounter--;
		if (fCounter == 0) {
			fBuffer.releaseDocument();
			unregister();
			fFrom.disconnect();
		}
	}
	
	protected abstract void unregister();
	
}
