/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.walware.ecommons.ltk.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.text.IMarkerPositionResolver;
import de.walware.ecommons.text.ui.AnnotationMarkerPositionResolver;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.RLineBreakpointValidator;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.IREditor;


/**
 * Toggles a line breakpoint in a R editor.
 */
public class RToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	
	
	private static class Data {
		
		
		private final IREditor fEditor;
		
		private final IRWorkspaceSourceUnit fSourceUnit;
		
		private AbstractDocument fDocument;
		
		
		Data(final IREditor editor, final IRWorkspaceSourceUnit su) {
			fEditor = editor;
			fSourceUnit = su;
		}
		
		
		public IREditor getEditor() {
			return fEditor;
		}
		
		public IRWorkspaceSourceUnit getSourceUnit() {
			return fSourceUnit;
		}
		
		public void init2(final IProgressMonitor monitor) {
			fDocument = fSourceUnit.getDocument(monitor);
		}
		
		public AbstractDocument getDocument() {
			return fDocument;
		}
		
	}
	
	
	private IRBreakpoint fLastBreakpoint;
	private long fLastStamp;
	
	
	public RToggleBreakpointAdapter() {
	}
	
	
	private Data createData(final IREditor editor) {
		if (editor == null) {
			return null;
		}
		final IRSourceUnit su = editor.getSourceUnit();
		if (!(su instanceof IRWorkspaceSourceUnit)) {
			return null;
		}
		return new Data(editor, (IRWorkspaceSourceUnit) su);
	}
	
	@Override
	public boolean canToggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection) {
		final IREditor editor = getREditor(part, selection);
		return (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection );
	}
	
	@Override
	public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
		final Data data = createData(getREditor(part, selection));
		if (data == null) {
			return;
		}
		final IProgressService progressService = (IProgressService) part.getSite().getWorkbenchWindow().getService(IProgressService.class);
		try {
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						data.init2(monitor);
						if (selection instanceof ITextSelection) {
							final ITextSelection textSelection = (ITextSelection) selection;
							doToggleLineBreakpoint(data, textSelection.getOffset(), monitor);
						}
					}
					catch (final BadLocationException e) {}
					catch (final CoreException e) {
						log(data, e);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {}
		catch (final InterruptedException e) {}
	}
	
	@Override
	public boolean canToggleMethodBreakpoints(final IWorkbenchPart part, final ISelection selection) {
		final IREditor editor = getREditor(part, selection);
		return (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection );
	}
	
	@Override
	public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
		final Data data = createData(getREditor(part, selection));
		if (data == null) {
			return;
		}
		final IProgressService progressService = (IProgressService) part.getSite().getWorkbenchWindow().getService(IProgressService.class);
		try {
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						data.init2(monitor);
						if (selection instanceof ITextSelection) {
							final ITextSelection textSelection = (ITextSelection) selection;
							doToggleMethodBreakpoint(data, textSelection.getOffset(), monitor);
						}
					}
					catch (final BadLocationException e) {}
					catch (final CoreException e) {
						log(data, e);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {}
		catch (final InterruptedException e) {}
	}
	
	@Override
	public boolean canToggleWatchpoints(final IWorkbenchPart part, final ISelection selection) {
		return false;
	}
	
	@Override
	public void toggleWatchpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
	}
	
	@Override
	public boolean canToggleBreakpoints(final IWorkbenchPart part, final ISelection selection) {
		final IREditor editor = getREditor(part, selection);
		return (editor != null && editor.getSourceUnit() instanceof IWorkspaceSourceUnit
				&& selection instanceof ITextSelection );
	}
	
	@Override
	public void toggleBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
		final Data data = createData(getREditor(part, selection));
		final IProgressService progressService = (IProgressService) part.getSite().getWorkbenchWindow().getService(IProgressService.class);
		try {
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						data.init2(monitor);
						if (selection instanceof ITextSelection) {
							final ITextSelection textSelection = (ITextSelection) selection;
							doToggleBestBreakpoint(data, textSelection.getOffset(), monitor);
						}
					}
					catch (final BadLocationException e) {}
					catch (final CoreException e) {
						log(data, e);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {}
		catch (final InterruptedException e) {}
	}
	
	public boolean removeBreakpoints(final IWorkbenchPart part, final ISelection selection,
			final IProgressMonitor monitor) throws CoreException {
		final Data data = createData(getREditor(part, selection));
		if (data == null) {
			return false;
		}
		if (selection instanceof ITextSelection) {
			try {
				data.init2(null);
				if (selection instanceof ITextSelection) {
					final ITextSelection textSelection = (ITextSelection) selection;
					return checkSelectedLine(data, textSelection.getOffset(), null, monitor);
				}
			}
			catch (final BadLocationException e) {}
			catch (final CoreException e) {
				log(data, e);
			}
		}
		return false;
	}
	
	
	private void doToggleLineBreakpoint(final Data data, final int offset, final IProgressMonitor monitor)
			throws BadLocationException, CoreException {
		if (checkSelectedLine(data, offset, RDebugModel.R_LINE_BREAKPOINT_TYPE_ID, monitor)) {
			return;
		}
		final RLineBreakpointValidator validator = new RLineBreakpointValidator(data.getSourceUnit(),
				RDebugModel.R_LINE_BREAKPOINT_TYPE_ID, offset, monitor );
		if (checkNewLine(data, validator, RDebugModel.R_LINE_BREAKPOINT_TYPE_ID, monitor)) {
			return;
		}
		createNew(validator, monitor);
	}
	
	private void doToggleMethodBreakpoint(final Data data, final int offset, final IProgressMonitor monitor)
			throws BadLocationException, CoreException {
		if (checkSelectedLine(data, offset, RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID, monitor)) {
			return;
		}
		final RLineBreakpointValidator validator = new RLineBreakpointValidator(data.getSourceUnit(),
				RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID, offset, monitor );
		if (checkNewLine(data, validator, RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID, monitor)) {
			return;
		}
		createNew(validator, monitor);
	}
	
	private void doToggleBestBreakpoint(final Data data, final int offset, final IProgressMonitor monitor)
			throws BadLocationException, CoreException {
		if (checkSelectedLine(data, offset, null, monitor)) {
			return;
		}
		final RLineBreakpointValidator validator = new RLineBreakpointValidator(data.getSourceUnit(),
				null, offset, monitor);
		if (checkNewLine(data, validator, null, monitor)) {
			return;
		}
		createNew(validator, monitor);
	}
	
	private boolean checkSelectedLine(final Data data, final int offset, final String type,
			final IProgressMonitor monitor)
			throws BadLocationException, CoreException {
		final int lineNumber = data.getDocument().getLineOfOffset(offset) + 1;
		final IRLineBreakpoint breakpoint = findFirst(data, lineNumber, type);
		if (breakpoint != null) {
			DebugUITools.deleteBreakpoints(new IBreakpoint[] { breakpoint },
					getShell(data), monitor);
			return true;
		}
		return false;
	}
	
	private boolean checkNewLine(final Data data, final RLineBreakpointValidator validator, final String type,
			final IProgressMonitor monitor)
			throws CoreException {
		if (validator.getLineNumber() >= 0) {
			final int lineCorr = Math.abs(validator.getLineNumber() - validator.getOriginalLineNumber());
			if (lineCorr > 0) {
				final IRLineBreakpoint breakpoint = findFirst(data, validator.getLineNumber(), type);
				if (breakpoint != null) {
					if (breakpoint == fLastBreakpoint 
							|| type == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID
							|| lineCorr <= 2) {
						DebugUITools.deleteBreakpoints(new IBreakpoint[] { breakpoint },
								getShell(data), monitor);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private IRLineBreakpoint findFirst(final Data data, final int lineNumber, final String type)
			throws CoreException {
		final List<IRLineBreakpoint> breakpoints = RDebugModel.getRLineBreakpoints(
				(IFile) data.getSourceUnit().getResource() );
		if (breakpoints.isEmpty()) {
			return null;
		}
		final IMarkerPositionResolver resolver = getMarkerPositionResolver(data);
		if (fLastBreakpoint != null
				&& (type == null || fLastBreakpoint.getBreakpointType() == type) ) {
			for (final IRLineBreakpoint breakpoint : breakpoints) {
				if (breakpoint == fLastBreakpoint
						&& ((resolver != null) ? resolver.getLine(breakpoint.getMarker()) : breakpoint.getLineNumber()) == lineNumber ) {
					return breakpoint;
				}
			}
		}
		for (final IRLineBreakpoint breakpoint : breakpoints) {
			if ((type == null || breakpoint.getBreakpointType() == type)
					&& ((resolver != null) ? resolver.getLine(breakpoint.getMarker()) : breakpoint.getLineNumber()) == lineNumber ) {
				return breakpoint;
			}
		}
		return null;
	}
	
	private boolean createNew(final RLineBreakpointValidator validator, final IProgressMonitor monitor) {
		final IRBreakpoint breakpoint = validator.createBreakpoint(monitor);
		if (breakpoint != null) {
			fLastBreakpoint = breakpoint;
			fLastStamp = System.currentTimeMillis();
			return true;
		}
		return false;
	}
	
	private IREditor getREditor(final IWorkbenchPart part, final ISelection selection) {
		if (part instanceof IREditor) {
			return (IREditor) part;
		}
		final Object adapter = part.getAdapter(ISourceEditor.class);
		if (adapter instanceof IREditor) {
			return (IREditor) adapter;
		}
		return null;
	}
	
	protected IMarkerPositionResolver getMarkerPositionResolver(final Data data) {
		final AbstractDocument document = data.getDocument();
		final AbstractMarkerAnnotationModel model = getAnnotationModel(data.getEditor());
		if (document != null && model != null) {
			return new AnnotationMarkerPositionResolver(document, model);
		}
		return null;
	}
	
	protected AbstractMarkerAnnotationModel getAnnotationModel(final IREditor editor) {
		if (editor instanceof AbstractTextEditor) {
			final AbstractTextEditor textEditor = (AbstractTextEditor) editor;
			final IDocumentProvider provider = textEditor.getDocumentProvider();
			final IAnnotationModel model = provider.getAnnotationModel(textEditor.getEditorInput());
			if (model instanceof AbstractMarkerAnnotationModel) {
				return (AbstractMarkerAnnotationModel) model;
			}
		}
		return null;
	}
	
	protected void log(final Data data, final CoreException e) {
		StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
				NLS.bind("An error occurred when toggling an R method breakpoint in ''{0}''.",
						data.getSourceUnit().getElementName().getDisplayName() ), e ));
	}
	
	private Shell getShell(final Data data) {
		{	IWorkbenchPart part = data.getEditor().getWorkbenchPart();
			if (part != null) {
				return part.getSite().getShell();
			}
		}
		return null;
	}
	
}
