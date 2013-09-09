package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ScaledResolution;

import com.oneofthesevenbillion.ziah.FriendModule.gui.GuiFriendChat;
import com.oneofthesevenbillion.ziah.FriendModule.gui.GuiFriends;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventClientConnect;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventClientDisconnect;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventKeyboard;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventListener;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventRender;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventTick;

public class EventHandler {
    @EventListener
    public void onKeyboard(EventKeyboard event) {
        if (event.getKey() == ModuleFriend.getInstance().getFriendsKey() && Minecraft.getMinecraft().currentScreen == null) {
        	Minecraft.getMinecraft().displayGuiScreen(new GuiFriends(null, ModuleFriend.getInstance().getFriends()));
        }
    }

    @EventListener
    public void onRender(EventRender event) {
    	if (Minecraft.getMinecraft().fontRenderer == null) return;
    	ScaledResolution var3 = new ScaledResolution(Minecraft.getMinecraft().gameSettings, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        int screenWidth = var3.getScaledWidth();
        int screenHeight = var3.getScaledHeight();
	    ModuleFriend.getInstance().getNotificationManager().render(screenWidth, screenHeight);
    }
}