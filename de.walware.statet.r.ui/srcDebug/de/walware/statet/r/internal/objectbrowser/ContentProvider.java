/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.objectbrowser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.models.core.util.ElementPartitionFactory;
import de.walware.ecommons.models.core.util.ElementProxy;
import de.walware.ecommons.models.core.util.IElementPartition;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;

import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.console.core.RWorkspace.ICombinedRList;
import de.walware.statet.r.core.data.ICombinedRElement;


class ContentProvider implements ITreeContentProvider {
	
	
	static ICombinedRElement getCombinedRElement(final Object object) {
		if (object instanceof ICombinedRElement) {
			return (ICombinedRElement) object;
		}
		if (object instanceof IAdaptable) {
			final IModelElement modelElement = (IModelElement) ((IAdaptable) object).getAdapter(IModelElement.class);
			if (modelElement instanceof ICombinedRElement) {
				return (ICombinedRElement) modelElement;
			}
		}
		return null;
	}
	
	private static final Object[] NO_CHILDREN = new Object[0];
	
	static class ElementPartition extends ElementProxy implements IElementPartition {
		
		private final PartitionFactory.PartitionHandle partition;
		
		public ElementPartition(final ICombinedRElement value, final PartitionFactory.PartitionHandle partition) {
			super(value);
			this.partition = partition;
		}
		
		
		@Override
		public long getPartitionStart() {
			return this.partition.getStart();
		}
		
		@Override
		public long getPartitionLength() {
			return this.partition.getLength();
		}
		
		
		@Override
		public int hashCode() {
			return super.hashCode() ^ this.partition.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ElementPartition)) {
				return false;
			}
			return (super.equals(obj)
					&& this.partition.equals(((ElementPartition) obj).partition) );
		}
		
	}
	
	
	private class PartitionFactory extends ElementPartitionFactory<Object, ICombinedRList> {
		
		public PartitionFactory() {
			super(Object.class, DEFAULT_PART_SIZE);
		}
		
		@Override
		protected ElementPartition createPartition(final ICombinedRList value, final PartitionHandle partition) {
			return new ElementPartition(value, partition);
		}
		
		@Override
		protected Object[] getChildren(final ICombinedRList value, final long start, final int length) {
			if (ContentProvider.this.activeInput.hasEnvFilter()
					&& value instanceof ICombinedREnvironment) {
				final Object[] all = ContentProvider.this.activeInput.getEnvFilterChildren(value);
				if (start == 0 && length == all.length) {
					return all;
				}
				final Object[] children = new Object[length];
				System.arraycopy(all, (int) start, children, 0, length);
				return children;
			}
			
			if (start == 0 && length == value.getLength()) {
				return value.getModelChildren(null).toArray();
			}
			final Object[] children = new Object[length];
			for (int i = 0; i < length; i++) {
				children[i] = value.get(start + i);
			}
			return children;
		}
		
	}
	
	
	private final PartitionFactory partitionFactory = new PartitionFactory();
	
	private ContentInput activeInput;
	
	/** References used by the viewer. Use only in UI thread */
	private Set<RReference> usedReferences = new HashSet<RReference>();
	
	
	public ContentProvider() {
	}
	
	
	@Override
	public void dispose() {
	}
	
	public void setInput(final ContentInput input) {
		this.activeInput = input;
	}
	
	public ContentInput getInput() {
		return this.activeInput;
	}
	
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}
	
	@Override
	public Object[] getElements(final Object inputElement) {
		if (this.activeInput != null && this.activeInput.rootElements != null) {
			return this.activeInput.rootElements;
		}
		return NO_CHILDREN;
	}
	
	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof ElementPartition) {
			return true;
		}
		
//		if (element instanceof TreeElement) {
//			final TreeElement treeElement = (TreeElement) element;
//			if (treeElement.children != null) {
//				return (treeElement.children.length > 0);
//			}
//			return hasChildren(treeElement, getCombinedRElement(element));
//		}
		
		return hasChildren((ICombinedRElement) element);
	}
	
	@Override
	public Object[] getChildren(final Object element) {
		if (element instanceof ElementPartition) {
			return ((ElementPartition) element).partition.getElements(
					(ICombinedRList) getCombinedRElement(element) );
		}
		
		return getChildren((ICombinedRElement) element);
	}
	
	private boolean hasChildren(final ICombinedRElement rElement) {
		switch (rElement.getRObjectType()) {
		case RObject.TYPE_DATAFRAME:
		case RObject.TYPE_LIST:
			return (rElement.getLength() > 0);
		case RObject.TYPE_ENV:
			if (this.activeInput.hasEnvFilter()) {
				return (this.activeInput.getEnvFilterChildren(rElement).length > 0);
			}
			return (rElement.getLength() > 0);
		case RObject.TYPE_REFERENCE: {
			final RObject realObject = ((RReference) rElement).getResolvedRObject();
			if (realObject != null) {
				this.usedReferences.add((RReference) rElement);
				return hasChildren((ICombinedRElement) realObject);
			}
			return false; }
		case RObject.TYPE_S4OBJECT:
			return rElement.hasModelChildren(this.activeInput.otherFilter);
		default:
			return false;
		}
	}
	
	private Object[] getChildren(final ICombinedRElement rElement) {
		switch (rElement.getRObjectType()) {
		case RObject.TYPE_DATAFRAME:
			return rElement.getModelChildren(null).toArray();
		case RObject.TYPE_LIST:
			if (rElement.hasModelChildren(null)) {
				return this.partitionFactory.getElements((ICombinedRList) rElement, rElement.getLength());
			}
			return NO_CHILDREN;
		case RObject.TYPE_ENV:
			if (this.activeInput.hasEnvFilter()) {
				final Object[] children = this.activeInput.getEnvFilterChildren(rElement);
				if (children.length > 5000) {
					return this.partitionFactory.getElements((ICombinedRList) rElement, children.length);
				}
				return children;
			}
			if (rElement.getLength() > 5000 && rElement.hasModelChildren(null)) {
				return this.partitionFactory.getElements((ICombinedRList) rElement, rElement.getLength());
			}
			return rElement.getModelChildren(null).toArray();
		case RObject.TYPE_REFERENCE: {
			final RObject realObject = ((RReference) rElement).getResolvedRObject();
			if (realObject != null) {
				return getChildren((ICombinedRElement) realObject);
			}
			return NO_CHILDREN; }
		case RObject.TYPE_S4OBJECT:
			return rElement.getModelChildren(this.activeInput.otherFilter).toArray();
		default:
			return NO_CHILDREN;
		}
	}
	
	@Override
	public Object getParent(final Object element) {
		return null;
	}
	
	
	public Set<RReference> resetUsedReferences() {
		if (this.usedReferences.isEmpty()) {
			return Collections.emptySet();
		}
		final Set<RReference> previousReferences = this.usedReferences;
		this.usedReferences = new HashSet<RReference>();
		return previousReferences;
	}
	
	public Set<RReference> getUsedReferences() {
		return this.usedReferences;
	}
	
}
