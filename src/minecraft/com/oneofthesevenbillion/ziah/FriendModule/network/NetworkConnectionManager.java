package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class NetworkConnectionManager {
	private Map<String, Connection> connections = new HashMap<String, Connection>();
	private boolean isCreating = false;

	public Connection getConnectionForAddress(String addr) {
		if (this.isCreating) System.out.println("Trying to get connection for " + addr + ", waiting for other connection creations to finish");

		while (this.isCreating) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}

		this.isCreating = true;

		System.out.println("Trying to get connection for " + addr);
		if (!this.connections.containsKey(addr)) {
			System.out.println("Trying to create connection.");
			try {
				this.connections.put(addr, this.createNewConnection(addr));
				System.out.println("Created connection.");
			} catch (Exception e) {
				System.out.println("Creating connection failed");
				e.printStackTrace();
				this.isCreating = false;
				return null;
			}
		}

		this.isCreating = false;
		return this.connections.get(addr);
	}

	private Connection createNewConnection(String addr) throws SocketException, UnknownHostException {
		return new UDPConnection(Inet4Address.getByName(addr));
	}
}