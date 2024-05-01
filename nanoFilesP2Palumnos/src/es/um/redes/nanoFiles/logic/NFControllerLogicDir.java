package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageField;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {

	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Método para comprobar que la comunicación con el directorio es exitosa (se
	 * pueden enviar y recibir datagramas) haciendo uso de la clase
	 * DirectoryConnector
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected void testCommunicationWithDirectory(String directoryHostname) throws IOException {
		assert (NanoFiles.testMode);
		System.out.println("[testMode] Testing communication with directory...");
		/*
		 * Crea un objeto DirectoryConnector a partir del parámetro directoryHostname y
		 * lo utiliza para hacer una prueba de comunicación con el directorio.
		 */
		DirectoryConnector directoryConnector = new DirectoryConnector(directoryHostname);
		if (directoryConnector.testSendAndReceive()) {
			System.out.println("[testMode] Test PASSED!");
		} else {
			System.err.println("[testMode] Test FAILED!");
		}
	}

																												/**
																												 * Método para conectar con el directorio y obtener la "sessionKey" que se
																												 * deberá utilizar en lo sucesivo para identificar a este cliente ante el
																												 * directorio
																												 * 
																												 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
																												 *                          directorio
																												 * @return true si se ha conseguido contactar con el directorio.
																												 * @throws IOException
																												 */
	protected boolean doLogin(String directoryHostname, String nickname) {

																												
		boolean result = false;
		try {
			this.directoryConnector = new DirectoryConnector(directoryHostname);
		} catch (IOException e) {
			System.err.println("I/O error : "+ e);
			System.exit(1); // no es posible recuperarse de un error I/O, se informa del error y se termina la ejecución
		}
		result=this.directoryConnector.logIntoDirectory(nickname);
		if(!result) {
			System.err.println("[error] invalid name");
		}else if(result) {
			System.out.println("[Logged] SessionKey : " + directoryConnector.getSessionKey() );
		}


		return result;
	}

																												/**
																												 * Método para desconectarse del directorio: cerrar sesión y dar de baja el
																												 * nombre de usuario registrado
																												 */
	public boolean doLogout() {
																												
		boolean result = false;
		result = this.directoryConnector.logoutFromDirectory();
		if(!result) {
			System.err.println("Could not logout the directory");
		}else if(result) {
			System.out.println("[logout success]");
		}

		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de nicks registrados en el directorio
	 */
	protected boolean getAndPrintUserList() {
																												
		boolean result = false;
		String[] list = this.directoryConnector.getUserList();
		try {
			if(list.length != 0) result = true;
		} catch (NullPointerException e) {
			System.err.println("[Failed Execution]");
			System.exit(-1);
		}
		
		for(int i = 0; i < list.length; i++) {
			System.out.println(DirMessageField.FIELDNAME_USER + " : "+ list[i]);
			i++;
			System.out.println(DirMessageField.FIELDNAME_ISSERVER + " : "+ list[i]);
			;
		} 
		
		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	protected boolean getAndPrintFileList() {
		
		boolean result = false;
		FileInfo[] fichs = this.directoryConnector.getFileList();
		
		if(fichs != null) {
			FileInfo.printToSysoutPlus(fichs);
			result = true;
		}
		
		


		return result;
	}

	/**
	 * Método para registrarse en el directorio como servidor de ficheros en un
	 * puerto determinado
	 * 
	 * @param serverPort el puerto en el que está escuchando nuestro servidor de
	 *                   ficheros
	 */

	public boolean registerFileServer(int serverPort) {
																								
		boolean result = false;
		result = this.directoryConnector.registerServerPort(serverPort);


		return result;
	}

	/**
	 * Método para enviar al directorio la lista de ficheros que este peer servidor
	 * comparte con el resto (ver método filelist).
	 * 
	 */
	protected boolean publishLocalFiles() {
		
		boolean result = false;
		result = this.directoryConnector.publishLocalFiles(NanoFiles.db.getFiles());



		return result;
	}

	/**
	 * Método para consultar al directorio el nick de un peer servidor y obtener
	 * como respuesta la dirección de socket IP:puerto asociada a dicho servidor
	 * 
	 * @param nickname el nick del servidor por cuya IP:puerto se pregunta
	 * @return La dirección de socket del servidor identificado por dich nick, o
	 *         null si no se encuentra ningún usuario con ese nick que esté
	 *         sirviendo ficheros.
	 */
	private InetSocketAddress lookupServerAddrByUsername(String nickname) {
		InetSocketAddress serverAddr = null;
		serverAddr = this.directoryConnector.lookupServerAddrByUsername(nickname);
		
		

		return serverAddr;
	}

	/**
	 * Método para obtener la dirección de socket asociada a un servidor a partir de
	 * una cadena de caracteres que contenga: i) el nick del servidor, o ii)
	 * directamente una IP:puerto.
	 * 
	 * @param serverNicknameOrSocketAddr El nick o IP:puerto del servidor por el que
	 *                                   preguntamos
	 * @return La dirección de socket del peer identificado por dicho nick, o null
	 *         si no se encuentra ningún peer con ese nick.
	 */
	public InetSocketAddress getServerAddress(String serverNicknameOrSocketAddr) {
		InetSocketAddress fserverAddr = null;
		if (serverNicknameOrSocketAddr.contains(":")) { // Then it has to be a socket address (IP:port)
			String[] partes = serverNicknameOrSocketAddr.split(":");
			String ip = partes[0];
			int port = Integer.parseInt(partes[1]);
			try {
				fserverAddr = new InetSocketAddress(InetAddress.getByName(ip),port);
			} catch (UnknownHostException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
																											
		} else {
																											
			fserverAddr = lookupServerAddrByUsername(serverNicknameOrSocketAddr);
		}
		return fserverAddr;
	}

	/**
	 * Método para consultar al directorio los nicknames de los servidores que
	 * tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 */
	public boolean getAndPrintServersNicknamesSharingThisFile(String fileHashSubstring) {
		
		boolean result = false;
		String[] names = this.directoryConnector.getServerNicknamesSharingThisFile(fileHashSubstring);
		if(names != null) {
			for(String n :names) {
				System.out.println("peer : " + n);
			}
		}

		return result;
	}

	/**
	 * Método para consultar al directorio las direcciones de socket de los
	 * servidores que tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 * @return El mensaje con toda la infrormación necesaria, la cual tratará el
	 * 		   contrladorP2P
	 * 
	 */
	public DirMessage getServerAddressesSharingThisFile(String downloadTargetFileHash) { 
		// se  podría hacer con search y lookupServerAdr, pero he modelado un mensaje para hacerlo todo de golpe
		// como son varios tipos de datos, se pasará el mensaje para obtenerlos donde sea necesario
		DirMessage resp = null;
		resp = this.directoryConnector.downloadAskInfo2Dir(downloadTargetFileHash);
		if(resp != null && resp.getOperation().matches(DirMessageOps.OPERATION_DOWNLOADASKINFOOK)) {
			System.out.println("[server] Info ok");
		}else {
			System.err.println("[warning] something went wrong");
		}
		return resp;
	}

	/**
	 * Método para dar de baja a nuestro servidor de ficheros en el directorio.
	 * 
	 * @return Éxito o fracaso de la operación
	 */
	public boolean unregisterFileServer() {
																															
		boolean result = false;
		FileInfo[] files = NanoFiles.db.getFiles();
		String[] hashes = new String[files.length];
		for(int i=0;i<files.length;i++) {
			hashes[i] = files[i].fileHash;
		}
		result = this.directoryConnector.unregisterFilesAndServer(hashes);
		if(result) {
			System.out.println("[stopserver] ok");
		}


		return result;
	}

	protected InetSocketAddress getDirectoryAddress() {
		return directoryConnector.getDirectoryAddress();
	}
	/**
	@return devuelve true si se poseé un session key, y false en caso contrario
	**/
	protected boolean test() {
		try {
			//System.out.println(this.directoryConnector.getSessionKey());
			if(this.directoryConnector.getSessionKey() == -1) {
				return false;
			}
			return true;
		} catch (NullPointerException e) {
			return false;
		}
		
	}

}
