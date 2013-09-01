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
import net.minecraft.src.ModLoader;

import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class GuiZiahsClient extends GuiScreen {
    private GuiScreen parent;
    private int lastWidth = 0;
    private int lastHeight = 0;
    private boolean initialized = false;
    private String title;

    public GuiZiahsClient(GuiScreen parent) {
        this.parent = parent;
        this.title = Locale.localize("ziahsclient.gui.menu");
    }

    @Override
    public void initGui() {
        this.buttonList.addAll(ZiahsClient.getInstance().getMenuButtons());
        this.initialized = true;
    }

    @Override
    public void updateScreen() {
        if (!this.initialized) return;
        if (this.lastWidth != this.width || this.lastHeight != this.height) {
            int x = 4;
            int y = this.height - 20 - 8;
            for (Object obj : this.buttonList) {
                if (obj instanceof GuiButton) {
                    GuiButton button = (GuiButton) obj;
                    int width = 200;
                    int height = 20;
                    try {
                        width = (Integer) ModLoader.getPrivateValue(GuiButton.class, button, 1);
                        height = (Integer) ModLoader.getPrivateValue(GuiButton.class, button, 2);
                    } catch (Exception e) {}
                    if (y - (height + 4) < 20) {
                        y = this.height - 20 - 8;
                        x += width + 4;
                    }
                    button.xPosition = x;
                    button.yPosition = y;
                    y -= height + 4;
                }
            }

            this.lastWidth = this.width;
            this.lastHeight = this.height;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(this.parent);
        }else{
            try {
                ZiahsClient.getInstance().getMenuButtonIdToMethod().get(button.id).invoke(ZiahsClient.getInstance().getMenuButtonIdToClickMethodObject().get(button.id));
            } catch (Exception e) {}
        }
    }
}