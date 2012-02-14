/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;

import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.RDebugModel;


public class RDebugElement extends DebugElement {
	
	
	public RDebugElement(final IRDebugTarget target) {
		super(target);
	}
	
	
	@Override
	public final String getModelIdentifier() {
		return RDebugModel.IDENTIFIER;
	}
	
	@Override
	public IRDebugTarget getDebugTarget() {
		return (IRDebugTarget) super.getDebugTarget();
	}
	
	
	protected DebugException newNotSupported() {
		return new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
				DebugException.NOT_SUPPORTED, "Not supported.", null));
	}
	
	protected DebugException newRequestLoadDataFailed() {
		return new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
				DebugException.TARGET_REQUEST_FAILED, "Request failed: cannot load R data.", null));
	}
	
	protected DebugException newRequestIllegalIndexFailed() {
		return new DebugException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
				DebugException.REQUEST_FAILED, "Request failed: the specified index is illegal.", null));
	}
	
	
	@Override
	public Object getAdapter(final Class adapter) {
		return super.getAdapter(adapter);
	}
	
}
