/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.intable;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;
import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.Direction;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeColumnAction;
import org.eclipse.nebula.widgets.nattable.resize.action.ColumnResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.ColumnResizeDragMode;
import org.eclipse.nebula.widgets.nattable.selection.action.CellSelectionDragMode;
import org.eclipse.nebula.widgets.nattable.selection.action.RowSelectionDragMode;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectCellAction;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRelativeCellCommand;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.action.SortColumnAction;
import org.eclipse.nebula.widgets.nattable.sort.command.ClearSortCommand;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.AbstractNavigationAction;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectDimPositionsAction;
import org.eclipse.nebula.widgets.nattable.viewport.command.ScrollStepCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.SelectRelativePageCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;

import de.walware.ecommons.ui.SharedUIResources;


public class UIBindings {
	
	
	private static final int CELL = 1;
	private static final int PAGE = 2;
	private static final int TABLE = 3;
	
	
	public static class ScrollAction extends AbstractNavigationAction {
		
		
		private final int fType;
		
		
		public ScrollAction(final Direction direction, final int type) {
			super(direction);
			
			fType = type;
		}
		
		
		@Override
		public void run(final NatTable natTable, final KeyEvent event) {
			switch (fType) {
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
			
			fType = type;
		}
		
		
		@Override
		public void run(final NatTable natTable, final KeyEvent event) {
			final int selectionFlags = (event.stateMask & SWT.SHIFT);
			switch (fType) {
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
					new ColumnResizeDragMode() );
			
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
			fMenuManager = new MenuManager();
			fMenuManager.createContextMenu(natTable);
			natTable.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent e) {
					fMenuManager.dispose();
				}
			});
			
			fMenuManager.add(new Separator("sorting"));
			fMenuManager.add(new NatTableContributionItem(
					SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SORT_ALPHA_IMAGE_ID), null,
					"Sort Increasing by Column", "I") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new SortColumnCommand(
							eventData.getNatTable(), eventData.getColumnPosition(),
							SortDirectionEnum.ASC, false ));
				}
			});
			fMenuManager.add(new NatTableContributionItem("Sort Decreasing by Column", "D") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new SortColumnCommand(
							eventData.getNatTable(), eventData.getColumnPosition(),
							SortDirectionEnum.DESC, false ));
				}
			});
			fMenuManager.add(new NatTableContributionItem("Clear All Sorting", "O") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new ClearSortCommand());
				}
			});
			
			fMenuManager.add(new Separator());
			fMenuManager.add(new NatTableContributionItem("Auto Resize Column", "R") {
				@Override
				protected void execute(final NatEventData eventData) throws ExecutionException {
					eventData.getNatTable().doCommand(new InitializeAutoResizeColumnsCommand(
							eventData.getNatTable(), eventData.getColumnPosition() ));
				}
			});
		}
		
		@Override
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			uiBindingRegistry.registerSingleClickBinding(
					new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, IMouseEventMatcher.RIGHT_BUTTON),
					new PopupMenuAction(fMenuManager.getMenu()));
		}
		
		public IMenuManager getMenuManager() {
			return fMenuManager;
		}
		
	}
	
}
