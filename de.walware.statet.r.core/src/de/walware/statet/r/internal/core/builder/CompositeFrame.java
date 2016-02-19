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

package de.walware.statet.r.internal.core.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import de.walware.jcommons.lang.ObjectUtils;

import de.walware.ecommons.ltk.core.model.IModelElement.Filter;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class CompositeFrame implements IRFrame {
	
	
	private final int frameType;
	private final RElementName elementName;
	
	public final Map<String, RUnitElement> modelElements;
	private final Lock lock;
	
	
	public CompositeFrame(final ReadWriteLock lock,
			final String packageName, final String projectName,
			final Map<String, RUnitElement> elements) {
		this.lock= lock.readLock();
		this.modelElements= (elements != null) ? elements : new HashMap<String, RUnitElement>();
		
		if (packageName != null) {
			this.frameType= PACKAGE;
			this.elementName= RElementName.create(RElementName.SCOPE_PACKAGE, packageName);
		}
		else {
			this.frameType= PROJECT;
			this.elementName= RElementName.create(RElementName.SCOPE_PROJECT, projectName);
		}
	}
	
	public CompositeFrame(final ReadWriteLock lock,
			final String packageName, final String projectName,
			final CompositeFrame copyFrom) {
		this(lock, packageName, projectName, copyFrom.modelElements);
	}
	
	public CompositeFrame(final ReadWriteLock lock,
			final String packageName, final String projectName) {
		this(lock, packageName, projectName, (Map<String, RUnitElement>) null);
	}
	
	
	@Override
	public RElementName getElementName() {
		return this.elementName;
	}
	
	@Override
	public String getFrameId() {
		return null;
	}
	
	@Override
	public int getFrameType() {
		return this.frameType;
	}
	
	@Override
	public List<? extends IRElement> getModelElements() {
		this.lock.lock();
		try {
			final Collection<RUnitElement> values= this.modelElements.values();
			final List<IRElement> list= new ArrayList<>(values.size());
			list.addAll(values);
			return list;
		}
		finally {
			this.lock.unlock();
		}
	}
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		this.lock.lock();
		try {
			if (this.modelElements.isEmpty()) {
				return false;
			}
			for (final IRElement element : this.modelElements.values()) {
				if (element.hasModelChildren(filter)) {
					return true;
				}
			}
			return false;
		}
		finally {
			this.lock.unlock();
		}
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		this.lock.lock();
		try {
			if (this.modelElements.isEmpty()) {
				return Collections.EMPTY_LIST;
			}
			final ArrayList<IRLangElement> children= new ArrayList<>();
			for (final IRLangElement element : this.modelElements.values()) {
				final List<? extends IRLangElement> elementChildren= element.getModelChildren(null);
				if (!elementChildren.isEmpty()) {
					children.ensureCapacity(children.size() + elementChildren.size());
					for (final IRLangElement child : elementChildren) {
						if (filter == null || filter.include(child)) {
							children.add(child);
						}
					}
				}
			}
			return children;
		}
		finally {
			this.lock.unlock();
		}
	}
	
	@Override
	public List<? extends IRFrame> getPotentialParents() {
		return Collections.EMPTY_LIST;
	}
	
	
	public RUnitElement setModelElement(final String suId, final RUnitElement element) {
		element.fEnvir= this;
		return this.modelElements.put(suId, element);
	}
	
	public RUnitElement removeModelElement(final String suId) {
		return this.modelElements.remove(suId);
	}
	
	public void removeModelElements(final String modelTypeId) {
		for (final Iterator<RUnitElement> iter= this.modelElements.values().iterator(); iter.hasNext(); ) {
			final RUnitElement unitElement= iter.next();
			if (unitElement.getModelTypeId() == modelTypeId) {
				iter.remove();
			}
		}
	}
	
	@Override
	public String toString() {
		final ObjectUtils.ToStringBuilder builder= new ObjectUtils.ToStringBuilder(
				"CompositeFrame", getClass() ); //$NON-NLS-1$
		builder.addProp("frameType", "0x%02X", this.frameType); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addProp("elementName", this.elementName); //$NON-NLS-1$
		return builder.build();
	}
	
}
