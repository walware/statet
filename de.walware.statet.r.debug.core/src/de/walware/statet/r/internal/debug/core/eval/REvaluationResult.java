/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.eval;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.debug.core.IREvaluationResult;
import de.walware.statet.r.debug.core.IRValue;
import de.walware.statet.r.debug.core.IRVariable;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.model.RElementVariable;
import de.walware.statet.r.internal.debug.core.model.RMainThread;


@NonNullByDefault
public final class REvaluationResult implements IREvaluationResult {
	
	
	private final String expression;
	
	private final RMainThread thread;
	
	private final int status;
	
	private final @Nullable RElementVariable variable;
	private final @Nullable String tmpId;
	
	private final @Nullable ImList<@NonNull String> errorMessages;
	
	private int lockCounter;
	
	
	public REvaluationResult(final String expression, final RMainThread thread,
			final RElementVariable variable, final String tmpId) {
		this.expression= expression;
		this.thread= thread;
		this.status= IStatus.OK;
		this.variable= variable;
		this.tmpId= tmpId;
		this.errorMessages= null;
		
		this.lockCounter= 1;
	}
	
	public REvaluationResult(final String expression, final RMainThread thread,
			final int status, final String errorMessage) {
		this.expression= expression;
		this.thread= thread;
		this.status= status;
		this.variable= null;
		this.tmpId= null;
		this.errorMessages= ImCollections.newList(errorMessage);
		
		this.lockCounter= 1;
	}
	
	public REvaluationResult(final String expression, final RMainThread thread) {
		this.expression= expression;
		this.thread= thread;
		this.status= SKIPPED;
		this.variable= null;
		this.tmpId= null;
		this.errorMessages= null;
		
		this.lockCounter= 1;
	}
	
	
	@Override
	public String getExpressionText() {
		return this.expression;
	}
	
	@Override
	public RMainThread getThread() {
		return this.thread;
	}
	
	@Override
	public int getStatus() {
		return this.status;
	}
	
	public @Nullable IRVariable getVariable() {
		return this.variable;
	}
	
	@Override
	public @Nullable IRValue getValue() {
		return (this.variable != null) ? this.variable.getCurrentValue() : null;
	}
	
	public @Nullable String getTmpId() {
		return this.tmpId;
	}
	
	@Override
	public @Nullable ImList<@NonNull String> getMessages() {
		return this.errorMessages;
	}
	
	
	public synchronized void lock() {
		this.lockCounter++;
	}
	
	@Override
	public synchronized void free() {
		this.lockCounter--;
		
		if (this.lockCounter < 0) {
			this.thread.getExpressionManager().scheduleClean();
		}
	}
	
	public synchronized boolean isLocked() {
		return (this.lockCounter > 0);
	}
	
	public void reset(final int stamp, final IProgressMonitor monitor) {
		if (this.variable != null) {
			try {
				this.variable.reset(stamp);
				this.variable.getValue(monitor);
			}
			catch (final DebugException e) {
				RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
						"An error occurred when refreshing expression result.", e ));
			}
		}
	}
	
}
