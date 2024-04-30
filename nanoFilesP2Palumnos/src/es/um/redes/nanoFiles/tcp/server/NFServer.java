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
		
		/*
		 * Crear un socket servidor y ligarlo a cualquier puerto disponible
		 */
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		//comprobamos que el servidor esté listo
		/*
		 * Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */

		/*
		 * Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */
		/*
		 * (Opcional) Crear un hilo nuevo de la clase NFServerThread, que llevará
		 * a cabo la comunicación con el cliente que se acaba de conectar, mientras este
		 * hilo vuelve a quedar a la escucha de conexiones de nuevos clientes (para
		 * soportar múltiples clientes). Si este hilo es el que se encarga de atender al
		 * cliente conectado, no podremos tener más de un cliente conectado a este
		 * servidor.
		 */
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
				//if(this.stopServer) System.out.println("[server] stop signal detected");
			}catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		//System.out.println("[Server] stopped");
																																	
	}
	
	public void runServer() {
		Thread hiloServer = new Thread(this);
		System.out.println("[Server] run");
		hiloServer.start();
		/*try {
			//NFServer RunnableServer = new NFServer();
			Thread hiloServer = new Thread(this);
			System.out.println("[Server] run");
			hiloServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public void stopServer() {
		//System.out.println("stopping server");
		this.stopServer = true;
	}
	
	public int getListeningPort() {
		int puerto = this.serverSocket.getLocalPort();
	    //System.out.println("Servidor escuchando en el puerto " + puerto);
	    return puerto;
	}
	
																																		/**
																																		 * Añadir métodos a esta clase para: 1) Arrancar el servidor en un hilo
																																		 * nuevo que se ejecutará en segundo plano 2) Detener el servidor (stopserver)
																																		 * 3) Obtener el puerto de escucha del servidor etc.
																																		 */




}
