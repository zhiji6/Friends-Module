package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;

import com.oneofthesevenbillion.ziah.FriendModule.gui.GuiFriends;
import com.oneofthesevenbillion.ziah.FriendModule.network.FriendServerNetworkManager;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketRegistry;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

@Module(moduleId = "ModuleFriend", name = "Friends", description = "This module adds an advanced friend system allowing you to add friends and show their status and receive notifications when they join or leave and chat with them and have group chats and much more.")
public class ModuleFriend {
    private static ModuleFriend instance;
    private List<Friend> friends = new ArrayList<Friend>();
    private List<String> ips = new ArrayList<String>();
	private List<String> onlineIps = new ArrayList<String>();
    private FriendServerNetworkManager friendServerNetworkManager;
    private Friend player;

    public void load() {
        new PacketRegistry();
        ModuleFriend.instance = this;
        this.friendServerNetworkManager = new FriendServerNetworkManager(25503);
        this.player = new Friend(Minecraft.getMinecraft().func_110432_I().func_111285_a(), "Example Realname", "Example Description", null, false, true);
        try {
            ZiahsClient.getInstance().registerMenuButton(new GuiSmallButton(0, 0, 0, Locale.localize("ziahsclient.gui.friends")), this.getClass().getDeclaredMethod("onFriendButtonClicked"), this);
        } catch (Exception e) {}
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

    public List<String> getIPs() {
        return this.ips;
    }

	public List<String> getOnlineIPs() {
		return this.onlineIps;
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
}