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

import java.util.List;

import net.minecraft.src.Tessellator;

import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ModuleManager;
import com.oneofthesevenbillion.ziah.ZiahsClient.RepoMod;

public class GuiSlotRepoModList extends GuiScrollingList {
    private GuiRepoModList parent;
    private List<RepoMod> mods;

    public GuiSlotRepoModList(GuiRepoModList parent, List<RepoMod> mods, int listWidth) {
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
        RepoMod mod = this.mods.get(listIndex);
        Module modAnnot = ModuleManager.getInstance().getModule(mod.getId());
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(mod.getName(), this.listWidth - 10), this.left + 3, var3 + 2, 0xFFFFFF);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(Locale.localize("ziahsclient.gui.modules.release_version").replace("%VERSION%", mod.getVersion()), this.listWidth - 10), this.left + 3, var3 + 12, 0xCCCCCC);
        this.parent.getFontRenderer().drawString(this.parent.getFontRenderer().trimStringToWidth(Locale.localize("ziahsclient.gui.modules.install_status" + (modAnnot != null ? "_and_update_status" : "")).replace("%INSTALL_STATUS%", modAnnot != null ? Locale.localize("ziahsclient.gui.modules.installed") : Locale.localize("ziahsclient.gui.modules.not_installed")).replace("%UPDATE_STATUS%", modAnnot != null && ModuleManager.getInstance().isModuleUpToDate(mod.getId(), modAnnot.version()) ? Locale.localize("ziahsclient.gui.modules.up_to_date") : Locale.localize("ziahsclient.gui.modules.out_of_date")), this.listWidth - 10), this.left + 3, var3 + 22, 0xCCCCCC);
    }
}