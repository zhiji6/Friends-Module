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

public class EventClientConnect extends Event {
    private String serverAddress;
    private NetClientHandler netHandler;

    public EventClientConnect(String serverAddress, NetClientHandler netHandler) {
        super(false);
        this.serverAddress = serverAddress;
        this.netHandler = netHandler;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public NetClientHandler getNetHandler() {
        return this.netHandler;
    }
}