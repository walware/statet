/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.internal.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceFragmentEditorInput;
import de.walware.ecommons.text.ISourceFragment;

import de.walware.statet.r.ui.RUI;



public class RCodeGenEditorInput implements ISourceFragmentEditorInput {
	
	
	private final ISourceFragment fFragment;
	
	
	public RCodeGenEditorInput(final ISourceFragment fragment) {
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
		return RUI.getImageDescriptor(RUI.IMG_OBJ_R_SCRIPT);
	}
	
	@Override
	public String getName() {
		return fFragment.getName();
	}
	
	@Override
	public boolean exists() {
		return false;
	}
	
	@Override
	public String getToolTipText() {
		return fFragment.getFullName();
	}
	
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
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
		if (!(obj instanceof RCodeGenEditorInput)) {
			return false;
		}
		final RCodeGenEditorInput other = (RCodeGenEditorInput) obj;
		return fFragment.equals(other.fFragment);
	}
	
	@Override
	public String toString() {
		return fFragment.getFullName();
	}
	
}
