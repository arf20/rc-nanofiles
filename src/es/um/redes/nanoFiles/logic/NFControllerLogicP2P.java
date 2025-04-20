package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.application.NanoFiles;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/*
	 * TODO: Se necesita un atributo NFServer que actuará como servidor de ficheros
	 * de este peer
	 */
	private NFServer fileServer = null;
	private Thread serverThread = null;

	public final int CHUNK_SIZE = 2097152; // 2MiB
	
	protected NFControllerLogicP2P() {
		
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		if (fileServer != null) {
			System.err.println("File server is already running");
			return true;
		}


		/*
		 * TODO: (Boletín Servidor TCP concurrente) Arrancar servidor en segundo plano
		 * creando un nuevo hilo, comprobar que el servidor está escuchando en un puerto
		 * válido (>0), imprimir mensaje informando sobre el puerto de escucha, y
		 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
		 * y tratadas en este método. Si se produce una excepción de entrada/salida
		 * (error del que no es posible recuperarse), se debe informar sin abortar el
		 * programa
		 * 
		 */
		try {
			fileServer = new NFServer();
		} catch (IOException e) {
			System.out.println("Unable to bind socket");
			return false;
		}

		serverThread = new Thread(fileServer);
		serverThread.start();
			
		return true;
	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			// Este código es inalcanzable: el método 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		try {
			NFConnector nfConnector = new NFConnector(new InetSocketAddress(NFServer.PORT));
			nfConnector.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList       La lista de direcciones de los servidores a
	 *                                los que se conectará
	 * @param targetFileNameSubstring Subcadena del nombre del fichero a descargar
	 * @param localFileName           Nombre con el que se guardará el fichero
	 *                                descargado
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, FileInfo targetFileInfo,
		String localFileName)
	{
		if (serverAddressList.length == 0) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector distinto para establecer una conexión TCP
		 * con cada servidor de ficheros proporcionado, y usar dicho objeto para
		 * descargar trozos (chunks) del fichero. Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre (localFileName) en esta máquina, en
		 * cuyo caso se informa y no se realiza la descarga. Se debe asegurar que el
		 * fichero cuyos datos se solicitan es el mismo para todos los servidores
		 * involucrados (el fichero está identificado por su hash). Una vez descargado,
		 * se debe comprobar la integridad del mismo calculando el hash mediante
		 * FileDigest.computeFileChecksumString. Si todo va bien, imprimir resumen de la
		 * descarga informando de los trozos obtenidos de cada servidor involucrado. Las
		 * excepciones que puedan lanzarse deben ser capturadas y tratadas en este
		 * método. Si se produce una excepción de entrada/salida (error del que no es
		 * posible recuperarse), se debe informar sin abortar el programa
		 */
		
		String filePath = NanoFiles.sharedDirname + "/" + localFileName;
		
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(filePath, "rw");
		} catch (FileNotFoundException e) {
			System.out.println("File exists - not downloading");
			return false;
		}

		ArrayList<NFConnector> connectors = new ArrayList<NFConnector>(serverAddressList.length);
		HashMap<NFConnector, Integer> chunkCounter = new HashMap<NFConnector, Integer>();
		for (int i = 0; i < serverAddressList.length; i++) {
			try {
				 NFConnector nfc = new NFConnector(serverAddressList[i]);
				 connectors.add(nfc);
				 chunkCounter.put(nfc, 0);
			} catch (IOException e) {
				System.out.println("Could not contact peer" + serverAddressList[i]);
			}
		}
		
		PeerMessage filereqMessage = new PeerMessage(PeerMessageOps.OPCODE_FILEREQUEST, targetFileInfo.getHash());
		
		// select file
		PeerMessage responseMessage = null;
		for (Iterator it = connectors.iterator(); it.hasNext(); ) {
			var c = (NFConnector)it.next();
			try {
				c.sendMessage(filereqMessage);
				responseMessage = c.receiveMessage();
			} catch (IOException e) {
				System.out.println("Client died: " + c);
				it.remove();
			}
			
			if (responseMessage.getOpcode() != PeerMessageOps.OPCODE_FILEREQUEST_ACCEPTED) {
				System.out.println("Client does not have file: " + c);
				it.remove();
			}
		}
		
		if (connectors.size() < 1) {
			System.out.println("No peers left - stopping");
			return false;
		}

		/* download chunks */
		int chunks = (int)(targetFileInfo.getSize() / (long)CHUNK_SIZE + (targetFileInfo.getSize() % (long)CHUNK_SIZE == 0 ? 0 : 1));
		
		System.out.println("Downloading " + chunks + " chunks from " + connectors.size() + " peers");
		
		for (int chunk = 0; chunk < chunks;) {
			// usar clientes diferentes para cada chunk, rotandose
			NFConnector conn = connectors.get(chunk % connectors.size());
			
			PeerMessage chunkreqMessage = new PeerMessage(PeerMessageOps.OPCODE_CHUNKREQUEST,
				(long)chunk * CHUNK_SIZE, chunk == chunks - 1 ? (int)(targetFileInfo.getSize() % (long)CHUNK_SIZE) : CHUNK_SIZE);
			
			try {
				conn.sendMessage(chunkreqMessage);
				responseMessage = conn.receiveMessage();
			} catch (IOException e) {
				System.out.println("Client died: " + conn);
				connectors.remove(conn);
				continue;
			}
			
			if (responseMessage.getOpcode() != PeerMessageOps.OPCODE_CHUNK) {
				System.out.println("Peer had an error or bad offset");
				connectors.remove(conn);
				continue;
			}
			
			try {
				file.seek(responseMessage.getOffset());
				file.write(responseMessage.getChunkData());
			} catch (IOException e) {
				System.out.println("Out of space");
				return false;
			}
		
			chunk++;
			chunkCounter.put(conn, chunkCounter.get(conn) + 1);
		}
		
		try {
			file.close();
		} catch (IOException e) {
			return false;
		}
		
		System.out.println("File downloaded.");
		
		if (!FileDigest.computeFileChecksumString(filePath).equals(targetFileInfo.getHash())) {
			System.out.println("File corrupted.");
			return false;
		}
		
		
		for (var conn : connectors) {
			System.out.println("client\t\tchunks");
			System.out.println(conn.getServerAddr() + "\t" + chunkCounter.get(conn));
		}

		return true;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		/*
		 * TODO: Devolver el puerto de escucha de nuestro servidor de ficheros
		 * -----------
		 * HECHO
		 */
		return fileServer.getPort();
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * TODO: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 * ---------
		 * HECHO
		 */
		fileServer.terminate();
	}

	protected boolean serving() {
		if (fileServer == null)
			return false;
		
		return fileServer.isAlive();

	}

	protected boolean uploadFileToServer(FileInfo matchingFile, String uploadToServer) {
		if(!uploadToServer.trim().matches("[\\w.-]+:\\d{1,5}")) {
			System.err.println("La cadena aportada no casa con el formato \"hostname:puerto\"");
			return false;
		}
		String[] serverField = uploadToServer.trim().split(":");
		NFConnector link = null;
		try {
			link = new NFConnector(new InetSocketAddress(serverField[0], Integer.parseInt(serverField[1])));
		} catch (NumberFormatException | IOException e) {
			System.out.println("Could not contact peer " + uploadToServer);
			return false;
		}
		
		PeerMessage msgToPeer = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD, matchingFile.getHash());
		PeerMessage msgFromPeer = null;
		try {
			link.sendMessage(msgToPeer);
			msgFromPeer = link.receiveMessage();
		} catch (IOException e) {
			System.out.println("Client died: " + link);
			link.close(); return false;
		}
		
		if(msgFromPeer.getOpcode() == PeerMessageOps.OPCODE_FILE_ALREADY_EXISTS) {
			System.out.println("The file already exists on the remote host");
			link.close(); return true;
		}
		msgToPeer = new PeerMessage(PeerMessageOps.OPCODE_FILENAME_TO_SAVE, matchingFile.getName().length(), matchingFile.getName());
		try {
			link.sendMessage(msgToPeer);
			msgFromPeer = link.receiveMessage();
		} catch (IOException e) {
			System.out.println("Client died: " + link);
			link.close(); return false;
		}
		
		if (msgFromPeer.getOpcode() != PeerMessageOps.OPCODE_FILEREQUEST_ACCEPTED) {
			System.err.println("The remote path is inaccessible");
			link.close(); return false;
		}
		
		//envio de datos
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(NanoFiles.sharedDirname +"/"+ matchingFile.getName(), "r");
		} catch (FileNotFoundException e) {
			System.err.println("Error when opening " + matchingFile.getName() + " file");
			link.close(); return false;
		}
		
		int chunks = (int)(matchingFile.getSize() / (long)CHUNK_SIZE + (matchingFile.getSize() % (long)CHUNK_SIZE == 0 ? 0 : 1));
		
		for (int chunk=0; chunk < chunks; chunk++) {
			byte[] data = new byte[chunk == chunks -1 ? (int) (matchingFile.getSize() % (long)CHUNK_SIZE) : CHUNK_SIZE];
			
			try {
				file.readFully(data);			
				msgToPeer = new PeerMessage(PeerMessageOps.OPCODE_CHUNK, (long)chunk*CHUNK_SIZE, data.length, data);
				link.sendMessage(msgToPeer);
			} catch (IOException e) {
				e.printStackTrace(); link.close(); return false;
			}
		}
		
		try {
			link.sendMessage(new PeerMessage(PeerMessageOps.OPCODE_STOP));
		} catch (IOException e) {
			System.out.println("WARNING: ERROR TO CLOSE SERVER CONNECTION");
		}
		
		System.out.println("Upload completed successfuly");
		try {
			file.close();
		} catch (IOException e) {}
		
		link.close();

		return true;
	}

}
