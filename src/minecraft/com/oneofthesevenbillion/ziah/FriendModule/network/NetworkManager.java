package com.oneofthesevenbillion.ziah.FriendModule.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.FriendModule.exception.AlreadyRunningException;
import com.oneofthesevenbillion.ziah.FriendModule.exception.NotRunningException;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class NetworkManager {
    private static NetworkManager instance;
    private int port;
    private Map<String, Thread> dataHandlerThreads = new HashMap<String, Thread>();
    private boolean isRunning;
    private NetworkConnectionManager conManager;

    public NetworkManager(int port) {
    	NetworkManager.instance = this;

        this.isRunning = false;
        this.port = port;
        this.conManager = new NetworkConnectionManager();
        try {
            this.start();
        } catch (AlreadyRunningException e) {
            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "AN IMPOSSIBLE ERROR HAS OCCURRED!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void stop() throws NotRunningException {
        if (!this.isRunning) throw new NotRunningException();
        for (Entry<String, Thread> entry : this.dataHandlerThreads.entrySet()) {
	        try {
	            entry.getValue().interrupt();
	        } catch (Exception e) {
	            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when interrupting the data handler thread for " + entry.getKey() + ".", e);
	        }
        }
        this.isRunning = false;
    }

    public void start() throws AlreadyRunningException {
        if (this.isRunning) throw new AlreadyRunningException();
        this.isRunning = true;
    }

    private void addAddress(String address) {
    	System.out.println("Adding address " + address);
    	try {
    		Thread thread = new Thread(new DataHandler(this, address), "[Friend Module] DataHandler for " + address);
            this.dataHandlerThreads.put(address, thread);
            thread.start();
        } catch (Exception e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when starting a friends server data handler.", e);
        }
    }

	public void addAddressIfNotAlreadyAdded(String address) {
		if (!this.dataHandlerThreads.containsKey(address)) this.addAddress(address);
	}

	public int getPort() {
		return port;
	}

	public NetworkConnectionManager getNetworkConnectionManager() {
		return this.conManager;
	}

	public static NetworkManager getInstance() {
		return NetworkManager.instance;
	}
}