/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.lang.reflect.InvocationTargetException;

import de.walware.eclipsecommons.ltk.IWorkingBuffer;
import de.walware.eclipsecommons.ltk.SourceDocumentRunnable;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.ltk.ui.FileBufferWorkingBuffer;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rmodel.RManagedWorkingCopy;


public class REditorWorkingCopy extends RManagedWorkingCopy {
	
	
	public REditorWorkingCopy(final IRSourceUnit from) {
		super(from);
	}
	
	
	public WorkingContext getWorkingContext() {
		return StatetCore.EDITOR_CONTEXT;
	}
	
	@Override
	protected IWorkingBuffer createWorkingBuffer() {
		return new FileBufferWorkingBuffer(this);
	}
	
	public void syncExec(final SourceDocumentRunnable runnable)
			throws InvocationTargetException {
		FileBufferWorkingBuffer.syncExec(runnable);
	}
	
}
