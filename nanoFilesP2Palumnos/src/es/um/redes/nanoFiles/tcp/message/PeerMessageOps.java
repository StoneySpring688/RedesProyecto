package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = 0;
	public static final byte OPCODE_DOWNL = 1;
	//Los codigos de error van del 10 en adelante
	public static final byte OPCODE_FNF = 10; //FileNotFound
	public static final byte OPCODE_MO = 11; //MultipleOptions
	//los codigos de confirmacion van del 20 en adelante
	public static final byte OPCODE_DOWNLRES = 20;
	
	
	//Codigo para probar el correcto envio y recepcion de información
	public static final byte TEST = 30;
	
	
	
	//FIN SECUENCIA PARA EL ARRAY DE OPCIONES
	public static final byte[] FINARRAY ="FIN_ARRAY".getBytes() ;




	/**
	 * TODO: Definir constantes con nuevos opcodes de mensajes
	 * definidos, añadirlos al array "valid_opcodes" y añadir su
	 * representación textual a "valid_operations_str" en el mismo orden
	 */
	private static final Byte[] _valid_opcodes = {
			OPCODE_INVALID_CODE,OPCODE_DOWNL,
			OPCODE_FNF,OPCODE_MO,
			OPCODE_DOWNLRES


			};
	private static final String[] _valid_operations_str = {
			"INVALID_OPCODE","CODE_DOWNLOAD",
			"ERROR_FILENOTFOUND","ERROR_MULTIPLEOPTIONS",
			"Download_Response"



			};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static { // este bloque, al ser estático, solo se ejecuta una vez, antes de que se haga cualquier instancia de esta clase 
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
