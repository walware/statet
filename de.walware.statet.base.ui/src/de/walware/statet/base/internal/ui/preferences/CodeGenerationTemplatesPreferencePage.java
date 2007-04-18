/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation in JDT
 *     Stephan Wahlbrink - adaptations to StatET
 *******************************************************************************/

package de.walware.statet.base.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.preferences.PropertyAndPreferencePage;


/**
 * The page to configure the code templates.
 */
public class CodeGenerationTemplatesPreferencePage extends PropertyAndPreferencePage<CodeGenerationTemplatesConfigurationBlock> {

	public static final String PREF_ID = "de.walware.statet.base.ui.preferencePages.CodeGenerationTemplates"; //$NON-NLS-1$
	public static final String PROP_ID = "de.walware.statet.base.ui.propertyPages.CodeGenerationTemplates"; //$NON-NLS-1$
	
//	public static final String DATA_SELECT_TEMPLATE = "CodeTemplatePreferencePage.select_template"; //$NON-NLS-1$
	
	
	public CodeGenerationTemplatesPreferencePage() {

		setPreferenceStore(StatetPlugin.getDefault().getPreferenceStore());
		
		// only used when page is shown programatically
		setTitle(Messages.CodeTemplates_title);		 
	}


	@Override
	protected String getPreferencePageID() {
		
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageID() {
		
		return PROP_ID;
	}
	
	@Override
	protected CodeGenerationTemplatesConfigurationBlock createConfigurationBlock()
			throws CoreException {
		
		return new CodeGenerationTemplatesConfigurationBlock(getProject());
	}

	
	/*
	 * @see org.eclipse.jface.preference.PreferencePage#applyData(java.lang.Object)
	 */
	public void applyData(Object data) {
//		if (data instanceof Map) {
//			Object id = ((Map) data).get(DATA_SELECT_TEMPLATE);
//			if (id instanceof String) {
//				System.out.println("todo");
//				final TemplatePersistenceData[] templates = fBlock.fTemplateStore.getTemplateData();
//				TemplatePersistenceData template = null;
//				for (int index = 0; index < templates.length; index++) {
//					template = templates[index];
//					if (template.getId().equals(id)) {
//						fBlock.postSetSelection(template);
//						break;
//					}
//				}
//			}
//		}
		super.applyData(data);
	}

	@Override
	protected boolean hasProjectSpecificSettings(IProject project) {
		
		return CodeGenerationTemplatesStore.hasProjectSpecificTempates(project);
	}

}
