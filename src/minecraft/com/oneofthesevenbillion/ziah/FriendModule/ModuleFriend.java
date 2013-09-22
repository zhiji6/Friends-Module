package com.oneofthesevenbillion.ziah.FriendModule;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.lwjgl.input.Keyboard;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import net.minecraft.src.PlayerControllerMP;

import com.google.gson.Gson;
import com.oneofthesevenbillion.ziah.FriendModule.gui.GuiFriendChat;
import com.oneofthesevenbillion.ziah.FriendModule.gui.GuiFriends;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

@Module(moduleId = "ModuleFriend", name = "Friends", description = "This module adds an advanced friend system allowing you to add friends and show their status and receive notifications when they join or leave and chat with them.")// and have group chats and much more
public class ModuleFriend {
    private static ModuleFriend instance;
    private List<Friend> friends = new ArrayList<Friend>();
	private Map<String, ChatManager> chatManagers = new HashMap<String, ChatManager>();
    private Friend player;
    private File friendFile;
    private File profileFile;
	private KeyBinding friendsKey;
	private NotificationManager notificationManager;

    public void load() {
    	new ActionRegistry();
        ModuleFriend.instance = this;

        this.notificationManager = new NotificationManager();

        this.friendsKey = new KeyBinding("Friends Menu", Keyboard.KEY_F);
        ZiahsClient.getInstance().registerKey(this, this.friendsKey);

        ZiahsClient.getInstance().getEventBus().registerEventHandler(this.getClass(), new EventHandler());

        this.player = new Friend(Minecraft.getMinecraft().func_110432_I().func_111285_a(), "", "", (int) (System.currentTimeMillis() / 1000), /*"",*/ null);

        this.friendFile = new File(ZiahsClient.getInstance().getDataDir(), "friends" + File.separator + "players" + File.separator + Minecraft.getMinecraft().func_110432_I().func_111285_a() + File.separator + "friends.dat");
        try {
			if (!this.friendFile.exists()) {
				this.friendFile.getParentFile().mkdirs();
				this.friendFile.createNewFile();
			}
        } catch (IOException e) {}

        this.profileFile = new File(ZiahsClient.getInstance().getDataDir(), "friends" + File.separator + "players" + File.separator + Minecraft.getMinecraft().func_110432_I().func_111285_a() + File.separator + "profile.dat");
        try {
        	if (!this.profileFile.exists()) {
				this.profileFile.getParentFile().mkdirs();
				this.profileFile.createNewFile();
			}
        } catch (IOException e) {}

        try {
        	DataInputStream friendFileIn = new DataInputStream(new FileInputStream(this.friendFile));
			this.loadFriends(friendFileIn);
			friendFileIn.close();
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "EXCEPTION WHEN READING FRIENDS FILE, NOT STORING FRIENDS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

        try {
        	DataInputStream profileFileIn = new DataInputStream(new FileInputStream(this.profileFile));
			this.loadProfile(profileFileIn);
			profileFileIn.close();
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when reading profile file, not storing profile!!!");
		}

        try {
            ZiahsClient.getInstance().registerMenuButton(new GuiSmallButton(0, 0, 0, "Friends"), this.getClass().getDeclaredMethod("onFriendButtonClicked"), this);
        } catch (Exception e) {}

        Friend us = this.getFriend(this.player.getUsername(), true);
        
        if (us == null) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("username", this.player.getUsername());
			map.put("realname", this.player.getRealname());
			map.put("description", this.player.getDescription());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (this.player.hasProfilePicture()) {
				try {
					ImageIO.write(this.player.getProfilePicture(), "png", baos);
				} catch (IOException e) {
					baos.reset();
				}
			}
			map.put("picture", baos.toString());
			try {
				Map<String, Object> response = ModuleFriend.getInstance().runFriendServerAction("addfriend", map);
				if (((Double) response.get("status")).intValue() == 0) {
					this.player.setID(Integer.parseInt((String) response.get("id")));
				}
			} catch (IOException e) {
				ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when adding the player as a friend!", e);
			}
        }else{
        	if (!us.equals(this.player)) {
        		if (us.getUpdateTime() < this.player.getUpdateTime()) {
	    			Map<String, String> map = new HashMap<String, String>();
	    			map.put("username", this.player.getUsername());
	    			map.put("realname", this.player.getRealname());
	    			map.put("description", this.player.getDescription());
	    			map.put("updatetime", String.valueOf(this.player.getUpdateTime()));
	    			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    			if (this.player.hasProfilePicture()) {
	    				try {
	    					ImageIO.write(this.player.getProfilePicture(), "png", baos);
	    				} catch (IOException e) {
	    					baos.reset();
	    				}
	    			}
	    			map.put("picture", baos.toString());
	    			try {
	    				ModuleFriend.getInstance().runFriendServerAction("heartbeatupdate", map);
	    			} catch (IOException e) {
	    				ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when updating the friend server!", e);
	    			}
        		}else{
        			this.player.setRealname(us.getRealname());
        			this.player.setDescription(us.getDescription());
        			this.player.setProfilePicture(us.getProfilePicture());
        			this.player.setUpdateTime(us.getUpdateTime());
        		}
        	}
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
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when loading friendss!", e);
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

    public void loadProfile(DataInputStream in) {
    	try {
    		Friend friend = Friend.readFromStream(in);
    		this.player = friend;
		} catch (Exception e) {
			if (e instanceof EOFException) return;
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when loading profile!", e);
		}
	}

	public void saveProfile() {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(this.profileFile));
			this.player.writeToStream(out);
		} catch (Exception e) {
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when saving profile!", e);
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

	public void receivedChatMessage(String sender, String message) {
		Friend friend = this.getFriend(sender);

		this.getChatManager(sender).addReceivedMessage(message);
		if (Minecraft.getMinecraft().currentScreen == null || !(Minecraft.getMinecraft().currentScreen instanceof GuiFriendChat) || (Minecraft.getMinecraft().currentScreen instanceof GuiFriendChat && !((GuiFriendChat) Minecraft.getMinecraft().currentScreen).getChatManager().getPlayer().equalsIgnoreCase(sender))) {
			this.notificationManager.getNotifications().add(new NotificationData("Message from " + sender, message));
		}
	}

	public ChatManager getChatManager(String sender) {
		if (!this.chatManagers.containsKey(sender)) this.chatManagers.put(sender, new ChatManager(sender));
		return this.chatManagers.get(sender);
	}

	public Friend getPlayer() {
		return this.player;
	}

	public KeyBinding getFriendsKey() {
		return this.friendsKey;
	}

	public NotificationManager getNotificationManager() {
		return this.notificationManager;
	}

	public Friend getFriend(String username) {
		return this.getFriend(username, false);
	}

	public Friend getFriend(String username, boolean onlyRemote) {
		Friend friend = null;

		if (!onlyRemote) {
			for (Friend curFriend : this.friends) {
				if (curFriend.getUsername().equalsIgnoreCase(username)) {
					friend = curFriend;
					break;
				}
			}
		}

		if (friend == null) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("username", username);
			try {
				Map<String, Object> response = ModuleFriend.getInstance().runFriendServerAction("getfriend", map);
				if (response.get("status") == Integer.valueOf(0)) {
					Map<String, Object> friendData = (Map<String, Object>) response.get("friend");
					friend = new Friend((String) friendData.get("username"), (String) friendData.get("realname"), (String) friendData.get("description"), Integer.parseInt((String) friendData.get("id")), null);
					friend.setUpdateTime(Integer.parseInt((String) friendData.get("updatetime")));
				}
			} catch (IOException e) {
				ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when getting friend from friend server!", e);
			}
		}

		return friend;
	}

	public Map<String, Object> runFriendServerAction(String action, Map<String, String> params) throws IOException {
		Response res = Jsoup.connect("http://magi-craft.net/friends.php").data("action", action).data(params).method(Method.POST).execute();
		String response = res.body();

		Gson gson = new Gson();
		Map<String, Object> map = gson.fromJson(response, Map.class);
		return map;
	}
}