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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

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

import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ModuleManager;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class GuiModList extends GuiScreen {
    private GuiScreen parent;
    private GuiSlotModList modList;
    private int selected = -1;
    private Module selectedMod;
    private int listWidth;
    private ArrayList<Module> mods;
    private String title;

    public GuiModList(GuiScreen parent) {
        this.parent = parent;
        this.mods = new ArrayList<Module>();
        for (Object modInst : ModuleManager.getInstance().getModuleOrder()) {
            for (Module modAnnotation : ModuleManager.getInstance().getLoadedModules().keySet()) {
                if (modInst.getClass().equals(ModuleManager.getInstance().getLoadedModules().get(modAnnotation))) {
                    this.mods.add(modAnnotation);
                    break;
                }
            }
        }
        for (Module modAnnotation : ModuleManager.getInstance().getDisabledModules().values()) {
            this.mods.add(modAnnotation);
        }
        for (Module modAnnotation : ModuleManager.getInstance().getUnloadedModules().keySet()) {
            this.mods.add(modAnnotation);
        }
        this.title = Locale.localize("ziahsclient.gui.module_list");
    }

    @Override
    public void initGui() {
        this.listWidth = 200;
        this.buttonList.add(new GuiSmallButton(7, 4, this.height - 52, Locale.localize("ziahsclient.gui.modules.disable")));
        this.buttonList.add(new GuiSmallButton(6, 4, this.height - 28, Locale.localize("ziahsclient.gui.done")));
        this.buttonList.add(new GuiSmallButton(8, 158, this.height - 28, Locale.localize("ziahsclient.gui.modules.download_modules")));
        this.buttonList.add(new GuiSmallButton(9, 158, this.height - 52, Locale.localize("ziahsclient.gui.modules.delete")));
        this.modList = new GuiSlotModList(this, this.mods, this.listWidth);
        this.modList.registerScrollButtons(this.buttonList, 7, 8);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 6:
                    this.mc.displayGuiScreen(this.parent);
                    return;
                case 7:
                    Class modClass = ModuleManager.getInstance().getLoadedModules().get(this.selectedMod);
                    if (modClass == null) {
                        for (Class curModClass : ModuleManager.getInstance().getDisabledModules().keySet()) {
                            if (ModuleManager.getInstance().getDisabledModules().get(curModClass).equals(this.selectedMod)) {
                                modClass = curModClass;
                                break;
                            }
                        }
                    }
                    if (modClass == null) return;
                    if (ModuleManager.getInstance().getDisabledModules().containsValue(this.selectedMod)) {
                        ModuleManager.getInstance().enableModule(modClass);
                    }else
                    if (ModuleManager.getInstance().getUnloadedModules().containsKey(this.selectedMod)) {
                        Map<Module, Class> modules = new HashMap<Module, Class>(ModuleManager.getInstance().getUnloadedModules());
                        ModuleManager.getInstance().getUnloadedModules().clear();
                        ModuleManager.getInstance().getUnloadedModules().put(this.selectedMod, modClass);
                        modules.remove(this.selectedMod);
                        ModuleManager.getInstance().processModuleDependencies();
                        ModuleManager.getInstance().getUnloadedModules().putAll(modules);

                        LinkedList<Object> moduleOrder = new LinkedList<Object>();
                        moduleOrder.add(ModuleManager.getInstance().getModuleInstances().get(modClass));
                        ModuleManager.getInstance().loadModules(moduleOrder);
                    }else{
                        ModuleManager.getInstance().disableModule(modClass, true);
                    }
                    return;
                case 8:
                    this.mc.displayGuiScreen(new GuiRepoModList(this));
                    return;
                case 9:
                    modClass = ModuleManager.getInstance().getAllModules().get(this.selectedMod);
                    File modFile = new File(ZiahsClient.getInstance().getDataDir(), "modules" + File.separator + this.selectedMod.name() + " Module " + this.selectedMod.version() + " MC" + ZiahsClient.getInstance().getMinecraftVersion() + ".jar");
                    if (modFile.exists()) {
                        ModuleManager.getInstance().disableModule(modClass, false);
                        ModuleManager.getInstance().getDisabledModules().remove(modClass);
                        modFile.delete();
                    }else{
                        this.mc.displayGuiScreen(new GuiMessage(this, Locale.localize("ziahsclient.gui.modules.unable_to_delete") + "\n" + Locale.localize("ziahsclient.gui.modules.cant_find_module")));
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
        if (this.buttonList.size() > 0) ((GuiButton) this.buttonList.get(0)).enabled = this.selected != -1;
        if (this.selected != -1) {
            ((GuiButton) this.buttonList.get(3)).enabled = true;
            if (ModuleManager.getInstance().getDisabledModules().containsValue(this.selectedMod)) {
                ((GuiButton) this.buttonList.get(0)).displayString = Locale.localize("ziahsclient.gui.modules.enable");
            }else
            if (ModuleManager.getInstance().getUnloadedModules().containsKey(this.selectedMod)) {
                ((GuiButton) this.buttonList.get(0)).displayString = Locale.localize("ziahsclient.gui.modules.reload");
            }else{
                ((GuiButton) this.buttonList.get(0)).displayString = Locale.localize("ziahsclient.gui.modules.disable");
            }
        }else{
            ((GuiButton) this.buttonList.get(3)).enabled = false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        if (this.modList == null) return;
        this.modList.drawScreen(mouseX, mouseY, tick);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        int offsetX = this.listWidth + 20;
        if (this.selectedMod != null) {
            GL11.glEnable(GL11.GL_BLEND);
            String requires = "";
            String[] requiresOrig = this.selectedMod.requiredModules().split(",");
            int i = 0;
            for (String modId : requiresOrig) {
                if (modId.trim().length() <= 0 || modId == null) continue;
                Module mod = ModuleManager.getInstance().getModule(modId);
                if (mod != null) {
                    requires += mod.name();
                }else{
                    requires += modId;
                }
                if (i < requiresOrig.length - 1) requires += ", ";
                i++;
            }
            if (requires.trim().length() == 0) requires = Locale.localize("ziahsclient.gui.nothing");

            String recommends = "";
            String[] recommendsOrig = this.selectedMod.recommendedModules().split(",");
            i = 0;
            for (String modId : recommendsOrig) {
                if (modId.trim().length() <= 0 || modId == null) continue;
                Module mod = ModuleManager.getInstance().getModule(modId);
                if (mod != null) {
                    recommends += mod.name();
                }else{
                    recommends += modId;
                }
                if (i < recommendsOrig.length - 1) recommends += ", ";
                i++;
            }
            if (recommends.trim().length() == 0) recommends = Locale.localize("ziahsclient.gui.nothing");

            String incompatibles = "";
            String[] incompatiblesOrig = this.selectedMod.incompatibleModules().split(",");
            i = 0;
            for (String modId : incompatiblesOrig) {
                if (modId.trim().length() <= 0 || modId == null) continue;
                Module mod = ModuleManager.getInstance().getModule(modId);
                if (mod != null) {
                    incompatibles += mod.name();
                }else{
                    incompatibles += modId;
                }
                if (i < incompatiblesOrig.length - 1) incompatibles += ", ";
                i++;
            }
            if (incompatibles.trim().length() == 0) incompatibles = Locale.localize("ziahsclient.gui.nothing");

            int offsetY = 0;

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            TextureManager tm = this.mc.func_110434_K();
            String logoBase64 = ModuleManager.getInstance().getModuleLogo(this.selectedMod);
            if (logoBase64 != null) {
                InputStream logoIn = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(logoBase64));

                if (logoIn != null) {
                    try {
                        BufferedImage logo = ImageIO.read(logoIn);
                        ResourceLocation rl = tm.func_110578_a(this.selectedMod.moduleId() + "-logo", new DynamicTexture(logo));
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            offsetY += 35;
            this.drawString(this.fontRenderer, this.selectedMod.name(), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.version").replace("%VERSION%", (ModuleManager.getInstance().isModuleUpToDate(this.selectedMod.moduleId(), this.selectedMod.version()) ? "§2" : "§4") + this.selectedMod.version() + "§r"), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.state").replace("%STATE%", ModuleManager.getInstance().getLoadedModules().containsKey(this.selectedMod) ? Locale.localize("ziahsclient.gui.modules.state.loaded") : (ModuleManager.getInstance().getDisabledModules().containsValue(this.selectedMod) ? Locale.localize("ziahsclient.gui.modules.state.disabled") : Locale.localize("ziahsclient.gui.modules.state.not_loaded"))), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            offsetY += 9;
            
            i = 0;
            for (String curStr : ((List<String>) this.fontRenderer.listFormattedStringToWidth(Locale.localize("ziahsclient.gui.modules.description").replace("%DESCRIPTION%", this.selectedMod.description()), (this.width - 5) - offsetX))) {
                this.drawString(this.fontRenderer, curStr, offsetX, offsetY, 0xDDDDDD);
                offsetY += 9;
                i++;
            }
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.required").replace("%REQUIRED%", requires), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.recommended").replace("%RECOMMENDED%", recommends), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.incompatible").replace("%INCOMPATIBLE%", incompatibles), offsetX, offsetY, 0xDDDDDD);
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

    public void selectModIndex(int var1) {
        this.selected = var1;
        if (var1 >= 0 && var1 <= this.mods.size()) {
            this.selectedMod = this.mods.get(this.selected);
        }else{
            this.selectedMod = null;
        }
    }

    public boolean modIndexSelected(int var1) {
        return var1 == this.selected;
    }
}