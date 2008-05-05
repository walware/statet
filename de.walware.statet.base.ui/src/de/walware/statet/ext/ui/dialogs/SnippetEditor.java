/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.base.ui.sourceeditors.IEditorInstallable;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;
import de.walware.statet.base.ui.sourceeditors.SourceViewerUpdater;
import de.walware.statet.base.ui.sourceeditors.TextViewerAction;
import de.walware.statet.base.ui.util.SettingsUpdater;


/**
 * Text snippet editor (no Eclipse editor) for support of
 * SourceViewerConfigurator, IEditorAdapter,...
 */
public class SnippetEditor extends Object {
	
	public static final int DEFAULT_SINGLE_LINE_STYLE = SWT.BORDER | SWT.SINGLE | SWT.LEFT_TO_RIGHT;
	public static final int DEFAULT_MULTI_LINE_STYLE = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.LEFT_TO_RIGHT;
	
	
	private static class ViewerEditorAdapter implements IEditorAdapter {
		
		private SourceViewer fSourceViewer;
		private SourceViewerConfigurator fConfigurator;
		
		
		public ViewerEditorAdapter(final SourceViewer viewer, final SourceViewerConfigurator configurator) {
			fSourceViewer = viewer;
			fConfigurator = configurator;
		}
		
		
		public SourceViewer getSourceViewer() {
			return fSourceViewer;
		}
		
		public IWorkbenchPart getWorkbenchPart() {
			return null;
		}
		
		public void install(final IEditorInstallable installable) {
			if (fConfigurator != null) {
				fConfigurator.installModul(installable);
			}
		}
		
		public boolean isEditable(final boolean validate) {
			return true;
		}
		
		public void setStatusLineErrorMessage(final String message) {
		}
		
		public Object getAdapter(final Class adapter) {
			return null;
		}
		
	}
	
	
	private Document fDocument;
	private SourceViewer fSourceViewer;
	
	private SourceViewerConfigurator fConfigurator;
	private Map<String, Action> fGloablActions;
	
	
	/**
	 * 
	 */
	public SnippetEditor(final SourceViewerConfigurator configurator) {
		fConfigurator = configurator;
		fDocument = new Document();
		fConfigurator.getDocumentSetupParticipant().setup(fDocument);
	}
	
	
	public SourceViewer create(final Composite parent, final int style) {
		fSourceViewer = new SourceViewer(parent, null, style);
		
		initSourceViewer();
		
		return fSourceViewer;
	}
	
	protected void initSourceViewer() {
		fSourceViewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		fSourceViewer.setDocument(fDocument);
		
		fConfigurator.setTarget(new ViewerEditorAdapter(fSourceViewer, fConfigurator), true);
		new SourceViewerUpdater(fSourceViewer, fConfigurator.getSourceViewerConfiguration(), fConfigurator.getPreferenceStore());
		new SettingsUpdater(fConfigurator, fSourceViewer.getControl());
		
		initActions();
		fSourceViewer.activatePlugins();
		fSourceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				updateActions();
			}
		});
		fSourceViewer.getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(final DocumentEvent event) {
			}
			public void documentChanged(final DocumentEvent event) {
				updateActions();
			}
		});
	}
	
	private void initActions() {
		fGloablActions = new HashMap<String, Action>(10);
		fGloablActions.put(ITextEditorActionConstants.UNDO, TextViewerAction.createUndoAction(fSourceViewer));
		fGloablActions.put(ITextEditorActionConstants.CUT, TextViewerAction.createCutAction(fSourceViewer));
		fGloablActions.put(ITextEditorActionConstants.COPY, TextViewerAction.createCopyAction(fSourceViewer));
		fGloablActions.put(ITextEditorActionConstants.PASTE, TextViewerAction.createPasteAction(fSourceViewer));
		fGloablActions.put(ITextEditorActionConstants.SELECT_ALL, TextViewerAction.createSelectAllAction(fSourceViewer));
		
		// create context menu
		final MenuManager manager = new MenuManager(null, null);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		
		final StyledText text= fSourceViewer.getTextWidget();
		final Menu menu= manager.createContextMenu(text);
		text.setMenu(menu);
	}
	
	private void updateActions() {
		for (final Action action : fGloablActions.values()) {
			if (action instanceof IUpdate) {
				((IUpdate) action).update();
			}
		}
	}
	
	private void fillContextMenu(final IMenuManager menu) {
		menu.add(new GroupMarker(ITextEditorActionConstants.GROUP_UNDO));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, fGloablActions.get(ITextEditorActionConstants.UNDO));
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_EDIT));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGloablActions.get(ITextEditorActionConstants.CUT));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGloablActions.get(ITextEditorActionConstants.COPY));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGloablActions.get(ITextEditorActionConstants.PASTE));
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fGloablActions.get(ITextEditorActionConstants.SELECT_ALL));
	}
	
	
	public Document getDocument() {
		return fDocument;
	}
	
	public SourceViewer getSourceViewer() {
		return fSourceViewer;
	}
	
	public StyledText getControl() {
		return fSourceViewer.getTextWidget();
	}
	
	public void reset() {
		fSourceViewer.resetPlugins();
		updateActions();
	}
	
}
