/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;

import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.IWorkingBuffer;
import de.walware.eclipsecommons.ltk.SourceContent;
import de.walware.eclipsecommons.ltk.SourceDocumentRunnable;
import de.walware.eclipsecommons.ltk.WorkingBuffer;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetUIPlugin;



/**
 *
 */
public class WorkbenchDefaultContext extends WorkingContext {

	private class FileBufferWorkingBuffer extends WorkingBuffer {

		ITextFileBuffer fBuffer;
		
		public FileBufferWorkingBuffer(ISourceUnit unit) {
			super(unit);
		}
		
		@Override
		protected AbstractDocument createDocument() {
			IProgressMonitor monitor = new NullProgressMonitor();
			if (fUnit.getResource() instanceof IFile) {
				IPath path = fUnit.getPath();
				try {
					FileBuffers.getTextFileBufferManager().connect(path, LocationKind.IFILE, monitor);
					fBuffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(path, LocationKind.IFILE);
					IDocument fileDoc = fBuffer.getDocument();
					if (!(fileDoc instanceof AbstractDocument)) {
						return null;
					}
					return (AbstractDocument) fileDoc;
				} catch (CoreException e) {
					StatetUIPlugin.log(e.getStatus());
				}
			}
			return super.createDocument();
		}
		
		@Override
		protected SourceContent createContent() {
			if (fUnit.getResource() instanceof IFile) {
				IPath path = fUnit.getPath();
				ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(path, LocationKind.IFILE);
				if (buffer != null) {
					return createContentFromDocument(buffer.getDocument());
				}
			}
			return super.createContent();
		}
		
		@Override
		public void releaseDocument() {
			IProgressMonitor monitor = new NullProgressMonitor();
			if (fBuffer != null) {
				IPath path = fUnit.getPath();
				try {
					FileBuffers.getTextFileBufferManager().disconnect(path, LocationKind.IFILE, monitor);
				} catch (CoreException e) {
					StatetUIPlugin.log(e.getStatus());
				}
				fBuffer = null;
			}
			super.releaseDocument();
		}
	}
	
	@Override
	public IWorkingBuffer createWorkingBuffer(ISourceUnit unit) {
		return new FileBufferWorkingBuffer(unit);
	}

	@Override
	public void syncExec(final SourceDocumentRunnable runnable)
			throws InvocationTargetException {
		final AtomicReference<InvocationTargetException> error = new AtomicReference<InvocationTargetException>();
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				Object docLock = null;
				final AbstractDocument document = runnable.getDocument();
				if (document instanceof ISynchronizable) {
					docLock = ((ISynchronizable) document).getLockObject();
				}
				if (docLock == null) {
					docLock = new Object();
				}

				DocumentRewriteSession rewriteSession = null;
				try {
					if (runnable.getRewriteSessionType() != null) {
						rewriteSession = document.startRewriteSession(runnable.getRewriteSessionType());
					}
					synchronized (docLock) {
						if (runnable.getStampAssertion() > 0 && document.getModificationStamp() != runnable.getStampAssertion()) {
							throw new CoreException(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, "Document out of sync (usuallly caused by concurrent document modifications)."));
						}
						runnable.run(document);
					}
				}
				catch (InvocationTargetException e) {
					error.set(e);
				}
				catch (Exception e) {
					error.set(new InvocationTargetException(e));
				}
				finally {
					if (rewriteSession != null) {
						document.stopRewriteSession(rewriteSession);
					}
				}
			}
		});
		if (error.get() != null) {
			throw error.get();
		}
	}
	
}
