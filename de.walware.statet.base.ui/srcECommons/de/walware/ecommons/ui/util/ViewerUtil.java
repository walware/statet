/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.util;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;


/**
 * 
 */
public class ViewerUtil {
	
	public static class Node {
		
		private String fName;
		private Node fParent;
		private Node[] fChildren;
		
		public Node(final String name, final Node[] children) {
			fName = name;
			fChildren = children;
			if (fChildren != null) {
				for (final Node node : fChildren) {
					node.fParent = this;
				}
			}
		}
		
		public String getName() {
			return fName;
		}
		
		public Node[] getChildren() {
			return fChildren;
		}
		
	}
	
	
	public static class NodeContentProvider implements ITreeContentProvider {
		
		public Object[] getElements(final Object inputElement) {
			return ((Node[]) inputElement);
		}
		
		public Object getParent(final Object element) {
			return ((Node) element).fParent;
		}
		
		public boolean hasChildren(final Object element) {
			return ((Node) element).fChildren != null;
		}
		
		public Object[] getChildren(final Object parentElement) {
			return ((Node) parentElement).fChildren;
		}
		
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}
		
		public void dispose() {
		}
		
	}
	
	public static Point calculateTreeSizeHint(final Control treeControl, final Node[] rootNodes, final int rows) {
		final Point pixels = new Point(0,0);
		final PixelConverter tool = new PixelConverter(treeControl);
		
		float factor = tool.convertWidthInCharsToPixels(2);
		final ScrollBar vBar = ((Scrollable) treeControl).getVerticalBar();
		if (vBar != null) {
			factor = vBar.getSize().x * 1.1f; // scrollbars and tree indentation guess
		}
		pixels.x = measureNodes(tool, factor, rootNodes, 1) + ((int) factor);
		pixels.y = tool.convertHeightInCharsToPixels(rows);
		
		return pixels;
	}
	
	/** recursive measure */
	private static int measureNodes(final PixelConverter tool, final float factor, final Node[] nodes, final int deepth) {
		int maxWidth = 0;
		for (final Node node : nodes) {
			maxWidth = Math.max(maxWidth, tool.convertWidthInCharsToPixels(node.fName.length()) + (int) (deepth * factor));
			final Node[] children = node.getChildren();
			if (children != null) {
				maxWidth = Math.max(maxWidth, measureNodes(tool, factor * 0.75f , children, deepth+1));
			}
		}
		return maxWidth;
	}
	
	
	public static void addDoubleClickExpansion(final TreeViewer viewer) {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.size() == 1) {
					final Object item = selection.getFirstElement();
					if (viewer.getExpandedState(item)) {
						viewer.collapseToLevel(item, TreeViewer.ALL_LEVELS);
					} else {
						viewer.expandToLevel(item, 1);
					}
				}
			}
		});
	}
	
	
	public static class TableComposite extends Composite {
		
		public TableViewer viewer;
		public Table table;
		public TableColumnLayout layout;
		
		public TableComposite(final Composite parent, final int tableStyle) {
			super(parent, SWT.NONE);
			
			layout = new TableColumnLayout();
			setLayout(layout);
			table = new Table(this, tableStyle);
			viewer = new TableViewer(table);
		}
	}
	
	public static class CheckboxTableComposite extends Composite {
		
		public CheckboxTableViewer viewer;
		public Table table;
		public TableColumnLayout layout;
		
		public CheckboxTableComposite(final Composite parent, final int tableStyle) {
			super(parent, SWT.NONE);
			
			layout = new TableColumnLayout();
			setLayout(layout);
			viewer = CheckboxTableViewer.newCheckList(this, tableStyle);
			table = viewer.getTable();
		}
	}
	
	public static class TreeComposite extends Composite {
		
		public TreeViewer viewer;
		public Tree tree;
		public TreeColumnLayout layout;
		
		public TreeComposite(final Composite parent, final int treeStyle) {
			super(parent, SWT.NONE);
			
			layout = new TreeColumnLayout();
			setLayout(layout);
			tree = new Tree(this, treeStyle);
			viewer = new TreeViewer(tree);
		}
	}
	
	
	private ViewerUtil() {}
	
}
