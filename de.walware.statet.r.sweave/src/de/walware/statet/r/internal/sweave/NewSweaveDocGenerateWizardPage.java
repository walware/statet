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

package de.walware.statet.r.internal.sweave;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import de.walware.ecommons.ltk.ui.templates.TemplateSelectionComposite;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.docmlet.tex.core.TexCore;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.sweave.TexRweaveCoreAccess;


public class NewSweaveDocGenerateWizardPage extends WizardPage {
	
	
	public static final String PREF_QUALIFIER = SweavePlugin.PLUGIN_ID + "/wizard"; //$NON-NLS-1$
	public static final String DEFAULT_NEWDOC_KEY = "NewDoc.Default.name"; //$NON-NLS-1$
	
	public static final Preference<String> DEFAULT_NEWDOC_PREF = new Preference.StringPref(
			PREF_QUALIFIER, DEFAULT_NEWDOC_KEY);
	
	
	private TemplateSelectionComposite fTemplateSelectComposite;
	
	private final ContextTypeRegistry fContextRegistry;
	private final TemplateStore fTemplateStore;
	
	
	protected NewSweaveDocGenerateWizardPage() {
		super("Sweave-CodeGen");
		fContextRegistry = SweavePlugin.getDefault().getSweaveDocTemplateContextRegistry();
		fTemplateStore = SweavePlugin.getDefault().getSweaveDocTemplateStore();
		
		setTitle(Messages.NewSweaveDocWizardPage_title);
		setDescription(Messages.NewSweaveDocWizardPage_description);
	}
	
	
	public Template getTemplate() {
		if (fTemplateSelectComposite != null) {
			return fTemplateSelectComposite.getSelectedTemplate();
		}
		else {
			return fTemplateStore.findTemplate(getDefaultTemplate(),
					LtxRweaveTemplatesContextType.NEW_RWEAVETEX_CONTEXTTYPE );
		}
	}
	
	private String getDefaultTemplate() {
		return PreferencesUtil.getInstancePrefs().getPreferenceValue(DEFAULT_NEWDOC_PREF);
	}
	
	@Override
	public void createControl(final Composite parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		group.setText(Messages.NewSweaveDocWizardPage_Template_group);
		
		fTemplateSelectComposite = new TemplateSelectionComposite(group);
		fTemplateSelectComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fTemplateSelectComposite.setConfigurator(new LtxRweaveTemplateConfigurator(
				new TexRweaveCoreAccess(TexCore.getWorkbenchAccess(), RCore.getWorkbenchAccess()),
				fTemplateSelectComposite.getPreview().getTemplateVariableProcessor() ));
		final Template[] templates = fTemplateStore.getTemplates(
				LtxRweaveTemplatesContextType.NEW_RWEAVETEX_CONTEXTTYPE );
		fTemplateSelectComposite.setInput(templates, true, fContextRegistry);
		fTemplateSelectComposite.setSelection(getDefaultTemplate());
		
		setControl(group);
	}
	
}
