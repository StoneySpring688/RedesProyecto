package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;

	public NFServerSimple() throws IOException {
		
		
		try {
			InetSocketAddress servAd = new InetSocketAddress(NFServerSimple.PORT);
			this.serverSocket = new ServerSocket();
			this.serverSocket.bind(servAd);
		}catch(BindException e){
			InetSocketAddress servAd = new InetSocketAddress(0);
			this.serverSocket = new ServerSocket();
			this.serverSocket.bind(servAd);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * 
	 */
	public void run() {
																																	
		if(this.serverSocket==null || this.serverSocket.isClosed()) {
			System.err.println("null or closed socket");
			System.exit(-1);
		}else {
			System.out.println("[socket] ok");
		}
																																
		while(true){
			try {
				System.out.println("[waiting]");
				Socket socket = this.serverSocket.accept();
				System.out.println("New client connected: " + socket.getInetAddress().toString() + ":" + socket.getPort());
				NFServerComm nfsc = new NFServerComm();
				nfsc.serveFilesToClient(socket);
				
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}																																	

		//TODO System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}
	
	public int getListeningPort() {
		int puerto = this.serverSocket.getLocalPort();
	    return puerto;
	}
	
}
