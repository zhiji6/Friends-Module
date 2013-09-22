package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class NetworkConnectionManager {
	private Map<String, Connection> connections = new HashMap<String, Connection>();
	private boolean isCreating = false;
	//private DatagramSocket socket;
	private int no = 0;

	public Connection getConnectionForAddress(String addr) {
		this.no++;
		if (this.isCreating) System.out.println(this.no + " Trying to get connection for " + addr + ", waiting for other connection creations to finish");

		while (this.isCreating) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}

		this.isCreating = true;

		System.out.println(this.no + " Trying to get connection for " + addr);
		if (!this.connections.containsKey(addr)) {
			System.out.println(this.no + " Trying to create connection.");
			try {
				this.connections.put(addr, this.createNewConnection(addr));
				System.out.println(this.no + " Created connection.");
			} catch (Exception e) {
				System.out.println(this.no + " Creating connection failed");
				e.printStackTrace();
				this.isCreating = false;
				return null;
			}
		}

		this.isCreating = false;
		System.out.println(this.no + " Done creating.");
		return this.connections.get(addr);
	}

	private Connection createNewConnection(String addr) throws IOException, UnknownHostException {
		//if (this.socket == null) this.socket = new DatagramSocket(NetworkManager.getInstance().getPort());
		return new TCPConnection(Inet4Address.getByName(addr));//new UDPConnection(this.socket, Inet4Address.getByName(addr));
	}
}