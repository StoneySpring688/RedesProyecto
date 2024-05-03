package es.um.redes.nanoFiles.udp.message;

import java.util.Arrays;
import java.util.HashMap;


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

																												
	private String operation = DirMessageOps.OPERATION_INVALID;																									
	private String nickname;
	private String ip;
	private String code;
	private int key;
	private int port;
	private int nfichs;
	private HashMap<String, Boolean> peers;
	private HashMap<String, String[]> fichpeers;
	private String[] fichhash;
	private String[] fichname;
	private int[] npeer;
	private long[] fichsize;
	
	
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
	public static DirMessage publish(String[]h,long[]s,String[]n, int nf, int k) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_PUBLISH);
		m.setKey(k);
		m.setNFichs(nf);
		m.setFichHash(h);
		m.setFichSize(s);
		m.setFichName(n);
		return m;
	}
	public static DirMessage fileList(int k) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_FILELIST);
		m.setKey(k);
		return m;
	}
	public static DirMessage search(int k, String h) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_SEARCH);
		m.setKey(k);
		m.setNFichs(1);
		m.setFichHash(new String[m.getNFichs()]);
		m.fichhash[0] = h;
		return m;
	}
	public static DirMessage stopsServer(int k, int nf, String[] h) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_STOPSERVER);
		m.setKey(k);
		m.setNFichs(nf);
		m.setFichName(h);
		return m;
	}
	public static DirMessage downloadAskInfo(String h) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_DOWNLOADASKINFO);
		m.setNickname(h);
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
	public static DirMessage publishOk() {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_PUBLISHOK);
		return m;
	}
	public static DirMessage filelistok(String[]h,long[]s,String[]n, int[]npeers, int nf) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_FILELISTOK);
		m.setNFichs(nf);
		m.setFichHash(h);
		m.setFichSize(s);
		m.setFichName(n);
		m.setNPeers(npeers);
		m.fichpeers = new HashMap<String, String[]>();
		return m;
	}
	public static DirMessage searchOk(int nf, String[]n) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_SEARCHOK);
		m.setNFichs(nf);
		m.setFichName(n); // fich name porque en el método fromString es el campo donde se itera
		return m;
	}
	public static DirMessage stopServerOk() {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_STOPSERVEROK);
		return m;
	}
	public static DirMessage downloadAskInfoOk(int nPeers, long tam, String h, int[] p, String[] ip) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_DOWNLOADASKINFOOK);
		m.setNFichs(nPeers);
		long[] t = new long[1];
		t[0] = tam;
		m.setFichSize(t);
		m.setNickname(h);
		m.setNPeers(p);
		m.setFichName(ip);
		return m;
	}
	public static DirMessage errorMessage(String code) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_ERROR);
		m.setCode(code);
		return m;
	}
	public static DirMessage confirmationMessageLoginOk(int key) {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOGINOK);
		m.setKey(key);
		return m;
	}
	public static DirMessage confirmationMessageLogoutOk() {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOGOUTOK);
		return m;
	}
	public static DirMessage confirmationMessageListOk() {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LISTOK);
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
	public void setNFichs(int nf) {
		this.nfichs = nf;
	}
	public void setPeers(String peer, Boolean isServer) {
		this.peers.put(peer, isServer);
	}
	public void setFichPeers(String h, String[] p) {
		this.fichpeers.put(h, p);
	}
	public void setFichHash(String[] h) {
		this.fichhash = h;
	}
	public void setFichSize(long[] s) {
		this.fichsize = s;
	}
	public void setNPeers(int[] npeeers) {
		this.npeer = npeeers;
	}
	public void setFichName(String[] n) {
		this.fichname = n;
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
	public int getNFichs() {
		return this.nfichs;
	}
	public String[] getPeers(){
		return this.peers.keySet().toArray(new String[this.peers.keySet().size()]);
		
	}
	public Boolean[] getIsServer() {
		return this.peers.values().toArray(new Boolean[this.peers.values().size()]);
	}
	public String[] getFichPeers(String h) {
		return this.fichpeers.get(h);
	}
	public String[] getFichHash() {
		return Arrays.copyOf(this.fichhash, this.fichhash.length);
	}
	public long[] getFichSize() {
		return Arrays.copyOf(this.fichsize, this.fichsize.length);
	}
	public int[] getNPeers() {
		return Arrays.copyOf(this.npeer, this.npeer.length);
	}
	public String[] getFichName() {
		return Arrays.copyOf(this.fichname, this.fichname.length);
	}




/**
* Método que convierte un mensaje codificado como una cadena de caracteres, a
* un objeto de la clase PeerMessage, en el cual los atributos correspondientes
* han sido establecidos con el valor de los campos del mensaje.
* 
* @param message El mensaje recibido por el socket, como cadena de caracteres
* @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores, etc.)
*/
	public static DirMessage fromString(String message) {

		String[] lines = message.split(END_LINE + "");
		DirMessage m = null;
		String auxNick = null;
		int aux = 0; // se utiliza para guardar en las estruturas fhash,fsize,fname
		int nn = 0;  // se utiliza para guardar en la estrutura nicks
		String[] nicks = null; // se pone a null cuando empiece la información de un nuevo fichero
		
		for (String line : lines) {
			if(!line.contains(END_MESSAGE)) {  //HAY QUE HACER QUE NO SE PROCESE LA LINEA FIN DEL MENSAJE
				
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
				}case DirMessageField.FIELDNAME_NFICHS : {
					m.setNFichs(Integer.parseInt(val));
					break;
				}
				case DirMessageField.FIELDNAME_FICHHASH: {
					try {
						m.fichhash[aux] = val;
					} catch (NullPointerException e) {
						m.setFichHash(new String[m.getNFichs()]);
						m.fichhash[aux] = val;
					}
					
					break;
				}
				case DirMessageField.FIELDNAME_FICHSIZE: {
					try {
						m.fichsize[aux] = Long.parseLong(val);
					} catch (NullPointerException e) {
						if(m.getOperation().matches(DirMessageOps.OPERATION_DOWNLOADASKINFOOK)) {
							m.setFichSize(new long[1]);
						}else {
							m.setFichSize(new long[m.getNFichs()]);
						}
						m.fichsize[aux] = Long.parseLong(val);
					}
					
					break;
				}
				case DirMessageField.FIELDNAME_FICHNAME: {
					try {
						m.fichname[aux] = val;
						if(m.getOperation().matches(DirMessageOps.OPERATION_FILELISTOK)) {
							//System.out.println("añadiendo");
							m.fichpeers.put(m.getFichHash()[aux], nicks);
							nn = 0;
							nicks = null;
						}
						aux++; // se itera ya que es el ultimo atributo de un fichero tal cual está la estructura de el mensaje
					} catch (NullPointerException e) {
						m.setFichName(new String[m.getNFichs()]);
						m.fichname[aux] = val;
						if(m.getOperation().matches(DirMessageOps.OPERATION_FILELISTOK)) {
							m.fichpeers.put(m.getFichHash()[aux], nicks);
							nn = 0;
							nicks = null;
						}
						aux++; // se itera ya que es el ultimo atributo de un fichero tal cual está la estructura de el mensaje
					}
					
					break;
				}
				case DirMessageField.FIELDNAME_NPEER : {
					
					try {
						m.npeer[aux] = Integer.parseInt(val);
					} catch (NullPointerException e) {
						m.setNPeers(new int[m.getNFichs()]);
						if(m.getOperation().matches(DirMessageOps.OPERATION_FILELISTOK)){
							m.fichpeers = new HashMap<String, String[]>();
						}
						m.npeer[aux] = Integer.parseInt(val);
					}
					break;
				}
				case DirMessageField.FIELDNAME_FICHPEER:{
					try {
						 nicks[nn] = val;
						 nn++;
					} catch (NullPointerException e) {
						nicks = new String[m.getNPeers()[aux]];
						//System.out.println("aux : " + aux);
						//System.out.println("nn : " + nn);
						nicks[nn] = val;
						nn++;
						}
					break;
				}
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
* según el formato campo:valor, a partir del tipo y los valores almacenados en los atributos.
* 
* @return La cadena de caracteres con el mensaje a enviar por el socket.
*/
	public String toString() {
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
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE);
			sb.append(DirMessageField.FIELDNAME_PORT + DELIMITER + port +END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_ERROR: {
			sb.append(DirMessageField.FIELDNAME_CODE + DELIMITER + code + END_LINE); //mensaje de error
			break;
		}
		case DirMessageOps.OPERATION_LOOKUP_SERVADR : {
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE);
			sb.append(DirMessageField.FIELDNAME_NICK + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_PUBLISH : {
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE);
			sb.append(DirMessageField.FIELDNAME_NFICHS + DELIMITER + nfichs + END_LINE);
			for(int i = 0; i< this.nfichs ; i++) {
				sb.append(DirMessageField.FIELDNAME_FICHHASH + DELIMITER + fichhash[i] + END_LINE);
				sb.append(DirMessageField.FIELDNAME_FICHSIZE + DELIMITER + fichsize[i] + END_LINE);
				sb.append(DirMessageField.FIELDNAME_FICHNAME + DELIMITER + fichname[i] + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_FILELIST :{
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SEARCH :{
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key + END_LINE);
			sb.append(DirMessageField.FIELDNAME_NFICHS + DELIMITER + nfichs + END_LINE);
			sb.append(DirMessageField.FIELDNAME_FICHHASH + DELIMITER + fichhash[0] + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_STOPSERVER :{
			sb.append(DirMessageField.FIELDNAME_KEY + DELIMITER + key +END_LINE);
			sb.append(DirMessageField.FIELDNAME_NFICHS + DELIMITER + nfichs + END_LINE);
			for(int i = 0; i< this.nfichs ; i++) {
				sb.append(DirMessageField.FIELDNAME_FICHNAME + DELIMITER + fichname[i] + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_DOWNLOADASKINFO :{
			sb.append(DirMessageField.FIELDNAME_NICK + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_REGISTERFILESERVEROK :{
			break; //no tiene más informacion a parte del codigo
		}
		case DirMessageOps.OPERATION_LOOKUPSERVADROK :{
			sb.append(DirMessageField.FIELDNAME_PORT + DELIMITER + port + END_LINE);
			sb.append(DirMessageField.FIELDNAME_IP + DELIMITER + ip + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_PUBLISHOK : {
			break; // no tiene más información a parte del codigo
		}
		case DirMessageOps.OPERATION_FILELISTOK : {
			sb.append(DirMessageField.FIELDNAME_NFICHS + DELIMITER + nfichs + END_LINE);
			for(int i = 0; i< this.nfichs ; i++) {
				sb.append(DirMessageField.FIELDNAME_FICHHASH + DELIMITER + fichhash[i] + END_LINE);
				sb.append(DirMessageField.FIELDNAME_FICHSIZE + DELIMITER + fichsize[i] + END_LINE);
				sb.append(DirMessageField.FIELDNAME_NPEER + DELIMITER + npeer[i] + END_LINE);
				for(int j = 0; j<npeer[i];j++) {
					sb.append(DirMessageField.FIELDNAME_FICHPEER + DELIMITER + fichpeers.get(fichhash[i])[j] + END_LINE);
				}
				sb.append(DirMessageField.FIELDNAME_FICHNAME + DELIMITER + fichname[i] + END_LINE);
			}
			
			break;
		}
		case DirMessageOps.OPERATION_SEARCHOK : {
			sb.append(DirMessageField.FIELDNAME_NFICHS + DELIMITER + nfichs + END_LINE);
			for(int i = 0; i< this.nfichs ; i++) {
				sb.append(DirMessageField.FIELDNAME_FICHNAME + DELIMITER + fichname[i] + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_STOPSERVEROK : {
			break; // no tiene más información a parte del codigo
		}
		case DirMessageOps.OPERATION_DOWNLOADASKINFOOK :{
			sb.append(DirMessageField.FIELDNAME_NFICHS + DELIMITER + nfichs + END_LINE);
			sb.append(DirMessageField.FIELDNAME_FICHSIZE + DELIMITER + fichsize[0] + END_LINE);
			sb.append(DirMessageField.FIELDNAME_NICK + DELIMITER + nickname + END_LINE);
			for(int i=0;i<nfichs;i++) {
				sb.append(DirMessageField.FIELDNAME_NPEER + DELIMITER + npeer[i] + END_LINE);
				sb.append(DirMessageField.FIELDNAME_FICHNAME + DELIMITER + fichname[i] + END_LINE);
			}
			break;
		}
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
			System.err.println("Unable to do toString method from DirMessage");
			System.exit(-1);
		}
		
		sb.append(END_MESSAGE);
		return sb.toString();
	}
}
