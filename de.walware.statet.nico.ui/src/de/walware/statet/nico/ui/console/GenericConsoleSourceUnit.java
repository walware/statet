/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.core.impl.WorkingBuffer;


/**
 * Generic source unit for console
 */
public abstract class GenericConsoleSourceUnit implements ISourceUnit {
	
	
	private final String fId;
	private final IElementName fName;
	
	protected final InputDocument fDocument;
	
	private int fCounter = 0;
	
	
	public GenericConsoleSourceUnit(final String id, final InputDocument document) {
		fId = id;
		fName = new IElementName() {
			@Override
			public int getType() {
				return 0x011; // see RElementName
			}
			@Override
			public String getDisplayName() {
				return fId;
			}
			@Override
			public String getSegmentName() {
				return fId;
			}
			@Override
			public IElementName getNextSegment() {
				return null;
			}
		};
		fDocument = document;
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
		return fName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return fId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists() {
		return fCounter > 0;
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
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractDocument getDocument(final IProgressMonitor monitor) {
		return fDocument;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getContentStamp(final IProgressMonitor monitor) {
		return fDocument.getModificationStamp();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SourceContent getContent(final IProgressMonitor monitor) {
		return WorkingBuffer.createContentFromDocument(fDocument);
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
		return NO_CHILDREN;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized final void connect(final IProgressMonitor monitor) {
		fCounter++;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized final void disconnect(final IProgressMonitor monitor) {
		fCounter--;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isConnected() {
		return (fCounter > 0);
	}
	
}
