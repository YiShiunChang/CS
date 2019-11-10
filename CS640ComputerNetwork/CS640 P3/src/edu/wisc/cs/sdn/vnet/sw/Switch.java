package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;

import java.util.*;
import static java.util.concurrent.TimeUnit.*;
import java.util.concurrent.*;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device
{		
	
	// a switch has a table to store "interfaces and its corresponing macaddress" 
	// a switch has a table to store "when a interface-macaddress pair is created"
	// String-to-Iface = macAddress-to-interface 
	// String-to-long = macAddress-to-time
	private Map<String, Iface> macToIface;
	private Map<String, Long> createdTime;
	
	// reset the macaddress-to-interface table every TIMEOUT seconds 
	private static final long TIMEOUT = 15000;// in ms
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		// creates a device with host as hostname, 
		// and logfile as PCAP dump file for logging all packets sent/received by the device
		super(host,logfile);
		macToIface = new HashMap<String, Iface>();
		createdTime = new HashMap<String, Long>();
		
		// timer is used for counting a period 15 seconds,
		// reset the macaddress-to-interface table every 15 seconds 
		timer();
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " + etherPacket.toString().replace("\n", "\n\t"));
		// get the info in the package
	  String sMacAddress = etherPacket.getSourceMAC().toString();
	  String dMacAddress = etherPacket.getDestinationMAC().toString();
	  
	  // "learn" source MAC, and builds the switch table by storing 
	  // which inIface should a macAddress correspond to, and which time a macAddress correspond to
	  macToIface.put(sMacAddress, inIface);
	  createdTime.put(sMacAddress, System.currentTimeMillis());

	  // check for destination MAC
	  if (macToIface.containsKey(dMacAddress)) {
	  	Iface dInterFace = macToIface.get(dMacAddress);
	  	this.sendPacket(etherPacket, dInterFace);
	  } else {
	  // if destination interFace not exist => broadcast
	  	Iterator<Iface> allInterfaces = this.interfaces.values().iterator();
	  	while (allInterfaces.hasNext()) {
	  		Iface out = allInterfaces.next();
	   		// don't broadcast to sender
	     	if (!out.equals(inIface)) {
	     		this.sendPacket(etherPacket, out);
	     	}
	  	}
	  }
	}
	
	/**
	 * This function check the "macAddress-to-interface" map, and the "macAddress-to-time" map
	 * every second, if a key is out of date, then remove it from both maps
	 */
	public void timer() {
		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					System.out.println("run");

					List<String> remove = new ArrayList<>();
					for (String macAds: macToIface.keySet()) {
						if (System.currentTimeMillis() - createdTime.get(macAds) > TIMEOUT) {
							remove.add(macAds);
						}
					}
	
					for (String macAds: remove) {
						macToIface.remove(macAds);
						createdTime.remove(macAds);
					}
				} catch (Exception e) {
					System.out.println("Caught Exception in run: " + e.getClass().getName());
					e.printStackTrace();
				}
			}
		}, 0, 1, SECONDS);
	}
}
