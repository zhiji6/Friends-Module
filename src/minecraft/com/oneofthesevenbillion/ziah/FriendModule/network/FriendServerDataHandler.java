package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class FriendServerDataHandler implements Runnable {
    private FriendServerNetworkManager netManager;

    public FriendServerDataHandler(FriendServerNetworkManager netManager) {
        this.netManager = netManager;
    }

    @Override
    public void run() {
        try {
            while (true) {
                //if (this.netManager.getSocket().isClosed() || !this.netManager.getSocket().isConnected()) break;
                try {
                	byte[] buf = new byte[4];
                	DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    this.netManager.getSocket().receive(packet);
                    int ch1 = buf[0];
                    int ch2 = buf[1];
                    int ch3 = buf[2];
                    int ch4 = buf[3];
                    if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
                    int size = (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
                    if (size != -1) {
                        byte[] bytes = new byte[size];
                        packet = new DatagramPacket(bytes, bytes.length);
                        this.netManager.getSocket().receive(packet);
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
                        int pktid = dis.readInt();
                        if (pktid != -1) {
                            PacketManager.onPacketData(this.netManager, packet.getAddress().getHostAddress(), pktid, dis);
                        }
                    }
                } catch (Exception e) {
                    if ((e instanceof SocketException) && (e.getMessage().toLowerCase().contains("socket closed") || e.getMessage().toLowerCase().contains("broken pipe") || e.getMessage().toLowerCase().contains("connection reset"))) break;
                    if (!(e instanceof EOFException)) ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when listening", e);
                }
            }
        } catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when listening for a friend server", e);
        }
    }
}