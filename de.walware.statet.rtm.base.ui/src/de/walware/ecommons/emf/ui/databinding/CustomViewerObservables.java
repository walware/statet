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

package de.walware.ecommons.emf.ui.databinding;

import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ISelectionProvider;

import de.walware.statet.rtm.base.internal.ui.ListViewerObservableValueDecorator;
import de.walware.statet.rtm.base.internal.ui.TableViewerObservableValueDecorator;


public class CustomViewerObservables {
	
	
	public static IViewerObservableValue observeComboSelection(final AbstractTableViewer viewer,
			final List<Object> input) {
		viewer.setInput(input);
		final IObservableValue observable = ViewersObservables.observeSingleSelection((ISelectionProvider) viewer);
		return new TableViewerObservableValueDecorator(observable, viewer, input);
	}
	
	public static IViewerObservableValue observeComboSelection(final AbstractListViewer viewer,
			final List<Object> input) {
		viewer.setInput(input);
		final IObservableValue observable = ViewersObservables.observeSingleSelection((ISelectionProvider) viewer);
		return new ListViewerObservableValueDecorator(observable, viewer, input);
	}
	
}
