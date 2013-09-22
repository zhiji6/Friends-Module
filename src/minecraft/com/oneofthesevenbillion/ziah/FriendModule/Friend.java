package com.oneofthesevenbillion.ziah.FriendModule;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class Friend {
    private String username;
    private String realname;
    private String description;
	private int id;
    private BufferedImage profilePicture;
    private boolean punchProtection = true;
	private boolean status = false;
	private int updateTime = 0;

    public Friend(String username, String realname, String description, /*String ip,*/ int id, BufferedImage profilePicture) {
        this.username = username;
        this.realname = realname;
        this.description = description;
        this.id = id;
        this.profilePicture = profilePicture;
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

    public boolean hasProfilePicture() {
        return this.profilePicture != null;
    }

    public BufferedImage getProfilePicture() {
        return this.profilePicture;
    }

    public void setProfilePicture(BufferedImage profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isPunchProtectionEnabled() {
        return this.punchProtection;
    }

    public void togglePunchProtection() {
        this.punchProtection = !this.punchProtection;
    }

	public boolean getStatus() {
		return this.status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(int updateTime) {
		this.updateTime = updateTime;
	}

	public int getID() {
		return this.id;
	}

	public void setID(int id) {
		this.id = id;
	}

    /*public boolean getStatus() {
    	if (this.ip == null) return false;

    	return ModuleFriend.getInstance().getOnlineIPs().contains(this.ip);
    }*/

    public void unfriend() {
        ModuleFriend.getInstance().getFriends().remove(this);
        ModuleFriend.getInstance().saveFriends();
    }

    public static Friend readFromStream(DataInputStream dataStream) throws IOException {
    	Friend friend = new Friend(dataStream.readUTF(), dataStream.readUTF(), dataStream.readUTF(), dataStream.readInt(), null);
    	friend.setUpdateTime(dataStream.readInt());
    	friend.setProfilePicture(ImageIO.read(dataStream));
		return friend;
    }

	public void writeToStream(DataOutputStream dataStream) throws IOException {
		dataStream.writeUTF(this.username);
		dataStream.writeUTF(this.realname);
		dataStream.writeUTF(this.description);
		//dataStream.writeUTF(this.ip);
		dataStream.writeInt(this.id);
		dataStream.writeInt(this.updateTime);
		if (this.hasProfilePicture()) ImageIO.write(this.profilePicture, "png", dataStream);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Friend)) return false;

		Friend friend = (Friend) obj;

		if (this.username == friend.getUsername() && this.realname == friend.getRealname() && this.description == friend.getDescription() && this.profilePicture == friend.getProfilePicture()) return true;

		boolean isEqual = true;

		try {
			if (!this.username.equals(friend.getUsername())) isEqual = false;
		} catch (Exception e) {}

		try {
			if (!this.realname.equals(friend.getRealname())) isEqual = false;
		} catch (Exception e) {}

		try {
			if (!this.description.equals(friend.getDescription())) isEqual = false;
		} catch (Exception e) {}

		try {
			if (!this.profilePicture.equals(friend.getProfilePicture())) isEqual = false;
		} catch (Exception e) {}

		return isEqual;
	}
}