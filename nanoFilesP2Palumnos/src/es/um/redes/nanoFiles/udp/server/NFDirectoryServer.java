package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
																		/**
																		 * Número de puerto UDP en el que escucha el directorio
																		 */
	public static final int DIRECTORY_PORT = 6868;

																		/**
																		 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
																		 */
	private DatagramSocket socket = null;
																		/**
																		 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
																		 * 
																		 */
	private HashMap<String, Integer> nicks;
																		/**
																		 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
																		 * 
																		 */
	private HashMap<Integer, String> sessionKeys;
																		/*
																		 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
																		 * para mantener en el directorio cualquier información necesaria para la
																		 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
																		 * registrados, etc.
																		 */
	// estructuras para la descarga por nick
	
	private HashMap<String, Integer> peerServerPort = new HashMap<String, Integer>();
	private HashMap<String,InetSocketAddress> peerServeDir  = new HashMap<String,InetSocketAddress>();
	
	// estructuras para almacenar la información de los ficheros que comparten los peers
	
	private HashMap <String, List<Integer> > fichPeer;
	private HashMap <String, String> fichName;
	private HashMap <String, Long> fichSize;
																		/**
																		 * Generador de claves de sesión aleatorias (sessionKeys)
																		 */
	Random random = new Random();
																		/**
																		 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
																		 * enlace no confiable y testear el código de retransmisión)
																		 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
																					/*
																					 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
																					 * confiable)
																					 */
		messageDiscardProbability = corruptionProbability;
																					/*
																					 * (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
																					 * ligado al puerto especificado por el argumento directoryPort en la máquina
																					 * local,
																					 */
		 this.socket = new DatagramSocket(DIRECTORY_PORT);
																					/*
																					 * (Boletín UDP) Inicializar el resto de atributos de esta clase
																					 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
																					 */
		this.nicks = new  HashMap<String, Integer>();
		this.sessionKeys = new HashMap<Integer, String>();
		this.fichPeer = new HashMap<String, List<Integer>>();
		this.fichName = new HashMap<String, String>();
		this.fichSize = new HashMap<String, Long>();

		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
																					/*
																					 * (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
																					 * asociado al búfer
																					 */
		//byte[] receptionBuffer = null;
		/*byte[] receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		InetSocketAddress clientAddr = null; // estos 2 valores se sobreecriben luego con el valor correcto
		int dataLength = -1;
		DatagramPacket pakFromClient = new DatagramPacket(receptionBuffer,receptionBuffer.length);*/
		
		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			byte[] receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
			InetSocketAddress clientAddr = null; // estos 2 valores se sobreecriben luego con el valor correcto
			int dataLength = -1;
			DatagramPacket pakFromClient = new DatagramPacket(receptionBuffer,receptionBuffer.length);
			try {
				this.socket.receive(pakFromClient);
			} catch (Exception e) {
				//TODO better handle exceptions
				e.printStackTrace();
			}
			dataLength = pakFromClient.getLength();
			clientAddr = (InetSocketAddress) pakFromClient.getSocketAddress();
			if(clientAddr.getAddress().isLoopbackAddress()) {
				InetAddress clientIp = InetAddress.getLocalHost();
				int clientPort = pakFromClient.getPort();
				clientAddr = new InetSocketAddress(clientIp, clientPort);
			}
			
																												//  (Boletín UDP) Recibimos a través del socket un datagrama
																									
																												//  (Boletín UDP) Establecemos dataLength con longitud del datagrama
																												// recibido
																									
																												//  (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
																												// obtenida del
																												// datagrama recibido

			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				String messageFromClient = null;
																												/*
																												 * (Boletín UDP) Construir una cadena a partir de los datos recibidos en
																												 * el buffer de recepción
																												 * //string creado con el datagrama, desde 0 a la longitud del datagrama -1
																												 */
				messageFromClient =  new String(receptionBuffer, 0, pakFromClient.getLength());

				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
					
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
																												/*
																												 * (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
																												 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
																												 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
																												 * y no se envía ninguna respuesta.
																												 */
					String messageToClient = new String("loginok");
					byte[] dataToClient = messageToClient.getBytes();
					DatagramPacket pakToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
					
					//String messageAEnv =  new String(dataToClient, 0, pakToClient.getLength());
					if(messageFromClient.equals("login")) {
						this.socket.send(pakToClient);
						//System.out.println("se ha enviado : "+messageAEnv);
					}else {
						System.err.println("Unexpected client message, expected message : "+messageFromClient);
					}

				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
																												/*
																												 * Construir String partir de los datos recibidos en el datagrama. A
																												 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
																												 * Después, usar la cadena para construir un objeto DirMessage que contenga en
																												 * sus atributos los valores del mensaje (fromString).
																												 */
					
					String clientResponse = new String(pakFromClient.getData(),0,pakFromClient.getData().length);
					System.out.println("La cadena contenida en el datagrama pakFromClient es : "+clientResponse);
					DirMessage msg  = DirMessage.fromString(clientResponse);
																												/*
																												 * Llamar a buildResponseFromRequest para construir, a partir del objeto
																												 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
																												 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
																												 * DirMessage de respuesta deben haber sido establecidos con los valores
																												 * adecuados para los diferentes campos del mensaje (operation, etc.)
																												 */
					DirMessage msgResponse = buildResponseFromRequest(msg, clientAddr);
					//String msgResponse = buildResponseFromRequest(msg, clientAddr);
																												/*
																												 * Convertir en string el objeto DirMessage con el mensaje de respuesta a
																												 * enviar, extraer los bytes en que se codifica el string (getBytes), y
																												 * finalmente enviarlos en un datagrama
																												 */
					String messageToClient2 = new String(msgResponse.toString());
					byte[] datatoClient2 = messageToClient2.getBytes();
					DatagramPacket pakToClient2 = new DatagramPacket(datatoClient2, datatoClient2.length, clientAddr);
					this.socket.send(pakToClient2);
					
				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}
			//this.socket.close(); si se cierra el socket desde el servidor (en el while al menos),comprobado, cuando intente volver a abrir el socket no podrá, dará una excepción y finalizará la ejecución
		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
																												/*
																												 * Construir un DirMessage con la respuesta en función del tipo de mensaje
																												 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
																												 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
																												 * files...)
																												 */
	

		DirMessage response = null;

		switch (msg.getOperation()) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();
			
			if(this.nicks.containsKey(username)) {
				response = DirMessage.errorMessage(DirMessageOps.OPERATION_LOGINFAILED);
			}
			else {
				int sesionKey = random.nextInt(10000);
				this.nicks.put(username, sesionKey);
				this.sessionKeys.put(sesionKey, username);
				response = DirMessage.confirmationMessageLoginOk(sesionKey);
				/*System.out.println("despues del login");
				for(int k : sessionKeys.keySet()) {
					System.out.println(k);
				}
				for(String n : nicks.keySet()) {
					System.out.println(n);
				}*/
				
				//System.out.println(response.toString());
			}
																												/*
																												 * Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
																												 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
																												 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(10000)
																												 * para generar la session key
																												 */
																												/*
																												 * Construimos un mensaje de respuesta que indique el éxito/fracaso del
																												 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
																												 * resultado del método.
																												 */
																												/*
																												 * Imprimimos por pantalla el resultado de procesar la petición recibida
																												 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
																												 * servidor
																												 */
			break;
		}
		case DirMessageOps.OPERATION_LOGOUT: {
			int key = msg.getKey();
				if(this.sessionKeys.containsKey(key)) {
					this.nicks.remove(this.sessionKeys.get(key));
					this.sessionKeys.remove(key);
					/*System.out.println("despues del logout");
					for(int k : sessionKeys.keySet()) {
						System.out.println(k);
					}
					for(String n : nicks.keySet()) {
						System.out.println(n);
					}*/
					response = DirMessage.confirmationMessageLogoutOk();
				}
				else {
					response = DirMessage.errorMessage(DirMessageOps.OPERATION_LOGOUTFAILED);
				}
			break;
		}
		case DirMessageOps.OPERATION_USERLIST: {
			if(this.sessionKeys.containsKey(msg.getKey())) {
				response = DirMessage.confirmationMessageListOk();
				for(String s : nicks.keySet()) {
					System.out.println(s);
					System.out.println(this.peerServerPort.keySet().contains(s));
					response.setPeers(s, this.peerServerPort.keySet().contains(s));
				}
				//System.out.println("msg enviado : "+response.toString());
			}else {
				response = DirMessage.errorMessage(DirMessageOps.OPERATION_LISTFAILED);
			}
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_FILESERVER: {
			int key = msg.getKey();
			int port = msg.getPort();
			String nick = this.sessionKeys.get(key);
			//System.out.println("el nick que añade es : " + nick);
			this.peerServerPort.put(nick,port);
			this.peerServeDir.put(nick, clientAddr);
			response = DirMessage.registerFileServeOk();
			break;
		}
		case DirMessageOps.OPERATION_LOOKUP_SERVADR : {
			String nick = msg.getNickname();
			if(this.peerServeDir.containsKey(nick) && this.peerServerPort.containsKey(nick) ) {
				InetSocketAddress ipInetSocket = this.peerServeDir.get(nick);
				String ip = ipInetSocket.getHostString();
				int port = this.peerServerPort.get(nick);
				response = DirMessage.lookupServAdrOk(port, ip);
			}else {
				response = DirMessage.errorMessage(DirMessageOps.OPERATION_LOOKUPSERVADRFAILED);
			}
			break;
		}case DirMessageOps.OPERATION_PUBLISH : {
			int key = msg.getKey();
			String nick = this.sessionKeys.get(key);
			String[] hashes = msg.getFichHash();
			String[] names = msg.getFichName();
			long[] sizes = msg.getFichSize();
			int nfichs = msg.getNFichs();
			if(this.peerServeDir.keySet().contains(nick)) { //comprobar que sea un servidor de ficheros dado de alta
				for(int i = 0; i<nfichs; i++) {
					if(this.fichPeer.keySet().contains(hashes[i]) && !this.fichPeer.get(hashes[i]).contains(key)) {
						//System.out.println("iteracion : " + i);
						this.fichPeer.get(hashes[i]).add(key);
						//System.out.println("añadiendo al hash : " + hashes[i]);
					}else if(!this.fichPeer.keySet().contains(hashes[i])) {
						//System.out.println("iteracion : " + i);
						List<Integer> listaPeers = new ArrayList<Integer>();
						listaPeers.add(key);
						fichPeer.put(hashes[i], listaPeers);
						//System.out.println("añadiendo hash : " + hashes[i]);
						fichName.put(hashes[i], names[i]);
						//System.out.println("añadiendo nombre : " + names[i]);
						fichSize.put(hashes[i], sizes[i]);
						//System.out.println("añadiendo tamano : " + sizes[i]);
					}
				}
				response = DirMessage.publishOk();
			}else {
				response = DirMessage.errorMessage(DirMessageOps.OPERATION_PUBLISH_FAILED);
			}
			
			break;
		}
		case DirMessageOps.OPERATION_FILELIST : {
			int nFichs = this.fichPeer.keySet().size();
			System.out.println(nFichs);
			if(nFichs >= 1) {
				System.out.println("nfichs : "+nFichs);
				String fichHash[] = new String[nFichs];
				String fichName[] = new String[nFichs];
				long fichSize[] = this.fichSize.values().stream().mapToLong(Long::longValue).toArray();
				this.fichPeer.keySet().toArray(fichHash);
				this.fichName.values().toArray(fichName);
				
				/*for(String h : fichHash) {
					System.out.println("hash : "+h);
				}
				for(String n : fichName) {
					System.out.println("name : "+n);
				}
				for(long s : fichSize) {
					System.out.println("size : "+s);
				}*/
				response = DirMessage.publish(fichHash, fichSize, fichName, nFichs, 0);
			}else {
				response  = DirMessage.errorMessage(DirMessageOps.OPERATION_FILELIST_FAILED);
			}
			break;
		}
		default:
			System.out.println("Unexpected message operation: \"" + msg.getOperation() + "\"");
		}
		System.out.println("\n"+response.toString());
		return response;

	}
}
