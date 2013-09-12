package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.src.DynamicTexture;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TextureManager;

import com.oneofthesevenbillion.ziah.FriendModule.ChatManager;
import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.FriendModule.Util;
import com.oneofthesevenbillion.ziah.FriendModule.exception.FriendNotFoundException;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiMessage;
import com.oneofthesevenbillion.ziah.ZiahsClient.util.ArrayUtils;

public class GuiFriendChat extends GuiScreen {
	private GuiScreen parent;
	private ChatManager chatManager;
    private boolean initialized = false;
	private GuiTextField message;

	public GuiFriendChat(GuiScreen parent, String player) {
		this.parent = parent;
		this.chatManager = ModuleFriend.getInstance().getChatManager(player);
	}

	@Override
    public void updateScreen() {
    	if (!this.initialized) return;
        this.message.updateCursorCounter();
        ((GuiButton) this.buttonList.get(0)).enabled = this.message.getText().length() > 0;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width - 4 - 50 - 4 - 50, this.height - 20 - 4, 50, 20, "Send"));
        this.buttonList.add(new GuiButton(1, this.width - 4 - 50, this.height - 20 - 4, 50, 20, "Back"));
        this.message = new GuiTextField(this.fontRenderer, 4, this.height - 20 - 4, this.width - 4 - 50 - 4 - 50 - 4 - 4, 20);
        this.message.setFocused(true);
        this.message.setMaxStringLength(1000);
        ((GuiButton) this.buttonList.get(0)).enabled = this.message.getText().length() > 0;
        this.initialized = true;
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
            	case 0:
					try {
						this.chatManager.sendMessage(this.message.getText());
					} catch (FriendNotFoundException e) {
						Minecraft.getMinecraft().displayGuiScreen(new GuiMessage(this.parent, "The friend you tried to send a message to doesn't exist!"));
					}
            		break;
            	case 1:
            		Minecraft.getMinecraft().displayGuiScreen(this.parent);
            		break;
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.message.textboxKeyTyped(par1, par2);

        if (par2 == 28 || par2 == 156) {
            this.actionPerformed((GuiButton) this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
        this.message.mouseClicked(par1, par2, par3);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
    	if (!this.initialized) return;
    	this.drawBackground(0);
        int offsetX = 4;
        int offsetCenteredX = this.width / 2;
        int offsetY = 17;
        this.drawCenteredString(this.fontRenderer, "Chat with " + this.chatManager.getSender(), offsetCenteredX, offsetY, 16777215);
        offsetY += 9;
        offsetY += 9;
        for (String message : Util.getLinesThatFit(this.chatManager.getReceivedMessages(), this.height - 4 - 20 - 4 - offsetY)) {
        	this.drawString(this.fontRenderer, message, offsetX, offsetY, 0xFFFFFF);
        	offsetY += 9;
        }
        this.message.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }
}