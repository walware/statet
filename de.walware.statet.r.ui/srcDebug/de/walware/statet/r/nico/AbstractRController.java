/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.core.runtime.CoreException;

import de.walware.statet.nico.runtime.SubmitType;
import de.walware.statet.nico.runtime.ToolController;


public class AbstractRController extends ToolController {

	public AbstractRController(String name) {
		
		super(name);
	}

	public void submit(String[] rCommands) throws CoreException {
		
		submit(rCommands, SubmitType.EDITOR);
	}

}
