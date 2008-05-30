/*******************************************************************************
 * Copyright (c) 2006-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - modified to use handlers
 *******************************************************************************/

package de.walware.eclipsecommons.ui;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;


/**
 * A contribution item which delegates to a handler of a command (not using all service).
 * It was created as workaround for toolbars in views (wrong enablement when lost focus).
 * <p>
 * It currently supports placement in menus and toolbars.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public final class HandlerContributionItem extends ContributionItem {
	
	/**
	 * A push button tool item or menu item.
	 */
	public static final int STYLE_PUSH = SWT.PUSH;
	
	/**
	 * A checked tool item or menu item.
	 */
	public static final int STYLE_CHECK = SWT.CHECK;
	
	/**
	 * A radio-button style menu item.
	 */
	public static final int STYLE_RADIO = SWT.RADIO;
	
	/**
	 * A ToolBar pulldown item.
	 */
	public static final int STYLE_PULLDOWN = SWT.DROP_DOWN;
	
	/**
	 * Mode bit: Show text on tool items or buttons, even if an image is
	 * present. If this mode bit is not set, text is only shown on tool items if
	 * there is no image present.
	 * 
	 * @since 3.4
	 */
	public static int MODE_FORCE_TEXT = 1;

	private LocalResourceManager localResourceManager;
	
	private Listener menuItemListener;
	
	private Widget widget;
	
	private IMenuService menuService;
	
	private ICommandService commandService;
	
	private IHandlerService handlerService;
	
	private IBindingService bindingService;
	
	private ParameterizedCommand command;
	private IHandler2 commandHandler;
	
	private ImageDescriptor icon;
	
	private String label;
	
	private String tooltip;
	
	private ImageDescriptor disabledIcon;
	
	private ImageDescriptor hoverIcon;
	
	private String mnemonic;
	
	private IElementReference elementRef;
	
	private boolean checkedState;
	
	private int style;
	
	private IHandlerListener commandListener;
	
	private String dropDownMenuOverride;
	
	private IWorkbenchHelpSystem workbenchHelpSystem;
	
	private String helpContextId;
	
	private int mode = 0;
	
	/**
	 * Create a CommandContributionItem to place in a ContributionManager.
	 * 
	 * @param contributionParameters
	 *            paramters necessary to render this contribution item.
	 */
	public HandlerContributionItem(
			final CommandContributionItemParameter contributionParameters, final IHandler2 handler) {
		super(contributionParameters.id);
		
		this.icon = contributionParameters.icon;
		this.disabledIcon = contributionParameters.disabledIcon;
		this.hoverIcon = contributionParameters.hoverIcon;
		this.label = contributionParameters.label;
		this.mnemonic = contributionParameters.mnemonic;
		this.tooltip = contributionParameters.tooltip;
		this.style = contributionParameters.style;
		this.helpContextId = contributionParameters.helpContextId;
		
		menuService = (IMenuService) contributionParameters.serviceLocator
				.getService(IMenuService.class);
		commandService = (ICommandService) contributionParameters.serviceLocator
				.getService(ICommandService.class);
		handlerService = (IHandlerService) contributionParameters.serviceLocator
				.getService(IHandlerService.class);
		bindingService = (IBindingService) contributionParameters.serviceLocator
				.getService(IBindingService.class);
		createCommand(contributionParameters.commandId,
				contributionParameters.parameters);
		commandHandler = handler;
		
		if (command != null) {
			try {
				final UIElement callback = new UIElement(
						contributionParameters.serviceLocator) {
					
					@Override
					public void setChecked(final boolean checked) {
						HandlerContributionItem.this.setChecked(checked);
					}
					
					@Override
					public void setDisabledIcon(final ImageDescriptor desc) {
						HandlerContributionItem.this.setDisabledIcon(desc);
					}
					
					@Override
					public void setHoverIcon(final ImageDescriptor desc) {
						HandlerContributionItem.this.setHoverIcon(desc);
					}
					
					@Override
					public void setIcon(final ImageDescriptor desc) {
						HandlerContributionItem.this.setIcon(desc);
					}
					
					@Override
					public void setText(final String text) {
						HandlerContributionItem.this.setText(text);
					}
					
					@Override
					public void setTooltip(final String text) {
						HandlerContributionItem.this.setTooltip(text);
					}
					
					@Override
					public void setDropDownId(final String id) {
						dropDownMenuOverride = id;
					}
				};
				elementRef = commandService.registerElementForCommand(command,
						callback);
				commandHandler.addHandlerListener(getHandlerListener());
				setImages(contributionParameters.serviceLocator,
						contributionParameters.iconStyle);
				
				if (contributionParameters.helpContextId == null) {
					try {
						this.helpContextId = commandService
								.getHelpContextId(contributionParameters.commandId);
					} catch (final NotDefinedException e) {
						// it's OK to not have a helpContextId
					}
				}
				final IWorkbenchLocationService wls = (IWorkbenchLocationService) contributionParameters.serviceLocator
						.getService(IWorkbenchLocationService.class);
				final IWorkbench workbench = wls.getWorkbench();
				if (workbench != null && helpContextId != null) {
					this.workbenchHelpSystem = workbench.getHelpSystem();
				}
			} catch (final NotDefinedException e) {
				WorkbenchPlugin
						.log("Unable to register menu item \"" + getId() //$NON-NLS-1$
								+ "\", command \"" + contributionParameters.commandId + "\" not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
	}
	
	
	private void setImages(final IServiceLocator locator, final String iconStyle) {
		if (icon == null) {
			final ICommandImageService service = (ICommandImageService) locator
					.getService(ICommandImageService.class);
			icon = service.getImageDescriptor(command.getId(),
					ICommandImageService.TYPE_DEFAULT, iconStyle);
			disabledIcon = service.getImageDescriptor(command.getId(),
					ICommandImageService.TYPE_DISABLED, iconStyle);
			hoverIcon = service.getImageDescriptor(command.getId(),
					ICommandImageService.TYPE_HOVER, iconStyle);
		}
	}
	
	private IHandlerListener getHandlerListener() {
		if (commandListener == null) {
			commandListener = new IHandlerListener() {
				public void handlerChanged(final HandlerEvent commandEvent) {
					if (commandEvent.isHandledChanged()
							|| commandEvent.isEnabledChanged()
							) {
						updateCommandProperties(commandEvent);
					}
				}
			};
		}
		return commandListener;
	}
	
	private void updateCommandProperties(final HandlerEvent commandEvent) {
		if (commandEvent.isHandledChanged()) {
			dropDownMenuOverride = null;
		}
		if (widget == null || widget.isDisposed()) {
			return;
		}
		final Display display = widget.getDisplay();
		final Runnable update = new Runnable() {
			public void run() {
				update(null);
			}
		};
		if (display.getThread() == Thread.currentThread()) {
			update.run();
		} else {
			display.asyncExec(update);
		}
	}
	
	ParameterizedCommand getCommand() {
		return command;
	}
	
	void createCommand(final String commandId, final Map parameters) {
		if (commandId == null) {
			WorkbenchPlugin.log("Unable to create menu item \"" + getId() //$NON-NLS-1$
					+ "\", no command id"); //$NON-NLS-1$
			return;
		}
		final Command cmd = commandService.getCommand(commandId);
		if (!cmd.isDefined()) {
			WorkbenchPlugin.log("Unable to create menu item \"" + getId() //$NON-NLS-1$
					+ "\", command \"" + commandId + "\" not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		command = ParameterizedCommand.generateCommand(cmd, parameters);
	}
	
	@Override
	public void fill(final Menu parent, final int index) {
		if (command == null) {
			return;
		}
		if (widget != null || parent == null) {
			return;
		}
		
		// Menus don't support the pulldown style
		int tmpStyle = style;
		if (tmpStyle == STYLE_PULLDOWN)
			tmpStyle = STYLE_PUSH;
		
		MenuItem item = null;
		if (index >= 0) {
			item = new MenuItem(parent, tmpStyle, index);
		} else {
			item = new MenuItem(parent, tmpStyle);
		}
		item.setData(this);
		if (workbenchHelpSystem != null) {
			workbenchHelpSystem.setHelp(item, helpContextId);
		}
		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());
		widget = item;
		
		update(null);
		updateIcons();
	}
	
	@Override
	public void fill(final ToolBar parent, final int index) {
		if (command == null) {
			return;
		}
		if (widget != null || parent == null) {
			return;
		}
		
		ToolItem item = null;
		if (index >= 0) {
			item = new ToolItem(parent, style, index);
		} else {
			item = new ToolItem(parent, style);
		}
		
		item.setData(this);
		
		item.addListener(SWT.Selection, getItemListener());
		item.addListener(SWT.Dispose, getItemListener());
		widget = item;
		
		update(null);
		updateIcons();
	}
	
	@Override
	public void update() {
		update(null);
	}
	
	@Override
	public void update(final String id) {
		if (widget != null) {
			if (widget instanceof MenuItem) {
				final MenuItem item = (MenuItem) widget;
				
				String text = label;
				if (text == null) {
					if (command != null) {
						try {
							text = command.getCommand().getName();
						} catch (final NotDefinedException e) {
							WorkbenchPlugin.log("Update item failed " //$NON-NLS-1$
									+ getId(), e);
						}
					}
				}
				text = updateMnemonic(text);
				
				String keyBindingText = null;
				if (command != null) {
					final TriggerSequence binding = bindingService
							.getBestActiveBindingFor(command);
					if (binding != null) {
						keyBindingText = binding.format();
					}
				}
				if (text != null) {
					if (keyBindingText == null) {
						item.setText(text);
					} else {
						item.setText(text + '\t' + keyBindingText);
					}
				}
				
				if (item.getSelection() != checkedState) {
					item.setSelection(checkedState);
				}
				
				final boolean shouldBeEnabled = isEnabled();
				if (item.getEnabled() != shouldBeEnabled) {
					item.setEnabled(shouldBeEnabled);
				}
			} else if (widget instanceof ToolItem) {
				final ToolItem item = (ToolItem) widget;
				
				String text = label;
				if (text == null) {
					if (command != null) {
						try {
							text = command.getCommand().getName();
						} catch (final NotDefinedException e) {
							WorkbenchPlugin.log("Update item failed " //$NON-NLS-1$
									+ getId(), e);
						}
					}
				}
				
				if ((icon == null || (mode & MODE_FORCE_TEXT) == MODE_FORCE_TEXT)
						&& text != null) {
					item.setText(text);
				}
				
				if (tooltip != null)
					item.setToolTipText(tooltip);
				else {
					if (text != null) {
						item.setToolTipText(text);
					}
				}
				
				if (item.getSelection() != checkedState) {
					item.setSelection(checkedState);
				}
				
				final boolean shouldBeEnabled = isEnabled();
				if (item.getEnabled() != shouldBeEnabled) {
					item.setEnabled(shouldBeEnabled);
				}
			}
		}
	}
	
	private String updateMnemonic(final String s) {
		if (mnemonic == null || s == null) {
			return s;
		}
		final int idx = s.indexOf(mnemonic);
		if (idx == -1) {
			return s;
		}
		
		return s.substring(0, idx) + '&' + s.substring(idx);
	}
	
	private void handleWidgetDispose(final Event event) {
		if (event.widget == widget) {
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget = null;
			disposeOldImages();
		}
	}
	
	@Override
	public void dispose() {
		if (widget != null) {
			widget.dispose();
			widget = null;
		}
		if (elementRef != null) {
			commandService.unregisterElement(elementRef);
			elementRef = null;
		}
		if (commandListener != null) {
			commandHandler.removeHandlerListener(commandListener);
			commandListener = null;
		}
		command = null;
		commandHandler = null;
		commandService = null;
		bindingService = null;
		menuService = null;
		handlerService = null;
		disposeOldImages();
		super.dispose();
	}
	
	private void disposeOldImages() {
		if (localResourceManager != null) {
			localResourceManager.dispose();
			localResourceManager = null;
		}
	}
	
	private Listener getItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				public void handleEvent(final Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						if (event.widget != null) {
							handleWidgetSelection(event);
						}
						break;
					}
				}
			};
		}
		return menuItemListener;
	}
	
	private void handleWidgetSelection(final Event event) {
		// Special check for ToolBar dropdowns...
		if (openDropDownMenu(event))
			return;
		
		if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
			if (event.widget instanceof ToolItem) {
				checkedState = ((ToolItem) event.widget).getSelection();
			} else if (event.widget instanceof MenuItem) {
				checkedState = ((MenuItem) event.widget).getSelection();
			}
		}
		
		try {
			final ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, event);
			commandHandler.execute(executionEvent);
		} catch (final ExecutionException e) {
			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
					+ getId(), e);
//		} catch (NotDefinedException e) {
//			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
//					+ getId(), e);
//		} catch (NotEnabledException e) {
//			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
//					+ getId(), e);
//		} catch (NotHandledException e) {
//			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
//					+ getId(), e);
		}
	}
	
	/**
	 * Determines if the selection was on the dropdown affordance and, if so,
	 * opens the drop down menu (populated using the same id as this item...
	 * 
	 * @param event
	 *            The <code>SWT.Selection</code> event to be tested
	 * 
	 * @return <code>true</code> iff a drop down menu was opened
	 */
	private boolean openDropDownMenu(final Event event) {
		final Widget item = event.widget;
		if (item != null) {
			final int style = item.getStyle();
			if ((style & SWT.DROP_DOWN) != 0) {
				if (event.detail == 4) { // on drop-down button
					final ToolItem ti = (ToolItem) item;
					
					final MenuManager menuManager = new MenuManager();
					final Menu menu = menuManager.createContextMenu(ti.getParent());
					if (workbenchHelpSystem != null) {
						workbenchHelpSystem.setHelp(menu, helpContextId);
					}
					menuManager.addMenuListener(new IMenuListener() {
						public void menuAboutToShow(final IMenuManager manager) {
							String id = getId();
							if (dropDownMenuOverride != null) {
								id = dropDownMenuOverride;
							}
							menuService.populateContributionManager(
									menuManager, "menu:" + id); //$NON-NLS-1$
						}
					});
					
					// position the menu below the drop down item
					final Point point = ti.getParent().toDisplay(
							new Point(event.x, event.y));
					menu.setLocation(point.x, point.y); // waiting for SWT
					// 0.42
					menu.setVisible(true);
					return true; // we don't fire the action
				}
			}
		}
		
		return false;
	}
	
	private void setIcon(final ImageDescriptor desc) {
		icon = desc;
		updateIcons();
	}
	
	private void updateIcons() {
		if (widget instanceof MenuItem) {
			final MenuItem item = (MenuItem) widget;
			final LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			item.setImage(icon == null ? null : m.createImage(icon));
			disposeOldImages();
			localResourceManager = m;
		} else if (widget instanceof ToolItem) {
			final ToolItem item = (ToolItem) widget;
			final LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			item.setDisabledImage(disabledIcon == null ? null : m
					.createImage(disabledIcon));
			item.setHotImage(hoverIcon == null ? null : m
					.createImage(hoverIcon));
			item.setImage(icon == null ? null : m.createImage(icon));
			disposeOldImages();
			localResourceManager = m;
		}
	}
	
	private void setText(final String text) {
		label = text;
		update(null);
	}
	
	private void setChecked(final boolean checked) {
		if (checkedState == checked) {
			return;
		}
		checkedState = checked;
		if (widget instanceof MenuItem) {
			((MenuItem) widget).setSelection(checkedState);
		} else if (widget instanceof ToolItem) {
			((ToolItem) widget).setSelection(checkedState);
		}
	}
	
	private void setTooltip(final String text) {
		tooltip = text;
		if (widget instanceof ToolItem) {
			((ToolItem) widget).setToolTipText(text);
		}
	}
	
	private void setDisabledIcon(final ImageDescriptor desc) {
		disabledIcon = desc;
		updateIcons();
	}
	
	private void setHoverIcon(final ImageDescriptor desc) {
		hoverIcon = desc;
		updateIcons();
	}
	
	@Override
	public boolean isEnabled() {
		if (commandHandler != null) {
			commandHandler.setEnabled(menuService.getCurrentState());
			return commandHandler.isEnabled();
		}
		return false;
	}
	
}
