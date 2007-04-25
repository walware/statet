/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;


/**
 * Control migration and backward compatiblity with pre 0.4 R nature and builder
 */
public class RCompatibilityBuilder extends IncrementalProjectBuilder {

	private static final String OLD_NATURE_ID = "de.walware.statet.r.core.RNature"; //$NON-NLS-1$
	private static final String OLD_BUILDER_ID = "de.walware.statet.r.core.RInternalBuilder"; //$NON-NLS-1$
	
	private boolean fAutoMigrate = true;
	private boolean fRemoveOldIds = false;
	
	
	public RCompatibilityBuilder() {
		
		super();
	}
	
	
	@Override
	protected void startupOnInitialize() {
		
		super.startupOnInitialize();
		
		try {
			IProject project = getProject();
			if (fAutoMigrate && project.hasNature(OLD_NATURE_ID) ) {  //$NON-NLS-1$
				if (!project.hasNature(RProject.NATURE_ID)) {
					RProject.addNature(project, new NullProgressMonitor());
				}
				if (fRemoveOldIds) {
					IProjectDescription description = project.getDescription();
					List<String> natures = new ArrayList<String>(Arrays.asList(description.getNatureIds()));
					natures.remove(OLD_NATURE_ID);
					description.setNatureIds(natures.toArray(new String[natures.size()]));
					List<ICommand> builders = new ArrayList<ICommand>(Arrays.asList(description.getBuildSpec()));
					Iterator<ICommand> iter = builders.iterator();
					while(iter.hasNext()) {
						ICommand command = iter.next();
						if (OLD_BUILDER_ID.equals(command.getBuilderName())) {
							iter.remove();
						}
					}
					description.setBuildSpec(builders.toArray(new ICommand[builders.size()]));
					project.setDescription(description, new NullProgressMonitor());
				}
			}
		}
		catch (CoreException e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, StatetCore.STATUSCODE_BUILD_ERROR, 
					"Error occured when migrating R project nature and builder.", e)); //$NON-NLS-1$
		}
	}
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {

		return null;
	}

}
