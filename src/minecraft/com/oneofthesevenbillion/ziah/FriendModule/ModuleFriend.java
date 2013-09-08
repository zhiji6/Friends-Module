package com.oneofthesevenbillion.ziah.FriendModule;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;

import com.oneofthesevenbillion.ziah.FriendModule.gui.GuiFriends;
import com.oneofthesevenbillion.ziah.FriendModule.network.FriendServerNetworkManager;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketRegistry;
import com.oneofthesevenbillion.ziah.FriendModule.network.ThreadPing;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

@Module(moduleId = "ModuleFriend", name = "Friends", description = "This module adds an advanced friend system allowing you to add friends and show their status and receive notifications when they join or leave and chat with them and have group chats and much more.")
public class ModuleFriend {
    private static ModuleFriend instance;
    private List<Friend> friends = new ArrayList<Friend>();
    private List<Friend> availableFriends = new ArrayList<Friend>();
    private List<String> ips = new ArrayList<String>();
	private List<String> onlineIps = new ArrayList<String>();
	private List<String> netOnlineIps = new ArrayList<String>();
	private Map<String, Long> ipNetPings = new HashMap<String, Long>();
    private FriendServerNetworkManager friendServerNetworkManager;
    private Friend player;
    private File ipFile;
    private File friendFile;

    public void load() {
        new PacketRegistry();
        ModuleFriend.instance = this;

        this.ipFile = new File(ZiahsClient.getInstance().getDataDir(), "friendIps.dat");
        try {
			if (!this.ipFile.exists()) this.ipFile.createNewFile();
        } catch (IOException e) {}
        this.friendFile = new File(ZiahsClient.getInstance().getDataDir(), "friends.dat");
        try {
			if (!this.friendFile.exists()) this.friendFile.createNewFile();
        } catch (IOException e) {}

        try {
			DataInputStream ipFileIn = new DataInputStream(new FileInputStream(this.ipFile));
			this.loadIPs(ipFileIn);
			ipFileIn.close();
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when reading ips file, not storing ips!!!");
		}

        try {
        	DataInputStream friendFileIn = new DataInputStream(new FileInputStream(this.friendFile));
			this.loadFriends(friendFileIn);
			friendFileIn.close();
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "EXCEPTION WHEN READING FRIENDS FILE, NOT STORING FRIENDS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

        this.player = new Friend(Minecraft.getMinecraft().func_110432_I().func_111285_a(), "Example Realname", "Example Description", null, false, true);

        this.friendServerNetworkManager = new FriendServerNetworkManager(25503);
        try {
            ZiahsClient.getInstance().registerMenuButton(new GuiSmallButton(0, 0, 0, Locale.localize("ziahsclient.gui.friends")), this.getClass().getDeclaredMethod("onFriendButtonClicked"), this);
        } catch (Exception e) {}
    }

	public void loadIPs(DataInputStream in) {
		try {
			List<String> ips = new ArrayList<String>();
			int length = in.readInt();
			for (int i = 0; i < length; i++) {
				String ip = in.readUTF();
				ips.add(ip);
			}
			this.ips.removeAll(ips);
			this.ips.addAll(ips);
		} catch (Exception e) {
			if (e instanceof EOFException) return;
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when loading ips!", e);
		}
	}

	public void saveIPs() {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(this.ipFile));
			out.writeInt(this.ips.size());
			for (String ip : this.ips) {
				out.writeUTF(ip);
			}
			out.close();
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when saving ips!", e);
		}
	}

    public void loadFriends(DataInputStream in) {
    	try {
    		List<Friend> friends = new ArrayList<Friend>();
			int length = in.readInt();
			for (int i = 0; i < length; i++) {
				Friend friend = Friend.readFromStream(in);
				friends.add(friend);
			}
			for (Friend friend : friends) {
				for (Friend friend2 : this.friends) {
					if (friend2.getUsername().equalsIgnoreCase(friend.getUsername())) {
						this.friends.remove(friend2);
					}
				}
			}
			this.friends.addAll(friends);
		} catch (Exception e) {
			if (e instanceof EOFException) return;
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when loading ips!", e);
		}
	}

	public void saveFriends() {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(this.friendFile));
			out.writeInt(this.friends.size());
			for (Friend friend : this.friends) {
				friend.writeToStream(out);
			}
			out.close();
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when saving friends!", e);
		}
	}

	public void onFriendButtonClicked() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiFriends(Minecraft.getMinecraft().currentScreen, this.getFriends()));
    }

    public static ModuleFriend getInstance() {
        return ModuleFriend.instance;
    }

    public List<Friend> getFriends() {
        return this.friends;
    }

    public List<Friend> getAvailableFriends() {
        return this.availableFriends;
    }

    public List<String> getIPs() {
        return this.ips;
    }

	public List<String> getOnlineIPs() {
		return this.onlineIps;
	}

	public List<String> getNetOnlineIPs() {
		return this.netOnlineIps;
	}

	public Map<String, Long> getIPNetPings() {
		return this.ipNetPings;
	}

    public FriendServerNetworkManager getFriendServerNetworkManager() {
        return this.friendServerNetworkManager;
    }

	public void receivedChatMessage(String message) {
		System.out.println("Friend Chat: " + message);
	}

	public Friend getPlayer() {
		return this.player;
	}

	public void ping(String ip) {
		new ThreadPing(ip).start();
	}
}