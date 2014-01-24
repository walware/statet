/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.ui.rexpr;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;

import de.walware.ecommons.emf.core.util.IContext;
import de.walware.ecommons.emf.core.util.IEMFPropertyContext;
import de.walware.ecommons.emf.ui.forms.EFEditor;
import de.walware.ecommons.emf.ui.forms.EFPropertySheetPage;
import de.walware.ecommons.emf.ui.forms.EFToolkit;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.rtm.base.ui.rexpr.RExprWidget.TypeDef;


public class HyperlinkType extends TypeDef implements IHyperlinkListener {
	
	
	public static interface IHyperlinkProvider {
		
		PropertyTabLink get(EClass eClass, EStructuralFeature eFeature);
		
	}
	
	public static class PropertyTabLink {
		
		private final String fLabel;
		
		final String fPropertyTabId;
		
		public PropertyTabLink(final String label, final String propertyTabId) {
			fLabel = label;
			fPropertyTabId = propertyTabId;
		}
		
		protected void run(final IContext context) {
			showProperties(context);
		}
		
		protected void showProperties(final IContext context) {
			final EFEditor editor = (EFEditor) context.getAdapter(EFEditor.class);
			
			final IWorkbenchPage workbenchPage = UIAccess.getActiveWorkbenchPage(true);
			final IViewReference[] viewReferences = workbenchPage.getViewReferences();
			final IViewReference[] rated = new IViewReference[10];
			for (final IViewReference viewReference : viewReferences) {
				final int rate = ratePropertyView(viewReference, editor);
				if (rate >= 0 && rated[rate] == null) {
					rated[rate] = viewReference;
				}
			}
			for (final IViewReference viewReference : rated) {
				if (viewReference != null) {
					final PropertySheet view = (PropertySheet) viewReference.getView(true);
					workbenchPage.activate(view);
					showPropertyTab(view);
					return;
				}
			}
			try {
				final PropertySheet view = (PropertySheet) workbenchPage.showView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
				if (!view.isPinned()) {
					showPropertyTab(view);
					return;
				}
			}
			catch (final PartInitException e) {
			}
		}
		
		private static String getId(final IViewReference ref) {
			// E-Bug #405563
			final String id = ref.getId();
			final int idx = id.indexOf(':');
			return (idx >= 0) ? id.substring(0, idx) : id;
		}
		
		private int ratePropertyView(final IViewReference viewReference, final EFEditor editor) {
			if (viewReference == null || !getId(viewReference).equals("org.eclipse.ui.views.PropertySheet")) { //$NON-NLS-1$
				return -1;
			}
			final PropertySheet view = (PropertySheet) viewReference.getPart(false);
			if (view != null) {
				if (!(view.getCurrentPage() instanceof EFPropertySheetPage)) {
					return -1;
				}
				final EFPropertySheetPage page = (EFPropertySheetPage) view.getCurrentPage();
				final ITabDescriptor tab = page.getSelectedTab();
				final boolean matches = (page.getEditor() == editor
						&& tab != null && tab.getId().equals(fPropertyTabId));
				if (view.isPinned()) {
					if (matches) {
						return 1;
					}
					return -1;
				}
				final int rate = (view.getViewSite().getPage().isPartVisible(view)) ? 2 : 5;
				if (matches) {
					return rate;
				}
				if (viewReference.getSecondaryId() == null) {
					return rate + 1;
				}
				return rate + 2;
			}
			if (viewReference.getSecondaryId() == null) {
				return 8;
			}
			return 9;
		}
		
		private void showPropertyTab(final PropertySheet view) {
			final EFPropertySheetPage efPage = (EFPropertySheetPage) view.getCurrentPage();
			efPage.setSelectedTab(fPropertyTabId);
		}
		
	}
	
	
	private Hyperlink fLink;
	
	private PropertyTabLink fDescriptor;
	
	
	public HyperlinkType(final RExprTypeUIAdapter type) {
		super(type);
	}
	
	
	@Override
	public boolean hasDetail() {
		return true;
	}
	
	@Override
	protected Control createDetailControl(final Composite parent) {
		final IEMFPropertyContext context = (IEMFPropertyContext) getContext();
		final IHyperlinkProvider linkProvider = (IHyperlinkProvider) getContext().getAdapter(IHyperlinkProvider.class);
		
		if (context == null || linkProvider == null) {
			return null;
		}
		fDescriptor = linkProvider.get(context.getEClass(), context.getEFeature());
		
		final EFEditor editor = (EFEditor) context.getAdapter(EFEditor.class);
		final EFToolkit toolkit = editor.getToolkit();
		fLink = toolkit.createHyperlink(parent, fDescriptor.fLabel, SWT.NONE);
		fLink.addHyperlinkListener(this);
		return fLink;
	}
	
	@Override
	public void linkEntered(final HyperlinkEvent e) {
	}
	
	@Override
	public void linkExited(final HyperlinkEvent e) {
	}
	
	@Override
	public void linkActivated(final HyperlinkEvent e) {
		if (fDescriptor != null) {
			fDescriptor.run(getContext());
		}
	}
	
}
