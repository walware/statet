/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import java.net.URI;

import org.eclipse.core.resources.IFile;

import de.walware.ecommons.ltk.AbstractFilePersistenceSourceUnitFactory;
import de.walware.ecommons.ltk.GenericResourceSourceUnit;
import de.walware.ecommons.ltk.IElementName;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.renv.IREnv;


/**
 * Generic source unit for R related files.
 */
public abstract class RResourceUnit extends GenericResourceSourceUnit {
	
	
	public static String createResourceId(final URI uri) {
		return AbstractFilePersistenceSourceUnitFactory.createResourceId(uri);
	}
	
	public static RResourceUnit createTempUnit(final IFile file, final String modelTypeId) {
		final String id = AbstractFilePersistenceSourceUnitFactory.createResourceId(file);
		return new RResourceUnit(id, file) {
			public String getModelTypeId() {
				return modelTypeId;
			}
		};
	}
	
	
	public RResourceUnit(final String id, final IFile file) {
		super(id, file);
	}
	
	@Override
	protected IElementName createElementName() {
		return RElementName.create(RElementName.RESOURCE, getResource().getName());
	}
	
	public IRCoreAccess getRCoreAccess() {
		final RProject project = RProject.getRProject(getResource().getProject());
		if (project != null) {
			return project;
		}
		return RCore.getWorkbenchAccess();
	}
	
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(IRCoreAccess.class)) {
			return getRCoreAccess();
		}
		return super.getAdapter(required);
	}
	
}
