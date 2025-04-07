package es.um.redes.nanoFiles.udp.message;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.StructureViolationException;

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

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */
	private static final String FIELDNAME_PROTOCOL = "protocol";	
	private static final String FIELDNAME_FILE = "file";



	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId;
	/*
	 * TODO: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */
	private LinkedList<String> files;	//atributo para guadar los ficheros de fileList reply



	public DirMessage(String op) {
		operation = op;
		files = new LinkedList<String>();
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */

	public DirMessage(String op, String idOfProtocol) {
		operation = op;
		protocolId = idOfProtocol;
	}



	public String getOperation() {
		return operation;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public void setProtocolID(String protocolIdent) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}

	public String getProtocolId() {
		if(!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new StructureViolationException(
					"getProtocolId: this message doesn't have the protocol's Id. Check \'getOperation() == DirMessageOps.OPERATION_PING\' first ");
		}
		return protocolId;
	}
	
	public List<String> getFiles(){
		if(!operation.equals(DirMessageOps.OPERATION_FILELIST_RES)) {
			throw new StructureViolationException(
					"getFiles: this message is not able to contain files. Check \'getOperation() == DirMessageOps.OPERATION_FILELIST_RES\' first ");
		}
		return Collections.unmodifiableList(this.files);
	}
	
	public void setFile(String newFile) {
		if(!operation.equals(DirMessageOps.OPERATION_FILELIST_RES)) {
			throw new StructureViolationException(
					"getFiles: this message is not able to contain files. Check \'getOperation() == DirMessageOps.OPERATION_FILELIST_RES\' first ");
		}
		files.add(newFile);
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
		 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
				case FIELDNAME_OPERATION: 
					assert (m == null);
					m = new DirMessage(value);
					break;
				case FIELDNAME_PROTOCOL:
					m.setProtocolID(value);
					break;
				case FIELDNAME_FILE:
					m.setFile(value);
					break;




				default:
					System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
					System.err.println("Message was:\n" + message);
					System.exit(-1);
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

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo linea a linea
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		switch(operation) {
			case DirMessageOps.OPERATION_PING:
				sb.append(FIELDNAME_PROTOCOL + DELIMITER + protocolId+ END_LINE);
				break;
			case DirMessageOps.OPERATION_FILELIST_RES:
				files.forEach(file-> sb.append(FIELDNAME_FILE + DELIMITER + file + END_LINE));

		}


		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

}
