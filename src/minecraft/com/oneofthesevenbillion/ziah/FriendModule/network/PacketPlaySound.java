package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.logging.Level;

import net.minecraft.src.Minecraft;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketPlaySound extends Packet {
    public String soundname;
    public float volume;
    public float pitch;

    public PacketPlaySound(String soundname, float volume, float pitch) {
        this.packetID = 1;
        this.soundname = soundname;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static Packet process(FriendServerNetworkManager netManager, String sender, DataInputStream dataStream) {
        PacketPlaySound packet = new PacketPlaySound(null, 0.0F, 0.0F);
        try {
            packet.read(dataStream);
        }catch (EOFException e) {
            return null;
        }
        Minecraft.getMinecraft().sndManager.playSound(packet.soundname, (float) Minecraft.getMinecraft().thePlayer.posX, (float) Minecraft.getMinecraft().thePlayer.posY, (float) Minecraft.getMinecraft().thePlayer.posZ, packet.volume, packet.pitch);
        return packet;
    }

    @Override
    public void read(DataInputStream dataStream) throws EOFException {
        super.read(dataStream);
        try {
            this.soundname = dataStream.readUTF();
            this.volume = dataStream.readFloat();
            this.pitch = dataStream.readFloat();
        }catch (Exception e) {
            if (e instanceof EOFException) throw (EOFException) e;
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when decoding play sound packet.", e);
        }
    }

    @Override
    public void write(DataOutputStream dataStream) {
        super.write(dataStream);
        PacketManager.encodeDataStream(dataStream, this.soundname, this.volume, this.pitch);
    }
}