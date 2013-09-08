package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

public class Packet {
    public int packetID;

    public Packet() {
    }

    public void write(DataOutputStream dataStream) {
    	System.out.println("Sending " + this.getClass().getSimpleName() + " packet");
        PacketManager.encodeDataStream(dataStream, this.packetID);
    }

    public void read(DataInputStream datainputstream) throws EOFException {}
}