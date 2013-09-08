package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.FriendModule.Friend;
import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.FriendModule.network.ThreadFindHostname;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiQuestion;

import net.minecraft.src.DynamicTexture;
import net.minecraft.src.EnumChatFormatting;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TextureManager;

public class GuiIPMenu extends GuiScreen {
    private GuiScreen parent;
    private GuiSlotIPs ipList;
    private int selected = -1;
    private String selectedIp;
    private int listWidth;
    private List<String> ips;
    private String title;
    private String hostname;
	private boolean hostnameChecked = false;
	private int lastSelected = -1;

	public GuiIPMenu(GuiScreen parent, List<String> ips) {
        this.parent = parent;
        this.ips = ips;
        this.title = Locale.localize("ziahsclient.gui.friends.ip_menu");
	}

	@Override
    public void initGui() {
        this.listWidth = 84;
        this.buttonList.add(new GuiSmallButton(6, 4, this.height - 28, Locale.localize("ziahsclient.gui.done")));
        this.buttonList.add(new GuiSmallButton(7, 4, this.height - 52, Locale.localize("ziahsclient.gui.friends.remove_ip")));
        this.buttonList.add(new GuiSmallButton(8, 158, this.height - 28, Locale.localize("ziahsclient.gui.friends.add_ip")));
        this.ipList = new GuiSlotIPs(this, this.ips, this.listWidth);
        this.ipList.registerScrollButtons(this.buttonList, 7, 8);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 6:
                    this.mc.displayGuiScreen(this.parent);
                    return;
                case 7:
                    try {
                        this.mc.displayGuiScreen(new GuiQuestion(this, Locale.localize("ziahsclient.gui.friends.are_you_sure_remove_ip").replace("%IP%", this.selectedIp), Locale.localize("ziahsclient.gui.yes"), Locale.localize("ziahsclient.gui.no"), this.getClass().getDeclaredMethod("removeipCallback", Integer.class), this));
                    } catch (NoSuchMethodException e) {
                        // Impossible
                    } catch (SecurityException e) {
                        // Impossible
                    }
                    return;
                case 8:
                    this.mc.displayGuiScreen(new GuiAddIP(this));
                    return;
            }
        }
        super.actionPerformed(button);
    }

    public int drawLine(String line, int offset, int shifty) {
        int r = this.fontRenderer.drawString(line, offset, shifty, 0xd7edea);
        return shifty + 10;
    }

    @Override
    public void updateScreen() {
        if (this.buttonList.size() <= 0) return;
        ((GuiButton) this.buttonList.get(1)).enabled = this.selected != -1;
        if (!this.hostnameChecked || this.lastSelected != this.selected) {
        	this.hostname = null;
			try {
				new ThreadFindHostname(this.selectedIp, this.getClass().getDeclaredMethod("findHostnameCallback", String.class), this).start();
			} catch (Exception e) {
				// Ignore exceptions
			}
        	this.hostnameChecked = true;
        	this.lastSelected = this.selected;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        if (this.ipList == null) return;
        this.ipList.drawScreen(mouseX, mouseY, tick);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        int offsetX = this.listWidth + 20;
        if (this.selectedIp != null) {
            GL11.glEnable(GL11.GL_BLEND);
            int offsetY = 35;

            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.ip").replace("%IP%", this.selectedIp) + (hostname != null ? Locale.localize("ziahsclient.gui.friends.ip.hostname").replace("%HOSTNAME%", hostname) : ""), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.friend_status").replace("%STATUS%", (ModuleFriend.getInstance().getOnlineIPs().contains(this.selectedIp) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + Locale.localize("ziahsclient.gui.friends.status." + (ModuleFriend.getInstance().getOnlineIPs().contains(this.selectedIp) ? "online" : "offline")) + EnumChatFormatting.RESET), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.network_status").replace("%STATUS%", (ModuleFriend.getInstance().getNetOnlineIPs().contains(this.selectedIp) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + Locale.localize("ziahsclient.gui.friends.status." + (ModuleFriend.getInstance().getNetOnlineIPs().contains(this.selectedIp) ? "online" : "offline")) + EnumChatFormatting.RESET) + (ModuleFriend.getInstance().getNetOnlineIPs().contains(this.selectedIp) ? Locale.localize("ziahsclient.gui.friends.network_status.ping").replace("%PING%", ModuleFriend.getInstance().getIPNetPings().containsKey(this.selectedIp) ? String.valueOf(ModuleFriend.getInstance().getIPNetPings().get(this.selectedIp) < 2000 ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + ModuleFriend.getInstance().getIPNetPings().get(this.selectedIp) + "ms" + EnumChatFormatting.RESET + " Roundtrip" : "Unknown") : ""), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            offsetY += 9;
            GL11.glDisable(GL11.GL_BLEND);
        }
        super.drawScreen(mouseX, mouseY, tick);
    }

    public Minecraft getMinecraftInstance() {
        return this.mc;
    }

    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    public void selectIPIndex(int var1) {
        this.selected = var1;
        if (var1 >= 0 && var1 <= this.ips.size()) {
            this.selectedIp = this.ips.get(this.selected);
        }else{
            this.selectedIp = null;
        }
    }

    public boolean ipIndexSelected(int var1) {
        return var1 == this.selected;
    }

    public void removeipCallback(Integer button) {
    	switch (button) {
	        case 0:
	            this.ips.remove(this.selected);
	            this.ipList.ips.remove(this.selectedIp);
	            this.selectIPIndex(-1);
	            ModuleFriend.getInstance().saveIPs();
	            break;
	    }
    }

    public void findHostnameCallback(String hostname) {
    	this.hostname = hostname;
    }
}