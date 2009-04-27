/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolWorkspace;


/**
 * R Tool Workspace
 */
public class RWorkspace extends ToolWorkspace {
	
	
	public RWorkspace(final AbstractRController controller) {
		this(controller, null);
	}
	
	public RWorkspace(final AbstractRController controller, final String remoteHost) {
		super(  controller,
				new Prompt("> ", IBasicRAdapter.META_PROMPT_DEFAULT),  //$NON-NLS-1$
				"\n", //$NON-NLS-1$
				remoteHost);
	}
	
}
