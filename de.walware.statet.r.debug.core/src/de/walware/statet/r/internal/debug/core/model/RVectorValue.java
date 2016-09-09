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

import static de.walware.statet.r.internal.debug.core.model.RElementVariable.DEFAULT_FRAGMENT_COUNT;

import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.ecommons.debug.core.model.VariablePartitionFactory;

import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.IFQRObjectRef;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.LazyRStore.Fragment;
import de.walware.rj.services.utils.dataaccess.RDataAssignment;
import de.walware.rj.services.utils.dataaccess.RVectorDataAdapter;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.data.RValueFormatter;
import de.walware.statet.r.core.data.RValueValidator;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RVectorValue extends RElementVariableValue<ICombinedRElement> implements IRIndexValueInternal {
	
	
	private static final RVectorDataAdapter ADAPTER= new RVectorDataAdapter();
	
	
	public static final int LOAD_SIZE= 1000;
	
	
	final long length;
	
	private @Nullable LazyRStore<RVector<?>> namesStore;
	private @Nullable LazyRStore<RVector<?>> dataStore;
	
	
	public RVectorValue(final RElementVariable variable) {
		super(variable);
		
		final RVector<?> element= getRObject();
		this.length= element.getLength();
	}
	
	
	public final RVector<?> getRObject() {
		return (RVector<?>) this.element;
	}
	
	
	public boolean hasValueChanged(final long index) {
		final RVectorValue previousValue;
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return false;
			}
			previousValue= (RVectorValue) this.variable.getPreviousValue();
		}
		
		if (previousValue != null) {
			if (index >= previousValue.length) {
				return true;
			}
			final LazyRStore.Fragment<RVector<?>> previousFragment;
			final LazyRStore.Fragment<RVector<?>> currentFragment;
			synchronized (previousValue) {
				previousFragment= previousValue.getLoadedDataFragment(index);
				if (previousFragment == null || previousFragment.getRObject() == null) {
					return false;
				}
			}
			synchronized (this) {
				currentFragment= getDataFragment(index);
				if (currentFragment == null || currentFragment.getRObject() == null) {
					return false;
				}
			}
			return (!Objects.equals(
					currentFragment.getRObject().getData().get(
							currentFragment.toLocalRowIdx(index) ),
					previousFragment.getRObject().getData().get(
							previousFragment.toLocalRowIdx(index) )));
		}
		return false;
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		if (this.length == 0) {
			return ""; //$NON-NLS-1$
		}
		else if (this.length == 1) {
			final String data= getData(0);
			if (data == null) {
				throw newRequestLoadDataFailed();
			}
			return data;
		}
		else {
			return "[" + this.length + ']'; //$NON-NLS-1$
		}
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (this.length > 1);
	}
	
	@Override
	public @NonNull IVariable[] getVariables() throws DebugException {
		if (this.length <= 1) {
			return NO_VARIABLES;
		}
		return getPartitionFactory().getVariables(this);
	}
	
	
	@Override
	public VariablePartitionFactory<IRIndexElementValue> getPartitionFactory() {
		return RElementVariableValue.PARTITION_FACTORY;
	}
	
	@Override
	public long getSize() throws DebugException {
		return this.length;
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length) {
		return getVariables(offset, length, this.variable);
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length, final IRVariable parent) {
		if (this.length <= 1) {
			throw new UnsupportedOperationException();
		}
		if (offset < 0 || length < 0 || offset > this.length - length) {
			throw new IllegalArgumentException();
		}
		final @NonNull IRVariable[] variables= new @NonNull IRVariable[length];
		for (int i= 0; i < length; i++) {
			variables[i]= new RVectorIndexVariable(this, offset + i, parent);
		}
		return variables;
	}
	
	
	protected @Nullable String getName(final long idx) {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return null;
			}
		}
		
		final LazyRStore.Fragment<RVector<?>> fragment;
		synchronized (this) {
			fragment= getNamesFragment(idx);
			if (fragment == null || fragment.getRObject() == null) {
				return null;
			}
		}
		
		final RValueFormatter formatter= getDebugTarget().getValueFormatter();
		synchronized (formatter) {
			return formatter.formatName(
					fragment.getRObject().getNames(), (int) fragment.toLocalRowIdx(idx) );
		}
	}
	
	private @Nullable Fragment<RVector<?>> getNamesFragment(final long idx) {
		if (this.namesStore == null) {
			this.namesStore= new LazyRStore<>(this.length, 1,
					DEFAULT_FRAGMENT_COUNT,
					new RDataLoader<RVector<?>>() {
				@Override
				protected RVector<?> doLoad(final IFQRObjectRef ref,
						final Fragment<RVector<?>> fragment,
						final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
					return ADAPTER.loadRowNames(ref, getRObject(), fragment, null,
							r, monitor );
				}
			});
		}
		return this.namesStore.getFragment(idx, 0, 0, null);
	}
	
	
	protected final @Nullable RStore<?> getValueType() {
		RStore<?> data= this.element.getData();
		if (data.getStoreType() == RStore.FACTOR) {
			synchronized (this) {
				final Fragment<RVector<?>> fragment= getDataFragmentAny();
				if (fragment == null || fragment.getRObject() == null) {
					return null;
				}
				data= fragment.getRObject().getData();
			}
		}
		return data;
	}
	
	protected @Nullable String getData(final long idx) {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return null;
			}
		}
		
		final LazyRStore.Fragment<RVector<?>> fragment;
		synchronized (this) {
			fragment= getDataFragment(idx);
			if (fragment == null || fragment.getRObject() == null) {
				return null;
			}
		}
		
		final RValueFormatter formatter= getDebugTarget().getValueFormatter();
		synchronized (formatter) {
			return formatter.format(
					fragment.getRObject().getData(), (int) fragment.toLocalRowIdx(idx) );
		}
	}
	
	protected boolean validateDataValue(final String expression) {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return false;
			}
		}
		
		final RStore<?> type= getValueType();
		final RValueValidator validator= getDebugTarget().getValueValidator();
		synchronized (validator) {
			return validator.isValid(type, expression);
		}
	}
	
	protected void setDataValue(final long idx, final String expression) throws DebugException {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				throw newRequestSetDataFailed();
			}
		}
		
		final RStore<?> type= getValueType();
		final RStore<?> data;
		final RValueValidator validator= getDebugTarget().getValueValidator();
		synchronized (validator) {
			data= validator.toRData(type, expression);
		}
		if (data == null) {
			throw newNotSupported();
		}
		
		final RDataAssignment assignment= new RDataAssignment(idx, 0, data);
		synchronized (this) {
			setData(assignment);
		}
	}
	
	private LazyRStore<RVector<?>> ensureDataStore() {
		if (this.dataStore == null) {
			this.dataStore= new LazyRStore<>(this.length, 1,
					DEFAULT_FRAGMENT_COUNT,
					new RDataLoader<RVector<?>>() {
				@Override
				protected void doSet(final IFQRObjectRef ref,
						final RDataAssignment assignment,
						final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
					ADAPTER.setData(ref, getRObject(), assignment, null,
							r, monitor );
				}
				@Override
				protected RVector<?> doLoad(final IFQRObjectRef ref,
						final Fragment<RVector<?>> fragment,
						final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
					return ADAPTER.loadData(ref, getRObject(), fragment, null,
							r, monitor );
				}
			});
		}
		return this.dataStore;
	}
	
	private @Nullable Fragment<RVector<?>> getDataFragment(final long idx) {
		return ensureDataStore().getFragment(idx, 0, 0, null);
	}
	
	private @Nullable Fragment<RVector<?>> getDataFragmentAny() {
		final LazyRStore<RVector<?>> dataStore= ensureDataStore();
		Fragment<RVector<?>> fragment= dataStore.getLoadedFragmentAny();
		if (fragment == null) {
			fragment= dataStore.getLoadedFragment(0, 0);
		}
		return fragment;
	}
	
	private @Nullable Fragment<RVector<?>> getLoadedDataFragment(final long idx) {
		return (this.dataStore != null) ?
				this.dataStore.getLoadedFragment(idx, 0) :
				null;
	}
	
	public void setData(final RDataAssignment assignment) {
		ensureDataStore().set(assignment, 0, null);
		
		this.variable.fireChangeEvent(DebugEvent.CONTENT);
	}
	
}
