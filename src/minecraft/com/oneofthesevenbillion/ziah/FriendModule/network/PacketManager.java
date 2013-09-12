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
import com.oneofthesevenbillion.ziah.FriendModule.exception.NotRunningException;
import com.oneofthesevenbillion.ziah.FriendModule.exception.UnableToSendException;
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

    public static void onPacketData(NetworkManager netManager, String sender, int pktid, DataInputStream dis) {
        try {
            Class packet = PacketRegistry.instance.getPacketByID(pktid);
            if (packet != null) {
            	packet.getMethod("process", NetworkManager.class, String.class, DataInputStream.class).invoke(null, netManager, sender, dis);
            }else{
                ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Unknown packet " + pktid + ".");
            }
        } catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when reading packet.", e);
        }
    }

    public static void sendPacket(String address, Packet packet) throws NotRunningException, UnableToSendException {
        if (!ModuleFriend.getInstance().getFriendServerNetworkManager().isRunning()) {
            throw new NotRunningException();
        }else{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            packet.write(new DataOutputStream(baos));
            try {
            	int size = baos.size();
            	NetworkManager.getInstance().addAddressIfNotAlreadyAdded(address);
            	Connection con = NetworkManager.getInstance().getNetworkConnectionManager().getConnectionForAddress(address);
            	if (con == null) throw new UnableToSendException();
            	con.sendData(new byte[] {(byte) ((size >>> 24) & 0xFF), (byte) ((size >>> 16) & 0xFF), (byte) ((size >>> 8) & 0xFF), (byte) ((size >>> 0) & 0xFF)});
            	con.sendData(baos.toByteArray());
            } catch (IOException e) {
                ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when sending packet.", e);
            }
        }
    }
}