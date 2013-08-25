package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

import net.minecraft.src.DynamicTexture;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TextureManager;

import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.FriendModule.Friend;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiQuestion;

public class GuiFriends extends GuiScreen {
    private GuiScreen parent;
    private GuiSlotFriends friendList;
    private int selected = -1;
    private Friend selectedFriend;
    private int listWidth;
    private List<Friend> friends;
    private String title;

    public GuiFriends(GuiScreen parent, List<Friend> friends) {
        this.parent = parent;
        this.friends = friends;
        this.title = Locale.localize("ziahsclient.gui.friends");
    }

    @Override
    public void initGui() {
        this.listWidth = 200;
        this.buttonList.add(new GuiSmallButton(7, 312, this.height - 28, Locale.localize("ziahsclient.gui.friends.block")));
        this.buttonList.add(new GuiSmallButton(6, 4, this.height - 28, Locale.localize("ziahsclient.gui.done")));
        this.buttonList.add(new GuiSmallButton(8, 158, this.height - 28, Locale.localize("ziahsclient.gui.friends.open_ip_menu")));
        this.buttonList.add(new GuiSmallButton(9, 158, this.height - 52, Locale.localize("ziahsclient.gui.friends.unfriend")));
        this.buttonList.add(new GuiSmallButton(10, 4, this.height - 52, Locale.localize("ziahsclient.gui.friends.disable_punch_protection")));
        this.friendList = new GuiSlotFriends(this, this.friends, this.listWidth);
        this.friendList.registerScrollButtons(this.buttonList, 7, 8);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 6:
                    this.mc.displayGuiScreen(this.parent);
                    return;
                case 7:
                    if (!this.selectedFriend.isBlocked()) {
                        try {
                            this.mc.displayGuiScreen(new GuiQuestion(this, Locale.localize("ziahsclient.gui.friends.are_you_sure_block").replace("%USERNAME%", this.selectedFriend.getUsername()), Locale.localize("ziahsclient.gui.yes"), Locale.localize("ziahsclient.gui.no"), this.getClass().getDeclaredMethod("blockCallback", Integer.class), this));
                        } catch (NoSuchMethodException e) {
                            // Impossible
                        } catch (SecurityException e) {
                            // Impossible
                        }
                    }else{
                        this.selectedFriend.toggleBlockedStatus();
                    }
                    return;
                case 8:
                    this.mc.displayGuiScreen(new GuiIPMenu());
                    return;
                case 9:
                    try {
                        this.mc.displayGuiScreen(new GuiQuestion(this, Locale.localize("ziahsclient.gui.friends.are_you_sure_unfriend").replace("%USERNAME%", this.selectedFriend.getUsername()), Locale.localize("ziahsclient.gui.yes"), Locale.localize("ziahsclient.gui.no"), this.getClass().getDeclaredMethod("unfriendCallback", Integer.class), this));
                    } catch (NoSuchMethodException e) {
                        // Impossible
                    } catch (SecurityException e) {
                        // Impossible
                    }
                    return;
                case 10:
                    this.selectedFriend.togglePunchProtection();
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
        ((GuiButton) this.buttonList.get(0)).enabled = this.selected != -1;
        ((GuiButton) this.buttonList.get(3)).enabled = this.selected != -1;
        ((GuiButton) this.buttonList.get(4)).enabled = this.selected != -1;
        if (this.selected != -1) {
            ((GuiButton) this.buttonList.get(0)).displayString = Locale.localize(this.selectedFriend.isBlocked() ? "ziahsclient.gui.friends.unblock" : "ziahsclient.gui.friends.block");
            ((GuiButton) this.buttonList.get(4)).displayString = Locale.localize(this.selectedFriend.isPunchProtectionEnabled() ? "ziahsclient.gui.friends.disable_punch_protection" : "ziahsclient.gui.friends.enable_punch_protection");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        if (this.friendList == null) return;
        this.friendList.drawScreen(mouseX, mouseY, tick);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        int offsetX = this.listWidth + 20;
        if (this.selectedFriend != null) {
            GL11.glEnable(GL11.GL_BLEND);
            int offsetY = 0;

            if (this.selectedFriend.hasProfilePicture()) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                TextureManager tm = this.mc.func_110434_K();
                BufferedImage logo = this.selectedFriend.getProfilePicture();
                ResourceLocation rl = tm.func_110578_a("profile-picture-" + String.valueOf(this.selectedFriend.getUID()), new DynamicTexture(logo));
                tm.func_110577_a(rl);

                Dimension dim = new Dimension(logo.getWidth(), logo.getHeight());
                double scaleX = dim.width / 200.0;
                double scaleY = dim.height / 65.0;
                double scale = 1.0;
                if (scaleX > 1 || scaleY > 1) {
                    scale = 1.0 / Math.max(scaleX, scaleY);
                }
                dim.width *= scale;
                dim.height *= scale;
                int top = 32;
                Tessellator tess = Tessellator.instance;
                tess.startDrawingQuads();
                tess.addVertexWithUV(offsetX,             top + dim.height, this.zLevel, 0, 1);
                tess.addVertexWithUV(offsetX + dim.width, top + dim.height, this.zLevel, 1, 1);
                tess.addVertexWithUV(offsetX + dim.width, top,              this.zLevel, 1, 0);
                tess.addVertexWithUV(offsetX,             top,              this.zLevel, 0, 0);
                tess.draw();
                offsetY += dim.height + 2;
            }

            offsetY += 35;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.username").replace("%USERNAME%", this.selectedFriend.getUsername()), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.friends.realname").replace("%REALNAME%", this.selectedFriend.getRealname()), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            this.drawString(this.fontRenderer, this.selectedFriend.isBlocked() ? Locale.localize("ziahsclient.gui.friends.blocked") : Locale.localize("ziahsclient.gui.friends.not_blocked"), offsetX, offsetY, this.selectedFriend.isBlocked() ? 0xFFFF5555 : 0xFF00AA00);
            offsetY += 9;
            this.drawString(this.fontRenderer, this.selectedFriend.isPunchProtectionEnabled() ? Locale.localize("ziahsclient.gui.friends.punch_protection_enabled") : Locale.localize("ziahsclient.gui.friends.punch_protection_disabled"), offsetX, offsetY, this.selectedFriend.isPunchProtectionEnabled() ? 0xFF00AA00 : 0xFFFF5555);
            offsetY += 9;
            offsetY += 9;
            
            int i = 0;
            for (String curStr : ((List<String>) this.fontRenderer.listFormattedStringToWidth(Locale.localize("ziahsclient.gui.friends.description").replace("%DESCRIPTION%", this.selectedFriend.getDescription()), (this.width - 5) - offsetX))) {
                this.drawString(this.fontRenderer, curStr, offsetX, offsetY, 0xDDDDDD);
                offsetY += 9;
                i++;
            }
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

    public void selectFriendIndex(int var1) {
        this.selected = var1;
        if (var1 >= 0 && var1 <= this.friends.size()) {
            this.selectedFriend = this.friends.get(this.selected);
        }else{
            this.selectedFriend = null;
        }
    }

    public boolean modIndexSelected(int var1) {
        return var1 == this.selected;
    }

    public void blockCallback(Integer button) {
        switch (button) {
            case 0:
                this.selectedFriend.toggleBlockedStatus();
                break;
        }
    }

    public void unfriendCallback(Integer button) {
        switch (button) {
            case 0:
                this.selectedFriend.unfriend();
                break;
        }
    }
}