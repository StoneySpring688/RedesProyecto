package es.um.redes.nanoFiles.logic;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.client.NFConnectorThread;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.tcp.server.NFServerSimple;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.util.FileDigest;





public class NFControllerLogicP2P {
																										/*
																										 * Para bgserve, se necesita un atributo NFServer que actuará como
																										 * servidor de ficheros en segundo plano de este peer
																										 */
	NFServer server = null;
	boolean isServer = false;
	NFServerSimple fgserv = null;
	boolean fgstatus = false;																			// para poder dar de alta el servidor de ficheros fg
																										// hace falta saber si está funcionando correctamente



	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para arrancar un servidor de ficheros en primer plano.
	 * 
	 */
	protected void foregroundServeFiles(NFControllerLogicDir nfld, NFControllerLogicP2P  nflp) {
																												/*
																												 * Crear objeto servidor NFServerSimple y ejecutarlo en primer plano.
		
																												 */
		if(this.server != null && this.isServer) {
			System.err.println("[fgserve] An error ocurred, a server is already running");
		}
		try {
			this.fgserv = new NFServerSimple();
			int port =this.fgserv.getListeningPort();
			if(port<=0) {
				System.err.println("[fgserve] An error ocurred, invalid port");
				System.exit(-1);
			}else {
				System.out.println("[fgserve] ok " + port);
				this.fgstatus = true;
			}
			ControllerThread ct = new ControllerThread(nfld, nflp);
			ct.start();
			this.fgserv.run();
			if (getFgStatus()) {
				stopForegroundServer();
				nfld.unregisterFileServer();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("[fgserve] An error occurred, failed to communicate with directory");
			System.exit(-1);
		}
																												/*
																												 * Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
																												 * este método. Si se produce una excepción de entrada/salida (error del que no
																												 * es posible recuperarse), se debe informar sin abortar el programa
																												 */



	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean backgroundServeFiles() {
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en
		 * cuyo caso el servidor ya está en marcha. Si no lo está, crear objeto servidor
		 * NFServer y arrancarlo en segundo plano creando un nuevo hilo. Finalmente,
		 * comprobar que el servidor está escuchando en un puerto válido (>0) e imprimir
		 * mensaje informando sobre el puerto, y devolver verdadero.
		 */
		if(this.server != null && this.isServer) {
			System.err.println("[bgserve] An error ocurred, a server is already running");
		}else {
			try {
				this.server = new NFServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.server.runServer();
			int port =this.server.getListeningPort();
			if(port<=0) {
				System.err.println("[bgserve] An error ocurred, invalid port");
				System.exit(-1);
			}else {
				System.out.println("[bgserve] ok " + port);
				this.isServer = true;
				return true;
			}
		}
		/*
		 * Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */



		return false;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param fserverAddr    La dirección del servidor al que se conectará
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 */
	protected boolean downloadFileFromSingleServer(InetSocketAddress fserverAddr, String targetFileHash,String localFileName) {
		boolean result = false;
		if (fserverAddr == null) {
			System.err.println("* Cannot start download - No server address provided");
			return false;
		}
																													/*
																													 * Crear un objeto NFConnector para establecer la conexión con el peer
																													 * servidor de ficheros, y usarlo para descargar el fichero mediante su método
																													 * "downloadFile". Se debe comprobar previamente si ya existe un fichero con el
																													 * mismo nombre en esta máquina, en cuyo caso se informa y no se realiza la
																													 * descarga. Si todo va bien, imprimir mensaje informando de que se ha
																													 * completado la descarga.
																													 */
		try {
			NFConnector connector = new NFConnector(fserverAddr);
			File f = new File(localFileName);
			if(!f.exists() || f.length()<=0) {
				f.createNewFile();
				result =connector.downloadFile(targetFileHash, f);
			}else {
				System.err.println("[downl]the file already exist\ndownload cancelled");
			}
			if(result) {
				System.out.println("[downl] download succeed");
			}
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
																														/*
																														 * Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
																														 * este método. Si se produce una excepción de entrada/salida (error del que no
																														 * es posible recuperarse), se debe informar sin abortar el programa
																														 */



		return result;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList La lista de direcciones de los servidores a los que
	 *                          se conectará
	 * @param targetFileHash    Hash completo del fichero a descargar
	 * @param localFileName     Nombre con el que se guardará el fichero descargado
	 */
	public boolean downloadFileFromMultipleServers(DirMessage MsgServerAddressList, String targetFileHash, String localFileName) {
		boolean downloaded = false;
		if(MsgServerAddressList != null) {
			long tam = MsgServerAddressList.getFichSize()[0];
			int np = MsgServerAddressList.getNFichs();
			String hash = MsgServerAddressList.getNickname();
			int[] ports = MsgServerAddressList.getNPeers();
			String[] ips = MsgServerAddressList.getFichName();
			InetSocketAddress[] addresses = new InetSocketAddress[np];
			long segment = tam/np;
			long bytesRest = tam%np;
			long init = 0;
			long fin = 0;
			NFConnectorThread[] threads = new NFConnectorThread[np];
			
			if (ips == null) {
				System.err.println("* Cannot start download - No list of server addresses provided");
				return false;
			}else {
				for(int i=0; i<np; i++) {
					try {
						addresses[i] = new InetSocketAddress(InetAddress.getByName(ips[i]),ports[i]);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					//System.out.println("dirección añadida : " + addresses[i]);
					fin = init + segment - 1;
					if(i < bytesRest) {
						fin++;
					}
					if(i == 0) {
						threads[i] = new NFConnectorThread(hash, init, fin-init+1, i, addresses[i]);
					}else {
						threads[i] = new NFConnectorThread(hash, init, fin-init+1, i, addresses[i]);
					}
					//System.out.println("Para el servidor " + i + ", el rango de bytes a leer es: " + init + " - " + fin);
					init = fin + 1;
				}
			}
			
			File f = new File(localFileName);
			if(!f.exists() || f.length()<=0) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else {
				System.err.println("[downl]the file already exist\ndownload cancelled");
				return false;
			}
			NFConnectorThread.npeers = np;
			NFConnectorThread.f = f;
			for(int i=0;i<np;i++) {
				threads[i].start();
			}
			
			for (int i = 0; i < np; i++) {
			    try {
			        threads[i].join();
			    } catch (InterruptedException e) {
			        System.err.println(e.getMessage());
			        e.printStackTrace();
			    }
			}
			
			
			String hash1 = FileDigest.computeFileChecksumString(f.getName());
			if(hash1.equals(hash)) {
				System.out.println("files are identical");
				downloaded = true;
			}else {
				System.err.println("files are not identical");
				System.out.println(hash1);
				System.out.println(hash);
			}
		}

		
		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros en
	 * segundo plano
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	public int getServerPort() {
		int port = 0;
		if(this.server!=null) {
			port=this.server.getListeningPort();
		}
		/*
		 * Devolver el puerto de escucha de nuestro servidor de ficheros en
		 * segundo plano
		 */



		return port;
	}
	
	//metodo para obtener el  puerto de escucha del fgserver
	
	public int getFgServerPort() {
		int port = 0;
		if(this.fgserv != null) {
			port = this.fgserv.getListeningPort();
		}
		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	public void stopBackgroundFileServer() {
		if(this.server!=null) {
			this.server.stopServer();
			this.isServer = false;
			System.out.println("[bgserve] stopping server");
		}

	}
	
	public void stopForegroundServer() {
		if(getFgStatus()) {
			this.fgstatus = false;
		}
	}
	
	protected boolean getFgStatus() {
		return this.fgstatus;
	}

}
