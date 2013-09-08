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
    private BufferedImage profilePicture;
    private boolean blocked = false;
    private boolean punchProtection = true;

    public Friend(String username, String realname, String description, BufferedImage profilePicture, boolean blocked, boolean punchProtection) {
        this.username = username;
        this.realname = realname;
        this.description = description;
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

    public String getDescription() {
        return this.description;
    }

    public boolean hasProfilePicture() {
        return this.profilePicture != null;
    }

    public BufferedImage getProfilePicture() {
        return this.profilePicture;
    }

    public void toggleBlockedStatus() {
        this.blocked = !this.blocked;
    }

    public void unfriend() {
        ModuleFriend.getInstance().getFriends().remove(this);
        ModuleFriend.getInstance().saveFriends();
    }

    public void togglePunchProtection() {
        this.punchProtection = !this.punchProtection;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public boolean isPunchProtectionEnabled() {
        return this.punchProtection;
    }

    public static Friend readFromStream(DataInputStream dataStream) throws IOException {
		return new Friend(dataStream.readUTF(), dataStream.readUTF(), dataStream.readUTF(), ImageIO.read(dataStream), false, true);
    }

	public void writeToStream(DataOutputStream dataStream) {
		PacketManager.encodeDataStream(dataStream, (String) this.username, (String) this.realname, (String) this.description, (BufferedImage) this.profilePicture);
	}
}