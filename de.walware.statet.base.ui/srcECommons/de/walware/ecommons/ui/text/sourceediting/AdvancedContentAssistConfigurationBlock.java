/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.icu.text.Collator;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.ecommons.internal.ui.text.EditingMessages;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.ui.dialogs.ButtonGroup;
import de.walware.ecommons.ui.dialogs.IStatusChangeListener;
import de.walware.ecommons.ui.preferences.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.CheckboxTableComposite;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class AdvancedContentAssistConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private class CategoryKindLabelProvider extends CellLabelProvider {
		@Override
		public void update(final ViewerCell cell) {
			final ContentAssistCategory category = (ContentAssistCategory) cell.getElement();
			cell.setImage(AdvancedContentAssistConfigurationBlock.this.getImage(category.getImageDescriptor()));
			cell.setText(category.getDisplayName());
		}
	}
	
	
	private static BindingManager gLocalBindingManager;
	
	
	private CheckboxTableViewer fDefaultList;
	private CheckboxTableViewer fCirclingList;
	private final Map<Object, Image> fImages = new HashMap<Object, Image>();
	
	private final ContentAssistComputerRegistry fRegistry;
	
	private WritableList fOrderedCategories;
	private ButtonGroup<ContentAssistCategory> fOrderButtons;
	
	private Command fSpecificCommand;
	private IParameter fSpecificParam;
	
	
	public AdvancedContentAssistConfigurationBlock(
			final ContentAssistComputerRegistry registry, final String specificCommandId,
			final IStatusChangeListener statusListener) {
		super(null, statusListener);
		fRegistry = registry;
		
		if (specificCommandId != null) {
			final ICommandService commandSvc = (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
			fSpecificCommand = commandSvc.getCommand(specificCommandId);
		}
	}
	
	private void prepareKeybindingInfo() {
		if (fSpecificCommand == null) {
			return;
		}
		if (gLocalBindingManager == null) {
			gLocalBindingManager = new BindingManager(new ContextManager(), new CommandManager());
			final IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
			gLocalBindingManager.setLocale(bindingService.getLocale());
			gLocalBindingManager.setPlatform(bindingService.getPlatform());
			
			final Scheme[] definedSchemes= bindingService.getDefinedSchemes();
			if (definedSchemes != null) {
				try {
					for (int i = 0; i < definedSchemes.length; i++) {
						final Scheme scheme= definedSchemes[i];
						final Scheme copy = gLocalBindingManager.getScheme(scheme.getId());
						copy.define(scheme.getName(), scheme.getDescription(), scheme.getParentId());
					}
				}
				catch (final NotDefinedException e) {
					StatetUIPlugin.logUnexpectedError(e);
				}
			}
		}
		try {
			fSpecificParam = fSpecificCommand.getParameters()[0];
		}
		catch (final Exception x) {
			fSpecificCommand = null;
			fSpecificParam = null;
		}
	}
	
	private String getDefaultKeybindingAsString() {
		final ICommandService commandSvc= (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
		final Command command = commandSvc.getCommand(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		final ParameterizedCommand pCmd= new ParameterizedCommand(command, null);
		final String key = getKeybindingAsString(pCmd);
		return key;
	}
	private String getSpecificKeybindingAsString(final ContentAssistCategory category) {
		if (gLocalBindingManager == null || category == null) {
			return null;
		}
		final Parameterization[] params = { new Parameterization(fSpecificParam, category.getId()) };
		final ParameterizedCommand pCmd = new ParameterizedCommand(fSpecificCommand, params);
		return getKeybindingAsString(pCmd);
	}
	
	private String getKeybindingAsString(final ParameterizedCommand command) {
		final IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		try {
			gLocalBindingManager.setBindings(bindingService.getBindings());
			final Scheme activeScheme = bindingService.getActiveScheme();
			if (activeScheme != null) {
				gLocalBindingManager.setActiveScheme(activeScheme);
			}
			
			final TriggerSequence[] binding = gLocalBindingManager.getActiveBindingsDisregardingContextFor(command);
			if (binding.length > 0) {
				return binding[0].format();
			}
			return null;
		}
		catch (final NotDefinedException e) {
			return null;
		}
	}
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		prefs.put(fRegistry.getPrefDefaultDisabledCategoryIds(), fRegistry.getSettingsGroupId());
		prefs.put(fRegistry.getPrefCirclingOrderedCategoryIds(), fRegistry.getSettingsGroupId());
		setupPreferenceManager(prefs);
		
		prepareKeybindingInfo();
		
		final Composite composite = new Composite(pageComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
//		final Composite composite = pageComposite;
		
		final String keybinding = getDefaultKeybindingAsString();
		final String message = ((keybinding != null) ? 
				NLS.bind(EditingMessages.ContentAssistAdvancedConfig_message_DefaultKeyBinding, keybinding) :
				EditingMessages.ContentAssistAdvancedConfig_message_NoDefaultKeyBinding) + ' ' +
				EditingMessages.ContentAssistAdvancedConfig_message_KeyBindingHint;
		final Link control = addLinkControl(composite, message);
		control.setLayoutData(applyWrapWidth(new GridData(SWT.FILL, SWT.FILL, true, false)));
		
		{	// Default
			final Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
			group.setText(EditingMessages.ContentAssistAdvancedConfig_Default_label);
			
			final Label label = new Label(group, SWT.NONE);
			label.setText(EditingMessages.ContentAssistAdvancedConfig_DefaultTable_label);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final Composite table = createDefaultTable(group);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		
		{	// Default
			final Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
			group.setText(EditingMessages.ContentAssistAdvancedConfig_Cicling_label);
			
			final Label label = new Label(group, SWT.WRAP);
			label.setText(EditingMessages.ContentAssistAdvancedConfig_CiclingTable_label);
			label.setLayoutData(applyWrapWidth(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1)));
			
			final Composite table = createCirclingTable(group);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			fOrderButtons = new ButtonGroup<ContentAssistCategory>(group);
			fOrderButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			fOrderButtons.addUpButton();
			fOrderButtons.addDownButton();
		}
		
		fOrderedCategories = new WritableList();
		fCirclingList.setInput(fOrderedCategories);
		fOrderButtons.connectTo(fCirclingList, fOrderedCategories, null);
		
		updateControls();
	}
	
	protected Composite createDefaultTable(final Composite parent) {
		final CheckboxTableComposite composite = new ViewerUtil.CheckboxTableComposite(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		fDefaultList = composite.viewer;
		composite.table.setHeaderVisible(true);
		composite.table.setLinesVisible(true);
		composite.viewer.setContentProvider(new ArrayContentProvider());
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(3));
			column.getColumn().setText(EditingMessages.ContentAssistAdvancedConfig_ProposalKinds_label);
			column.setLabelProvider(new CategoryKindLabelProvider());
		}
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText(EditingMessages.ContentAssistAdvancedConfig_KeyBinding_label);
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final ContentAssistCategory category = (ContentAssistCategory) cell.getElement();
					final String keybindingAsString = getSpecificKeybindingAsString(category);
					cell.setText((keybindingAsString != null) ? keybindingAsString : ""); //$NON-NLS-1$
				}
			});
		}
		
		return composite;
	}
	
	protected Composite createCirclingTable(final Composite parent) {
		final CheckboxTableComposite composite = new ViewerUtil.CheckboxTableComposite(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		fCirclingList = composite.viewer;
		composite.table.setHeaderVisible(true);
		composite.table.setLinesVisible(true);
		composite.viewer.setContentProvider(new ArrayContentProvider());
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText(EditingMessages.ContentAssistAdvancedConfig_ProposalKinds_label);
			column.setLabelProvider(new CategoryKindLabelProvider());
		}
		return composite;
	}
	
	private Image getImage(final ImageDescriptor imgDesc) {
		if (imgDesc == null) {
			return null;
		}
		Image img = fImages.get(imgDesc);
		if (img == null) {
			img = imgDesc.createImage(false);
			fImages.put(imgDesc, img);
		}
		return img;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		for (final Image img : fImages.values()) {
			img.dispose();
		}
		fImages.clear();
	}
	
	
	@Override
	protected void updateControls() {
		final List<ContentAssistCategory> orderedCategories = fRegistry.applyPreferences(this, fRegistry.getCopyOfCategories());
		
		final List<ContentAssistCategory> defaultCategories = new ArrayList<ContentAssistCategory>(orderedCategories);
		Collections.sort(defaultCategories, new Comparator<ContentAssistCategory>() {
			private final Collator NAMES_COLLARTOR = Collator.getInstance();
			public int compare(final ContentAssistCategory o1, final ContentAssistCategory o2) {
				return NAMES_COLLARTOR.compare(o1.getDisplayName(), o2.getDisplayName());
			}
		});
		fDefaultList.setInput(defaultCategories);
		for (final ContentAssistCategory category : defaultCategories) {
			fDefaultList.setChecked(category, category.fIsIncludedInDefault);
		}
		
		fOrderedCategories.clear();
		fOrderedCategories.addAll(orderedCategories);
		fCirclingList.refresh();
		for (final ContentAssistCategory category : orderedCategories) {
			fCirclingList.setChecked(category, category.fIsEnabledAsSeparate);
		}
	}
	
	@Override
	protected void updatePreferences() {
		final List<ContentAssistCategory> orderedCategories = new ArrayList<ContentAssistCategory>(fOrderedCategories);
		for (final ContentAssistCategory category : orderedCategories) {
			category.fIsIncludedInDefault = fDefaultList.getChecked(category);
			category.fIsEnabledAsSeparate = fCirclingList.getChecked(category);
		}
		
		final Map<Preference, Object> preferences = fRegistry.createPreferences(orderedCategories);
		setPrefValues(preferences);
	}
	
}
