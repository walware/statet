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

package de.walware.ecommons.emf.internal.forms;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;


public class EFEditingDomain extends AdapterFactoryEditingDomain {
	
	
	private static Collection<Object> gClipboard;
	
	
	public EFEditingDomain(final AdapterFactory adapterFactory, final CommandStack commandStack) {
		super(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
	}
	
	
	@Override
	public Collection<Object> getClipboard() {
		return gClipboard;
	}
	
	@Override
	public void setClipboard(final Collection<Object> clipboard) {
		gClipboard = clipboard;
	}
	
}
