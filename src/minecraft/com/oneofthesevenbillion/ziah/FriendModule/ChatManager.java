package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.List;

import com.oneofthesevenbillion.ziah.FriendModule.exception.FriendNotFoundException;
import com.oneofthesevenbillion.ziah.FriendModule.network.NotConnectedException;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketChat;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketManager;

public class ChatManager {
	private String sender;
	private List<String> messages = new ArrayList<String>();

	public ChatManager(String sender) {
		this.sender = sender;
	}

	public String getSender() {
		return this.sender;
	}

	public List<String> getMessages() {
		return this.messages;
	}

	public void sendMessage(String message) throws FriendNotFoundException {
		try {
			PacketManager.sendPacket(ModuleFriend.getInstance().getFriend(this.sender).getIP(), new PacketChat(this.sender, message));
		} catch (NotConnectedException e) {
			// Ignore
		} catch (NullPointerException e) {
			throw new FriendNotFoundException();
		}
	}

	public List<String> getMessagesThatFit(int size) {
		int fromIndex = this.messages.size() - size / 9;
		return this.messages.subList(fromIndex > 0 ? fromIndex : 0, this.messages.size());
	}
}