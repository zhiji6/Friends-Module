package com.oneofthesevenbillion.ziah.FriendModule.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.DynamicTexture;
import net.minecraft.src.EnumChatFormatting;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TextureManager;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.FriendModule.Friend;
import com.oneofthesevenbillion.ziah.FriendModule.ModuleFriend;
import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiQuestion;

public class GuiAvailableFriends extends GuiScreen {
    private GuiScreen parent;
    private GuiSlotAvailableFriends friendList;
    private int selected = -1;
    private Friend selectedFriend;
    private int listWidth;
    private List<Friend> friends;
    private String title;
    private GuiTextField search;
    private String message;

    public GuiAvailableFriends(GuiScreen parent) {
        this.parent = parent;
        this.friends = new ArrayList<Friend>();
        this.title = "Find More Friends";
    }

    @Override
    public void initGui() {
        this.listWidth = 200;
        this.buttonList.add(new GuiSmallButton(0, 4, this.height - 28, 130, 20, "Done"));
        this.buttonList.add(new GuiSmallButton(1, 4, this.height - 52, 130, 20, "Add Friend"));
        this.friendList = new GuiSlotAvailableFriends(this, this.friends, this.listWidth);
        this.friendList.registerScrollButtons(this.buttonList, 7, 8);
        this.search = new GuiTextField(this.fontRenderer, 10, 16 + 9 + 4 + 9 + 4, 200, 20);
        this.search.setFocused(true);
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
        this.search.updateCursorCounter();
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
        this.drawString(this.fontRenderer, "Search:", 10, 16 + 9 + 4, 0xFFFFFF);
        if (this.friends.isEmpty() && this.message != null && this.message.length() > 0) this.drawCenteredString(this.fontRenderer, this.message, 10 + (this.listWidth / 2), this.height / 2, 0xFFFFFF);
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
            this.drawString(this.fontRenderer, "Status: " + (this.selectedFriend.getStatus() ? EnumChatFormatting.GREEN + "Online" : EnumChatFormatting.RED + "Offline") + EnumChatFormatting.RESET, offsetX, offsetY, 0xDDDDDD);
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
        this.search.drawTextBox();
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

    @Override
    public void keyTyped(char character, int characterCode) {
        super.keyTyped(character, characterCode);
        this.search.textboxKeyTyped(character, characterCode);

        if (this.search.isFocused() && characterCode == Keyboard.KEY_RETURN) {
        	this.friends.clear();
        	this.message = null;
    		Map<String, String> map = new HashMap<String, String>();
    		map.put("query", this.search.getText());
        	try {
				Map<String, Object> response = ModuleFriend.getInstance().runFriendServerAction("search", map);
				if (((Double) response.get("status")).intValue() == 0) {
					List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

					for (Map<String, Object> friend : results) {
						Friend friendObj = new Friend((String) friend.get("username"), (String) friend.get("realname"), (String) friend.get("description"), Integer.parseInt((String) friend.get("id")), ImageIO.read(new ByteArrayInputStream(((String) friend.get("picture")).getBytes())));
						friendObj.setStatus((Boolean) friend.get("status"));
						friendObj.setUpdateTime(Integer.parseInt((String) friend.get("updatetime")));
						this.friends.add(friendObj);
					}
				}else{
	            	this.message = (String) response.get("message");
				}
    		} catch (IOException e) {
    			ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when searching!", e);
    		}

	        for (Friend friend : new ArrayList<Friend>(this.friends)) {
	        	if (friend.getUsername().equalsIgnoreCase(ModuleFriend.getInstance().getPlayer().getUsername())) {
	        		this.friends.remove(friend);
	        	}
	        }
	
	        for (Friend friend2 : new ArrayList<Friend>(ModuleFriend.getInstance().getFriends())) {
	            for (Friend friend : new ArrayList<Friend>(this.friends)) {
	            	if (friend.getUsername().equalsIgnoreCase(friend2.getUsername())) {
	            		this.friends.remove(friend);
	            	}
	            }
	    	}

	        if (this.selected < this.friends.size()) {
	        	this.selectFriendIndex(this.selected);
	        }else{
	        	this.selectFriendIndex(-1);
	        }
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        super.mouseClicked(par1, par2, par3);
        this.search.mouseClicked(par1, par2, par3);
    }
}