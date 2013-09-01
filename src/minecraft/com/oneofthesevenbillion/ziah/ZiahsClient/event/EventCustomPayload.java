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

package com.oneofthesevenbillion.ziah.ZiahsClient.event;

import net.minecraft.src.NetClientHandler;
import net.minecraft.src.Packet250CustomPayload;

public class EventCustomPayload extends Event {
    private NetClientHandler netHandler;
    private Packet250CustomPayload packet;

    public EventCustomPayload(NetClientHandler netHandler, Packet250CustomPayload packet) {
        super(false);
        this.netHandler = netHandler;
        this.packet = packet;
    }

    public NetClientHandler getNetHandler() {
        return this.netHandler;
    }

    public Packet250CustomPayload getPacket() {
        return this.packet;
    }
}