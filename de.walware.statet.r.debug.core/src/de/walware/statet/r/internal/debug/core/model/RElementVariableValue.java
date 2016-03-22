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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.rj.data.RObject;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.IFQRObjectRef;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.LazyRStore.Fragment;
import de.walware.rj.services.utils.dataaccess.RDataAssignment;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RElementVariableValue<TRElement extends ICombinedRElement> extends RElementValue<TRElement> {
	
	
	protected abstract class RDataLoader<V extends RObject> implements LazyRStore.Updater<V> {
		
		
		private @Nullable IFQRObjectRef ref;
		
		
		public RDataLoader() {
		}
		
		
		@Override
		public void scheduleUpdate(final LazyRStore<V> store,
				final @Nullable RDataAssignment assignment, @Nullable final Fragment<V> fragment) {
			final AtomicReference<IStatus> set= new AtomicReference<>();
			V data= null;
			try {
				final RMainThread thread= RElementVariableValue.this.variable.getThread();
				data= thread.loadData(thread.new AccessDataRunnable<V>() {
					@Override
					protected int getRequiredStamp() {
						return RElementVariableValue.this.stamp;
					}
					private @Nullable IFQRObjectRef getRef(final IProgressMonitor monitor) {
						if (RDataLoader.this.ref == null) {
							RDataLoader.this.ref= getThread().createElementRef(
									RElementVariableValue.this.element, getRequiredStamp(),
									monitor );
						}
						return RDataLoader.this.ref;
					}
					@Override
					protected V doRun(final IRToolService r, final IProgressMonitor monitor)
							throws CoreException, UnexpectedRDataException {
						final IFQRObjectRef ref= getRef(monitor);
						if (ref == null) {
							return null;
						}
						if (assignment != null) {
							RDataLoader.this.doSet(ref, assignment, r, monitor);
							set.set(Status.OK_STATUS);
						}
						if (fragment != null) {
							return RDataLoader.this.doLoad(ref,
									fragment, r, monitor);
						}
						else {
							return null;
						}
					}
				});
			}
			finally {
				if (assignment != null) {
					store.updateAssignment(assignment, set.get());
				}
				if (fragment != null) {
					store.updateFragment(fragment, data);
				}
			}
		}
		
		protected void doSet(final IFQRObjectRef ref,
				final RDataAssignment assignment,
				final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
			throw new UnsupportedOperationException();
		}
		
		protected abstract V doLoad(IFQRObjectRef ref,
				Fragment<V> fragment,
				IRToolService r, IProgressMonitor monitor) throws CoreException, UnexpectedRDataException;
		
	}
	
	
	protected final RElementVariable variable;
	
	
	public RElementVariableValue(final RElementVariable variable) {
		super(variable.getDebugTarget(),
				(TRElement) variable.getCurrentElement(), variable.getCurrentStamp());
		this.variable= variable;
	}
	
	
	@Override
	public IRVariable getAssignedVariable() {
		return this.variable;
	}
	
}
