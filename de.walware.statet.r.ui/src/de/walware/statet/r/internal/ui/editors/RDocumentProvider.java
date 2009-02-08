/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.IDocumentModelProvider;
import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.SourceAnnotationModel;
import de.walware.ecommons.ltk.ui.SourceProblemAnnotation;
import de.walware.ecommons.ltk.ui.SourceProblemAnnotation.PresentationConfig;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.REditorOptions;


public class RDocumentProvider extends TextFileDocumentProvider implements IDocumentModelProvider, IDisposable {
	
	public static final PresentationConfig PROBLEM = new PresentationConfig(1,""); //$NON-NLS-1$
	
	public static final String R_ERROR_ANNOTATION_TYPE = "de.walware.statet.r.ui.error"; //$NON-NLS-1$
	public static final String R_WARNING_ANNOTATION_TYPE = "de.walware.statet.r.ui.warning"; //$NON-NLS-1$
	public static final String R_INFO_ANNOTATION_TYPE = "de.walware.statet.r.ui.info"; //$NON-NLS-1$
	
	
	public static class RSourceFileInfo extends FileInfo {
		
		public IRSourceUnit fWorkingCopy;
		
	}
	
	private class RAnnotationModel extends SourceAnnotationModel {
		
		public RAnnotationModel(final IResource resource) {
			super(resource);
		}
		
		@Override
		protected boolean isHandlingTemporaryProblems() {
			return fHandleTemporaryProblems;
		}
		
		@Override
		protected SourceProblemAnnotation createAnnotation(final IProblem problem) {
			switch (problem.getSeverity()) {
			case IProblem.SEVERITY_ERROR:
				return new SourceProblemAnnotation(R_ERROR_ANNOTATION_TYPE, problem, SourceProblemAnnotation.ERROR_CONFIG);
			case IProblem.SEVERITY_WARNING:
				return new SourceProblemAnnotation(R_WARNING_ANNOTATION_TYPE, problem, SourceProblemAnnotation.WARNING_CONFIG);
			default:
				return new SourceProblemAnnotation(R_INFO_ANNOTATION_TYPE, problem, SourceProblemAnnotation.INFO_CONFIG);
			}
		}
		
	}
	
	
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	private IEclipsePreferences.IPreferenceChangeListener fEditorPrefListener;
	private boolean fHandleTemporaryProblems;
	
	
	public RDocumentProvider() {
		fDocumentSetupParticipant = new RDocumentSetupParticipant();
		final IDocumentProvider provider = new ForwardingDocumentProvider(IRDocumentPartitions.R_PARTITIONING,
				fDocumentSetupParticipant, new TextFileDocumentProvider());
		setParentDocumentProvider(provider);
		final IPreferenceAccess access = PreferencesUtil.getInstancePrefs();
		RUIPlugin.getDefault().registerPluginDisposable(this);
		fEditorPrefListener = new IEclipsePreferences.IPreferenceChangeListener() {
			public void preferenceChange(final PreferenceChangeEvent event) {
				if (event.getKey().equals(REditorOptions.PREF_PROBLEMCHECKING_ENABLED.getKey())) {
					updateEditorPrefs();
				}
			}
		};
		access.addPreferenceNodeListener(REditorOptions.PREF_PROBLEMCHECKING_ENABLED.getQualifier(), fEditorPrefListener);
		fHandleTemporaryProblems = access.getPreferenceValue(REditorOptions.PREF_PROBLEMCHECKING_ENABLED);
	}
	
	
	public void dispose() {
		if (fEditorPrefListener != null) {
			final IPreferenceAccess access = PreferencesUtil.getInstancePrefs();
			access.removePreferenceNodeListener(REditorOptions.PREF_PROBLEMCHECKING_ENABLED.getQualifier(), fEditorPrefListener);
			fEditorPrefListener = null;
		}
	}
	
	private void updateEditorPrefs() {
		final IPreferenceAccess access = PreferencesUtil.getInstancePrefs();
		final boolean newHandleTemporaryProblems = access.getPreferenceValue(REditorOptions.PREF_PROBLEMCHECKING_ENABLED);
		if (fHandleTemporaryProblems != newHandleTemporaryProblems) {
			fHandleTemporaryProblems = newHandleTemporaryProblems;
			if (fHandleTemporaryProblems) {
				RCore.getRModelManager().refresh(ECommonsLTK.EDITOR_CONTEXT);
			}
			else {
				final List<? extends ISourceUnit> sus = RCore.getRModelManager().getWorkingCopies(ECommonsLTK.EDITOR_CONTEXT);
				for (final ISourceUnit su : sus) {
					final IAnnotationModel model = getAnnotationModel(su);
					if (model instanceof RAnnotationModel) {
						((RAnnotationModel) model).clearProblems(RModel.TYPE_ID);
					}
				}
			}
		}
	}
	
	@Override
	protected FileInfo createEmptyFileInfo() {
		return new RSourceFileInfo();
	}
	
	@Override
	protected IAnnotationModel createAnnotationModel(final IFile file) {
		return new RAnnotationModel(file);
	}
	
	@Override
	public void connect(final Object element) throws CoreException {
		super.connect(element);
		
		final IDocument document = getDocument(element);
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 extension = (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(IRDocumentPartitions.R_PARTITIONING) == null) {
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
		if (!(info instanceof RSourceFileInfo)) {
			return null;
		}
		
		final IAdaptable adaptable = (IAdaptable) element;
		final RSourceFileInfo rinfo = (RSourceFileInfo) info;
		
		final IProgressMonitor monitor = getProgressMonitor();
		final SubMonitor progress = SubMonitor.convert(monitor, 2);
		try {
			final Object ifile = adaptable.getAdapter(IFile.class);
			if (ifile != null) {
				final ISourceUnit pUnit = ECommonsLTK.PERSISTENCE_CONTEXT.getUnit(ifile, RModel.TYPE_ID, true, progress.newChild(1));
				rinfo.fWorkingCopy = (IRSourceUnit) ECommonsLTK.EDITOR_CONTEXT.getUnit(pUnit, RModel.TYPE_ID, true, progress.newChild(1));
			}
			else if (element instanceof IURIEditorInput) {
				final IFileStore store;
				try {
					store = EFS.getStore(((IURIEditorInput) element).getURI());
				}
				catch (final CoreException e) {
					return rinfo;
				}
				rinfo.fWorkingCopy = (IRSourceUnit) ECommonsLTK.EDITOR_CONTEXT.getUnit(store, RModel.TYPE_ID, true, progress.newChild(1));
			}
		}
		finally {
			if (monitor != null) {
				monitor.done();
			}
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
	
	@Override
	public IAnnotationModel getAnnotationModel(Object element) {
		if (element instanceof ISourceUnit) {
			element = new FileEditorInput((IFile) ((ISourceUnit) element).getResource());
		}
		return super.getAnnotationModel(element);
	}
	
}
