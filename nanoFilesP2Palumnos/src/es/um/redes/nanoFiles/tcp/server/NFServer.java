package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {

	private ServerSocket serverSocket = null;
	private boolean stopServer = false;
	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;

	public NFServer() throws IOException {
		InetSocketAddress servAd = new InetSocketAddress(0);
		try {
			this.serverSocket = new ServerSocket();
			this.serverSocket.bind(servAd);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		if(this.serverSocket==null || this.serverSocket.isClosed()) {
			System.err.println("null or closed socket");
			System.exit(-1);
		}else {
			System.out.println("[socket] ok");
		}
		
																																	
		try {
			this.serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		System.out.println("[waiting]");
		while(!this.stopServer){
			try {
				Socket socket = this.serverSocket.accept();
				NFServerThread servThread = new NFServerThread(socket);
				servThread.start();
			}catch(SocketTimeoutException e) {
			}catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
																																	
	}
	
	public void runServer() {
		Thread hiloServer = new Thread(this);
		System.out.println("[Server] run");
		hiloServer.start();
	}
	
	public void stopServer() {
		this.stopServer = true;
	}
	
	public int getListeningPort() {
		int puerto = this.serverSocket.getLocalPort();
	    return puerto;
	}

}
