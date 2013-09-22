package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.IOException;

public interface Connection {
	/**
	 * Sends data
	 * 
	 * @param data
	 */
	public void sendData(byte[] data) throws IOException;

	/**
	 * Receives as much data as can fit into the data array
	 * 
	 * @param data
	 */
	public void receiveData(byte[] data) throws IOException;
}