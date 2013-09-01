package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;

import com.oneofthesevenbillion.ziah.FriendModule.gui.GuiFriends;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

@Module(moduleId = "ModuleFriend", name = "Friends", description = "This module adds an advanced friend system allowing you to add friends and show their status and receive notifications when they join or leave and chat with them and have group chats and much more.")
public class ModuleFriend {
    private static ModuleFriend instance;
    private List<Friend> friends = new ArrayList<Friend>();

    public void load() {
        ModuleFriend.instance = this;
        this.getFriends().add(new Friend("HelloWorld", "Hello World", "Hello, my name is Hello World", null, false, true));
        this.getFriends().add(new Friend("BigMeany1003", "Big Meany", "Hello, my name is Big Meany", null, true, true));
        this.getFriends().add(new Friend("98TheCiaran98", "Ciaran Farley", "Hello, my name is Ciaran Farley", null, false, true));
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
}