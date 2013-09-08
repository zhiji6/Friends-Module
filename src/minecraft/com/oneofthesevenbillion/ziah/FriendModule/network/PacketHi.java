package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketHi extends Packet {
    public PacketHi() {
        this.packetID = 3;
    }

    public static Packet process(FriendServerNetworkManager netManager, String sender, DataInputStream dataStream) {
        PacketHi packet = new PacketHi();
        try {
            packet.read(dataStream);
        } catch (EOFException e) {
            return null;
        }
        System.out.println("Received hi packet from " + sender + ", sending serve packets");
        try {
			PacketManager.sendPacket(sender, new PacketServeFriend(ModuleFriend.getInstance().getPlayer()));
		} catch (NotConnectedException e) {
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when serving friend!", e);
		}
        try {
			PacketManager.sendPacket(sender, new PacketServeIPs(ModuleFriend.getInstance().getIPs()));
		} catch (NotConnectedException e) {
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when serving ips!", e);
		}
        return packet;
    }
}