package de.walware.ecommons.emf.ui.forms;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;


public class EFRawOutlinePage extends ContentOutlinePage {
	
	
	private final EFEditor fEditor;
	
	/**
	 * This is the content outline page's viewer.
	 * @generated
	 */
	private TreeViewer fViewer;
	
	private IStatusLineManager fContentOutlineStatusLineManager;
	
	
	public EFRawOutlinePage(final EFEditor editor) {
		fEditor = editor;
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		fViewer = getTreeViewer();
		fViewer.addSelectionChangedListener(this);
		
		final EditingDomain editingDomain = fEditor.getEditingDomain();
		
		// Set up the tree viewer
		fViewer.setContentProvider(new AdapterFactoryContentProvider(fEditor.getAdapterFactory()));
		fViewer.setLabelProvider(new AdapterFactoryLabelProvider(fEditor.getAdapterFactory()));
		fViewer.setInput(editingDomain.getResourceSet());
		
		// Make sure our popups work
		fEditor.createRawContextMenuFor(fViewer);
		
		if (!editingDomain.getResourceSet().getResources().isEmpty()) {
			// Select the root object in the view
			fViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);
		}
	}
	
	@Override
	public void makeContributions(final IMenuManager menuManager, final IToolBarManager toolBarManager, final IStatusLineManager statusLineManager) {
		super.makeContributions(menuManager, toolBarManager, statusLineManager);
		fContentOutlineStatusLineManager = statusLineManager;
	}
	
	@Override
	public void setActionBars(final IActionBars actionBars) {
		super.setActionBars(actionBars);
		fEditor.getActionBarContributor().shareGlobalActions(this, actionBars);
	}
	
}
