package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;

public class ThreadFindHostname extends Thread {
	private String ip;
	private Method callback;
	private Object callbackObj;

	public ThreadFindHostname(String ip, Method callback, Object callbackObj) {
		this.ip = ip;
		this.callback = callback;
		this.callbackObj = callbackObj;
	}

	@Override
	public void run() {
		try {
			InetAddress addr = InetAddress.getByName(this.ip);
        	String hostname = addr.getHostName();
			try {
				this.callback.invoke(this.callbackObj, hostname.equalsIgnoreCase(this.ip) ? null : hostname);
			} catch (Exception e) {
				// Ignore exceptions from callback method
			}
		} catch (UnknownHostException e) {
			// Can't get hostname if the host is unknown
		}
	}
}