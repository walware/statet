/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.LazyRStore.Fragment;

import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedRList;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.internal.debug.core.RStackFrame.LoadDataRunnable;


public class RElementVariable extends RVariable implements IRElementVariable {
	
	
	public static final int DEFAULT_FRAGMENT_COUNT = 100;
	
	abstract class RDataLoader<V extends RObject> implements LazyRStore.Updater<V> {
		
		
		public RDataLoader() {
		}
		
		
		@Override
		public void scheduleUpdate(final LazyRStore<V> store, final Fragment<V> fragment) {
			V data = null;
			try {
				final String refExpr = fFrame.createRefExpression(fElement, fStamp);
				if (refExpr == null) {
					return;
				}
				final LoadDataRunnable<V> runnable = fFrame.new LoadDataRunnable<V>() {
					@Override
					protected V doLoad(final IRToolService r, final IProgressMonitor monitor)
							throws CoreException, UnexpectedRDataException {
						return RDataLoader.this.doLoad(refExpr, fragment, r, monitor);
					}
				};
				data = fFrame.loadData(runnable, fStamp);
			}
			finally {
				store.updateFragment(fragment, data);
			}
		}
		
		protected abstract V doLoad(final String refExpr, Fragment<V> fragment,
				IRToolService r, IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
		
	}
	
	
	protected final RStackFrame fFrame;
	protected final int fStamp;
	
	protected ICombinedRElement fElement;
	
	RValue fValue;
	
	
	public RElementVariable(final ICombinedRElement element, final RStackFrame frame, final int stamp) {
		super(frame.getDebugTarget());
		fFrame = frame;
		fElement = element;
		fStamp = stamp;
	}
	
	
	@Override
	public ICombinedRElement getElement() {
		if (fElement.getRObjectType() == RObject.TYPE_REFERENCE) {
			resolveRef();
		}
		return fElement;
	}
	
	@Override
	public String getName() throws DebugException {
		return fElement.getElementName().getDisplayName();
	}
	
	@Override
	public String getReferenceTypeName() throws DebugException {
		return fElement.getRClassName();
	}
	
	@Override
	public synchronized RValue getValue() throws DebugException {
		if (fValue == null) {
			fValue = createValue();
		}
		return fValue;
	}
	
	protected ICombinedRElement resolveRef() {
		final RWorkspace workspace = getDebugTarget().getProcess().getWorkspaceData();
		if (workspace != null) {
			final RReference reference = (RReference) fElement;
			ICombinedRElement element = workspace.resolve(reference, true);
			if (element != null) {
				fFrame.registerReference(reference, fStamp);
				return element;
			}
			element = fFrame.loadReference(reference, fStamp);
			if (element != null) {
				fFrame.registerReference(reference, fStamp);
				return element;
			}
		}
		return fElement;
	}
	
	protected RValue createValue() {
		ICombinedRElement element = fElement;
		if (element.getRObjectType() == RObject.TYPE_REFERENCE) {
			element = resolveRef();
		}
		switch (element.getRObjectType()) {
		case RObject.TYPE_VECTOR:
			return new RVectorValue(this);
		case RObject.TYPE_ARRAY:
			return new RArrayValue(this);
		case RObject.TYPE_LIST:
		case RObject.TYPE_DATAFRAME:
		case RObject.TYPE_S4OBJECT:
		case RObject.TYPE_ENV:
			return new RListValue(this, (ICombinedRList) element);
		case RObject.TYPE_FUNCTION:
			return new RFunctionValue(this);
		}
		return new RValue(this);
	}
	
}
