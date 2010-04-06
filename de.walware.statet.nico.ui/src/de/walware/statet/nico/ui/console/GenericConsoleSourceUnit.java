package de.walware.statet.nico.ui.console;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.WorkingBuffer;
import de.walware.ecommons.ltk.WorkingContext;


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
			public int getType() {
				return 0x011; // see RElementName
			}
			public String getDisplayName() {
				return fId;
			}
			public String getSegmentName() {
				return fId;
			}
			public IElementName getNamespace() {
				return null;
			}
			public IElementName getNextSegment() {
				return null;
			}
		};
		fDocument = document;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public WorkingContext getWorkingContext() {
		return LTK.EDITOR_CONTEXT;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ISourceUnit getUnderlyingUnit() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getElementType() {
		return IModelElement.C2_SOURCE_CHUNK;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IElementName getElementName() {
		return fName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return fId;
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
	
	/**
	 * {@inheritDoc}
	 * 
	 * A console is always modifiable.
	 */
	public boolean checkState(final boolean validate, final IProgressMonitor monitor) {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IPath getPath() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * A console has no resource.
	 */
	public IResource getResource() {
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public AbstractDocument getDocument(final IProgressMonitor monitor) {
		return fDocument;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SourceContent getContent(final IProgressMonitor monitor) {
		return WorkingBuffer.createContentFromDocument(fDocument);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(final Class required) {
		return null;
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
	}
	
	/**
	 * {@inheritDoc}
	 */
	public synchronized final void disconnect(final IProgressMonitor monitor) {
		fCounter--;
	}
	
	
}
