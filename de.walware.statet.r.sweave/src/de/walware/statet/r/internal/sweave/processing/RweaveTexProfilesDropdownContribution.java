/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_PREVIEW;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_TEX;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_WEAVE;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;

import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.sweave.processing.SweaveProcessing.IProcessingListener;


public class RweaveTexProfilesDropdownContribution extends CompoundContributionItem implements IProcessingListener {
	
	
	public static class ProcessWeave extends RweaveTexProfilesDropdownContribution {
		
		public ProcessWeave() {
			super(STEP_WEAVE);
		}
		
	}
	
	public static class ProcessDoc extends RweaveTexProfilesDropdownContribution {
		
		public ProcessDoc() {
			super(STEP_WEAVE | STEP_TEX);
		}
		
	}
	
	public static class PreviewDoc extends RweaveTexProfilesDropdownContribution {
		
		public PreviewDoc() {
			super(STEP_PREVIEW);
		}
		
	}
	
	public static class ProcessAndPreview extends RweaveTexProfilesDropdownContribution {
		
		public ProcessAndPreview() {
			super(0);
		}
		
	}
	
	
	private class ItemAction extends Action {
		
		private final ILaunchConfiguration fConfiguration;
		
		public ItemAction(final ILaunchConfiguration configuration, final int accelerator) {
			fConfiguration = configuration;
			final boolean isActiveProfile = (fSweaveManager.getActiveProfile() == configuration);
			setText(RweaveTexProfilesMenuContribution.createLabel(configuration, accelerator));
			setImageDescriptor(SweavePlugin.getDefault().getImageRegistry().getDescriptor(isActiveProfile ? SweavePlugin.IMG_OBJ_LTXRWEAVE_ACTIVE : SweavePlugin.IMG_OBJ_LTXRWEAVE));
		}
		
		@Override
		public void run() {
			doRun(fConfiguration);
		}
		
	}
	
	
	private SweaveProcessing fSweaveManager;
	private final int fLaunchFlags;
	
	
	public RweaveTexProfilesDropdownContribution(final int flags) {
		fLaunchFlags = flags;
		fSweaveManager = SweavePlugin.getDefault().getRweaveTexProcessingManager();
		fSweaveManager.addProcessingListener(this);
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
		final IContributionItem[] items = new IContributionItem[configs.length];
		
		int accelerator = 1;
		for (int i = 0; i < configs.length; i++) {
			items[i] = new ActionContributionItem(new ItemAction(configs[i], accelerator++));
		}
		return items;
	}
	
	@Override
	public void activeProfileChanged(final ILaunchConfiguration config) {
	}
	
	@Override
	public void availableProfileChanged(final ILaunchConfiguration[] configs) {
	}
	
	protected void doRun(final ILaunchConfiguration configuration) {
		fSweaveManager.setActiveProfile(configuration);
		fSweaveManager.launch(configuration, fLaunchFlags);
	}
	
}
