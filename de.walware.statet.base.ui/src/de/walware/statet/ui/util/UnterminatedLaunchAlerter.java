package de.walware.statet.ui.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetMessages;


/**
 * A workbench listener that warns the user about any running tools when
 * the workbench closes. Programs are killed when the VM exits.
 */
public class UnterminatedLaunchAlerter implements IWorkbenchListener {

	
	private static Object gMutex = new Object();
	private static UnterminatedLaunchAlerter gInstance;
	
	public static void registerLaunchType(String id) {

		synchronized (gMutex) {
			if (gInstance == null) {
				gInstance = new UnterminatedLaunchAlerter();
			}
			gInstance.fLauchTypeIds.add(id);
		}
	}
	
	
	private Set<String> fLauchTypeIds = new HashSet<String>();

	private UnterminatedLaunchAlerter() { 
		
		PlatformUI.getWorkbench().addWorkbenchListener(this);
	}
	
	
	public boolean preShutdown(IWorkbench workbench, boolean forced) {

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		List<ILaunchConfigurationType> programTypes = new LinkedList<ILaunchConfigurationType>();
		synchronized (gMutex) {
			for (String id : fLauchTypeIds) {
				ILaunchConfigurationType programType = manager.getLaunchConfigurationType(id);
				if (programType != null) {
					programTypes.add(programType);
				}
			}
		}
		if (programTypes.isEmpty()) {
			return true;
		}
		
		Set<ILaunchConfigurationType> stillRunningTypes = new HashSet<ILaunchConfigurationType>();
		int count = 0;
		ILaunch launches[] = manager.getLaunches();
		for (ILaunch launch : launches) {
			ILaunchConfigurationType configType;
			ILaunchConfiguration config;
			try {
				config = launch.getLaunchConfiguration();
				if (config == null) {
					continue;
				}
				configType = config.getType();
			} catch (CoreException e) {
				continue;
			}
			if (programTypes.contains(configType) && !launch.isTerminated()) {
				count++;
				stillRunningTypes.add(configType);
			}
		}
		if (stillRunningTypes.isEmpty()) {
			return true;
		}
		
		StringBuilder names = new StringBuilder(stillRunningTypes.size()*20);
		names.append('\n');
		for (ILaunchConfigurationType type : stillRunningTypes) {
			names.append("- ");
			names.append(type.getName());
			names.append('\n');
		}
		String message = NLS.bind(StatetMessages.UnterminatedLaunchAlerter_WorkbenchClosing_message, 
				new Object[] { count, names });
		if (forced) {
			MessageDialog.openWarning(UIAccess.getDisplay().getActiveShell(),
					StatetMessages.UnterminatedLaunchAlerter_WorkbenchClosing_title, message);
			return true;
		}
		else {
			MessageDialog dialog = new MessageDialog(UIAccess.getDisplay().getActiveShell(),
					StatetMessages.UnterminatedLaunchAlerter_WorkbenchClosing_title, null, message,
					MessageDialog.WARNING,
					new String[] { 
						StatetMessages.UnterminatedLaunchAlerter_WorkbenchClosing_button_Continue,
						StatetMessages.UnterminatedLaunchAlerter_WorkbenchClosing_button_Cancel, 
					}, 1);
			int answer = dialog.open();
			if (answer == 1) {
				return false;
			}
			return true;
		}
	}

	public void postShutdown(IWorkbench workbench) {
	}
}
