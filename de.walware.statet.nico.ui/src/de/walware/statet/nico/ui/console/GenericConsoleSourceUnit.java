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

package de.walware.statet.nico.ui.console;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.impl.WorkingBuffer;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.text.core.util.AbstractFragmentDocument;


/**
 * Generic source unit for console
 */
public abstract class GenericConsoleSourceUnit implements ISourceUnit {
	
	
	private final String id;
	private final IElementName name;
	
	private final AbstractFragmentDocument document;
	
	private int counter= 0;
	
	
	public GenericConsoleSourceUnit(final String id, final AbstractFragmentDocument document) {
		this.id= id;
		this.name= new IElementName() {
			@Override
			public int getType() {
				return 0x011; // see RElementName
			}
			@Override
			public String getDisplayName() {
				return GenericConsoleSourceUnit.this.id;
			}
			@Override
			public String getSegmentName() {
				return GenericConsoleSourceUnit.this.id;
			}
			@Override
			public IElementName getNextSegment() {
				return null;
			}
		};
		this.document= document;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkingContext getWorkingContext() {
		return LTK.EDITOR_CONTEXT;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISourceUnit getUnderlyingUnit() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSynchronized() {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getElementType() {
		return IModelElement.C2_SOURCE_CHUNK;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IElementName getElementName() {
		return this.name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return this.id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists() {
		return this.counter > 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly() {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * A console is always modifiable.
	 */
	@Override
	public boolean checkState(final boolean validate, final IProgressMonitor monitor) {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * A console has no resource.
	 */
	@Override
	public Object getResource() {
		return null;
	}
	
	
	protected final AbstractFragmentDocument getDocument() {
		return this.document;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractDocument getDocument(final IProgressMonitor monitor) {
		return this.document;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getContentStamp(final IProgressMonitor monitor) {
		return this.document.getModificationStamp();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SourceContent getContent(final IProgressMonitor monitor) {
		return WorkingBuffer.createContentFromDocument(this.document);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAdapter(final Class required) {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IModelElement getModelParent() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<? extends IModelElement> getModelChildren(final Filter filter) {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized final void connect(final IProgressMonitor monitor) {
		this.counter++;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized final void disconnect(final IProgressMonitor monitor) {
		this.counter--;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isConnected() {
		return (this.counter > 0);
	}
	
	
	@Override
	public String toString() {
		return getModelTypeId() + '/' + getWorkingContext() + ": " + getId(); //$NON-NLS-1$
	}
	
}
