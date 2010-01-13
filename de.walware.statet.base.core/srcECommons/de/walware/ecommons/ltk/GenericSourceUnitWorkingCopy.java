/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.AbstractDocument;


/**
 * Generic source unit for working copies based on the same unit in the underlying context
 */
public abstract class GenericSourceUnitWorkingCopy implements ISourceUnit {
	
	
	protected final ISourceUnit fFrom;
	private IWorkingBuffer fBuffer;
	
	private int fCounter = 0;
	
	
	/**
	 * Creates new working copy of the source unit
	 * 
	 * @param from the underlying unit to create a working copy from
	 */
	public GenericSourceUnitWorkingCopy(final ISourceUnit from) {
		fFrom = from;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ISourceUnit getUnderlyingUnit() {
		return fFrom;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getModelTypeId() {
		return fFrom.getModelTypeId();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getElementType() {
		return fFrom.getElementType();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IElementName getElementName() {
		return fFrom.getElementName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return fFrom.getId();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean exists() {
		return fCounter > 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isReadOnly() {
		return false;
	}
	
	public boolean checkState(final boolean validate, final IProgressMonitor monitor) {
		return fBuffer.checkState(validate, monitor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IPath getPath() {
		return fFrom.getPath();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IResource getResource() {
		return fFrom.getResource();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public AbstractDocument getDocument(final IProgressMonitor monitor) {
		return fBuffer.getDocument(monitor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SourceContent getContent(final IProgressMonitor monitor) {
		return fBuffer.getContent(monitor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IModelElement getModelParent() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<? extends IModelElement> getModelChildren(final Filter filter) {
		return NO_CHILDREN;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public synchronized final void connect(final IProgressMonitor monitor) {
		fCounter++;
		if (fCounter == 1) {
			final SubMonitor progress = SubMonitor.convert(monitor, 1);
			if (fBuffer == null) {
				progress.setWorkRemaining(2);
				fBuffer = createWorkingBuffer(progress.newChild(1));
			}
			register();
			fFrom.connect(progress.newChild(1));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public synchronized final void disconnect(final IProgressMonitor monitor) {
		fCounter--;
		if (fCounter == 0) {
			final SubMonitor progress = SubMonitor.convert(monitor, 2);
			fBuffer.releaseDocument(progress.newChild(1));
			unregister();
			fFrom.disconnect(progress.newChild(1));
		}
	}
	
	
	protected abstract IWorkingBuffer createWorkingBuffer(SubMonitor progress);
	
	protected abstract void register();
	
	protected abstract void unregister();
	
	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(final Class adapter) {
		return fFrom.getAdapter(adapter);
	}
	
}
