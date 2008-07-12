/*******************************************************************************
 * Copyright (c) 2000-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.core.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.UndoTextFileChange;
import org.eclipse.text.edits.UndoEdit;

import de.walware.eclipsecommons.ltk.ISourceUnit;


/**
 * Undo variant of {@link SourceUnitChange}
 */
public class UndoSourceUnitChange extends UndoTextFileChange {
	
	
	private ISourceUnit fSourceUnit;
	
	
	public UndoSourceUnitChange(final String name, final ISourceUnit su, final IFile file, 
			final UndoEdit undo, final ContentStamp stampToRestore, final int saveMode) throws CoreException {
		super(name, file, undo, stampToRestore, saveMode);
		fSourceUnit = su;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getModifiedElement() {
		return fSourceUnit;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Change createUndoChange(final UndoEdit edit, final ContentStamp stampToRestore) throws CoreException {
		return new UndoSourceUnitChange(getName(), fSourceUnit, (IFile) super.getModifiedElement(), edit, stampToRestore, getSaveMode());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Change perform(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, 10);
		fSourceUnit.connect(progress.newChild(1));
		try {
			return super.perform(progress.newChild(8));
		}
		finally {
			fSourceUnit.disconnect(progress.newChild(1));
		}
	}
	
}
