/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.HashMap;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.rsource.ast.DocuTag;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RClass;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RMethod;


/**
 * Describes how to handle special Roxygen tags
 */
public abstract class RoxygenTagType {
	
	
	public static final int INITIAL = 0x0;
	
	public static final int SCAN_MODE_FREETEXT = 0x0;
	public static final int SCAN_MODE_SYMBOL = 0x1;
	public static final int SCAN_MODE_RCODE = 0x2;
	
	public static final int FRAGMENT_ACTION_SELF_ACCESS = 0x1; 
	public static final int FRAGMENT_ACTION_PARAM_ACCESS = 0x2; 
	public static final int FRAGMENT_ACTION_SLOT_ACCESS = 0x3; 
	public static final int FRAGMENT_ACTION_PACKAGE_IMPORT = 0x4; 
	
	
	public static final HashMap<String, RoxygenTagType> TYPES = new HashMap<String, RoxygenTagType>();
	
	static {
		TYPES.put("param", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				if (previous == INITIAL) {
					return (0x100 | SCAN_MODE_SYMBOL);
				}
				return 0x900;
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				if (element == null || (element.getElementType() & IRElement.MASK_C1) != IRElement.C1_METHOD) {
					return;
				}
				final int count = docuTag.getChildCount();
				if (count >= 1) {
					final RAstNode child = docuTag.getChild(0);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createArgAccess((RMethod) element, child);
					}
				}
			}
		});
		TYPES.put("slot", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				if (previous == INITIAL) {
					return (0x100 | SCAN_MODE_SYMBOL);
				}
				return 0x900;
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				if (element == null || (element.getElementType() & IRElement.MASK_C1) != IRElement.C1_CLASS) {
					return;
				}
				final int count = docuTag.getChildCount();
				if (count >= 1) {
					final RAstNode child = docuTag.getChild(0);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createSlotAccess((RClass) element, child);
					}
				}
			}
		});
		TYPES.put("name", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				if (previous == INITIAL) {
					return (0x100 | SCAN_MODE_SYMBOL);
				}
				return 0x900;
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				if (element == null) {
					return;
				}
				final int count = docuTag.getChildCount();
				if (count >= 1) {
					final RAstNode child = docuTag.getChild(0);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createSelfAccess(element, child);
					}
				}
			}
		});
		TYPES.put("aliases", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				if (element == null) {
					return;
				}
				final int count = docuTag.getChildCount();
				for (int i = 0; i < count; i++) {
					final RAstNode child = docuTag.getChild(i);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createSelfAccess(element, child);
					}
				}
			}
		});
		TYPES.put("export", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				if (element == null) {
					return;
				}
				final int count = docuTag.getChildCount();
				for (int i = 0; i < count; i++) {
					final RAstNode child = docuTag.getChild(i);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createSelfAccess(element, child);
					}
				}
			}
		});
		TYPES.put("exportClass", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				if (element == null
						|| ((element.getElementType() & IRElement.MASK_C1) != IRElement.C1_CLASS
								&& (element.getElementType() & IRElement.MASK_C1) != IRElement.C1_VARIABLE) ) {
					return;
				}
				final int count = docuTag.getChildCount();
				for (int i = 0; i < count; i++) {
					final RAstNode child = docuTag.getChild(i);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createSelfAccess(element, child);
					}
				}
			}
		});
		TYPES.put("exportMethod", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				if (element == null
						|| ((element.getElementType() & IRElement.MASK_C1) != IRElement.C1_METHOD
								&& (element.getElementType() & IRElement.MASK_C1) != IRElement.C1_VARIABLE) ) {
					return;
				}
				final int count = docuTag.getChildCount();
				for (int i = 0; i < count; i++) {
					final RAstNode child = docuTag.getChild(i);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createSelfAccess(element, child);
					}
				}
			}
		});
		TYPES.put("import", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				final int count = docuTag.getChildCount();
				for (int i = 0; i < count; i++) {
					final RAstNode child = docuTag.getChild(i);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createNamespaceImportAccess(child);
					}
				}
			}
		});
		TYPES.put("importFrom", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				final int count = docuTag.getChildCount();
				if (count >= 1) {
					RAstNode child = docuTag.getChild(0);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createNamespaceImportAccess(child);
					}
					final String name = child.getText();
					
					if (count >= 2 && name != null) {
						final IRFrameInSource frame = context.getNamespaceFrame(name);
						if (frame != null) {
							for (int i = 1; i < count; i++) {
								child = docuTag.getChild(i);
								if (child.getNodeType() == NodeType.SYMBOL) {
									context.createNamespaceObjectImportAccess(frame, child);
								}
							}
						}
					}
				}
			}
		});
		TYPES.put("importClassesFrom", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				final int count = docuTag.getChildCount();
				if (count >= 1) {
					final RAstNode child = docuTag.getChild(0);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createNamespaceImportAccess(child);
					}
				}
			}
		});
		TYPES.put("importMethodsFrom", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_SYMBOL);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				final int count = docuTag.getChildCount();
				if (count >= 1) {
					final RAstNode child = docuTag.getChild(0);
					if (child.getNodeType() == NodeType.SYMBOL) {
						context.createNamespaceImportAccess(child);
					}
				}
			}
		});
		TYPES.put("examples", new RoxygenTagType() {
			@Override
			public int getNextScanMode(final int previous) {
				return (0x100 | SCAN_MODE_RCODE);
			}
			@Override
			public void analyze(final IRoxygenAnalyzeContext context, final DocuTag docuTag, final IRLangSourceElement element) {
				final int count = docuTag.getChildCount();
				if (count >= 1) {
					final RAstNode child = docuTag.getChild(0);
					if (child.getNodeType() == NodeType.SOURCELINES) {
						context.createRSourceRegion(child);
					}
				}
			}
		});
	}
	
	
	public RoxygenTagType() {
	}
	
	
	public abstract int getNextScanMode(int previous);
	
	public abstract void analyze(IRoxygenAnalyzeContext context, DocuTag docuTag, IRLangSourceElement element);
	
	
}
