package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;




public class NFServer implements Runnable {
	public static final int PORT = 10000;

	private int ephemeralPort = 0;
	
	private ServerSocket serverSocket = null;
	
	private static boolean alive = false;
	
	private static LinkedList<Socket> sockets = new LinkedList<Socket>();	//lista para cerrar todos los sockes con quit. 

	public NFServer() throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		InetSocketAddress serverSocketAddress = new InetSocketAddress(ephemeralPort);
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		serverSocket = new ServerSocket();
		serverSocket.bind(serverSocketAddress);
		ephemeralPort = serverSocket.getLocalPort();
		alive = true;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void terminate() {
		alive = false;
		sockets.forEach((s) -> {
				try { s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				});		
		try {	
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getPort() {
		return ephemeralPort;
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			/*
			 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			/*boolean connectionOk = false;
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				// FALTA COMPLETAR...
			} finally { // BORRAR FINALLY
				
			}*/
			/*
			 * TODO: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */



		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		Socket clientSocket = null;
		while (alive) {
			try {
				clientSocket = serverSocket.accept();
				sockets.add(clientSocket);
				NFServerThread clientThread = new NFServerThread(clientSocket);
				clientThread.start();
			} catch (IOException e) {
				System.out.println("Error accepting client - server terminated");
				return;
			}
		}
		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */
	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */

	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */

		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Unable to read client message - terminate connection");
			return;
		}
		
		FileInfo reqFileInfo = null;
		FileInputStream fileStream = null;
		
		while (alive) {
			PeerMessage peerMessage = null, messageToSend = null;
			try {
				peerMessage = PeerMessage.readMessageFromInputStream(dis);
			} catch (IOException e) {
				System.out.println("Unable to read client message - terminate connection");
				return;
			}
			
			System.out.println("Recibido mensaje del peer " + PeerMessageOps.opcodeToOperation(peerMessage.getOpcode()));
			
			switch (peerMessage.getOpcode()) {
			case PeerMessageOps.OPCODE_FILEREQUEST: {
				var files = NanoFiles.db.getFiles();
				reqFileInfo = FileInfo.lookupHash(files, peerMessage.getReqFileHash());
				if (reqFileInfo == null) {
					messageToSend = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
					break;
				}
				File reqFile = new File(reqFileInfo.getPath());
				try {
					fileStream = new FileInputStream(reqFile);
				} catch (FileNotFoundException e) {
					System.out.println("File not found" + reqFile);
					messageToSend = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
					break;
				}
				messageToSend = new PeerMessage(PeerMessageOps.OPCODE_FILEREQUEST_ACCEPTED);
			} break;
			case PeerMessageOps.OPCODE_CHUNKREQUEST: {
				if (fileStream == null) {
					messageToSend = new PeerMessage(PeerMessageOps.OPCODE_CHUNKREQUEST_OUTOFRANGE);
					break;
				}
				
				byte[] chunk = null;
				try {
					fileStream.getChannel().position(peerMessage.getOffset());
					DataInputStream fdis = new DataInputStream(fileStream);
					// literalmente no hay forma en java de hacer esto sin copiar el bufer XD
					chunk = fdis.readNBytes(peerMessage.getSize());
				} catch (IOException e) {
					messageToSend = new PeerMessage(PeerMessageOps.OPCODE_CHUNKREQUEST_OUTOFRANGE);
					break;
				}
				
				messageToSend = new PeerMessage(PeerMessageOps.OPCODE_CHUNK, peerMessage.getOffset(), chunk.length, chunk);
			} break;
			}
			
			
			
			System.out.println("Enviando " + PeerMessageOps.opcodeToOperation(messageToSend.getOpcode()));
			try {
				messageToSend.writeMessageToOutputStream(dos);
			} catch (IOException e) {
				System.out.println("Unable to send message - terminate connection");
			}
		}
		try {
			fileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}