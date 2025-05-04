package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;

public class ThreadMonitor {
	
	private HashMap<NFConnector, Integer> chunkCounter;
//	private ArrayList<NFConnector> connectors;
	private long fileSize;
	private RandomAccessFile file;
	private int currentChunk;	//El chunk que se pide
	private int savedChunks;			//Cuenta de los chunks guardados
	private long nextOffset;	//Posicion del siguiente chunk a guardar
	private int totalChunks;	//total chunks a descargar
	
	/*constructor*/
	public ThreadMonitor(Map<NFConnector, Integer> threadRecord, RandomAccessFile file, long fileSize) {
		chunkCounter = (HashMap<NFConnector, Integer>) threadRecord;
	//	connectors = (ArrayList<NFConnector>) servers;
		this.file = file;
		this.fileSize = fileSize;
		currentChunk = savedChunks = 0;
		nextOffset= 0;
		totalChunks = (int)(fileSize / (long)NFControllerLogicP2P.CHUNK_SIZE + (fileSize % (long)NFControllerLogicP2P.CHUNK_SIZE == 0 ? 0 : 1));

	}
	
	synchronized public PeerMessage getChunk() {
		if (currentChunk == totalChunks) {
			return new PeerMessage(PeerMessageOps.OPCODE_STOP);
		}
		PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_CHUNKREQUEST,
				(long)currentChunk * NFControllerLogicP2P.CHUNK_SIZE, 
				currentChunk == totalChunks - 1 ? (int)(fileSize % (long)NFControllerLogicP2P.CHUNK_SIZE ) : NFControllerLogicP2P.CHUNK_SIZE );
		currentChunk++;
		return msg;			
	}
	
	synchronized public boolean pushChunk(NFConnector thread, PeerMessage chunk) {
		while(chunk.getOffset() != nextOffset) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		try {
			file.seek(chunk.getOffset());
			file.write(chunk.getChunkData());
			savedChunks++;
		} catch (IOException e) {
			System.out.println("Out of space");
			return false;
		}
		nextOffset = (long)savedChunks * NFControllerLogicP2P.CHUNK_SIZE;
		chunkCounter.put(thread, chunkCounter.get(thread) + 1);
		notifyAll();
		/*if (savedChunks == totalChunks) {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		return true;
	}

}
