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

package de.walware.statet.r.internal.debug.core.breakpoints;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;

import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;


public abstract class RBreakpoint extends Breakpoint implements IRBreakpoint {
	
	
	public static final String INSTALL_COUNT_MARKER_ATTR = "de.walware.statet.r.debug.markers.InstallCountAttribute"; //$NON-NLS-1$
	
//	public static final String HIT_COUNT_MARKER_ATTR = "de.walware.statet.r.debug.markers.HitCountAttribute"; //$NON-NLS-1$
	
	
	private final Map<IRDebugTarget, ITargetData> fInstalledTargets= new ConcurrentHashMap<>();
	private final AtomicInteger fInstallCount = new AtomicInteger();
	
	
	
	protected RBreakpoint() {
		updateInstallCount(fInstallCount.get());
	}
	
	
	@Override
	public String getModelIdentifier() {
		return RDebugModel.IDENTIFIER;
	}
	
	/**
	 * Add this breakpoint to the breakpoint manager, or sets it as unregistered.
	 */
	protected void register(final boolean register) throws CoreException {
		final DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin != null && register) {
			plugin.getBreakpointManager().addBreakpoint(this);
		}
		else {
			setRegistered(false);
		}
	}
	
	protected void update() {
	}
	
	
//	public int getHitCount() throws CoreException {
//		return ensureMarker().getAttribute(HIT_COUNT_MARKER_ATTR, -1);
//	}
//	
//	public void setHitCount(final int count) throws CoreException {
//		if (getHitCount() != count) {
//			if (!isEnabled() && count > -1) {
//				setAttributes(new String [] { ENABLED, HIT_COUNT_MARKER_ATTR },
//						new Object[]{ Boolean.TRUE, Integer.valueOf(count) });
//			}
//			else {
//				setAttributes(new String[]{ HIT_COUNT_MARKER_ATTR },
//						new Object[]{ Integer.valueOf(count) });
//			}
//			update();
//		}
//	}
	
	
	@Override
	public ITargetData registerTarget(final IRDebugTarget target, final ITargetData data) {
		if (target == null) {
			throw new NullPointerException("target");
		}
		final ITargetData oldData = fInstalledTargets.put(target, data);
		if (oldData != null && oldData.isInstalled()) {
			if (data == null || !data.isInstalled()) {
				updateInstallCount(fInstallCount.decrementAndGet());
			}
		}
		else {
			if (data != null && data.isInstalled()) {
				updateInstallCount(fInstallCount.incrementAndGet());
			}
		}
		return oldData;
	}
	
	@Override
	public ITargetData unregisterTarget(final IRDebugTarget target) {
		if (target == null) {
			throw new NullPointerException("target");
		}
		final ITargetData oldData = fInstalledTargets.remove(target);
		if (oldData != null && oldData.isInstalled()) {
			updateInstallCount(fInstallCount.decrementAndGet());
		}
		return oldData;
	}
	
	private void updateInstallCount(final int count) {
		try {
			ensureMarker().setAttribute(INSTALL_COUNT_MARKER_ATTR, count);
		}
		catch (final CoreException e) {}
	}
	
	@Override
	public ITargetData getTargetData(final IRDebugTarget target) {
		return fInstalledTargets.get(target);
	}
	
	
	@Override
	public boolean isInstalled() throws CoreException {
		return (fInstallCount.get() > 0);
	}
	
}
