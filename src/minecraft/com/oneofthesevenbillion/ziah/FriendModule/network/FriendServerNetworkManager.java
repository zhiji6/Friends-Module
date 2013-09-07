package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;

import net.minecraft.src.Minecraft;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class FriendServerNetworkManager {
    private DatagramSocket socket;
    private int port;
    private Thread dataHandlerThread;
    private boolean isRunning;

    public FriendServerNetworkManager(int port) {
        this.isRunning = false;
        this.port = port;
        try {
            this.start();
        } catch (AlreadyConnectedException e) {
            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "AN IMPOSSIBLE ERROR HAS OCCURRED!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void stop() throws NotConnectedException {
        if (!this.isRunning) throw new NotConnectedException();
        try {
            this.dataHandlerThread.interrupt();
        } catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when interrupting the data handler thread.", e);
        }
        try {
            this.socket.close();
        } catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when stopping the friends server socket.", e);
        }
        this.isRunning = false;
    }

    public void start() throws AlreadyConnectedException {
        if (this.isRunning) throw new AlreadyConnectedException();
        try {
            this.socket = new DatagramSocket(this.getPort());
            this.dataHandlerThread = new Thread(new FriendServerDataHandler(this), "FriendServerDataHandler");
            this.dataHandlerThread.start();
            this.isRunning = true;
        }catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when starting the friends server socket.", e);
        }
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }

	public int getPort() {
		return port;
	}
}