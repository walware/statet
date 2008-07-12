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

package de.walware.statet.base.ui.sourceeditors;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.walware.eclipsecommons.FastList;
import de.walware.eclipsecommons.ui.text.PairMatcher;
import de.walware.eclipsecommons.ui.util.ISettingsChangedHandler;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


/**
 * 
 */
public abstract class SourceViewerConfigurator implements ISettingsChangedHandler {
	
	
	private PairMatcher fPairMatcher;
	private IPreferenceStore fPreferenceStore;
	private StatextSourceViewerConfiguration fConfiguration;
	private IEditorAdapter fEditor;
	private final FastList<IEditorInstallable> fInstallables = new FastList<IEditorInstallable>(IEditorInstallable.class);
	
	protected boolean fIsConfigured;
	
	
	protected SourceViewerConfigurator() {
	}
	
	
	/**
	 * A setup participant for the document of the editor.
	 * 
	 * @return a document setup participant or <code>null</code>.
	 */
	public abstract IDocumentSetupParticipant getDocumentSetupParticipant();
	
	protected void setPreferenceStore(final IPreferenceStore store) {
		fPreferenceStore = store;
	}
	
	/**
	 * @return Returns the jface PreferenceStore.
	 */
	public IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}
	
	protected void setPairMatcher(final PairMatcher pairMatcher) {
		fPairMatcher = pairMatcher;
	}
	
	/**
	 * PairMatcher used for pairmatching decoration and
	 * goto matching bracket action.
	 * 
	 * @return the pair matcher of <code>null</code>.
	 */
	public PairMatcher getPairMatcher() {
		return fPairMatcher;
	}
	
	
	protected void setConfiguration(final StatextSourceViewerConfiguration config) {
		fConfiguration = config;
	}
	
	public StatextSourceViewerConfiguration getSourceViewerConfiguration() {
		return fConfiguration;
	}
	
	public void configureSourceViewerDecorationSupport(final SourceViewerDecorationSupport support) {
		if (fPairMatcher != null) {
			support.setCharacterPairMatcher(fPairMatcher);
			support.setMatchingCharacterPainterPreferenceKeys(
					IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS,
					IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
		}
	}
	
	
	public void setTarget(final IEditorAdapter editor, final boolean configure) {
		assert (editor != null);
		fEditor = editor;
		if (configure) {
			fEditor.getSourceViewer().getControl().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(final DisposeEvent e) {
					if (fIsConfigured) {
						uninstallCurrentModules();
					}
				}
			});
			configureTarget();
		}
		else {
			installCurrentModules();
		}
		handleSettingsChanged(null, null);
	}
	
	protected ISourceViewer getSourceViewer() {
		if (fEditor != null) {
			return fEditor.getSourceViewer();
		}
		return null;
	}
	
	public final void unconfigureTarget() {
		if (fEditor != null) {
			fIsConfigured = false;
			uninstallCurrentModules();
			fEditor.getSourceViewer().unconfigure();
		}
	}
	
	public final void configureTarget() {
		if (fEditor != null) {
			fIsConfigured = true;
			fEditor.getSourceViewer().configure(fConfiguration);
			installCurrentModules();
		}
	}
	
	private void installCurrentModules() {
		final IEditorInstallable[] installables = fInstallables.toArray();
		for (int i = 0; i < installables.length; i++) {
			installables[i].install(fEditor);
		}
	}
	
	private void uninstallCurrentModules() {
		final IEditorInstallable[] installables = fInstallables.toArray();
		for (int i = 0; i < installables.length; i++) {
			installables[i].uninstall();
		}
	}
	
	public final void installModul(final IEditorInstallable installable) {
		fInstallables.add(installable);
		if (fIsConfigured) {
			installable.install(fEditor);
		}
	}
	
	protected final void reconfigureSourceViewer() {
		if (fIsConfigured) {
			fIsConfigured = false;
			fEditor.getSourceViewer().unconfigure();
			fIsConfigured = true;
			fEditor.getSourceViewer().configure(fConfiguration);
		}
	}
	
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
	}
	
}
