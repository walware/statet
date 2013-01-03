/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.util.CombinedPreferenceStore;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.ImageRegistryUtil;

import de.walware.docmlet.tex.ui.sourceediting.LtxViewerConfiguration;

import de.walware.statet.base.ui.StatetUIServices;

import de.walware.statet.r.internal.sweave.editors.LtxRweaveDocumentProvider;
import de.walware.statet.r.internal.sweave.processing.SweaveProcessing;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.sweave.Sweave;


/**
 * The activator class controls the plug-in life cycle
 */
public class SweavePlugin extends AbstractUIPlugin {
	
	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "de.walware.statet.r.sweave"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_LTXRWEAVE = PLUGIN_ID + "/image/obj/rweavetex"; //$NON-NLS-1$
	public static final String IMG_OBJ_LTXRWEAVE_ACTIVE = PLUGIN_ID + "/image/obj/rweavetex-active"; //$NON-NLS-1$
	public static final String IMG_OBJ_RCHUNK = PLUGIN_ID + "/image/obj/rchunk"; //$NON-NLS-1$
	
	public static final String IMG_TOOL_NEW_LTXRWEAVE = PLUGIN_ID + "/image/tool/new-ltxrweave"; //$NON-NLS-1$
	
	public static final String IMG_TOOL_BUILD = PLUGIN_ID + "/image/tool/build"; //$NON-NLS-1$
	public static final String IMG_TOOL_BUILDANDPREVIEW = PLUGIN_ID + "/image/tool/buildandpreview"; //$NON-NLS-1$
	public static final String IMG_TOOL_PREVIEW = PLUGIN_ID + "/image/tool/preview"; //$NON-NLS-1$
	public static final String IMG_TOOL_RWEAVE = PLUGIN_ID + "/image/tool/rweave"; //$NON-NLS-1$
	public static final String IMG_TOOL_BUILDTEX = PLUGIN_ID + "/image/tool/build-tex"; //$NON-NLS-1$
	
	public static final String IMG_LOCTOOL_FILTERCHUNKS = PLUGIN_ID + "/image/loctool/filter-r_chunks"; //$NON-NLS-1$
	
	public static final String IMG_WIZBAN_NEW_LTXRWEAVE_FILE = PLUGIN_ID + "/image/wizban/new-ltxrweave_file"; //$NON-NLS-1$
	
	public static final String RWEAVETEX_EDITOR_NODE = PLUGIN_ID + "/rweavetex.editor/options"; //$NON-NLS-1$
	
	public static final String RWEAVETEX_EDITOR_ASSIST_GROUP_ID = "sweave/rweavetex.editor/assist"; //$NON-NLS-1$
	
	
	
	// The shared instance
	private static SweavePlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the plug-in instance
	 */
	public static SweavePlugin getDefault() {
		return gPlugin;
	}
	
	public static void logError(final int code, final String message, final Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, code, message, e));
	}
	
	
	private boolean fStarted;
	
	private final List<IDisposable> fDisposables = new ArrayList<IDisposable>();
	
	private LtxRweaveDocumentProvider fRTexDocumentProvider;
	
	private IPreferenceStore fEditorRTexPreferenceStore;
	
	private ContextTypeRegistry fSweaveDocTemplatesContextTypeRegistry;
	private TemplateStore fSweaveDocTemplatesStore;
	
	private ContextTypeRegistry fRweaveTexTemplatesContextTypeRegistry;
	private TemplateStore fRweaveTexTemplatesStore;
	private ContentAssistComputerRegistry fRweaveTexEditorContentAssistRegistry;
	
	private SweaveProcessing fRweaveTexProcessingManager;
	
	
	
	/**
	 * The constructor
	 */
	public SweavePlugin() {
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		gPlugin = this;
		fStarted = true;
	}
	
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			if (fRweaveTexTemplatesStore != null) {
				fRweaveTexTemplatesStore.stopListeningForPreferenceChanges();
			}
			
			synchronized (this) {
				fStarted = false;
				
				fEditorRTexPreferenceStore = null;
				
				fRweaveTexTemplatesStore = null;
				fRweaveTexTemplatesContextTypeRegistry = null;
				fRweaveTexEditorContentAssistRegistry = null;
				
				fRweaveTexProcessingManager = null;
			}
			
			for (final IDisposable listener : fDisposables) {
				try {
					listener.dispose();
				}
				catch (final Throwable e) {
					getLog().log(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN, "Error occured when dispose module", e)); 
				}
			}
			fDisposables.clear();
		}
		finally {
			gPlugin = null;
			super.stop(context);
		}
	}
	
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		
		util.register(IMG_OBJ_LTXRWEAVE, ImageRegistryUtil.T_OBJ, "texsweave-file.png"); //$NON-NLS-1$
		final Image baseImage = reg.get(IMG_OBJ_LTXRWEAVE);
		reg.put(IMG_OBJ_LTXRWEAVE_ACTIVE, new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, SharedUIResources.getImages().getDescriptor(SharedUIResources.OVR_DEFAULT_MARKER_IMAGE_ID), null},
				new Point(baseImage.getBounds().width, baseImage.getBounds().height)));
		util.register(IMG_OBJ_RCHUNK, ImageRegistryUtil.T_OBJ, "r_chunk.png"); //$NON-NLS-1$
		
		util.register(IMG_TOOL_NEW_LTXRWEAVE, ImageRegistryUtil.T_TOOL, "new-ltxrweave_file.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_BUILD, ImageRegistryUtil.T_TOOL, "build.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_BUILDANDPREVIEW, ImageRegistryUtil.T_TOOL, "build_and_preview.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_PREVIEW, ImageRegistryUtil.T_TOOL, "preview.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_RWEAVE, ImageRegistryUtil.T_TOOL, "rweave.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_BUILDTEX, ImageRegistryUtil.T_TOOL, "build-tex.png"); //$NON-NLS-1$
		
		util.register(IMG_LOCTOOL_FILTERCHUNKS, ImageRegistryUtil.T_LOCTOOL, "filter-r_chunks.png"); //$NON-NLS-1$
		
		util.register(IMG_WIZBAN_NEW_LTXRWEAVE_FILE, ImageRegistryUtil.T_WIZBAN, "new-ltxrweave_file.png"); //$NON-NLS-1$
	}
	
	
	public synchronized LtxRweaveDocumentProvider getRTexDocumentProvider() {
		if (fRTexDocumentProvider == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fRTexDocumentProvider = new LtxRweaveDocumentProvider();
			fDisposables.add(fRTexDocumentProvider);
		}
		return fRTexDocumentProvider;
	}
	
	public synchronized IPreferenceStore getEditorTexRPreferenceStore() {
		if (fEditorRTexPreferenceStore == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fEditorRTexPreferenceStore = CombinedPreferenceStore.createStore(
					getPreferenceStore(),
					LtxViewerConfiguration.getTexPreferenceStore(),
					RUIPlugin.getDefault().getPreferenceStore(),
					StatetUIServices.getBaseUIPreferenceStore(),
					EditorsUI.getPreferenceStore() );
		}
		return fEditorRTexPreferenceStore;
	}
	
	
	/**
	 * Returns the template context type registry for the new documents.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getSweaveDocTemplateContextRegistry() {
		if (fSweaveDocTemplatesContextTypeRegistry == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fSweaveDocTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
			fSweaveDocTemplatesContextTypeRegistry.addContextType(new LtxRweaveTemplatesContextType(
					LtxRweaveTemplatesContextType.NEW_RWEAVETEX_CONTEXTTYPE ));
		}
		return fSweaveDocTemplatesContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the new documents.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getSweaveDocTemplateStore() {
		if (fSweaveDocTemplatesStore == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fSweaveDocTemplatesStore = new ContributionTemplateStore(
					getSweaveDocTemplateContextRegistry(), getPreferenceStore(),
					LtxRweaveTemplatesContextType.SWEAVEDOC_TEMPLATES_KEY );
			try {
				fSweaveDocTemplatesStore.load();
			}
			catch (final IOException e) {
				logError(-1, "Error occured when loading 'Sweave document' template store.", e); //$NON-NLS-1$
			}
		}
		return fSweaveDocTemplatesStore;
	}
	
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getRweaveTexGenerationTemplateContextRegistry() {
		if (fRweaveTexTemplatesContextTypeRegistry == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fRweaveTexTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
			LtxRweaveTemplatesContextType.registerContextTypes(fRweaveTexTemplatesContextTypeRegistry);
		}
		return fRweaveTexTemplatesContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getRweaveTexGenerationTemplateStore() {
		if (fRweaveTexTemplatesStore == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fRweaveTexTemplatesStore = new ContributionTemplateStore(
					getRweaveTexGenerationTemplateContextRegistry(), getPreferenceStore(),
					LtxRweaveTemplatesContextType.LTXRWEAVE_TEMPLATES_KEY );
			try {
				fRweaveTexTemplatesStore.load();
			}
			catch (final IOException e) {
				logError(-1, "Error occured when loading 'R code generation' template store.", e); //$NON-NLS-1$
			}
		}
		return fRweaveTexTemplatesStore;
	}
	
	public synchronized ContentAssistComputerRegistry getTexEditorContentAssistRegistry() {
		if (fRweaveTexEditorContentAssistRegistry == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fRweaveTexEditorContentAssistRegistry = new ContentAssistComputerRegistry("de.walware.docmlet.tex.contentTypes.Ltx", 
					RWEAVETEX_EDITOR_NODE, RWEAVETEX_EDITOR_ASSIST_GROUP_ID); 
			fDisposables.add(fRweaveTexEditorContentAssistRegistry);
		}
		return fRweaveTexEditorContentAssistRegistry;
	}
	
	public synchronized SweaveProcessing getRweaveTexProcessingManager() {
		if (fRweaveTexProcessingManager == null) {
			if (!fStarted) {
				throw new IllegalStateException("Plug-in is not started.");
			}
			fRweaveTexProcessingManager = new SweaveProcessing(Sweave.RWEAVETEX_DOC_PROCESSING_LAUNCHCONFIGURATION_ID);
			fDisposables.add(fRweaveTexProcessingManager);
		}
		return fRweaveTexProcessingManager;
	}
	
}
