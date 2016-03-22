/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.dataeditor;

import de.walware.jcommons.collections.CopyOnWriteIdentityListSet;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.util.ToolTerminateListener;

import de.walware.rj.services.IFQRObjectRef;

import de.walware.statet.r.core.model.RElementName;


public class RToolDataTableInput implements IRDataTableInput {
	
	
	private final RElementName elementName;
	
	private final String fullName;
	private final String shortName;
	
	private final IFQRObjectRef elementRef;
	
	private final ITool tool;
	private ToolTerminateListener processListener;
	
	private final CopyOnWriteIdentityListSet<IRDataTableInput.StateListener> listeners= new CopyOnWriteIdentityListSet<>();
	
	
	public RToolDataTableInput(RElementName elementName, final IFQRObjectRef elementRef) {
		if (elementName == null) {
			throw new NullPointerException("name"); //$NON-NLS-1$
		}
		if (elementRef == null) {
			throw new NullPointerException("elementRef"); //$NON-NLS-1$
		}
		if (!(elementRef.getRHandle() instanceof ITool)) {
			throw new IllegalArgumentException("Unsupported elementRef.rHandle"); //$NON-NLS-1$
		}
		
		this.elementName= elementName;
		this.elementRef= elementRef;
		this.fullName= RElementName.createDisplayName(elementName, RElementName.DISPLAY_FQN | RElementName.DISPLAY_EXACT);
		
		RElementName name= elementName;
		while (elementName.getNextSegment() != null) {
			if (elementName.getType() == RElementName.MAIN_DEFAULT) {
				name= elementName;
			}
			
			elementName= elementName.getNextSegment();
		}
		this.shortName= name.getDisplayName();
		
		this.tool= (ITool) elementRef.getRHandle();
	}
	
	
	@Override
	public RElementName getElementName() {
		return this.elementName;
	}
	
	@Override
	public String getFullName() {
		return this.fullName;
	}
	
	@Override
	public String getName() {
		return this.shortName;
	}
	
	@Override
	public IFQRObjectRef getElementRef() {
		return this.elementRef;
	}
	
	@Override
	public ITool getTool() {
		return this.tool;
	}
	
	@Override
	public boolean isAvailable() {
		return !this.tool.isTerminated();
	}
	
	@Override
	public void addStateListener(final StateListener listener) {
		synchronized (this.listeners) {
			if (this.listeners.add(listener)
					&& this.processListener == null) {
				this.processListener= new ToolTerminateListener(this.tool) {
					@Override
					public void toolTerminated() {
						final ImList<StateListener> listeners;
						synchronized (RToolDataTableInput.this.listeners) {
							dispose();
							processListener= null;
							
							listeners= RToolDataTableInput.this.listeners.toList();
						}
						for (final IRDataTableInput.StateListener listener : listeners) {
							listener.tableUnavailable();
						}
					}
				};
				this.processListener.install();
			}
		}
	}
	
	@Override
	public void removeStateListener(final StateListener listener) {
		synchronized (this.listeners) {
			if (this.listeners.remove(listener)
					&& this.listeners.isEmpty() && this.processListener != null) {
				this.processListener.dispose();
				this.processListener= null;
			}
		}
	}
	
	
	@Override
	public int hashCode() {
		return this.shortName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RToolDataTableInput)) {
			return false;
		}
		final RToolDataTableInput other= (RToolDataTableInput) obj;
		return (this == other || (
				this.tool.equals(other.tool)
				&& this.fullName.equals(other.fullName) ));
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "(" + this.fullName //$NON-NLS-1$
				+ " in " + this.tool.getLabel(ITool.LONG_LABEL) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
