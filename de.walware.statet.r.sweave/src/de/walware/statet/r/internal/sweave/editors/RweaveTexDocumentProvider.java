/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.walware.eclipsecommons.ltk.IDocumentModelProvider;
import de.walware.eclipsecommons.ltk.ISourceUnit;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.sweave.Sweave;


public class RweaveTexDocumentProvider extends TextFileDocumentProvider implements IDocumentModelProvider {
	
	
	public static class SweaveSourceFileInfo extends FileInfo {
		public ISourceUnit fWorkingCopy;
	}
	
	
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	
	
	public RweaveTexDocumentProvider() {
		fDocumentSetupParticipant = new RweaveTexDocumentSetupParticipant();
		final IDocumentProvider provider = new ForwardingDocumentProvider(Rweave.R_TEX_PARTITIONING,
				fDocumentSetupParticipant, new TextFileDocumentProvider());
		setParentDocumentProvider(provider);
	}
	
	
	@Override
	protected FileInfo createEmptyFileInfo() {
		return new SweaveSourceFileInfo();
	}
	
	@Override
	public void connect(final Object element) throws CoreException {
		super.connect(element);
		
		final IDocument document = getDocument(element);
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 extension= (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(Rweave.R_TEX_PARTITIONING) == null) {
				fDocumentSetupParticipant.setup(document);
			}
		}
	}
	
	@Override
	public void disconnect(final Object element) {
		final FileInfo info = getFileInfo(element);
		if (info instanceof SweaveSourceFileInfo) {
			final SweaveSourceFileInfo rinfo = (SweaveSourceFileInfo) info;
			if (rinfo.fCount == 1 && rinfo.fWorkingCopy != null) {
				rinfo.fWorkingCopy.disconnect();
				rinfo.fWorkingCopy = null;
			}
		}
		super.disconnect(element);
	}
	
	@Override
	protected FileInfo createFileInfo(final Object element) throws CoreException {
		final FileInfo info = super.createFileInfo(element);
		if (!(info instanceof SweaveSourceFileInfo)) {
			return null;
		}
		
		final IAdaptable adaptable = (IAdaptable) element;
		final SweaveSourceFileInfo rinfo = (SweaveSourceFileInfo) info;
		setUpSynchronization(info);
		
		final ISourceUnit pUnit = StatetCore.PERSISTENCE_CONTEXT.getUnit(
				adaptable.getAdapter(IFile.class), Sweave.R_TEX_UNIT_TYPE_ID, true);
		if (pUnit != null) {
			rinfo.fWorkingCopy = StatetCore.EDITOR_CONTEXT.getUnit(pUnit, Sweave.R_TEX_UNIT_TYPE_ID, true);
		}
		
		return rinfo;
	}
	
	public ISourceUnit getWorkingCopy(final Object element) {
		final FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof SweaveSourceFileInfo) {
			return ((SweaveSourceFileInfo) fileInfo).fWorkingCopy;
		}
		return null;
	}
	
}
