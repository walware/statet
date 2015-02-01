/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import static de.walware.statet.r.internal.core.sourcemodel.BuildSourceFrame.CREATED_IMPORTED;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.walware.statet.r.core.model.IPackageReferences;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.internal.core.sourcemodel.BuildSourceFrame.ElementAccessList;


public class PackageReferences implements IPackageReferences {
	
	
	protected final Map<String, ElementAccessList> fData;
	
	
	public PackageReferences() {
		fData = new HashMap<String, ElementAccessList>();
	}
	
	
	@Override
	public Set<String> getAllPackageNames() {
		return Collections.unmodifiableSet(fData.keySet());
	}
	
	@Override
	public boolean isImported(final String name) {
		final ElementAccessList list = fData.get(name);
		if (list == null) {
			return false;
		}
		return (list.isCreated == CREATED_IMPORTED);
	}
	
	@Override
	public List<? extends RElementAccess> getAllAccessOfPackage(final String name) {
		final ElementAccessList list = fData.get(name);
		if (list == null) {
			return null;
		}
		return Collections.unmodifiableList(list.entries);
	}
	
	public void add(final String name, final ElementAccess access) {
		ElementAccessList detail = fData.get(name);
		if (detail == null) {
			detail = new ElementAccessList(name);
			fData.put(name, detail);
		}
		detail.entries.add(access);
		/*if (access.isWriteAccess() && !access.isDeletion()) {
			detail.isCreated = CREATED_EXPLICITE;
		}
		else */if (access.isImport()) {
			detail.isCreated = CREATED_IMPORTED;
		}
		access.fShared = detail;
		
		access.fFullNode.addAttachment(access);
	}
	
}
