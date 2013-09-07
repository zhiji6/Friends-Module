package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.util.List;

import net.minecraft.src.Tessellator;

import com.oneofthesevenbillion.ziah.FriendModule.Friend;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiScrollingList;

public class GuiSlotIPs extends GuiScrollingList {
    private GuiIPMenu parent;
    public List<String> ips;

    public GuiSlotIPs(GuiIPMenu parent, List<String> ips, int listWidth) {
        super(parent.getMinecraftInstance(), listWidth, parent.height, 32, parent.height - 65 + 4, 10, 15);
        this.parent = parent;
        this.ips = ips;
    }

    @Override
    protected int getSize() {
        return this.ips.size();
    }

    @Override
    protected void elementClicked(int var1, boolean var2) {
        this.parent.selectIPIndex(var1);
    }

    @Override
    protected boolean isSelected(int var1) {
        return this.parent.ipIndexSelected(var1);
    }

    @Override
    protected void drawBackground() {
        this.parent.drawDefaultBackground();
    }

    @Override
    protected int getContentHeight() {
        return (this.getSize()) * 35 + 1;
    }

    @Override
    protected void drawSlot(int listIndex, int var2, int var3, int var4, Tessellator var5) {
        String ip = this.ips.get(listIndex);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(ip, this.listWidth - 10), this.left + 3, var3 + 2, 0xFFFFFF);
    }
}