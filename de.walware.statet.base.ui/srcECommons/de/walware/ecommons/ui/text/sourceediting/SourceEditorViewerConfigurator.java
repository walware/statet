/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.walware.ecommons.text.PartitioningConfiguration;
import de.walware.ecommons.ui.text.PairMatcher;
import de.walware.ecommons.ui.util.ISettingsChangedHandler;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


/**
 * 
 */
public abstract class SourceEditorViewerConfigurator implements ISettingsChangedHandler {
	
	
	private PairMatcher fPairMatcher;
	private IPreferenceStore fPreferenceStore;
	private SourceEditorViewerConfiguration fConfiguration;
	private ISourceEditor fSourceEditor;
	private final List<ISourceEditorAddon> fAddons = new ArrayList<ISourceEditorAddon>();
	
	protected boolean fIsConfigured;
	
	
	protected SourceEditorViewerConfigurator() {
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
	
	public abstract PartitioningConfiguration getPartitioning();
	
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
	
	
	protected void setConfiguration(final SourceEditorViewerConfiguration config) {
		fConfiguration = config;
	}
	
	public SourceEditorViewerConfiguration getSourceViewerConfiguration() {
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
	
	
	public void setTarget(final ISourceEditor sourceEditor, final boolean configure) {
		assert (sourceEditor != null);
		fSourceEditor = sourceEditor;
		if (configure) {
			fSourceEditor.getViewer().getControl().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(final DisposeEvent e) {
					if (fIsConfigured) {
						uninstallCurrentAddons();
					}
				}
			});
			configureTarget();
		}
		else {
			installCurrentAddons();
		}
		handleSettingsChanged(null, null);
	}
	
	protected ISourceViewer getSourceViewer() {
		if (fSourceEditor != null) {
			return fSourceEditor.getViewer();
		}
		return null;
	}
	
	public final void unconfigureTarget() {
		if (fSourceEditor != null) {
			fIsConfigured = false;
			uninstallCurrentAddons();
			fSourceEditor.getViewer().unconfigure();
		}
	}
	
	public final void configureTarget() {
		if (fSourceEditor != null) {
			fIsConfigured = true;
			fSourceEditor.getViewer().configure(fConfiguration);
			fAddons.addAll(getSourceViewerConfiguration().getAddOns());
			installCurrentAddons();
		}
	}
	
	private void installCurrentAddons() {
		for (final ISourceEditorAddon addon : fAddons) {
			addon.install(fSourceEditor);
		}
	}
	
	private void uninstallCurrentAddons() {
		for (final ISourceEditorAddon addon : fAddons) {
			addon.uninstall();
		}
	}
	
	public final void installAddon(final ISourceEditorAddon installable) {
		fAddons.add(installable);
		if (fIsConfigured) {
			installable.install(fSourceEditor);
		}
	}
	
	protected final void reconfigureSourceViewer() {
		if (fIsConfigured) {
			fIsConfigured = false;
			fSourceEditor.getViewer().unconfigure();
			fIsConfigured = true;
			fSourceEditor.getViewer().configure(fConfiguration);
		}
	}
	
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
	}
	
}
