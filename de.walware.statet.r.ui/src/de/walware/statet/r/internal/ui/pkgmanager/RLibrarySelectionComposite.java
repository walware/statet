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

package de.walware.statet.r.internal.ui.pkgmanager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRLibPaths.Entry;
import de.walware.statet.r.core.renv.IRLibraryGroup;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.ui.REnvLabelProvider;


public class RLibrarySelectionComposite extends Composite {
	
	
	public static class Validator implements IValidator {
		
		
		private IRLibPaths fLibPaths;
		
		private int fRequired;
		
		
		public void setRequired(final int access) {
			fRequired = access;
		}
		
		
		@Override
		public IStatus validate(final Object value) {
			if (!(value instanceof IRLibraryLocation)) {
				return ValidationStatus.error("No library location selected where to install the package to.");
			}
//			final IRLibraryLocation location = (IRLibraryLocation) value;
//			if (location.isReadOnly() && !location.getDirectoryPath().startsWith("${workspace_loc")) { //$NON-NLS-1$
//				return ValidationStatus.warning("The selected library location is not recommend for user packages.");
//			}
			return ValidationStatus.ok();
		}
		
		private boolean matchesRequired(final IRLibraryLocation location) {
			if (fRequired == 0) {
				return true;
			}
			final Entry entry = fLibPaths.getEntryByLocation(location);
			return (entry != null && (entry.getAccess() & fRequired) == fRequired);
		}
		
	}
	
	
	private TreeViewer fTreeViewer;
	
	private final Validator fValidator;
	
	
	public RLibrarySelectionComposite(final Composite parent) {
		super(parent, SWT.NONE);
		
		fValidator = new Validator();
		setLayout(new FillLayout());
		createComponents(this);
	}
	
	
	private void createComponents(final Composite parent) {
		final Tree tree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		final TreeViewer viewer = new TreeViewer(tree);
		fTreeViewer = viewer;
		
		viewer.setLabelProvider(new REnvLabelProvider());
		viewer.setContentProvider(new ITreeContentProvider() {
			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			}
			@Override
			public void dispose() {
			}
			@Override
			public Object[] getElements(final Object inputElement) {
				return ((List<?>) inputElement).toArray();
			}
			@Override
			public Object getParent(final Object element) {
				return null;
			}
			@Override
			public boolean hasChildren(final Object element) {
				if (element instanceof IRLibraryGroup) {
					for (final IRLibraryLocation location : ((IRLibraryGroup) element).getLibraries()) {
						if (fValidator.matchesRequired(location)) {
							return true;
						}
					}
				}
				return false;
			}
			@Override
			public Object[] getChildren(final Object parentElement) {
				if (parentElement instanceof IRLibraryGroup) {
					final List<? extends IRLibraryLocation> all = ((IRLibraryGroup) parentElement).getLibraries();
					final List<IRLibraryLocation> list = new ArrayList<IRLibraryLocation>(all.size());
					for (final IRLibraryLocation location : all) {
						if (fValidator.matchesRequired(location)) {
							list.add(location);
						}
					}
					return list.toArray();
				}
				return null;
			}
		});
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
	}
	
	public GridData createGD() {
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = LayoutUtil.hintHeight(fTreeViewer.getTree(), 8);
		return gd;
	}
	
	
	public Validator getValidator() {
		return fValidator;
	}
	
	public void setInput(final IRLibPaths libPaths) {
		fValidator.fLibPaths = libPaths;
		fTreeViewer.setInput(libPaths.getRLibraryGroups());
	}
	
	public TreeViewer getSelectionViewer() {
		return fTreeViewer;
	}
	
}
