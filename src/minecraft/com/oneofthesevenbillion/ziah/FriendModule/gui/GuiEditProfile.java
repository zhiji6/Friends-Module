package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
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

public class GuiEditProfile extends GuiScreen {
	private GuiScreen parent;
    private GuiTextField realname;
    private GuiTextField description;
    private BufferedImage profilePicture;
    private boolean initialized = false;
	private ResourceLocation profilePictureResourceLocation;
	private BufferedImage lastProfilePicture;

    public GuiEditProfile(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void updateScreen() {
    	if (!this.initialized) return;
        this.realname.updateCursorCounter();
        this.description.updateCursorCounter();
        ((GuiButton) this.buttonList.get(0)).enabled = !this.realname.getText().equals(ModuleFriend.getInstance().getPlayer().getRealname()) || !this.description.getText().equals(ModuleFriend.getInstance().getPlayer().getDescription()) || (this.profilePicture != null && !this.profilePicture.equals(ModuleFriend.getInstance().getPlayer().getProfilePicture()));
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, 4, this.height - 20 - 8 - 20 - 4, 130, 20, Locale.localize("ziahsclient.gui.done")));
        this.buttonList.add(new GuiButton(1, 4, this.height - 20 - 8, 130, 20, Locale.localize("ziahsclient.gui.cancel")));
        this.buttonList.add(new GuiButton(2, 4 + this.fontRenderer.getStringWidth(Locale.localize("ziahsclient.gui.friends.edit_profile.picture")) + 4, 111, 130, 20, Locale.localize("ziahsclient.gui.friends.edit_profile.change_picture")));
        this.realname = new GuiTextField(this.fontRenderer, 4 + this.fontRenderer.getStringWidth(Locale.localize("ziahsclient.gui.friends.edit_profile.realname")) + 4, 55, 200, 20);
        this.realname.setFocused(true);
        this.realname.setText(ModuleFriend.getInstance().getPlayer().getRealname());
        this.description = new GuiTextField(this.fontRenderer, 4 + this.fontRenderer.getStringWidth(Locale.localize("ziahsclient.gui.friends.edit_profile.description")) + 4, 82, 200, 20);
        this.description.setMaxStringLength(1000);
        this.description.setText(ModuleFriend.getInstance().getPlayer().getDescription());
        this.profilePicture = ModuleFriend.getInstance().getPlayer().getProfilePicture();
        ((GuiButton) this.buttonList.get(0)).enabled = !this.realname.getText().equals(ModuleFriend.getInstance().getPlayer().getRealname()) || !this.description.getText().equals(ModuleFriend.getInstance().getPlayer().getDescription()) || (this.profilePicture != null && !this.profilePicture.equals(ModuleFriend.getInstance().getPlayer().getProfilePicture()));
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
                ModuleFriend.getInstance().getPlayer().setRealname(this.realname.getText());
                ModuleFriend.getInstance().getPlayer().setDescription(this.description.getText());
                ModuleFriend.getInstance().getPlayer().setProfilePicture(this.profilePicture);
                ModuleFriend.getInstance().saveProfile();
                Minecraft.getMinecraft().displayGuiScreen(this.parent);
            }else
            if (button.id == 2) {
            	JFileChooser fc = new JFileChooser();
            	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            	for (FileFilter filter : fc.getChoosableFileFilters()) {
                	fc.removeChoosableFileFilter(filter);
            	}
            	String[] imgExts = ImageIO.getReaderFileSuffixes();
            	fc.addChoosableFileFilter(new FileNameExtensionFilter("Images (*." + ArrayUtils.join(imgExts, ", *.") + ")", imgExts));
                int returnVal = fc.showOpenDialog(null);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                	File file = fc.getSelectedFile();
                	BufferedImage img = null;
                	try {
						img = ImageIO.read(file);
					} catch (IOException e) {}
                	if (img != null) {
                        int newWidth = img.getWidth();
                        int newHeight = img.getHeight();
                        boolean scale = false;
                		if (img.getWidth() > 100) {
	                        newWidth = 100;
	                        scale = true;
                		}else{
                			newWidth = (int) (((float) img.getWidth()) * (100.0F / ((float) img.getHeight())));
                		}
                		if (img.getHeight() > 100) {
	                        newHeight = 100;
	                        scale = true;
                		}else{
                			newHeight = (int) (((float) img.getHeight()) * (100.0F / ((float) img.getWidth())));
                		}
                		if (scale) {
                			this.profilePicture = this.scaleImage(img, newWidth, newHeight);
                		}else{
                			this.profilePicture = img;
                		}
                	}else{
                		Minecraft.getMinecraft().displayGuiScreen(new GuiMessage(this, "Unable to read the image you selected!"));
                	}
                }
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.realname.textboxKeyTyped(par1, par2);
        this.description.textboxKeyTyped(par1, par2);

        if (par2 == 28 || par2 == 156) {
            this.actionPerformed((GuiButton) this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
        this.realname.mouseClicked(par1, par2, par3);
        this.description.mouseClicked(par1, par2, par3);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
    	this.drawBackground(0);
        int offsetX = 4;
        int offsetCenteredX = this.width / 2;
        int offsetY = 17;
        this.drawCenteredString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.edit_profile"), offsetCenteredX, offsetY, 16777215);
        offsetY += 9;
        offsetY += 9;
        this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.edit_profile.username").replace("%USERNAME%", ModuleFriend.getInstance().getPlayer().getUsername()), offsetX, offsetY, 10526880);
        offsetY += 9;
        offsetY += 9;
        offsetY += 9;
        this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.edit_profile.realname"), offsetX, offsetY, 10526880);
        offsetY += 9;
        offsetY += 9;
        offsetY += 9;
        this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.edit_profile.description"), offsetX, offsetY, 10526880);
        offsetY += 9;
        offsetY += 9;
        offsetY += 9;
        this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.edit_profile.picture"), offsetX, offsetY, 10526880);
        offsetY += 9;
        offsetY += 10;
        if (this.profilePicture != null) {
            TextureManager tm = this.mc.func_110434_K();
        	if (this.lastProfilePicture != this.profilePicture) {
        		this.profilePictureResourceLocation = tm.func_110578_a("profile-picture-" + ModuleFriend.getInstance().getPlayer().getUsername(), new DynamicTexture(this.profilePicture));
        		this.lastProfilePicture = this.profilePicture;
        	}
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            tm.func_110577_a(this.profilePictureResourceLocation);

            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(offsetX,             offsetY + this.profilePicture.getHeight(), this.zLevel, 0, 1);
            tess.addVertexWithUV(offsetX + this.profilePicture.getWidth(), offsetY + this.profilePicture.getHeight(), this.zLevel, 1, 1);
            tess.addVertexWithUV(offsetX + this.profilePicture.getWidth(), offsetY,              this.zLevel, 1, 0);
            tess.addVertexWithUV(offsetX,             offsetY,              this.zLevel, 0, 0);
            tess.draw();
            offsetY += this.profilePicture.getHeight() + 2;
        }
        this.realname.drawTextBox();
        this.description.drawTextBox();
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