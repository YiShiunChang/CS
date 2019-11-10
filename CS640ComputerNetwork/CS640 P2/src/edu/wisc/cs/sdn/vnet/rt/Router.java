package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.*;
import java.util.*;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " + etherPacket.toString().replace("\n", "\n\t"));
		
		// check whether etherPacket contains IPv4 or not
    if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) {
      System.out.println("drop: not IPv4: " + etherPacket.getEtherType());
      return;
    }
    
    // if the frame contains an IPv4 packet, verify the checksum and time-to-live (TTL) of the 
    // IPv4 packet. Use the getPayload() method of the Ethernet class to get the IPv4 header.
    IPv4 ipv4 = (IPv4) etherPacket.getPayload();
    
    // check whether checksum is correct, this packet is corrupted if not 
    // the IP checksum should only be computed over the IP header. 
    // the length of the IP header can be determined from the header length field in the IP header, 
    // the checksum field in the IP header should be zeroed before calculating the IP checksum. 
    // use code from the serialize() method in the IPv4 class to compute the checksum.
    short currentChecksum = ipv4.getChecksum();
    ipv4.resetChecksum();
    ipv4.serialize(); // recalculate the checksum
    short correctChecksum = ipv4.getChecksum();
    if (currentChecksum != correctChecksum) {
      System.out.println("drop: checksum failed");
      return;
    }
    
    // Time-to-live (TTL) or hop limit is a mechanism that limits the lifespan or lifetime of data 
    // in a computer or network. TTL may be implemented as a counter or timestamp attached to or 
    // embedded in the data. Once the prescribed event count or timespan has elapsed, data is 
    // discarded or revalidated. 
    // in computer networking, TTL prevents a data packet from circulating indefinitely. 
    ipv4.setTtl((byte) (ipv4.getTtl() - 1));
    if (ipv4.getTtl() == 0) {
      System.out.println("drop: ttl 0");
      return;
    }
    
    // determine whether the packet is destined for one of the router’s interfaces.
    // the interfaces variable inherited from the Device class contains all interfaces on the 
    // router. Each interface has a name, MAC address, IP address, and subnet mask. 
    // if the packet’s destination IP address exactly matches one of the interface’s IP addresses, 
    // your router should drop the packet because the destination is the router itself.
    Iterator<Iface> allInterfaces = this.interfaces.values().iterator();
    while (allInterfaces.hasNext()) {
      if (ipv4.getDestinationAddress() == ((Iface) allInterfaces.next()).getIpAddress()) {
        System.out.println("drop: destination IP is router");
        return; 
      }
    }

    // forward packages
    // use the lookup() method in the RouteTable class to obtain the RouteEntry that has the longest prefix 
    // match with the destination IP address. If no entry matches, your router should drop the packet.
    int destAddress = ipv4.getDestinationAddress();
    // lookup() returns the route entry with matching address, null if none exists
    RouteEntry matchEntry = this.routeTable.lookup(destAddress);
    
    // nothing matches: drop
    if (matchEntry == null) {
      System.out.println("drop: routeTable lookup null");
      return;
    }

    // destination Iface is same as source Iface
    if (inIface.equals(matchEntry.getInterface())) {
      System.out.println("drop: destination Iface same as source Iface");
      return;
    }
    
    ipv4.resetChecksum();
    ipv4.serialize(); // recalculate the checksum
    
    // if an entry matches, determine the next-hop IP address and lookup the MAC address 
    // corresponding to that IP address. Use the lookup() method in the ArpCache class to obtain 
    // the MAC address from the statically populated ARP cache. 
    
    // set destMACAddress of sending-out packet
    // MAC address that corresponds to the next-hop IP address should be the new destination MAC 
    // address for the Ethernet frame
    int nextRouterIP = matchEntry.getGatewayAddress(); // next router IP address
    if(nextRouterIP == 0) {
    	nextRouterIP = ipv4.getDestinationAddress();
    }
    ArpEntry arpEntry = this.arpCache.lookup(nextRouterIP);
    
    // checks if an IP-MAC mapping is in the cache, drop the packet if none
    if (arpEntry == null) {
      System.out.println("drop: arp lookup null");
      return;
    }
    
    // set srcMACAddress of sending-out packet
    // the MAC address of the outgoing interface should be the new source MAC address for the Ethernet frame
    etherPacket.setSourceMACAddress(matchEntry.getInterface().getMacAddress().toBytes());
    
    // use the sendPacket() function inherited from the Device class to send the frame
    etherPacket.setDestinationMACAddress(arpEntry.getMac().toBytes());
    sendPacket(etherPacket, matchEntry.getInterface());
	}
}

