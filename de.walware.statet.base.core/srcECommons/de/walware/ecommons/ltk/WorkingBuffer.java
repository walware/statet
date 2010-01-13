/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ISynchronizable;

import de.walware.ecommons.FileUtil;
import de.walware.ecommons.ICommonStatusConstants;

import de.walware.statet.base.internal.core.BaseCorePlugin;


public class WorkingBuffer implements IWorkingBuffer {
	
	/** Mode for IFile (in workspace) */
	protected static final int IFILE = 1;
	/** Mode for IFileStore (URI) */
	protected static final int FILESTORE = 2;
	
	
	public static SourceContent createContentFromDocument(final IDocument doc) {
		Object lock = null;
		if (doc instanceof ISynchronizable) {
			lock = ((ISynchronizable) doc).getLockObject();
		}
		if (lock != null && doc instanceof IDocumentExtension4) {
			synchronized (lock) {
				return new SourceContent(
						((IDocumentExtension4) doc).getModificationStamp(),
						doc.get() );
			}
		}
		else {
			return new SourceContent(System.currentTimeMillis(), doc.get());
		}
	}
	
	
	protected final ISourceUnit fUnit;
	private AbstractDocument fDocument;
	
	/**
	 * Mode of this working buffer:<ul>
	 *   <li>= 0 - uninitialized</li>
	 *   <li>< 0 - invalid/no source found</li>
	 *   <li>> 0 - mode constant {@link #IFILE}, {@link #FILESTORE}</li>
	 * </ul>
	 */
	private int fMode;
	
	public WorkingBuffer(final ISourceUnit unit) {
		fUnit = unit;
	}
	
	
	/**
	 * Checks the mode of this working buffer
	 * 
	 * @return <code>true</code> if valid mode, otherwise <code>false</code>
	 */
	protected final boolean detectMode() {
		if (fMode == 0) {
			if (fUnit.getResource() != null) {
				if (fUnit.getResource() instanceof IFile) {
					fMode = IFILE;
				}
			}
			else {
				final IFileStore store = (IFileStore) fUnit.getAdapter(IFileStore.class);
				if (store != null && !store.fetchInfo().isDirectory()) {
					fMode = FILESTORE;
				}
			}
			if (fMode == 0) {
				fMode = -1;
			}
		}
		return (fMode > 0);
	}
	
	protected final int getMode() {
		return fMode;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public synchronized AbstractDocument getDocument(final IProgressMonitor monitor) {
		if (fDocument == null) {
			final SubMonitor progress = SubMonitor.convert(monitor);
			final AbstractDocument doc = createDocument(progress);
			fDocument = doc;
		}
		return fDocument;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SourceContent getContent(final IProgressMonitor monitor) {
		final SubMonitor progress = SubMonitor.convert(monitor);
		final IDocument doc = fDocument;
		if (doc != null) {
			return createContentFromDocument(doc);
		}
		return createContent(progress);
	}
	
	public void saveDocument(final IProgressMonitor monitor) {
	}
	
	public synchronized void releaseDocument(final IProgressMonitor monitor) {
		fDocument = null;
	}
	
	public boolean checkState(final boolean validate, final IProgressMonitor monitor) {
		final ISourceUnit underlyingUnit = fUnit.getUnderlyingUnit();
		if (underlyingUnit != null) {
			return underlyingUnit.checkState(validate, monitor);
		}
		else if (detectMode()) {
			if (getMode() == IFILE) {
				final IFile resource = (IFile) fUnit.getResource();
				if (!validate) {
					return !resource.getResourceAttributes().isReadOnly();
				}
				else {
					return resource.getWorkspace().validateEdit(new IFile[] { resource }, IWorkspace.VALIDATE_PROMPT).isOK();
				}
			}
			if (getMode() == FILESTORE) {
				final IFileStore store = (IFileStore) fUnit.getAdapter(IFileStore.class);
				try {
					return !store.fetchInfo(EFS.NONE, monitor).getAttribute(EFS.ATTRIBUTE_READ_ONLY);
				}
				catch (final CoreException e) {
					BaseCorePlugin.logError(ICommonStatusConstants.IO_ERROR, "An error occurred when checking modifiable state of the file.", e);
				}
			}
		}
		return false;
	}
	
	
	protected AbstractDocument createDocument(final SubMonitor progress) {
		final IDocument fileDoc = FileBuffers.getTextFileBufferManager().createEmptyDocument(null, null);
		if (!(fileDoc instanceof AbstractDocument)) {
			return null;
		}
		final AbstractDocument document = (AbstractDocument) fileDoc;
		
		final ISourceUnit underlyingUnit = fUnit.getUnderlyingUnit();
		if (underlyingUnit != null) {
			final SourceContent underlyingContent = underlyingUnit.getContent(progress);
//			if (document instanceof IDocumentExtension4) {
			document.set(underlyingContent.text, underlyingContent.stamp);
//			}
//			else {
//				document.set(underlyingContent.text);
//			}
		}
		else {
			final IResource resource = fUnit.getResource();
			if (resource instanceof IFile) {
				loadDocumentFromFile((IFile) resource, document, progress);
			}
		}
		return document;
	}
	
	protected final void loadDocumentFromFile(final IFile file, final AbstractDocument document, final SubMonitor progress) {
		try {
			FileUtil.getFileUtil(file).createReadTextFileOp(new FileUtil.ReaderAction() {
				public void run(final BufferedReader reader, final IProgressMonitor monitor) throws IOException {
					final StringBuilder buffer = new StringBuilder();
					final char[] readBuffer = new char[2048];
					int n;
					while ((n = reader.read(readBuffer)) > 0) {
						buffer.append(readBuffer, 0, n);
					}
//					if (document instanceof IDocumentExtension4) {
					document.set(buffer.toString(), file.getModificationStamp());
//					}
//					else {
//						document.set(buffer.toString());
//					}
				}
			}).doOperation(progress);
		} catch (final OperationCanceledException e) {
		} catch (final CoreException e) {
			ECommonsLTK.getPlugin().getLog().log(e.getStatus());
		}
	}
	
	protected SourceContent createContent(final SubMonitor progress) {
		final ISourceUnit underlyingUnit = fUnit.getUnderlyingUnit();
		if (underlyingUnit != null) {
			return underlyingUnit.getContent(progress);
		}
		else {
			final IResource resource = fUnit.getResource();
			final AtomicReference<SourceContent> content = new AtomicReference<SourceContent>();
			if (resource instanceof IFile) {
				loadContentFromFile((IFile) resource, content, progress);
			}
			return content.get();
		}
	}
	
	protected final void loadContentFromFile(final IFile file, final AtomicReference<SourceContent> content, final SubMonitor progress) {
		try {
			FileUtil.getFileUtil(file).createReadTextFileOp(new FileUtil.ReaderAction() {
				public void run(final BufferedReader reader, final IProgressMonitor monitor) throws IOException {
					final StringBuilder buffer = new StringBuilder();
					final char[] readBuffer = new char[2048];
					int n;
					while ((n = reader.read(readBuffer)) >= 0) {
						buffer.append(readBuffer, 0, n);
					}
					content.set(new SourceContent(file.getModificationStamp(), buffer.toString()));
				}
			}).doOperation(progress);
		}
		catch (final OperationCanceledException e) {
		}
		catch (final CoreException e) {
			ECommonsLTK.getPlugin().getLog().log(e.getStatus());
		}
	}
	
}
