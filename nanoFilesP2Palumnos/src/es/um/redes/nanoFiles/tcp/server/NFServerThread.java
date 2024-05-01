package es.um.redes.nanoFiles.tcp.server;

import java.net.Socket;



public class NFServerThread extends Thread {
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
