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

// Org: org.eclipse.jdt.internal.ui.viewsupport.ProjectTemplateStore

package de.walware.statet.internal.ui.preferences;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import de.walware.statet.base.StatetPlugin;


public final class CodeGenerationTemplatesStore {
	
	
	public static final String KEY = "de.walware.statet.base.ui.text.custom_code_templates"; //$NON-NLS-1$

	private final TemplateStore[] fInstanceStores;
	private final TemplateStore fProjectStore;

	
	public CodeGenerationTemplatesStore(IProject project, TemplateStore[] pluginStores) {
		
		fInstanceStores = pluginStores;
		
		if (project == null) {
			fProjectStore = null;
		} else {
			final IPreferenceStore projectSettings = new ScopedPreferenceStore(new ProjectScope(project), StatetPlugin.ID);
			fProjectStore = new TemplateStore(projectSettings, KEY) {
				/*
				 * Make sure we keep the id of added code templates - add removes
				 * it in the usual add() method
				 */
				public void add(TemplatePersistenceData data) {
					
					internalAdd(data);
				}
				
				public void save() throws IOException {
					
					StringWriter output = new StringWriter();
					TemplateReaderWriter writer = new TemplateReaderWriter();
					writer.save(getTemplateData(false), output);
					
					projectSettings.setValue(KEY, output.toString());
				}
			};
		}
	}
	
	public TemplatePersistenceData[] getAllTemplateData() {
		
		if (fProjectStore != null) {
			return fProjectStore.getTemplateData(true);
		} else {
			int length = 0;
			TemplatePersistenceData[][] datas = new TemplatePersistenceData[fInstanceStores.length][];
			for (int i = 0; i < fInstanceStores.length; i++) {
				datas[i] = fInstanceStores[i].getTemplateData(true);
				length += datas[i].length;
			}
			TemplatePersistenceData[] allData = new TemplatePersistenceData[length];
			
			for (int i = 0, k = 0; i < datas.length; k += datas[i].length, i++)
				System.arraycopy(datas[i], 0, allData, 0, datas[i].length);
			
			return allData;
		}
	}

	public TemplatePersistenceData[] getTemplateData(int categoryIndex) {
		
		if (fProjectStore != null) {
			TemplatePersistenceData[] data = fInstanceStores[categoryIndex].getTemplateData(true);
			TemplatePersistenceData[] allProjectData = fProjectStore.getTemplateData(true);
			
			for (int i = 0; i < data.length; i++) {
				SEARCH_IN_PROJECT: for (int j = 0; j < allProjectData.length; j++) {
					if (data[i].getId().equals(allProjectData[j].getId() )) {
						data[i] = allProjectData[j];
						break SEARCH_IN_PROJECT;
					}
				}
			}
			return data;
		} else {
			return fInstanceStores[categoryIndex].getTemplateData(true);
		}
	}
	
	public Template findTemplateById(String id) {
		
		Template template = null;

		if (fProjectStore != null)
			template = fProjectStore.findTemplateById(id);
		
		for (int i = 0; template != null && i < fInstanceStores.length; i++) {
				template = fInstanceStores[i].findTemplateById(id);
		}
		
		return template;
	}
	
	public void load() throws IOException {
		
		if (fProjectStore != null) {
			fProjectStore.load();
			
			Set<String> collectedDatas = new HashSet<String>();
			TemplatePersistenceData[] datas = fProjectStore.getTemplateData(false);
			for (int i= 0; i < datas.length; i++) {
				collectedDatas.add(datas[i].getId());
			}
			
			for (int i = 0; i < fInstanceStores.length; i++) {
				datas = fInstanceStores[i].getTemplateData(false);
				
				for (int j = 0; j < datas.length; j++) {
					TemplatePersistenceData orig = datas[j];
					if (!collectedDatas.contains(orig.getId())) {
						TemplatePersistenceData copy = new TemplatePersistenceData(new Template(orig.getTemplate()), orig.isEnabled(), orig.getId());
						fProjectStore.add(copy);
						copy.setDeleted(true);
					}
				}
			}
		}
	}
	
	public boolean isProjectSpecific(String id) {
		
		if (id == null) {
			return false;
		}
		
		if (fProjectStore == null)
			return false;
		
		return fProjectStore.findTemplateById(id) != null;
	}
	
	public void setProjectSpecific(String id, boolean projectSpecific) {
		
		Assert.isNotNull(fProjectStore);
		
		TemplatePersistenceData data = fProjectStore.getTemplateData(id);
		if (data == null) {
			return; // does not exist
		} else {
			data.setDeleted(!projectSpecific);
		}
	}

	public void restoreDefaults() {
		
		if (fProjectStore == null) {
			for (int i = 0; i < fInstanceStores.length; i++)
				fInstanceStores[i].restoreDefaults();
		} else {
			fProjectStore.restoreDefaults();
		}
	}
	
	public void save() throws IOException {
		
		if (fProjectStore == null) {
			for (int i = 0; i < fInstanceStores.length; i++)
				fInstanceStores[i].save();
		} else {
			fProjectStore.save();
		}
	}
	
	public void revertChanges() throws IOException {
		
		if (fProjectStore != null) {
			// nothing to do
		} else {
			for (int i = 0; i < fInstanceStores.length; i++)
				fInstanceStores[i].load();
		}
	}
	
	
	public static boolean hasProjectSpecificTempates(IProject project) {
		
		String pref = new ProjectScope(project).getNode(StatetPlugin.ID).get(KEY, null);
		if (pref != null && pref.trim().length() > 0) {
			Reader input = new StringReader(pref);
			TemplateReaderWriter reader= new TemplateReaderWriter();
			TemplatePersistenceData[] datas;
			try {
				datas= reader.read(input);
				return datas.length > 0;
			} catch (IOException e) {
				// ignore
			}
		}
		return false;
	}
	
}
