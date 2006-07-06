package de.walware.statet.base.core.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class BasePlugin extends Plugin {


	/** The shared instance. */
	private static BasePlugin gPlugin;
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BasePlugin getDefault() {
		
		return gPlugin;
	}

	
	/**
	 * The constructor
	 */
	public BasePlugin() {
		
		gPlugin = this;
	}

	public void start(BundleContext context) throws Exception {
		
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		
		gPlugin = null;
		super.stop(context);
	}

}
