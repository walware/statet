/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

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
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


public class RDocumentProvider extends TextFileDocumentProvider implements IDocumentModelProvider {
	
	
	public static class RSourceFileInfo extends FileInfo {
		public IRSourceUnit fWorkingCopy;
	}
	
	
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	
	
	public RDocumentProvider() {
		fDocumentSetupParticipant = new RDocumentSetupParticipant();
		final IDocumentProvider provider = new ForwardingDocumentProvider(IRDocumentPartitions.R_DOCUMENT_PARTITIONING,
				fDocumentSetupParticipant, new TextFileDocumentProvider());
		setParentDocumentProvider(provider);
	}
	
	
	@Override
	protected FileInfo createEmptyFileInfo() {
		return new RSourceFileInfo();
	}
	
	@Override
	public void connect(final Object element) throws CoreException {
		super.connect(element);
		
		final IDocument document = getDocument(element);
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 extension= (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(IRDocumentPartitions.R_DOCUMENT_PARTITIONING) == null) {
				fDocumentSetupParticipant.setup(document);
			}
		}
	}
	
	@Override
	public void disconnect(final Object element) {
		final FileInfo info = getFileInfo(element);
		if (info instanceof RSourceFileInfo) {
			final RSourceFileInfo rinfo = (RSourceFileInfo) info;
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
		if (!(info instanceof RSourceFileInfo)) {
			return null;
		}
		
		final IAdaptable adaptable = (IAdaptable) element;
		final RSourceFileInfo rinfo = (RSourceFileInfo) info;
		setUpSynchronization(info);
		
		final ISourceUnit pUnit = StatetCore.PERSISTENCE_CONTEXT.getUnit(
				adaptable.getAdapter(IFile.class), "r", true); //$NON-NLS-1$
		if (pUnit != null) {
			rinfo.fWorkingCopy = (IRSourceUnit) StatetCore.EDITOR_CONTEXT.getUnit(pUnit, "r", true); //$NON-NLS-1$
		}
		
		return rinfo;
	}
	
	public IRSourceUnit getWorkingCopy(final Object element) {
		final FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof RSourceFileInfo) {
			return ((RSourceFileInfo) fileInfo).fWorkingCopy;
		}
		return null;
	}
	
}
