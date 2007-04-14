/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.util;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;


/**
 *
 */
public class TreeUtil {
	
	public static class Node {
		
		private String fName;
		private Node fParent;
		private Node[] fChildren;
		
		public Node(String name, Node[] children) {

			fName = name;
			fChildren = children;
			if (fChildren != null) {
				for (Node node : fChildren) {
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
		
		public Object[] getElements(Object inputElement) {
			
			return ((Node[]) inputElement);
		}
		
		public Object getParent(Object element) {
			
			return ((Node) element).fParent;
		}

		public boolean hasChildren(Object element) {
			
			return ((Node) element).fChildren != null;
		}

		public Object[] getChildren(Object parentElement) {

			return ((Node) parentElement).fChildren;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		public void dispose() {
		}
	}

	
	public static Point calculateTreeSizeHint(Control treeControl, Node[] rootNodes, int rows) {
		
		Point pixels = new Point(0,0);
		PixelConverter tool = new PixelConverter(treeControl);

		int factor = tool.convertWidthInCharsToPixels(2);
		ScrollBar vBar = ((Scrollable) treeControl).getVerticalBar();
		if (vBar != null) {
			factor = (int) (vBar.getSize().x * 1.25); // scrollbars and tree indentation guess
		}
		pixels.x = measureNodes(tool, factor, rootNodes, 0) + factor * 2;

		pixels.y = tool.convertHeightInCharsToPixels(rows);
		
		return pixels;
	}
	
	/** recursive measure */
	private static int measureNodes(PixelConverter tool, int factor, Node[] nodes, int deepth) {

		int maxWidth = 0;
		for (Node node : nodes) {
			maxWidth = Math.max(maxWidth, tool.convertWidthInCharsToPixels(node.fName.length()) + deepth * factor);
			Node[] children = node.getChildren();
			if (children != null) {
				maxWidth = Math.max(maxWidth, measureNodes(tool, factor, children, deepth+1));
			}
		}
		return maxWidth;
	}
	

	public static void addDoubleClickExpansion(final TreeViewer viewer) {
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.size() == 1) {
					Object item = selection.getFirstElement();
					if (viewer.getExpandedState(item)) {
						viewer.collapseToLevel(item, TreeViewer.ALL_LEVELS);
					} else {
						viewer.expandToLevel(item, 1);
					}
				}
			}
		});
	}
	
}
