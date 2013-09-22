package com.oneofthesevenbillion.ziah.FriendModule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumGameType;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.PlayerControllerMP;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
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
import com.oneofthesevenbillion.ziah.ZiahsClient.util.Utils;

public class EventHandler {
	private long lastTime = 0;

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

    @EventListener
    public void onTick(EventTick event) {
    	if (Minecraft.getMinecraft().thePlayer != null) {
	    	for (Object obj : Minecraft.getMinecraft().thePlayer.getActivePotionEffects()) {
	    		if (obj instanceof PotionEffect) {
	    			PotionEffect effect = (PotionEffect) obj;
	    			if (effect.getDuration() <= 0) {
	    				Minecraft.getMinecraft().thePlayer.removePotionEffect(effect.getPotionID());
	    			}
	    		}
	    	}
    	}

    	if (System.currentTimeMillis() - this.lastTime > 3000) {
    		if (ModuleFriend.getInstance().getPlayer().getID() > 0) {
	    		Map<String, String> map = new HashMap<String, String>();
	    		map.put("id", String.valueOf(ModuleFriend.getInstance().getPlayer().getID()));
	    		try {
	    			ModuleFriend.getInstance().runFriendServerAction("heartbeat", map);
	    		} catch (IOException e) {
	    			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when beating heart!", e);
	    		}
    		}

    		Map<String, String> map = new HashMap<String, String>();
    		map.put("username", ModuleFriend.getInstance().getPlayer().getUsername());
    		try {
    			Map<String, Object> response = ModuleFriend.getInstance().runFriendServerAction("getactionsandclear", map);
    			if (((Double) response.get("status")).intValue() == 0) {
    				List<String> actions = (List<String>) response.get("actions");
    				if (actions != null) {
	    				for (String actionStr : actions) {
	    					try {
	    						Class<? extends Action> actionClass = ActionRegistry.instance.getActionByName(actionStr.split(" ")[0]);
	    						if (actionClass != null) {
	    							Action action = actionClass.getDeclaredConstructor(String.class).newInstance(actionStr);
	    							action.run();
	    						}else{
	    							ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Unknown action: " + actionStr);
	    						}
							} catch (Exception e) {
								ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when running action!", e);
							}
	    				}
    				}
    			}
    		} catch (IOException e) {
    			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when getting actions!", e);
    		}
    		this.lastTime = System.currentTimeMillis();
    	}
    }

    @EventListener
    public void onClientConnect(EventClientConnect event) {
    	EnumGameType gameType = EnumGameType.NOT_SET;
		try {
			gameType = (EnumGameType) ModLoader.getPrivateValue(PlayerControllerMP.class, Minecraft.getMinecraft().playerController, 10);
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when getting game type!", e);
		}
    	Minecraft.getMinecraft().playerController = new PlayerControllerMP(Minecraft.getMinecraft(), Minecraft.getMinecraft().getNetHandler()) {
			@Override
			public void attackEntity(EntityPlayer par1EntityPlayer, Entity par2Entity) {
				boolean canAttack = true;
				if (par2Entity instanceof EntityPlayer) {
					Friend friend = ModuleFriend.getInstance().getFriend(((EntityPlayer) par2Entity).getCommandSenderName());
					if (friend != null && friend.isPunchProtectionEnabled()) {
						canAttack = false;
					}
				}
				if (canAttack) {
					super.attackEntity(par1EntityPlayer, par2Entity);
				}
			}
		};
		Minecraft.getMinecraft().playerController.setGameType(gameType);
    }
}