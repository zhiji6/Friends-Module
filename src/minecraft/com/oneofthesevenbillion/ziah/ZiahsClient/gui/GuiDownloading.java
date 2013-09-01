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

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;

import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.ThreadDownloading;
import com.oneofthesevenbillion.ziah.ZiahsClient.util.Utils;

public class GuiDownloading extends GuiScreen {
    private ResourceLocation grass = new ResourceLocation("textures/blocks/grass_side.png");
    private GuiScreen parent;
    private String message;
    private float percentage = 0;
    public ThreadDownloading dlthread;

    public GuiDownloading(GuiScreen parent) {
        this.parent = parent;
        this.message = Locale.localize("ziahsclient.gui.downloading");
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiSmallButton(0, this.width / 2 - 75, this.height / 2 - 10 + 4, Locale.localize("ziahsclient.gui.cancel")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 0:
                    this.dlthread.interrupt();
                    this.dlthread.file.delete();
                    this.mc.displayGuiScreen(this.getParent());
                    return;
            }
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.message, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        int progressBarX = this.width / 2 - 75 - 22;
        int progressBarY = this.height / 2 - 30;
        int progressBarX2 = this.width / 2 - 75 + 150 + 21;
        int progressBarY2 = this.height / 2 - 13;
        this.drawHorizontalLine(progressBarX2, progressBarX, progressBarY, 0xFF888888);
        this.drawHorizontalLine(progressBarX2, progressBarX, progressBarY2, 0xFF888888);
        this.drawVerticalLine(progressBarX, progressBarY, progressBarY2, 0xFF888888);
        this.drawVerticalLine(progressBarX2, progressBarY, progressBarY2, 0xFF888888);
        Minecraft.getMinecraft().func_110434_K().func_110577_a(this.grass);
        int x = progressBarX + 1;
        int y = progressBarY + 1;
        for (int i = 0; i < (this.percentage / 100.0F) * 12; i++) {
            Utils.drawTexturedModalRect(x + i * 16, y, 16, 16);
        }
        //this.drawGradientRect(this.width / 2 - 75 - 20 + 1, this.height / 2 - 30 + (this.message.split("\n").length * 9 / 2) + 1, this.width / 2 - 75 - 20 + 1 + 189, this.height / 2 - 30 + (this.message.split("\n").length * 9 / 2) + 1 + 16, 0xFF00BE00, 0xFF007300);
        super.drawScreen(mouseX, mouseY, tick);
    }

    public void finish() {
        this.mc.displayGuiScreen(new GuiMessage(this.getParent(), "Download complete!"));
    }

    public void percentageUpdate(float percentage) {
        this.percentage = percentage;
    }

    public GuiScreen getParent() {
        return this.parent;
    }
}