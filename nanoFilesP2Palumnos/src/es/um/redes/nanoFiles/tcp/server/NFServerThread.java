package es.um.redes.nanoFiles.tcp.server;

import java.net.Socket;



public class NFServerThread extends Thread {
														/*
														 * TODO: Esta clase modela los hilos que son creados desde NFServer y cada uno
														 * de los cuales simplemente se encarga de invocar a
														 * NFServerComm.serveFilesToClient con el socket retornado por el método accept
														 * (un socket distinto para "conversar" con un cliente)
														 */
	private Socket socket;
	
	public NFServerThread(Socket s) {
		this.socket = s;
	}

	
	public void run() {
		System.out.println("New client connected: " + socket.getInetAddress().toString() + ":" + socket.getPort());
		NFServerComm nfsc = new NFServerComm();
		nfsc.serveFilesToClient(socket);
	}

}
