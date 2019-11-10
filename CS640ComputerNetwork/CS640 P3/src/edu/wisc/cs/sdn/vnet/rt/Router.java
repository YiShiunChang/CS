package edu.wisc.cs.sdn.vnet.rt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.SECONDS;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.MACAddress;
import net.floodlightcontroller.packet.RIPv2;
import net.floodlightcontroller.packet.RIPv2Entry;
import net.floodlightcontroller.packet.UDP;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device {	
	/** Destination IP address should be the IP address reserved for RIP (224.0.0.9) */
	private final static int RIP_BROADCAST_IP = IPv4.toIPv4Address("224.0.0.9");
	/** Destination Ethernet address should be the broadcast MAC address (FF:FF:FF:FF:FF:FF) */
  private final static byte[] RIP_BROADCAST_MAC = new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/** Map: IP--Queue */
  private Map<Integer, LinkedList<Ethernet>> PacWaitMAC;

  /** Map: SubnetNumber--Distance vectors */
  public Map<Integer, RIPTableEntry> ripTable;
  
  private Router routerPointerThis = this;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile) {
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
		this.PacWaitMAC = new HashMap<Integer, LinkedList<Ethernet>>();
    this.ripTable = new HashMap<Integer, RIPTableEntry>();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable() { 
		return this.routeTable; 
	}
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile) {
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
   * When a static route table is not provided for current router, 
   * this method build RIP to build a route table.
   */
  public void startRIP() {
    // when the current router starts, add entries to the route table for the subnets that are directly reachable via the router’s interfaces
		// router should send a RIP request out all of the current router’s interfaces when RIP is initialized
		for (Iface iface : this.interfaces.values()) {
			// subnet can be determined based on the IP address and mask that is
			// associated with each of the router’s interfaces. 
      int mask = iface.getSubnetMask();
      int subnetNumber = iface.getIpAddress() & mask;
      // these entries should have no gateway.
      // insert(int dstIp, int gwIp, int maskIp, Iface iface)
      routeTable.insert(subnetNumber, 0, mask, iface);
      // map with key: SubnetNumber, and value: Distance vector
      // RIPTableEntry has timestamp and cost
      ripTable.put(subnetNumber, new RIPTableEntry(0, 1));

      // when sending RIP requests, 
      // the destination IP address should be the IP address reserved for RIP (224.0.0.9) 
      // the destination Ethernet address should be the broadcast MAC address (FF:FF:FF:FF:FF:FF).
      // generateRIPv2Packet(Iface iface, int destIP, byte[] destMAC, boolean isRequest)
      Ethernet eth = generateRIPv2Packet(iface, RIP_BROADCAST_IP, RIP_BROADCAST_MAC, true);
      sendPacket(eth, iface);
    }
    System.out.println(routeTable); 
    
    // scheduler for broadcast that occurs every 10 seconds
    ScheduledExecutorService broadcastScheduler = Executors.newScheduledThreadPool(1);
    Runnable sendRIP = new Runnable() {
        public void run() {
            routerPointerThis.broadcastRIP();
        }
    };
    // your router should send an unsolicited RIP response out all of the router’s interfaces every 10 seconds 
    // your router should time out route table entries for which an update has not been received for more than 30 seconds. 
    // You should never remove route entries for the subnets that are directly reachable via the router’s interfaces .
    broadcastScheduler.scheduleAtFixedRate(sendRIP, 0, 10, SECONDS);
    ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);
    Runnable cleanup = new Runnable() {
      public void run() {
        try {
          boolean hasUpdates = false;
          // when multiple threads want to access ripTable, synchronized makes sure only one thread thread can do it at a moment
          synchronized (ripTable) {
          	// when multiple threads want to access routeTable, synchronized makes sure only one thread thread can do it at a moment
            synchronized (routeTable) {
              long currentTime = System.currentTimeMillis();
              List<RouteEntry> toRemove = new ArrayList<RouteEntry>();
              
              for (RouteEntry routeEntry : routeTable.getEntries()) {
                int subnetNumber = routeEntry.getDestinationAddress() & routeEntry.getMaskAddress();
                if (ripTable.containsKey(subnetNumber)) {
                  RIPTableEntry ripTableEntry = ripTable.get(subnetNumber);
                  if (ripTableEntry.timestamp != 0 && ripTableEntry.timestamp + 30000 < currentTime) {
                    // timeout
                    System.out.println("found outdated rip, subnetNumber: " + subnetNumber);
                    ripTable.remove(subnetNumber);
                    toRemove.add(routeEntry);
                    hasUpdates = true;
                  }
                }
              }
              
              for (RouteEntry routeEntry : toRemove) {
                routeTable.remove(routeEntry.getDestinationAddress(), routeEntry.getMaskAddress());
              }
            }
          }
          
          if (hasUpdates) {
            routerPointerThis.broadcastRIP();
          }
        } catch (Exception e) {
          System.out.println("Caught exception in cleanup: " + e.getClass().getName());
          e.printStackTrace();
        }
      }
    };
    cleanupScheduler.scheduleAtFixedRate(cleanup, 0, 1, SECONDS);
  }
  
  /**
   * 
   * 
   */
  private void broadcastRIP() {
      System.out.println("broadcasting rip");
      for (Iface iface : routerPointerThis.interfaces.values()) {
      	// generateRIPv2Packet(Iface iface, int destIP, byte[] destMAC, boolean isRequest)
      	// this RIPv2Packet is a not a request packet
        Ethernet eth = generateRIPv2Packet(iface, RIP_BROADCAST_IP, RIP_BROADCAST_MAC, false);
        sendPacket(eth, iface);
      }
  }
  
  /**
   * The RIPv2 and RIPv2Entry classes in the net.floodlightcontroller.packet package 
   * define the format for RIPv2 packets. 
   * 
   * When sending a RIP response for a specific RIP request, 
   * the destination IP address and destination Ethernet address 
   * should be the IP address and MAC address of the router interface that sent the request.
   * 
   */
  private Ethernet generateRIPv2Packet(Iface iface, int destIP, byte[] destMAC, boolean isRequest) {
      Ethernet eth = new Ethernet();
      eth.setEtherType(Ethernet.TYPE_IPv4);
      eth.setSourceMACAddress(iface.getMacAddress().toBytes());
      eth.setDestinationMACAddress(destMAC);
      
      // all RIPv2 packets should be encapsulated in UDP packets
      // whose source and destination ports are 520 (defined as a constant RIP_PORT in the UDP class) 
      UDP udp = new UDP();
      udp.setSourcePort(UDP.RIP_PORT);
      udp.setDestinationPort(UDP.RIP_PORT);

      IPv4 ip = new IPv4();
      ip.setSourceAddress(iface.getIpAddress());
      ip.setDestinationAddress(destIP);
      ip.setTtl((byte) 32);
      ip.setProtocol(IPv4.PROTOCOL_UDP);

      RIPv2 rip = new RIPv2();
      rip.setCommand(isRequest ? RIPv2.COMMAND_REQUEST : RIPv2.COMMAND_RESPONSE);

      if (!isRequest) {
        synchronized (routeTable) {
          for (RouteEntry entry : routeTable.getEntries()) {
            int entryIP = entry.getDestinationAddress();
            int mask = entry.getMaskAddress();
            RIPTableEntry ripTS = ripTable.get(entryIP & mask);
            RIPv2Entry ripEntry = new RIPv2Entry(entryIP, mask, ripTS.cost);
            ripEntry.setNextHopAddress(iface.getIpAddress());
            rip.addEntry(ripEntry);
          }
        }
      }

      udp.setPayload(rip);
      ip.setPayload(udp);
      eth.setPayload(ip);
      return eth;
  }
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile) {
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
	public void handlePacket(Ethernet etherPacket, Iface inIface) {
		System.out.println("*** -> Received packet: " + etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		switch(etherPacket.getEtherType()) {
			case Ethernet.TYPE_IPv4:
				this.handleIpPacket(etherPacket, inIface);
				break;
			// to accept ARP packets (i.e., the EtherType field in the Ethernet header equals Ethernet.TYPE_ARP)
			case Ethernet.TYPE_ARP:
        this.handleArpPacket(etherPacket, inIface);
        break;
		}
		
		/********************************************************************/
	}
	
	/**
	 * 
	 * @param etherPacket
	 * @param inIface
	 */
	private void handleArpPacket(Ethernet etherPacket, Iface inIface) {
    if (etherPacket.getEtherType() != Ethernet.TYPE_ARP) {
        return;
    }
    System.out.println("handle ARP packet");
    
    ARP arpPacket = (ARP) etherPacket.getPayload();
    // if an ARP packet is an ARP request (i.e., the opcode field in the ARP header equals ARP.OP_REQUEST), 
    // then your router may need to send an ARP reply. 
    if (arpPacket.getOpCode() == ARP.OP_REQUEST) {
      // save the sender ip-mac info in the arpCache
      int senderIP = IPv4.toIPv4Address(arpPacket.getSenderProtocolAddress());
      MACAddress senderMAC = MACAddress.valueOf(arpPacket.getSenderHardwareAddress());
      arpCache.insert(senderMAC, senderIP);

      int targetIp = ByteBuffer.wrap(arpPacket.getTargetProtocolAddress()).getInt();// target
      // your router must only respond to ARP requests whose target IP protocol address equals the IP address of the interface 
      // on which the ARP request was received.
      for (Iface iface : this.interfaces.values()) {
        if (targetIp == iface.getIpAddress()) {
          Ethernet ether = ARPReply(etherPacket, arpPacket, inIface);
          // Once the ARP reply is fully constructed, you should send it on the same interface on which the original packet arrived.
          sendPacket(ether, inIface);
          return;
        } 
      }
    } else if (arpPacket.getOpCode() == ARP.OP_REPLY) {
      // put in the info
      int senderIP = IPv4.toIPv4Address(arpPacket.getSenderProtocolAddress());
      MACAddress senderMAC = MACAddress.valueOf(arpPacket.getSenderHardwareAddress());
      arpCache.insert(senderMAC, senderIP);
      // remove all the packets in the corresponding queue
      LinkedList<Ethernet> pacs = PacWaitMAC.get(senderIP);
      if (!pacs.isEmpty()) {
        for (Ethernet pac : pacs) {
          pac.setDestinationMACAddress(senderMAC.toBytes());
          int destAddress = ((IPv4) pac.getPayload()).getDestinationAddress();
          Iface OutIface = routeTable.lookup(destAddress).getInterface();
          this.sendPacket(pac, OutIface);
        }
        PacWaitMAC.remove(senderIP);
      }
    } else {
      return;// return if it is not ARP request nor ARP reply
    }
	}
	
	/**
	 * 
	 * 
	 * @param etherPacket
	 * @param inIface
	 */
	private void handleIpPacket(Ethernet etherPacket, Iface inIface) {
		// check whether etherPacket contains IPv4 or not
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) { 
			System.out.println("drop: not IPv4: " + etherPacket.getEtherType());
			return; 
		}
		
		// if the frame contains an IPv4 packet, verify the checksum and time-to-live (TTL) of the 
    // IPv4 packet. Use the getPayload() method of the Ethernet class to get the IPv4 header.
		IPv4 ipPacket = (IPv4) etherPacket.getPayload();
    System.out.println("Handle IP packet");

    // check whether checksum is correct, this packet is corrupted if not correct
    // the IP checksum should only be computed over the IP header. 
    // the length of the IP header can be determined from the header length field in the IP header, 
    // the checksum field in the IP header should be zeroed before calculating the IP checksum. 
    // use code from the serialize() method in the IPv4 class to compute the checksum.
    short origCksum = ipPacket.getChecksum();
    ipPacket.resetChecksum();
    byte[] serialized = ipPacket.serialize();
    ipPacket.deserialize(serialized, 0, serialized.length);
    short calcCksum = ipPacket.getChecksum();
    if (origCksum != calcCksum) { 
    	System.out.println("drop: checksum failed");
    	return; 
    }
        
    // check TTL
    // Time-to-live (TTL) or hop limit is a mechanism that limits the lifespan or lifetime of data 
    // in a computer or network. TTL may be implemented as a counter or timestamp attached to or 
    // embedded in the data. Once the prescribed event count or timespan has elapsed, data is 
    // discarded or revalidated. 
    // in computer networking, TTL prevents a data packet from circulating indefinitely. 
    ipPacket.setTtl((byte)(ipPacket.getTtl() - 1));
    if (ipPacket.getTtl() == 0) { 
      this.sendICMPpacket(ipPacket, inIface, inIface, (byte) 11, (byte) 0);
    	System.out.println("drop: ttl 0");
    	return; 
    }
        
    // reset checksum now that TTL is decremented
    ipPacket.resetChecksum();
    
    // check if an arriving IP packet is RIP
    // if an arriving IP packet has a destination 224.0.0.9, 
    // a protocol type of UDP, and a UDP destination port of 520.
    // Packets that match this criteria are RIP requests or responses
    if (ipPacket.getProtocol() == IPv4.PROTOCOL_UDP && 
    		(((UDP) ipPacket.getPayload()).getDestinationPort() == UDP.RIP_PORT)) {
        handleRipPacket(etherPacket, inIface);
        return;
    }
        
    // check if packet is destined for one of router's interfaces
    // ICMP message should be sent if your router receives a TCP or UDP packet destined for one of its interfaces. 
    for (Iface iface : this.interfaces.values()) {
    	if (ipPacket.getDestinationAddress() == iface.getIpAddress()) {
    		// if a TCP or UDP header comes after the IP header (i.e., the protocol field in the IP header equals IPv4.PROTOCOL_UDP or IPv4.PROTOCOL_TCP) 
        // then you should construct and send destination port unreachable message. 
        // you should drop the original packet after sending the destination port unreachable message.
    		if (ipPacket.getProtocol() == IPv4.PROTOCOL_TCP || ipPacket.getProtocol() == IPv4.PROTOCOL_UDP) {
		        this.sendICMPpacket(ipPacket, inIface, inIface, (byte) 3, (byte) 3);
		        return;
		    // if an ICMP header comes after the IP header (i.e., the protocol field in the IP header equals IPv4.PROTOCOL_ICMP), 
        // then you should check if the ICMP message is an echo request (i.e., the type field in the ICMP header equals 8). 
        // if the ICMP message is an echo request (used by ping), 
        // then you should construct and send an echo reply message, otherwise, you should drop the packet.
		    } else if (ipPacket.getProtocol() == IPv4.PROTOCOL_ICMP) {
		        ICMP icmpHeader = (ICMP) ipPacket.getPayload();
		        if (icmpHeader.getIcmpType() == (byte) 8) {
		            Ethernet echoReplyPac = echoReply(etherPacket, ipPacket, icmpHeader, inIface);
		            sendPacket(echoReplyPac, inIface);
		            return;
		        } else {
		            return;
		        }
		    } else {
		        return;
		    }
    	}
    }
		
    // do route lookup and forward
    this.forwardIpPacket(etherPacket, inIface);
	}
	
	private void handleRipPacket(Ethernet etherPacket, Iface inIface) {
    System.out.println("Handle RIP Packet");
    IPv4 ip = (IPv4) etherPacket.getPayload();
    UDP udp = (UDP) ip.getPayload();
    RIPv2 rip = (RIPv2) udp.getPayload();
    // when sending a RIP response for a specific RIP request, 
    // the destination IP address and destination Ethernet address 
    // should be the IP address and MAC address of the router interface that sent the request
    if (rip.getCommand() == RIPv2.COMMAND_REQUEST) {
        sendPacket(generateRIPv2Packet(inIface, ip.getSourceAddress(), etherPacket.getSourceMACAddress(), false), inIface);
        return;
    }

    boolean hasUpdates = false;
    for (RIPv2Entry entry : rip.getEntries()) {
        int subnetNumber = entry.getAddress() & entry.getSubnetMask();
        int newCost = entry.getMetric() + 1;
        // check if need to update
        if (ripTable.containsKey(subnetNumber)) {
            if (newCost < ripTable.get(subnetNumber).cost) {
                // insert
                hasUpdates = true;
                // update ripTable
                ripTable.put(subnetNumber,
                        new RIPTableEntry(System.currentTimeMillis(), newCost));

                // update routeTable
                if (!routeTable.update(entry.getAddress(), entry.getSubnetMask(),
                        entry.getNextHopAddress(), inIface)) {
                    routeTable.insert(entry.getAddress(), entry.getNextHopAddress(),
                            entry.getSubnetMask(), inIface);
                }
            } else {
                // only update timestamp
                ripTable.get(subnetNumber).timestamp = System.currentTimeMillis();
            }
        } else {
            // insert
            hasUpdates = true;
            // update ripTable
            ripTable.put(subnetNumber, new RIPTableEntry(System.currentTimeMillis(), newCost));

            // update routeTable
            if (!routeTable.update(entry.getAddress(), entry.getSubnetMask(),
                    entry.getNextHopAddress(), inIface)) {
                routeTable.insert(entry.getAddress(), entry.getNextHopAddress(),
                        entry.getSubnetMask(), inIface);
            }
        }
    }

    if (hasUpdates) {
        broadcastRIP();
    }
	}	
	
	/**
	 * 
	 * @param etherPacket
	 * @param inIface
	 */
	private void forwardIpPacket(Ethernet etherPacket, Iface inIface) {
    // make sure it's an IP packet
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) { 
			return; 
		}
    System.out.println("Forward IP packet");
		
		// get IP header
		IPv4 ipPacket = (IPv4) etherPacket.getPayload();
    int dstAddr = ipPacket.getDestinationAddress();

    // find matching route table entry 
    RouteEntry bestMatch = this.routeTable.lookup(dstAddr);

    // if no entry matched, generate an ICMP destination net unreachable message prior to dropping the packet
    if (null == bestMatch) { 
    	this.sendICMPpacket(ipPacket, inIface, inIface, (byte) 3, (byte) 0);
    	return; 
    }

    // make sure we don't sent a packet back out the interface it came in
    Iface outIface = bestMatch.getInterface();
    if (outIface == inIface) { 
    	return; 
    }

    // set source MAC address in Ethernet header
    etherPacket.setSourceMACAddress(outIface.getMacAddress().toBytes());

    // if no gateway, then nextHop is IP destination
    int nextHop = bestMatch.getGatewayAddress();
    if (nextHop == 0) { 
    	nextHop = dstAddr; 
    }

    // all packet headers should be complete, except the destination MAC address in the Ethernet header, before enqueueing the packet.
    // your router should call the lookup() function of the ARPCache class when trying to forward packets,
    // and if no entry is found, generate a destination host unreachable ICMP message and drop the packet.
    ArpEntry arpEntry = this.arpCache.lookup(nextHop);
    // your router enqueues the packet and generates an ARP request if no matching entry is found in the ARP cache
    if (arpEntry == null) { 
    	// if the router wants to forward other IP packets to the same IP that your router is already trying to resolve, 
      // you should simply add those packets to the queue for the corresponding IP address.
    	if (PacWaitMAC.containsKey(nextHop)) {
        PacWaitMAC.get(nextHop).add(etherPacket);
      // router maintains a separate queue of packets for each IP address for which we are waiting for the corresponding MAC address
    	} else {
        LinkedList<Ethernet> packets = new LinkedList<Ethernet>();
        packets.add(etherPacket);
        PacWaitMAC.put(nextHop, packets);

        Iface OutIface = routeTable.lookup(nextHop).getInterface();
        sendARPrequests(etherPacket, inIface, OutIface, nextHop);
    	}
    	return; 
    }
    
    // set destination MAC address in Ethernet header
    etherPacket.setDestinationMACAddress(arpEntry.getMac().toBytes());
    
    this.sendPacket(etherPacket, outIface);
  }
	
	/**
	 * when the router generates ICMP messages, the packet must contains an Ethernet header, IP header, ICMP header, and ICMP payload. 
	 * You can construct these headers, by creating  Ethernet, IPv4, ICMP, and Data objects 
	 * by using the classes in the net.floodlightcontroller.packet package. 
	 * 
	 * To link the headers together, you should call the setPayload(..) method defined in the BasePacket class, 
	 * which is the superclass for all of the header classes. Below is a snippet of code to get you started:
	 * 
	 * @param ipPacket
	 * @param inIface
	 * @param outIface
	 * @param icmpType
	 * @param icmpCode
	 */
	private void sendICMPpacket(IPv4 ipPacket, Iface inIface, Iface outIface, byte icmpType, byte icmpCode) {
		// Ethernet ether = new Ethernet();
	  Ethernet ether = new Ethernet();
	  ether.setEtherType(Ethernet.TYPE_IPv4);
	  ether.setSourceMACAddress(inIface.getMacAddress().toBytes());
	  int nextHop = this.routeTable.lookup(ipPacket.getSourceAddress()).getGatewayAddress();
	  if (nextHop == 0) {
	  	nextHop = ipPacket.getSourceAddress();
	  }
	  ArpEntry arpEntry = this.arpCache.lookup(nextHop);
	  if (arpEntry == null) {
	  	return;
	  }  
	  ether.setDestinationMACAddress(arpEntry.getMac().toBytes());
	  
	  // IPv4 ip = new IPv4();
	  IPv4 ip = new IPv4();
	  ip.setTtl((byte) 64);
	  ip.setProtocol(IPv4.PROTOCOL_ICMP);
	  ip.setSourceAddress(inIface.getIpAddress());
	  ip.setDestinationAddress(ipPacket.getSourceAddress());
	  
	  // ICMP icmp = new ICMP();
	  ICMP icmp = new ICMP();
	  icmp.setIcmpType((byte) icmpType);
	  icmp.setIcmpCode((byte) icmpCode);
	  
	  // Data data = new Data();
	  Data data = new Data();
	  short headerLength = ipPacket.getHeaderLength();
	  short dataLength = (short) (headerLength * 4 + 12);
	  int padding = 0;
	  short addedLength = (short) (headerLength * 4 + 8);
	  byte[] serialized = ipPacket.serialize();
	  byte[] addedByte = Arrays.copyOfRange(serialized, 0, addedLength); // header+8
	  byte[] allData = new byte[dataLength]; // header+12
	  ByteBuffer bytebuffer = ByteBuffer.wrap(allData);
	  bytebuffer.putInt(padding);// first put 4 bytes padding
	  bytebuffer.put(addedByte);
	  data.setData(allData);
	  System.out.println(serialized);
	  System.out.println(addedByte);
	  System.out.println(allData);
	
	  // ether.setPayload(ip);
	  ether.setPayload(ip);
	  // ip.setPayload(icmp);
	  ip.setPayload(icmp);
	  // icmp.setPayload(data);
	  icmp.setPayload(data);
	
	  this.sendPacket(ether, outIface);
	}
	
	/**
	 * generate echo reply packet
	 * 
	 * ICMP message should be sent when your router receives an ICMP echo request destined for one of its interfaces. 
	 * you should not send an ICMP echo reply if you receive an echo request 
	 * whose destination IP address does not match any of the IP addresses assigned to the router’s interfaces; 
	 * these packets should be forwarded as they were before, since they are destined for hosts (or other routers).
	 * 
	 * the ICMP echo reply message should be constructed similar to the time exceeded message. 
	 * however, the source IP in the IP header should be set to the destination IP from the IP header in the echo request. 
	 * additionally, the type field in the ICMP header should be set to 0. 
	 * lastly, the payload that follows the ICMP header in the echo reply must contain the entire payload 
	 * that follows the ICMP header in the echo request.
	 * 
	 * 
	 * 
	 * @param etherPacket
	 * @param ipPacket
	 * @param icmpHeader
	 * @param inIface
	 * @return
	 */
  private Ethernet echoReply(Ethernet etherPacket, IPv4 ipPacket, ICMP icmpHeader, Iface inIface) {
      Ethernet ether = new Ethernet();
      ether.setEtherType(Ethernet.TYPE_IPv4);
      ether.setSourceMACAddress(inIface.getMacAddress().toBytes());
      int nextHop = this.routeTable.lookup(ipPacket.getSourceAddress()).getGatewayAddress();
      if (nextHop == 0) {
          nextHop = ipPacket.getSourceAddress();
      }
      ether.setDestinationMACAddress(this.arpCache.lookup(nextHop).getMac().toBytes());

      IPv4 ip = new IPv4();
      ip.setTtl((byte) 64);
      ip.setProtocol(IPv4.PROTOCOL_ICMP);
      ip.setSourceAddress(ipPacket.getDestinationAddress());
      ip.setDestinationAddress(ipPacket.getSourceAddress());

      ICMP icmp = new ICMP();
      icmp.setIcmpType((byte) 0);
      icmp.setIcmpCode((byte) 0);

      // IPacket icmpRequestPayload = icmpHeader.getPayload();
      Data icmpRequestPayload = (Data) icmpHeader.getPayload();

      ether.setPayload(ip);

      ip.setPayload(icmp);

      icmp.setPayload(icmpRequestPayload);

      return ether;
  }
  
  /**
   *  An ARP reply packet must contain an Ethernet header and an ARP header. 
   * 
   * @param etherPacket
   * @param arpPacket
   * @param inIface
   * @return
   */
  private Ethernet ARPReply(Ethernet etherPacket, ARP arpPacket, Iface inIface) {
  	// construct Ethernet header with the following specifics
  	// EtherType — set to Ethernet.TYPE_ARP
  	// Source MAC — set to the MAC address of the interface on which the original packet arrived
  	// Destination MAC — set to the source MAC address of the original packet
  	Ethernet ether = new Ethernet();
    ether.setEtherType(Ethernet.TYPE_ARP);
    ether.setSourceMACAddress(inIface.getMacAddress().toBytes());
    ether.setDestinationMACAddress(etherPacket.getSourceMACAddress());
    
    // construct ARP header with the following specifics
    // Hardware type — set to ARP.HW_TYPE_ETHERNET
    // Protocol type  — set to ARP.PROTO_TYPE_IP
    // Hardware address length — set to Ethernet.DATALAYER_ADDRESS_LENGTH
    // Protocol address length — set to 4
    // Opcode  — set to ARP.OP_REPLY
    // Sender hardware address — set to the MAC address of the interface on which the original packet arrived
    // Sender protocol address — set to the IP address of the interface on which the original packet arrived
    // Target hardware address — set to the sender hardware address from the original packet
    // Target protocol address — set to the sender protocol address from the original packet
    ARP arp = new ARP();
    arp.setHardwareType(ARP.HW_TYPE_ETHERNET);
    arp.setProtocolType(ARP.PROTO_TYPE_IP);
    arp.setHardwareAddressLength((byte) Ethernet.DATALAYER_ADDRESS_LENGTH);
    arp.setProtocolAddressLength((byte) 4);
    arp.setOpCode(ARP.OP_REPLY);
    arp.setSenderHardwareAddress(inIface.getMacAddress().toBytes());
    arp.setSenderProtocolAddress(inIface.getIpAddress());
    arp.setTargetHardwareAddress(arpPacket.getSenderHardwareAddress());
    arp.setTargetProtocolAddress(arpPacket.getSenderProtocolAddress());
    
    // link the ethernet header and arp header together using the setPayload() defined in the BasePacket class
    ether.setPayload(arp);

    return ether;
  }
  
  /**
   * 
   * 
   * 
   * 
   * 
   * @param etherPacket
   * @param inIface
   * @param nexthopIP
   * @return
   */
  private Ethernet ARPRequest(Ethernet etherPacket, Iface inIface, int nexthopIP) {
  	// an ARP request should be construct similar to an ARP reply, except for the following specifics
    // Destination MAC address — set to the broadcast MAC address FF:FF:FF:FF:FF:FF
    
    Ethernet ether = new Ethernet();
    ether.setEtherType(Ethernet.TYPE_ARP);
    ether.setSourceMACAddress(inIface.getMacAddress().toBytes());
    // (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
    ether.setDestinationMACAddress("FF:FF:FF:FF:FF:FF");
    
    // construct ARP header with the following specifics
    // Opcode  — set to ARP.OP_REQUEST
    // Target hardware address — set to 0
    // Target protocol address — set to the IP address whose associated MAC address we want
    ARP arp = new ARP();
    arp.setHardwareType(ARP.HW_TYPE_ETHERNET);
    arp.setProtocolType(ARP.PROTO_TYPE_IP);
    arp.setHardwareAddressLength((byte) Ethernet.DATALAYER_ADDRESS_LENGTH);
    arp.setProtocolAddressLength((byte) 4);
    arp.setOpCode(ARP.OP_REQUEST);
    arp.setSenderHardwareAddress(inIface.getMacAddress().toBytes());
    arp.setSenderProtocolAddress(inIface.getIpAddress());
    arp.setTargetHardwareAddress(new byte[6]); // {0,0,0,0,0,0} new byte[6]
    arp.setTargetProtocolAddress(nexthopIP);

    ether.setPayload(arp);

    return ether;
  }
  
  /**
   * 
   * 
   * 
   * 
   *  
   * 
   * 
   * @param etherPacket
   * @param inIface
   * @param outIface
   * @param nextHop
   */
  private void sendARPrequests(final Ethernet etherPacket, final Iface inIface, final Iface outIface, final int nextHop) {
	  final Ethernet pacToSend = ARPRequest(etherPacket, inIface, nextHop);
	  final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	  
	  final Runnable ARPrequest = new Runnable() {
      int sendTimes = 0;
	
	    public void run() {
        ArpEntry arpEntry = routerPointerThis.arpCache.lookup(nextHop);
        // you should continue to send ARP requests every second until 
        // your router either receives a corresponding ARP reply or you have sent the same ARP request 3 times. 
        if (arpEntry == null) {
        	// if you have sent an ARP request 3 times and no corresponding ARP reply has been received 1 second 
        	// after sending the third request, then you should drop any packets that were queued waiting for a reply,
        	// however, before dropping each packet, you should generate a destination host unreachable message.
          if (sendTimes == 3) {
            for (Ethernet pac : routerPointerThis.PacWaitMAC.get(nextHop)) {
              IPv4 ipPacket = (IPv4) etherPacket.getPayload();
              sendICMPpacket(ipPacket, inIface, inIface, (byte) 3, (byte) 1);
            }
            scheduler.shutdown();
          } else {
          	// if a corresponding ARP reply does not arrive within 1 second of issuing the ARP request, 
            // then you should send another ARP request that’s exactly the same as the original ARP request. 
            routerPointerThis.sendPacket(pacToSend, outIface);
            sendTimes = sendTimes + 1;
          }
        } else {
          // send away all the packets for the ip
          LinkedList<Ethernet> pacs = routerPointerThis.PacWaitMAC.get(nextHop);
          Iterator<Ethernet> itr = pacs.iterator();
          while (itr.hasNext()) {
            Ethernet ether = (Ethernet) itr.next();
            ether.setDestinationMACAddress(arpEntry.getMac().toBytes());
            routerPointerThis.sendPacket(ether, outIface);
          }
          scheduler.shutdown();
        }
      }
	  };
	  scheduler.scheduleAtFixedRate(ARPrequest, 0, 1, SECONDS);
  }
  
  class RIPTableEntry {
    long timestamp;
    int cost;

    public RIPTableEntry(long timestamp, int cost) {
        this.timestamp = timestamp;
        this.cost = cost;
    }
}
}