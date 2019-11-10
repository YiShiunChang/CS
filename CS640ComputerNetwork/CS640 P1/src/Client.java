import java.net.*;
import java.text.DecimalFormat;
import java.io.*;

/**
 * When running as a client, Iperfer must establish a TCP connection with the server and send data 
 * as quickly as possible for time seconds. Data should be sent in chunks of 1000 bytes and the 
 * data should be all zeros. Keep a running total of the number of bytes sent. After time seconds 
 * have passed, Iperfer must stop sending data and close the connection. Iperfer must print a one 
 * line summary that includes:
 * 
 * 1. The total number of bytes sent (in kilobytes)
 * 2. The rate at which traffic could be sent (in megabits per second (Mbps))
 * For example: sent=6543 KB rate=5.234 Mbps
 * 
 * You should assume 1 kilobyte (KB) = 1000 bytes (B) and 1 megabyte (MB) = 1000 KB. 
 * As always, 1 byte (B) = 8 bits (b).
 */
public class Client {
	public Client(String hostName, int portNumber, int time) {
		try {
			// build a client socket, the class for client socket is called Socket
			// when the class for server socket is called ServerSocket
			
			// if the serverSocket with hostName and portNumber exists, then clientSocket can be
			// connected to, or said accepted by, the serverSocket once we new it
			Socket clientSocket = new Socket(hostName, portNumber);
			System.out.println("Connect successfully on " + hostName);
			// for data transmission, PrintWriter helps us write data into OutputStream, so we
			// use out to send data to serverSocket
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			
			long timeMS = (long) time * 1000;
			long startTime = System.currentTimeMillis();
			long endTime = 0;
			long bytes = 0;
			
			char[] chunk = new char[1000];
			while (true) {
				// write data to OutputStream
				out.print(chunk);
				bytes += 1000;
				if (System.currentTimeMillis() - startTime >= timeMS) {
					endTime = System.currentTimeMillis();
					break;
				}
			}
			
			long durationMS = endTime - startTime;
			out.close();
			clientSocket.close();
			
			double kbSent = bytes / (double) 1000;
			// 1 megabit = 10**6 bits, and 1 second = 1000 millisecond
			double sentRate = kbSent * 8 / durationMS;
			DecimalFormat ft = new DecimalFormat("#.###");
			System.out.println("sent = " + kbSent + " KB " + ", rate = " + ft.format(sentRate) + " Mbps");
			
		} catch (UnknownHostException e) {
      System.err.println("Don't know about host " + hostName);
      System.exit(1);
		} catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to " + hostName);
      System.exit(1);
		}
	}
}
