package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketChat;
import com.oneofthesevenbillion.ziah.FriendModule.network.PacketManager;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiQuestion;

public class GuiAvailableFriends extends GuiScreen {
    private GuiScreen parent;
    private GuiSlotAvailableFriends friendList;
    private int selected = -1;
    private Friend selectedFriend;
    private int listWidth;
    private List<Friend> friends;
    private String title;

    public GuiAvailableFriends(GuiScreen parent, List<Friend> friends) {
        this.parent = parent;
        this.friends = new ArrayList<Friend>(friends);
        this.title = "Find More Friends";

        for (Friend friend2 : ModuleFriend.getInstance().getFriends()) {
            for (Friend friend : this.friends) {
            	if (friend.getUsername().equalsIgnoreCase(friend2.getUsername())) {
            		this.friends.remove(friend);
            	}
            }
    	}
    }

    @Override
    public void initGui() {
        this.listWidth = 200;
        this.buttonList.add(new GuiSmallButton(0, 4, this.height - 28, 130, 20, "Done"));
        this.buttonList.add(new GuiSmallButton(1, 4, this.height - 52, 130, 20, "Add Friend"));
        this.friendList = new GuiSlotAvailableFriends(this, this.friends, this.listWidth);
        this.friendList.registerScrollButtons(this.buttonList, 7, 8);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 0:
                    this.mc.displayGuiScreen(this.parent);
                    return;
                case 1:
                    try {
                        this.mc.displayGuiScreen(new GuiQuestion(this, "Are you sure you want to add " + this.selectedFriend.getUsername() + " to your friends list?", "Yes", "No", this.getClass().getDeclaredMethod("friendCallback", Integer.class), this));
                    } catch (NoSuchMethodException e) {
                        // Impossible
                    } catch (SecurityException e) {
                        // Impossible
                    }
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
        boolean isFriended = false;
        if (this.selectedFriend != null) {
	        for (Friend friend : ModuleFriend.getInstance().getFriends()) {
	        	if (friend.getUsername().equalsIgnoreCase(this.selectedFriend.getUsername())) {
	        		isFriended = true;
	        		break;
	        	}
	        }
        }
        ((GuiButton) this.buttonList.get(1)).enabled = this.selected != -1 && !isFriended;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        if (this.friendList == null) return;
        this.friendList.drawScreen(mouseX, mouseY, tick);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        int offsetX = this.listWidth + 20;
        if (this.selectedFriend != null) {
            GL11.glEnable(GL11.GL_BLEND);
            int offsetY = 35;

            if (this.selectedFriend.hasProfilePicture()) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                TextureManager tm = this.mc.func_110434_K();
                BufferedImage logo = this.selectedFriend.getProfilePicture();
                ResourceLocation rl = tm.func_110578_a("profile-picture-" + this.selectedFriend.getUsername(), new DynamicTexture(logo));
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

            this.drawString(this.fontRenderer, "Username: " + this.selectedFriend.getUsername(), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, "Realname: " + this.selectedFriend.getRealname(), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            offsetY += 9;
            
            int i = 0;
            for (String curStr : ((List<String>) this.fontRenderer.listFormattedStringToWidth("Description: " + this.selectedFriend.getDescription(), (this.width - 5) - offsetX))) {
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

    public boolean friendIndexSelected(int var1) {
        return var1 == this.selected;
    }

    public void friendCallback(Integer button) {
        switch (button) {
            case 0:
            	ModuleFriend.getInstance().getFriends().add(this.selectedFriend);
                this.friends.remove(this.selectedFriend);
                this.selectFriendIndex(-1);
                break;
        }
    }
}