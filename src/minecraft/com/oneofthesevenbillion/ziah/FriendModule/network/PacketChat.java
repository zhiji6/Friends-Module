package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketChat extends Packet {
    public String username;
    public String message;

    public PacketChat(String username, String message) {
        this.packetID = 0;
        this.username = username;
        this.message = message;
    }

    public static Packet process(FriendServerNetworkManager netManager, String sender, DataInputStream dataStream) {
        PacketChat packet = new PacketChat(null, null);
        try {
            packet.read(dataStream);
        }catch (EOFException e) {
            return null;
        }
        ModuleFriend.getInstance().receivedChatMessage(packet.username, packet.message);
        return packet;
    }

    @Override
    public void read(DataInputStream dataStream) throws EOFException {
        super.read(dataStream);
        try {
            this.username = dataStream.readUTF();
            this.message = dataStream.readUTF();
        }catch (Exception e) {
            if (e instanceof EOFException) throw (EOFException) e;
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when decoding chat packet.", e);
        }
    }

    @Override
    public void write(DataOutputStream dataStream) {
        super.write(dataStream);
        PacketManager.encodeDataStream(dataStream, this.username, this.message);
    }
}
