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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;

import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;


@NonNullByDefault
public final class RElementVariable extends RVariable implements IRElementVariable {
	
	
	public static final int DEFAULT_FRAGMENT_COUNT= 100;
	
	
	public static @Nullable RElementName createFQElementName(IRVariable variable) {
		final List<RElementName> segments= new ArrayList<>();
		byte lastType= 0;
		do {
			if (variable instanceof IRElementVariable) {
				final ICombinedRElement element= ((IRElementVariable) variable).getElement();
				segments.add(element.getElementName());
				lastType= element.getRObjectType();
			}
			variable= variable.getParent();
		}
		while (variable != null);
		
		if (lastType != RObject.TYPE_ENV) {
			return null;
		}
		
		Collections.reverse(segments);
		
		return RElementName.create(segments);
	}
	
	
	private final RMainThread thread;
	
	private ICombinedRElement element;
	
	private int stamp;
	
	private @Nullable IRValue value;
	
	private @Nullable ICombinedRElement previousElement;
	private @Nullable IValue previousValue;
	
	
	public RElementVariable(final ICombinedRElement element,
			final RMainThread thread, final int stamp,
			final @Nullable IRVariable parent) {
		super(thread.getDebugTarget(), parent);
		this.thread= thread;
		this.element= element;
		this.stamp= stamp;
	}
	
	
	public synchronized boolean update(final ICombinedRElement element, final int stamp) {
		if (isValidUpdate(element)) {
			this.previousElement= this.element;
			this.previousValue= this.value;
			
			this.element= element;
			this.stamp= stamp;
			this.value= null;
			return true;
		}
		return false;
	}
	
	private boolean isValidUpdate(final ICombinedRElement element) {
		if (element.getRObjectType() == this.element.getRObjectType()) {
			switch (element.getRObjectType()) {
			case RObject.TYPE_VECTOR:
				return (element.getData().getStoreType() == this.element.getData().getStoreType());
			case RObject.TYPE_ARRAY:
				return (element.getData().getStoreType() == this.element.getData().getStoreType()
						&& ((RArray<?>) element).getDim().getLength() == ((RArray<?>) this.element).getDim().getLength() );
			case RObject.TYPE_ENV:
			case RObject.TYPE_S4OBJECT:
				return (Objects.equals(element.getRClassName(), this.element.getRClassName()));
			default:
				return true;
			}
		}
		return false;
	}
	
	public synchronized void reset(final int stamp) {
		this.stamp= stamp;
		this.value= null;
	}
	
	
	@Override
	public final RMainThread getThread() {
		return this.thread;
	}
	
	
	public final ICombinedRElement getCurrentElement() {
		return this.element;
	}
	
	public final @Nullable IRValue getCurrentValue() {
		return this.value;
	}
	
	public final int getCurrentStamp() {
		return this.stamp;
	}
	
	public final @Nullable ICombinedRElement getPreviousElement() {
		return this.previousElement;
	}
	
	public final @Nullable IValue getPreviousValue() {
		return this.previousValue;
	}
	
	
	@Override
	public synchronized ICombinedRElement getElement() {
		return this.element;
	}
	
	@Override
	public @Nullable RElementName getFQElementName() {
		return createFQElementName(this);
	}
	
	@Override
	public synchronized String getName() {
		return this.element.getElementName().getDisplayName();
	}
	
	@Override
	public synchronized String getReferenceTypeName() throws DebugException {
		return this.element.getRClassName();
	}
	
	@Override
	public boolean hasValueChanged() throws DebugException {
		final ICombinedRElement element;
		final ICombinedRElement previousElement;
		final IValue previousValue;
		synchronized (this) {
			element= this.element;
			previousElement= this.previousElement;
			previousValue= this.previousValue;
		}
		if (previousElement != null) {
			switch (element.getRObjectType()) {
			case RObject.TYPE_VECTOR:
				if (previousValue != null && element.getLength() == 1) {
					return ((RVectorValue) getValue()).hasValueChanged(0);
				}
				break;
			case RObject.TYPE_REFERENCE:
				return (((RReference) element).getHandle() != ((RReference) previousElement).getHandle());
			default:
				break;
			}
		}
		return false;
	}
	
	@Override
	public IRValue getValue() throws DebugException {
		return this.getValue(null);
	}
	
	public IRValue getValue(final @Nullable IProgressMonitor monitor) throws DebugException {
		ICombinedRElement element;
		int stamp;
		synchronized (this) {
			if (this.value != null) {
				return this.value;
			}
			
			element= this.element;
			stamp= this.stamp;
			if (element.getRObjectType() != RObject.TYPE_REFERENCE) {
				return this.value= createValue(element);
			}
		}
		// (element.getRObjectType() == RObject.TYPE_REFERENCE)
		if (monitor != null) {
			try {
				element= getThread().resolveReference(element, stamp, monitor);
			}
			catch (final CoreException e) {
				throw new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
						DebugException.TARGET_REQUEST_FAILED, "Request failed: cannot resolve reference.",
						e ));
			}
		}
		else {
			element= getThread().resolveReference(element, stamp);
		}
		IRValue value= null;
		if (element.getRObjectType() == RObject.TYPE_ENV) {
			value= createEnvValue((ICombinedREnvironment) element, stamp);
		}
		synchronized (this) {
			if (this.value != null) {
				return this.value;
			}
			if (value == null) {
				value= createValue(element);
			}
			return this.value= value;
		}
	}
	
	@Override
	public synchronized boolean supportsValueModification() {
		switch (this.element.getRObjectType()) {
		case RObject.TYPE_VECTOR:
			return (this.element.getLength() == 1);
		default:
			return false;
		}
	}
	
	@Override
	public boolean verifyValue(final String expression) throws DebugException {
		final ICombinedRElement element;
		synchronized (this) {
			element= this.element;
		}
		switch (element.getRObjectType()) {
		case RObject.TYPE_VECTOR:
			if (element.getLength() == 1) {
				return ((RVectorValue) getValue()).validateDataValue(expression);
			}
			throw newNotSupported();
		default:
			throw newNotSupported();
		}
	}
	
	@Override
	public void setValue(final String expression) throws DebugException {
		final ICombinedRElement element;
		synchronized (this) {
			element= this.element;
		}
		switch (element.getRObjectType()) {
		case RObject.TYPE_VECTOR:
			if (element.getLength() == 1) {
				((RVectorValue) getValue()).setDataValue(0, expression);
				return;
			}
			throw newNotSupported();
		default:
			throw newNotSupported();
		}
	}
	
	
	private IRValue createValue(final ICombinedRElement element) throws DebugException {
		switch (element.getRObjectType()) {
		case RObject.TYPE_VECTOR:
			return new RVectorValue(this);
		case RObject.TYPE_ARRAY:
			return new RArrayValue(this);
		case RObject.TYPE_LIST:
			return new RListValue(this);
		case RObject.TYPE_ENV:
			return createEnvValue((ICombinedREnvironment) element, this.stamp);
		case RObject.TYPE_DATAFRAME:
		case RObject.TYPE_S4OBJECT:
			return new RListValue.ByName(this);
		case RObject.TYPE_FUNCTION:
			return new RFunctionValue(this);
		default:
			return new RElementVariableValue<>(this);
		}
	}
	
	private @Nullable IRValue createEnvValue(final ICombinedREnvironment element, final int stamp)
			throws DebugException {
		final REnvValue envValue= this.thread.getEnvValue(element, stamp);
		if (envValue != null) {
			final RElementName elementName= element.getElementName();
			if (elementName != null && elementName.getNextSegment() == null
					&& envValue.setVariable(this)) {
				return envValue;
			}
			return RValueProxy.create(envValue, this);
		}
		throw new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
				DebugException.TARGET_REQUEST_FAILED, "Request failed: reference is stale.", null));
	}
	
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		obj= RVariableProxy.unproxy(obj);
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ": " + getName(); //$NON-NLS-1$
	}
	
}
