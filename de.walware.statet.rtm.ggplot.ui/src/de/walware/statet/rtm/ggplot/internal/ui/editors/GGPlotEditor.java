/**
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 */
package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ui.PartInitException;

import de.walware.statet.rtm.base.ui.editors.RTaskEditor;
import de.walware.statet.rtm.base.ui.rexpr.HyperlinkType;
import de.walware.statet.rtm.base.ui.rexpr.HyperlinkType.IHyperlinkProvider;
import de.walware.statet.rtm.base.ui.rexpr.HyperlinkType.PropertyTabLink;
import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;
import de.walware.statet.rtm.ggplot.ui.RtGGPlotDescriptor;


public class GGPlotEditor extends RTaskEditor {
	
	private static final IHyperlinkProvider LINK_DESCRIPTORS = new IHyperlinkProvider() {
		
		private final PropertyTabLink mainTitle = new PropertyTabLink("Text Style...",
				"de.walware.statet.rtm.ggplot.properties.MainTitleStyleTab"); //$NON-NLS-1$
		private final PropertyTabLink axXLabel = new PropertyTabLink("Text Style...",
				"de.walware.statet.rtm.ggplot.properties.AxXLabelStyleTab"); //$NON-NLS-1$
		private final PropertyTabLink axYLabel = new PropertyTabLink("Text Style...",
				"de.walware.statet.rtm.ggplot.properties.AxYLabelStyleTab"); //$NON-NLS-1$
		private final PropertyTabLink layerTextLabel = new PropertyTabLink("Text Style...",
				"de.walware.statet.rtm.ggplot.properties.LayerTextStyleTab"); //$NON-NLS-1$
		
		@Override
		public PropertyTabLink get(final EClass eClass, final EStructuralFeature eFeature) {
			if (eFeature == Literals.GG_PLOT__MAIN_TITLE) {
				return mainTitle;
			}
			if (eFeature == Literals.GG_PLOT__AX_XLABEL) {
				return axXLabel;
			}
			if (eFeature == Literals.GG_PLOT__AX_YLABEL) {
				return axYLabel;
			}
			if (eFeature == Literals.GEOM_TEXT_LAYER__LABEL) {
				return layerTextLabel;
			}
			return null;
		}
		
	};
	
	
	public static String FACET_PAGE_ID = "Facet"; //$NON-NLS-1$
	
	
	public GGPlotEditor() {
		super(RtGGPlotDescriptor.INSTANCE);
	}
	
	
	@Override
	protected IAdapterFactory createContextAdapterFactory() {
		return new ContextAdapterFactory() {
			@Override
			public Object getAdapter(final Object adaptableObject, final Class required) {
				if (required.equals(HyperlinkType.IHyperlinkProvider.class)) {
					return LINK_DESCRIPTORS;
				}
				return super.getAdapter(adaptableObject, required);
			}
		};
	}
	
	@Override
	protected void addFormPages() throws PartInitException {
		addPage(new MainPage(this));
		addPage(new FacetPage(this));
	}
	
}
