/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import java.util.List;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.AbstractMarkOccurrencesProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceFragmentEditorInput;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.actions.SpecificContentAssistHandler;
import de.walware.ecommons.ltk.ui.sourceediting.folding.FoldingEditorAddon;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.base.ui.IStatetUIMenuIds;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIHelp;
import de.walware.statet.r.ui.editors.IREditor;
import de.walware.statet.r.ui.editors.RCorrectIndentHandler;
import de.walware.statet.r.ui.editors.RDefaultFoldingProvider;
import de.walware.statet.r.ui.editors.REditorOptions;
import de.walware.statet.r.ui.editors.RMarkOccurrencesLocator;
import de.walware.statet.r.ui.sourceediting.InsertAssignmentHandler;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;


public class REditor extends SourceEditor1 implements IREditor {
	
	public static IRCoreAccess getRCoreAccess(final ISourceEditor editor) {
		final IRCoreAccess adapter = (IRCoreAccess) editor.getAdapter(IRCoreAccess.class);
		return (adapter != null) ? adapter : RCore.getWorkbenchAccess();
	}
	
	
	private static class MarkOccurrencesProvider extends AbstractMarkOccurrencesProvider {
		
		
		private final RMarkOccurrencesLocator fLocator = new RMarkOccurrencesLocator();
		
		
		public MarkOccurrencesProvider(final SourceEditor1 editor) {
			super(editor, IRDocumentPartitions.R_PARTITIONING_CONFIG.getDefaultPartitionConstraint() );
		}
		
		@Override
		protected void doUpdate(final RunData run, final ISourceUnitModelInfo info,
				final AstSelection astSelection, final ITextSelection orgSelection)
				throws BadLocationException, BadPartitioningException, UnsupportedOperationException {
			fLocator.run(run, info, astSelection, orgSelection);
		}
		
	}
	
	
	protected RSourceViewerConfigurator fRConfig;
	protected IContextProvider fHelpContextProvider;
	
	
	public REditor() {
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
		setHelpContextId(IRUIHelpContextIds.R_EDITOR);
		setEditorContextMenuId("de.walware.statet.r.menus.REditorContextMenu"); //$NON-NLS-1$
		setRulerContextMenuId("de.walware.statet.r.menus.REditorRulerMenu"); //$NON-NLS-1$
	}
	
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		setDocumentProvider(RUIPlugin.getDefault().getRDocumentProvider());
		
		enableStructuralFeatures(RCore.getRModelManager(),
				REditorOptions.FOLDING_ENABLED_PREF,
				REditorOptions.PREF_MARKOCCURRENCES_ENABLED );
		
		final IRCoreAccess initAccess = RCore.getWorkbenchAccess();
		fRConfig = new RSourceViewerConfigurator(initAccess,
				new RSourceViewerConfiguration(this, null, null, SharedUIResources.getColors()) );
		return fRConfig;
	}
	
	@Override
	protected SourceEditorViewerConfigurator createInfoConfigurator() {
		return new RSourceViewerConfigurator(getRCoreAccess(), new RSourceViewerConfiguration(
				null, SharedUIResources.getColors() ));
	}
	
	@Override
	protected void setDocumentProvider(final IEditorInput input) {
		if (input instanceof ISourceFragmentEditorInput) {
			setDocumentProvider(RUIPlugin.getDefault().getRFragmentDocumentProvider());
			overwriteTitleImage(input.getImageDescriptor());
		}
		else {
			setDocumentProvider(RUIPlugin.getDefault().getRDocumentProvider());
			overwriteTitleImage(null);
		}
	}
	
	@Override
	protected Image getDefaultImage() {
		return RUIPlugin.getDefault().getImageRegistry().get(RUI.IMG_OBJ_R_SCRIPT);
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		
		// Editor Help:
		final SourceViewer viewer = (SourceViewer) getSourceViewer();
		fHelpContextProvider = RUIHelp.createEnrichedRHelpContextProvider(this, IRUIHelpContextIds.R_EDITOR);
		viewer.getTextWidget().addHelpListener(new HelpListener() {
			@Override
			public void helpRequested(final HelpEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(fHelpContextProvider.getContext(null));
			}
		});
	}
	
	@Override
	protected ISourceEditorAddon createCodeFoldingProvider() {
		return new FoldingEditorAddon(new RDefaultFoldingProvider());
	}
	
	@Override
	protected ISourceEditorAddon createMarkOccurrencesProvider() {
		return new MarkOccurrencesProvider(this);
	}
	
	
	@Override
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public IRSourceUnit getSourceUnit() {
		return (IRSourceUnit) super.getSourceUnit();
	}
	
	protected IRCoreAccess getRCoreAccess() {
		return fRConfig;
	}
	
	@Override
	protected void setupConfiguration(final IEditorInput newInput) {
		super.setupConfiguration(newInput);
		
		final IRSourceUnit su = getSourceUnit();
		fRConfig.setSource((su != null) ? su.getRCoreAccess() : null);
	}
	
	
	@Override
	protected void handlePreferenceStoreChanged(final org.eclipse.jface.util.PropertyChangeEvent event) {
		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(event.getProperty())
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS.equals(event.getProperty())) {
			return;
		}
		super.handlePreferenceStoreChanged(event);
	}
	
	
	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		return false;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] {
				"de.walware.statet.r.contexts.REditor", //$NON-NLS-1$
		});
	}
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		super.collectContextMenuPreferencePages(pageIds);
		pageIds.add("de.walware.statet.r.preferencePages.REditorOptions"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RTextStyles"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RCodeStyle"); //$NON-NLS-1$
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		final IHandlerService handlerService = (IHandlerService) getServiceLocator().getService(IHandlerService.class);
		
		{	final IHandler2 handler = new InsertAssignmentHandler(this);
			handlerService.activateHandler(LTKUI.INSERT_ASSIGNMENT_COMMAND_ID, handler);
			markAsStateDependentHandler(handler, true);
		}
		{	final Action action = new RDoubleCommentAction(this, getRCoreAccess());
			setAction(action.getId(), action);
			markAsStateDependentAction(action.getId(), true);
		}
		{	final IHandler2 handler = new SpecificContentAssistHandler(this, RUIPlugin.getDefault().getREditorContentAssistRegistry());
			handlerService.activateHandler(ISourceEditorCommandIds.SPECIFIC_CONTENT_ASSIST_COMMAND_ID, handler);
		}
		{	final IHandler2 handler = new RStripCommentsHandler(this);
			handlerService.activateHandler(LTKUI.STRIP_COMMENTS_COMMAND_ID, handler);
		}
	}
	
	@Override
	protected IHandler2 createCorrectIndentHandler() {
		final IHandler2 handler = new RCorrectIndentHandler(this);
		markAsStateDependentHandler(handler, true);
		return handler;
	}
	
	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager m) {
		super.editorContextMenuAboutToShow(m);
		final IRSourceUnit su = getSourceUnit();
		
		m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID, new Separator("search")); //$NON-NLS-1$
		
		m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID, new Separator(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID));
		final IContributionItem additions = m.find(SharedUIResources.ADDITIONS_MENU_ID);
		if (additions != null) {
			additions.setVisible(false);
		}
		
		m.remove(ITextEditorActionConstants.SHIFT_RIGHT);
		m.remove(ITextEditorActionConstants.SHIFT_LEFT);
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(new CommandContributionItemParameter(
				getSite(), null, RCodeLaunching.SUBMIT_SELECTION_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(new CommandContributionItemParameter(
				getSite(), null, RCodeLaunching.SUBMIT_UPTO_SELECTION_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
		if (su != null && !su.isReadOnly()) {
			m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(new CommandContributionItemParameter(
					getSite(), null, RCodeLaunching.SUBMIT_SELECTION_PASTEOUTPUT_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
		}
	}
	
	@Override
	protected IRegion getRangeToReveal(final ISourceUnitModelInfo modelInfo, final ISourceStructElement element) {
		final FDef def= (FDef) element.getAdapter(FDef.class);
		if (def != null) {
			final RAstNode cont= def.getContChild();
			final int offset= element.getSourceRange().getOffset();
			int length= cont.getOffset() - offset;
			if (cont.getLength() > 0) {
				length++;
			}
			return new Region(offset, length);
		}
		return null;
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IContextProvider.class.equals(required)) {
			return fHelpContextProvider;
		}
		if (IRCoreAccess.class.equals(required)) {
			return getRCoreAccess();
		}
		return super.getAdapter(required);
	}
	
	@Override
	protected SourceEditor1OutlinePage createOutlinePage() {
		return new ROutlinePage(this);
	}
	
	@Override
	protected ITemplatesPage createTemplatesPage() {
		return new REditorTemplatesPage(this, getSourceViewer());
	}
	
	@Override
	public String[] getShowInTargetIds() {
		return new String[] { IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.ID_OUTLINE, RUI.R_HELP_VIEW_ID };
	}
	
}
