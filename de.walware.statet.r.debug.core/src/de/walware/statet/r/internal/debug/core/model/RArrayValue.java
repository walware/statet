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

import static de.walware.rj.services.utils.dataaccess.LazyRStore.DEFAULT_FRAGMENT_SIZE;
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

import de.walware.rj.data.RArray;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.IFQRObjectRef;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.LazyRStore.Fragment;
import de.walware.rj.services.utils.dataaccess.RArrayAsVectorDataAdapter;
import de.walware.rj.services.utils.dataaccess.RDataAssignment;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.data.RValueFormatter;
import de.walware.statet.r.core.data.RValueValidator;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RArrayValue extends RElementVariableValue<ICombinedRElement>
		implements IRIndexValueInternal {
	
	
	private static final RArrayAsVectorDataAdapter ADAPTER= new RArrayAsVectorDataAdapter();
	
	
	final long length;
	final RIntegerStore dim;
	final int dimCount;
	
	private @Nullable LazyRStore<RVector<?>> dimNameStore;
	private @Nullable LazyRStore<RVector<?>>[] dimItemNameStore;
	private @Nullable LazyRStore<RVector<?>> dataStore;
	
	
	public RArrayValue(final RElementVariable variable) {
		super(variable);
		
		final RArray<?> element= getRObject();
		this.length= element.getLength();
		this.dim= element.getDim();
		this.dimCount= (int) this.dim.getLength();
	}
	
	
	public final RArray<?> getRObject() {
		return (RArray<?>) this.element;
	}
	
	
	public boolean hasValueChanged(final int[] dimIndex) throws DebugException {
		final RArrayValue previousValue;
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return false;
			}
			previousValue= (RArrayValue) this.variable.getPreviousValue();
		}
		
		if (previousValue != null) {
			for (int i= 0; i < this.dimCount; i++) {
				if (dimIndex[i] >= previousValue.dim.getInt(i)) {
					return true;
				}
			}
			final long previousIdx= RDataUtil.getDataIdx(previousValue.dim, dimIndex);
			final long currentIdx= RDataUtil.getDataIdx(this.dim, dimIndex);
			final Fragment<RVector<?>> previousFragment;
			final Fragment<RVector<?>> currentFragment;
			synchronized (previousValue) {
				previousFragment= previousValue.getLoadedDataFragment(previousIdx);
				if (previousFragment == null || previousFragment.getRObject() == null) {
					return false;
				}
			}
			synchronized (this) {
				currentFragment= getDataFragment(currentIdx);
				if (currentFragment == null || currentFragment.getRObject() == null) {
					return false;
				}
			}
			return (!Objects.equals(
					currentFragment.getRObject().getData().get(
							currentFragment.toLocalRowIdx(currentIdx) ),
					previousFragment.getRObject().getData().get(
							previousFragment.toLocalRowIdx(previousIdx) )));
		}
		return false;
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb= new StringBuilder();
		sb.append('['); 
		if (this.dimCount > 0) {
			sb.append(this.dim.getInt(0));
			for (int i= 1; i < this.dimCount; i++) {
				sb.append('×');
				sb.append(this.dim.getInt(i));
			}
		}
		sb.append(']');
		
		if (this.dimCount > 0) {
			final String dimName= getDimName(this.dimCount - 1);
			if (dimName != null) {
				sb.append(" / "); //$NON-NLS-1$
				sb.append(dimName);
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (this.length > 0 && this.dim.getInt(this.dimCount - 1) > 0);
	}
	
	@Override
	public @NonNull IVariable[] getVariables() throws DebugException {
		return getPartitionFactory().getVariables(this);
	}
	
	
	@Override
	public final VariablePartitionFactory<IRIndexElementValue> getPartitionFactory() {
		return RElementValue.PARTITION_FACTORY;
	}
	
	@Override
	public long getSize() throws DebugException {
		return (this.length > 0) ? this.dim.getInt(this.dimCount - 1) : 0;
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length) {
		return getVariables(offset, length, this.variable);
	}
	
	@Override
	public @NonNull IRVariable[] getVariables(final long offset, final int length, final IRVariable parent) {
		{	final int n= this.dim.getInt(this.dimCount - 1);
			if (n <= 0) {
				throw new UnsupportedOperationException();
			}
			if (offset < 0 || length < 0 || offset > n - length) {
				throw new IllegalArgumentException();
			}
		}
		final int o= (int) offset;
		final @NonNull IRVariable[] variables= new @NonNull IRVariable[length];
		if (this.dimCount == 1) {
			for (int i= 0; i < length; i++) {
				final int[] d= new int[] { o + i };
				variables[i]= new RArrayIndexVariable(this, d, parent);
			}
		}
		else {
			for (int i= 0; i < length; i++) {
				final int[] d= new int[] { o + i };
				variables[i]= new RArrayDimVariable(this, d, parent);
			}
		}
		return variables;
	}
	
	
	protected @Nullable String getDimName(final int dimIdx) {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return null;
			}
		}
		
		final Fragment<RVector<?>> fragment;
		synchronized (this) {
			fragment= getDimNameFragment(dimIdx);
			if (fragment == null || fragment.getRObject() == null) {
				return null;
			}
		}
		
		final RValueFormatter formatter= getDebugTarget().getValueFormatter();
		synchronized (formatter) {
			return formatter.formatName(
					fragment.getRObject().getData(), (int) fragment.toLocalRowIdx(dimIdx) );
		}
	}
	
	private @Nullable Fragment<RVector<?>> getDimNameFragment(final int dimIdx) {
		if (this.dimNameStore == null) {
			this.dimNameStore= new LazyRStore<>(this.dimCount, 1,
					DEFAULT_FRAGMENT_COUNT,
					new RDataLoader<RVector<?>>() {
				@Override
				protected RVector<?> doLoad(final IFQRObjectRef ref,
						final Fragment<RVector<?>> fragment,
						final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
					return ADAPTER.loadDimNames(ref, getRObject(), fragment,
							r, monitor );
				}
			});
		}
		return this.dimNameStore.getFragment(dimIdx, 0, 0, null);
	}
	
	
	protected @Nullable String getDimItemName(final int dimIdx, final int idx) {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return null;
			}
		}
		
		final Fragment<RVector<?>> fragment;
		synchronized (this) {
			fragment= getDimItemNamesFragment(dimIdx, idx);
			if (fragment == null || fragment.getRObject() == null) {
				return null;
			}
		}
		
		final RValueFormatter formatter= getDebugTarget().getValueFormatter();
		synchronized (formatter) {
			return formatter.formatName(
					fragment.getRObject().getData(), (int) fragment.toLocalRowIdx(idx) );
		}
	}
	
	private @Nullable Fragment<RVector<?>> getDimItemNamesFragment(final int dimIdx, final int idx) {
		if (this.dimItemNameStore == null) {
			this.dimItemNameStore= new LazyRStore[this.dimCount];
		}
		if (this.dimItemNameStore[dimIdx] == null) {
			this.dimItemNameStore[dimIdx]= new LazyRStore<>(this.dim.get(dimIdx), 1,
					DEFAULT_FRAGMENT_COUNT, new RDataLoader<RVector<?>>() {
				@Override
				protected RVector<?> doLoad(final IFQRObjectRef ref,
						final Fragment<RVector<?>> fragment,
						final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
					return ADAPTER.loadDimItemNames(ref, getRObject(), dimIdx, fragment,
							r, monitor );
				}
			});
		}
		return this.dimItemNameStore[dimIdx].getFragment(idx, 0, 0, null);
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
	
	protected @Nullable String getData(final int[] dimIndex) {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				return null;
			}
		}
		
		final LazyRStore.Fragment<RVector<?>> fragment;
		final long idx= RDataUtil.getDataIdx(this.dim, dimIndex);
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
	
	protected void setDataValue(final int[] dimIndex, final String expression) throws DebugException {
		synchronized (this.variable) {
			if (this != this.variable.getCurrentValue()) {
				throw newRequestSetDataFailed();
			}
		}
		
		final RStore<?> type= getValueType();
		final RValueValidator validator= getDebugTarget().getValueValidator();
		final RStore<?> data;
		synchronized (validator) {
			data= validator.toRData(type, expression);
		}
		if (data == null) {
			throw newNotSupported();
		}
		
		final long idx= RDataUtil.getDataIdx(this.dim, dimIndex);
		final RDataAssignment assignment= new RDataAssignment(idx, 0, data);
		synchronized (this) {
			setData(assignment);
		}
	}
	
	private LazyRStore<RVector<?>> ensureDataStore() {
		if (this.dataStore == null) {
			final int fragmentSize= estimateFragmentSize();
			this.dataStore= new LazyRStore<>(this.length, 1,
					(int) Math.ceil((double) (DEFAULT_FRAGMENT_SIZE * DEFAULT_FRAGMENT_COUNT) / fragmentSize), fragmentSize,
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
	
	private int estimateFragmentSize() {
		if (this.dimCount <= 1) {
			return DEFAULT_FRAGMENT_SIZE;
		}
		int size= this.dim.getInt(this.dim.getLength() - 1);
		if (size > DEFAULT_FRAGMENT_SIZE) {
			do {
				if (size % 2 != 0) {
					return DEFAULT_FRAGMENT_SIZE;
				}
				size /= 2;
			} while (size > DEFAULT_FRAGMENT_SIZE);
		}
		else {
			while (size <= DEFAULT_FRAGMENT_SIZE / 2) {
				size *= 2;
			}
		}
		return size;
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
