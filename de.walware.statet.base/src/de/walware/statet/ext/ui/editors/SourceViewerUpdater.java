/*******************************************************************************
 * Copyright (c) 2000-2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink
 *******************************************************************************/

// Org: org.eclipse.jdt.internal.ui.preferences.JavaSourcePreviewerUpdater

package de.walware.statet.ext.ui.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;


/**
 * Handles editor font and properties changes for source viewers.
 * <p>
 * It disposes itself automatically.
 */
public class SourceViewerUpdater {


	private IPreferenceStore fPreferenceStore;
	private IPropertyChangeListener fFontChangeListener;
	private IPropertyChangeListener fPropertyChangeListener;
	
	/**
	 * Creates a source preview updater for the given viewer, configuration and preference store from configuration.
	 *
	 * @param viewer the viewer
	 * @param configuration the configuration
	 */
	public SourceViewerUpdater(SourceViewer viewer, StatextSourceViewerConfiguration configuration) {
		
		this(viewer, configuration, configuration.getPreferences());
	}

	/**
	 * Creates a source preview updater for the given viewer, configuration and preference store.
	 *
	 * @param viewer the viewer
	 * @param configuration the configuration
	 * @param preferenceStore the preference store
	 */
	public SourceViewerUpdater(final SourceViewer viewer, final StatextSourceViewerConfiguration configuration, final IPreferenceStore preferenceStore) {
		
		assert (viewer != null);
		assert (configuration != null);
		assert (preferenceStore != null);
		
		fPreferenceStore = preferenceStore;
		
		fFontChangeListener = new IPropertyChangeListener() {
			/*
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(JFaceResources.TEXT_FONT)) {
					Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
					viewer.getTextWidget().setFont(font);
				}
			}
		};
		fPropertyChangeListener= new IPropertyChangeListener() {
			/*
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				if (configuration.affectsTextPresentation(event)) {
					configuration.handlePropertyChangeEvent(event);
					viewer.invalidateTextPresentation();
				}
			}
		};
		viewer.getTextWidget().addDisposeListener(new DisposeListener() {
			/*
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				unregister();
			}
		});

		JFaceResources.getFontRegistry().addListener(fFontChangeListener);
		preferenceStore.addPropertyChangeListener(fPropertyChangeListener);
	}

	public synchronized void unregister() {
		
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
			JFaceResources.getFontRegistry().removeListener(fFontChangeListener);
		}
		fPreferenceStore = null;
		fPropertyChangeListener = null;
		fFontChangeListener = null;
	}
	
}
