package com.oneofthesevenbillion.ziah.FriendModule;

import java.awt.image.BufferedImage;

public class Friend {
    private long uid;
    private String username;
    private String realname;
    private String description;
    private BufferedImage profilePicture;
    private boolean blocked = false;
    private boolean punchProtection = true;

    public Friend(long uid, String username, String realname, String description, BufferedImage profilePicture, boolean blocked, boolean punchProtection) {
        this.uid = uid;
        this.username = username;
        this.realname = realname;
        this.description = description;
        this.profilePicture = profilePicture;
        this.blocked = blocked;
        this.punchProtection = punchProtection;
    }

    public long getUID() {
        return this.uid;
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
}