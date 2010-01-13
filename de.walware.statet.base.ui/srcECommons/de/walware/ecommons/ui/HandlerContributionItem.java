/*******************************************************************************
 * Copyright (c) 2006-2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - modified to use handlers
 *******************************************************************************/

package de.walware.ecommons.ui;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;


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
	
	
	public static final String NO_COMMAND_ID = "NO_COMMAND"; //$NON-NLS-1$
	
	
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
	public static final int MODE_FORCE_TEXT = 1;
	
	
	private LocalResourceManager localResourceManager;
	
	private Listener menuItemListener;
	
	private Widget widget;
	
	private IMenuService menuService;
	
	private ICommandService commandService;
	private IHandlerService handlerService;
	private IBindingService bindingService;
	
	private Display display;
	
	private ParameterizedCommand command;
	private boolean noCommandMode;
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
	
	private final UIElement callback;
	
	private IBindingManagerListener bindingManagerListener = new IBindingManagerListener() {
		public void bindingManagerChanged(final BindingManagerEvent event) {
			if (event.isActiveBindingsChanged()
					&& event.isActiveBindingsChangedFor(getCommand())) {
				update();
			}
		}
	};
	
	
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
		final IWorkbenchLocationService workbenchLocationService = (IWorkbenchLocationService) contributionParameters.serviceLocator.getService(IWorkbenchLocationService.class);
		display = workbenchLocationService.getWorkbench().getDisplay();
		
		createCommand(contributionParameters.commandId,
				contributionParameters.parameters);
		commandHandler = handler;
		
		callback = new UIElement(
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
		if (command != null || noCommandMode) {
			commandHandler.addHandlerListener(getHandlerListener());
		}
		if (command != null) {
			try {
				elementRef = commandService.registerElementForCommand(command, callback);
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
			}
			catch (final NotDefinedException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
						"Unable to register menu item \"" + getId() + "\", command \"" + contributionParameters.commandId + "\" not defined")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}
	
	
	private void setImages(final IServiceLocator locator, final String iconStyle) {
		if (icon == null && command != null) {
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
	
	/**
	 * Returns the ParameterizedCommand for this contribution.
	 * <p>
	 * <strong>NOTE:</strong> The returned object should be treated
	 * as 'read-only', do <b>not</b> execute this instance or attempt
	 * to modify its state.
	 * </p>
	 * @return The parameterized command for this contribution.
	 */
	public ParameterizedCommand getCommand() {
		return command;
	}
	
	void createCommand(final String commandId, final Map parameters) {
		if (commandId == null) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
					"Unable to create menu item \"" + getId() + "\", no command id")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (commandId == NO_COMMAND_ID) {
			noCommandMode = true;
			return;
		}
		final Command cmd = commandService.getCommand(commandId);
		if (!cmd.isDefined()) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
					"Unable to create menu item \"" + getId() + "\", command \"" + commandId + "\" not defined")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}
		command = ParameterizedCommand.generateCommand(cmd, parameters);
	}
	
	@Override
	public void fill(final Menu parent, final int index) {
		if (command == null && !noCommandMode) {
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
		
		bindingService.addBindingManagerListener(bindingManagerListener);
	}
	
	@Override
	public void fill(final ToolBar parent, final int index) {
		if (command == null && !noCommandMode) {
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
		
		bindingService.addBindingManagerListener(bindingManagerListener);
	}
	
	@Override
	public void fill(final Composite parent) {
		if (command == null && !noCommandMode) {
			return;
		}
		if (widget != null || parent == null) {
			return;
		}
		
		// Buttons don't support the pulldown style
		int tmpStyle = style;
		if (tmpStyle == STYLE_PULLDOWN)
			tmpStyle = STYLE_PUSH;
		
		final Button item = new Button(parent, tmpStyle);
		item.setData(this);
		if (workbenchHelpSystem != null) {
			workbenchHelpSystem.setHelp(item, helpContextId);
		}
		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());
		widget = item;
		
		update(null);
		updateIcons();
		
		bindingService.addBindingManagerListener(bindingManagerListener);
	}
	
	@Override
	public void update() {
		update(null);
	}
	
	@Override
	public void update(final String id) {
		if (widget != null) {
			if (widget instanceof MenuItem) {
				updateMenuItem();
			} else if (widget instanceof ToolItem) {
				updateToolItem();
			} else if (widget instanceof Button) {
				updateButton();
			}
		}
	}
	
	private void updateMenuItem() {
		final MenuItem item = (MenuItem) widget;
		
		final boolean shouldBeEnabled = isEnabled();
		
		// disabled command + visibility follows enablement == disposed
		if (item.isDisposed()) {
			return;
		}
		
		String text = label;
		if (text == null) {
			if (command != null) {
				try {
					text = command.getCommand().getName();
				} catch (final NotDefinedException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
							"Update item failed " + getId(), e)); //$NON-NLS-1$
				}
			}
		}
		text = updateMnemonic(text);
		
		String keyBindingText = null;
		if (command != null) {
			final TriggerSequence binding = bindingService.getBestActiveBindingFor(command);
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
		
		if (item.getEnabled() != shouldBeEnabled) {
			item.setEnabled(shouldBeEnabled);
		}
		
		if (noCommandMode && commandHandler instanceof IElementUpdater) {
			((IElementUpdater) commandHandler).updateElement(callback, Collections.EMPTY_MAP);
		}
	}
	
	private void updateToolItem() {
		final ToolItem item = (ToolItem) widget;
		
		final boolean shouldBeEnabled = isEnabled();
		
		// disabled command + visibility follows enablement == disposed
		if (item.isDisposed()) {
			return;
		}
		
		String text = label;
		if (text == null) {
			if (command != null) {
				try {
					text = command.getCommand().getName();
				} catch (final NotDefinedException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
							"Update item failed " + getId(), e)); //$NON-NLS-1$
				}
			}
		}
		
		if ((icon == null || (mode & MODE_FORCE_TEXT) == MODE_FORCE_TEXT)
				&& text != null) {
			item.setText(text);
		}
		
		final String toolTipText = getToolTipText(text);
		item.setToolTipText(toolTipText);
		
		if (item.getSelection() != checkedState) {
			item.setSelection(checkedState);
		}
		
		if (item.getEnabled() != shouldBeEnabled) {
			item.setEnabled(shouldBeEnabled);
		}
		
		if (noCommandMode && commandHandler instanceof IElementUpdater) {
			((IElementUpdater) commandHandler).updateElement(callback, Collections.EMPTY_MAP);
		}
	}
	
	private void updateButton() {
		final Button item = (Button) widget;
		
		final boolean shouldBeEnabled = isEnabled();
		
		// disabled command + visibility follows enablement == disposed
		if (item.isDisposed()) {
			return;
		}
		
		String text = label;
		if (text == null) {
			if (command != null) {
				try {
					text = command.getCommand().getName();
				} catch (final NotDefinedException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
							"Update item failed " + getId(), e)); //$NON-NLS-1$
				}
			}
		}
		
		if (text != null) {
			item.setText(text);
		}
		
		final String toolTipText = getToolTipText(text);
		item.setToolTipText(toolTipText);
		
		if (item.getSelection() != checkedState) {
			item.setSelection(checkedState);
		}
		
		if (item.getEnabled() != shouldBeEnabled) {
			item.setEnabled(shouldBeEnabled);
		}
		
		if (noCommandMode && commandHandler instanceof IElementUpdater) {
			((IElementUpdater) commandHandler).updateElement(callback, Collections.EMPTY_MAP);
		}
	}
	
	private String getToolTipText(final String text) {
		String tooltipText = tooltip;
		if (tooltip == null)
			if (text != null)
				tooltipText = text;
			else
				tooltipText = ""; //$NON-NLS-1$
		
		if (command != null) {
			final TriggerSequence activeBinding = bindingService.getBestActiveBindingFor(command);
			if (activeBinding != null && !activeBinding.isEmpty()) {
				final String acceleratorText = activeBinding.format();
				if (acceleratorText != null
						&& acceleratorText.length() != 0) {
					tooltipText = NLS.bind("{0} ({1})", tooltipText, acceleratorText); //$NON-NLS-1$
				}
			}
		}
		
		return tooltipText;
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
		if (bindingService != null) {
			bindingService.removeBindingManagerListener(bindingManagerListener);
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
			final ExecutionEvent executionEvent = command != null ?
					handlerService.createExecutionEvent(command, event) : new ExecutionEvent();
			commandHandler.execute(executionEvent);
		} catch (final ExecutionException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
					"Failed to execute item " + getId(), e)); //$NON-NLS-1$
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
	
	public void setIcon(final ImageDescriptor desc) {
		icon = desc;
		updateIcons();
	}
	
	private void updateIcons() {
		if (widget instanceof MenuItem) {
			final MenuItem item = (MenuItem) widget;
			final LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			try {
				item.setImage(icon == null ? null : m.createImage(icon));
			} catch (final DeviceResourceException e) {
				icon = ImageDescriptor.getMissingImageDescriptor();
				item.setImage(m.createImage(icon));
				// as we replaced the failed icon, log the message once.
				StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
						"Failed to load image", e)); //$NON-NLS-1$
			}
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
