/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.source.IAnnotationModel;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ui.sourceediting.SourceAnnotationModel;
import de.walware.ecommons.ltk.ui.sourceediting.SourceDocumentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.SourceProblemAnnotation;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;
import de.walware.ecommons.preferences.SettingsChangeNotifier.ChangeListener;

import de.walware.docmlet.tex.core.model.TexModel;
import de.walware.docmlet.tex.ui.editors.LtxEditorBuild;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.sweave.ILtxRweaveSourceUnit;
import de.walware.statet.r.sweave.Sweave;
import de.walware.statet.r.ui.editors.REditorBuild;


public class LtxRweaveDocumentProvider extends SourceDocumentProvider<ILtxRweaveSourceUnit>
		implements IDisposable {
	
	
	private class ThisAnnotationModel extends SourceAnnotationModel {
		
		
		private class ThisProblemRequestor extends ProblemRequestor {
			
			
			private final boolean fHandleTemporaryRProblems;
			
			
			public ThisProblemRequestor() {
				fHandleTemporaryRProblems = isHandlingTemporaryRProblems();
			}
			
			
			@Override
			public void acceptProblems(final IProblem problem) {
				if (problem.getCategoryId() == RModel.TYPE_ID) {
					if (fHandleTemporaryRProblems) {
						fReportedProblems.add(problem);
					}
				}
				else {
					if (fHandleTemporaryProblems) {
						fReportedProblems.add(problem);
					}
				}
			}
			
			@Override
			public void acceptProblems(final String modelTypeId, final List<IProblem> problems) {
				if (modelTypeId == RModel.TYPE_ID) {
					if (fHandleTemporaryRProblems) {
						fReportedProblems.addAll(problems);
					}
				}
				else {
					if (fHandleTemporaryProblems) {
						fReportedProblems.addAll(problems);
					}
				}
			}
			
		}
		
		
		public ThisAnnotationModel(final IResource resource) {
			super(resource);
		}
		
		@Override
		protected boolean isHandlingTemporaryProblems() {
			return fHandleTemporaryTexProblems;
		}
		
		protected boolean isHandlingTemporaryRProblems() {
			return fHandleTemporaryRProblems;
		}
		
		@Override
		protected IProblemRequestor doCreateProblemRequestor(final long stamp) {
			return new ThisProblemRequestor();
		}
		
		@Override
		protected SourceProblemAnnotation createAnnotation(final IProblem problem) {
			if (problem.getCategoryId() == RModel.TYPE_ID) {
				switch (problem.getSeverity()) {
				case IProblem.SEVERITY_ERROR:
					return new SourceProblemAnnotation(REditorBuild.ERROR_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.ERROR_CONFIG );
				case IProblem.SEVERITY_WARNING:
					return new SourceProblemAnnotation(REditorBuild.WARNING_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.WARNING_CONFIG );
				default:
					return new SourceProblemAnnotation(REditorBuild.INFO_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.INFO_CONFIG );
				}
			}
			else if (problem.getCategoryId() == TexModel.LTX_TYPE_ID) {
				switch (problem.getSeverity()) {
				case IProblem.SEVERITY_ERROR:
					return new SourceProblemAnnotation(LtxEditorBuild.ERROR_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.ERROR_CONFIG );
				case IProblem.SEVERITY_WARNING:
					return new SourceProblemAnnotation(LtxEditorBuild.WARNING_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.WARNING_CONFIG );
				default:
					return new SourceProblemAnnotation(LtxEditorBuild.INFO_ANNOTATION_TYPE, problem,
							SourceProblemAnnotation.INFO_CONFIG );
				}
			}
			return null;
		}
		
	}
	
	
	private ChangeListener fEditorPrefListener;
	
	private boolean fHandleTemporaryTexProblems;
	private boolean fHandleTemporaryRProblems;
	
	
	public LtxRweaveDocumentProvider() {
		super(Sweave.LTX_R_MODEL_TYPE_ID, new LtxRweaveDocumentSetupParticipant());
		
		fEditorPrefListener = new SettingsChangeNotifier.ChangeListener() {
			@Override
			public void settingsChanged(final Set<String> groupIds) {
				if (groupIds.contains(REditorBuild.GROUP_ID)
						|| groupIds.contains(LtxEditorBuild.GROUP_ID)) {
					updateEditorPrefs();
				}
			}
		};
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(fEditorPrefListener);
		final IPreferenceAccess access = PreferencesUtil.getInstancePrefs();
		fHandleTemporaryTexProblems = access.getPreferenceValue(LtxEditorBuild.PROBLEMCHECKING_ENABLED_PREF);
		fHandleTemporaryRProblems = access.getPreferenceValue(REditorBuild.PROBLEMCHECKING_ENABLED_PREF);
	}
	
	@Override
	public void dispose() {
		if (fEditorPrefListener != null) {
			PreferencesUtil.getSettingsChangeNotifier().removeChangeListener(fEditorPrefListener);
			fEditorPrefListener = null;
		}
	}
	
	private void updateEditorPrefs() {
		final IPreferenceAccess access = PreferencesUtil.getInstancePrefs();
		final boolean newHandleTemporaryTexProblems = access.getPreferenceValue(LtxEditorBuild.PROBLEMCHECKING_ENABLED_PREF);
		final boolean newHandleTemporaryRProblems = access.getPreferenceValue(REditorBuild.PROBLEMCHECKING_ENABLED_PREF);
		if (fHandleTemporaryTexProblems != newHandleTemporaryTexProblems
				|| fHandleTemporaryRProblems != newHandleTemporaryRProblems ) {
			final boolean enabled = ((fHandleTemporaryTexProblems != newHandleTemporaryTexProblems) ? newHandleTemporaryTexProblems : false)
					|| ((fHandleTemporaryRProblems != newHandleTemporaryRProblems) ? newHandleTemporaryRProblems : false);
			fHandleTemporaryTexProblems = newHandleTemporaryRProblems;
			fHandleTemporaryRProblems = newHandleTemporaryRProblems;
			if (enabled) {
				TexModel.getModelManager().refresh(LTK.EDITOR_CONTEXT);
			}
			else {
				final String mode;
				if (!fHandleTemporaryTexProblems) {
					if (!fHandleTemporaryRProblems) {
						mode = null;
					}
					else {
						mode = TexModel.LTX_TYPE_ID;
					}
				}
				else {
					mode = RModel.TYPE_ID;
				}
				final List<? extends ISourceUnit> sus = LTK.getSourceUnitManager().getOpenSourceUnits(
						RModel.TYPE_ID, LTK.EDITOR_CONTEXT );
				for (final ISourceUnit su : sus) {
					final IAnnotationModel model = getAnnotationModel(su);
					if (model instanceof ThisAnnotationModel) {
						((ThisAnnotationModel) model).clearProblems(mode);
					}
				}
			}
		}
	}
	
	@Override
	protected IAnnotationModel createAnnotationModel(final IFile file) {
		return new ThisAnnotationModel(file);
	}
	
}
