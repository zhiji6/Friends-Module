package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.lwjgl.input.Keyboard;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ServerData;

public class GuiAddIP extends GuiScreen {
    private GuiScreen parent;
    private GuiTextField ip;
    private boolean initialized = false;

    public GuiAddIP(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void updateScreen() {
    	if (!this.initialized) return;
        this.ip.updateCursorCounter();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "Add IP"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, "Cancel"));
        this.ip = new GuiTextField(this.fontRenderer, this.width / 2 - 100, 106, 200, 20);
        this.ip.setFocused(true);
        this.ip.setMaxStringLength(128);
        ((GuiButton) this.buttonList.get(0)).enabled = this.ip.getText().length() > 0;
        this.initialized = true;
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {
                Minecraft.getMinecraft().displayGuiScreen(this.parent);
            }else
            if (button.id == 0) {
                if (this.ip.getText().length() > 0) {
                	try {
						String ip = InetAddress.getByName(this.ip.getText()).getHostAddress();
	                	ModuleFriend.getInstance().getIPs().add(ip);
	                	ModuleFriend.getInstance().saveIPs();
	                	ModuleFriend.getInstance().ping(ip);
					} catch (UnknownHostException e) {
						// Unknown host, don't add it
					}
                    Minecraft.getMinecraft().displayGuiScreen(this.parent);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.ip.textboxKeyTyped(par1, par2);

        if (par2 == 28 || par2 == 156) {
            this.actionPerformed((GuiButton) this.buttonList.get(0));
        }

        ((GuiButton) this.buttonList.get(0)).enabled = this.ip.getText().length() > 0;
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
        this.ip.mouseClicked(par1, par2, par3);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
    	this.drawBackground(0);
        this.drawCenteredString(this.fontRenderer, "Add IP", this.width / 2, 17, 16777215);
        this.drawString(this.fontRenderer, "Type the address below", this.width / 2 - 100, 94, 10526880);
        this.ip.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }
}