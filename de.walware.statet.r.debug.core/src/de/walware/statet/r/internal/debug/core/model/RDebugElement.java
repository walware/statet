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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;


@NonNullByDefault
public class RDebugElement extends DebugElement {
	
	
	public RDebugElement(final RDebugTarget target) {
		super(target);
	}
	
	
	@Override
	public final String getModelIdentifier() {
		return RDebugModel.IDENTIFIER;
	}
	
	@Override
	public RDebugTarget getDebugTarget() {
		return (RDebugTarget) super.getDebugTarget();
	}
	
	
	protected DebugException newNotSupported() {
		return new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
				DebugException.NOT_SUPPORTED, "Not supported.", null));
	}
	
	protected DebugException newRequestLoadDataFailed() {
		return new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
				DebugException.TARGET_REQUEST_FAILED, "Request failed: cannot load R data.", null));
	}
	
	protected DebugException newRequestSetDataFailed() {
		return new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
				DebugException.TARGET_REQUEST_FAILED, "Request failed: cannot set R data.", null));
	}
	
	
	@Override
	public <T> @Nullable T getAdapter(final Class<T> type) {
		return super.getAdapter(type);
	}
	
}
