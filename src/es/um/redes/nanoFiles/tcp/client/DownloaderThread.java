package es.um.redes.nanoFiles.tcp.client;

import java.io.IOException;

import es.um.redes.nanoFiles.logic.ThreadMonitor;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;

public class DownloaderThread extends Thread{
	private ThreadMonitor monitor;
	private NFConnector connector;
	
	/*constructor*/
	public DownloaderThread(NFConnector conector, ThreadMonitor monitor) {
		this.connector = conector;
		this.monitor = monitor;
	}
	
	public void run() {
		while(true) {
			PeerMessage chunkRequest = monitor.getChunk();
			PeerMessage responseMessage = null;
			if (chunkRequest.getOpcode() == PeerMessageOps.OPCODE_STOP) {
				connector.close();
				break;
			}
			try {
				connector.sendMessage(chunkRequest);
				responseMessage = connector.receiveMessage();
			} catch (IOException e) {
				System.out.println("Client died: " + connector);
				break;
			}
			
			if (responseMessage.getOpcode() != PeerMessageOps.OPCODE_CHUNK) {
				System.out.println("Peer had an error or bad offset");
				connector.close();
				break;
			}
			
			if(!monitor.pushChunk(connector, responseMessage)) {
				connector.close();
				break;
			}
		}
	}
}
