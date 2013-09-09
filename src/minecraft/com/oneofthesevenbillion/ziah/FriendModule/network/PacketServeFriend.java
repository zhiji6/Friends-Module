package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.FriendModule.Friend;
import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketServeFriend extends Packet {
    public Friend friend;

    public PacketServeFriend(Friend friend) {
        this.packetID = 2;
        this.friend = friend;
    }

    public static Packet process(FriendServerNetworkManager netManager, String sender, DataInputStream dataStream) {
        PacketServeFriend packet = new PacketServeFriend(null);
        try {
            packet.read(dataStream);
        } catch (EOFException e) {
            return null;
        }

        packet.friend.setIP(sender);

        for (Friend friend : ModuleFriend.getInstance().getAvailableFriends()) {
        	if (friend.getUsername().equals(packet.friend.getUsername())) {
        		ModuleFriend.getInstance().getAvailableFriends().remove(friend);
        		break;
        	}
        }
        if (!ModuleFriend.getInstance().getPlayer().getUsername().equalsIgnoreCase(packet.friend.getUsername())) ModuleFriend.getInstance().getAvailableFriends().add(packet.friend);
        return packet;
    }

    @Override
    public void read(DataInputStream dataStream) throws EOFException {
        super.read(dataStream);
        try {
            this.friend = Friend.readFromStream(dataStream);
        } catch (Exception e) {
            if (e instanceof EOFException) throw (EOFException) e;
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when decoding serve friend packet.", e);
        }
    }

    @Override
    public void write(DataOutputStream dataStream) {
        super.write(dataStream);
        this.friend.writeToStream(dataStream);
    }
}
