package de.walware.ecommons.net;

import java.io.File;
import java.net.BindException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.internal.net.Messages;


/**
 * Utility managing local RMI registries:
 * <ul>
 *   <li>Embedded private RMI registry: A single registry
 *     intended to use for the current application only.</li>
 *   <li>Separate RMI registries: Started in a separate
 *     process at a specified port. Multiple registries and
 *     shutdown behaviour possible.</li>
 * </ul>
 * 
 * Note: stop modes are not applied if the registry is already started. 
 */
public class RMIUtil {
	
	
	public static enum StopRule {
		
		/**
		 * Mode to stop the registry always automatically.
		 */
		ALWAYS,
		
		/**
		 * Mode to stop the registry never automatically.
		 */
		NEVER,
		
		/**
		 * Mode to stop the registry if empty.
		 */
		IF_EMPTY,
		
	}
	
	private static final class Port {
		
		private final int port;
		
		public Port(final int port) {
			this.port = port;
		}
		
		public int get() {
			return this.port;
		}
		
		@Override
		public int hashCode() {
			return this.port << 8;
		}
		
		@Override
		public boolean equals(final Object obj) {
			return ((obj instanceof Port) && this.port == ((Port) obj).port);
		}
		
	}
	
	private static final class ManagedRegistry {
		
		final RMIRegistry registry;
		Process process;
		StopRule stopRule;
		
		public ManagedRegistry(final RMIRegistry registry, final StopRule stopRule) {
			this.registry = registry;
			this.stopRule = stopRule;
		}
		
	}
	
	
	private static final int EMBEDDED_PORT_FROM_DEFAULT = 49152;
	private static final int EMBEDDED_PORT_TO_DEFAULT = 65535;
	
	public static final RMIUtil INSTANCE = new RMIUtil();
	
	
	private final Map<Port, ManagedRegistry> registries = new HashMap<Port, ManagedRegistry>();
	
	private final Object embeddedLock = new Object();
	private int embeddedPortFrom = EMBEDDED_PORT_FROM_DEFAULT;
	private int embeddedPortTo = EMBEDDED_PORT_TO_DEFAULT;
	private boolean embeddedStartSeparate = false;
	private ManagedRegistry embeddedRegistry;
	
	
	private RMIUtil() {
		initDispose();
	}
	
	protected void initDispose() {
		ECommons.getEnv().addStoppingListener(new IDisposable() {
			public void dispose() {
				RMIUtil.this.dispose();
			}
		});
	}
	
	
	/**
	 * Returns a handler for the RMI registry at the local host and the given port.
	 * 
	 * The registry must be started by this util instance.
	 * 
	 * @param port the registry port
	 * @return the registry handler or <code>null</code>
	 */
	public RMIRegistry getRegistry(int port) {
		if (port <= 0) {
			port = Registry.REGISTRY_PORT;
		}
		
		final Port key = new Port(port);
		final ManagedRegistry r;
		synchronized (this) {
			r = this.registries.get(key);
		}
		return (r != null) ? r.registry : null;
	}
	
	/**
	 * Sets the port for the managed embedded private RMI registry.
	 * 
	 * @param port the registry port
	 */
	public void setEmbeddedPrivatePort(final int port) {
		setEmbeddedPrivatePortDynamic(port, port);
	}
	
	/**
	 * Sets the valid port range for the managed embedded private RMI registry.
	 * An unused port for the registry is search inside this range.
	 * 
	 * @param from lowest valid registry port
	 * @param to highest valid the registry port
	 */
	public void setEmbeddedPrivatePortDynamic(final int from, final int to) {
		if (from > to) {
			throw new IllegalArgumentException("from > to");
		}
		synchronized (this.embeddedLock) {
			this.embeddedPortFrom = (from >= 0) ? from : EMBEDDED_PORT_FROM_DEFAULT;
			this.embeddedPortTo = (to >= 0) ? to : EMBEDDED_PORT_TO_DEFAULT;
		}
	}
	
	/**
	 * Sets the start mode for the managed embedded private RMI registry.
	 * 
	 * @param separate start registry in separate process
	 */
	public void setEmbeddedPrivateMode(final boolean separate) {
		synchronized (this.embeddedLock) {
			this.embeddedStartSeparate = separate;
		}
	}
	
	/**
	 * Returns the managed embedded private RMI registry.
	 * 
	 * @return the registry or <code>null</code> if not available
	 */
	public RMIRegistry getEmbeddedPrivateRegistry() {
		ManagedRegistry r = null;
		synchronized (this.embeddedLock) {
			if (this.embeddedRegistry != null) {
				return this.embeddedRegistry.registry;
			}
			
			final int startPort = new Random().nextInt(this.embeddedPortTo-this.embeddedPortFrom+1) + this.embeddedPortFrom;
			for (int port = startPort; ; ) {
				try {
					final RMIAddress rmiAddress = new RMIAddress(RMIAddress.LOOPBACK, port, null);
					if (this.embeddedStartSeparate) {
						if (startSeparateRegistry(rmiAddress, StopRule.ALWAYS).getSeverity() < IStatus.ERROR) {
							r = this.registries.get(new Port(port));
						}
					}
					else {
						final Registry javaRegistry = LocateRegistry.createRegistry(port);
						final RMIRegistry registry = new RMIRegistry(rmiAddress, javaRegistry, false);
						r = new ManagedRegistry(registry, StopRule.NEVER);
						r.stopRule = StopRule.ALWAYS;
					}
					if (r != null) {
						this.embeddedRegistry = r;
						break;
					}
				}
				catch (final Exception e) {
					if (!(e.getCause() instanceof BindException)) {
						return null;
					}
				}
				port++;
				if (this.embeddedPortFrom - this.embeddedPortTo > 200
						&& (port % 10 == 0)) {
					port += 10;
				}
				if (port > this.embeddedPortTo) {
					port = this.embeddedPortFrom;
				}
				if (port == startPort) {
					return null;
				}
			}
		}
		final Port key = new Port(r.registry.getAddress().getPortNum());
		synchronized (this) {
			this.registries.put(key, r);
		}
		return r.registry;
	}
	
	
	public IStatus startSeparateRegistry(RMIAddress address, final StopRule stopRule) {
		final InetAddress hostAddress = address.getHostAddress();
		if (!(hostAddress.isLinkLocalAddress() || hostAddress.isLoopbackAddress())) {
			throw new IllegalArgumentException("address not local");
		}
		if (address.getName() != null) {
			try {
				address = new RMIAddress(address, null);
			} catch (final MalformedURLException e) {}
		}
		
		try {
			final Registry registry = LocateRegistry.getRegistry(address.getPortNum());
			registry.list();
			final Port key = new Port(address.getPortNum());
			synchronized (this) {
				if (!this.registries.containsKey(key)) {
					final ManagedRegistry r = new ManagedRegistry(new RMIRegistry(address, registry, false), StopRule.NEVER);
					this.registries.put(key, r);
				}
			}
			return new Status(IStatus.INFO, ECommons.PLUGIN_ID, MessageFormat.format(Messages.RMI_status_RegistryAlreadyStarted_message, address.getPort()));
		}
		catch (final AccessException e) {}
		catch (final RemoteException e) {}
		final Process process;
		try {
			final String registryExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "rmiregistry"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			process = Runtime.getRuntime().exec(new String[] { registryExe, address.getPort() });
		}
		catch (final Exception e) {
			return new Status(IStatus.ERROR, ECommons.PLUGIN_ID, MessageFormat.format(Messages.RMI_status_RegistryStartFailed_message, address.getPort()), e);
		}
		RemoteException lastException = null;
		for (int i = 0; i < 20; i++) {
			try {
				final int exit = process.exitValue();
				return new Status(IStatus.ERROR, ECommons.PLUGIN_ID, MessageFormat.format(Messages.RMI_status_RegistryStartFailedWithExitValue_message, address.getPort(), exit));
			}
			catch (final IllegalThreadStateException e) {
			}
			try {
				final Registry registry = LocateRegistry.getRegistry(address.getPortNum());
				final ManagedRegistry r = new ManagedRegistry(new RMIRegistry(address, registry, true),
						(stopRule != null) ? stopRule : StopRule.IF_EMPTY);
				r.process = process;
				final Port key = new Port(address.getPortNum());
				synchronized (this) {
					this.registries.put(key, r);
				}
				return Status.OK_STATUS;
			}
			catch (final AccessException e) {}
			catch (final RemoteException e) {
				lastException = e;
			}
			try {
				Thread.sleep(50);
			}
			catch (final InterruptedException e) {
				Thread.interrupted();
			}
		}
		return new Status(IStatus.ERROR, ECommons.PLUGIN_ID, MessageFormat.format(Messages.RMI_status_RegistryStartFailed_message, address.getPort()), lastException);
	}
	
	public IStatus startSeparateRegistry(int port, final StopRule stopRule) {
		if (port <= 0) {
			port = Registry.REGISTRY_PORT;
		}
		try {
			return startSeparateRegistry(new RMIAddress((String) null, port, null), stopRule);
		}
		catch (final UnknownHostException e) {
			throw new IllegalStateException();
		}
		catch (final MalformedURLException e) { // invalid port
			return new Status(IStatus.ERROR, ECommons.PLUGIN_ID, MessageFormat.format(Messages.RMI_status_RegistryStartFailed_message, port), e);
		}
	}
	
	public IStatus stopSeparateRegistry(final RMIAddress address) {
		return stopSeparateRegistry(address.getPortNum());
	}
	
	public IStatus stopSeparateRegistry(int port) {
		if (port <= 0) {
			port = Registry.REGISTRY_PORT;
		}
		
		final Port key = new Port(port);
		final ManagedRegistry r;
		synchronized (this) {
			r = this.registries.get(key);
			if (r == null || r.process == null) {
				return new Status(IStatus.ERROR, ECommons.PLUGIN_ID, MessageFormat.format(Messages.RMI_status_RegistryStopFailedNotFound_message, port));
			}
			this.registries.remove(key);
		}
		
		r.process.destroy();
		return Status.OK_STATUS;
	}
	
	
	protected void dispose() {
		synchronized(this) {
			for (final ManagedRegistry r : this.registries.values()) {
				if (r.process == null) {
					continue;
				}
				switch (r.stopRule) {
				case ALWAYS:
					break;
				case NEVER:
					continue;
				case IF_EMPTY:
					try {
						final Registry registry = LocateRegistry.getRegistry(r.registry.getAddress().getPortNum());
						if (registry.list().length > 0) {
							continue;
						}
					}
					catch (final RemoteException e) {}
					break;
				}
				r.process.destroy();
				r.process = null;
			}
			this.registries.clear();
		}
	}
	
}
