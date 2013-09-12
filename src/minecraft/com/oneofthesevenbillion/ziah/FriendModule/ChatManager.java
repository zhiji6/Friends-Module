package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.List;

import com.oneofthesevenbillion.ziah.FriendModule.exception.FriendNotFoundException;
import com.oneofthesevenbillion.ziah.FriendModule.exception.NotRunningException;
import com.oneofthesevenbillion.ziah.FriendModule.exception.UnableToSendException;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketChat;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketManager;

public class ChatManager {
	private String sender;
	private List<String> receivedMessages = new ArrayList<String>();
	private List<String> sentMessages = new ArrayList<String>();

	public ChatManager(String sender) {
		this.sender = sender;
	}

	public String getSender() {
		return this.sender;
	}

	public List<String> getReceivedMessages() {
		return this.receivedMessages;
	}

	public List<String> getSentMessages() {
		return this.sentMessages;
	}

	public void sendMessage(String message) throws FriendNotFoundException {
		try {
			PacketManager.sendPacket(ModuleFriend.getInstance().getFriend(this.sender).getIP(), new PacketChat(this.sender, message));
		} catch (NotRunningException e) {
			// Ignore
		} catch (UnableToSendException e) {
			// Ignore
		} catch (NullPointerException e) {
			throw new FriendNotFoundException();
		}
	}
}