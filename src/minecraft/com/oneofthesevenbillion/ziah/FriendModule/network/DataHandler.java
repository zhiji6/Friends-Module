package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class DataHandler implements Runnable {
    private NetworkManager netManager;
    private String address;

    public DataHandler(NetworkManager netManager, String address) {
        this.netManager = netManager;
        this.address = address;
        System.out.println("New data handler for " + address);
    }

    @Override
    public void run() {
    	Connection con = this.netManager.getNetworkConnectionManager().getConnectionForAddress(this.address);
    	if (con == null) return;
    	System.out.println("Data handler for " + this.address + " started");
        try {
            while (true) {
                try {
                	byte[] buf = new byte[4];
                    con.receiveData(buf);
                    int ch1 = buf[0];
                    int ch2 = buf[1];
                    int ch3 = buf[2];
                    int ch4 = buf[3];
                    if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
                    int size = (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
                    if (size != -1) {
                        byte[] bytes = new byte[size];
                        con.receiveData(bytes);
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
                        int pktid = dis.readInt();
                        if (pktid != -1) {
                            PacketManager.onPacketData(this.netManager, this.address, pktid, dis);
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
        System.out.println("Data handler for " + this.address + " stopped");
    }
}