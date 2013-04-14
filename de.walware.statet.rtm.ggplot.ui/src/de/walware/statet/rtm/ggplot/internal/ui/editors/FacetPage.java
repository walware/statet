/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.TableWrapData;

import de.walware.statet.rtm.base.ui.editors.RTaskEditor;
import de.walware.statet.rtm.base.ui.editors.RTaskFormPage;


public class FacetPage extends RTaskFormPage {
	
	
	private FacetSection fFacetSection;
	
	
	public FacetPage(final RTaskEditor editor) {
		super(editor, GGPlotEditor.FACET_PAGE_ID, "Layout (Facets)");
	}
	
	
	@Override
	protected void createFormBodyContent(final Composite body) {
		fFacetSection = new FacetSection(this, body);
		fFacetSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		registerSection(fFacetSection);
	}
	
}
