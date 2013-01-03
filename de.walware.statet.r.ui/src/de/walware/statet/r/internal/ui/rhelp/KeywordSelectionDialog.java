/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpKeywordNode;


public class KeywordSelectionDialog extends ElementTreeSelectionDialog {
	
	
	public static class KeywordsContentProvider implements ITreeContentProvider {
		
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}
		
		@Override
		public Object[] getElements(final Object inputElement) {
			return ((List<?>) inputElement).toArray();
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public Object getParent(final Object element) {
			return null;
		}
		
		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof IRHelpKeywordNode) {
				return ((IRHelpKeywordNode) parentElement).getNestedKeywords().toArray();
			}
			return null;
		}
		
		@Override
		public boolean hasChildren(final Object element) {
			return (element instanceof IRHelpKeywordNode
					&& !((IRHelpKeywordNode) element).getNestedKeywords().isEmpty() );
		}
		
	}
	
	
	public KeywordSelectionDialog(final Shell parent, final List<IRHelpKeyword.Group> keywords) {
		super(parent, new RHelpLabelProvider(), new KeywordsContentProvider());
		setShellStyle(getShellStyle() | SWT.SHEET);
		setTitle(Messages.KeywordSelection_title);
		setMessage(Messages.KeywordSelection_message);
		setInput(keywords);
		setAllowMultiple(false);
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		final Control control = super.createContents(parent);
		getTreeViewer().expandToLevel(1);
		return control;
	}
	
}
