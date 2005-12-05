/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.r.codegeneration.RCodeTemplatesContextType;
import de.walware.statet.r.codegeneration.RdCodeTemplatesContextType;
import de.walware.statet.r.ui.editors.RDocumentProvider;
import de.walware.statet.r.ui.editors.RdDocumentProvider;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;


/**
 * The main plugin class to be used in the desktop.
 */
public class RUiPlugin extends AbstractUIPlugin {


	public static final String ID = "de.walware.statet.r.ui";
	
	private static final String R_CODE_TEMPLATES_KEY  = "de.walware.statet.r.ui.text.r_code_templates"; //$NON-NLS-1$
	private static final String RD_CODE_TEMPLATES_KEY = "de.walware.statet.r.ui.text.rd_code_templates"; //$NON-NLS-1$
	private static final String R_EDITOR_TEMPLATES_KEY  = "de.walware.statet.r.ui.text.r_editor_templates"; //$NON-NLS-1$
	
	
	//The shared instance.
	private static RUiPlugin fgPlugin;

	private RDocumentProvider fRDocumentProvider;
	private RdDocumentProvider fRdDocumentProvider;

	private TemplateStore fRCodeTemplatesStore;
	private ContextTypeRegistry fRCodeTemplatesContextTypeRegistry;
	private TemplateStore fRdCodeTemplatesStore;
	private ContextTypeRegistry fRdCodeTemplatesContextTypeRegistry;

	private TemplateStore fREditorTemplatesStore;
	private ContextTypeRegistry fREditorContextTypeRegistry;

	
	
	/**
	 * The constructor.
	 */
	public RUiPlugin() {
		fgPlugin = this;
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
		
		} finally {
			super.stop(context);
			fgPlugin = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static RUiPlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("de.walware.statet.r.ui", path);
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
	public TemplateStore getRCodeGenerationTemplateStore() {
		
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
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public ContextTypeRegistry getRdCodeGenerationTemplateContextRegistry() {
		
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
	public TemplateStore getRdCodeGenerationTemplateStore() {
		
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


	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public ContextTypeRegistry getREditorTemplateContextRegistry() {
		
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
	public TemplateStore getREditorTemplateStore() {
		
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
