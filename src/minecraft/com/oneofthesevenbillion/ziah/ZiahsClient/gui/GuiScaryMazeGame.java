/*
Ziah_'s Client
Copyright (C) 2013  Ziah Jyothi

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see [http://www.gnu.org/licenses/].
*/

package com.oneofthesevenbillion.ziah.ZiahsClient.gui;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.ZiahsClient.util.Utils;

public class GuiScaryMazeGame extends GuiScreen {
    private static ResourceLocation texture = new ResourceLocation("ziahsclient/textures/gui/scarymazegame.png");
    private GuiScreen parent;
    private long openTime = 0;

    public GuiScaryMazeGame(GuiScreen parent) {
        this.parent = parent;
        this.openTime = System.currentTimeMillis();
    }

    @Override
    public void initGui() {
        Minecraft.getMinecraft().sndManager.playSound("ziahsclient.scarymazegame", 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        this.drawRect(0, 0, this.width, this.height, 0xff000000);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.func_110434_K().func_110577_a(GuiScaryMazeGame.texture);
        int imgwidth = (int) (this.height * 1.33611691023);
        int imgheight = (int) (this.width * 0.7484375);
        if (this.height > this.width || imgwidth > imgheight) {
            imgheight = this.height;
        }else{
            imgwidth = this.width;
        }
        Utils.drawTexturedModalRect(-((imgwidth - this.width) / 2), -((imgheight - this.height) / 2), imgwidth, imgheight);
        super.drawScreen(mouseX, mouseY, tick);
    }

    @Override
    public void updateScreen() {
        if (System.currentTimeMillis() >= this.openTime + 1000) {
            this.mc.displayGuiScreen(this.parent);
        }
    }
}