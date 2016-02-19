/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.SimpleContributionItem;

import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.datafilter.FilterSet;
import de.walware.statet.r.internal.ui.datafilter.FilterType;
import de.walware.statet.r.internal.ui.datafilter.VariableFilter;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class VariableComposite extends ExpandableRowComposite {
	
	
	private final VariableContainer fSite;
	private RDataTableColumn fColumn;
	
	private final MenuManager fMenuManager;
	
	private boolean fActive;
	
	
	public VariableComposite(final Composite parent, final VariableContainer site,
			final RDataTableColumn column) {
		super(parent, SWT.NONE, TWISTIE | CLIENT_INDENT | IMAGE);
		
		fSite = site;
		setColumn(column);
		
		fMenuManager = new MenuManager();
		fMenuManager.setRemoveAllWhenShown(true);
		fMenuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				fillMenu(manager);
			}
		});
		setMenu(fMenuManager.createContextMenu(this));
	}
	
	
	@Override
	public void setClient(final Control client) {
		if (!(client instanceof FilterClient)) {
			throw new IllegalArgumentException();
		}
		super.setClient(client);
		updateImage(false);
	}
	
	@Override
	public void layout(final Control[] changed, final int flags) {
		super.layout(changed, flags);
		getParent().layout(new Control[] { this });
		if (isExpanded()) {
			fSite.getVariableComposite().reflow(true);
		}
	}
	
	
	public VariableContainer getContainer() {
		return fSite;
	}
	
	@Override
	public FilterClient getClient() {
		return (FilterClient) super.getClient();
	}
	
	public void setColumn(final RDataTableColumn column) {
		fColumn = column;
		
		updateImage(true);
		setText(fColumn.getName());
	}
	
	protected void updateImage(final boolean force) {
		final boolean isActive = isFilterActive();
		if (!force && fActive == isActive) {
			return;
		}
		Image image = fSite.getLabelProvider().getImage(fColumn);
		if (isActive) {
			final DecorationOverlayIcon descriptor = new DecorationOverlayIcon(image,
					new ImageDescriptor[] { null, SharedUIResources.getImages()
							.getDescriptor(SharedUIResources.OVR_YELLOW_LIGHT_IMAGE_ID)
				});
			image = RUIPlugin.getDefault().getImageDescriptorRegistry().get(descriptor);
			fActive = true;
		}
		else {
			fActive = false;
		}
		setImage(image);
	}
	
	private boolean isFilterActive() {
		final FilterClient client = getClient();
		if (client != null) {
			final String rExpression = client.getFilter().getFilterRExpression();
			return (rExpression != null && !rExpression.isEmpty());
		}
		return false;
	}
	
	public RDataTableColumn getColumn() {
		return fColumn;
	}
	
	protected void fillMenu(final IMenuManager menu) {
		final VariableFilter currentFilter = getClient().getFilter();
		final FilterSet filterSet = currentFilter.getSet();
		final List<FilterType> filters = filterSet.getAvailableFilters(fColumn);
		for (int i = 0; i < filters.size(); i++) {
			final FilterType filterType = filters.get(i);
			final SimpleContributionItem item = new SimpleContributionItem(filterType.getLabel(), null,
					SimpleContributionItem.STYLE_RADIO) {
				@Override
				protected void execute() throws ExecutionException {
					setFilterType(filterType);
				}
			};
			item.setChecked(currentFilter.getType() == filterType);
			menu.add(item);
		}
		menu.add(new Separator());
		menu.add(new SimpleContributionItem(Messages.Variable_Clear_label, null) {
			@Override
			protected void execute() throws ExecutionException {
				getClient().getFilter().reset();
			}
		});
	}
	
	protected void setFilterType(final FilterType type) {
		final VariableFilter currentFilter = getClient().getFilter();
		if (currentFilter.getType() == type) {
			return;
		}
		final FilterSet filterSet = currentFilter.getSet();
		filterSet.replace(currentFilter, type);
		layout(new Control[] { getClient() });
	}
	
}
