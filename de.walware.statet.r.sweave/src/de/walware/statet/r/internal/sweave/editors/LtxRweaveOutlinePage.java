/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.editors;

import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_EXPAND_ALL;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelElement.Filter;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.sourceediting.OutlineContentProvider;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor2OutlinePage;
import de.walware.ecommons.ltk.ui.util.ViewerDragSupport;
import de.walware.ecommons.ltk.ui.util.ViewerDropSupport;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.ViewerUtil;

import de.walware.docmlet.tex.core.commands.TexCommand;
import de.walware.docmlet.tex.core.model.IEmbeddedForeignElement;
import de.walware.docmlet.tex.core.model.ILtxModelInfo;
import de.walware.docmlet.tex.core.model.ILtxSourceElement;
import de.walware.docmlet.tex.core.model.TexModel;
import de.walware.docmlet.tex.ui.TexUIResources;

import de.walware.statet.base.ui.IStatetUIMenuIds;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.sweave.ui.tex.sourceediting.LtxROutlineContentProvider;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.sweave.TexRweaveLabelProvider;


public class LtxRweaveOutlinePage extends SourceEditor2OutlinePage {
	
	
	private static final String EXPAND_ELEMENTS_COMMAND_ID = "de.walware.ecommons.base.commands.ExpandElements"; //$NON-NLS-1$
	
	
	private static boolean isRChunk(final IModelElement element) {
		if (element instanceof IEmbeddedForeignElement) {
			final ISourceStructElement foreignElement = ((IEmbeddedForeignElement) element).getForeignElement();
			if (foreignElement != null && foreignElement.getModelTypeId() == RModel.TYPE_ID) {
				return true;
			}
		}
		return false;
	}
	
	
	private static Map<String, String> SECTION_PARAMETERS = Collections.singletonMap("type", "sections"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Map<String, String> CHAPTER_PARAMETERS = Collections.singletonMap("type", "chapters"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Map<String, String> SUBSECTION_PARAMETERS = Collections.singletonMap("type", "subsections"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Map<String, String> RCHUNK_PARAMETERS = Collections.singletonMap("type", "rchunks"); //$NON-NLS-1$ //$NON-NLS-2$
	
	
	private class FilterRChunks extends AbstractToggleHandler {
		
		public FilterRChunks() {
			super("filter.r_chunks.enabled", false, null, 0); //$NON-NLS-1$
		}
		
		@Override
		protected void apply(final boolean on) {
			final TreeViewer viewer = getViewer();
			fFilter.hideRChunks = on;
			if (UIAccess.isOkToUse(viewer)) {
				viewer.refresh(false);
			}
		}
		
	}
	
	public class ExpandElementsContributionItem extends HandlerContributionItem {
		
		private final IHandler2 fExpandElementsHandler;
		
		public ExpandElementsContributionItem(
				final IServiceLocator serviceLocator, final HandlerCollection handlers) {
			super(new CommandContributionItemParameter(serviceLocator,
					".ExpandElements", NAVIGATE_EXPAND_ALL, null, //$NON-NLS-1$
					null, null, null,
					"Expand All", "E", null,
					HandlerContributionItem.STYLE_PULLDOWN, null, false ),
					handlers.get(NAVIGATE_EXPAND_ALL) );
			fExpandElementsHandler = handlers.get(EXPAND_ELEMENTS_COMMAND_ID);
		}
		
		
		@Override
		protected void initDropDownMenu(final MenuManager menuManager) {
			menuManager.addMenuListener(new IMenuListener2() {
				@Override
				public void menuAboutToShow(final IMenuManager manager) {
					final TreeViewer viewer = getViewer();
					if (viewer == null) {
						return;
					}
					
					final ILtxModelInfo modelInfo = getCurrentInputModel();
					final TexUIResources texResources = TexUIResources.INSTANCE;
					final ImageRegistry sweaveImages = SweavePlugin.getDefault().getImageRegistry();
					if (modelInfo.getMinSectionLevel() > 0) {
						if (modelInfo.getMinSectionLevel() < TexCommand.CHAPTER_LEVEL
								&& modelInfo.getMaxSectionLevel() >= TexCommand.CHAPTER_LEVEL) {
							manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
									getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, CHAPTER_PARAMETERS,
									texResources.getImageDescriptor(TexUIResources.OBJ_CHAPTER_IMAGE_ID), null, null,
									"Show all Chapters", "C", null,
									HandlerContributionItem.STYLE_PUSH, null, false ),
									fExpandElementsHandler ));
						}
						if (modelInfo.getMinSectionLevel() < TexCommand.SECTION_LEVEL
								&& modelInfo.getMaxSectionLevel() >= TexCommand.SECTION_LEVEL) {
							manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
									getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, SECTION_PARAMETERS,
									texResources.getImageDescriptor(TexUIResources.OBJ_SECTION_IMAGE_ID), null, null,
									"Show all Sections", "S", null,
									HandlerContributionItem.STYLE_PUSH, null, false ),
									fExpandElementsHandler ));
						}
						if (modelInfo.getMinSectionLevel() < TexCommand.SUBSECTION_LEVEL
								&& modelInfo.getMaxSectionLevel() >= TexCommand.SUBSECTION_LEVEL) {
							manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
									getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, SUBSECTION_PARAMETERS,
									texResources.getImageDescriptor(TexUIResources.OBJ_SUBSECTION_IMAGE_ID), null, null,
									"Show all SubSections", "u", null,
									HandlerContributionItem.STYLE_PUSH, null, false ),
									fExpandElementsHandler ));
							manager.add(new Separator());
						}
					}
					if (!fFilter.hideRChunks) {
						manager.add(new HandlerContributionItem(new CommandContributionItemParameter(
								getSite(), null, EXPAND_ELEMENTS_COMMAND_ID, RCHUNK_PARAMETERS,
								sweaveImages.getDescriptor(SweavePlugin.IMG_OBJ_RCHUNK), null, null,
								"Show all R Chunks", "R", null,
								HandlerContributionItem.STYLE_PUSH, null, false ),
								fExpandElementsHandler ));
					}
				}
				@Override
				public void menuAboutToHide(final IMenuManager manager) {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							menuManager.dispose();
						}
					});
				}
			});
		}
		
	}
	
	public class ExpandElementsHandler extends AbstractHandler {
		
		public ExpandElementsHandler() {
		}
		
		@Override
		public Object execute(final ExecutionEvent event) {
			final TreeViewer viewer = getViewer();
			final String type = event.getParameter("type");
			if (UIAccess.isOkToUse(viewer) && type != null) {
				final ISourceUnitModelInfo modelInfo = getModelInfo(viewer.getInput());
				if (modelInfo == null) {
					return null;
				}
				final Filter contentFilter = getContentFilter();
				final Filter expandFilter;
				if (type.equals("rchunks")) {
					expandFilter = new Filter() {
						@Override
						public boolean include(final IModelElement element) {
							if (contentFilter.include(element)) {
								if ((element.getElementType() & IModelElement.MASK_C1) == ILtxSourceElement.C1_EMBEDDED
										&& isRChunk(element)) {
									ViewerUtil.expandToLevel(viewer, element, 0);
									return false;
								}
								((ISourceStructElement) element).hasSourceChildren(this);
								return false;
							}
							return false;
						}
					};
				}
				else {
					final int sectionLevel;
					if (type.equals("chapters")) {
						sectionLevel = 2;
					}
					else if (type.equals("sections")) {
						sectionLevel = 3;
					}
					else if (type.equals("subsections")) {
						sectionLevel = 4;
					}
					else {
						sectionLevel = 0;
					}
					if (sectionLevel < 1 || sectionLevel > 5) {
						return null;
					}
					expandFilter = new Filter() {
						private boolean childExpand;
						@Override
						public boolean include(final IModelElement element) {
							if (contentFilter.include(element)
									&& (element.getElementType() & IModelElement.MASK_C2) == ILtxSourceElement.C2_SECTIONING) {
								final int currentLevel = (element.getElementType() & 0xf);
								if (currentLevel < 1 || currentLevel > sectionLevel) {
									return false; // nothing to do
								}
								if (currentLevel < sectionLevel) {
									childExpand = false;
									((ISourceStructElement) element).hasSourceChildren(this);
									if (childExpand) {
										return false; // done
									}
								}
								// expand
								ViewerUtil.expandToLevel(viewer, element, 0);
								childExpand = true;
								return false;
							}
							return false;
						}
					};
				}
				modelInfo.getSourceElement().hasSourceChildren(expandFilter);
			}
			return null;
		}
	}
	
	
	private class ContentFilter implements IModelElement.Filter {
		
		private boolean hideRChunks;
		
		@Override
		public boolean include(final IModelElement element) {
			switch ((element.getElementType() & IModelElement.MASK_C1)) {
			case ILtxSourceElement.C1_EMBEDDED:
				if (isRChunk(element)) {
					return !hideRChunks;
				}
			}
			return true;
		};
		
	}
	
	
	private final ContentFilter fFilter = new ContentFilter();
	
	
	public LtxRweaveOutlinePage(final SourceEditor1 editor) {
		super(editor, TexModel.LTX_TYPE_ID, new LtxRweaveRefactoringFactory(),
				"de.walware.statet.r.menus.LtxOutlineViewContextMenu"); //$NON-NLS-1$
	}
	
	
	@Override
	protected IDialogSettings getDialogSettings() {
		return DialogUtil.getDialogSettings(SweavePlugin.getDefault(), "SweaveOutlineView"); //$NON-NLS-1$
	}
	
	@Override
	protected OutlineContentProvider createContentProvider() {
		return new LtxROutlineContentProvider(new OutlineContent());
	}
	
	@Override
	protected IModelElement.Filter getContentFilter() {
		return fFilter;
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		super.configureViewer(viewer);
		
		viewer.setLabelProvider(new TexRweaveLabelProvider(0));
		
		final ViewerDropSupport drop = new ViewerDropSupport(viewer, this,
				getRefactoringFactory() );
		drop.init();
		final ViewerDragSupport drag = new ViewerDragSupport(viewer);
		drag.init();
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator,
			final HandlerCollection handlers) {
		super.initActions(serviceLocator, handlers);
		
		final IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
		
		handlers.add(".FilterRChunks", new FilterRChunks()); //$NON-NLS-1$
		
		{	final IHandler2 handler = new ExpandElementsHandler();
			handlers.add(EXPAND_ELEMENTS_COMMAND_ID, handler);
//			handlerService.activateHandler(EXPAND_ELEMENTS_COMMAND_ID, handler); //$NON-NLS-1$
		}
	}
	
	@Override
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final IActionBars actionBars, final HandlerCollection handlers) {
		super.contributeToActionBars(serviceLocator, actionBars, handlers);
		
		final IToolBarManager toolBarManager = actionBars.getToolBarManager();
		final ImageRegistry sweaveImages = SweavePlugin.getDefault().getImageRegistry();
		
		toolBarManager.appendToGroup(SharedUIResources.VIEW_EXPAND_MENU_ID,
				new ExpandElementsContributionItem(serviceLocator, handlers));
		
//		toolBarManager.appendToGroup(ECommonsUI.VIEW_SORT_MENU_ID,
//				new AlphaSortAction());
		toolBarManager.appendToGroup(SharedUIResources.VIEW_FILTER_MENU_ID,
				new HandlerContributionItem(new CommandContributionItemParameter(serviceLocator,
						null, HandlerContributionItem.NO_COMMAND_ID, null,
						sweaveImages.getDescriptor(SweavePlugin.IMG_LOCTOOL_FILTERCHUNKS), null, null,
						"Hide R Chunks", "R", null,
						HandlerContributionItem.STYLE_CHECK, null, false ),
						handlers.get(".FilterRChunks") ) ); //$NON-NLS-1$
//		toolBarManager.appendToGroup(ECommonsUI.VIEW_FILTER_MENU_ID,
//				new FilterLocalDefinitions());
	}
	
	@Override
	protected void contextMenuAboutToShow(final IMenuManager m) {
		super.contextMenuAboutToShow(m);
		final IPageSite site = getSite();
		
		if (m.find(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID) == null) {
			m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID,
					new Separator(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID) );
		}
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, 
				new CommandContributionItem(new CommandContributionItemParameter(
						site, null, RCodeLaunching.SUBMIT_SELECTION_COMMAND_ID, null,
						null, null, null,
						null, "R", null, //$NON-NLS-1$
						CommandContributionItem.STYLE_PUSH, null, false) ));
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, 
				new CommandContributionItem(new CommandContributionItemParameter(
						site, null, RCodeLaunching.SUBMIT_UPTO_SELECTION_COMMAND_ID, null,
						null, null, null,
						null, "U", null, //$NON-NLS-1$
						CommandContributionItem.STYLE_PUSH, null, false) ));
		
		m.add(new Separator(IStatetUIMenuIds.GROUP_ADD_MORE_ID));
	}
	
	protected ILtxModelInfo getCurrentInputModel() {
		final TreeViewer viewer = getViewer();
		if (viewer == null) {
			return null;
		}
		return (ILtxModelInfo) getModelInfo(viewer.getInput());
	}
	
}
