/*******************************************************************************
 * Copyright (c) 2009-2013 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.objectbrowser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.models.core.util.IElementPartition;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.ui.IToolRunnableDecorator;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.util.ToolMessageDialog;

import de.walware.rj.data.RObject;

import de.walware.statet.r.console.core.AbstractRDataRunnable;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;


class DeleteHandler extends AbstractHandler {
	
	
	private static class DeleteRunnable extends AbstractRDataRunnable implements IToolRunnableDecorator {
		
		private final List<String> names;
		private final List<String> commands;
		private final Set<IElementName> topEnvirs;
		
		public DeleteRunnable(final List<String> names, final List<String> commands,
				final Set<IElementName> topEnvirs) {
			super("r/objectbrowser/delete", "Delete Elements"); //$NON-NLS-1$
			this.names = names;
			this.commands = commands;
			this.topEnvirs = topEnvirs;
		}
		
		@Override
		public Image getImage() {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void run(final IRDataAdapter r,
				final IProgressMonitor monitor) throws CoreException {
			try {
				for (int i = 0; i < this.names.size(); i++) {
					r.evalVoid(this.commands.get(i), monitor);
				}
			}
			finally {
				r.briefAboutChange(this.topEnvirs, 0);
			}
		}
		
	}
	
	
	private final ObjectBrowserView view;
	
	
	public DeleteHandler(final ObjectBrowserView view) {
		this.view = view;
	}
	
	
	private boolean isValidSelection(ITreeSelection selection) {
		if (selection == null || selection.isEmpty()) {
			return false;
		}
		for (final Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
			Object element = iter.next();
			if (element instanceof IElementPartition) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final ToolProcess process = this.view.getTool();
		setBaseEnabled(process != null && !process.isTerminated()
				&& isValidSelection(this.view.getSelection()) );
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (!UIAccess.isOkToUse(this.view.getViewer())) {
			return null;
		}
		final ITreeSelection selection = this.view.getSelection();
		if (!isValidSelection(selection)) {
			return null;
		}
		final TreePath[] treePaths = selection.getPaths();
		Arrays.sort(treePaths, new Comparator<TreePath>() {
			@Override
			public int compare(final TreePath o1, final TreePath o2) {
				return o1.getSegmentCount()-o2.getSegmentCount();
			}
		});
		final IElementComparer comparer = new IElementComparer() {
			@Override
			public int hashCode(final Object e) {
				return e.hashCode();
			}
			@Override
			public boolean equals(final Object e1, final Object e2) {
				return (e1 == e2);
			}
		};
		final List<String> commands = new ArrayList<String>(treePaths.length);
		final List<String> names = new ArrayList<String>(treePaths.length);
		final Set<IElementName> topEnvirs = new HashSet<IElementName>(treePaths.length);
		ITER_ELEMENTS: for (int i = 0; i < treePaths.length; i++) {
			for (int j = 0; j < i; j++) {
				if (treePaths[j] != null && treePaths[i].startsWith(treePaths[j], comparer)) {
					treePaths[i] = null;
					continue ITER_ELEMENTS;
				}
			}
			
			final TreePath treePath = treePaths[i];
			final ICombinedRElement element = ContentProvider.getCombinedRElement(treePath.getLastSegment());
			final ICombinedRElement parent = element.getModelParent();
			
			final RElementName elementName = this.view.getElementName(treePath);
			if (parent != null && elementName != null) {
				final RElementName topName;
				switch (parent.getRObjectType()) {
				case RObject.TYPE_ENV: {
					final RElementName envirName = (treePath.getSegmentCount() > 1) ? this.view.getElementName(treePath.getParentPath()) : parent.getElementName();
					final IElementName itemName = element.getElementName();
					topName = elementName.getNamespace();
					if (envirName != null) { // elementName ok => segmentName ok
						commands.add("rm(`"+itemName.getSegmentName()+"`,"+ //$NON-NLS-1$ //$NON-NLS-2$ 
								"pos="+RElementName.createDisplayName(envirName, RElementName.DISPLAY_NS_PREFIX | RElementName.DISPLAY_EXACT)+ //$NON-NLS-1$
								")"); //$NON-NLS-1$
						names.add(elementName.getDisplayName());
						topEnvirs.add(topName);
						continue ITER_ELEMENTS;
					}
					break; }
				case RObject.TYPE_LIST:
				case RObject.TYPE_DATAFRAME:
				case RObject.TYPE_S4OBJECT:
					topName = elementName.getNamespace();
					final String name = RElementName.createDisplayName(elementName, RElementName.DISPLAY_EXACT);
					commands.add("with("+RElementName.createDisplayName(topName, RElementName.DISPLAY_NS_PREFIX)+","+ //$NON-NLS-1$ //$NON-NLS-2$ 
							name+"<-NULL"+ //$NON-NLS-1$
							")"); //$NON-NLS-1$
					names.add(elementName.getDisplayName());
					topEnvirs.add(topName);
					continue ITER_ELEMENTS;
				}
			}
			
			final StringBuilder message = new StringBuilder("Selection contains unsupported object");
			if (elementName != null) {
				message.append("\n\t"); //$NON-NLS-1$
				message.append(elementName.getDisplayName());
			}
			else {
				message.append("."); //$NON-NLS-1$
			}
			MessageDialog.openError(this.view.getSite().getShell(), "Delete", message.toString());
			return null;
		}
		
		final StringBuilder message = new StringBuilder(names.size() == 1 ?
				"Are you sure you want to delete the object" :
				NLS.bind("Are you sure you want to delete these {0} objects:", names.size()));
		final int show = (names.size() > 5) ? 3 : names.size();
		for (int i = 0; i < show; i++) {
			message.append("\n\t"); //$NON-NLS-1$
			message.append(names.get(i));
		}
		if (show < names.size()) {
			message.append("\n\t..."); //$NON-NLS-1$
		}
		
		final ToolProcess process = this.view.getTool();
		if (ToolMessageDialog.openConfirm(process, this.view.getSite().getShell(), "Delete", message.toString())) {
			process.getQueue().add(new DeleteRunnable(names, commands, topEnvirs));
		}
		
		return null;
	}
	
}
