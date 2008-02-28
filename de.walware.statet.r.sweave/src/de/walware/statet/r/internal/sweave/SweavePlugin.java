/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.walware.eclipsecommons.ui.util.ImageRegistryUtil;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.base.ui.sourceeditors.StatextSourceViewerConfiguration;
import de.walware.statet.r.internal.sweave.processing.SweaveCreation;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.sweave.Sweave;
import de.walware.statet.r.sweave.editors.RweaveTexDocumentProvider;
import de.walware.statet.r.ui.RUI;


/**
 * The activator class controls the plug-in life cycle
 */
public class SweavePlugin extends AbstractUIPlugin {
	
	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "de.walware.statet.r.sweave"; //$NON-NLS-1$
	
	private static final String RWEAVETEX_TEMPLATES_KEY = "de.walware.statet.r.sweave.rweave_tex_templates"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_RWEAVETEX = RUI.PLUGIN_ID + "/image/tool/rweavetex"; //$NON-NLS-1$
	public static final String IMG_OBJ_RWEAVETEX_ACTIVE = RUI.PLUGIN_ID + "/image/tool/rweavetex-active"; //$NON-NLS-1$
	
	public static final String IMG_TOOL_BUILD = RUI.PLUGIN_ID + "/image/tool/build"; //$NON-NLS-1$
	public static final String IMG_TOOL_BUILDANDPREVIEW = RUI.PLUGIN_ID + "/image/tool/buildandpreview"; //$NON-NLS-1$
	public static final String IMG_TOOL_PREVIEW = RUI.PLUGIN_ID + "/image/tool/preview"; //$NON-NLS-1$
	public static final String IMG_TOOL_RWEAVE = RUI.PLUGIN_ID + "/image/tool/rweave"; //$NON-NLS-1$
	public static final String IMG_TOOL_BUILDTEX = RUI.PLUGIN_ID + "/image/tool/build-tex"; //$NON-NLS-1$
	
	
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
		getDefault().getLog().log(new Status(IStatus.ERROR, RUI.PLUGIN_ID, code, message, e));
	}
	
	
	private RweaveTexDocumentProvider fRTexDocumentProvider;
	
	private IPreferenceStore fEditorRTexPreferenceStore;
	
	private ContextTypeRegistry fRweaveTexTemplatesContextTypeRegistry;
	private TemplateStore fRweaveTexTemplatesStore;
	
	private SweaveCreation fSweaveCreationManager;
	
	
	/**
	 * The constructor
	 */
	public SweavePlugin() {
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		gPlugin = this;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		if (fSweaveCreationManager != null) {
			fSweaveCreationManager.dispose();
			fSweaveCreationManager = null;
		}
		
		gPlugin = null;
		super.stop(context);
	}
	
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry reg) {
		final ImageRegistryUtil util = new ImageRegistryUtil(this);
		
		util.register(IMG_OBJ_RWEAVETEX, ImageRegistryUtil.T_OBJ, "texsweave-file.png"); //$NON-NLS-1$
		final Image baseImage = reg.get(IMG_OBJ_RWEAVETEX);
		reg.put(IMG_OBJ_RWEAVETEX_ACTIVE, new DecorationOverlayIcon(baseImage, new ImageDescriptor[] {
				null, null, null, StatetImages.getDescriptor(StatetImages.OVR_DEFAULT_MARKER), null},
				new Point(baseImage.getBounds().width, baseImage.getBounds().height)));
		
		util.register(IMG_TOOL_BUILD, ImageRegistryUtil.T_TOOL, "build.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_BUILDANDPREVIEW, ImageRegistryUtil.T_TOOL, "build_and_preview.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_PREVIEW, ImageRegistryUtil.T_TOOL, "preview.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_RWEAVE, ImageRegistryUtil.T_TOOL, "rweave.png"); //$NON-NLS-1$
		util.register(IMG_TOOL_BUILDTEX, ImageRegistryUtil.T_TOOL, "build-tex.png"); //$NON-NLS-1$
		
		util.register(RUI.IMG_OBJ_R_ENVIRONMENT, ImageRegistryUtil.T_OBJ, "r-env.png"); //$NON-NLS-1$
	}
	
	
	public synchronized RweaveTexDocumentProvider getRTexDocumentProvider() {
		if (fRTexDocumentProvider == null) {
			fRTexDocumentProvider = new RweaveTexDocumentProvider();
		}
		return fRTexDocumentProvider;
	}
	
	public IPreferenceStore getEditorRTexPreferenceStore() {
		if (fEditorRTexPreferenceStore == null) {
			fEditorRTexPreferenceStore = StatextSourceViewerConfiguration.createCombinedPreferenceStore(new IPreferenceStore[] {
					getPreferenceStore(),
					RUIPlugin.getDefault().getPreferenceStore()
			});
		}
		return fEditorRTexPreferenceStore;
	}
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getRweaveTexGenerationTemplateContextRegistry() {
		if (fRweaveTexTemplatesContextTypeRegistry == null) {
			fRweaveTexTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
			
			RweaveTexTemplatesContextType.registerContextTypes(fRweaveTexTemplatesContextTypeRegistry);
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
			fRweaveTexTemplatesStore = new ContributionTemplateStore(
					getRweaveTexGenerationTemplateContextRegistry(), getPreferenceStore(), RWEAVETEX_TEMPLATES_KEY);
			try {
				fRweaveTexTemplatesStore.load();
			} catch (final IOException e) {
				logError(-1, "Error occured when loading 'R code generation' template store.", e); //$NON-NLS-1$
			}
		}
		return fRweaveTexTemplatesStore;
	}
	
	public synchronized SweaveCreation getSweaveCreationManager() {
		if (fSweaveCreationManager == null) {
			fSweaveCreationManager = new SweaveCreation(Sweave.RWEAVETEX_DOC_CREATION_LAUNCHCONFIGURATION_ID);
		}
		return fSweaveCreationManager;
	}
	
}
