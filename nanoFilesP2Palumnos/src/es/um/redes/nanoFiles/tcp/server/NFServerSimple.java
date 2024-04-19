package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;

	public NFServerSimple() throws IOException {
		
		InetSocketAddress servAd = new InetSocketAddress(NFServerSimple.PORT);
		try {
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
																																		/*
																																		 *	Comprobar que el socket servidor está creado y ligado
																																		 */
		if(this.serverSocket==null || this.serverSocket.isClosed()) {
			System.err.println("null or closed socket");
			System.exit(-1);
		}else {
			System.out.println("[socket] ok");
		}
																																		/*
																																		 * Usar el socket servidor para esperar conexiones de otros peers que
																																		 * soliciten descargar ficheros
																																		 */
		while(true){
			try {
				System.out.println("[waiting]");
				Socket socket = this.serverSocket.accept();
				System.out.println("New client connected: " + socket.getInetAddress().toString() + ":" + socket.getPort());
				NFServerComm.serveFilesToClient(socket);
				
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
																																		/*
																																		 * Al establecerse la conexión con un peer, la comunicación con dicho
																																		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
																																		 * hay que pasarle el socket devuelto por accept
																																		 */



		//TODO System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}
}
