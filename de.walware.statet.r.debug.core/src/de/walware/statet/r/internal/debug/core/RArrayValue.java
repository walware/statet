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

package de.walware.statet.r.internal.debug.core;

import static de.walware.rj.services.utils.dataaccess.LazyRStore.DEFAULT_FRAGMENT_SIZE;
import static de.walware.statet.r.internal.debug.core.RElementVariable.DEFAULT_FRAGMENT_COUNT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.debug.core.model.IIndexedValue;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.LazyRStore.Fragment;
import de.walware.rj.services.utils.dataaccess.RArrayAsVectorDataAdapter;


public class RArrayValue extends RValue implements IIndexedValue {
	
	
	private static final RArrayAsVectorDataAdapter ADAPTER = new RArrayAsVectorDataAdapter();
	
	
	private LazyRStore<RVector<?>> fDimNameStore;
	private LazyRStore<RVector<?>>[] fDimItemNameStore;
	private LazyRStore<RVector<?>> fDataStore;
	
	protected final long fLength;
	protected final RIntegerStore fDim;
	protected final int fDimCount;
	
	
	public RArrayValue(final RElementVariable variable) {
		super(variable);
		
		fLength = fVariable.fElement.getLength();
		fDim = ((RArray<?>) fVariable.fElement).getDim();
		fDimCount = (int) fDim.getLength();
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		final StringBuilder sb = new StringBuilder();
		sb.append('['); 
		if (fDimCount > 0) {
			sb.append(fDim.getInt(0));
			for (int i = 1; i < fDimCount; i++) {
				sb.append('×');
				sb.append(fDim.getInt(i));
			}
		}
		sb.append(']');
		
		if (fDimCount > 0) {
			final String dimName = getDimName(fDimCount - 1);
			if (dimName != null) {
				sb.append(" / "); //$NON-NLS-1$
				sb.append(dimName);
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (fLength > 0 && fDim.getInt(fDimCount - 1) > 0);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		return PARTITION_FACTORY.getVariables(this);
	}
	
	
	@Override
	public long getSize() throws DebugException {
		return (fLength > 0) ? fDim.getInt(fDimCount - 1) : 0;
	}
	
	
	@Override
	public IVariable[] getVariables(final long offset, final int length) {
		{	final int n = fDim.getInt(fDimCount - 1);
			if (n <= 0) {
				throw new UnsupportedOperationException();
			}
			if (offset < 0 || length < 0 || offset > n - length) {
				throw new IllegalArgumentException();
			}
		}
		final int o = (int) offset;
		final RVariable[] variables = new RVariable[length];
		if (fDimCount == 1) {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[] { o + i };
				variables[i] = new RArrayIndexVariable(this, d);
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				final int[] d = new int[] { o + i };
				variables[i] = new RArrayDimVariable(this, d);
			}
		}
		return variables;
	}
	
	
	protected String getDimName(final int dimIdx) {
		final Fragment<RVector<?>> fragment = ensureDimName(dimIdx);
		if (fragment == null || fragment.getRObject() == null) {
			return null;
		}
		final RStore data = fragment.getRObject().getData();
		final int i = fragment.toLocalRowIdx(dimIdx);
		return (!data.isNA(i)) ? data.getChar(i) : "<NA>"; //$NON-NLS-1$
	}
	
	private LazyRStore.Fragment<RVector<?>> ensureDimName(final int dimIdx) {
		synchronized (fVariable) {
			if (fVariable.fValue != this) {
				return null;
			}
			if (fDimNameStore == null) {
				fDimNameStore= new LazyRStore<>(fDimCount, 1,
						DEFAULT_FRAGMENT_COUNT,
						fVariable.new RDataLoader<RVector<?>>() {
					@Override
					protected RVector<?> doLoad(final String refExpr, final Fragment<RVector<?>> fragment,
							final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
						return ADAPTER.loadDimNames(refExpr, (RArray<?>) fVariable.fElement, fragment,
								r, monitor);
					}
				});
			}
			return fDimNameStore.getFragment(dimIdx, 0);
		}
	}
	
	
	protected String getDimItemName(final int dimIdx, final int idx) {
		final Fragment<RVector<?>> fragment = ensureDimItemNames(dimIdx, idx);
		if (fragment == null || fragment.getRObject() == null) {
			return null;
		}
		final RStore data = fragment.getRObject().getData();
		final int i = fragment.toLocalRowIdx(idx);
		return (!data.isNA(i)) ? data.getChar(i) : "<NA>"; //$NON-NLS-1$
	}
	
	private LazyRStore.Fragment<RVector<?>> ensureDimItemNames(final int dimIdx, final int idx) {
		synchronized (fVariable) {
			if (fVariable.fValue != this) {
				return null;
			}
			if (fDimItemNameStore == null) {
				fDimItemNameStore = new LazyRStore[fDimCount];
			}
			if (fDimItemNameStore[dimIdx] == null) {
				fDimItemNameStore[dimIdx]= new LazyRStore<>(fDim.get(dimIdx), 1,
						DEFAULT_FRAGMENT_COUNT, fVariable.new RDataLoader<RVector<?>>() {
					@Override
					protected RVector<?> doLoad(final String refExpr, final Fragment<RVector<?>> fragment,
							final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
						return ADAPTER.loadDimItemNames(refExpr, (RArray<?>) fVariable.fElement, dimIdx, fragment,
								r, monitor);
					}
				});
			}
			return fDimItemNameStore[dimIdx].getFragment(idx, 0);
		}
	}
	
	
	protected String getData(final long idx) {
		final LazyRStore.Fragment<RVector<?>> fragment = ensureData(idx);
		if (fragment == null) {
			return null;
		}
		final RStore data = fragment.getRObject().getData();
		final int i = fragment.toLocalRowIdx(idx);
		return (!data.isNA(i)) ? data.getChar(i) : "<NA>"; //$NON-NLS-1$
	}
	
	private LazyRStore.Fragment<RVector<?>> ensureData(final long idx) {
		synchronized (fVariable) {
			if (fVariable.fValue != this) {
				return null;
			}
			if (fDataStore == null) {
				final int fragmentSize = estimateFragmentSize();
				fDataStore= new LazyRStore<>(fLength, 1,
						(int) Math.ceil((double) (DEFAULT_FRAGMENT_SIZE * DEFAULT_FRAGMENT_COUNT) / fragmentSize), fragmentSize,
						fVariable.new RDataLoader<RVector<?>>() {
					@Override
					protected RVector<?> doLoad(final String refExpr, final Fragment<RVector<?>> fragment,
							final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
						return ADAPTER.loadData(refExpr, (RArray<?>) fVariable.fElement, fragment, null,
								r, monitor);
					}
				});
			}
			return fDataStore.getFragment(idx, 0);
		}
	}
	
	private int estimateFragmentSize() {
		if (fDimCount <= 1) {
			return DEFAULT_FRAGMENT_SIZE;
		}
		int size = fDim.getInt(fDim.getLength() - 1);
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
	
}
