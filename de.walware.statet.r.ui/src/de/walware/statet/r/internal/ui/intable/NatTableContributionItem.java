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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.waltable.ui.NatEventData;



/**
 * A contribution item
 */
public abstract class NatTableContributionItem extends ContributionItem {
	
	
	/**
	 * A push button tool item or menu item.
	 */
	public static final int STYLE_PUSH= SWT.PUSH;
	
	/**
	 * A checked tool item or menu item.
	 */
	public static final int STYLE_CHECK= SWT.CHECK;
	
	/**
	 * A radio-button style menu item.
	 */
	public static final int STYLE_RADIO= SWT.RADIO;
	
	/**
	 * A ToolBar pulldown item.
	 */
	public static final int STYLE_PULLDOWN= SWT.DROP_DOWN;
	
	/**
	 * Mode bit: Show text on tool items or buttons, even if an image is
	 * present. If this mode bit is not set, text is only shown on tool items if
	 * there is no image present.
	 * 
	 * @since 3.4
	 */
	public static final int MODE_FORCE_TEXT= 1;
	
	
	private LocalResourceManager localResourceManager;
	
	private Listener menuItemListener;
	
	private Widget widget;
	
	private ImageDescriptor icon;
	
	private String label;
	
	private String tooltip;
	
	private ImageDescriptor disabledIcon;
	
	private ImageDescriptor hoverIcon;
	
	private final String mnemonic;
	
	private boolean checkedState;
	
	private final int style;
	
	private IWorkbenchHelpSystem workbenchHelpSystem;
	
	private String helpContextId;
	
	private final int mode= 0;
	
	
	/**
	 * Create a CommandContributionItem to place in a ContributionManager.
	 * 
	 * @param contributionParameters
	 *            paramters necessary to render this contribution item.
	 */
	public NatTableContributionItem(
			final CommandContributionItemParameter contributionParameters) {
		super(contributionParameters.id);
		
		this.icon= contributionParameters.icon;
		this.disabledIcon= contributionParameters.disabledIcon;
		this.hoverIcon= contributionParameters.hoverIcon;
		this.label= contributionParameters.label;
		this.mnemonic= contributionParameters.mnemonic;
		this.tooltip= contributionParameters.tooltip;
		this.style= contributionParameters.style;
		this.helpContextId= contributionParameters.helpContextId;
	}
	
	protected NatTableContributionItem(final ImageDescriptor icon, final ImageDescriptor disabledIcon,
			final String label, final String mnemonic) {
		super();
		
		this.icon= icon;
		this.disabledIcon= disabledIcon;
		this.label= label;
		this.mnemonic= mnemonic;
		this.style= STYLE_PUSH;
	}
	
	protected NatTableContributionItem(
			final String label, final String mnemonic) {
		super();
		
		this.label= label;
		this.mnemonic= mnemonic;
		this.style= STYLE_PUSH;
	}
	
	protected NatTableContributionItem(final ImageDescriptor icon, final ImageDescriptor disabledIcon,
			final String label, final String mnemonic, final int style) {
		super();
		
		this.icon= icon;
		this.disabledIcon= disabledIcon;
		this.label= label;
		this.mnemonic= mnemonic;
		this.style= style;
	}
	
	protected NatTableContributionItem(
			final String label, final String mnemonic, final int style) {
		super();
		
		this.label= label;
		this.mnemonic= mnemonic;
		this.style= style;
	}
	
	
	@Override
	public void fill(final Menu parent, final int index) {
		if (this.widget != null || parent == null) {
			return;
		}
		
		// Menus don't support the pulldown style
		int tmpStyle= this.style;
		if (tmpStyle == STYLE_PULLDOWN) {
			tmpStyle= STYLE_PUSH;
		}
		
		MenuItem item= null;
		if (index >= 0) {
			item= new MenuItem(parent, tmpStyle, index);
		} else {
			item= new MenuItem(parent, tmpStyle);
		}
		item.setData(this);
		if (this.workbenchHelpSystem != null) {
			this.workbenchHelpSystem.setHelp(item, this.helpContextId);
		}
		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());
		this.widget= item;
		
		update(null);
		updateIcons();
	}
	
	@Override
	public void fill(final ToolBar parent, final int index) {
		if (this.widget != null || parent == null) {
			return;
		}
		
		ToolItem item= null;
		if (index >= 0) {
			item= new ToolItem(parent, this.style, index);
		} else {
			item= new ToolItem(parent, this.style);
		}
		
		item.setData(this);
		
		item.addListener(SWT.Selection, getItemListener());
		item.addListener(SWT.Dispose, getItemListener());
		this.widget= item;
		
		update(null);
		updateIcons();
	}
	
	@Override
	public void fill(final Composite parent) {
		if (this.widget != null || parent == null) {
			return;
		}
		
		// Buttons don't support the pulldown style
		int tmpStyle= this.style;
		if (tmpStyle == STYLE_PULLDOWN) {
			tmpStyle= STYLE_PUSH;
		}
		
		final Button item= new Button(parent, tmpStyle);
		item.setData(this);
		if (this.workbenchHelpSystem != null) {
			this.workbenchHelpSystem.setHelp(item, this.helpContextId);
		}
		item.addListener(SWT.Dispose, getItemListener());
		item.addListener(SWT.Selection, getItemListener());
		this.widget= item;
		
		update(null);
		updateIcons();
	}
	
	@Override
	public void update() {
		update(null);
	}
	
	@Override
	public void update(final String id) {
		if (this.widget != null) {
			if (this.widget instanceof MenuItem) {
				updateMenuItem();
			} else if (this.widget instanceof ToolItem) {
				updateToolItem();
			} else if (this.widget instanceof Button) {
				updateButton();
			}
		}
	}
	
	private void updateMenuItem() {
		final MenuItem item= (MenuItem) this.widget;
		
		final boolean shouldBeEnabled= isEnabled();
		
		// disabled command + visibility follows enablement == disposed
		if (item.isDisposed()) {
			return;
		}
		
		String text= this.label;
		text= updateMnemonic(text);
		
		final String keyBindingText= null;
		if (text != null) {
			if (keyBindingText == null) {
				item.setText(text);
			} else {
				item.setText(text + '\t' + keyBindingText);
			}
		}
		
		if (item.getSelection() != this.checkedState) {
			item.setSelection(this.checkedState);
		}
		
		if (item.getEnabled() != shouldBeEnabled) {
			item.setEnabled(shouldBeEnabled);
		}
	}
	
	private void updateToolItem() {
		final ToolItem item= (ToolItem) this.widget;
		
		final boolean shouldBeEnabled= isEnabled();
		
		// disabled command + visibility follows enablement == disposed
		if (item.isDisposed()) {
			return;
		}
		
		final String text= this.label;
		if ((this.icon == null || (this.mode & MODE_FORCE_TEXT) == MODE_FORCE_TEXT)
				&& text != null) {
			item.setText(text);
		}
		
		final String toolTipText= getToolTipText(text);
		item.setToolTipText(toolTipText);
		
		if (item.getSelection() != this.checkedState) {
			item.setSelection(this.checkedState);
		}
		
		if (item.getEnabled() != shouldBeEnabled) {
			item.setEnabled(shouldBeEnabled);
		}
	}
	
	private void updateButton() {
		final Button item= (Button) this.widget;
		
		final boolean shouldBeEnabled= isEnabled();
		
		// disabled command + visibility follows enablement == disposed
		if (item.isDisposed()) {
			return;
		}
		
		final String text= this.label;
		if (text != null) {
			item.setText(text);
		}
		
		final String toolTipText= getToolTipText(text);
		item.setToolTipText(toolTipText);
		
		if (item.getSelection() != this.checkedState) {
			item.setSelection(this.checkedState);
		}
		
		if (item.getEnabled() != shouldBeEnabled) {
			item.setEnabled(shouldBeEnabled);
		}
	}
	
	private String getToolTipText(final String text) {
		String tooltipText= this.tooltip;
		if (this.tooltip == null) {
			if (text != null) {
				tooltipText= text;
			} else {
				tooltipText= ""; //$NON-NLS-1$
			}
		}
		
		return tooltipText;
	}
	
	private String updateMnemonic(final String s) {
		if (this.mnemonic == null || s == null) {
			return s;
		}
		final int idx= s.indexOf(this.mnemonic);
		if (idx == -1) {
			return s;
		}
		
		return s.substring(0, idx) + '&' + s.substring(idx);
	}
	
	private void handleWidgetDispose(final Event event) {
		if (event.widget == this.widget) {
			this.widget.removeListener(SWT.Selection, getItemListener());
			this.widget.removeListener(SWT.Dispose, getItemListener());
			this.widget= null;
			disposeOldImages();
		}
	}
	
	@Override
	public void dispose() {
		if (this.widget != null) {
			this.widget.dispose();
			this.widget= null;
		}
		disposeOldImages();
		super.dispose();
	}
	
	private void disposeOldImages() {
		if (this.localResourceManager != null) {
			this.localResourceManager.dispose();
			this.localResourceManager= null;
		}
	}
	
	private Listener getItemListener() {
		if (this.menuItemListener == null) {
			this.menuItemListener= new Listener() {
				@Override
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
		return this.menuItemListener;
	}
	
	
	private static NatEventData getNatEventData(final Event selectionEvent) {
		final Widget widget= selectionEvent.widget;
		if (widget == null || !(widget instanceof MenuItem)) {
			return null;
		}
		
		final MenuItem menuItem= (MenuItem) widget;
		Menu parentMenu= menuItem.getParent();
		Object data= null;
		while (parentMenu != null) {
			if (parentMenu.getData() == null) {
				parentMenu= parentMenu.getParentMenu();
			} else {
				data= parentMenu.getData();
				break;
			}
		}
		
		return data != null ? (NatEventData) data : null;
	}
	
	private void handleWidgetSelection(final Event event) {
		// Special check for ToolBar dropdowns...
		if (openDropDownMenu(event)) {
			return;
		}
		
		if ((this.style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
			if (event.widget instanceof ToolItem) {
				this.checkedState= ((ToolItem) event.widget).getSelection();
			} else if (event.widget instanceof MenuItem) {
				this.checkedState= ((MenuItem) event.widget).getSelection();
			}
		}
		
		final NatEventData eventData= getNatEventData(event);
		if (eventData == null) {
			return;
		}
		try {
			execute(eventData);
		} catch (final ExecutionException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
					"Failed to execute item " + getId(), e)); //$NON-NLS-1$
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
		final Widget item= event.widget;
		if (item != null) {
			final int style= item.getStyle();
			if ((style & SWT.DROP_DOWN) != 0) {
				if (event.detail == 4) { // on drop-down button
					final ToolItem ti= (ToolItem) item;
					
					final MenuManager menuManager= new MenuManager();
					final Menu menu= menuManager.createContextMenu(ti.getParent());
					if (this.workbenchHelpSystem != null) {
						this.workbenchHelpSystem.setHelp(menu, this.helpContextId);
					}
					menuManager.addMenuListener(new IMenuListener() {
						@Override
						public void menuAboutToShow(final IMenuManager manager) {
							dropDownMenuAboutToShow(manager);
						}
					});
					
					// position the menu below the drop down item
					final Point point= ti.getParent().toDisplay(
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
	
	private void updateIcons() {
		if (this.widget instanceof MenuItem) {
			final MenuItem item= (MenuItem) this.widget;
			final LocalResourceManager m= new LocalResourceManager(JFaceResources
					.getResources());
			try {
				item.setImage(this.icon == null ? null : m.createImage(this.icon));
			} catch (final DeviceResourceException e) {
				this.icon= ImageDescriptor.getMissingImageDescriptor();
				item.setImage(m.createImage(this.icon));
				// as we replaced the failed icon, log the message once.
				StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
						"Failed to load image", e)); //$NON-NLS-1$
			}
			disposeOldImages();
			this.localResourceManager= m;
		} else if (this.widget instanceof ToolItem) {
			final ToolItem item= (ToolItem) this.widget;
			final LocalResourceManager m= new LocalResourceManager(JFaceResources
					.getResources());
			item.setDisabledImage(this.disabledIcon == null ? null : m
					.createImage(this.disabledIcon));
			item.setHotImage(this.hoverIcon == null ? null : m
					.createImage(this.hoverIcon));
			item.setImage(this.icon == null ? null : m.createImage(this.icon));
			disposeOldImages();
			this.localResourceManager= m;
		}
	}
	
	public void setText(final String text) {
		this.label= text;
		update(null);
	}
	
	public void setChecked(final boolean checked) {
		if (this.checkedState == checked) {
			return;
		}
		this.checkedState= checked;
		if (this.widget instanceof MenuItem) {
			((MenuItem) this.widget).setSelection(this.checkedState);
		} else if (this.widget instanceof ToolItem) {
			((ToolItem) this.widget).setSelection(this.checkedState);
		}
	}
	
	public void setTooltip(final String text) {
		this.tooltip= text;
		if (this.widget instanceof ToolItem) {
			((ToolItem) this.widget).setToolTipText(text);
		}
	}
	
	public void setIcon(final ImageDescriptor desc) {
		this.icon= desc;
		updateIcons();
	}
	
	public void setDisabledIcon(final ImageDescriptor desc) {
		this.disabledIcon= desc;
		updateIcons();
	}
	
	public void setHoverIcon(final ImageDescriptor desc) {
		this.hoverIcon= desc;
		updateIcons();
	}
	
	
	protected void dropDownMenuAboutToShow(final IMenuManager manager) {
	}
	
	protected void execute(final NatEventData natEventData) throws ExecutionException {
	}
	
}
