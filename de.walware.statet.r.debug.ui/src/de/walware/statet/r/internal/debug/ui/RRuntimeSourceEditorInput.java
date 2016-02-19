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

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceFragmentEditorInput;
import de.walware.ecommons.text.ISourceFragment;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.debug.core.sourcelookup.RRuntimeSourceFragment;


public class RRuntimeSourceEditorInput implements ISourceFragmentEditorInput {
	
	
	private final RRuntimeSourceFragment fFragment;
	
	
	public RRuntimeSourceEditorInput(final RRuntimeSourceFragment fragment) {
		if (fragment == null) {
			throw new NullPointerException("fragment"); //$NON-NLS-1$
		}
		fFragment = fragment;
	}
	
	
	@Override
	public ISourceFragment getSourceFragment() {
		return fFragment;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return RDebugUIPlugin.getDefault().getImageRegistry().getDescriptor(
				RDebugUIPlugin.IMG_OBJ_R_SOURCE_FROM_RUNTIME );
	}
	
	@Override
	public String getName() {
		return fFragment.getName();
	}
	
	@Override
	public boolean exists() {
		return !fFragment.getProcess().isTerminated();
	}
	
	@Override
	public String getToolTipText() {
		return fFragment.getFullName() + '\n' + fFragment.getProcess().getLabel(ITool.LONG_LABEL);
	}
	
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (required == ToolProcess.class) {
			return fFragment.getProcess();
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return fFragment.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RRuntimeSourceEditorInput)) {
			return false;
		}
		final RRuntimeSourceEditorInput other = (RRuntimeSourceEditorInput) obj;
		return fFragment.equals(other.fFragment);
	}
	
	@Override
	public String toString() {
		return fFragment.getFullName() + " - " + fFragment.getProcess().getLabel(ITool.LONG_LABEL); //$NON-NLS-1$
	}
	
}
