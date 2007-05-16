package de.walware.statet.base.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.core.preferences.SettingsChangeNotifier;


/**
 * The activator class controls the plug-in life cycle
 */
public class BaseCorePlugin extends Plugin {


	/** The shared instance. */
	private static BaseCorePlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BaseCorePlugin getDefault() {
		return gPlugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void logError(int code, String message, Throwable e) {
		log(new Status(IStatus.ERROR, StatetCore.PLUGIN_ID, code, message, e)); 
	}

	
	private SettingsChangeNotifier fSettingsNotifier;
	
	
	/**
	 * The constructor
	 */
	public BaseCorePlugin() {
		gPlugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		fSettingsNotifier = new SettingsChangeNotifier();
	}

	public void stop(BundleContext context) throws Exception {
		gPlugin = null;
		super.stop(context);
		
		if (fSettingsNotifier != null) {
			fSettingsNotifier.dispose();
			fSettingsNotifier = null;
		}
	}
	
	
	public SettingsChangeNotifier getSettingsChangeNotifier() {
		return fSettingsNotifier;
	}

}
