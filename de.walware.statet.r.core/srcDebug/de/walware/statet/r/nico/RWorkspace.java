/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolWorkspace;


/**
 *
 */
public class RWorkspace extends ToolWorkspace {

	
	public RWorkspace(AbstractRController controller) {
		
		super(  controller,
				new Prompt("> ", IBasicRAdapter.META_PROMPT_DEFAULT), 
				"\n");
	}
}
