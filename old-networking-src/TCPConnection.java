package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;

public class TCPConnection implements Connection {
	private Socket socket;
	private InetAddress ip;
	private OutputStream out;
	private InputStream in;

    public TCPConnection(InetAddress ip) throws IOException {
    	this.socket = new Socket();
    	this.ip = ip;
    	this.socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), NetworkManager.getInstance().getPort()));
    	this.socket.connect(new InetSocketAddress(ip, NetworkManager.getInstance().getPort()), 0);
    	this.out = this.socket.getOutputStream();
    	this.in = this.socket.getInputStream();
    	System.out.println("New Connection to: " + ip.getHostAddress());
    }

	@Override
	public void sendData(byte[] data) throws IOException {
		System.out.println("Sending data to " + this.ip.getHostAddress() + ": " + data);
		this.out.write(data);
	}

	@Override
	public void receiveData(byte[] data) throws IOException {
		System.out.println("Receiving " + data.length + " bytes from " + this.ip.getHostAddress());
		this.in.read(data);
	}
}