package Server.RMI;

import java.rmi.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.util.*;
import java.io.*;
import Server.Interface.*;
import Server.Common.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIMiddleware extends Middleware implements Remote {

  private static int s_serverPort = 1099;
	private static String s_rmiPrefix = "group28";

	private static String s_flightServerName = "Flight";
	private static String s_carServerName = "Car";
	private static String s_roomServerName = "Room";
	private static String s_customerServerName = "Customer";

	private static String s_flightServer = "localhost";
	private static String s_carServer = "localhost";
	private static String s_roomServer = "localhost";
	private static String s_customerServer = "localhost";

	private static ResourceManager flightRM;
  private static ResourceManager carRM;
  private static ResourceManager roomRM;
  private static ResourceManager customerRM;
	
	public static void main(String args[]) {

		if (args.length > 3) {
			s_flightServer = args[0];
			s_carServer = args[1];
			s_roomServer = args[2];
			s_customerServer = args[3];

			// Create the RMI server entry
			try {
				
				connectServers();
				// Create a new Server object that routes to four RMs
				RMIMiddleware middleware =
						new RMIMiddleware(flightRM, carRM, roomRM, customerRM);

				// Dynamically generate the stub (client proxy)
				IResourceManager middlewareEndpoint =
						(IResourceManager) UnicastRemoteObject.exportObject(middleware, 0);

				// Bind the four remote objects to endpoints with different names, but to the
				// same middleware interface
				Registry l_registry;
				try {
					l_registry = LocateRegistry.createRegistry(1099);
				} catch (RemoteException e) {
					l_registry = LocateRegistry.getRegistry(1099);
				}
				final Registry registry = l_registry;
				registry.rebind(s_rmiPrefix + s_flightServerName, middlewareEndpoint);
				registry.rebind(s_rmiPrefix + s_carServerName, middlewareEndpoint);
				registry.rebind(s_rmiPrefix + s_roomServerName, middlewareEndpoint);
				registry.rebind(s_rmiPrefix + s_customerServerName, middlewareEndpoint);

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						try {
							registry.unbind(s_rmiPrefix + s_flightServerName);
							System.out.println("'" + s_flightServerName + "' resource manager unbound");
							registry.unbind(s_rmiPrefix + s_carServerName);
							System.out.println("'" + s_carServerName + "' resource manager unbound");
							registry.unbind(s_rmiPrefix + s_roomServerName);
							System.out.println("'" + s_roomServerName + "' resource manager unbound");
							registry.unbind(s_rmiPrefix + s_customerServerName);
							System.out.println("'" + s_customerServerName + "' resource manager unbound");
						} catch (Exception e) {
							System.err.println(
									(char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
							e.printStackTrace();
						}
					}
				});
				System.out
						.println("'" + s_flightServerName + "' resource manager server ready and bound to '"
								+ s_rmiPrefix + s_flightServerName + "'");
				System.out.println("'" + s_carServerName + "' resource manager server ready and bound to '"
						+ s_rmiPrefix + s_carServerName + "'");
				System.out.println("'" + s_roomServerName + "' resource manager server ready and bound to '"
						+ s_rmiPrefix + s_roomServerName + "'");
				System.out
						.println("'" + s_customerServerName + "' resource manager server ready and bound to '"
								+ s_rmiPrefix + s_customerServerName + "'");
			} catch (Exception e) {
				System.err
						.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
				e.printStackTrace();
				System.exit(1);
			}

			// Create and install a security manager
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
		}
	}

	public static void connectServers() throws RemoteException {
		connectServer(s_flightServer, s_serverPort, s_flightServerName);
		connectServer(s_carServer, s_serverPort, s_carServerName);
		connectServer(s_roomServer, s_serverPort, s_roomServerName);
		connectServer(s_customerServer, s_serverPort, s_customerServerName);
	}

	public static void connectServer(String server, int port, String name) throws RemoteException {
		boolean first = true;
		while (true) {
			try {
				Registry registry = LocateRegistry.getRegistry(server, port);
				switch (name) {
					case "Flight": {
						flightRM =
							(ResourceManager) registry.lookup(s_rmiPrefix + name);
					}
					case "Car": {
						carRM =
							(ResourceManager) registry.lookup(s_rmiPrefix + name);
					}
					case "Room": {
						roomRM =
							(ResourceManager) registry.lookup(s_rmiPrefix + name);
					}
					case "Customer": {
						customerRM =
							(ResourceManager) registry.lookup(s_rmiPrefix + name);
					}
				}
				System.out.println("Connected to '" + name + "' server [" + server + ":" + port
					+ "/" + s_rmiPrefix + name + "]");
				Thread.sleep(500);
				break;
			} catch (NotBoundException | RemoteException | InterruptedException e) {
				if (first) {
				System.out.println("Waiting for '" + name + "' server [" + server + ":"
					+ port + "/" + s_rmiPrefix + name + "]");
				first = false;
				}
			}
		}
	}

	public RMIMiddleware(ResourceManager flightRM, ResourceManager carRM, ResourceManager roomRM, ResourceManager customerRM)
	{
		super(flightRM, carRM, roomRM, customerRM);
	}
}
