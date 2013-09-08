package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;

public class ThreadPing extends Thread {
	private String ip;

	public ThreadPing(String ip) {
		this.ip = ip;
	}

	@Override
	public void run() {
		try {
			ModuleFriend.getInstance().getOnlineIPs().remove(this.ip);
			PacketManager.sendPacket(this.ip, new PacketPingPong(false));
		} catch (Exception e) {
			// Shouldn't happen
		}

		try {
			long startTime = System.currentTimeMillis();
			if (InetAddress.getByName(this.ip).isReachable(10000)) {
				long endTime = System.currentTimeMillis();
				if (!ModuleFriend.getInstance().getNetOnlineIPs().contains(this.ip)) ModuleFriend.getInstance().getNetOnlineIPs().add(this.ip);
				ModuleFriend.getInstance().getIPNetPings().put(this.ip, endTime - startTime);
			}
		} catch (Exception e) {
			if (ModuleFriend.getInstance().getNetOnlineIPs().contains(this.ip)) ModuleFriend.getInstance().getNetOnlineIPs().remove(this.ip);
		}
	}
}