/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.emf.ui.forms;

import java.util.HashMap;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;


public class DirectResourceEditorInput extends PlatformObject
		implements IEditorInput {
	
	
	public static URI NO_URI = URI.createURI("", true); //$NON-NLS-1$
	
	private static HashMap<String, Integer> NAME_NUMBERS = new HashMap<String, Integer>();
	
	public static String createNumberedName(final String prefix) {
		synchronized (NAME_NUMBERS) {
			final Integer previous = NAME_NUMBERS.get(prefix);
			final int number = (previous != null) ? previous.intValue() + 1 : 1;
			NAME_NUMBERS.put(prefix, Integer.valueOf(number));
			return prefix + ' ' + number;
		}
	}
	
	
	private final Resource fResource;
	
	private final String fName;
	
	
	public DirectResourceEditorInput(final String name, final Resource resource) {
//		Assert.isNotNull(fileStore);
//		Assert.isTrue(EFS.SCHEME_FILE.equals(fileStore.getFileSystem().getScheme()));
//		fFileStore= fileStore;
		fName =  name;
		fResource = resource;
	}
	
	@Override
	public boolean exists() {
		return false;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(fName);
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}
	
	@Override
	public String getToolTipText() {
		return fName;
	}
	
	public Resource getResource() {
		return fResource;
	}
	
	
	@Override
	public Object getAdapter(final Class adapter) {
		return super.getAdapter(adapter);
	}
	
	
	@Override
	public int hashCode() {
		return fResource.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DirectResourceEditorInput)) {
			return false;
		}
		return (fResource == ((DirectResourceEditorInput) obj).fResource);
	}
	
}
