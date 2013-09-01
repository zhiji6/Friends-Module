/*
Ziah_'s Client
Copyright (C) 2013  Ziah Jyothi

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see [http://www.gnu.org/licenses/].
*/

package com.oneofthesevenbillion.ziah.ZiahsClient.network;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.Minecraft;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.Packet250CustomPayload;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketManager {
    public static DataOutputStream encodeDataStream(DataOutputStream data, Object... sendData) {
        try {
            for (Object dataValue : sendData) {
                if (dataValue instanceof Integer) {
                    data.writeInt((Integer) dataValue);
                }else if (dataValue instanceof Float) {
                    data.writeFloat((Float) dataValue);
                }else if (dataValue instanceof Double) {
                    data.writeDouble((Double) dataValue);
                }else if (dataValue instanceof Byte) {
                    data.writeByte((Byte) dataValue);
                }else if (dataValue instanceof Boolean) {
                    data.writeBoolean((Boolean) dataValue);
                }else if (dataValue instanceof String) {
                    data.writeUTF((String) dataValue);
                }else if (dataValue instanceof Short) {
                    data.writeShort((Short) dataValue);
                }else if (dataValue instanceof Long) {
                    data.writeLong((Long) dataValue);
                }else if (dataValue instanceof BufferedImage) {
                    ImageIO.write((BufferedImage) dataValue, "png", data);
                }
            }

            return data;
        }catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Packet data encoding failed.");
        }

        return data;
    }

    public static void onPacketData(NetClientHandler netHandler, Packet250CustomPayload payload) {
        try {
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload.data));

            int packetTypeID = data.readInt();
            Class<? extends Packet> packet = PacketRegistry.instance.getPacketByID(packetTypeID);
            if (packet != null) {
                packet.getMethod("process", DataInputStream.class).invoke(null, data);
            }else {
                ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Unknown packet " + packetTypeID + ".");
            }
        }catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when reading packet.", e);
        }
    }

    public static void sendPacket(Packet packet) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            packet.write(dos);

            Packet250CustomPayload payload = new Packet250CustomPayload();
            payload.channel = "ziahsclient";
            payload.data = baos.toByteArray();
            payload.length = payload.data.length;

            dos.close();
            baos.close();

            Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(payload);
        }catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when sending packet.", e);
        }
    }
}