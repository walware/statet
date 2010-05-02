/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import com.ibm.icu.text.DateFormat;

import org.eclipse.debug.core.ILaunch;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;


/**
 * Process (in the Eclipse debug framework) for an R instance.
 */
public class RProcess extends ToolProcess<RWorkspace> {
	
	
	private final IREnvConfiguration fREnvConfig;
	
	
	/**
	 * Creates an new R process handle.
	 * 
	 * @param launch
	 * @param renv
	 * @param labelPrefix
	 * @param name
	 * @param address
	 * @param wd
	 * @param timestamp
	 */
	public RProcess(final ILaunch launch, final IREnvConfiguration renv,
			final String labelPrefix, final String name,
			final String address, final String wd, final long timestamp) {
		super(launch, RTool.TYPE, labelPrefix, name,
				address, wd, timestamp);
		fREnvConfig = renv;
	}
	
	
	@Override
	public String createTimestampComment(final long timestamp) {
		final String datetime = DateFormat.getDateTimeInstance().format(timestamp);
		// default R format (R: timestamp())
		return "##------ " + datetime + " ------##\n";
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(IREnv.class)) {
			return fREnvConfig.getReference();
		}
		if (required.equals(IREnvConfiguration.class)) {
			return fREnvConfig;
		}
		return super.getAdapter(required);
	}
	
}
