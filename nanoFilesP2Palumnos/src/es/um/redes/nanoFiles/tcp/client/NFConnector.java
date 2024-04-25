package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataInputStream dis;
	private DataOutputStream dos;



	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
																											/*
																											 *Se crea el socket a partir de la dirección del servidor (IP, puerto). La
																											 * creación exitosa del socket significa que la conexión TCP ha sido
																											 * establecida.
																											 */
		this.socket = new Socket(fserverAddr.getAddress(),fserverAddr.getPort());
		
																											/*
																											 * Se crean los DataInputStream/DataOutputStream a partir de los streams de
																											 * entrada/salida del socket creado. Se usarán para enviar (dos) y recibir (dis)
																											 * datos del servidor.
																											 */
		this.dis = new DataInputStream(this.socket.getInputStream());
		this.dos = new DataOutputStream(this.socket.getOutputStream());


	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr Subcadena del hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escribirán los datos
	 *                             descargados del servidor
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
		boolean downloaded = false;
		/*
		Random random = new Random();
		int n = random.nextInt(10000);
		PeerMessage p = PeerMessage.peerMessageTest(n);
		p.writeMessageToOutputStream(dos);
		System.out.println("entero enviado : "+n);
		PeerMessage msgFromServ = PeerMessage.readMessageFromInputStream(dis);
		System.out.println("entero recivido : " + msgFromServ.getTest());
		if(n==msgFromServ.getTest()) {
			System.out.println("[comms] ok");
			downloaded = true;
		}
		*/
		FileInfo[] fichs = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), targetFileHashSubstr);
		long tam = -1;
		if(fichs.length >= 1) { // si hay más de uno se toma el primero y salta multiple options, no tiene impacto
			tam = fichs[0].getFileSize(); 
		}else {
			System.err.println("no hash coincidence in database");
			System.exit(1);
		}
		//System.out.println("[debug] fich tam "+ tam);
		
		PeerMessage p = PeerMessage.peerMessageDownload(targetFileHashSubstr, 0, tam);
		p.writeMessageToOutputStream(dos);
		PeerMessage msgFromServ = PeerMessage.readMessageFromInputStream(dis);
		
		if (msgFromServ.getOpcode() == PeerMessageOps.OPCODE_FNF) {
			System.err.println("FileNotFound");
		}
		else if(msgFromServ.getOpcode() == PeerMessageOps.OPCODE_MO) {
			System.err.println("MultipleOptions found : "+msgFromServ.getNOps()/40+ " options available"); // divido nops entre 40 porque por alguna razon guarda el numero de bytes
																										   // y es más sencillo hacer esto que buscar el fallo
			byte[] o = msgFromServ.getOptions();
			int hashLength = 40; //longitud del hash
			int numHashes = msgFromServ.getNOps()/40;
			int option = 1;
			int ind = 0;
			for (int i = 0; i < numHashes; i++) {
			    byte[] hashByte = Arrays.copyOfRange(o, ind, ind + hashLength); // extraer el hash
			    String hash = new String(hashByte);
			    FileInfo[] fs = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), hash);
			    String name = fs[0].fileName;
			    System.err.println("Opción " + option + " - "+ hash + " : " + name );
			    option++;
			    ind += hashLength; // Mover el indice
			}
		}
		else {
			byte[] data = msgFromServ.getData();
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(data);
				downloaded = true;
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String hash1 = FileDigest.computeFileChecksumString(file.getName());
			String hash2 = msgFromServ.getHash();
			if(hash1.equals(hash2)) {
				System.out.println("files are identical");
			}else {
				System.err.println("files are not identical");
				System.out.println(hash1);
				System.out.println(hash2);
			}
		}
		/*
		 * TODO: Construir objetos PeerMessage que modelen mensajes con los valores
		 * adecuados en sus campos (atributos), según el protocolo diseñado, y enviarlos
		 * al servidor a través del "dos" del socket mediante el método
		 * writeMessageToOutputStream.
		 */
		/*
		 * TODO: Recibir mensajes del servidor a través del "dis" del socket usando
		 * PeerMessage.readMessageFromInputStream, y actuar en función del tipo de
		 * mensaje recibido, extrayendo los valores necesarios de los atributos del
		 * objeto (valores de los campos del mensaje).
		 */
		/*
		 * TODO: Para escribir datos de un fichero recibidos en un mensaje, se puede
		 * crear un FileOutputStream a partir del parámetro "file" para escribir cada
		 * fragmento recibido (array de bytes) en el fichero mediante el método "write".
		 * Cerrar el FileOutputStream una vez se han escrito todos los fragmentos.
		 */
		/*
		 * NOTA: Hay que tener en cuenta que puede que la subcadena del hash pasada como
		 * parámetro no identifique unívocamente ningún fichero disponible en el
		 * servidor (porque no concuerde o porque haya más de un fichero coincidente con
		 * dicha subcadena)
		 */

		/*
		 * TODO: Finalmente, comprobar la integridad del fichero creado para comprobar
		 * que es idéntico al original, calculando el hash a partir de su contenido con
		 * FileDigest.computeFileChecksumString y comparándolo con el hash completo del
		 * fichero solicitado. Para ello, es necesario obtener del servidor el hash
		 * completo del fichero descargado, ya que quizás únicamente obtuvimos una
		 * subcadena del mismo como parámetro.
		 */




		return downloaded;
	}





	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
