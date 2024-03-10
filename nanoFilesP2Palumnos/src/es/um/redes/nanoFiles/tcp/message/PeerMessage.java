package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;
	private long hash;
	private InetSocketAddress from;
	private long init;
	private long tam;
	private long[] options;
	private byte[] data;

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
	public static PeerMessage peerMessageDownload(long h, long i, long t,InetSocketAddress f) {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_DOWNL);
		try {
			p.setHash(h);
			p.setInit(i);
			p.setTam(t);
			p.setAdrress(f);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return p;
	}
	public static PeerMessage peerMessageErrorFileNotFound() {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_FNF);
		return p;
	}
	public static PeerMessage peerMessageErrorMultipleOptions(long[] o) {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_MO);
		try {
			p.setOptions(o);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return p;
	}
	public static PeerMessage peerMessageDownlResponse(long h, byte[] d) {
		PeerMessage p = new PeerMessage(PeerMessageOps.OPCODE_DOWNLRES);
		try {
			p.setHash(h);
			p.setData(d);
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
	
	public byte getOpcode() {
		return opcode;
	}
	public long getHash() {
		return this.hash;
	}
	public InetSocketAddress getAdrress() {
		return this.from;
	}
	public long getInit() {
		return this.init;
	}
	public long getTam() {
		return this.tam;
	}
	public long[] getOptions() {
		return this.options;
	}
	public byte[] getData() {
		return this.data;
	}

	public void setHash(long h) throws NoSuchFieldException {
		if(this.opcode>=0x0) {
			this.hash = h;
		}else throw new NoSuchFieldException("OpCode does not match with this field");
	}
	public void setAdrress(InetSocketAddress a) throws NoSuchFieldException {
		if(this.opcode>=0x0) {
			this.from = a;
		}else throw new NoSuchFieldException("OpCode does not match with this field");
	}
	public void setInit(long i) throws NoSuchFieldException {
		if(this.opcode>=0x0) {
			this.init = i;
		}else throw new NoSuchFieldException("OpCode does not match with this field");
	}
	public void setTam(long t) throws NoSuchFieldException {
		if(this.opcode>=0x0) {
			this.tam = t;
		}else throw new NoSuchFieldException("OpCode does not match with this field");
	}
	public void setOptions(long[] o) throws NoSuchFieldException {
		if(this.opcode>=0x0) {
			this.options = o;
		}else throw new NoSuchFieldException("OpCode does not match with this field");
	}
	public void setData(byte[] d) throws NoSuchFieldException {
		if(this.opcode>=0x0) {
			this.data = d;
		}else throw new NoSuchFieldException("OpCode does not match with this field");
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
		byte opcode = dis.readByte();
		switch (opcode) {



		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
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

		dos.writeByte(opcode);
		switch (opcode) {




		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
