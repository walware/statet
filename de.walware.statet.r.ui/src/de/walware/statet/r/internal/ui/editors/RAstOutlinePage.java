/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreeViewer;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RLabelProvider;


/**
 * Shows the AST in the outline - for debugging purposes
 */
public class RAstOutlinePage extends SourceEditor1OutlinePage {
	
	
	public RAstOutlinePage(final REditor editor) {
		super(editor, RModel.TYPE_ID, null);
	}
	
	
	@Override
	protected IDialogSettings getDialogSettings() {
		return DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "RAstOutlineView"); //$NON-NLS-1$
	}
	
	@Override
	protected OutlineContentProvider createContentProvider() {
		return new AstContentProvider();
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		super.configureViewer(viewer);
		
		viewer.setComparer(new IElementComparer() {
			public int hashCode(final Object element) {
				return ((RAstNode) element).hashCodeIgnoreAst();
			}
			public boolean equals(final Object a, final Object b) {
				return ((RAstNode) a).equalsIgnoreAst(b);
			}
		});
		viewer.setLabelProvider(new RLabelProvider());
	}
	
}
