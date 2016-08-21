/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.intable;

import static de.walware.ecommons.waltable.coordinate.Orientation.HORIZONTAL;
import static de.walware.ecommons.waltable.coordinate.Orientation.VERTICAL;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.waltable.NatTable;
import de.walware.ecommons.waltable.config.AbstractUiBindingConfiguration;
import de.walware.ecommons.waltable.coordinate.Direction;
import de.walware.ecommons.waltable.grid.GridRegion;
import de.walware.ecommons.waltable.resize.ColumnResizeEventMatcher;
import de.walware.ecommons.waltable.resize.InitializeAutoResizeCommand;
import de.walware.ecommons.waltable.resize.PositionResizeDragMode;
import de.walware.ecommons.waltable.resize.action.AutoResizeColumnAction;
import de.walware.ecommons.waltable.resize.action.ColumnResizeCursorAction;
import de.walware.ecommons.waltable.selection.SelectRelativeCellCommand;
import de.walware.ecommons.waltable.selection.action.CellSelectionDragMode;
import de.walware.ecommons.waltable.selection.action.RowSelectionDragMode;
import de.walware.ecommons.waltable.selection.action.SelectCellAction;
import de.walware.ecommons.waltable.sort.ClearSortCommand;
import de.walware.ecommons.waltable.sort.SortDimPositionCommand;
import de.walware.ecommons.waltable.sort.SortDirection;
import de.walware.ecommons.waltable.sort.action.SortColumnAction;
import de.walware.ecommons.waltable.ui.NatEventData;
import de.walware.ecommons.waltable.ui.action.AbstractNavigationAction;
import de.walware.ecommons.waltable.ui.action.ClearCursorAction;
import de.walware.ecommons.waltable.ui.action.NoOpMouseAction;
import de.walware.ecommons.waltable.ui.binding.UiBindingRegistry;
import de.walware.ecommons.waltable.ui.matcher.IMouseEventMatcher;
import de.walware.ecommons.waltable.ui.matcher.KeyEventMatcher;
import de.walware.ecommons.waltable.ui.matcher.MouseEventMatcher;
import de.walware.ecommons.waltable.ui.menu.PopupMenuAction;
import de.walware.ecommons.waltable.viewport.ScrollStepCommand;
import de.walware.ecommons.waltable.viewport.SelectRelativePageCommand;
import de.walware.ecommons.waltable.viewport.action.ViewportSelectDimPositionsAction;


public class UIBindings {
	
	
	private static final int CELL= 1;
	private static final int PAGE= 2;
	private static final int TABLE= 3;
	
	
	public static class ScrollAction extends AbstractNavigationAction {
		
		
		private final int fType;
		
		
		public ScrollAction(final Direction direction, final int type) {
			super(direction);
			
			this.fType= type;
		}
		
		
		@Override
		public void run(final NatTable natTable, final KeyEvent event) {
			switch (this.fType) {
			case CELL:
				natTable.doCommand(new ScrollStepCommand(getDirection()));
				break;
			case PAGE:
				break;
			case TABLE:
				natTable.doCommand(new ScrollStepCommand(getDirection()));
				break;
			default:
				throw new IllegalStateException();
			}
		}
		
	}
	
	public static class SelectRelativeAction extends AbstractNavigationAction {
		
		
		private final int fType;
		
		
		public SelectRelativeAction(final Direction direction, final int type) {
			super(direction);
			
			this.fType= type;
		}
		
		
		@Override
		public void run(final NatTable natTable, final KeyEvent event) {
			final int selectionFlags= (event.stateMask & SWT.SHIFT);
			switch (this.fType) {
			case CELL:
				natTable.doCommand(new SelectRelativeCellCommand(getDirection(), 1, selectionFlags));
				break;
			case PAGE:
				natTable.doCommand(new SelectRelativePageCommand(getDirection(), selectionFlags));
				break;
			case TABLE:
				natTable.doCommand(new SelectRelativeCellCommand(getDirection(), -1, selectionFlags));
				break;
			default:
				throw new IllegalStateException();
			}
		}
		
	}
	
	
	public static class ColumnHeaderConfiguration extends AbstractUiBindingConfiguration {
		
		public ColumnHeaderConfiguration() {
		}
		
		@Override
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			uiBindingRegistry.registerFirstMouseMoveBinding(new ColumnResizeEventMatcher(
					IMouseEventMatcher.NO_BUTTON, true),
					new ColumnResizeCursorAction() );
			uiBindingRegistry.registerMouseMoveBinding(new MouseEventMatcher(),
					new ClearCursorAction() );
			
			uiBindingRegistry.registerFirstMouseDragMode(new ColumnResizeEventMatcher(
					IMouseEventMatcher.LEFT_BUTTON, true),
					new PositionResizeDragMode(HORIZONTAL) );
			
			uiBindingRegistry.registerDoubleClickBinding(new ColumnResizeEventMatcher(
					IMouseEventMatcher.LEFT_BUTTON, true),
					new AutoResizeColumnAction() );
			uiBindingRegistry.registerSingleClickBinding(new ColumnResizeEventMatcher(
					IMouseEventMatcher.LEFT_BUTTON, true),
					new NoOpMouseAction() );
		}
		
	}
	
	
	public static class SelectionConfiguration extends AbstractUiBindingConfiguration {
		
		
		public SelectionConfiguration() {
		}
		
		
		@Override
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			// scroll/roll navigation
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_UP),
					new ScrollAction(Direction.UP, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_DOWN),
					new ScrollAction(Direction.DOWN, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_LEFT),
					new ScrollAction(Direction.LEFT, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_RIGHT),
					new ScrollAction(Direction.RIGHT, CELL) );
			
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SCROLL_LOCK, SWT.PAGE_UP),
//					new ScrollAction(Direction.UP, Scale.PAGE) );
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SCROLL_LOCK, SWT.PAGE_DOWN),
//					new ScrollAction(Direction.DOWN, Scale.PAGE) );
			
			// move anchor
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_UP),
					new SelectRelativeAction(Direction.UP, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_DOWN),
					new SelectRelativeAction(Direction.DOWN, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_LEFT),
					new SelectRelativeAction(Direction.LEFT, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_RIGHT),
					new SelectRelativeAction(Direction.RIGHT, CELL) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_UP),
					new SelectRelativeAction(Direction.UP, PAGE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_DOWN),
					new SelectRelativeAction(Direction.DOWN, PAGE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.HOME),
					new SelectRelativeAction(Direction.LEFT, TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.END),
					new SelectRelativeAction(Direction.RIGHT, TABLE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.HOME),
					new SelectRelativeAction(Direction.UP, TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.END),
					new SelectRelativeAction(Direction.DOWN, TABLE) );
			
			// resize selection
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_UP),
					new SelectRelativeAction(Direction.UP, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_DOWN),
					new SelectRelativeAction(Direction.DOWN, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_LEFT),
					new SelectRelativeAction(Direction.LEFT, CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_RIGHT),
					new SelectRelativeAction(Direction.RIGHT, CELL) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.PAGE_UP),
					new SelectRelativeAction(Direction.UP, PAGE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.PAGE_DOWN),
					new SelectRelativeAction(Direction.DOWN, PAGE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.HOME),
					new SelectRelativeAction(Direction.LEFT, TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.END),
					new SelectRelativeAction(Direction.RIGHT, TABLE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CTRL, SWT.HOME),
					new SelectRelativeAction(Direction.UP, TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CTRL, SWT.END),
					new SelectRelativeAction(Direction.DOWN, TABLE) );
			
			
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.ARROW_UP), new MoveToFirstRowAction());
//			
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.CR), new MoveSelectionAction(Direction.UP, false, false));
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.CR), action);
//			
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.TAB), action);
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.TAB), action);
//			
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.TAB), new MoveSelectionAction(Direction.LEFT, false, false));
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CONTROL, SWT.TAB), action);
//			
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.CR), action);
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.CR), action);
			
			// mouse
			uiBindingRegistry.registerMouseDownBinding(
					new MouseEventMatcher(MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT,
							GridRegion.BODY, IMouseEventMatcher.LEFT_BUTTON ),
					new SelectCellAction());
			
			uiBindingRegistry.registerMouseDownBinding(
					new MouseEventMatcher(MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT,
							GridRegion.COLUMN_HEADER, IMouseEventMatcher.LEFT_BUTTON ),
					new ViewportSelectDimPositionsAction(HORIZONTAL));
			
			uiBindingRegistry.registerMouseDownBinding(
					new MouseEventMatcher(MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT,
							GridRegion.ROW_HEADER, IMouseEventMatcher.LEFT_BUTTON ),
					new ViewportSelectDimPositionsAction(VERTICAL));
			
			uiBindingRegistry.registerMouseDragMode(
					new MouseEventMatcher(MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT,
							GridRegion.BODY, IMouseEventMatcher.LEFT_BUTTON ),
					new CellSelectionDragMode());
			
			uiBindingRegistry.registerMouseDragMode(
					new MouseEventMatcher(MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT,
							GridRegion.ROW_HEADER, IMouseEventMatcher.LEFT_BUTTON ),
					new RowSelectionDragMode());
		}
		
	}
	
	public static class SortConfiguration extends AbstractUiBindingConfiguration {
		
		
		public SortConfiguration() {
		}
		
		
		@Override
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			uiBindingRegistry.registerSingleClickBinding(
					new MouseEventMatcher(SWT.ALT, GridRegion.COLUMN_HEADER, 1),
					new SortColumnAction(false));
		}
		
	}
	
	
	public static class HeaderContextMenuConfiguration extends AbstractUiBindingConfiguration {
		
		
		private final MenuManager fMenuManager;
		
		
		public HeaderContextMenuConfiguration(final NatTable natTable) {
			this.fMenuManager= new MenuManager();
			this.fMenuManager.createContextMenu(natTable);
			natTable.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent e) {
					HeaderContextMenuConfiguration.this.fMenuManager.dispose();
				}
			});
			
			this.fMenuManager.add(new Separator("sorting"));
			this.fMenuManager.add(new NatTableContributionItem(
					SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SORT_ALPHA_IMAGE_ID), null,
					"Sort Increasing by Column", "I") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new SortDimPositionCommand(
							eventData.getNatTable().getDim(HORIZONTAL),
							eventData.getColumnPosition(), SortDirection.ASC, false ));
				}
			});
			this.fMenuManager.add(new NatTableContributionItem("Sort Decreasing by Column", "D") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new SortDimPositionCommand(
							eventData.getNatTable().getDim(HORIZONTAL),
							eventData.getColumnPosition(), SortDirection.DESC, false ));
				}
			});
			this.fMenuManager.add(new NatTableContributionItem("Clear All Sorting", "O") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new ClearSortCommand());
				}
			});
			
			this.fMenuManager.add(new Separator());
			this.fMenuManager.add(new NatTableContributionItem("Auto Resize Column", "R") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new InitializeAutoResizeCommand(
							eventData.getNatTable().getDim(HORIZONTAL), eventData.getColumnPosition() ));
				}
			});
		}
		
		@Override
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			uiBindingRegistry.registerSingleClickBinding(
					new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, IMouseEventMatcher.RIGHT_BUTTON),
					new PopupMenuAction(this.fMenuManager.getMenu()));
		}
		
		public IMenuManager getMenuManager() {
			return this.fMenuManager;
		}
		
	}
	
}
