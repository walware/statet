/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ltk.ui.sourceediting.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.InfoHoverRegistry;
import de.walware.ecommons.ltk.ui.util.CombinedPreferenceStore;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesManageListener;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.nico.core.ConsoleInstanceScope;
import de.walware.statet.nico.core.NicoCore;

import de.walware.statet.r.codegeneration.RCodeTemplatesContextType;
import de.walware.statet.r.codegeneration.RdCodeTemplatesContextType;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.internal.ui.editors.RDocumentProvider;
import de.walware.statet.r.internal.ui.editors.RdDocumentProvider;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.REditorOptions;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;


/**
 * The main plugin class to be used in the desktop.
 */
public class RUIPlugin extends AbstractUIPlugin {
	
	
	public static final int INTERNAL_ERROR = 100;
	public static final int IO_ERROR = 101;
	
	
	public static final String IMG_WIZBAN_NEWRDFILE = RUI.PLUGIN_ID + "/image/wizban/new.rd-file"; //$NON-NLS-1$
	public static final String IMG_WIZBAN_NEWRFILE = RUI.PLUGIN_ID + "/image/wizban/new.r-file"; //$NON-NLS-1$
	public static final String IMG_WIZBAN_NEWRPROJECT = RUI.PLUGIN_ID + "/image/wizban/new.r-project"; //$NON-NLS-1$
	
	public static final String IMG_LOCTOOL_FILTER_GENERAL = RUI.PLUGIN_ID + "/image/ltool/filter.general"; //$NON-NLS-1$
	public static final String IMG_LOCTOOL_FILTER_LOCAL = RUI.PLUGIN_ID + "/image/ltool/filter.local"; //$NON-NLS-1$
	
	public static final String IMG_LOCTOOL_REFRESH_RECOMMENDED = RUI.PLUGIN_ID + "/image/ltool/refresh.recommended"; //$NON-NLS-1$
	
	private static final String R_CODE_TEMPLATES_KEY  = "de.walware.statet.r.ui.text.r_code_templates"; //$NON-NLS-1$
	private static final String RD_CODE_TEMPLATES_KEY = "de.walware.statet.r.ui.text.rd_code_templates"; //$NON-NLS-1$
	private static final String R_EDITOR_TEMPLATES_KEY  = "de.walware.statet.r.ui.text.r_editor_templates"; //$NON-NLS-1$
	
	
	public static boolean isSearchPlugInActivated() {
		return Platform.getBundle("org.eclipse.search").getState() == Bundle.ACTIVE; //$NON-NLS-1$
	}
	
	
	//The shared instance.
	private static RUIPlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the plug-in instance
	 */
	public static RUIPlugin getDefault() {
		return gPlugin;
	}
	
	public static void logError(final int code, final String message, final Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, RUI.PLUGIN_ID, code, message, e));
	}
	
	
	private RDocumentProvider fRDocumentProvider;
	private RdDocumentProvider fRdDocumentProvider;
	
	private IPreferenceStore fEditorPreferenceStore;
	
	private RIdentifierGroups fIdentifierGroups;
	private REditorOptions fEditorSettings;
	private REditorOptions fConsoleSettings;
	private List<PreferencesManageListener> fPrefUpdaters;
	
	private TemplateStore fRCodeTemplatesStore;
	private ContextTypeRegistry fRCodeTemplatesContextTypeRegistry;
	private TemplateStore fRdCodeTemplatesStore;
	private ContextTypeRegistry fRdCodeTemplatesContextTypeRegistry;
	
	private TemplateStore fREditorTemplatesStore;
	private ContextTypeRegistry fREditorContextTypeRegistry;
	
	private ContentAssistComputerRegistry fRConsoleContentAssistRegistry;
	private ContentAssistComputerRegistry fREditorContentAssistRegistry;
	private InfoHoverRegistry fREditorInfoHoverRegistry;
	
	private List<IDisposable> fDisposables;
	
	
	/**
	 * The constructor.
	 */
	public RUIPlugin() {
		gPlugin = this;
	}
	
	
	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		fPrefUpdaters = new ArrayList<PreferencesManageListener>();
		fDisposables = new ArrayList<IDisposable>();
		
		super.start(context);
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			for (final IDisposable d : fDisposables) {
				try {
					d.dispose();
				}
				catch (final Throwable e) {
					logError(-1, "Error occured when dispose module", e); 
				}
			}
			fDisposables = null;
			fRDocumentProvider = null;
			fRdDocumentProvider = null;
			fEditorPreferenceStore = null;
			fRCodeTemplatesStore = null;
			fRCodeTemplatesContextTypeRegistry = null;
			fRdCodeTemplatesStore = null;
			fRdCodeTemplatesContextTypeRegistry = null;
			fREditorTemplatesStore = null;
			fREditorContextTypeRegistry = null;
			fREditorContentAssistRegistry = null;
			fRConsoleContentAssistRegistry = null;
			fREditorInfoHoverRegistry = null;
			final Iterator<PreferencesManageListener> iter = fPrefUpdaters.iterator();
			while (iter.hasNext()) {
				iter.next().dispose();
			}
			fPrefUpdaters.clear();
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		util.register(IMG_WIZBAN_NEWRPROJECT, ImageRegistryUtil.T_WIZBAN, "new_r-project.png"); //$NON-NLS-1$
		util.register(IMG_WIZBAN_NEWRFILE, ImageRegistryUtil.T_WIZBAN, "new_r-file.png");  //$NON-NLS-1$
		util.register(IMG_WIZBAN_NEWRDFILE, ImageRegistryUtil.T_WIZBAN, "new_rd-file.png");  //$NON-NLS-1$
		
		util.register(RUI.IMG_OBJ_R_SCRIPT, ImageRegistryUtil.T_OBJ, "r-file_obj.gif"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_R_RUNTIME_ENV, ImageRegistryUtil.T_OBJ, "r_env.png");  //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_R_REMOTE_ENV, ImageRegistryUtil.T_OBJ, "r_env-remote.png");  //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_R_PACKAGE, ImageRegistryUtil.T_OBJ, "package.png");  //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_R_HELP_PAGE, ImageRegistryUtil.T_OBJ, "rhelp-page.png");  //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_R_HELP_SEARCH, ImageRegistryUtil.T_TOOL, "rhelp-search.png");  //$NON-NLS-1$
		
		util.register(RUI.IMG_OBJ_COMMON_FUNCTION, ImageRegistryUtil.T_OBJ, "function.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_COMMON_LOCAL_FUNCTION, ImageRegistryUtil.T_OBJ, "function-local.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_GENERIC_FUNCTION, ImageRegistryUtil.T_OBJ, "generic_function.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_METHOD, ImageRegistryUtil.T_OBJ, "method.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_GENERAL_VARIABLE, ImageRegistryUtil.T_OBJ, "var.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_GENERAL_LOCAL_VARIABLE, ImageRegistryUtil.T_OBJ, "var-local.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_SLOT, ImageRegistryUtil.T_OBJ, "slot.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_PACKAGEENV, ImageRegistryUtil.T_OBJ, "packageenv.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_GLOBALENV, ImageRegistryUtil.T_OBJ, "globalenv.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_EMPTYENV, ImageRegistryUtil.T_OBJ, "emptyenv.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_OTHERENV, ImageRegistryUtil.T_OBJ, "otherenv.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_DATAFRAME, ImageRegistryUtil.T_OBJ, "dataframe.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_DATAFRAME_COLUMN, ImageRegistryUtil.T_OBJ, "dataframe_col.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_LIST, ImageRegistryUtil.T_OBJ, "list.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_VECTOR, ImageRegistryUtil.T_OBJ, "vector.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_ARRAY, ImageRegistryUtil.T_OBJ, "array.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_NULL, ImageRegistryUtil.T_OBJ, "null.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_MISSING, ImageRegistryUtil.T_OBJ, "missing.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_S4OBJ, ImageRegistryUtil.T_OBJ, "s4obj.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_S4OBJ_VECTOR, ImageRegistryUtil.T_OBJ, "s4obj-vector.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_S4OBJ_DATAFRAME_COLUMN, ImageRegistryUtil.T_OBJ, "s4obj-dataframe_col.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_ARGUMENT_ASSIGN, ImageRegistryUtil.T_OBJ, "arg-assign.png"); //$NON-NLS-1$
		
		util.register(RUI.IMG_OBJ_LIBRARY_GROUP, ImageRegistryUtil.T_OBJ, "library.png"); //$NON-NLS-1$
		util.register(RUI.IMG_OBJ_LIBRARY_LOCATION, ImageRegistryUtil.T_OBJ, "package_folder.png"); //$NON-NLS-1$
		
		util.register(IMG_LOCTOOL_FILTER_GENERAL, ImageRegistryUtil.T_LOCTOOL, "filter-general.png"); //$NON-NLS-1$
		util.register(IMG_LOCTOOL_FILTER_LOCAL, ImageRegistryUtil.T_LOCTOOL, "filter-local.png"); //$NON-NLS-1$
		util.register(RUI.IMG_LOCTOOL_SORT_PACKAGE, ImageRegistryUtil.T_LOCTOOL, "sort-package.png"); //$NON-NLS-1$
		
		util.register(IMG_LOCTOOL_REFRESH_RECOMMENDED, ImageRegistryUtil.T_LOCTOOL, "refresh-rec.png"); //$NON-NLS-1$
	}
	
	
	public synchronized RDocumentProvider getRDocumentProvider() {
		if (fRDocumentProvider == null) {
			fRDocumentProvider = new RDocumentProvider();
		}
		return fRDocumentProvider;
	}
	
	public synchronized RdDocumentProvider getRdDocumentProvider() {
		if (fRdDocumentProvider == null) {
			fRdDocumentProvider = new RdDocumentProvider();
		}
		return fRdDocumentProvider;
	}
	
	
	public IPreferenceStore getEditorPreferenceStore() {
		if (fEditorPreferenceStore == null) {
			fEditorPreferenceStore = CombinedPreferenceStore.createStore(
					getPreferenceStore(),
					StatetUIServices.getBaseUIPreferenceStore(),
					EditorsUI.getPreferenceStore() );
		}
		return fEditorPreferenceStore;
	}
	
	public synchronized RIdentifierGroups getRIdentifierGroups() {
		if (fIdentifierGroups == null) {
			fIdentifierGroups = new RIdentifierGroups();
			fPrefUpdaters.add(new PreferencesManageListener(
					fIdentifierGroups, PreferencesUtil.getInstancePrefs(), RIdentifierGroups.GROUP_ID));
		}
		return fIdentifierGroups;
	}
	
	public synchronized REditorOptions getREditorSettings(final IPreferenceAccess prefs) {
		final IScopeContext[] contexts = prefs.getPreferenceContexts();
		for (int i = 0; i < contexts.length; i++) {
			if (contexts[i].getName().equals(ConsoleInstanceScope.SCOPE)) {
				if (fConsoleSettings== null) {
					fConsoleSettings = new REditorOptions(1);
					fPrefUpdaters.add(new PreferencesManageListener(
							fConsoleSettings, NicoCore.getInstanceConsolePreferences(), REditorOptions.GROUP_ID));
				}
				return fConsoleSettings;
			}
			if (contexts[i].getName().equals(InstanceScope.SCOPE)) {
				if (fEditorSettings == null) {
					fEditorSettings = new REditorOptions(1);
					fPrefUpdaters.add(new PreferencesManageListener(
							fEditorSettings, PreferencesUtil.getInstancePrefs(), REditorOptions.GROUP_ID));
				}
				return fEditorSettings;
			}
		}
		return null;
	}
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getRCodeGenerationTemplateContextRegistry() {
		if (fRCodeTemplatesContextTypeRegistry == null) {
			fRCodeTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
			
			RCodeTemplatesContextType.registerContextTypes(fRCodeTemplatesContextTypeRegistry);
		}
		return fRCodeTemplatesContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getRCodeGenerationTemplateStore() {
		if (fRCodeTemplatesStore == null) {
			fRCodeTemplatesStore = new ContributionTemplateStore(
					getRCodeGenerationTemplateContextRegistry(), getPreferenceStore(), R_CODE_TEMPLATES_KEY);
			try {
				fRCodeTemplatesStore.load();
			} catch (final IOException e) {
				RUIPlugin.logError(IO_ERROR, "Error occured when loading 'R code generation' template store.", e); 
			}
		}
		return fRCodeTemplatesStore;
	}
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getRdCodeGenerationTemplateContextRegistry() {
		if (fRdCodeTemplatesContextTypeRegistry == null) {
			fRdCodeTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
			
			RdCodeTemplatesContextType.registerContextTypes(fRdCodeTemplatesContextTypeRegistry);
		}
		return fRdCodeTemplatesContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getRdCodeGenerationTemplateStore() {
		if (fRdCodeTemplatesStore == null) {
			fRdCodeTemplatesStore = new ContributionTemplateStore(
					getRdCodeGenerationTemplateContextRegistry(), getPreferenceStore(), RD_CODE_TEMPLATES_KEY);
			try {
				fRdCodeTemplatesStore.load();
			} catch (final IOException e) {
				RUIPlugin.logError(IO_ERROR, "Error occured when loading 'Rd code generation' template store.", e); 
			}
		}
		return fRdCodeTemplatesStore;
	}
	
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getREditorTemplateContextRegistry() {
		if (fREditorContextTypeRegistry == null) {
			fREditorContextTypeRegistry = new ContributionContextTypeRegistry();
			
			REditorTemplatesContextType.registerContextTypes(fREditorContextTypeRegistry);
		}
		return fREditorContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getREditorTemplateStore() {
		if (fREditorTemplatesStore == null) {
			fREditorTemplatesStore = new ContributionTemplateStore(
					getREditorTemplateContextRegistry(), getPreferenceStore(), R_EDITOR_TEMPLATES_KEY);
			try {
				fREditorTemplatesStore.load();
			} catch (final IOException e) {
				RUIPlugin.logError(IO_ERROR, "Error occured when loading 'R Editor' template store.", e); 
			}
		}
		return fREditorTemplatesStore;
	}
	
	public synchronized ContentAssistComputerRegistry getREditorContentAssistRegistry() {
		if (fREditorContentAssistRegistry == null) {
			fREditorContentAssistRegistry = new ContentAssistComputerRegistry(IRSourceUnit.R_CONTENT,
					RUIPreferenceInitializer.REDITOR_NODE, RUIPreferenceInitializer.REDITOR_ASSIST_GROUP_ID);
			fDisposables.add(fREditorContentAssistRegistry);
		}
		return fREditorContentAssistRegistry;
	}
	
	public synchronized ContentAssistComputerRegistry getRConsoleContentAssistRegistry() {
		if (fRConsoleContentAssistRegistry == null) {
			fRConsoleContentAssistRegistry = new ContentAssistComputerRegistry(IRSourceUnit.R_CONTENT+"Console",
					RUIPreferenceInitializer.RCONSOLE_NODE, RUIPreferenceInitializer.RCONSOLE_ASSIST_GROUP_ID);
			fDisposables.add(fRConsoleContentAssistRegistry);
		}
		return fRConsoleContentAssistRegistry;
	}
	
	public synchronized InfoHoverRegistry getREditorInfoHoverRegistry() {
		if (fREditorInfoHoverRegistry == null) {
			fREditorInfoHoverRegistry = new InfoHoverRegistry(IRSourceUnit.R_CONTENT,
					RUIPreferenceInitializer.REDITOR_NODE, RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID);
			fDisposables.add(fREditorInfoHoverRegistry);
		}
		return fREditorInfoHoverRegistry;
	}
	
	public void registerPluginDisposable(final IDisposable d) {
		final List<IDisposable> disposables = fDisposables;
		if (disposables != null) {
			disposables.add(d);
		}
		else {
			throw new IllegalStateException();
		}
	}
	
}
