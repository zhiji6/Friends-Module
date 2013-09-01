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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class PacketControlSaddledEntity extends Packet {
    public boolean jump;
    public boolean sneak;
    public float moveForward;
    public float moveStrafe;

    public PacketControlSaddledEntity(boolean jump, boolean sneak, float moveForward, float moveStrafe) {
        this.packetID = 2;
        this.jump = jump;
        this.sneak = sneak;
        this.moveForward = moveForward;
        this.moveStrafe = moveStrafe;
    }

    public static Packet process(DataInputStream dataStream) {
        ZiahsClient.getInstance().getLogger().log(Level.WARNING, "RECIEVED UNEXPECTED PACKET!!!!!!!!!!!!!!!!!!!!");
        return null;
    }

    @Override
    public void read(DataInputStream dataStream) {
        super.read(dataStream);
        try {
            this.jump = dataStream.readBoolean();
            this.sneak = dataStream.readBoolean();
            this.moveForward = dataStream.readFloat();
            this.moveStrafe = dataStream.readFloat();
        }catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when decoding control saddled entity packet.", e);
        }
    }

    @Override
    public void write(DataOutputStream dataStream) {
        super.write(dataStream);
        PacketManager.encodeDataStream(dataStream, this.jump, this.sneak, this.moveForward, this.moveStrafe);
    }
}