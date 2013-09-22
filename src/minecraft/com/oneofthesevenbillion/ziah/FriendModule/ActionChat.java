package com.oneofthesevenbillion.ziah.FriendModule;

public class ActionChat extends Action {
	private String sender;
	private String message;

	public ActionChat(String str) {
		String[] params = str.split(" ");
		this.sender = params[1].replace((char) 0x01, ' ');
		this.message = params[2].replace((char) 0x01, ' ');
	}

	public ActionChat(String sender, String message) {
		this.sender = sender;
		this.message = message;
	}

	@Override
	public void run() {
		ModuleFriend.getInstance().receivedChatMessage(this.sender, this.message);
	}

	@Override
	public String toString() {
		return "chat " + this.sender.replace(' ', (char) 0x01) + " " + this.message.replace(' ', (char) 0x01);
	}
}