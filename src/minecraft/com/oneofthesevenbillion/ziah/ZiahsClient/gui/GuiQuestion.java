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

import java.lang.reflect.Method;
import java.util.logging.Level;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;

import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class GuiQuestion extends GuiScreen {
    private GuiScreen parent;
    private String message;
    private String button0Text;
    private String button1Text;
    private Method callback;
    private Object callbackObj;

    public GuiQuestion(GuiScreen parent, String message, String button0Text, String button1Text, Method callback, Object callbackObj) {
        this.parent = parent;
        this.message = message;
        this.button0Text = button0Text;
        this.button1Text = button1Text;
        this.callback = callback;
        this.callbackObj = callbackObj;
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiSmallButton(0, this.width / 2 - 75 - 75 - 2, this.height / 2 - 10 + 4 + (this.message.split("\n").length * 9 / 2), this.button0Text));
        this.buttonList.add(new GuiSmallButton(1, this.width / 2 - 75 + 75 + 2, this.height / 2 - 10 + 4 + (this.message.split("\n").length * 9 / 2), this.button1Text));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 0:
                    this.mc.displayGuiScreen(this.parent);
					try {
						this.callback.invoke(this.callbackObj, 0);
					} catch (Exception e) {
					    ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when calling callback function!", e);
					}
                    return;
                case 1:
                    this.mc.displayGuiScreen(this.parent);
					try {
						this.callback.invoke(this.callbackObj, 1);
					} catch (Exception e) {
					    ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when calling callback function!", e);
					}
                    return;
            }
        }
        super.actionPerformed(button);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        this.drawDefaultBackground();
        int i = 0;
        for (String curStr : this.message.split("\n")) {
            if (curStr == null || curStr.trim().length() == 0) continue;
            this.drawCenteredString(this.fontRenderer, curStr, this.width / 2, this.height / 2 - 20 - (this.message.split("\n").length * 9 / 2) + i * 9, 0xFFFFFF);
            i++;
        }
        super.drawScreen(mouseX, mouseY, tick);
    }
}