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

package de.walware.statet.rtm.ftable.internal.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.TableWrapData;

import de.walware.ecommons.emf.ui.forms.EFLayoutUtil;

import de.walware.statet.rtm.base.ui.editors.RTaskEditor;
import de.walware.statet.rtm.base.ui.editors.RTaskFormPage;


public class MainPage extends RTaskFormPage {
	
	
	private TableDataSection fMainDataSection;
	private TableVarsSection fVarsSection;
	
	
	public MainPage(final RTaskEditor editor) {
		super(editor);
	}
	
	
	@Override
	protected void createFormBodyContent(final Composite body) {
		{	final Composite composite = addBodyComposite();
			composite.setLayout(EFLayoutUtil.createMainColumnLayout());
			
			fMainDataSection = new TableDataSection(this, composite);
			fMainDataSection.getSection().setLayoutData(new ColumnLayoutData(300));
			registerSection(fMainDataSection);
		}
		{	fVarsSection = new TableVarsSection(this, body);
			fVarsSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
			registerSection(fVarsSection);
		}
	}
	
}
