package es.um.redes.nanoFiles.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

/**
 * @author mar_sang
 * 
 * 		Clase descendiente de FileInfo que permite registrar
 * 		los servidores de ficheros remotos. 
 */
public class ExternFile extends FileInfo{
	private HashSet<String> servers;
	
	public ExternFile() {
	}
	
	public ExternFile(String hash, String name, long size, String path) {
		super(hash, name, size, path);
		servers = new HashSet<String>(1);
		servers.add("Unknown Origin");
	}
	
	public ExternFile(String hash, String name, long size, String path, String... servers ) {
		super(hash, name, size, path);
		this.servers = new HashSet<String>();
		for (var s : servers)
			this.servers.add(s);		
	}
	
	public ExternFile(FileInfo file ) {
		super(file.getHash(), file.getName(), file.getSize(), file.getPath());
		this.servers = new HashSet<String>();		
	}
	
	/**
	 * Obtine los hosts remotos que sirven este archivo.
	 * @return Array con obejetos InetSocketAddress. 
	 */
	public InetSocketAddress[] getServers() {
		HashSet<InetSocketAddress> peerSet = new HashSet<InetSocketAddress>();
		for (var peer : servers) {
			String[] peerfields = peer.split(":");
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(peerfields[0]);
			} catch (UnknownHostException e) {
				continue;
			}
			int port = Integer.parseInt(peerfields[1]);
			peerSet.add(new InetSocketAddress(addr, port));
		}

		return peerSet.toArray(new InetSocketAddress[0]);
	}
	
	/**
	 * Añade un nuevo servidor a la lista de servidores actual.
	 * @param hostname
	 * @param port
	 */
	public void insertServer(String hostname, int port) {
		servers.add(hostname + ":" + port);
	}
	
	/**
	 * Elimina un host de la lista de servidores de este fichero.
	 * @param host
	 */
	public void deleteServer(String host) {
		servers.remove(host);
	}
	
	/**
	 * Elimina un host de la lista de servidores de este fichero.
	 * @param host
	 */
	public void deleteServer(InetSocketAddress host) {
		servers.remove(host.getHostName() + ":" + host.getPort());
	}
}
