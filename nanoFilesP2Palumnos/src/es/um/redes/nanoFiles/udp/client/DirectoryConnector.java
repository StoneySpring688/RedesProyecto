package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

																		/**
																		 * Cliente con métodos de consulta y actualización específicos del directorio
																		 */
public class DirectoryConnector {
																		/**
																		 * Puerto en el que atienden los servidores de directorio
																		 */
	private static final int DIRECTORY_PORT = 6868;
																		/**
																		 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
																		 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
																		 * recuperar el control
																		 */
	private static final int TIMEOUT = 1000; //999999999;
																		/**
																		 * Número de intentos máximos para obtener del directorio una respuesta a una
																		 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
																		 * cuenta como un intento.
																		 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

																		/**
																		 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
																		 * loguearse
																		 */
	public static final int INVALID_SESSION_KEY = -1;

																		/**
																		 * Socket UDP usado para la comunicación con el directorio
																		 */
	private DatagramSocket socket;
																		/**
																		 * Dirección de socket del directorio (IP:puertoUDP)
																		 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;
	private boolean successfulResponseStatus;
	private String errorDescription;
	
	public DirectoryConnector(String address) throws IOException {
																										/*
																										 * Convertir el nombre de host 'address' a InetAddress y guardar la
																										 * dirección de socket (address:DIRECTORY_PORT) del directorio en el atributo
																										 * directoryAddress, para poder enviar datagramas a dicho destino.
																										 */
		this.directoryAddress = new InetSocketAddress(InetAddress.getByName(address),DirectoryConnector.DIRECTORY_PORT);
																										/*
																										 * Crea el socket UDP en cualquier puerto para enviar datagramas al
																										 * directorio
																										 */
		this.socket = new DatagramSocket();



	}

																										/**
																										 * Método para enviar y recibir datagramas al/del directorio
																										 * 
																										 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
																										 * @return los datos recibidos del directorio (mensaje de respuesta)
																										 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
																										/*
																										 * Enviar datos en un datagrama al directorio y recibir una respuesta. El
																										 * array devuelto debe contener únicamente los datos recibidos, *NO* el búfer de
																										 * recepción al completo.
																										 */
											
		DatagramPacket pakToServ = new DatagramPacket(requestData, requestData.length,this.directoryAddress);
		DatagramPacket pakFromServ = new DatagramPacket(responseData, responseData.length);
		try {
			this.socket.send(pakToServ);
			this.socket.setSoTimeout(TIMEOUT);
			this.socket.receive(pakFromServ);
		} catch(SocketTimeoutException e){
			boolean recived = false;
			for(int i = 0; i<DirectoryConnector.MAX_NUMBER_OF_ATTEMPTS && recived == false; i++) {
					recived = this.resend(pakFromServ); // pedir que revise esto
			}
			
		} catch (IOException e1) {
			System.err.println("I/O error : "+ e1);
			System.exit(1);
		}
		String messageFromServer = new String(responseData, 0, pakFromServ.getLength());
		//System.out.println("se ha recivido : "+messageFromServer);
		response = messageFromServer.getBytes();
		// this.socket.close(); no cerrar en este método solo se puede hacer login, los métodos de despues no funcionan pporque el socket se cierra y solo se hace en el constructor
		/*
																							 * Una vez el envío y recepción asumiendo un canal confiable (sin
																							 * pérdidas) esté terminado y probado, debe implementarse un mecanismo de
																							 * retransmisión usando temporizador, en caso de que no se reciba respuesta en
																							 * el plazo de TIMEOUT. En caso de salte el timeout, se debe reintentar como
																							 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
																							 */
																							/*
																							 *  Las excepciones que puedan lanzarse al leer/escribir en el socket deben
																							 * ser capturadas y tratadas en este método. Si se produce una excepción de
																							 * entrada/salida (error del que no es posible recuperarse), se debe informar y
																							 * terminar el programa.
																							 */
																							/*
																							 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
																							 * SocketTimeoutException es más concreta que IOException.
																							 */
		
		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n" + "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}
	
	public boolean resend(DatagramPacket pak) { //función propia, auxiliar para hacer el reenvio
		try {
			this.socket.setSoTimeout(TIMEOUT);
			this.socket.receive(pak);
		} catch (SocketTimeoutException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

																							/**
																							 * Método para probar la comunicación con el directorio mediante el envío y
																							 * recepción de mensajes sin formatear ("en crudo")
																							 * 
																							 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
																							 */
	public boolean testSendAndReceive() {
																							/*
																							 * Probar el correcto funcionamiento de sendAndReceiveDatagrams. Se debe
																							 * enviar un datagrama con la cadena "login" y comprobar que la respuesta
																							 * recibida es "loginok". En tal caso, devuelve verdadero, falso si la respuesta
																							 * no contiene los datos esperados.
																							 */
		boolean success = false;
		byte[] data = "login".getBytes();
		byte[] response = this.sendAndReceiveDatagrams(data);
		String messageRecived =  new String(response, 0, response.length);
		String wanted = new String("loginok");
		//System.out.println("se ha interpretado : "+messageRecived);
		if(messageRecived.equals(wanted)) success = true;


		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

																										/**
																										 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
																										 * obtener la clave de sesión asociada a este usuario.
																										 * 
																										 * @param nickname El nickname del usuario a registrar
																										 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
																										 *         en caso de error
																										 */
	public boolean logIntoDirectory(String nickname) {
		assert (sessionKey == INVALID_SESSION_KEY);//los assert no funcionan si no tiene el parametro -ea al ejecutar el programa, esto no es el error que buscas
		boolean success = false;
																										// 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
																										// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la clase
																										// DirMessageOps
		DirMessage msgToServ = DirMessage.loginMessage(nickname);
																										// 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		String strToServ = msgToServ.toString();
		//System.out.println(strToServ);																		
																										// 3.Crear un datagrama con los bytes en que se codifica la cadena
		byte[] byteBuff = strToServ.getBytes();
																										// 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
																										// 5.Convertir respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
																										// 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
		String recived =  new String(byteDataRecived, 0, byteDataRecived.length);
		DirMessage recivedDir = DirMessage.fromString(recived);
		//System.out.println("Respuesta : " +recivedDir);
		String confirmation = recivedDir.getCode();
		int key = recivedDir.getKey();
		
		if(confirmation.matches("loginok")) {//la key es un entero, por lo tanto será una clave valida
			this.sessionKey = key;
			success =  true;
		}							
																										// 7.Devolver éxito/fracaso de la operación
		return success;
	}
	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 */
	public String[] getUserList() {
		
		String[] userlist = null;
		DirMessage msgToServe = DirMessage.userListMessage(this.getSessionKey());
		String strToServe = msgToServe.toString();
		//System.out.println("mensaje enviado :" + strToServe);
		
		byte[] byteBuff = strToServe.getBytes();
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
		
		String recived = new String(byteDataRecived, 0, byteDataRecived.length);
		//System.out.println("msg recivido " +recived);
		DirMessage recivedDir = DirMessage.fromString(recived);
		if(recivedDir.getCode().equals(DirMessageOps.OPERATION_LISTFAILED)) {
			System.err.println("[error] Failed getting userlist");
		}else {
			//userlist = recivedDir.getPeers();
			String[] peers = recivedDir.getPeers();
			Boolean[] isServer = recivedDir.getIsServer();
			userlist = new String[peers.length * 2];
			for(int i = 0; i < peers.length; i++) {
				userlist[i*2] = peers[i];
				userlist[i*2+1] = isServer[i].toString();
			}
			//for(String s : userlist) System.out.println("Peer : "+ s);
		}

		return userlist;
	}

																										/**
																										 * Método para "cerrar sesión" en el directorio
																										 * 
																										 * @return Verdadero si el directorio eliminó a este usuario exitosamente
																										 */
	public boolean logoutFromDirectory() {
																										// Ver TODOs en logIntoDirectory y seguir esquema similar
		try {
			assert (this.sessionKey != INVALID_SESSION_KEY);
		} catch (AssertionError e) {
			System.err.println("[error] invalid session key \ncould not logout"); //los assert no funcionan si no tiene el parametro -ea al ejecutar el programa, esto no es el error que buscas
		}
		
		boolean success = false;
		DirMessage msg = DirMessage.logoutMessage(this.getSessionKey());
		String msgToServe = msg.toString();
		//System.out.println(msgToServe);
		byte[] byteBuff = msgToServe.getBytes();
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
		String Recived = new String(byteDataRecived, 0, byteDataRecived.length);
		DirMessage recivedDir = DirMessage.fromString(Recived);
		String confirmation = recivedDir.getCode();
		if(confirmation.matches(DirMessageOps.OPERATION_LOGOUTOK)) success = true;
		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
																														// Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		DirMessage msg = DirMessage.registerFileServer(this.sessionKey, serverPort);
		String msgToServe = msg.toString();
		//System.out.println(msgToServe);
		byte[] byteBuff = msgToServe.getBytes();
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
		String recived = new String(byteDataRecived,0,byteDataRecived.length);
		DirMessage recivedDir = DirMessage.fromString(recived);
		String confirmation = recivedDir.getOperation();
		if(confirmation.matches(DirMessageOps.OPERATION_REGISTERFILESERVEROK)) success = true;
		return success;
	}

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) {
		InetSocketAddress serverAddr = null;
																														// Ver TODOs en logIntoDirectory y seguir esquema similar
		DirMessage msg = DirMessage.lookupServAdr(this.sessionKey, nick);
		String msgToServe = msg.toString();
		//System.out.println(msgToServe);
		byte[] byteBuff = msgToServe.getBytes();
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
		String recived = new String(byteDataRecived,0,byteDataRecived.length);
		DirMessage recivedDir = DirMessage.fromString(recived);
		//System.out.println(recivedDir.getOperation());
		if(recivedDir.getOperation().matches(DirMessageOps.OPERATION_LOOKUPSERVADROK)) {
			int port = recivedDir.getPort();
			String ip = recivedDir.getIp();
			//System.out.println("ip : "+ ip + "port : "+ port);
			try {
				serverAddr = new InetSocketAddress(InetAddress.getByName(ip),port);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}else if(recivedDir.getOperation().matches(DirMessageOps.OPERATION_ERROR)) {
			System.err.println("[warning] user not found");
		}
		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean publishLocalFiles(FileInfo[] files) {
		boolean success = false;
		String[] hashes = new String[files.length]; 
		String[] names =  new String[files.length];
		long[] sizes = new long[files.length];
		for(int i  = 0; i<files.length; i++) {
			hashes[i] = files[i].fileHash;
			names[i] = files[i].fileName;
			sizes[i] = files[i].fileSize;
			//System.out.println("an cl nombre : " + names[i]);
		}
		DirMessage msg = DirMessage.publish(hashes, sizes, names, files.length, this.getSessionKey());
		String msgToServ = msg.toString();
		//System.out.println(msgToServ);
		byte[] byteBuff = msgToServ.getBytes();
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
		String recived = new String(byteDataRecived, 0, byteDataRecived.length);
		DirMessage recivedDir = DirMessage.fromString(recived);
		String confirmation = recivedDir.getOperation();
		//System.out.println("[recived]\n"+recivedDir);
		if(confirmation.matches(DirMessageOps.OPERATION_PUBLISHOK)) {
			success = true;
		}else {
			System.err.println("[warning] an error occurred");
		}
		



		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = null;
		DirMessage msgToServe = DirMessage.fileList(this.getSessionKey());
		String strToServe = msgToServe.toString();
		//System.out.println("mensaje enviado : "+strToServe);
		byte[] byteBuff = strToServe.getBytes();
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
		String recived = new String(byteDataRecived,0,byteDataRecived.length);
		DirMessage recivedDir = DirMessage.fromString(recived);
		if(recivedDir.getOperation().matches(DirMessageOps.OPERATION_PUBLISH)) {
			filelist = new FileInfo[recivedDir.getNFichs()];
			for(int i = 0;i<recivedDir.getNFichs();i++) {
				filelist[i] = new FileInfo(recivedDir.getFichHash()[i], recivedDir.getFichName()[i], recivedDir.getFichSize()[i]);
			}
		}else {
			System.err.println("[warning] no files upload at the server or an error occurred");
		}



		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) {
		String[] nicklist = null;
		DirMessage msgToServe =  DirMessage.search(this.getSessionKey(), fileHash);
		String strToServe = msgToServe.toString();
		//System.out.println("mensaje enviado : "+strToServe);
		byte[] byteBuff = strToServe.getBytes();
		byte[] byteDataRecived = this.sendAndReceiveDatagrams(byteBuff);
		String recived = new String(byteDataRecived,0,byteDataRecived.length);
		DirMessage recivedDir = DirMessage.fromString(recived);
		if(recivedDir.getOperation().matches(DirMessageOps.OPERATION_SEARCHOK)) {
			nicklist = recivedDir.getFichName();
		}else if(recivedDir.getOperation().matches(DirMessageOps.OPERATION_PUBLISH)){
			System.err.println("[warning] multiple options found");
			System.out.println(recivedDir.getNFichs() + "options aviable");
			FileInfo[] filelist = new FileInfo[recivedDir.getNFichs()];
			for(int i = 0;i<recivedDir.getNFichs();i++) {
				filelist[i] = new FileInfo(recivedDir.getFichHash()[i], recivedDir.getFichName()[i], recivedDir.getFichSize()[i]);
			}
			if(filelist != null) {
				FileInfo.printToSysout(filelist);
			}
		}else  if(recivedDir.getOperation().matches(DirMessageOps.OPERATION_ERROR)){
			System.err.println("[warning] file not found or an error occurred");
		}



		return nicklist;
	}
	//@return devuelve un booleano como true, esto  sirve para ver si directory connector está  iniciado
	public boolean test () {
		return true;
	}



}
