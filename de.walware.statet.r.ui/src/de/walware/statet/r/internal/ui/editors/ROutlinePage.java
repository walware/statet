/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.walware.eclipsecommons.FastList;
import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.ElementChangedEvent;
import de.walware.eclipsecommons.ltk.IElementChangedListener;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.ui.sourceeditors.StatextOutlinePage;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.editors.REditor;


/**
 * 
 */
public class ROutlinePage extends StatextOutlinePage<REditor> {
	
	
	private class ChangeListener implements IElementChangedListener {
		
		public void elementChanged(final ElementChangedEvent event) {
			if (event.context != fContext || event.delta.getModelElement() != fInputUnit) {
				return;
			}
			UIAccess.getDisplay().asyncExec(new Runnable() {
				public void run() {
					final TreeViewer viewer = getTreeViewer();
					if (event.delta.getModelElement() != fInputUnit || !UIAccess.isOkToUse(viewer)) {
						return;
					}
					viewer.removePostSelectionChangedListener(fPostSelectionListener);
					viewer.refresh(true);
					if (event.delta.getOldAst() == null) {
						viewer.expandToLevel(getAutoExpandLevel());
					}
					
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							if (UIAccess.isOkToUse(viewer)) {
								viewer.addPostSelectionChangedListener(fPostSelectionListener);
							}
						}
					});
					
//					viewer.expandAll();
				}
			});
		}
		
	}
	
	public class ContentProvider implements ITreeContentProvider {
		
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}
		
		public Object[] getElements(final Object inputElement) {
			if (inputElement instanceof IRSourceUnit) {
				final AstInfo info = ((IRSourceUnit) inputElement).getAstInfo("r", false, null); //$NON-NLS-1$
				if (info != null) {
					return new Object[] { info.root };
				}
			}
			return new Object[0];
		}
		
		public void dispose() {
		}
		
		public Object getParent(final Object element) {
			final RAstNode o = (RAstNode) element;
			return o.getParent();
		}
		
		public boolean hasChildren(final Object element) {
			final RAstNode o = (RAstNode) element;
			return o.hasChildren();
		}
		
		public Object[] getChildren(final Object parentElement) {
			final RAstNode o = (RAstNode) parentElement;
			return o.getChildren();
		}
	}
	
	private class PostSelectionChangeListener implements ISelectionChangedListener {
		
		public void selectionChanged(final SelectionChangedEvent event) {
			firePostSelectionChange(event);
		}
		
	}
	
	private class PostSelectionChangeRunner extends SafeRunnable {
		
		final SelectionChangedEvent fEvent;
		ISelectionChangedListener fListener;
		
		public PostSelectionChangeRunner(final SelectionChangedEvent event) {
			fEvent = event;
		}
		
		public void run() {
			fListener.selectionChanged(fEvent);
		}
		
	}
	
	private ChangeListener fListener;
	private final WorkingContext fContext = StatetCore.EDITOR_CONTEXT;
	private ContentProvider fContentProvider;
	
	private ISelectionChangedListener fPostSelectionListener = new PostSelectionChangeListener();
	private boolean fIgnoreSelection;
	private FastList<ISelectionChangedListener> fPostSelectionListeners = new FastList<ISelectionChangedListener>(ISelectionChangedListener.class);
	
	private IRSourceUnit fInputUnit;
	
	
	public ROutlinePage(final REditor editor) {
		fEditor = editor;
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		final TreeViewer viewer = getTreeViewer();
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new RLabelProvider());
		fContentProvider = new ContentProvider();
		viewer.setContentProvider(fContentProvider);
		viewer.setAutoExpandLevel(getAutoExpandLevel());
		
		initActions();
		
		fListener = new ChangeListener();
		RCore.getRModelManger().addElementChangedListener(fListener, fContext);
		viewer.setInput(fInputUnit);
	}
	
	private void initActions() {
		final TreeViewer viewer = getTreeViewer();
		viewer.addPostSelectionChangedListener(fPostSelectionListener);
	}
	
	public void setInput(final IRSourceUnit unit) {
		fInputUnit = unit;
		final TreeViewer viewer = getTreeViewer();
		if (UIAccess.isOkToUse(viewer)) {
			viewer.setInput(fInputUnit);
		}
	}
	
	private int getAutoExpandLevel() {
		return 3;
	}
	
	protected void firePostSelectionChange(final SelectionChangedEvent event) {
		final ISelectionChangedListener[] listeners = fPostSelectionListeners.toArray();
		final PostSelectionChangeRunner runner = new PostSelectionChangeRunner(event);
		for (int i = 0; i < listeners.length; i++) {
			runner.fListener = listeners[i];
			SafeRunner.run(runner);
		}
		
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			final Object first = selection.getFirstElement();
			if (first instanceof RAstNode) {
				final RAstNode node = (RAstNode) first;
				fEditor.selectAndReveal(node.getStartOffset(), node.getStopOffset()-node.getStartOffset());
			}
		}
	}
	
	@Override
	public void dispose() {
		if (fListener != null) {
			RCore.getRModelManger().removeElementChangedListener(fListener, fContext);
			fListener = null;
		}
		super.dispose();
	}
	
}
