/*=============================================================================#
 # Copyright (c) 2000-2015 IBM Corporation and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     IBM Corporation - initial API and implementation in JDT
 #     Stephan Wahlbrink - adaptations to StatET
 #=============================================================================*/

// ORG: org.eclipse.jdt.internal.ui.viewsupport.ProjectTemplateStore

package de.walware.statet.base.internal.ui.preferences;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import de.walware.ecommons.preferences.ui.ScopedPreferenceStore;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public final class CodeGenerationTemplatesStore {
	
	
	public static final String KEY = "de.walware.statet.base.ui.text.custom_code_templates"; //$NON-NLS-1$
	
	public static boolean hasProjectSpecificTempates(final IProject project) {
		final String pref = new ProjectScope(project).getNode(StatetUIPlugin.PLUGIN_ID).get(KEY, null);
		if (pref != null && pref.trim().length() > 0) {
			final Reader input = new StringReader(pref);
			final TemplateReaderWriter reader= new TemplateReaderWriter();
			TemplatePersistenceData[] datas;
			try {
				datas = reader.read(input);
				return datas.length > 0;
			}
			catch (final IOException e) {
				// ignore
			}
		}
		return false;
	}
	
	
	private final TemplateStore[] fInstanceStores;
	private final TemplateStore fProjectStore;
	private boolean fDisableProjectSettings;
	
	private final String fPreferenceQualifier;
	private final String fPreferenceKey;
	
	
	public CodeGenerationTemplatesStore(final IProject project, final TemplateStore[] pluginStores) {
		fPreferenceQualifier = StatetUIPlugin.PLUGIN_ID;
		fPreferenceKey = KEY;
		
		fInstanceStores = pluginStores;
		
		if (project == null) {
			fProjectStore = null;
		} else {
			final ScopedPreferenceStore projectSettings = new ScopedPreferenceStore(new ProjectScope(project), fPreferenceQualifier);
			fProjectStore = new TemplateStore(projectSettings, fPreferenceKey) {
				/*
				 * Make sure we keep the id of added code templates - add removes
				 * it in the usual add() method
				 */
				@Override
				public void add(final TemplatePersistenceData data) {
					internalAdd(data);
				}
				
				@Override
				public void save() throws IOException {
					if (fDisableProjectSettings) {
						projectSettings.setValue(fPreferenceKey, null);
					}
					else {
						final StringWriter output = new StringWriter();
						final TemplateReaderWriter writer = new TemplateReaderWriter();
						writer.save(getTemplateData(false), output);
						
						projectSettings.setValue(fPreferenceKey, output.toString());
					}
					if (projectSettings.needsSaving()) {
						projectSettings.save();
					}
				}
			};
		}
	}
	
	
	public TemplatePersistenceData[] getAllTemplateData() {
		if (fProjectStore != null) {
			return fProjectStore.getTemplateData(true);
		} else {
			int length = 0;
			final TemplatePersistenceData[][] datas = new TemplatePersistenceData[fInstanceStores.length][];
			for (int i = 0; i < fInstanceStores.length; i++) {
				datas[i] = fInstanceStores[i].getTemplateData(true);
				length += datas[i].length;
			}
			final TemplatePersistenceData[] allData = new TemplatePersistenceData[length];
			
			for (int i = 0, k = 0; i < datas.length; k += datas[i].length, i++) {
				System.arraycopy(datas[i], 0, allData, k, datas[i].length);
			}
			
			return allData;
		}
	}
	
	public TemplatePersistenceData[] getTemplateData(final int categoryIndex) {
		if (fProjectStore != null) {
			final TemplatePersistenceData[] data = fInstanceStores[categoryIndex].getTemplateData(true);
			final TemplatePersistenceData[] allProjectData = fProjectStore.getTemplateData(true);
			
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
	
	public Template findTemplateById(final String id) {
		Template template = null;
		if (fProjectStore != null) {
			template = fProjectStore.findTemplateById(id);
		}
		for (int i = 0; template == null && i < fInstanceStores.length; i++) {
			template = fInstanceStores[i].findTemplateById(id);
		}
		return template;
	}
	
	public TemplatePersistenceData getTemplateData(final String id) {
		TemplatePersistenceData data = null;
		if (fProjectStore != null) {
			data = fProjectStore.getTemplateData(id);
		}
		for (int i = 0; data == null && i < fInstanceStores.length; i++) {
			data = fInstanceStores[i].getTemplateData(id);
		}
		return data;
	}
	
	public TemplatePersistenceData getTemplateData(final int categoryIndex, final String id) {
		TemplatePersistenceData data = null;
		if (fProjectStore != null) {
			data = fProjectStore.getTemplateData(id);
		}
		if (data == null) {
			data = fInstanceStores[categoryIndex].getTemplateData(id);
		}
		return data;
	}
	
	public void load() throws IOException {
		if (fProjectStore != null) {
			fProjectStore.load();
			
			final Set<String> collectedDatas= new HashSet<>();
			TemplatePersistenceData[] datas = fProjectStore.getTemplateData(false);
			for (int i = 0; i < datas.length; i++) {
				collectedDatas.add(datas[i].getId());
			}
			
			for (int i = 0; i < fInstanceStores.length; i++) {
				datas = fInstanceStores[i].getTemplateData(false);
				
				for (int j = 0; j < datas.length; j++) {
					final TemplatePersistenceData orig = datas[j];
					if (!collectedDatas.contains(orig.getId())) {
						final TemplatePersistenceData copy = new TemplatePersistenceData(new Template(orig.getTemplate()), orig.isEnabled(), orig.getId());
						fProjectStore.add(copy);
						copy.setDeleted(true);
					}
				}
			}
		}
	}
	
	public boolean isProjectSpecific(final String id) {
		if (id == null) {
			return false;
		}
		
		if (fProjectStore == null) {
			return false;
		}
		else {
			final TemplatePersistenceData data = fProjectStore.getTemplateData(id);
			return (data != null && !data.isDeleted());
		}
	}
	
	/**
	 * Setting to globally enable/disable project settings.
	 * If enabled, template based settings are still required.
	 */
	public void setProjectSpecific(final boolean projectSpecific) {
		assert (fProjectStore != null);
		
		fDisableProjectSettings = !projectSpecific;
	}
	
	public void setProjectSpecific(final String id, final boolean projectSpecific) {
		assert (fProjectStore != null);
		
		final TemplatePersistenceData data = fProjectStore.getTemplateData(id);
		if (data != null) {
			data.setDeleted(!projectSpecific);
		}
	}
	
	public void restoreDefaults() {
		if (fProjectStore == null) {
			for (int i = 0; i < fInstanceStores.length; i++) {
				fInstanceStores[i].restoreDefaults();
			}
		}
		else {
			try {
				load();
			}
			catch (final IOException e) {
				StatetUIPlugin.logUnexpectedError(e);
			}
		}
	}
	
	public void save() throws IOException {
		if (fProjectStore == null) {
			for (int i = 0; i < fInstanceStores.length; i++) {
				fInstanceStores[i].save();
			}
		}
		else {
			fProjectStore.save();
		}
	}
	
	public void revertChanges() throws IOException {
		if (fProjectStore == null) {
			for (int i = 0; i < fInstanceStores.length; i++) {
				fInstanceStores[i].load();
			}
		}
		else {
			// nothing to do
		}
	}
	
}
