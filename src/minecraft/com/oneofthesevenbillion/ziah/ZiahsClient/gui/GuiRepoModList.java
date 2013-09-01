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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.Minecraft;

import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.ZiahsClient.Locale;
import com.oneofthesevenbillion.ziah.ZiahsClient.Module;
import com.oneofthesevenbillion.ziah.ZiahsClient.ModuleManager;
import com.oneofthesevenbillion.ziah.ZiahsClient.RepoMod;
import com.oneofthesevenbillion.ziah.ZiahsClient.ThreadDownloading;
import com.oneofthesevenbillion.ziah.ZiahsClient.ZiahsClient;

public class GuiRepoModList extends GuiScreen {
    private GuiScreen parent;
    private GuiSlotRepoModList modList;
    private int selected = -1;
    private int lastWidth = -1;
    private int lastHeight = -1;
    private RepoMod selectedMod;
    private int listWidth;
    private List<RepoMod> mods;
    private String title;
    private String requires = "";
    private String recommends = "";
    private String incompatible = "";
    private List<String> descriptionLines = new ArrayList<String>();

    public GuiRepoModList(GuiScreen parent) {
        this.parent = parent;
        ModuleManager.getInstance().loadRepoMods();
        this.mods = new ArrayList<RepoMod>(ModuleManager.getInstance().getModuleRepoData().values());
        this.title = Locale.localize("ziahsclient.gui.modules.download_modules");
    }

    @Override
    public void initGui() {
        this.listWidth = 200;
        this.buttonList.add(new GuiSmallButton(7, 4, this.height - 52, Locale.localize("ziahsclient.gui.modules.download")));
        this.buttonList.add(new GuiSmallButton(6, 4, this.height - 28, Locale.localize("ziahsclient.gui.done")));
        this.buttonList.add(new GuiSmallButton(8, 158, this.height - 28, Locale.localize("ziahsclient.gui.modules.update")));
        this.modList = new GuiSlotRepoModList(this, this.mods, this.listWidth);
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
                    Module modAnnot = ModuleManager.getInstance().getModule(this.selectedMod.getId());
                    if (modAnnot == null) {
                        try {
                            GuiDownloading gui = new GuiDownloading(this);
                            ThreadDownloading thread = new ThreadDownloading(new File(ZiahsClient.getInstance().getDataDir(), "modules" + File.separator + this.selectedMod.getName() + " Module " + this.selectedMod.getVersion() + " MC" + ZiahsClient.getInstance().getMinecraftVersion() + ".jar"), new URL("http://oneofthesevenbillion.com/ziah/zcmodules/" + URLEncoder.encode(this.selectedMod.getId(), "UTF-8") + "/" + URLEncoder.encode(this.selectedMod.getVersion(), "UTF-8") + ".jar"), gui) {
                                @Override
                                public void onComplete() {
                                    try {
                                        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                                        method.setAccessible(true);
                                        method.invoke(ModuleManager.getInstance().getModuleClassLoader(), this.file.toURI().toURL());
                                        ModuleManager.getInstance().getModuleFiles().add(this.file);
                                        try {
                                            ModuleManager.getInstance().getDetector().detect(new File[] {this.file});
                                        } catch (IOException e) {
                                            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when searching for modules in classpath!", e);
                                        }
                                    } catch (NoSuchMethodException e) {
                                        // Impossible
                                    } catch (MalformedURLException e) {
                                        // Impossible
                                    } catch (IllegalAccessException e) {
                                        // Impossible
                                    } catch (IllegalArgumentException e) {
                                        // Impossible
                                    } catch (Exception e) {
                                        ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when loading module!", e);
                                    }
                                }
                            };
                            gui.dlthread = thread;
                            thread.start();
                            this.mc.displayGuiScreen(gui);
                        } catch (Exception e) {
                            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when downloading module!", e);
                        }
                    }
                    return;
                case 8:
                    modAnnot = ModuleManager.getInstance().getModule(this.selectedMod.getId());
                    if (modAnnot != null) {
                        Class modClass = ModuleManager.getInstance().getAllModules().get(modAnnot);
                        ModuleManager.getInstance().disableModule(modClass, false);
                        File modFile = new File(ZiahsClient.getInstance().getDataDir(), "modules" + File.separator + modAnnot.name() + " Module " + modAnnot.version() + " MC" + ZiahsClient.getInstance().getMinecraftVersion() + ".jar");
                        if (modFile.exists()) {
                            modFile.delete();
                            ModuleManager.getInstance().getDisabledModules().remove(modClass);
                        }else{
                            this.mc.displayGuiScreen(new GuiMessage(this, Locale.localize("ziahsclient.gui.modules.unable_to_delete") + "\n" + Locale.localize("ziahsclient.gui.modules.cant_find_module_update")));
                            return;
                        }
                        try {
                            GuiDownloading gui = new GuiDownloading(this);
                            ThreadDownloading thread = new ThreadDownloading(new File(ZiahsClient.getInstance().getDataDir(), "modules" + File.separator + this.selectedMod.getName() + " Module " + this.selectedMod.getVersion() + " MC" + ZiahsClient.getInstance().getMinecraftVersion() + ".jar"), new URL("http://oneofthesevenbillion.com/ziah/zcmodules/" + URLEncoder.encode(this.selectedMod.getId(), "UTF-8") + "/" + URLEncoder.encode(this.selectedMod.getVersion(), "UTF-8") + ".jar"), gui) {
                                @Override
                                public void onComplete() {
                                    try {
                                        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                                        method.setAccessible(true);
                                        method.invoke(ModuleManager.getInstance().getModuleClassLoader(), this.file.toURI().toURL());
                                        ModuleManager.getInstance().getModuleFiles().add(this.file);
                                        try {
                                            ModuleManager.getInstance().getDetector().detect(new File[] {this.file});
                                        } catch (IOException e) {
                                            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when searching for modules in classpath!", e);
                                        }
                                    } catch (NoSuchMethodException e) {
                                        // Impossible
                                    } catch (MalformedURLException e) {
                                        // Impossible
                                    } catch (IllegalAccessException e) {
                                        // Impossible
                                    } catch (IllegalArgumentException e) {
                                        // Impossible
                                    } catch (Exception e) {
                                        ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when loading module!", e);
                                    }
                                }
                            };
                            gui.dlthread = thread;
                            thread.start();
                            this.mc.displayGuiScreen(gui);
                        } catch (Exception e) {
                            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when downloading module!", e);
                        }
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
        ((GuiButton) this.buttonList.get(0)).enabled = this.selected != -1;
        if (this.selected != -1) {
            Module module = ModuleManager.getInstance().getModule(this.selectedMod.getId());
            if (module != null) {
                ((GuiButton) this.buttonList.get(0)).enabled = false;
                ((GuiButton) this.buttonList.get(2)).enabled = !ModuleManager.getInstance().isModuleUpToDate(module.moduleId(), module.version());
            }else{
                ((GuiButton) this.buttonList.get(0)).enabled = true;
                ((GuiButton) this.buttonList.get(2)).enabled = false;
            }
        }else{
            ((GuiButton) this.buttonList.get(2)).enabled = false;
        }
        if (this.width != this.lastWidth || this.height != this.lastHeight) {
            this.lastWidth = this.width;
            this.lastHeight = this.height;
            this.processDescription();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tick) {
        if (this.modList == null) return;
        this.modList.drawScreen(mouseX, mouseY, tick);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        int offsetX = this.listWidth + 20;
        if (this.selectedMod != null) {
            Module modAnnot = ModuleManager.getInstance().getModule(this.selectedMod.getId());
            GL11.glEnable(GL11.GL_BLEND);
            int offsetY = 35;
            this.drawString(this.fontRenderer, this.selectedMod.getName(), offsetX, offsetY, 0xFFFFFF);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.install_status").replace("%INSTALL_STATUS%", modAnnot != null ? Locale.localize("ziahsclient.gui.modules.installed") : Locale.localize("ziahsclient.gui.modules.not_installed")), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.release_version").replace("%VERSION%", this.selectedMod.getVersion()), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            if (modAnnot != null) {
                this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.installed_version").replace("%VERSION%", modAnnot.version()), offsetX, offsetY, 0xDDDDDD);
                offsetY += 9;
                if (!ModuleManager.getInstance().isModuleUpToDate(this.selectedMod.getId(), modAnnot.version())) {
                    this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.new_version").replace("%MODULE%", this.selectedMod.getName()).replace("%VERSION%", this.selectedMod.getVersion()), offsetX, offsetY, 0xDDDDDD);
                    offsetY += 9;
                }
            }
            offsetY += 9;

            int i = 0;
            for (String curStr : this.descriptionLines) {
                this.drawString(this.fontRenderer, curStr, offsetX, offsetY, 0xDDDDDD);
                offsetY += 9;
                i++;
            }
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.required").replace("%REQUIRED%", this.requires), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.recommended").replace("%RECOMMENDED%", this.recommends), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            this.drawString(this.fontRenderer, Locale.localize("ziahsclient.gui.modules.incompatible").replace("%INCOMPATIBLE%", this.incompatible), offsetX, offsetY, 0xDDDDDD);
            offsetY += 9;
            GL11.glDisable(GL11.GL_BLEND);
        }
        super.drawScreen(mouseX, mouseY, tick);
    }

    private RepoMod findRepoMod(String modId) {
        for (RepoMod mod : this.mods) {
            if (mod.getId().equalsIgnoreCase(modId)) {
                return mod;
            }
        }

        return null;
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
        this.processDependencies();
        this.processDescription();
    }

    public boolean modIndexSelected(int var1) {
        return var1 == this.selected;
    }

    public void processDescription() {
        if (this.selectedMod == null) return;
        this.descriptionLines = (this.fontRenderer.listFormattedStringToWidth(Locale.localize("ziahsclient.gui.modules.description").replace("%DESCRIPTION%", this.selectedMod.getDescription()), (this.width - 5) - (this.listWidth + 20)));
    }

    public void processDependencies() {
        if (this.selectedMod == null) return;
        this.requires = "";
        String[] requiresOrig = this.selectedMod.getRequiredModules().split(",");
        int i = 0;
        for (String modId : requiresOrig) {
            if (modId.trim().length() <= 0 || modId == null) continue;
            RepoMod mod = this.findRepoMod(modId);
            if (mod != null) {
                this.requires += mod.getName();
            }else{
                this.requires += modId;
            }
            if (i < requiresOrig.length - 1) this.requires += ", ";
            i++;
        }
        if (this.requires.trim().length() == 0) this.requires = Locale.localize("ziahsclient.gui.nothing");

        this.recommends = "";
        String[] recommendsOrig = this.selectedMod.getRecommendedModules().split(",");
        i = 0;
        for (String modId : recommendsOrig) {
            if (modId.trim().length() <= 0 || modId == null) continue;
            RepoMod mod = this.findRepoMod(modId);
            if (mod != null) {
                this.recommends += mod.getName();
            }else{
                this.recommends += modId;
            }
            if (i < recommendsOrig.length - 1) this.recommends += ", ";
            i++;
        }
        if (this.recommends.trim().length() == 0) this.recommends = Locale.localize("ziahsclient.gui.nothing");

        this.incompatible = "";
        String[] incompatibleOrig = this.selectedMod.getIncompatibleModules().split(",");
        i = 0;
        for (String modId : incompatibleOrig) {
            if (modId.trim().length() <= 0 || modId == null) continue;
            RepoMod mod = this.findRepoMod(modId);
            if (mod != null) {
                this.incompatible += mod.getName();
            }else{
                this.incompatible += modId;
            }
            if (i < incompatibleOrig.length - 1) this.incompatible += ", ";
            i++;
        }
        if (this.incompatible.trim().length() == 0) this.incompatible = Locale.localize("ziahsclient.gui.nothing");
    }
}