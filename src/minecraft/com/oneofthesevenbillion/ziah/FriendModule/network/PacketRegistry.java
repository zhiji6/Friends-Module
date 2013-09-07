package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {
    public static PacketRegistry instance;
    private Map packets;

    public PacketRegistry() {
        this.packets = new HashMap();
        instance = this;
        this.registerPacket(0, PacketChat.class);
        this.registerPacket(1, PacketPlaySound.class);
        this.registerPacket(2, PacketServeFriend.class);
        this.registerPacket(3, PacketHi.class);
        this.registerPacket(4, PacketServeIPs.class);
        this.registerPacket(5, PacketPingPong.class);
    }

    public void registerPacket(int id, Class packet) {
        this.packets.put(Integer.valueOf(id), packet);
    }

    public Class getPacketByID(int id) {
        return (Class) this.packets.get(Integer.valueOf(id));
    }
}