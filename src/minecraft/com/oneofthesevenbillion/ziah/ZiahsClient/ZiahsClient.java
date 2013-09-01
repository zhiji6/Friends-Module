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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.minecraft.src.BaseMod;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.ResourceLocation;

import org.lwjgl.opengl.Display;

import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventAddRenderers;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventBus;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventChat;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventClientConnect;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventClientDisconnect;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventCustomPayload;
import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventLoad;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiModList;
import com.oneofthesevenbillion.ziah.ZiahsClient.network.PacketManager;
import com.oneofthesevenbillion.ziah.ZiahsClient.network.PacketRegistry;

public class ZiahsClient {
    private static ZiahsClient instance;
    private final String modAltName = "ZiahsClient";
    private String msgPrefix;
    private File dataDir;
    private Logger logger;
    private File logFile;
    private FileHandler logFileHandler;
    private Formatter logFormatter;
    private Config config;
    private final BaseMod modClass;
    private EventBus eventBus;
    private GuiButton menuButton;
    private Map<KeyBinding, Object> keybindings = new HashMap<KeyBinding, Object>();
    private Map<KeyBinding, Boolean> lastKeybindings = new HashMap<KeyBinding, Boolean>();
    private String runningMCVersion;
    private List<GuiButton> menuButtons = new ArrayList<GuiButton>();
    private Map<Integer, Method> menuButtonIdToMethod = new HashMap<Integer, Method>();
    private Map<Integer, Object> menuButtonIdToClickMethodObject = new HashMap<Integer, Object>();
    private int nextMenuId = 1;

    public ZiahsClient(BaseMod modClass) {
        Display.setTitle(Display.getTitle() + " with Ziah_'s Client");

        instance = this;
        this.modClass = modClass;
        this.eventBus = new EventBus();

        this.runningMCVersion = ModLoader.VERSION.split(" ")[1];
        if (!this.runningMCVersion.equalsIgnoreCase(this.getMinecraftVersion())) {
            System.err.println("INCORRECT MINECRAFT VERSION " + this.runningMCVersion + " EXPECTED " + this.getMinecraftVersion() + "!!!");
            return;
        }

        new PacketRegistry();
    }

    public void load() {
        if ((new File("options.txt")).exists()) {
            this.dataDir = new File("." + File.separator + "ZiahsClient" + File.separator);
        }else{
            this.dataDir = new File(Minecraft.getMinecraft().mcDataDir, "ZiahsClient");
        }
        if (!this.dataDir.exists()) {
            if (!this.dataDir.mkdirs() && (!this.dataDir.exists() || !this.dataDir.isDirectory())) {
                System.err.println("[ZiahsClient] ERROR: Unable to create data directory, unloading!");
                return;
            }
        }

        this.logger = Logger.getLogger(this.modAltName);
        try {
            this.logFile = new File(this.dataDir, "ziahsclient.log");
            this.logFileHandler = new FileHandler(this.logFile.getAbsolutePath(), true);
            this.logger.addHandler(this.logFileHandler);
            this.logger.setLevel(Level.ALL);
            this.logFormatter = new SimpleFormatter();
            this.logFileHandler.setFormatter(this.logFormatter);
        } catch(IOException e) {
            this.logger.log(Level.SEVERE, "IOException when setting up the logger.", e);
        }

        try {
            this.config = new Config(this.getName() + " Configuration", new File(this.dataDir, "ziahsclient.cfg"), new URL("http://magi-craft.net/defaultconfigs/ziahsclient.cfg"));
        } catch(IOException e) {
            this.logger.log(Level.SEVERE, "IOException when setting up the config.", e);
        }

        try {
            List<ResourceLocation> langFiles = new ArrayList<ResourceLocation>();
            BufferedReader in = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().func_110442_L().func_110536_a(new ResourceLocation("ziahsclient/lang/languages.list")).func_110527_b()));

            while (true) {
                try {
                    String line = in.readLine();
                    if (line == null) break;
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        langFiles.add(new ResourceLocation("ziahsclient/lang/" + line + ".lang"));
                    }
                } catch(IOException e) {
                    this.logger.log(Level.SEVERE, "Failed to load language!", e);
                }
            }

            Locale.loadLanguages(langFiles);
        } catch(IOException e) {
            this.logger.log(Level.SEVERE, "Failed to load language list!", e);
        }

        Minecraft.getMinecraft().sndManager.addSound("ziahsclient/scarymazegame.ogg");

        this.eventBus.registerEventHandler(null, new EventHandler());
        new Thread(new ThreadTick()).start();

        if (!this.runningMCVersion.equalsIgnoreCase(this.getMinecraftVersion())) return;

        this.menuButton = new GuiButton(9001, 0, 0, Locale.localize("ziahsclient.gui.menu"));

        try {
            this.registerMenuButton(new GuiSmallButton(0, 0, 0, Locale.localize("ziahsclient.gui.modules")), this.getClass().getDeclaredMethod("onModuleButtonClicked"), this);
        } catch(Exception e) {}

        this.menuButtons.add(0, new GuiSmallButton(0, 0, 0, Locale.localize("ziahsclient.gui.back")));

        new ModuleManager();

        this.logger.log(Level.INFO, "Found " + (ModuleManager.getInstance().getUnloadedModules().size() + ModuleManager.getInstance().getLoadedModules().size()) + " modules, " + ModuleManager.getInstance().getUnloadedModules().size() + " modules not loaded, " + ModuleManager.getInstance().getLoadedModules().size() + " modules loaded.");

        this.msgPrefix = "§7[§6" + this.getName() + "§7]§r ";

        this.logger.log(Level.INFO, this.getName() + " starting...");
        this.logger.log(Level.INFO, "Storing data in " + this.dataDir.getAbsolutePath());
        ModLoader.registerPacketChannel(this.modClass, this.modAltName.toLowerCase());

        this.eventBus.callEvent(new EventLoad());
    }

    public void registerKey(Object module, KeyBinding key) {
        key.keyDescription = Locale.localize(key.keyDescription);
        this.keybindings.put(key, module);
        ModLoader.registerKey(this.modClass, key, false);
    }

    public void addRenderer(Map map) {
        this.eventBus.callEvent(new EventAddRenderers(map));
    }

    public void clientChat(String message) {
        this.eventBus.callEvent(new EventChat(message));
    }

    public void clientCustomPayload(NetClientHandler clientHandler, Packet250CustomPayload payload) {
        this.eventBus.callEvent(new EventCustomPayload(clientHandler, payload));

        if (payload.channel.equals(this.modAltName.toLowerCase())) PacketManager.onPacketData(clientHandler, payload);
    }

    public void clientConnect(NetClientHandler clientHandler) {
        this.eventBus.callEvent(new EventClientConnect(clientHandler.getNetManager().getSocketAddress().toString(), clientHandler));

        if (!this.config.getData().containsKey("hasPlayed") || this.config.getData().containsKey("hasPlayed") && this.config.getData().getProperty("hasPlayed") == "false") {
            this.config.getData().setProperty("hasPlayed", "true");
            try {
                this.config.save();
            } catch(IOException e) {
                this.logger.log(Level.WARNING, "Exception when saving config.", e);
            }
        }
        this.logger.log(Level.INFO, "Client joined " + clientHandler.getNetManager().getSocketAddress().toString());
    }

    public void clientDisconnect(NetClientHandler clientHandler) {
        this.eventBus.callEvent(new EventClientDisconnect(clientHandler.getNetManager().getSocketAddress().toString(), clientHandler));
    }

    public File getLogFile() {
        return this.logFile;
    }

    public File getDataDir() {
        return this.dataDir;
    }

    public Config getConfig() {
        return this.config;
    }

    public GuiButton getMenuButton() {
        return this.menuButton;
    }

    public static ZiahsClient getInstance() {
        return instance;
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public BaseMod getModClass() {
        return this.modClass;
    }

    public String getName() {
        return Locale.localize("ziahsclient.ziahsclient");
    }

    public String getMinecraftVersion() {
        return "1.6.2";
    }

    public String getRunningMinecraftVersion() {
        return this.runningMCVersion;
    }

    public Map<KeyBinding, Object> getKeybindings() {
        return this.keybindings;
    }

    public Map<KeyBinding, Boolean> getKeybindingAndLastValues() {
        return this.lastKeybindings;
    }

    public void unregisterKeys(Class modClass) {
        for (KeyBinding key : new ArrayList<KeyBinding>(this.keybindings.keySet())) {
            if (this.keybindings.get(key).getClass().equals(modClass)) {
                this.keybindings.remove(key);
                this.lastKeybindings.remove(key);
            }
        }
    }

    public void registerMenuButton(GuiButton button, Method method, Object obj) {
        button.id = this.nextMenuId;
        this.nextMenuId++;
        this.menuButtons.add(button);
        this.menuButtonIdToMethod.put(button.id, method);
        this.menuButtonIdToClickMethodObject.put(button.id, obj);
    }

    public List<GuiButton> getMenuButtons() {
        return this.menuButtons;
    }

    public void onModuleButtonClicked() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiModList(Minecraft.getMinecraft().currentScreen));
    }

    public Map<Integer, Method> getMenuButtonIdToMethod() {
        return this.menuButtonIdToMethod;
    }

    public Map<Integer, Object> getMenuButtonIdToClickMethodObject() {
        return this.menuButtonIdToClickMethodObject;
    }
}