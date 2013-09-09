package com.oneofthesevenbillion.ziah.FriendModule;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.oneofthesevenbillion.ziah.FriendModule.network.PacketManager;

public class Friend {
    private String username;
    private String realname;
    private String description;
	private String ip;
    private BufferedImage profilePicture;
    private boolean blocked = false;
    private boolean punchProtection = true;

    public Friend(String username, String realname, String description, String ip, BufferedImage profilePicture, boolean blocked, boolean punchProtection) {
        this.username = username;
        this.realname = realname;
        this.description = description;
        this.ip = ip;
        this.profilePicture = profilePicture;
        this.blocked = blocked;
        this.punchProtection = punchProtection;
    }

    public String getUsername() {
        return this.username;
    }

    public String getRealname() {
        return this.realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public String getIP() {
		return this.ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

    public boolean hasProfilePicture() {
        return this.profilePicture != null;
    }

    public BufferedImage getProfilePicture() {
        return this.profilePicture;
    }

    public void setProfilePicture(BufferedImage profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public void toggleBlockedStatus() {
        this.blocked = !this.blocked;
    }

    public boolean isPunchProtectionEnabled() {
        return this.punchProtection;
    }

    public void togglePunchProtection() {
        this.punchProtection = !this.punchProtection;
    }

    public boolean getStatus() {
    	if (this.ip == null) return false;

    	return ModuleFriend.getInstance().getOnlineIPs().contains(this.ip);
    }

    public void unfriend() {
        ModuleFriend.getInstance().getFriends().remove(this);
        ModuleFriend.getInstance().saveFriends();
    }

    public static Friend readFromStream(DataInputStream dataStream) throws IOException {
		return new Friend(dataStream.readUTF(), dataStream.readUTF(), dataStream.readUTF(), dataStream.readUTF(), ImageIO.read(dataStream), false, true);
    }

	public void writeToStream(DataOutputStream dataStream) {
		PacketManager.encodeDataStream(dataStream, (String) this.username, (String) this.realname, (String) this.description, (String) this.ip, (BufferedImage) this.profilePicture);
	}
}