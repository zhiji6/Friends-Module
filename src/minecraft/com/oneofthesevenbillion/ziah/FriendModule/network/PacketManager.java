package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketManager {
    public static DataOutputStream encodeDataStream(DataOutputStream data, Object... sendData) {
        try {
            Object aobj[];
            int j = (aobj = sendData).length;
            for (int i = 0; i < j; i++) {
                Object dataValue = aobj[i];
                if (dataValue instanceof Integer) data.writeInt(((Integer) dataValue).intValue());
                else if (dataValue instanceof Float) data.writeFloat(((Float) dataValue).floatValue());
                else if (dataValue instanceof Double) data.writeDouble(((Double) dataValue).doubleValue());
                else if (dataValue instanceof Byte) data.writeByte(((Byte) dataValue).byteValue());
                else if (dataValue instanceof Boolean) data.writeBoolean(((Boolean) dataValue).booleanValue());
                else if (dataValue instanceof String) data.writeUTF((String) dataValue);
                else if (dataValue instanceof Short) data.writeShort(((Short) dataValue).shortValue());
                else if (dataValue instanceof Long) data.writeLong(((Long) dataValue).longValue());
                else if (dataValue instanceof BufferedImage) ImageIO.write((BufferedImage) dataValue, "png", data);
            }

            return data;
        } catch (Exception e) {
            if (!(e instanceof SocketException) || !e.getMessage().toLowerCase().contains("socket closed") && !e.getMessage().toLowerCase().contains("broken pipe")) ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Packet data encoding failed.", e);
        }
        return data;
    }

    public static void onPacketData(FriendServerNetworkManager netManager, int pktid, DataInputStream dis) {
        try {
            Class packet = PacketRegistry.instance.getPacketByID(pktid);
            if (packet != null) packet.getMethod("process", FriendServerNetworkManager.class, DataInputStream.class).invoke(null, netManager, dis);
            else
                ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Unknown packet " + pktid + ".");
        }catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when reading packet.", e);
        }
    }

    public static void sendPacket(String ip, Packet packet) throws NotConnectedException {
        if (!ModuleFriend.getInstance().getFriendServerNetworkManager().isRunning()) {
            throw new NotConnectedException();
        }else{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            packet.write(new DataOutputStream(baos));
            try {
            	int size = baos.size();
            	ModuleFriend.getInstance().getFriendServerNetworkManager().getSocket().send(new DatagramPacket(new byte[] {(byte) ((size >>> 24) & 0xFF), (byte) ((size >>> 16) & 0xFF), (byte) ((size >>> 8) & 0xFF), (byte) ((size >>> 0) & 0xFF)}, 4, Inet4Address.getByName(ip), ModuleFriend.getInstance().getFriendServerNetworkManager().getPort()));
            	ModuleFriend.getInstance().getFriendServerNetworkManager().getSocket().send(new DatagramPacket(baos.toByteArray(), size, Inet4Address.getByName(ip), ModuleFriend.getInstance().getFriendServerNetworkManager().getPort()));
            } catch (IOException e) {
                ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when sending packet.", e);
            }
        }
    }
}