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

package com.oneofthesevenbillion.ziah.ZiahsClient;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ModLoader;

import org.lwjgl.input.Mouse;

import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventKeyboard;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventListener;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventTick;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiMessage;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiScaryMazeGame;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiZiahsClient;

public class EventHandler {
    private boolean wasButtonDown = false;
    private boolean lastButtonDown = false;
    private boolean versionDismissed = false;
    private boolean firstRun = true;
    private GuiMainMenu mainMenu = null;

    @EventListener
    public void onTick(EventTick event) {
        if (!ZiahsClient.getInstance().getRunningMinecraftVersion().equalsIgnoreCase(ZiahsClient.getInstance().getMinecraftVersion())) {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu && !this.versionDismissed) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiMessage(Minecraft.getMinecraft().currentScreen, Locale.localize("ziahsclient.not_compatible").replace("%MCVERSION%", ZiahsClient.getInstance().getRunningMinecraftVersion()) + "\n" + Locale.localize("ziahsclient.install_ziahsclient").replace("%MCVERSION%", ZiahsClient.getInstance().getRunningMinecraftVersion()) + "\n" + Locale.localize("ziahsclient.or_install_mc_version").replace("%CORRECTMCVERSION%", ZiahsClient.getInstance().getMinecraftVersion())));
                this.versionDismissed = true;
            }
            return;
        }

        Map<KeyBinding, Boolean> lastKeybindings = ZiahsClient.getInstance().getKeybindingAndLastValues();
        for (KeyBinding key : ZiahsClient.getInstance().getKeybindings().keySet()) {
            if (!lastKeybindings.containsKey(key)) lastKeybindings.put(key, false);
            if (!lastKeybindings.get(key) && key.isPressed()) {
                ZiahsClient.getInstance().getEventBus().callEvent(new EventKeyboard(key));
            }
            lastKeybindings.put(key, key.isPressed());
        }

        if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu) {
            try {
                GuiMainMenu screen = (GuiMainMenu) Minecraft.getMinecraft().currentScreen;

                String splashText = null;

                if (this.firstRun || this.mainMenu != screen) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    if (cal.get(Calendar.MONTH) == Calendar.NOVEMBER && cal.get(Calendar.DAY_OF_MONTH) == 21) {
                        splashText = "Happy birthday, Ziah!";
                    }else
                    if (cal.get(Calendar.MONTH) == Calendar.JULY && cal.get(Calendar.DAY_OF_MONTH) == 30) {
                        splashText = "Happy birthday, Ciaran!";
                    }else
                    if (cal.get(Calendar.MONTH) == Calendar.AUGUST && cal.get(Calendar.DAY_OF_MONTH) == 4) {
                        splashText = "Happy birthday, Kavi!";
                    }
    
                    Random random = new Random();
                    int randomInt = random.nextInt(25);
                    if (randomInt == 0) {
                        splashText = "Ziah is awesome!";
                    }else
                    if (randomInt == 1) {
                        splashText = "Kavi is awesome!";
                    }else
                    if (randomInt == 2) {
                        splashText = "Ciaran is awesome!";
                    }else
                    if (randomInt == 3) {
                        splashText = "Anthony is awesome!";
                    }else
                    if (randomInt == 4) {
                        splashText = "Christopher is awesome!";
                    }

                    if (this.firstRun && (random.nextInt(75) == 0 || cal.get(Calendar.MONTH) == Calendar.OCTOBER && cal.get(Calendar.DAY_OF_MONTH) == 31)) {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiScaryMazeGame(screen));
                    }

                    this.firstRun = false;
                    this.mainMenu = screen;
                }

                if (splashText != null) {
                    try {
                        ModLoader.setPrivateValue(GuiMainMenu.class, screen, 2, splashText);
                    } catch (Exception e) {}
                }

                List buttonList = (List) ModLoader.getPrivateValue(GuiScreen.class, screen, 3);
                GuiButton button = ZiahsClient.getInstance().getMenuButton();
                GuiButton realmsButton = ((GuiButton) ModLoader.getPrivateValue(GuiMainMenu.class, screen, 23));
                Object fmlModsButtonPlace = null;
                try {
                    fmlModsButtonPlace = ModLoader.getPrivateValue(GuiMainMenu.class, screen, 24);
                } catch (Exception e) {}
                GuiButton fmlModsButton = (fmlModsButtonPlace != null && fmlModsButtonPlace instanceof GuiButton ? ((GuiButton) fmlModsButtonPlace) : null);
                button.xPosition = screen.width / 2 - 100;
                button.yPosition = screen.height / 4 + 48 + 24 * 2;
                if (fmlModsButton != null && fmlModsButton.drawButton && realmsButton.drawButton) {
                    ModLoader.setPrivateValue(GuiButton.class, realmsButton, 1, 200);

                    button.yPosition = screen.height / 4 + 48 + 24 * 2 + 20 + 4;
                    button.displayString = Locale.localize("ziahsclient.ziahsclient_short") + " " + Locale.localize("ziahsclient.gui.modules");

                    ModLoader.setPrivateValue(GuiButton.class, fmlModsButton, 1, 98);
                    fmlModsButton.xPosition = screen.width / 2 + 2;
                    fmlModsButton.yPosition = screen.height / 4 + 48 + 24 * 2 + 20 + 4;
                    fmlModsButton.displayString = "FML Mods";

                    ((GuiButton) buttonList.get(4)).yPosition = screen.height / 4 + 48 + 24 * 2 + 20 + 4 + 20 + 4;
                    ((GuiButton) buttonList.get(5)).yPosition = screen.height / 4 + 48 + 24 * 2 + 20 + 4 + 20 + 4;
                    ((GuiButton) buttonList.get(6)).yPosition = screen.height / 4 + 48 + 24 * 2 + 20 + 4 + 20 + 4;
                }else
                if (fmlModsButton != null && fmlModsButton.drawButton) {
                    ModLoader.setPrivateValue(GuiButton.class, fmlModsButton, 1, 98);
                    fmlModsButton.xPosition = screen.width / 2 + 2;
                    fmlModsButton.displayString = "FML Mods";

                    button.displayString = Locale.localize("ziahsclient.ziahsclient_short") + " " + Locale.localize("ziahsclient.gui.modules");
                }else
                if (realmsButton != null && realmsButton.drawButton) {
                    button.xPosition = screen.width / 2 - 100 + 102;

                    ModLoader.setPrivateValue(GuiButton.class, realmsButton, 1, 98);
                    realmsButton.xPosition = screen.width / 2 - 100;
                }
                if (!buttonList.contains(button)) {
                    buttonList.add(button);
                }
                if (Mouse.isCreated() && Mouse.isButtonDown(0) && !this.lastButtonDown && (Boolean) ModLoader.getPrivateValue(GuiButton.class, button, 9)) {
                    if (!this.wasButtonDown) {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiZiahsClient(screen));
                        this.wasButtonDown = true;
                    }
                }else{
                    this.wasButtonDown = false;
                }
                this.lastButtonDown = Mouse.isCreated() && Mouse.isButtonDown(0);
            }catch (IllegalArgumentException e) {
                // Impossible
            }catch (SecurityException e) {
                // Ignore, probably impossible
            }catch (NoSuchFieldException e) {
                // Ignore
            }
        }
    }
}