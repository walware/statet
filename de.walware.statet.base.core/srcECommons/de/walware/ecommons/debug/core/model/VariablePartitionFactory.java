/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.debug.core.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.models.core.util.ElementPartitionFactory;

import de.walware.statet.base.core.StatetCore;


public class VariablePartitionFactory<T extends IIndexedValue>
		extends ElementPartitionFactory<IVariable, T> {
	
	
	static DebugException newNotSupported() {
		return new DebugException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID,
				DebugException.NOT_SUPPORTED, "Not supported for partitions.", null ));
	}
	
	static DebugException newNotSupported(final Throwable cause) {
		return new DebugException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID,
				DebugException.NOT_SUPPORTED, "Not supported for partitions.", cause ));
	}
	
	static DebugException newRequestInvalidFailed(final Throwable cause) {
		return new DebugException(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID,
				DebugException.REQUEST_FAILED, "Request failed.", cause));
	}
	
	
	public VariablePartitionFactory() {
		super(IVariable.class, DEFAULT_PART_SIZE);
	}
	
	
	public IVariable[] getVariables(final T value) throws DebugException {
		try {
			return getElements(value, value.getSize());
		}
		catch (final UnsupportedOperationException e) {
			throw newNotSupported(e);
		}
		catch (final IllegalArgumentException e) {
			throw newRequestInvalidFailed(e);
		}
	}
	
	@Override
	protected IVariable createPartition(final T value, final PartitionHandle partition) {
		return new VariablePartition<T>(value, partition);
	}
	
	@Override
	protected IVariable[] getChildren(final T value, final long start, final int length) {
		return value.getVariables(start, length);
	}
	
}
