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

package de.walware.statet.r.ui.dataeditor;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.IToolLifeListener;
import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.model.RElementName;


public class RToolDataTableInput implements IRDataTableInput {
	
	
	private final RElementName fElementName;
	
	private final String fFullName;
	private final String fLastName;
	
	private final ToolProcess fProcess;
	private IToolLifeListener fProcessListener;
	
	private final FastList<IRDataTableInput.StateListener> fListeners= new FastList<>(IRDataTableInput.StateListener.class);
	
	
	public RToolDataTableInput(RElementName name, final ToolProcess process) {
		if (process == null) {
			throw new NullPointerException("process");
		}
		if (name == null) {
			throw new NullPointerException("name");
		}
		
		fElementName = name;
		fFullName = RElementName.createDisplayName(name, RElementName.DISPLAY_FQN | RElementName.DISPLAY_EXACT);
		
		while (name.getNextSegment() != null) {
			name = name.getNextSegment();
		}
		fLastName = name.getDisplayName();
		
		fProcess = process;
	}
	
	
	@Override
	public RElementName getElementName() {
		return fElementName;
	}
	
	@Override
	public String getFullName() {
		return fFullName;
	}
	
	@Override
	public String getLastName() {
		return fLastName;
	}
	
	public ITool getTool() {
		return fProcess;
	}
	
	@Override
	public boolean isAvailable() {
		return !fProcess.isTerminated();
	}
	
	@Override
	public void addStateListener(final StateListener listener) {
		synchronized (fListeners) {
			fListeners.add(listener);
			if (fListeners.size() > 0 && fProcessListener == null) {
				fProcessListener = new IToolLifeListener() {
					@Override
					public void toolStarted(final ToolProcess process) {
					}
					@Override
					public void toolTerminated(final ToolProcess process) {
						if (fProcess == process) {
							final IRDataTableInput.StateListener[] listeners;
							synchronized (fListeners) {
								if (fProcessListener != null) {
									NicoCore.removeToolLifeListener(fProcessListener);
									fProcessListener = null;
								}
								listeners = fListeners.toArray();
							}
							for (final IRDataTableInput.StateListener listener : listeners) {
								listener.tableUnavailable();
							}
						}
					}
				};
				NicoCore.addToolLifeListener(fProcessListener);
				if (fProcess.isTerminated()) {
					NicoCore.removeToolLifeListener(fProcessListener);
					fProcessListener = null;
				}
			}
		}
	}
	
	@Override
	public void removeStateListener(final StateListener listener) {
		synchronized (fListeners) {
			fListeners.remove(listener);
			if (fListeners.size() == 0 && fProcessListener != null) {
				NicoCore.removeToolLifeListener(fProcessListener);
				fProcessListener = null;
			}
		}
	}
	
	
	@Override
	public int hashCode() {
		return fLastName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RToolDataTableInput)) {
			return false;
		}
		final RToolDataTableInput other = (RToolDataTableInput) obj;
		return (this == other || (
				fProcess.equals(other.fProcess)
				&& fFullName.equals(other.fFullName) ));
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "(" + fFullName //$NON-NLS-1$
				+ " in " + fProcess.getLabel(ITool.LONG_LABEL) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
