/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.part.FileEditorInput;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ui.sourceediting.SourceAnnotationModel;
import de.walware.ecommons.ltk.ui.sourceediting.SourceDocumentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.SourceProblemAnnotation;
import de.walware.ecommons.ltk.ui.sourceediting.SourceProblemAnnotation.PresentationConfig;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.REditorOptions;


public class RDocumentProvider extends SourceDocumentProvider<IRSourceUnit> implements IDisposable {
	
	public static final PresentationConfig PROBLEM = new PresentationConfig(1, ""); //$NON-NLS-1$
	
	public static final String R_ERROR_ANNOTATION_TYPE = "de.walware.statet.r.ui.error"; //$NON-NLS-1$
	public static final String R_WARNING_ANNOTATION_TYPE = "de.walware.statet.r.ui.warning"; //$NON-NLS-1$
	public static final String R_INFO_ANNOTATION_TYPE = "de.walware.statet.r.ui.info"; //$NON-NLS-1$
	
	
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
	
	
	private IEclipsePreferences.IPreferenceChangeListener fEditorPrefListener;
	private boolean fHandleTemporaryProblems;
	
	
	public RDocumentProvider() {
		super(RModel.TYPE_ID, new RDocumentSetupParticipant());
		
		final IPreferenceAccess access = PreferencesUtil.getInstancePrefs();
		RUIPlugin.getDefault().registerPluginDisposable(this);
		fEditorPrefListener = new IEclipsePreferences.IPreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent event) {
				if (event.getKey().equals(REditorOptions.PREF_PROBLEMCHECKING_ENABLED.getKey())) {
					updateEditorPrefs();
				}
			}
		};
		access.addPreferenceNodeListener(REditorOptions.PREF_PROBLEMCHECKING_ENABLED.getQualifier(), fEditorPrefListener);
		fHandleTemporaryProblems = access.getPreferenceValue(REditorOptions.PREF_PROBLEMCHECKING_ENABLED);
	}
	
	
	@Override
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
				RCore.getRModelManager().refresh(LTK.EDITOR_CONTEXT);
			}
			else {
				final List<? extends ISourceUnit> sus = LTK.getSourceUnitManager().getOpenSourceUnits(RModel.TYPE_ID, LTK.EDITOR_CONTEXT);
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
	protected IAnnotationModel createAnnotationModel(final IFile file) {
		return new RAnnotationModel(file);
	}
	
	@Override
	public IAnnotationModel getAnnotationModel(Object element) {
		if (element instanceof ISourceUnit) {
			element = new FileEditorInput((IFile) ((ISourceUnit) element).getResource());
		}
		return super.getAnnotationModel(element);
	}
	
}
