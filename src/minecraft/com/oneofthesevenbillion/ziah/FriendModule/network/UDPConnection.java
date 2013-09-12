package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;

public class UDPConnection implements Connection {
	private DatagramSocket socket;
	private InetAddress ip;

    public UDPConnection(InetAddress ip) throws SocketException {
    	this(new DatagramSocket(NetworkManager.getInstance().getPort()), ip);
    }

    public UDPConnection(DatagramSocket socket, InetAddress ip) {
    	this.socket = socket;
    	this.ip = ip;
    	System.out.println("New Connection to: " + ip.getHostAddress());
    }

	@Override
	public void sendData(byte[] data) throws IOException {
		System.out.println("Sending data to " + this.ip.getHostAddress() + ": " + data);
		this.socket.send(new DatagramPacket(data, data.length, this.ip, NetworkManager.getInstance().getPort()));
	}

	@Override
	public void receiveData(byte[] data) throws IOException {
		System.out.println("Receiving " + data.length + " bytes from " + this.ip.getHostAddress());
		this.socket.receive(new DatagramPacket(data, data.length, this.ip, NetworkManager.getInstance().getPort()));
	}
}