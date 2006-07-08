/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;


public abstract class AbstractRController<
		RunnableAdapterType extends IBasicRAdapter,
		WorkspaceType extends RWorkspace> 
		extends ToolController<RunnableAdapterType, WorkspaceType> {

	public AbstractRController(ToolProcess process) {
		
		super(process);
	}

}
