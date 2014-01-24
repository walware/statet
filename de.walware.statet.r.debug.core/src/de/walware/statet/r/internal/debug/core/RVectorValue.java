/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core;

import static de.walware.statet.r.internal.debug.core.RElementVariable.DEFAULT_FRAGMENT_COUNT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.debug.core.model.IIndexedValue;

import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.LazyRStore.Fragment;
import de.walware.rj.services.utils.dataaccess.RVectorDataAdapter;


public class RVectorValue extends RValue implements IIndexedValue {
	
	
	private static final RVectorDataAdapter ADAPTER = new RVectorDataAdapter();
	
	
	public static final int LOAD_SIZE = 1000;
	
	
	protected final long fLength;
	
	protected LazyRStore<RVector<?>> fNamesStore;
	protected LazyRStore<RVector<?>> fDataStore;
	
	
	public RVectorValue(final RElementVariable variable) {
		super(variable);
		fLength = fVariable.fElement.getLength();
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		if (fLength == 0) {
			return ""; //$NON-NLS-1$
		}
		else if (fLength == 1) {
			final String data = getData(0);
			if (data == null) {
				throw newRequestLoadDataFailed();
			}
			return data;
		}
		else {
			return "[" + fLength + ']'; //$NON-NLS-1$
		}
	}
	
	@Override
	public boolean hasVariables() throws DebugException {
		return (fLength > 1);
	}
	
	@Override
	public IVariable[] getVariables() throws DebugException {
		if (fLength <= 1) {
			throw newNotSupported();
		}
		return PARTITION_FACTORY.getVariables(this);
	}
	
	
	@Override
	public long getSize() throws DebugException {
		return fLength;
	}
	
	@Override
	public IVariable[] getVariables(final long offset, final int length) {
		if (fLength <= 1) {
			throw new UnsupportedOperationException();
		}
		if (offset < 0 || length < 0 || offset > fLength - length) {
			throw new IllegalArgumentException();
		}
		final RVariable[] variables = new RVariable[length];
		for (int i = 0; i < length; i++) {
			variables[i] = new RVectorIndexVariable(this, offset + i);
		}
		return variables;
	}
	
	
	protected String getName(final long idx) {
		final LazyRStore.Fragment<RVector<?>> fragment = ensureNames(idx);
		if (fragment == null || fragment.getRObject() == null) {
			return null;
		}
		final RStore names = fragment.getRObject().getData();
		final long i = fragment.toLocalRowIdx(idx);
		return (!names.isNA(i)) ? names.getChar(i) : "<NA>"; //$NON-NLS-1$
	}
	
	private LazyRStore.Fragment<RVector<?>> ensureNames(final long idx) {
		synchronized (fVariable) {
			if (fVariable.fValue != this) {
				return null;
			}
			if (fNamesStore == null) {
				fNamesStore = new LazyRStore<RVector<?>>(fLength, 1,
						DEFAULT_FRAGMENT_COUNT,
						fVariable.new RDataLoader<RVector<?>>() {
					@Override
					protected RVector<?> doLoad(final String refExpr, final Fragment<RVector<?>> fragment,
							final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
						return ADAPTER.loadRowNames(refExpr, (RVector<?>) fVariable.fElement, fragment, null,
								r, monitor);
					}
				});
			}
			return fNamesStore.getFragment(idx, 0);
		}
	}
	
	
	protected String getData(final long idx) {
		final LazyRStore.Fragment<RVector<?>> fragment = ensureData(idx);
		if (fragment == null || fragment.getRObject() == null) {
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
				fDataStore = new LazyRStore<RVector<?>>(fLength, 1,
						DEFAULT_FRAGMENT_COUNT,
						fVariable.new RDataLoader<RVector<?>>() {
					@Override
					protected RVector<?> doLoad(final String refExpr, final Fragment<RVector<?>> fragment,
							final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
						return ADAPTER.loadData(refExpr, (RVector<?>) fVariable.fElement, fragment, null,
								r, monitor);
					}
				});
			}
			return fDataStore.getFragment(idx, 0);
		}
	}
	
}
