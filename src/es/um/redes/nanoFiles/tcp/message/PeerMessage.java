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
import java.util.Arrays;
import java.util.concurrent.StructureViolationException;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	//Campo con la longitud en bytes del nombre de fichero en una petición de descarga.
	private byte longFileName; 
	//Campo con el nombre del fichero que se quiere descargar.
	private byte[] fileName;



	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	public PeerMessage(byte op, String fileName) {
		opcode = op;
		this.fileName = fileName.getBytes();
		longFileName = (byte) this.fileName.length;
	}

	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	public byte[] getFileName() {
		if (opcode != PeerMessageOps.OPCODE_FILEREQUEST) {
			throw new StructureViolationException("This instance does not support getFileName. Check \'getOpcode() == PeerMessageOps.OPCODE_FILEREQUEST\' first");
		}
		return Arrays.copyOf(fileName, longFileName);
	}
	
	public byte getLongFileName () {
		if (opcode != PeerMessageOps.OPCODE_FILEREQUEST)
			throw new StructureViolationException("This instance does not support getLongFileName. Check \'getOpcode() == PeerMessageOps.OPCODE_FILEREQUEST\' first");
		return longFileName;
	}
	
	private void setFileName(byte[] newName) {
		if (opcode != PeerMessageOps.OPCODE_FILEREQUEST) 
			throw new StructureViolationException("This instance does not have the field fileName");
		fileName = newName;
	}

	private void setLongFileName(byte size) {
		if (opcode != PeerMessageOps.OPCODE_FILEREQUEST) 
			throw new StructureViolationException("This instance does not have the field longFileName");
		longFileName = size;
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
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		switch (opcode) {
			case PeerMessageOps.OPCODE_FILEREQUEST:
				message.opcode = opcode;
				message.setLongFileName(dis.readByte());
				byte[] nameBytes = new byte[message.getLongFileName()];
				dis.readFully(nameBytes);
				message.setFileName(nameBytes);
				break;



		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para escribir un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
			case PeerMessageOps.OPCODE_FILEREQUEST:
				dos.writeByte(longFileName);
				dos.write(fileName);
				break;
			


		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}




}
