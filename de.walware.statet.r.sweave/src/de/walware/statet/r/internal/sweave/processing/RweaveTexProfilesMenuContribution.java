/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.processing;

import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.BUILDTEX_TYPE_DISABLED;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_PREVIEW;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_TEX;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_WEAVE;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.keys.IBindingService;

import de.walware.ecommons.ui.actions.SubMenuContributionItem;
import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.sweave.processing.SweaveProcessing.IProcessingListener;


public class RweaveTexProfilesMenuContribution extends CompoundContributionItem implements IProcessingListener {
	
	
	static String createLabel(final ILaunchConfiguration config, final int num) {
		final String orgLabel = config.getName();
		final StringBuffer label = new StringBuffer(orgLabel.length()+5);
		if (num >= 0 && num < 10) {
			//add the numerical accelerator
			label.append('&');
			label.append(num);
			label.append(' ');
		}
		label.append(MessageUtil.escapeForMenu(orgLabel));
		return label.toString();
	}
	
	final static String RUN = "run"; //$NON-NLS-1$
	final static String RWEAVE = "rweave"; //$NON-NLS-1$
	final static String TEX = "tex"; //$NON-NLS-1$
	final static String BUILD = "build"; //$NON-NLS-1$
	final static String PREVIEW = "preview"; //$NON-NLS-1$
	final static String SELECT = "select"; //$NON-NLS-1$
	final static String EDIT = "edit"; //$NON-NLS-1$
	
	
	private class ProfileContribution extends SubMenuContributionItem implements SelectionListener {
		
		
		private final int fNum;
		private final ILaunchConfiguration fConfig;
		
		
		public ProfileContribution(final ILaunchConfiguration config, final int num) {
			super();
			
			fNum = num;
			fConfig = config;
		}
		
		
		@Override
		protected Image getImage() {
			final boolean isActiveProfile = (fSweaveManager.getActiveProfile() == fConfig);
			return SweavePlugin.getDefault().getImageRegistry().get(isActiveProfile ? SweavePlugin.IMG_OBJ_LTXRWEAVE_ACTIVE : SweavePlugin.IMG_OBJ_LTXRWEAVE);
		}
		
		@Override
		protected String getLabel() {
			return createLabel(fConfig, fNum);
		}
		
		@Override
		protected void fillMenu(final Menu menu) {
			final boolean isActiveProfile = (fSweaveManager.getActiveProfile() == fConfig);
			final StringBuilder detailComplete = new StringBuilder(Messages.ProcessingAction_BuildAndPreview_label);
			boolean showBuild = false;
			final StringBuilder detailBuild = new StringBuilder(Messages.ProcessingAction_BuildDoc_label);
			boolean disableSweave = false;
			final StringBuilder detailSweave = new StringBuilder(Messages.ProcessingAction_Sweave_label);
			boolean disableTex = false;
			final StringBuilder detailTex = new StringBuilder(Messages.ProcessingAction_Tex_label);
			final StringBuilder detailPreview = new StringBuilder(Messages.ProcessingAction_Preview_label);
			try {
				final String attribute = fConfig.getAttribute(RweaveTab.ATTR_SWEAVE_ID, (String) null);
				if (attribute != null && !attribute.isEmpty()) {
					detailSweave.append("  (Rnw > TeX)"); //$NON-NLS-1$
				}
				else {
					disableSweave = true;
				}
				
				if (fConfig.getAttribute(TexTab.ATTR_BUILDTEX_TYPE, -2) > BUILDTEX_TYPE_DISABLED) {
					String format = fConfig.getAttribute(TexTab.ATTR_BUILDTEX_FORMAT, (String) null);
					if (format != null) {
						detailTex.append("  (TeX > "+ format + ")"); //$NON-NLS-1$ //$NON-NLS-2$
						if (!disableSweave) {
							showBuild = true;
							detailBuild.append("  (Rnw > "+ format + ")"); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				else {
					disableTex = true;
				}
			} catch (final CoreException e) {
			}
			
			if (isActiveProfile) {
				final IBindingService bindings = (IBindingService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IBindingService.class);
				TriggerSequence binding;
				binding = bindings.getBestActiveBindingFor(RweaveTexProfileDefaultHandler.ProcessAndPreview.COMMAND_ID);
				if (binding != null) {
					detailComplete.append('\t');
					detailComplete.append(binding.format());
				}
				if (showBuild) {
					binding = bindings.getBestActiveBindingFor(RweaveTexProfileDefaultHandler.ProcessDoc.COMMAND_ID);
					if (binding != null) {
						detailBuild.append('\t');
						detailBuild.append(binding.format());
					}
				}
				binding = bindings.getBestActiveBindingFor(RweaveTexProfileDefaultHandler.ProcessWeave.COMMAND_ID);
				if (binding != null) {
					detailSweave.append('\t');
					detailSweave.append(binding.format());
				}
				binding = bindings.getBestActiveBindingFor(RweaveTexProfileDefaultHandler.ProcessTex.COMMAND_ID);
				if (binding != null) {
					detailTex.append('\t');
					detailTex.append(binding.format());
				}
				binding = bindings.getBestActiveBindingFor(RweaveTexProfileDefaultHandler.PreviewDoc.COMMAND_ID);
				if (binding != null) {
					detailPreview.append('\t');
					detailPreview.append(binding.format());
				}
			}
			
			{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(detailComplete.toString());
				item.setImage(SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_BUILDANDPREVIEW));
				item.setData(RUN);
				item.addSelectionListener(this);
			}
			if (showBuild) {
				final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(detailBuild.toString());
				item.setImage(SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_BUILD));
				item.setData(BUILD);
				item.addSelectionListener(this);
				item.setEnabled(!disableTex);
			}
			new MenuItem(menu, SWT.SEPARATOR);
			
			{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(detailSweave.toString());
				item.setImage(SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_RWEAVE));
				item.setData(RWEAVE);
				item.addSelectionListener(this);
				item.setEnabled(!disableSweave);
			}
			{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(detailTex.toString());
				item.setImage(SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_BUILDTEX));
				item.setData(TEX);
				item.addSelectionListener(this);
				item.setEnabled(!disableTex);
			}
			{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(detailPreview.toString());
				item.setImage(SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_PREVIEW));
				item.setData(PREVIEW);
				item.addSelectionListener(this);
				item.setEnabled(!disableTex);
			}
			new MenuItem(menu, SWT.SEPARATOR);
			
			{	final MenuItem item = new MenuItem(menu, SWT.RADIO);
				item.setText(Messages.ProcessingAction_ActivateProfile_label);
				item.setData(SELECT);
				item.addSelectionListener(this);
				item.setSelection(isActiveProfile);
			}
			{	final MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(Messages.ProcessingAction_EditProfile_label);
				item.setData(EDIT);
				item.addSelectionListener(this);
			}
		}
		
		
		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Object data = e.widget.getData();
			if (data == null) {
				return;
			}
			if (data == RUN) {
				fSweaveManager.launch(fConfig, 0);
				return;
			}
			if (data == RWEAVE) {
				fSweaveManager.launch(fConfig, STEP_WEAVE);
				return;
			}
			if (data == TEX) {
				fSweaveManager.launch(fConfig, STEP_TEX);
				return;
			}
			if (data == BUILD) {
				fSweaveManager.launch(fConfig, STEP_WEAVE | STEP_TEX);
				return;
			}
			if (data == PREVIEW) {
				fSweaveManager.launch(fConfig, STEP_PREVIEW);
				return;
			}
			if (data == SELECT) {
				fSweaveManager.setActiveProfile(fConfig);
				return;
			}
			if (data == EDIT) {
				fSweaveManager.openConfigurationDialog(UIAccess.getActiveWorkbenchShell(true),
						new StructuredSelection(fConfig));
				return;
			}
		}
		
	}
	
	private class ConfigureContribution extends ContributionItem implements SelectionListener {
		
		private MenuItem fMenuItem;
		
		@Override
		public void dispose() {
			if (fMenuItem != null) {
				fMenuItem.dispose();
				fMenuItem = null;
			}
			super.dispose();
		}
		
		@Override
		public void fill(final Menu parent, final int index) {
			if (fMenuItem == null || fMenuItem.isDisposed()) {
				if (index >= 0) {
					fMenuItem = new MenuItem(parent, SWT.PUSH, index);
				} else {
					fMenuItem = new MenuItem(parent, SWT.PUSH);
				}
				
				fMenuItem.setText(Messages.ProcessingAction_CreateEditProfiles_label);
				fMenuItem.addSelectionListener(this);
			}
		}
		
		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			fSweaveManager.openConfigurationDialog(UIAccess.getActiveWorkbenchShell(true), null);
		}
		
	}
	
	
	private SweaveProcessing fSweaveManager;
	
	
	public RweaveTexProfilesMenuContribution() {
		fSweaveManager = SweavePlugin.getDefault().getRweaveTexProcessingManager();
		fSweaveManager.addProcessingListener(this);
	}
	
	public RweaveTexProfilesMenuContribution(final String id) {
		super(id);
	}
	
	@Override
	public void dispose() {
		if (fSweaveManager != null) {
			fSweaveManager.removeProcessingListener(this);
			fSweaveManager = null;
		}
		super.dispose();
	}
	
	@Override
	protected IContributionItem[] getContributionItems() {
		final ILaunchConfiguration[] configs = fSweaveManager.getAvailableProfiles();
		final IContributionItem[] items = new IContributionItem[configs.length+1];
		
		int accelerator = 1;
		int i = 0;
		for (; i < configs.length; i++) {
			items[i] = new ProfileContribution(configs[i], accelerator++);
		}
		items[i++] = new ConfigureContribution();
		return items;
	}
	
	@Override
	public void activeProfileChanged(final ILaunchConfiguration config) {
	}
	
	@Override
	public void availableProfileChanged(final ILaunchConfiguration[] configs) {
	}
	
}
