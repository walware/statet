/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.preferences;

import static de.walware.statet.r.internal.debug.ui.launcher.RCodeLaunchRegistry.PREF_R_CONNECTOR;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.templates.TemplateVariableProcessor;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.PixelConverter;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.ext.ui.dialogs.SnippetEditor;
import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants;
import de.walware.statet.r.internal.debug.ui.launcher.RCodeLaunchRegistry;
import de.walware.statet.r.internal.debug.ui.launcher.RCodeLaunchRegistry.ContentHandler.FileCommand;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;
import de.walware.statet.r.ui.editors.RTemplateSourceViewerConfigurator;


public class RInteractionPreferencePage extends ConfigurationBlockPreferencePage<RInteractionConfigurationBlock> {

	
	public RInteractionPreferencePage() {
		setPreferenceStore(RUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.RInteraction_description);
	}
	
	@Override
	protected RInteractionConfigurationBlock createConfigurationBlock() {
		return new RInteractionConfigurationBlock();
	}
	
}

class RInteractionConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private RCodeLaunchRegistry.ConnectorConfig[] fConnectors;
	private Combo fConnectorsSelector;
	private Link fConnectorsDescription;
	
	private FileCommand[] fFileCommands;
	private SnippetEditor[] fCommandEditors;
	
	
	RInteractionConfigurationBlock () {
		super(null);
	}
	
	@Override
	public void createContents(final Composite pageComposite, final IWorkbenchPreferenceContainer container,
			final IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		
		fConnectors = RCodeLaunchRegistry.getAvailableConnectors();
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		prefs.put(PREF_R_CONNECTOR, null);
		setupPreferenceManager(container, prefs);
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		Composite group = createConnectorComponent(pageComposite);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		group = createHandlerComponent(pageComposite);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		updateControls();
	}
	
	private Composite createConnectorComponent(final Composite parent) {
		final String[] connectorLabels = new String[fConnectors.length];
		for (int i = 0; i < fConnectors.length; i++) {
			connectorLabels[i] = fConnectors[i].fName;
		}
		
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.RInteraction_RConnector);
		final Layouter layouter = new Layouter(group, 2);
		
		fConnectorsSelector = layouter.addComboControl(connectorLabels, 2);
		
		// Description
		layouter.addLabel(Messages.RInteraction_RConnector_Description_label, 0, 1, true);
		
		final ScrolledComposite scrolled = new ScrolledComposite(layouter.composite, SWT.V_SCROLL);
		fConnectorsDescription = addLinkControl(scrolled, ""); //$NON-NLS-1$
		scrolled.addControlListener(new ControlListener() {
			public void controlMoved(final org.eclipse.swt.events.ControlEvent e) {};
			public void controlResized(final org.eclipse.swt.events.ControlEvent e) {
				updateDescriptionSize();
			};
		});
		
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		final PixelConverter pixelConverter = new PixelConverter(fConnectorsDescription);
		gd.horizontalSpan = 1;
		gd.widthHint = pixelConverter.convertWidthInCharsToPixels(40);
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(5);
		scrolled.setLayoutData(gd);
		scrolled.setContent(fConnectorsDescription);
		
		fConnectorsSelector.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				final int idx = fConnectorsSelector.getSelectionIndex();
				if (idx >= 0) {
					setPrefValue(PREF_R_CONNECTOR, fConnectors[idx].fId);
					updateDescription(idx);
				}
			};
		});
		
		return group;
	}
	
	private void updateDescription(final int idx) {
		String description = fConnectors[idx].fDescription;
		if (description == null) {
			description = ""; //$NON-NLS-1$
		}
		fConnectorsDescription.setText(description);
		updateDescriptionSize();
	}
	
	private void updateDescriptionSize() {
		final Composite scroller = fConnectorsDescription.getParent();
		int widthHint = fConnectorsDescription.getParent().getClientArea().width;
		if (!scroller.getVerticalBar().isVisible())
			widthHint -= scroller.getVerticalBar().getSize().x;
		fConnectorsDescription.setSize(fConnectorsDescription.computeSize(
				widthHint, SWT.DEFAULT));
		
	}
	
	
	private Composite createHandlerComponent(final Composite parent) {
		fFileCommands = RCodeLaunchRegistry.getAvailableFileCommands();
		final IContentTypeManager manager = Platform.getContentTypeManager();
		
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.RInteraction_FileCommands_label);
		final GridLayout layout = LayoutUtil.applyGroupDefaults(new GridLayout(), 2);
		layout.verticalSpacing = 3;
		group.setLayout(layout);
		fCommandEditors = new SnippetEditor[fFileCommands.length];
		
		final TemplateVariableProcessor templateVariableProcessor = new TemplateVariableProcessor();
		// templateVariableProcessor does not work without context type, but prevents NPE etc. by use of RTemplateSourceViewerConfiguration
		// templateVariableProcessor.setContextType(contextType);
		
		for (int i = 0; i < fFileCommands.length; i++) {
			final Label label = new Label(group, SWT.NONE);
			label.setText(fFileCommands[i].getLabel() + ":"); //$NON-NLS-1$
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final RSourceViewerConfigurator configurator = new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(), templateVariableProcessor);
			fCommandEditors[i] = new SnippetEditor(configurator, fFileCommands[i].getCurrentCommand(), PlatformUI.getWorkbench());
			fCommandEditors[i].create(group, SnippetEditor.DEFAULT_SINGLE_LINE_STYLE);
			fCommandEditors[i].getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		return group;
	}
	
	
	@Override
	protected void updateControls() {
		loadValues();
		UIAccess.getDisplay(getShell()).asyncExec(new Runnable() {
			public void run() {
				final int idx = fConnectorsSelector.getSelectionIndex();
				if (idx >= 0) {
					updateDescription(idx);
				}
			}
		});
	}
		
	private void loadValues() {
		final String selectedConnector = getPreferenceValue(PREF_R_CONNECTOR);
		
		for (int i = 0; i < fConnectors.length; i++) {
			if (selectedConnector.equals(fConnectors[i].fId))
				fConnectorsSelector.select(i);
		}
	}
	
	
/* Load/Save of Handlers *****************************************************/
	
	private void saveHandlerConfig(final boolean save) {
		final IEclipsePreferences node = new InstanceScope().getNode(RDebugPreferenceConstants.CAT_CODELAUNCH_CONTENTHANDLER_QUALIFIER);
		for (int i = 0; i < fFileCommands.length; i++) {
			if (fCommandEditors[i] != null) {
				final String key = fFileCommands[i].getId()+":command"; //$NON-NLS-1$
				final String command = fCommandEditors[i].getDocument().get();
				if (command == null || command.length() == 0 || command.equals(fFileCommands[i].getDefaultCommand())) {
					node.remove(key);
				}
				else {
					node.put(key, command);
				}
			}
		}
		if (save) {
			try {
				node.flush();
			} catch (final BackingStoreException e) {
				logSaveError(e);
			}
		}
	}
	
	@Override
	public void performDefaults() {
		for (int i = 0; i < fFileCommands.length; i++) {
			if (fCommandEditors[i] != null) {
				fCommandEditors[i].getDocument().set(fFileCommands[i].getDefaultCommand());
				fCommandEditors[i].reset();
			}
		}
		super.performDefaults();
	}
	
	@Override
	public void performApply() {
		saveHandlerConfig(true);
		super.performApply();
	}
	
	@Override
	public boolean performOk() {
		saveHandlerConfig(false);
		return super.performOk();
	}
	
}
