/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
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

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;



public class RDocumentProvider extends TextFileDocumentProvider {

	
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	

	public static class RSourceFileInfo extends FileInfo {
		public IRSourceUnit fWorkingCopy;
	}

	
	public RDocumentProvider() {
		fDocumentSetupParticipant = new RDocumentSetupParticipant();
		IDocumentProvider provider = new ForwardingDocumentProvider(IRDocumentPartitions.R_DOCUMENT_PARTITIONING,
				fDocumentSetupParticipant, new TextFileDocumentProvider());
		setParentDocumentProvider(provider);
	}
	
	
	@Override
	protected FileInfo createEmptyFileInfo() {
		return new RSourceFileInfo();
	}
	
	@Override
	public void connect(Object element) throws CoreException {
		super.connect(element);
		
		IDocument document = getDocument(element);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension= (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(IRDocumentPartitions.R_DOCUMENT_PARTITIONING) == null) {
				fDocumentSetupParticipant.setup(document);
			}
		}
	}
	
	@Override
	public void disconnect(Object element) {
		FileInfo info = getFileInfo(element);
		if (info instanceof RSourceFileInfo) {
			RSourceFileInfo rinfo = (RSourceFileInfo) info;
			if (rinfo.fCount == 1 && rinfo.fWorkingCopy != null) {
				rinfo.fWorkingCopy.disconnect();
				rinfo.fWorkingCopy = null;
			}
		}
		super.disconnect(element);
	}
	
	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo info = super.createFileInfo(element);
		if (!(info instanceof RSourceFileInfo)) {
			return null;
		}
		
		IAdaptable adaptable = (IAdaptable) element;
		RSourceFileInfo rinfo = (RSourceFileInfo) info;
		setUpSynchronization(info);
		
		IRSourceUnit pUnit = RCore.getUnit((IFile)adaptable.getAdapter(IFile.class));
		if (pUnit != null) {
			rinfo.fWorkingCopy = pUnit.getWorkingCopy(RCore.PRIMARY_WORKING_CONTEXT, true);
		}
		
		return rinfo;
	}
	
	public IRSourceUnit getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof RSourceFileInfo) {
			return ((RSourceFileInfo) fileInfo).fWorkingCopy;
		}
		return null;
	}
	
}