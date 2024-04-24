package es.um.redes.nanoFiles.udp.message;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import es.um.redes.nanoFiles.util.FileInfo;

																												/**
																												 * Clase que modela los mensajes del protocolo de comunicación entre pares para
																												 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
																												 * mensajes son intercambiados entre las clases DirectoryServer y
																												 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
																												 * 
																												 * @author rtitos
																												 *
																												 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea
	private static final String END_MESSAGE = "END_MESSAGE";

																												/**
																												 * Nombre del campo que define el tipo de mensaje (primera línea)
																												 */
																												//private static final String FIELDNAME_OPERATION = "operation";
																												/*
																												 * Definir de manera simbólica los nombres de todos los campos que pueden
																												 * aparecer en los mensajes de este protocolo (formato campo:valor) Esto está en la clase DirMessageField
																												 */

																												/**
																												 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
																												 */
	private String operation = DirMessageOps.OPERATION_INVALID;
																												/*
																												 * Crear un atributo correspondiente a cada uno de los campos de los
																												 * diferentes mensajes de este protocolo.
																												 */
	private String nickname;
	private String ip;
	private String code;
	private int key;
	private int port;
	private HashMap<String, Boolean> peers;
	
	
	//"constructores"
	public DirMessage(String op) {
		operation = op;
	}
	public static DirMessage loginMessage(String nick) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOGIN);
		m.setNickname(nick);
		return m;
	}
	public static DirMessage logoutMessage(int key) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOGOUT);
		m.setKey(key);
		return m;
	}
	public static DirMessage userListMessage(int key){
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_USERLIST);
		m.setKey(key);
		return m;
	}
	public static DirMessage registerFileServer(int key,int port) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_REGISTER_FILESERVER);
		m.setKey(key);
		m.setPort(port);
		return m;
	}
	public static DirMessage lookupServAdr(int key,String nick) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOOKUP_SERVADR);
		m.setKey(key);
		m.setNickname(nick);
		return m;
	}
	public static DirMessage lookupServAdrOk(int port,String ip) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOOKUPSERVADROK);
		m.setPort(port);
		m.setIp(ip);
		return m;
	}
	public static DirMessage registerFileServeOk() {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_REGISTERFILESERVEROK);
		return m;
	}
	public static DirMessage errorMessage(String code) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_ERROR);
		m.setCode(code);
		return m;
	}
	public static DirMessage confirmationMessage(String code) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_CONFIRMATION);
		m.setCode(code);
		return m;
	}
	public static DirMessage confirmationMessageLoginOk(int key) {
		DirMessage m = DirMessage.confirmationMessage(DirMessageOps.OPERATION_LOGINOK);
		m.setKey(key);
		return m;
	}
	public static DirMessage confirmationMessageLogoutOk() {
		DirMessage m = DirMessage.confirmationMessage(DirMessageOps.OPERATION_LOGOUTOK);
		return m;
	}
	public static DirMessage confirmationMessageListOk() {
		DirMessage m = DirMessage.confirmationMessage(DirMessageOps.OPERATION_LISTOK);
		m.peers = new HashMap<String, Boolean>(); //la estructura se cargará con la información en otra clase, donde se recorran las estructuras correspondientes
		return m;
	}


																					/*
																					 * Crear diferentes constructores adecuados para construir mensajes de
																					 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
																					 * NO, PARA LA IMPLEMENTACION HABRA UN CONSTRUCTOR PARA LA OPERACION Y EL RESTO SE MODIFICARA MEDIANTE SET(mediante un método para el mensaje)
																					 */

	//set
	public void setCode(String error) {
		this.code = error;
	}
	
	public void setNickname(String nick) {
		nickname = nick;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void setPeers(String peer, Boolean isServer) {
		this.peers.put(peer, isServer);
	}
	
	//get
	public String getOperation() {
		return operation;
	}
	public String getNickname() {
		return nickname;
	}
	public String getIp() {
		return this.ip;
	}
	public String getCode() {
		return this.code;
	}
	public int getKey() {
		return this.key;
	}
	public int getPort() {
		return this.port;
	}
	public String[] getPeers(){
		return this.peers.keySet().toArray(new String[this.peers.keySet().size()]);
		
	}
	public Boolean[] getIsServer() {
		return this.peers.values().toArray(new Boolean[this.peers.values().size()]);
	}




																							/**
																							 * Método que convierte un mensaje codificado como una cadena de caracteres, a
																							 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
																							 * han sido establecidos con el valor de los campos del mensaje.
																							 * 
																							 * @param message El mensaje recibido por el socket, como cadena de caracteres
																							 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
																							 *         etc.)
																							 */
	public static DirMessage fromString(String message) {
																							/*
																							 * Usar un bucle para parsear el mensaje línea a línea, extrayendo para
																							 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
																							 * guardarlo en variables locales.
																							 */

		//System.out.println("DirMessage read from socket:");
		//System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;
		String auxNick = null;
		
		for (String line : lines) {
			if(!line.contains(END_MESSAGE)) {  //HAY QUE HACER QUE NO SE PROCESE LA LINEA FIN DEL MENSAJE
				//System.out.println("linea procesada  : "+ line);
				
				int ind = line.indexOf(DELIMITER); // Posición del delimitador
				String fieldName = line.substring(0, ind).toLowerCase(); // minúsculas
				String val = line.substring(ind + 1).trim();

				switch (fieldName) {
				case DirMessageField.FIELDNAME_OPERATION:{
					assert (m == null);
					m = new DirMessage(val);
					break;
				}
				case DirMessageField.FIELDNAME_NICK:{
					m.setNickname(val);
					break;
				}
				case DirMessageField.FIELDNAME_IP:{
					m.setIp(val);
					break;
				}
				case DirMessageField.FIELDNAME_CODE:{
					m.setCode(val);
					break;
				}
				case DirMessageField.FIELDNAME_KEY:{ 
					m.setKey(Integer.parseInt(val));
					break;
				}
				case DirMessageField.FIELDNAME_PORT:{
					m.setPort(Integer.parseInt(val));
					break;
				}
				case DirMessageField.FIELDNAME_USER:{
					auxNick = val; // almacena el nick en una variable auxiliar, y lo va machacando cada vez que llega a este campo
					break;
				}
				case DirMessageField.FIELDNAME_ISSERVER:{
					try {
						m.setPeers(auxNick, Boolean.parseBoolean(val)); // una vez llega al fin del par user-isServer usa esa información para almacenarla en la estructura
					} catch (NullPointerException e) {
						m.peers = new HashMap<String, Boolean>();
						m.setPeers(auxNick, Boolean.parseBoolean(val));
						}
					break;
				}
				//TODO ir ampliando para el resto de mensajes
				default:
					System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
					System.err.println("Message was:\n" + message);
					System.exit(-1);
				}
				
				
			}
		
		}

		return m;
	}

																												/**
																												 * Método que devuelve una cadena de caracteres con la codificación del mensaje
																												 * según el formato campo:valor, a partir del tipo y los valores almacenados en
																												 * los atributos.
																												 * 
																												 * @return La cadena de caracteres con el mensaje a enviar por el socket.
																												 */
	public String toString() {
		
																												/*
																												 * En función del tipo de mensaje, crear una cadena con el tipo y
																												 * concatenar el resto de campos necesarios usando los valores de los atributos
																												 * del objeto.
																												 */
		StringBuffer sb = new StringBuffer();
		sb.append(DirMessageField.FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		
		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			sb.append(DirMessageField.FIELDNAME_NICK + DELIMITER + nickname + END_LINE); //mensaje de tipo login
			break;
			}
		case DirMessageOps.OPERATION_LOGOUT: {
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE); //mensaje de tipo logout
			break;
		}
		case DirMessageOps.OPERATION_USERLIST: {
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE); //mensaje del tipo userList
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_FILESERVER :{
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE + DirMessageField.FIELDNAME_PORT + DELIMITER + port +END_LINE );
			break;
		}
		case DirMessageOps.OPERATION_ERROR: {
			sb.append(DirMessageField.FIELDNAME_CODE + DELIMITER + code + END_LINE); //mensaje de error
			break;
		}
		case DirMessageOps.OPERATION_LOOKUP_SERVADR : {
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE + DirMessageField.FIELDNAME_NICK + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_REGISTERFILESERVEROK :{
			break; //no tiene más informacion a parte del codigo
		}
		case DirMessageOps.OPERATION_LOOKUPSERVADROK :{
			sb.append(DirMessageField.FIELDNAME_PORT + DELIMITER + port + END_LINE + DirMessageField.FIELDNAME_IP + DELIMITER + ip + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_CONFIRMATION: {
			sb.append(DirMessageField.FIELDNAME_CODE + DELIMITER + code + END_LINE); //mensaje de confirmacion
			switch (code) {
			case DirMessageOps.OPERATION_LOGINOK: {
				sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE); //confirmación de tipo loginok
				break;
			}
			case DirMessageOps.OPERATION_LOGOUTOK: {
				break;																	//confirmacionb de tipo logoutok
			}
			case DirMessageOps.OPERATION_LISTOK: {
					for(String n : this.peers.keySet()) {
						sb.append(DirMessageField.FIELDNAME_USER + DELIMITER + n + END_LINE);
						sb.append(DirMessageField.FIELDNAME_ISSERVER + DELIMITER + this.peers.get(n) + END_LINE);
					}
				break;
			}
			default:
				System.err.println("Unrecognised confirmation code, toString method error");
				System.exit(-1);
			}
			break;
		}
		default:
			System.err.println("Unable to do toString method from DirMessage");
			System.exit(-1);
		}
		
		sb.append(END_MESSAGE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
