package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketServeIPs extends Packet {
    public List<String> ips;

    public PacketServeIPs(List<String> ips) {
        this.packetID = 4;
        this.ips = ips;
    }

    public static Packet process(FriendServerNetworkManager netManager, String sender, DataInputStream dataStream) {
        PacketServeIPs packet = new PacketServeIPs(null);
        try {
            packet.read(dataStream);
        }catch (EOFException e) {
            return null;
        }
        System.out.println("Received serve ips packet from " + sender);
        ModuleFriend.getInstance().getIPs().removeAll(packet.ips);
        ModuleFriend.getInstance().getIPs().addAll(packet.ips);
        for (String ip : new ArrayList<String>(ModuleFriend.getInstance().getIPs())) {
        	try {
				if (InetAddress.getByName(ip).isLoopbackAddress()) {
					ModuleFriend.getInstance().getIPs().remove(ip);
				}
			} catch (UnknownHostException e) {
				// Friend server is unknown
			}
        }
        ModuleFriend.getInstance().saveIPs();
        return packet;
    }

    @Override
    public void read(DataInputStream dataStream) throws EOFException {
        super.read(dataStream);
        try {
        	this.ips = new ArrayList<String>();
        	int length = dataStream.readInt();
        	for (int i = 0; i < length; i++) {
        		this.ips.add(dataStream.readUTF());
        	}
        } catch (Exception e) {
            if (e instanceof EOFException) throw (EOFException) e;
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when decoding serve ips packet.", e);
        }
    }

    @Override
    public void write(DataOutputStream dataStream) {
        super.write(dataStream);
        try {
	        dataStream.writeInt(this.ips.size());
	        for (String ip : this.ips) {
	        	dataStream.writeUTF(ip);
	        }
        } catch (Exception e) {
        	ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when writing serve ips packet!", e);
        }
    }
}
