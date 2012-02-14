/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug.core.sourcelookup;

import de.walware.ecommons.text.ISourceFragment;
import de.walware.ecommons.ts.ITool;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.renv.IREnv;


/**
 * Source fragment of an R process
 */
public class RRuntimeSourceFragment implements ISourceFragment {
	
	
	private final RProcess fProcess;
	
	private final String fId;
	
	private final String fName;
	private final String fFullName;
	private final String fSource;
	
	
	public RRuntimeSourceFragment(final RProcess process, final String name, 
			final String fullName, final String source) {
		fProcess = process;
		fName = name;
		fFullName = fullName;
		fSource = source;
		
		fId = "r:" + fProcess.getLabel(ITool.DEFAULT_LABEL) + '-' + fProcess.getStartupTimestamp() + '/' +
				fFullName + '-' + fSource.hashCode();
	}
	
	
	@Override
	public String getId() {
		return fId;
	}
	
	public RProcess getProcess() {
		return fProcess;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public String getFullName() {
		return fFullName;
	}
	
	@Override
	public String getSource() {
		return fSource;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (ToolProcess.class.equals(required)) {
			return fProcess;
		}
		if (IREnv.class.equals(required)) {
			fProcess.getAdapter(required);
		}
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return fId.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RRuntimeSourceFragment)) {
			return false;
		}
		final RRuntimeSourceFragment other = (RRuntimeSourceFragment) obj;
		return (fProcess.equals(other.fProcess)
				&& fFullName.equals(other.fFullName)
				&& fSource.equals(other.fSource) );
	}
	
	@Override
	public String toString() {
		return fFullName;
	}
	
}
