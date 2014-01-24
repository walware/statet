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

package de.walware.statet.rtm.ftable.ui;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.emf.core.util.RuleSet;

import de.walware.statet.rtm.base.core.AbstractRCodeGenerator;
import de.walware.statet.rtm.base.ui.IRtDescriptor;
import de.walware.statet.rtm.ftable.FTable;
import de.walware.statet.rtm.ftable.FTablePackage;
import de.walware.statet.rtm.ftable.core.FTableRCodeGen;
import de.walware.statet.rtm.ftable.core.FTableRuleSet;
import de.walware.statet.rtm.ftable.provider.FTableItemProviderAdapterFactory;
import de.walware.statet.rtm.ftable.util.FTableAdapterFactory;


public class RtFTableDescriptor implements IRtDescriptor {
	
	
	public static final IRtDescriptor INSTANCE = new RtFTableDescriptor();
	
	
	private static final String XMI_FILE_EXTENSION = "Rtx-ftable"; //$NON-NLS-1$
	
	private static final ConstList<String> FILE_EXTENSIONS = new ConstList<String>(XMI_FILE_EXTENSION);
	
	
	public RtFTableDescriptor() {
	}
	
	
	@Override
	public String getTaskId() {
		return RtFTableEditorPlugin.RT_ID;
	}
	
	@Override
	public String getModelPluginID() {
		return "de.walware.statet.rtm.ftable.core"; //$NON-NLS-1$
	}
	
	@Override
	public String getEditorPluginID() {
		return "de.walware.statet.rtm.ftable.ui"; //$NON-NLS-1$
	}
	
	@Override
	public String getEditorID() {
		return "de.walware.statet.rtm.ftable.editors.FTable"; //$NON-NLS-1$
	}
	
	@Override
	public Image getImage() {
		return RtFTableEditorPlugin.getPlugin().getImageRegistry().get(RtFTableEditorPlugin.IMG_OBJ_FTABLE_TASK);
	}
	
	@Override
	public String getName() {
		return "Contingency Table ('ftable')";
	}
	
	@Override
	public String getDefaultContentTypeID() {
		return FTablePackage.eCONTENT_TYPE;
	}
	
	@Override
	public String getDefaultFileExtension() {
		return XMI_FILE_EXTENSION;
	}
	
	@Override
	public String getAssociatedPerspectiveId() {
		return null;
	}
	
	@Override
	public List<String> getFileExtensions() {
		return FILE_EXTENSIONS;
	}
	
	@Override
	public FTablePackage getEPackage() {
		return FTablePackage.eINSTANCE;
	}
	
	@Override
	public FTable createInitialModelObject() {
		return getEPackage().getFTableFactory().createFTable();
	}
	
	@Override
	public FTableAdapterFactory createItemProviderAdapterFactory() {
		return new FTableItemProviderAdapterFactory();
	}
	
	@Override
	public RuleSet getRuleSet() {
		return FTableRuleSet.INSTANCE;
	}
	
	@Override
	public AbstractRCodeGenerator createCodeGenerator() {
		return new FTableRCodeGen();
	}
	
}
