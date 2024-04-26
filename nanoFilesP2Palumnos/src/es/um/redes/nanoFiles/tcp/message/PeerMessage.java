package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {

	private byte opcode;
	private String hash;
	private long init;
	private long tam;
	private int nOps;
	private String host;
	private String[] names;
	private int port;
	private long FLength;
	private byte[] options;
	private byte[] data;
	
	private int test;

																													/*
																													 * TODO: Añadir atributos y crear otros constructores específicos para crear
																													 * mensajes con otros campos (tipos de datos)
																													 * 
																													 */

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	public static PeerMessage peerMessageTest(int n) {
		PeerMessage p = new PeerMessage(PeerMessageOps.TEST);
		try {
			p.setTest(n);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static PeerMessage peerMessageDownload(String h, long i, long t) { //String ho,int port,
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_DOWNL);
		try {
			p.setHash(h);
			//p.setHost(ho);
			//p.setPort(port);
			p.setInit(i);
			p.setTam(t);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	public static PeerMessage peerMessageAskTam(String h) {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_ASKTAM);
		try {
			p.setHash(h);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static PeerMessage peerMessageErrorFileNotFound() {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_FNF);
		return p;
	}

	public static PeerMessage peerMessageErrorMultipleOptions(byte[] o, int nops, String[] n) {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_MO);
		try {
			p.setNOps(nops);
			p.setNames(n);
			p.setOptions(o);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static PeerMessage peerMessageDownlResponse(String h, long f, byte[] d) {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_DOWNLRES);
		try {
			p.setHash(h);
			p.setFLength(f);
			p.setData(d);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	public static PeerMessage peerMessageAskTamRes(Long t) {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_ASKTAMRES);
		try {
			p.setTam(t);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return p;
	}

																														/*
																														 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
																														 * comprobando previamente que dichos atributos han sido establecidos por el
																														 * constructor (sanity checks)
																														 */

	// get
	public byte getOpcode() {
		return opcode;
	}
	
	public int getTest() {
		return this.test;
	}
	
	public String getHash() {
		return this.hash;
	}

	public long getInit() {
		return this.init;
	}

	public long getTam() {
		return this.tam;
	}

	public int getNOps() {
		return this.nOps;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String[] getNames() {
		return Arrays.copyOf(this.names, this.names.length);
	}
	
	public int getPort() {
		return this.port;
	}

	public long getFLength() {
		return this.FLength;
	}

	public byte[] getOptions() {
		return this.options;
	}

	public byte[] getData() {
		return this.data;
	}

	// set
	public void setHash(String h) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNL || this.opcode == PeerMessageOps.OPCODE_DOWNLRES || this.opcode == PeerMessageOps.OPCODE_ASKTAM) {
			this.hash = h;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x1");
	}

	public void setTest(int t) throws NoSuchFieldException {
		if(this.opcode == PeerMessageOps.TEST) {
			this.test = t;
		}else {
			throw new NoSuchFieldException("OpCode does not match with this field code 0x2");
		}
	}
	
	public void setInit(long i) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNL) {
			this.init = i;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x3");
	}

	public void setTam(long t) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNL || this.opcode == PeerMessageOps.OPCODE_ASKTAMRES) {
			this.tam = t;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x4");
	}

	public void setNOps(int n) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_MO) {
			this.nOps = n;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x5");
	}
	
	public void setHost(String h) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNL || this.opcode == PeerMessageOps.OPCODE_DOWNLRES) {
			this.hash = h;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x6");
	}
	
	public void setNames(String[] n) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_MO) {
			this.names = n;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x6");
	}
	
	public void setPort(int p) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_MO) {
			this.port = p;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x7");
	}

	public void setFLength(long f) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNLRES) {
			this.FLength = f;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x8");
	}

	public void setOptions(byte[] o) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_MO) {
			this.options = o;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0x9");
	}

	public void setData(byte[] d) throws NoSuchFieldException {
		if (this.opcode == PeerMessageOps.OPCODE_DOWNLRES) {
			this.data = d;
		} else
			throw new NoSuchFieldException("OpCode does not match with this field code 0xA");
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {

																													/*
																													 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
																													 * resto de campos para ir extrayendo con los valores y establecer los atributos
																													 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
																													 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
																													 * bytes, dis.readInt para leer un entero, etc.
																													 */
		PeerMessage message = new PeerMessage();
		try {
			 message.opcode = dis.readByte();
			//System.out.println("byte leido : "+message.opcode );
			switch (message.opcode) {
			case PeerMessageOps.OPCODE_DOWNL: {
				try {
					message.setHash(dis.readUTF());
					//message.setHost(dis.readUTF());
					//message.setPort(dis.readInt());
					message.setInit(dis.readLong());
					message.setTam(dis.readLong());
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				break;
			}
			case PeerMessageOps.OPCODE_ASKTAM: {
				try {
					message.setHash(dis.readUTF());
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				break;
			}
			case PeerMessageOps.OPCODE_FNF: {
				break; //este mensaje solo tiene el código de operación
			}
			case PeerMessageOps.OPCODE_MO: {
				try {
					int n = dis.readInt();
					message.setNOps(n);
					message.setOptions(new byte[message.getNOps()]);
					message.setNames(new String[message.getNOps()/40]);
					
					for(int i = 0; i< n/40; i++) { // si falla, poner (n/40)-1
						message.names[i] = dis.readUTF();
						dis.readInt(); //separador entre nombres para poder leer cadenas separadas
					}
					
					dis.readFully(message.options);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				break;
			}
			case PeerMessageOps.OPCODE_DOWNLRES: {
				try {
					message.setHash(dis.readUTF());
					message.setFLength(dis.readLong());
					message.setData(new byte[(int) message.getFLength()]);
					dis.readFully(message.data);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				break;
			}
			case PeerMessageOps.OPCODE_ASKTAMRES: {
				try {
					message.setTam(dis.readLong());
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				break;
			}
			case PeerMessageOps.TEST: {
				try {
					message.setTest(dis.readInt());
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				break;
			}

			default:
				System.err.println(
						"PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
								+ PeerMessageOps.opcodeToOperation(message.opcode));
				System.exit(-1);
			}
			// dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
																															/*
																															 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
																															 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
																															 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
																															 * de bytes, dos.writeInt para escribir un entero, etc.
																															 */

		try {
			dos.writeByte(this.opcode);
			switch (this.opcode) {
			case PeerMessageOps.OPCODE_DOWNL: {
				dos.writeUTF(this.getHash());
				//dos.writeUTF(this.getHost());
				//dos.writeInt(this.getPort());
				dos.writeLong(this.getInit());
				dos.writeLong(this.getTam());
				break;
			}
			case PeerMessageOps.OPCODE_ASKTAM: {
				dos.writeUTF(this.getHash());
				break;
			}
			case PeerMessageOps.OPCODE_FNF: {
				break; // este mensaje solo tiene el código de operación
			}
			case PeerMessageOps.OPCODE_MO: {
				dos.writeInt(this.options.length);
				for(int i = 0; i < this.names.length; i++) {
					dos.writeUTF(this.names[i]);
					dos.writeInt(12);
				}
				dos.write(this.options);
				break;
			}
			case PeerMessageOps.OPCODE_DOWNLRES: {
				dos.writeUTF(this.getHash());
				dos.writeLong(this.data.length);
				dos.write(data);
				break;
			}
			case PeerMessageOps.OPCODE_ASKTAMRES: {
				dos.writeLong(this.getTam());
				break;
			}
			case PeerMessageOps.TEST: {
				dos.writeInt(this.getTest());
				break;
			}

			default:
				System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode
						+ "(" + PeerMessageOps.opcodeToOperation(opcode) + ")");
			}
			// dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
