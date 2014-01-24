/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import de.walware.ecommons.emf.ui.forms.EFLayoutUtil;
import de.walware.ecommons.emf.ui.forms.MasterDetailComposite;

import de.walware.statet.rtm.base.ui.editors.RTaskEditor;
import de.walware.statet.rtm.base.ui.editors.RTaskFormPage;


public class MainPage extends RTaskFormPage {
	
	
	private PlotDataSection fMainDataSection;
	private PlotLabelSection fLabelSection;
	
	private LayersSection fLayersSection;
	
	
	public MainPage(final RTaskEditor editor) {
		super(editor);
	}
	
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		
		setSelectionProvider(fLayersSection.getSelectionProvider());
	}
	
	@Override
	protected void createFormBodyContent(final Composite body) {
		{	final Composite composite = addBodyComposite();
			composite.setLayout(EFLayoutUtil.createMainColumnLayout());
			
			fMainDataSection = new PlotDataSection(this, composite);
			fMainDataSection.getSection().setLayoutData(new ColumnLayoutData(300));
			registerSection(fMainDataSection);
			
			fLabelSection = new PlotLabelSection(this, composite);
			fLabelSection.getSection().setLayoutData(new ColumnLayoutData(300));
			registerSection(fLabelSection);
		}
		{	final MasterDetailComposite composite = addBodySashComposite();
			
			fLayersSection = new LayersSection(this, composite.getMasterContainer());
			registerSection(fLayersSection);
			
			fLayersSection.createDetails(composite.getDetailContainer());
			
			composite.layout();
		}
	}
	
}
