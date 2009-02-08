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

package de.walware.ecommons.ltk;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ISynchronizable;

import de.walware.ecommons.FileUtil;

import de.walware.statet.base.internal.core.BaseCorePlugin;


public class WorkingBuffer implements IWorkingBuffer {
	
	protected final ISourceUnit fUnit;
	private AbstractDocument fDocument;
	
	
	public WorkingBuffer(final ISourceUnit unit) {
		fUnit = unit;
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
	
	public void releaseDocument(final IProgressMonitor monitor) {
		fDocument = null;
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
			if (document instanceof IDocumentExtension4) {
				((IDocumentExtension4) document).set(underlyingContent.text, underlyingContent.stamp);
			}
			else {
				document.set(underlyingContent.text);
			}
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
					if (document instanceof IDocumentExtension4) {
						((IDocumentExtension4)document).set(buffer.toString(), file.getModificationStamp());
					}
					else {
						document.set(buffer.toString());
					}
				}
			}).doOperation(progress);
		} catch (final OperationCanceledException e) {
		} catch (final CoreException e) {
			BaseCorePlugin.log(e.getStatus());
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
	
	protected SourceContent createContentFromDocument(final IDocument doc) {
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
	
	protected final void loadContentFromFile(final IFile file, final AtomicReference<SourceContent> content, final SubMonitor progress) {
		try {
			FileUtil.getFileUtil(file).createReadTextFileOp(new FileUtil.ReaderAction() {
				public void run(final BufferedReader reader, final IProgressMonitor monitor) throws IOException {
					final StringBuilder buffer = new StringBuilder();
					final char[] readBuffer = new char[2048];
					int n;
					while ((n = reader.read(readBuffer)) > 0) {
						buffer.append(readBuffer, 0, n);
					}
					content.set(new SourceContent(file.getModificationStamp(), buffer.toString()));
				}
			}).doOperation(progress);
		}
		catch (final OperationCanceledException e) {
		}
		catch (final CoreException e) {
			BaseCorePlugin.log(e.getStatus());
		}
	}
	
}
