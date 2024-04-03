package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
																																								/*
																																								 * Crear dis/dos a partir del socket
																																								 */
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			System.out.println("leyendo mensaje...");
			PeerMessage msgFormclient = PeerMessage.readMessageFromInputStream(dis);
			System.out.println("haciendo respuesta...");
			PeerMessage resp = buildResponseFromRequest(msgFormclient);
			System.out.println("escribiendo respuesta...");
			resp.writeMessageToOutputStream(dos);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
																																/*
																																 * Mientras el cliente esté conectado, leer mensajes de socket,
																																 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
																																 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
																																 */
																																/*
																																 *  Para servir un fichero, hay que localizarlo a partir de su hash (o
																																 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
																																 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
																																 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
																																 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
																																 * devuelve la ruta al fichero a partir de su hash completo.
																																 */



	}
	
	private static PeerMessage buildResponseFromRequest(PeerMessage msg) { //debe ser statico para poder usarlo en un método estático
		PeerMessage response = null;
		
		switch (msg.getOpcode()){
		case PeerMessageOps.OPCODE_DOWNL: {
			FileInfo[] fichs = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), msg.getHash());
			
			if(fichs.length > 1) {
				byte[] o;
				
				/*utiliza la clase byteArrayOutputStream para "concatenar" los distitos arrays de byte(hash)*/
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				int nops = 0; //PERD*****
				for(FileInfo f : fichs) {
					byte[] a = f.fileHash.getBytes();
					try {
						os.write(a);
						nops++;
						os.write(PeerMessageOps.FINARRAY);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				o =os.toByteArray();
				response = PeerMessage.peerMessageErrorMultipleOptions(o,nops);
				return response;
			}else if(fichs.length == 0) {
				response = PeerMessage.peerMessageErrorFileNotFound();
				return response;
			}else {
				 String path = fichs[0].filePath;
				 byte [] data = new byte[(int) msg.getTam()];
				 try {
					RandomAccessFile fich = new RandomAccessFile(path,"r");
					fich.seek(msg.getInit());
					fich.readFully(data);
					response = PeerMessage.peerMessageDownlResponse(FileDigest.computeFileChecksumString(path), fich.length(), data);
					fich.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		case PeerMessageOps.TEST: {
			try {
				System.out.println("entero recibido : "+msg.getTest());
				response = PeerMessage.peerMessageTest(msg.getTest());
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + msg.getOpcode());
		}
		
		return response;
	}




}
