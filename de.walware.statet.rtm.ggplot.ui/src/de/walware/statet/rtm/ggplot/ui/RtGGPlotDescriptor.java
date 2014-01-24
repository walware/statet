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

package de.walware.statet.rtm.ggplot.ui;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.emf.core.util.RuleSet;

import de.walware.statet.rtm.base.ui.IRtDescriptor;
import de.walware.statet.rtm.base.ui.RtModelUIPlugin;
import de.walware.statet.rtm.ggplot.GGPlot;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.core.GGPlotRCodeGen;
import de.walware.statet.rtm.ggplot.core.GGPlotRuleSet;
import de.walware.statet.rtm.ggplot.provider.GGPlotItemProviderAdapterFactory;
import de.walware.statet.rtm.ggplot.util.GGPlotAdapterFactory;


public class RtGGPlotDescriptor implements IRtDescriptor {
	
	
	public static final IRtDescriptor INSTANCE = new RtGGPlotDescriptor();
	
	
	private static final String XMI_FILE_EXTENSION = "Rtx-ggplot"; //$NON-NLS-1$
	
	private static final ConstList<String> FILE_EXTENSIONS = new ConstList<String>(XMI_FILE_EXTENSION);
	
	
	public RtGGPlotDescriptor() {
	}
	
	
	@Override
	public String getTaskId() {
		return RtGGPlotEditorPlugin.RT_ID;
	}
	
	@Override
	public String getModelPluginID() {
		return "de.walware.statet.rtm.ggplot.core"; //$NON-NLS-1$
	}
	
	@Override
	public String getEditorPluginID() {
		return "de.walware.statet.rtm.ggplot.ui"; //$NON-NLS-1$
	}
	
	@Override
	public String getEditorID() {
		return "de.walware.statet.rtm.ggplot.editors.GGPlot"; //$NON-NLS-1$
	}
	
	@Override
	public Image getImage() {
		return RtGGPlotEditorPlugin.getPlugin().getImageRegistry().get(RtGGPlotEditorPlugin.IMG_OBJ_GGPLOT_TASK);
	}
	
	@Override
	public String getName() {
		return "Graph ('ggplot2')";
	}
	
	@Override
	public String getDefaultContentTypeID() {
		return GGPlotPackage.eCONTENT_TYPE;
	}
	
	@Override
	public String getDefaultFileExtension() {
		return XMI_FILE_EXTENSION;
	}
	
	@Override
	public List<String> getFileExtensions() {
		return FILE_EXTENSIONS;
	}
	
	@Override
	public String getAssociatedPerspectiveId() {
		return RtModelUIPlugin.R_GRAPHICS_PERSPECTIVE_ID;
	}
	
	@Override
	public GGPlotPackage getEPackage() {
		return GGPlotPackage.eINSTANCE;
	}
	
	@Override
	public GGPlot createInitialModelObject() {
		return getEPackage().getGGPlotFactory().createGGPlot();
	}
	
	@Override
	public GGPlotAdapterFactory createItemProviderAdapterFactory() {
		return new GGPlotItemProviderAdapterFactory();
	}
	
	@Override
	public RuleSet getRuleSet() {
		return GGPlotRuleSet.INSTANCE;
	}
	
	@Override
	public GGPlotRCodeGen createCodeGenerator() {
		return new GGPlotRCodeGen();
	}
	
}
