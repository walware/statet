/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.internal;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.r.codegeneration.RCodeTemplatesContextType;
import de.walware.statet.r.codegeneration.RdCodeTemplatesContextType;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.RDocumentProvider;
import de.walware.statet.r.ui.editors.RdDocumentProvider;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;


/**
 * The main plugin class to be used in the desktop.
 */
public class RUIPlugin extends AbstractUIPlugin {

	
	public static final int INTERNAL_ERROR = 100;
	

	public static final String IMG_WIZBAN_NEWRDFILE = RUI.PLUGIN_ID + "/img/wizban/new.rd-file"; //$NON-NLS-1$
	public static final String IMG_WIZBAN_NEWRFILE = RUI.PLUGIN_ID + "/img/wizban/new.r-file"; //$NON-NLS-1$
	public static final String IMG_WIZBAN_NEWRPROJECT = RUI.PLUGIN_ID + "/img/wizban/new.r-project"; //$NON-NLS-1$

	
	private static final String R_CODE_TEMPLATES_KEY  = "de.walware.statet.r.ui.text.r_code_templates"; //$NON-NLS-1$
	private static final String RD_CODE_TEMPLATES_KEY = "de.walware.statet.r.ui.text.rd_code_templates"; //$NON-NLS-1$
	private static final String R_EDITOR_TEMPLATES_KEY  = "de.walware.statet.r.ui.text.r_editor_templates"; //$NON-NLS-1$
	
	
	//The shared instance.
	private static RUIPlugin gPlugin;

	private RDocumentProvider fRDocumentProvider;
	private RdDocumentProvider fRdDocumentProvider;

	private TemplateStore fRCodeTemplatesStore;
	private ContextTypeRegistry fRCodeTemplatesContextTypeRegistry;
	private TemplateStore fRdCodeTemplatesStore;
	private ContextTypeRegistry fRdCodeTemplatesContextTypeRegistry;

	private TemplateStore fREditorTemplatesStore;
	private ContextTypeRegistry fREditorContextTypeRegistry;
	
	private ImageRegistry fImageRegistry;
	
	
	/**
	 * The constructor.
	 */
	public RUIPlugin() {
		gPlugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			if (fImageRegistry != null) {
				fImageRegistry.dispose();
				fImageRegistry = null;
			}
		} finally {
			gPlugin = null;
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static RUIPlugin getDefault() {
		
		return gPlugin;
	}

	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {

		fImageRegistry = reg;
		ImageRegistryUtil util = new ImageRegistryUtil(this);
		util.register(IMG_WIZBAN_NEWRPROJECT, ImageRegistryUtil.T_WIZBAN, "new_r-project.png"); //$NON-NLS-1$
		util.register(IMG_WIZBAN_NEWRFILE, ImageRegistryUtil.T_WIZBAN, "new_r-file.png"); //$NON-NLS-1$
		util.register(IMG_WIZBAN_NEWRDFILE, ImageRegistryUtil.T_WIZBAN, "new_rd-file.png"); //$NON-NLS-1$
		util.register(RUI.IMG_RCONSOLE, ImageRegistryUtil.T_TOOL, "r-console.png"); //$NON-NLS-1$
	}
	
	
    public synchronized RDocumentProvider getRDocumentProvider() {
    	
		if (fRDocumentProvider == null)
			fRDocumentProvider = new RDocumentProvider();
		return fRDocumentProvider;
	}

    public synchronized RdDocumentProvider getRdDocumentProvider() {
    	
		if (fRdDocumentProvider == null)
			fRdDocumentProvider = new RdDocumentProvider();
		return fRdDocumentProvider;
	}
    

	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public ContextTypeRegistry getRCodeGenerationTemplateContextRegistry() {
		
		if (fRCodeTemplatesContextTypeRegistry != null) {
			return fRCodeTemplatesContextTypeRegistry;
		}
		synchronized (this) {
			if (fRCodeTemplatesContextTypeRegistry == null) {
				fRCodeTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
				
				RCodeTemplatesContextType.registerContextTypes(fRCodeTemplatesContextTypeRegistry);
			}
			return fRCodeTemplatesContextTypeRegistry;
		}
	}

	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public TemplateStore getRCodeGenerationTemplateStore() {
		
		if (fRCodeTemplatesStore != null) {
			return fRCodeTemplatesStore;
		}
		synchronized (this) {
			if (fRCodeTemplatesStore == null) {
				fRCodeTemplatesStore = new ContributionTemplateStore(
						getRCodeGenerationTemplateContextRegistry(), getPreferenceStore(), R_CODE_TEMPLATES_KEY);
				try {
					fRCodeTemplatesStore.load();
				} catch (IOException e) {
					StatetPlugin.logUnexpectedError(e);
				}
			}
			return fRCodeTemplatesStore;
		}
	}
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public ContextTypeRegistry getRdCodeGenerationTemplateContextRegistry() {
		
		if (fRdCodeTemplatesContextTypeRegistry != null) {
			return fRdCodeTemplatesContextTypeRegistry;
		}
		synchronized (this) {
			if (fRdCodeTemplatesContextTypeRegistry == null) {
				fRdCodeTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
	
				RdCodeTemplatesContextType.registerContextTypes(fRdCodeTemplatesContextTypeRegistry);
			}
			return fRdCodeTemplatesContextTypeRegistry;
		}
	}

	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public TemplateStore getRdCodeGenerationTemplateStore() {
		
		if (fRdCodeTemplatesStore != null) {
			return fRdCodeTemplatesStore;
		}
		synchronized (this) {
			if (fRdCodeTemplatesStore == null) {
				fRdCodeTemplatesStore = new ContributionTemplateStore(
						getRdCodeGenerationTemplateContextRegistry(), getPreferenceStore(), RD_CODE_TEMPLATES_KEY);
				try {
					fRdCodeTemplatesStore.load();
				} catch (IOException e) {
					StatetPlugin.logUnexpectedError(e);
				}
			}
			return fRdCodeTemplatesStore;
		}
	}


	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public ContextTypeRegistry getREditorTemplateContextRegistry() {
		
		if (fREditorContextTypeRegistry != null) {
			return fREditorContextTypeRegistry;
		}
		synchronized (this) {
			if (fREditorContextTypeRegistry == null) {
				fREditorContextTypeRegistry = new ContributionContextTypeRegistry();
				
				REditorTemplatesContextType.registerContextTypes(fREditorContextTypeRegistry);
			}
			return fREditorContextTypeRegistry;
		}
	}

	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public TemplateStore getREditorTemplateStore() {
		
		if (fREditorTemplatesStore != null) {
			return fREditorTemplatesStore;
		}
		synchronized (this) {
			if (fREditorTemplatesStore == null) {
				fREditorTemplatesStore = new ContributionTemplateStore(
						getREditorTemplateContextRegistry(), getPreferenceStore(), R_EDITOR_TEMPLATES_KEY);
				try {
					fREditorTemplatesStore.load();
				} catch (IOException e) {
					StatetPlugin.logUnexpectedError(e);
				}
			}
			return fREditorTemplatesStore;
		}
	}

	
	public static void log(IStatus status) {

		getDefault().getLog().log(status);
	}

	public static void logError(int code, String message, Throwable e) {

		log(new Status(IStatus.ERROR, RUI.PLUGIN_ID, code, message, e));
	}

}
