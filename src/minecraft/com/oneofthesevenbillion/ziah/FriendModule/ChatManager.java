package com.oneofthesevenbillion.ziah.FriendModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.oneofthesevenbillion.ziah.FriendModule.exception.FriendNotFoundException;
import com.oneofthesevenbillion.ziah.FriendModule.exception.NotRunningException;
import com.oneofthesevenbillion.ziah.FriendModule.exception.UnableToSendException;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class ChatManager {
	private String player;
	private List<String> receivedMessages = new ArrayList<String>();
	private List<String> sentMessages = new ArrayList<String>();
	private List<String> allMessages = new ArrayList<String>();

	public ChatManager(String player) {
		this.player = player;
	}

	public String getPlayer() {
		return this.player;
	}

	public List<String> getReceivedMessages() {
		return this.receivedMessages;
	}

	public List<String> getSentMessages() {
		return this.sentMessages;
	}

	public List<String> getAllMessages() {
		return this.allMessages;
	}

	public void addSentMessage(String message) {
		this.sentMessages.add(message);
		this.allMessages.add("You: " + message);
	}

	public void addReceivedMessage(String message) {
		this.receivedMessages.add(message);
		this.allMessages.add(this.player + ": " + message);
	}

	public void sendMessage(String message) {
		this.addSentMessage(message);
		ActionChat action = new ActionChat(ModuleFriend.getInstance().getPlayer().getUsername(), message);
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", this.player);
		map.put("action2", action.toString());
		try {
			ModuleFriend.getInstance().runFriendServerAction("addactiontoqueue", map);
		} catch (IOException e) {
			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when sending chat message!", e);
		}
	}
}