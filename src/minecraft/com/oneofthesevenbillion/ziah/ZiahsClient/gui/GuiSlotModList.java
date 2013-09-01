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

import java.util.ArrayList;

import net.minecraft.src.Tessellator;

import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ModuleManager;

public class GuiSlotModList extends GuiScrollingList {
    private GuiModList parent;
    private ArrayList<Module> mods;

    public GuiSlotModList(GuiModList parent, ArrayList<Module> mods, int listWidth) {
        super(parent.getMinecraftInstance(), listWidth, parent.height, 32, parent.height - 65 + 4, 10, 35);
        this.parent = parent;
        this.mods = mods;
    }

    @Override
    protected int getSize() {
        return this.mods.size();
    }

    @Override
    protected void elementClicked(int var1, boolean var2) {
        this.parent.selectModIndex(var1);
    }

    @Override
    protected boolean isSelected(int var1) {
        return this.parent.modIndexSelected(var1);
    }

    @Override
    protected void drawBackground() {
        this.parent.drawDefaultBackground();
    }

    @Override
    protected int getContentHeight() {
        return (this.getSize()) * 35 + 1;
    }

    @Override
    protected void drawSlot(int listIndex, int var2, int var3, int var4, Tessellator var5) {
        Module mod = this.mods.get(listIndex);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(mod.name(), this.listWidth - 10), this.left + 3, var3 + 2, 0xFFFFFF);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth((ModuleManager.getInstance().isModuleUpToDate(mod.moduleId(), mod.version()) ? "§2" : "§4") + mod.version() + "§r", this.listWidth - 10), this.left + 3, var3 + 12, 0xCCCCCC);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(Locale.localize("ziahsclient.gui.modules.state").replace("%STATE%", ModuleManager.getInstance().getLoadedModules().containsKey(mod) ? Locale.localize("ziahsclient.gui.modules.state.loaded") : (ModuleManager.getInstance().getDisabledModules().containsValue(mod) ? Locale.localize("ziahsclient.gui.modules.state.disabled") : Locale.localize("ziahsclient.gui.modules.state.not_loaded"))), this.listWidth - 10), this.left + 3, var3 + 22, 0xCCCCCC);
    }
}