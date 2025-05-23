package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;
import es.um.redes.nanoFiles.util.ExternFile;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	private HashMap<String, ExternFile> database;
	private HashMap<InetSocketAddress, HashSet<FileInfo>> peers;

	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 * -----------------
		 * HECHO
		 */
		socket = new DatagramSocket(DIRECTORY_PORT);
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.)
		 */
		database = new HashMap<>();
		peers = new HashMap<>();
		


		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * TODO: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 * ---------------
			 * HECHO
			 */
			byte[] dataReceived = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(dataReceived, dataReceived.length);
			/*
			 * TODO: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 * -------------
			 * HECHO
			 */
			socket.receive(datagramReceivedFromClient);
			if (datagramReceivedFromClient == null) {
				System.err.println("[testMode] NFDirectoryServer.receiveDatagram: code not yet fully functional.\n"
						+ "Check that all TODOs have been correctly addressed!");
				System.exit(-1);
			} else {
				// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println(
						"Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
				} else {
					datagramReceived = true;
					System.out.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
						+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
				}
			}

		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 * ----------------
		 * HECHO
		 */
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);
		/*
		 * TODO: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 * ------------
		 * HECHO
		 */
		byte[] responce = null;
		DatagramPacket responceToClient;
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();	//se extrae la dierección del cliente.
		if (messageFromClient.equals("ping")) {
			responce  = (messageFromClient+"ok").getBytes();
			System.out.println("Sending \""+messageFromClient+"ok\"");
		}
		else if(messageFromClient.startsWith("ping&")) {
			int codeBeginingIndex = 5;
			String code = messageFromClient.substring(codeBeginingIndex);	// Se saca el código de la cadena.
			if(code.equals(NanoFiles.PROTOCOL_ID)){
				responce = "welcome".getBytes();
				System.out.println("Sending \"welcome\"");				
			}
			else	responce = "denied".getBytes();
		}
		else {
			System.err.println("Error, \"ping\" expected");
			responce = "invalid".getBytes();
		}
		responceToClient = new DatagramPacket(responce, responce.length, clientAddr);
		socket.send(responceToClient);
		/*
		 * TODO: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 * ----------------
		 * HECHO
		 */
	}

	public void run() throws IOException {
		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();
			sendResponse(rcvDatagram);
		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		DatagramPacket responceToClient;
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();	//se extrae la dierección del cliente.
		/*
		 * TODO: (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 *--------------
		 * HECHO
		 */
		String data = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Message received from client: " + data);
		DirMessage clientMessage = DirMessage.fromString(data);

		/*
		 * TODO: Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 * 
		 */
		String operation = clientMessage.getOperation();

		/*
		 * TODO: (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */
		DirMessage msgToSend = null;
		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
			/*
			 * TODO: (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
			 * cliente coincide con el nuestro.
			 * ---------
			 * HECHO
			 */
			if (clientMessage.getProtocolId() != null && clientMessage.getProtocolId().equals(NanoFiles.PROTOCOL_ID)) {
			/*
			 * TODO: (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
			 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
			 * resultado del método.
			 *  ---------
			 * HECHO
			 */
			/*
			 * TODO: (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
			 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
			 * modo de depuración en el servidor
			 *  ---------
			 * HECHO
			 */
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_OK);
				System.out.println("Sending: "+ DirMessageOps.OPERATION_PING_OK + "...");
			}
			else {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_BAD);
				System.out.println("Sending: "+ DirMessageOps.OPERATION_PING_BAD + "...");
			}
		} break;
		case DirMessageOps.OPERATION_FILELIST: {
			HashMap<String, HashSet<String>> peersForFile = new HashMap<String, 
					HashSet<String>>();
			
			for (var file : database.keySet()) {
				peersForFile.put(file, new HashSet<>());
				InetSocketAddress[] servers = database.get(file).getServers();
				for(var s : servers) {
					peersForFile.get(file).add(s.getHostName() + ":" + s.getPort());
				}
			}
			
			msgToSend = new DirMessage(DirMessageOps.OPERATION_FILELIST_RES, 
					new HashSet<FileInfo>(database.values()), peersForFile);
			
		} break;

		case DirMessageOps.OPERATION_PUBLISH: {
			var files = clientMessage.getFiles();
			int port = NFServer.PORT;
			if (clientMessage.getPort() != 0)
				port = clientMessage.getPort();
			
			if (files.isEmpty()) {
				var list = peers.get(clientAddr); 
				for(var file : list) {
					database.get(file.getHash()).deleteServer(clientAddr.getHostName(), port);
					if (database.get(file.getHash()).getServers().length == 0) {
						database.remove(file.getHash());
					}
				}			
				peers.remove(clientAddr);
			} else {
				for (var file : files) {
					if (database.containsKey(file.getHash())) {
						database.get(file.getHash()).insertServer(clientAddr.getHostName(), port);
					} else {
						ExternFile newFile = new ExternFile(file); 
						newFile.insertServer(clientAddr.getHostName(), port);
						database.put(file.getHash(), newFile);
					}
				}
				
				peers.put(new InetSocketAddress(clientAddr.getHostName(), clientAddr.getPort()), new HashSet<FileInfo>(files));
			}

			msgToSend = new DirMessage(DirMessageOps.OPERATION_PUBLISH_RES);
			System.out.println("Sending: "+ DirMessageOps.OPERATION_PUBLISH_RES + "...");
		} break;

		default:
			System.err.println("Unexpected message operation: \"" + operation + "\"");
			// System.exit(-1); // nice denial of service there, let me comment that for you
		}

		/*
		 * TODO: (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagrama
		 * ----------
		 * HECHO
		 */
		byte[] dataToSend = msgToSend.toString().getBytes();	// Se transforma el mensaje a String y de String a bytes.
		responceToClient = new DatagramPacket(dataToSend, dataToSend.length, clientAddr);
		socket.send(responceToClient);
	}
}
