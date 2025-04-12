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
	// Campo con el hash del archivo en una petición de descarga
	private byte[] reqFileHash;
	private long offset;
	private int size;
	private byte[] chunkData;

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	public PeerMessage(byte op, String reqFileHash) {
		opcode = op;
		this.reqFileHash = hexStringToBytes(reqFileHash);
	}
	
	public PeerMessage(byte op, byte[] chunkData) {
		opcode = op;
		this.chunkData = chunkData;
	}
	
	public PeerMessage(byte opcode, long offset, int size ) {
		this.opcode = opcode;
		this.offset = offset;
		this.size = size;
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
	
	public String getReqFileHash() {
		if (opcode != PeerMessageOps.OPCODE_FILEREQUEST) {
			throw new StructureViolationException("This instance does not support getFileName. Check \'getOpcode() == PeerMessageOps.OPCODE_FILEREQUEST\' first");
		}
		return bytesToHex(reqFileHash);
	}
	
	private void setReqFileHash(String reqFileHash) {
		if (opcode != PeerMessageOps.OPCODE_FILEREQUEST) 
			throw new StructureViolationException("This instance does not have the field fileName");
		this.reqFileHash = hexStringToBytes(reqFileHash);
	}
	
	private void setReqFileHash(byte[] reqFileHash) {
		if (opcode != PeerMessageOps.OPCODE_FILEREQUEST) 
			throw new StructureViolationException("This instance does not have the field fileName");
		this.reqFileHash = reqFileHash;
	}
	
	public long getOffset() {
		return this.offset;
	}
	
	private void setOffset(long offset) {
		this.offset = offset;
	}
	
	public int getSize() {
		return this.size;
	}
	
	private void setSize(int size) {
		this.size = size;
	}
	
	public byte[] getChunkData() {
		return chunkData;
	}
	
	private static byte[] hexStringToBytes(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

	public static String bytesToHex(byte[] bytes) {
		final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars).toLowerCase();
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
		message.opcode = opcode;
		switch (opcode) {
			case PeerMessageOps.OPCODE_FILEREQUEST: {
				message.setReqFileHash(dis.readNBytes(20));
			} break;
			case PeerMessageOps.OPCODE_CHUNKREQUEST: {
				message.opcode = opcode;
				message.setOffset(dis.readLong());
				message.setSize(dis.readInt());
			} break;
		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: " + opcode);
			//System.exit(-1); // esto es Denial of Service
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
			case PeerMessageOps.OPCODE_FILEREQUEST: {
				dos.write(reqFileHash);
			} break;
			case PeerMessageOps.OPCODE_CHUNKREQUEST:
			case PeerMessageOps.OPCODE_CHUNK: {
				dos.writeLong(offset);
				dos.writeInt(size);
				dos.write(chunkData);
			} break;
			case PeerMessageOps.OPCODE_STOP:
			case PeerMessageOps.OPCODE_FILEREQUEST_ACCEPTED:
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			case PeerMessageOps.OPCODE_CHUNKREQUEST_OUTOFRANGE:
				break;
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode);
		}
	}
}
