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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
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
				final IProgressMonitor monitor = getProgressMonitor();
				final SubMonitor progress = SubMonitor.convert(monitor, 1);
				try {
					rinfo.fWorkingCopy.disconnect(progress.newChild(1));
				}
				finally {
					rinfo.fWorkingCopy = null;
					if (monitor != null) {
						monitor.done();
					}
				}
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
		
		final Object ifile = adaptable.getAdapter(IFile.class);
		if (ifile != null) {
			final IProgressMonitor monitor = getProgressMonitor();
			final SubMonitor progress = SubMonitor.convert(monitor, 2);
			try {
				final ISourceUnit pUnit = StatetCore.PERSISTENCE_CONTEXT.getUnit(ifile, Sweave.R_TEX_MODEL_TYPE_ID, true, progress.newChild(1));
				rinfo.fWorkingCopy = StatetCore.EDITOR_CONTEXT.getUnit(pUnit, Sweave.R_TEX_MODEL_TYPE_ID, true, progress.newChild(1));
			}
			finally {
				if (monitor != null) {
					monitor.done();
				}
			}
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
