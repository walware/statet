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

package de.walware.statet.r.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.ecommons.debug.core.model.VariablePartitionFactory;

import de.walware.rj.data.RCharacterStore;

import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class REnvValue extends RElementValue<ICombinedREnvironment> implements IRIndexValueInternal {
	
	
	private final RMainThread thread;
	
	private @Nullable RElementVariable variable;
	
	private final RElementVariableStore childVariables;
	
	private @Nullable ICombinedREnvironment previousElement;
	private @Nullable RElementVariableStore previousChildVariables;
	
	
	public REnvValue(final ICombinedREnvironment element, final RMainThread thread, final int stamp,
			final @Nullable REnvValue previousValue) {
		super(thread.getDebugTarget(), element, stamp);
		this.thread= thread;
		
		this.childVariables= new RElementVariableStore(this.element.getLength());
		
		if (previousValue != null) {
			this.previousElement= previousValue.element;
			this.previousChildVariables= previousValue.childVariables;
		}
	}
	
	
	public boolean setVariable(final RElementVariable variable) {
		if (variable.getParent() != null) {
			return false;
		}
		synchronized (this.childVariables) {
			if (this.variable != null) {
				return false;
			}
			this.variable= variable;
			
			this.childVariables.forEachSet(
					(childVariable) -> childVariable.setParent(variable) );
			
			return true;
		}
	}
	
	@Override
	public IRVariable getAssignedVariable() {
		return this.variable;
	}
	
	
	@Override
	public String getReferenceTypeName() throws DebugException {
		return this.element.getRClassName();
	}
	
	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}
	
	@Override
	public String getValueString() throws DebugException {
		return getValueString(this.variable);
	}
	
	public String getValueString(final IRVariable variable) throws DebugException {
		final StringBuilder sb= new StringBuilder();
		sb.append('[');
		sb.append(this.element.getLength());
		sb.append(']');
		
		{	String envName= null;
			RElementName elementName= this.element.getElementName();
			if (elementName != null && elementName.getNextSegment() == null
					&& RElementName.isPackageFacetScopeType(elementName.getType()) ) {
				envName= elementName.getDisplayName();
			}
			if (envName == null) {
				envName= this.element.getEnvironmentName();
			}
			if (envName == null) {
				if (this.element.getHandle() != 0) {
					envName= Long.toString(this.element.getHandle());
				}
			}
			else if (variable != null && envName.equals(variable.getName())) {
				envName= null;
			}
			if (envName != null) {
				sb.append("\u2002("); //$NON-NLS-1$
				sb.append(envName);
				sb.append(')');
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return this.element.hasModelChildren(null);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return getPartitionFactory().getVariables(this);
	}
	
	
	@Override
	public final VariablePartitionFactory<IRIndexElementValue> getPartitionFactory() {
		return RListValue.LIST_PARTITION_FACTORY;
	}
	
	@Override
	public long getSize() throws DebugException {
		return this.element.getLength();
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length) {
		return getVariables(offset, length, this.variable);
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length, final IRVariable parent) {
		synchronized (this.childVariables) {
			if (this.stamp != this.thread.getCurrentStamp()) {
				return RElementVariableValue.NO_VARIABLES;
			}
			final IRVariable[] variables= new @NonNull IRVariable[length];
			final boolean direct= (parent == this.variable);
			for (int i= 0; i < length; i++) {
				final long idx= offset + i;
				RElementVariable childVariable= this.childVariables.get(idx);
				if (childVariable == null) {
					final ICombinedRElement childElement= this.element.get(idx);
					if (this.previousElement != null) {
						childVariable= checkPreviousVariable(idx, childElement);
					}
					if (childVariable == null) {
						childVariable= new RElementVariable(childElement, this.thread, this.stamp,
								this.variable );
					}
					this.childVariables.set(idx, childVariable);
				}
				variables[i]= (direct) ? childVariable : RVariableProxy.create(childVariable, parent);
			}
			return variables;
		}
	}
	
	protected RElementVariable checkPreviousVariable(long idx, final ICombinedRElement element) {
		final RCharacterStore names= this.previousElement.getNames();
		if (names != null) {
			idx= names.indexOf(getElement().getName(idx));
			if (idx >= 0 && idx < this.previousElement.getLength()) {
				final RElementVariable previousVariable= this.previousChildVariables.clear(idx);
				if (previousVariable != null
						&& previousVariable.update(element, this.stamp) ) {
					return previousVariable;
				}
			}
		}
		return null;
	}
	
	
}
