package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.util.List;

import net.minecraft.src.Tessellator;

import com.oneofthesevenbillion.ziah.FriendModule.Friend;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiScrollingList;

public class GuiSlotAvailableFriends extends GuiScrollingList {
    private GuiAvailableFriends parent;
    private List<Friend> friends;

    public GuiSlotAvailableFriends(GuiAvailableFriends parent, List<Friend> friends, int listWidth) {
        super(parent.getMinecraftInstance(), listWidth, parent.height, 32, parent.height - 65 + 4, 10, 35);
        this.parent = parent;
        this.friends = friends;
    }

    @Override
    protected int getSize() {
        return this.friends.size();
    }

    @Override
    protected void elementClicked(int var1, boolean var2) {
        this.parent.selectFriendIndex(var1);
    }

    @Override
    protected boolean isSelected(int var1) {
        return this.parent.friendIndexSelected(var1);
    }

    @Override
    protected void drawBackground() {
    	this.parent.drawBackground(0);
    }

    @Override
    protected int getContentHeight() {
        return (this.getSize()) * 35 + 1;
    }

    @Override
    protected void drawSlot(int listIndex, int var2, int var3, int var4, Tessellator var5) {
        Friend friend = this.friends.get(listIndex);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(friend.getUsername(), this.listWidth - 10), this.left + 3, var3 + 2, 0xFFFFFF);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(friend.getRealname(), this.listWidth - 10), this.left + 3, var3 + 12, 0xCCCCCC);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(friend.getDescription(), this.listWidth - 10), this.left + 3, var3 + 22, 0xCCCCCC);
    }
}