package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.HashMap;
import java.util.Map;

public class ActionRegistry {
	public static ActionRegistry instance;
    private Map<String, Class<? extends Action>> actions;

    public ActionRegistry() {
        this.actions = new HashMap<String, Class<? extends Action>>();
        ActionRegistry.instance = this;
        this.registerAction("chat", ActionChat.class);
        this.registerAction("potioneffect", ActionPotionEffect.class);
    }

    public void registerAction(String name, Class<? extends Action> action) {
        this.actions.put(name, action);
    }

    public Class<? extends Action> getActionByName(String action) {
        return this.actions.get(action);
    }
}