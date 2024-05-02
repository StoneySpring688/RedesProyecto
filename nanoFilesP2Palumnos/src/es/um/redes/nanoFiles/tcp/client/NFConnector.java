package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;

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
		PeerMessage pt = PeerMessage.peerMessageAskTam(targetFileHashSubstr);
		pt.writeMessageToOutputStream(dos);
		PeerMessage msgFromServt = PeerMessage.readMessageFromInputStream(dis);
		
		if (msgFromServt.getOpcode() == PeerMessageOps.OPCODE_FNF) {
			System.err.println("FileNotFound");
		}
		else if(msgFromServt.getOpcode() == PeerMessageOps.OPCODE_MO) {
			System.err.println("MultipleOptions found : "+msgFromServt.getNOps()/40+ " options available"); // divido nops entre 40 porque por alguna razon guarda el numero de bytes
			   																								// y es más sencillo hacer esto que buscar el fallo
			byte[] o = msgFromServt.getOptions();
			String[] n = msgFromServt.getNames();
			int hashLength = 40; //longitud del hash
			int numHashes = msgFromServt.getNOps()/40;
			int option = 1;
			int ind = 0;
			
			   StringBuffer strBuf = new StringBuffer();
			    strBuf.append(String.format("%1$-10s", "Option"));
			    strBuf.append(String.format("%1$-45s", "Hash"));
				strBuf.append(String.format("%1$-30s", "Name"));
			    System.err.println(strBuf);
			
			for (int i = 0; i < numHashes; i++) {
			    byte[] hashByte = Arrays.copyOfRange(o, ind, ind + hashLength); // extraer el hash
			    String hash = new String(hashByte);
			    String name = n[i];
			    
				StringBuffer strBuff = new StringBuffer();
				strBuff.append(String.format("%1$-10s", option));
				strBuff.append(String.format("%1$-45s", hash));
				strBuff.append(String.format("%1$-30s", name));
				System.err.println(strBuff);
			    
			    //System.err.println("Opción " + option + " - "+ hash + " : " + name );
			    option++;
			    ind += hashLength; // Mover el indice
			}
			
		}else {
			Long tam = msgFromServt.getTam();

			PeerMessage p = PeerMessage.peerMessageDownload(targetFileHashSubstr, 0, tam);
			p.writeMessageToOutputStream(dos);
			PeerMessage msgFromServ = PeerMessage.readMessageFromInputStream(dis);
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
		return downloaded;
	}
	
	
	public byte[] downloadChunk(String hash, long init, long fin) throws IOException {
		byte data[] = null;
		PeerMessage p = PeerMessage.peerMessageDownload(hash, init, fin);
		p.writeMessageToOutputStream(dos);
		PeerMessage msgFromServ = PeerMessage.readMessageFromInputStream(dis);
		data = msgFromServ.getData();
		return data;	
			
	}

	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
