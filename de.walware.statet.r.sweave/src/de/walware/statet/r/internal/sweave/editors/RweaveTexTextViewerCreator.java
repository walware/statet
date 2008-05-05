/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.base.ui.sourceeditors.CompareTextViewer;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.sweave.SweavePlugin;


public class RweaveTexTextViewerCreator implements IViewerCreator {
	
	public Viewer createViewer(final Composite parent, final CompareConfiguration config) {
		final RweaveTexSourceViewerConfigurator viewerConfigurator = new RweaveTexSourceViewerConfigurator(
				RCore.getWorkbenchAccess(), SweavePlugin.getDefault().getEditorRTexPreferenceStore());
		viewerConfigurator.setConfiguration(new RweaveTexSourceViewerConfiguration(
				viewerConfigurator, viewerConfigurator.getPreferenceStore(), StatetUIServices.getSharedColorManager()));
		return new CompareTextViewer(parent, config, viewerConfigurator);
	}
	
}
