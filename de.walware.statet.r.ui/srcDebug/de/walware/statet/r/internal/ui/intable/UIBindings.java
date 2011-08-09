/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.intable;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;

import de.walware.ecommons.ui.SharedUIResources;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.coordinate.IRelative.Direction;
import net.sourceforge.nattable.coordinate.IRelative.Scale;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.resize.action.AutoResizeColumnAction;
import net.sourceforge.nattable.resize.action.ColumnResizeCursorAction;
import net.sourceforge.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import net.sourceforge.nattable.resize.event.ColumnResizeEventMatcher;
import net.sourceforge.nattable.resize.mode.ColumnResizeDragMode;
import net.sourceforge.nattable.selection.action.CellSelectionDragMode;
import net.sourceforge.nattable.selection.action.RowSelectionDragMode;
import net.sourceforge.nattable.selection.action.SelectCellAction;
import net.sourceforge.nattable.selection.command.ISelectionCommand.SelectionFlag;
import net.sourceforge.nattable.selection.command.SelectRelativelyCommand;
import net.sourceforge.nattable.sort.SortDirectionEnum;
import net.sourceforge.nattable.sort.action.SortColumnAction;
import net.sourceforge.nattable.sort.command.ClearSortCommand;
import net.sourceforge.nattable.sort.command.SortColumnCommand;
import net.sourceforge.nattable.ui.NatEventData;
import net.sourceforge.nattable.ui.action.ClearCursorAction;
import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.action.NoOpMouseAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.IMouseEventMatcher;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.ui.menu.PopupMenuAction;
import net.sourceforge.nattable.viewport.action.ViewportSelectColumnAction;
import net.sourceforge.nattable.viewport.action.ViewportSelectRowAction;
import net.sourceforge.nattable.viewport.command.ScrollCommand;


public class UIBindings {
	
	
	public static abstract class AbstractNavigationAction implements IKeyAction {
		
		
		private final Direction fDirection;
		
		private final Scale fStepSize;
		
		
		public AbstractNavigationAction(final Direction direction, final Scale stepSize) {
			fDirection = direction;
			fStepSize = stepSize;
		}
		
		
		public Direction getDirection() {
			return fDirection;
		}
		
		public Scale getStepSize() {
			return fStepSize;
		}
		
	}
	
	public static class ScrollAction extends AbstractNavigationAction {
		
		
		public ScrollAction(final Direction direction, final Scale stepSize) {
			super(direction, stepSize);
		}
		
		
		public void run(final NatTable natTable, final KeyEvent event) {
			natTable.doCommand(new ScrollCommand(getDirection(), getStepSize()));
		}
		
	}
	
	public static class RelativeSelectionAction extends AbstractNavigationAction {
		
		
		public RelativeSelectionAction(final Direction direction, final Scale stepSize) {
			super(direction, stepSize);
		}
		
		
		public void run(final NatTable natTable, final KeyEvent event) {
			final Set<SelectionFlag> flags = ((event.stateMask & SWT.SHIFT) == SWT.SHIFT) ?
					EnumSet.of(SelectionFlag.RANGE_SELECTION) : SelectionFlag.NONE;
			natTable.doCommand(new SelectRelativelyCommand(getDirection(), getStepSize(), flags));
		}
		
	}
	
	
	public static class ColumnHeaderConfiguration extends AbstractUiBindingConfiguration {
		
		public ColumnHeaderConfiguration() {
		}
		
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
		
		
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			// scroll/roll navigation
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_UP),
					new ScrollAction(Direction.UP, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_DOWN),
					new ScrollAction(Direction.DOWN, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_LEFT),
					new ScrollAction(Direction.LEFT, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.ARROW_RIGHT),
					new ScrollAction(Direction.RIGHT, Scale.CELL) );
			
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SCROLL_LOCK, SWT.PAGE_UP),
//					new ScrollAction(Direction.UP, Scale.PAGE) );
//			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SCROLL_LOCK, SWT.PAGE_DOWN),
//					new ScrollAction(Direction.DOWN, Scale.PAGE) );
			
			// move anchor
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_UP),
					new RelativeSelectionAction(Direction.UP, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_DOWN),
					new RelativeSelectionAction(Direction.DOWN, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_LEFT),
					new RelativeSelectionAction(Direction.LEFT, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_RIGHT),
					new RelativeSelectionAction(Direction.RIGHT, Scale.CELL) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_UP),
					new RelativeSelectionAction(Direction.UP, Scale.PAGE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_DOWN),
					new RelativeSelectionAction(Direction.DOWN, Scale.PAGE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.HOME),
					new RelativeSelectionAction(Direction.LEFT, Scale.TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.END),
					new RelativeSelectionAction(Direction.RIGHT, Scale.TABLE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.HOME),
					new RelativeSelectionAction(Direction.UP, Scale.TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, SWT.END),
					new RelativeSelectionAction(Direction.DOWN, Scale.TABLE) );
			
			// resize selection
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_UP),
					new RelativeSelectionAction(Direction.UP, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_DOWN),
					new RelativeSelectionAction(Direction.DOWN, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_LEFT),
					new RelativeSelectionAction(Direction.LEFT, Scale.CELL) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_RIGHT),
					new RelativeSelectionAction(Direction.RIGHT, Scale.CELL) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.PAGE_UP),
					new RelativeSelectionAction(Direction.UP, Scale.PAGE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.PAGE_DOWN),
					new RelativeSelectionAction(Direction.DOWN, Scale.PAGE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.HOME),
					new RelativeSelectionAction(Direction.LEFT, Scale.TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.END),
					new RelativeSelectionAction(Direction.RIGHT, Scale.TABLE) );
			
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CTRL, SWT.HOME),
					new RelativeSelectionAction(Direction.UP, Scale.TABLE) );
			uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.CTRL, SWT.END),
					new RelativeSelectionAction(Direction.DOWN, Scale.TABLE) );
			
			
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
			uiBindingRegistry.registerFirstSingleClickBinding(
					new MouseEventMatcher(GridRegion.BODY, IMouseEventMatcher.LEFT_BUTTON,
							MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT),
					new SelectCellAction());
			
			uiBindingRegistry.registerSingleClickBinding(
					new MouseEventMatcher(GridRegion.COLUMN_HEADER, IMouseEventMatcher.LEFT_BUTTON,
							MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT),
					new ViewportSelectColumnAction());
			
			uiBindingRegistry.registerSingleClickBinding(
					new MouseEventMatcher(GridRegion.ROW_HEADER, IMouseEventMatcher.LEFT_BUTTON,
							MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT),
					new ViewportSelectRowAction());
			
			uiBindingRegistry.registerMouseDragMode(
					new MouseEventMatcher(GridRegion.BODY, IMouseEventMatcher.LEFT_BUTTON,
							MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT),
					new CellSelectionDragMode());
			
			uiBindingRegistry.registerMouseDragMode(
					new MouseEventMatcher(GridRegion.ROW_HEADER, IMouseEventMatcher.LEFT_BUTTON,
							MouseEventMatcher.WILDCARD_MASK | SWT.CTRL | SWT.SHIFT),
					new RowSelectionDragMode());
		}
		
	}
	
	public static class SortConfiguration extends AbstractUiBindingConfiguration {
		
		
		public SortConfiguration() {
		}
		
		
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			uiBindingRegistry.registerSingleClickBinding(
					new MouseEventMatcher(GridRegion.COLUMN_HEADER, 1, SWT.ALT),
					new SortColumnAction(false));
		}
		
	}
	
	
	public static class HeaderContextMenuConfiguration extends AbstractUiBindingConfiguration {
		
		
		private final MenuManager fMenuManager;
		
		
		public HeaderContextMenuConfiguration(final NatTable natTable) {
			fMenuManager = new MenuManager();
			fMenuManager.createContextMenu(natTable);
			natTable.addDisposeListener(new DisposeListener() {
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
							eventData.getNatTable(), eventData.getColumnPosition(), eventData.getNatTable() ));
				}
			});
		}
		
		public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
			uiBindingRegistry.registerSingleClickBinding(
					new MouseEventMatcher(GridRegion.COLUMN_HEADER, IMouseEventMatcher.RIGHT_BUTTON, SWT.NONE),
					new PopupMenuAction(fMenuManager.getMenu()));
		}
		
		public IMenuManager getMenuManager() {
			return fMenuManager;
		}
		
	}
	
}
