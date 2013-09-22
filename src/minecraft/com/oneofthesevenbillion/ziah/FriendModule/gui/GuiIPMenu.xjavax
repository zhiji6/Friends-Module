package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.util.List;

import net.minecraft.src.EnumChatFormatting;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;

import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.FriendModule.network.ThreadFindHostname;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiQuestion;

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
        this.title = "IP Menu";
	}

	@Override
    public void initGui() {
        this.listWidth = 84;
        this.buttonList.add(new GuiSmallButton(6, 4, this.height - 28, 130, 20, "Done"));
        this.buttonList.add(new GuiSmallButton(10, 4, this.height - 52, 130, 20, "Remove IP"));
        this.buttonList.add(new GuiSmallButton(11, 138, this.height - 52, 130, 20, "Add New IP"));
        this.buttonList.add(new GuiSmallButton(9, 138, this.height - 28, 130, 20, "Refresh"));
        this.ipList = new GuiSlotIPs(this, this.ips, this.listWidth);
        this.ipList.registerScrollButtons(this.buttonList, 7, 8);
        for (String ip : this.ips) {
        	ModuleFriend.getInstance().ping(ip);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 6:
                    this.mc.displayGuiScreen(this.parent);
                    return;
                case 10:
                    try {
                        this.mc.displayGuiScreen(new GuiQuestion(this, "Are you sure you want to remove the ip " + this.selectedIp + "?", "Yes", "No", this.getClass().getDeclaredMethod("removeipCallback", Integer.class), this));
                    } catch (NoSuchMethodException e) {
                        // Impossible
                    } catch (SecurityException e) {
                        // Impossible
                    }
                    return;
                case 11:
                    this.mc.displayGuiScreen(new GuiAddIP(this));
                    return;
                case 9:
                	ModuleFriend.getInstance().ping(this.selectedIp);
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
        ((GuiButton) this.buttonList.get(3)).enabled = this.selected != -1;
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

            this.drawString(this.fontRenderer, "IP: " + this.selectedIp + (this.hostname != null ? ", Hostname: " + this.hostname : ""), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, "Friend Status: " + (ModuleFriend.getInstance().getOnlineIPs().contains(this.selectedIp) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + (ModuleFriend.getInstance().getOnlineIPs().contains(this.selectedIp) ? "Online" : "Offline") + EnumChatFormatting.RESET, offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, "Network Status: " + (ModuleFriend.getInstance().getNetOnlineIPs().contains(this.selectedIp) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + (ModuleFriend.getInstance().getNetOnlineIPs().contains(this.selectedIp) ? "Online" : "Offline") + EnumChatFormatting.RESET + (ModuleFriend.getInstance().getNetOnlineIPs().contains(this.selectedIp) ? ", Ping: " + (ModuleFriend.getInstance().getIPNetPings().containsKey(this.selectedIp) ? String.valueOf(ModuleFriend.getInstance().getIPNetPings().get(this.selectedIp) < 2000 ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + ModuleFriend.getInstance().getIPNetPings().get(this.selectedIp) + "ms" + EnumChatFormatting.RESET + " Roundtrip" : "Unknown") : ""), offsetX, offsetY, 0xFFFFFF);
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