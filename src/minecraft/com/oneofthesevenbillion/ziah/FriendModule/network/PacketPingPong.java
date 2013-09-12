package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.FriendModule.exception.NotRunningException;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketPingPong extends Packet {
	public boolean isResponse;

    public PacketPingPong(boolean isResponse) {
        this.packetID = 4;
        this.isResponse = isResponse;
    }

    public static Packet process(NetworkManager netManager, String sender, DataInputStream dataStream) {
        PacketPingPong packet = new PacketPingPong(false);
        try {
            packet.read(dataStream);
        } catch (EOFException e) {
            return null;
        }

        if (packet.isResponse) {
        	if (!ModuleFriend.getInstance().getIPs().contains(sender)) ModuleFriend.getInstance().getIPs().add(sender);
	        if (!ModuleFriend.getInstance().getOnlineIPs().contains(sender)) ModuleFriend.getInstance().getOnlineIPs().add(sender);
	        try {
				PacketManager.sendPacket(sender, new PacketServeFriend(ModuleFriend.getInstance().getPlayer()));
			} catch (Exception e) {
				ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when serving friend!", e);
			}
	        try {
				PacketManager.sendPacket(sender, new PacketServeIPs(ModuleFriend.getInstance().getIPs()));
			} catch (Exception e) {
				ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when serving ips!", e);
			}
        }else{
        	try {
				PacketManager.sendPacket(sender, new PacketPingPong(true));
			} catch (Exception e) {
				ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when sending pong!", e);
			}
        }
        return packet;
    }

    @Override
    public void read(DataInputStream dataStream) throws EOFException {
        super.read(dataStream);
        try {
        	this.isResponse = dataStream.readBoolean();
        } catch (Exception e) {
            if (e instanceof EOFException) throw (EOFException) e;
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when decoding ping pong packet.", e);
        }
    }

    @Override
    public void write(DataOutputStream dataStream) {
        super.write(dataStream);
        PacketManager.encodeDataStream(dataStream, this.isResponse);
    }
}