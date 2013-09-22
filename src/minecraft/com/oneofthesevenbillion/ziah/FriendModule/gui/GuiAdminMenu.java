package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.FriendModule.ActionChat;
import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiMessage;
import com.oneofthesevenbillion.ziah.ZiahsClient.util.ArrayUtils;

import net.minecraft.src.DynamicTexture;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TextureManager;

public class GuiAdminMenu extends GuiScreen {
	private GuiScreen parent;
    private GuiTextField action;
    private boolean initialized = false;
	private String player;

    public GuiAdminMenu(GuiScreen parent, String player) {
        this.parent = parent;
        this.player = player;
    }

    @Override
    public void updateScreen() {
    	if (!this.initialized) return;
        this.action.updateCursorCounter();
        ((GuiButton) this.buttonList.get(1)).enabled = this.action.getText().length() > 0;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, 4, this.height - 20 - 8, 130, 20, "Done"));
        this.buttonList.add(new GuiButton(1, 4 + 130 + 4, this.height - 20 - 8, 130, 20, "Send Action"));
        this.action = new GuiTextField(this.fontRenderer, 4 + this.fontRenderer.getStringWidth("Action: ") + 4, 35, 200, 20);
        this.action.setFocused(true);
        this.action.setMaxStringLength(1000);
        ((GuiButton) this.buttonList.get(1)).enabled = this.action.getText().length() > 0;
        this.initialized = true;
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 0) {
                Minecraft.getMinecraft().displayGuiScreen(this.parent);
            }else
            if (button.id == 1) {
        		Map<String, String> map = new HashMap<String, String>();
        		map.put("username", this.player);
        		map.put("action2", this.action.getText());
        		try {
        			ModuleFriend.getInstance().runFriendServerAction("addactiontoqueue", map);
        		} catch (IOException e) {
        			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when sending chat message!", e);
        		}
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.action.textboxKeyTyped(par1, par2);

        if (par2 == 28 || par2 == 156) {
            this.actionPerformed((GuiButton) this.buttonList.get(1));
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
        this.action.mouseClicked(par1, par2, par3);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
    	this.drawBackground(0);
        int offsetX = 4;
        int offsetCenteredX = this.width / 2;
        int offsetY = 17;
        this.drawCenteredString(this.fontRenderer, "Admin Control Panel for " + this.player, offsetCenteredX, offsetY, 16777215);
        offsetY += 9;
        offsetY += 9;
        this.drawString(this.fontRenderer, "Action: ", offsetX, offsetY, 10526880);
        offsetY += 9;
        offsetY += 9;
        offsetY += 9;
        this.action.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }

    private BufferedImage scaleImage(Image srcImg, int width, int height) {
    	BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.scale(((float) width) / ((float) srcImg.getWidth(null)), ((float) height) / ((float) srcImg.getHeight(null)));
        g.drawImage(srcImg, 0, 0, null);
        g.dispose();
        return scaled;
    }
}