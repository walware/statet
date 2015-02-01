/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import de.walware.rj.renv.IRPkg;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;


public class REnvLabelProvider extends StyledCellLabelProvider {
	
	
	public static String getSafeLabel(final IRLibraryLocation location) {
		String label = location.getLabel();
		if (label == null) {
			final IFileStore store = location.getDirectoryStore();
			if (store != null) {
				label = store.toString();
			}
		}
		if (label == null) {
			label = location.getDirectoryPath();
		}
		return label;
	}
	
	
	public REnvLabelProvider() {
	}
	
	
	@Override
	public void update(final ViewerCell cell) {
		final Object element = cell.getElement();
		if (element instanceof IREnv) {
			update(cell, (IREnv) element);
		}
		else if (element instanceof IREnvConfiguration) {
			update(cell, (IREnv) element);
		}
		else if (element instanceof IRLibraryGroup) {
			update(cell, (IRLibraryGroup) element);
		}
		else if (element instanceof IRLibraryLocation) {
			update(cell, (IRLibraryLocation) element);
		}
		else if (element instanceof IRPkg) {
			update(cell, (IRPkg) element);
		}
		finishUpdate(cell);
	}
	
	protected void finishUpdate(final ViewerCell cell) {
		super.update(cell);
	}
	
	protected void update(final ViewerCell cell, final IREnv rEnv) {
		cell.setImage(RUI.getImage((rEnv.getId().startsWith(IREnv.USER_REMOTE_ENV_ID_PREFIX)) ?
				RUI.IMG_OBJ_R_RUNTIME_ENV : RUI.IMG_OBJ_R_RUNTIME_ENV ));
		cell.setText(rEnv.getName());
	}
	
	protected void update(final ViewerCell cell, final IREnvConfiguration rConfig) {
		cell.setImage(RUI.getImage((rConfig.isRemote()) ?
				RUI.IMG_OBJ_R_RUNTIME_ENV : RUI.IMG_OBJ_R_RUNTIME_ENV ));
		cell.setText(rConfig.getName());
	}
	
	protected void update(final ViewerCell cell, final IRLibraryGroup libGroup) {
		cell.setImage(RUI.getImage(RUI.IMG_OBJ_LIBRARY_GROUP));
		cell.setText(libGroup.getLabel());
	}
	
	protected void update(final ViewerCell cell, final IRLibraryLocation libLocation) {
		cell.setImage(RUI.getImage(RUI.IMG_OBJ_LIBRARY_LOCATION));
		cell.setText(getSafeLabel(libLocation));
	}
	
	protected void update(final ViewerCell cell, final IRPkg pkg) {
		cell.setImage(null);
		cell.setText(pkg.getName());
	}
	
}
