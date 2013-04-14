package de.walware.statet.rtm.base.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.StringPref2;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.rtm.base.internal.ui.actions.Messages;
import de.walware.statet.rtm.base.ui.RtModelUIPlugin;


public class PerspectiveUtil {
	// see org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard.updatePerspective(IConfigurationElement)
	
	
	private static String QUALIFIER = RtModelUIPlugin.PLUGIN_ID + "/perspectives"; //$NON-NLS-1$
	
	
	/**
	 * Updates the perspective based on the current settings in the
	 * Workbench/Perspectives preference page.
	 * 
	 * Use the setting for the new perspective opening if we are set to open in
	 * a new perspective.
	 */
	public void updatePerspective(final String finalPerspId) {
		if (finalPerspId == null) {
			return;
		}
		final Preference<String> pref = new StringPref2(QUALIFIER, "SwitchTo-" + finalPerspId + "-.action"); //$NON-NLS-1$ //$NON-NLS-2$
		
		final String prefValue = PreferencesUtil.getInstancePrefs().getPreferenceValue(pref);
		
		// Already disabled
		if ((MessageDialogWithToggle.NEVER.equals(prefValue))) {
			return;
		}
		
		// Map perspective id to descriptor.
		final IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
		
		// leave this code in - the perspective of a given project may map to
		// activities other than those that the wizard itself maps to.
		final IPerspectiveDescriptor finalPersp = reg.findPerspectiveWithId(finalPerspId);
		if (finalPersp != null && finalPersp instanceof IPluginContribution) {
			final IPluginContribution contribution = (IPluginContribution) finalPersp;
			if (contribution.getPluginId() != null) {
				final IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
						.getWorkbench().getActivitySupport();
				final IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
				final IIdentifier identifier = activityManager.getIdentifier(
						WorkbenchActivityHelper.createUnifiedId(contribution) );
				@SuppressWarnings("unchecked")
				final
				Set<String> idActivities = identifier.getActivityIds();
				
				if (!idActivities.isEmpty()) {
					@SuppressWarnings("unchecked")
					final
					Set<String> enabledIds = new HashSet<String>(activityManager.getEnabledActivityIds());
					
					if (enabledIds.addAll(idActivities)) {
						workbenchActivitySupport.setEnabledActivityIds(enabledIds);
					}
				}
			}
		}
		else {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID,
					NLS.bind("Unable to find perspective with id '{0}'.", finalPerspId) )); //$NON-NLS-1$
			return;
		}
		
		// gather the preferred perspectives
		// always consider the final perspective (and those derived from it)
		// to be preferred
		final ArrayList<String> preferredPerspIds = new ArrayList<String>();
		addPerspectiveAndDescendants(preferredPerspIds, finalPerspId);
		{	final List<String> preferred = getPreferedPerspectives(finalPerspId);
			for (final String preferedId : preferred) {
				addPerspectiveAndDescendants(preferredPerspIds, preferedId);
			}
		}
		
		final IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
		if (window != null) {
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				final IPerspectiveDescriptor currentPersp = page.getPerspective();
				
				// don't switch if the current perspective is a preferred
				// perspective
				if (currentPersp != null && preferredPerspIds.contains(currentPersp.getId())) {
					return;
				}
			}
		}
		
		// prompt the user to switch
		if (!MessageDialogWithToggle.ALWAYS.equals(prefValue)
				&& !confirmPerspectiveSwitch(window, finalPersp, pref)) {
			return;
		}
		
		try {
			PlatformUI.getWorkbench().showPerspective(finalPerspId, window);
		}
		catch (final WorkbenchException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID, 0,
					NLS.bind("An error occurred when opening the {0} perspective, associated with the R task.", finalPersp.getLabel()),
					e ));
		}
	}
	
	private List<String> getPreferedPerspectives(final String perspId) {
		return Collections.emptyList();
	}
	
	/**
	 * Adds to the list all perspective IDs in the Workbench who's original ID
	 * matches the given ID.
	 * 
	 * @param perspectiveIds
	 *            the list of perspective IDs to supplement.
	 * @param id
	 *            the id to query.
	 * @since 3.0
	 */
	private void addPerspectiveAndDescendants(final List<String> perspectiveIds, final String id) {
		final IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		final IPerspectiveDescriptor[] perspectives = registry.getPerspectives();
		for (int i = 0; i < perspectives.length; i++) {
			// @issue illegal ref to workbench internal class;
			// consider adding getOriginalId() as API on IPerspectiveDescriptor
			final PerspectiveDescriptor descriptor = ((PerspectiveDescriptor) perspectives[i]);
			if (descriptor.getOriginalId().equals(id)) {
				perspectiveIds.add(descriptor.getId());
			}
		}
	}
	
	/**
	 * Prompts the user for whether to switch perspectives.
	 * 
	 * @param window
	 *            The workbench window in which to switch perspectives; must not
	 *            be <code>null</code>
	 * @param finalPersp
	 *            The perspective to switch to; must not be <code>null</code>.
	 * 
	 * @return <code>true</code> if it's OK to switch, <code>false</code>
	 *         otherwise
	 */
	private boolean confirmPerspectiveSwitch(final IWorkbenchWindow window,
			final IPerspectiveDescriptor finalPersp, final Preference<String> pref) {
		final String desc = finalPersp.getDescription();
		String message;
		if (desc == null || desc.length() == 0) {
			message = NLS.bind(Messages.NewTask_PerspSwitch_message,
					finalPersp.getLabel() );
		}
		else {
			message = NLS.bind(Messages.NewTask_PerspSwitch_WithDesc_message,
					new String[] { finalPersp.getLabel(), desc } );
		}
		
		final MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
				window.getShell(),
				Messages.NewTask_PerspSwitch_title, message,
				null /* use the default message for the toggle */,
				false /* toggle is initially unchecked */, null, null );
		final int result = dialog.getReturnCode();
		
		// If we are not going to prompt anymore propogate the choice.
		if (dialog.getToggleState()) {
			String prefValue;
			if (result == IDialogConstants.YES_ID) {
				// Doesn't matter if it is replace or new window
				// as we are going to use the open perspective setting
				prefValue = MessageDialogWithToggle.ALWAYS;
			}
			else {
				prefValue = MessageDialogWithToggle.NEVER;
			}
			PreferencesUtil.setPrefValue(InstanceScope.INSTANCE, pref, prefValue);
		}
		return result == IDialogConstants.YES_ID;
	}
	
}
