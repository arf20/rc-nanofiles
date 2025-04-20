package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = 0;

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con un par
	 * servidor de ficheros (valores posibles del campo "operation").
	 */
	
	public static final byte OPCODE_FILEREQUEST = 0x01;
	public static final byte OPCODE_CHUNKREQUEST = 0x02;
	public static final byte OPCODE_STOP = 0x03;
	public static final byte OPCODE_UPLOAD = 0x04;
	public static final byte OPCODE_FILENAME_TO_SAVE = 0x05;
	
	public static final byte OPCODE_FILEREQUEST_ACCEPTED = 0x11;
	public static final byte OPCODE_FILE_NOT_FOUND = 0x12;
	public static final byte OPCODE_CHUNK = 0x13;
	public static final byte OPCODE_CHUNKREQUEST_OUTOFRANGE = 0x14;
	public static final byte OPCODE_FILE_ALREADY_EXISTS = 0x15;



	/*
	 * TODO: (Boletín MensajesBinarios) Definir constantes con nuevos opcodes de
	 * mensajes definidos anteriormente, añadirlos al array "valid_opcodes" y añadir
	 * su representación textual a "valid_operations_str" EN EL MISMO ORDEN.
	 */
	private static final Byte[] _valid_opcodes = {
		OPCODE_INVALID_CODE,
		OPCODE_FILEREQUEST,
		OPCODE_CHUNKREQUEST,
		OPCODE_STOP,
		OPCODE_UPLOAD,
		OPCODE_FILENAME_TO_SAVE,
		OPCODE_FILEREQUEST_ACCEPTED,
		OPCODE_FILE_NOT_FOUND,
		OPCODE_CHUNK,
		OPCODE_CHUNKREQUEST_OUTOFRANGE,
		OPCODE_FILE_ALREADY_EXISTS
	};
	
	private static final String[] _valid_operations_str = {
		"INVALID_CODE",
		"FILEREQUEST",
		"CHUNKREQUEST",
		"STOP",
		"UPLOAD",
		"FILENAME_TO_SAVE",
		"FILEREQUEST_ACCEPTED",
		"FILE_NOT_FOUND",
		"CHUNK",
		"CHUNKREQUEST_OUTOFRANGE",
		"FILE_ALREADY_EXISTS"
	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
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
