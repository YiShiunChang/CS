
/**
 * Iperfer checks the arguments then builds a server socket or client socket in a computer, or 
 * returns "Error: invalid arguments".
 * 
 * For server arguments:
 * java Iperfer -c -h <server hostname> -p <server port> -t <time> 
 * 
 * For client arguments:
 * java Iperfer -s -p <listen port>
 * 
 * Once a server socket and a client socket is built on computers, data can be transported through
 * the connection between server and client
 */
public class Iperfer {
	/**
	 * This function checks whether the arguments is valid or not, then builds server or client if it
	 * is valid
	 * 
	 * @param args for building a server socket or a client socket
	 */
	public static void main(String[] args) {
		if (args.length == 7 && args[0].equals("-c") && args[1].equals("-h") && 
				args[3].equals("-p") && args[5].equals("-t")) {
			// java Iperfer -c -h <server hostname> -p <server port> -t <time>
			String host = args[2];
			int port = 1024; // default port number
			int time = 0; // default time

			try {
				port = Integer.valueOf(args[4]);
			} catch(Exception e) {
				System.out.println("Caught exception when parsing port number: " + args[4]);
				System.exit(1);
			}

			try {
				time = Integer.valueOf(args[6]);
			} catch(Exception e) {
				System.out.println("Caught exception when parsing time: " + args[6]);
				System.exit(1);
			}
			
			// If the server port argument is less than 1024 or greater than 65535, you should print the 
			// following and exit: Error: port number must be in the range 1024 to 65535
			if (port < 1024 || port > 65535) {
				System.err.println("Error: port number must be in the range 1024 to 65535");
				System.exit(1);
			}

			System.out.println("Using client mode");
			new Client(host, port, time);
		} else if (args.length == 3 && args[0].equals("-s") && args[1].equals("-p")) {
			// java Iperfer -s -p <listen port>
			int port = 1024; // default port number
			
			try {
				port = Integer.valueOf(args[2]);
			} catch(Exception e) {
				System.out.println("Caught exception when parsing port number: " + args[2]);
				System.exit(1);
			}
			
			// If the listen port argument is less than 1024 or greater than 65535, you should print the 
			// following and exit: Error: port number must be in the range 1024 to 65535
			if (port < 1024 || port > 65535) {
				System.err.println("Error: port number must be in the range 1024 to 65535");
				System.exit(1);
			}

			System.out.println("Using server mode");
			new Server(port);
		} else {
			// If any arguments are missing or additional arguments are given or arguments are given in 
			// wrong order, you should print the following and exit: Error: invalid arguments
			System.out.println("Error: invalid arguments");
			System.exit(1);
		}
	}
}
