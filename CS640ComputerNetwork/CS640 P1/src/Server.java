import java.net.*;
import java.text.DecimalFormat;
import java.io.*;

/**
 * When running as a server, Iperfer must listen for TCP connections from a client and receive data
 * as quickly as possible until the client closes the connection. Data should be read in chunks of 
 * 1000 bytes. Keep a running total of the number of bytes received. After the client has closed 
 * the connection, Iperfer must print a one line summary that includes:
 * 
 * 1. The total number of bytes received (in kilobytes)
 * 2. The rate at which traffic could be read (in megabits per second (Mbps))
 * For example: received=6543 KB rate=4.758 Mbps
 * 
 * The Iperfer server should shut down after it handles one connection from a client.
 */
public class Server {
	public Server(int portNumber) {
		try {
			// we build serverSocket before clientSocket, so clientSocket can be accepted by serverSocket
			// once clientSocket is built
			ServerSocket serverSocket = new ServerSocket(portNumber);
			System.out.println("Waiting for client connection on port: " + portNumber);
			Socket clientSocket = serverSocket.accept();
			System.out.println("Connected IP: " + clientSocket.getRemoteSocketAddress());
			
			long startTime = System.currentTimeMillis();
			
			// public InputStream getInputStream() throws IOException
			// an InputStreamReader is a bridge from byte streams to character streams
			// for data transmission, InputStreamReader helps us read data form InputStream, so we
			// use in to read data from clientSocket
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			char[] buff = new char[1000];
			int length = 0;
			long bytes = 0;
			while ((length = in.read(buff, 0, 1000)) != -1) {
				bytes += length;
      }
			
			long timeMS = System.currentTimeMillis() - startTime;
			in.close();
			clientSocket.close();
			serverSocket.close();
			
			double kbReceive = bytes / (double) 1000;
			// 1 megabit = 10**6 bits, and 1 second = 1000 millisecond
			double rate = kbReceive * 8 / timeMS;
			DecimalFormat ft = new DecimalFormat("#.###");
			System.out.println("received = " + kbReceive + " KB" + ", rate = " + ft.format(rate) + " Mbps");
       
    } catch (IOException e) {
    		System.out.println("Exception caught when trying to listen on port " + 
    				portNumber + " or listening for a connection");
	      System.out.println(e.getMessage());
	  }
	}
}
