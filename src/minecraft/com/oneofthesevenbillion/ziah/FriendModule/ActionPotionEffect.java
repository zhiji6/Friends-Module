package com.oneofthesevenbillion.ziah.FriendModule;

import net.minecraft.src.Minecraft;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;

public class ActionPotionEffect extends Action {
	private int potionId;
	private int length;
	private int modifier;
	private boolean isAmbient;

	public ActionPotionEffect(String str) {
		String[] params = str.split(" ");
		this.potionId = Integer.parseInt(params[1]);
		this.length = Integer.parseInt(params[2]);
		this.modifier = Integer.parseInt(params[3]);
		this.isAmbient = Boolean.parseBoolean(params[4]);
	}

	public ActionPotionEffect(int potionId, int length, int modifier, boolean isAmbient) {
		this.potionId = potionId;
		this.length = length;
		this.modifier = modifier;
		this.isAmbient = isAmbient;
	}

	@Override
	public void run() {
		if (this.potionId > Potion.potionTypes.length || Potion.potionTypes[this.potionId] == null) return;
		PotionEffect effect = new PotionEffect(this.potionId, this.length == -1 ? Integer.MAX_VALUE : this.length, this.modifier, this.isAmbient);
		if (this.length == -1) effect.setPotionDurationMax(true);
		Minecraft.getMinecraft().thePlayer.addPotionEffect(effect);
	}

	@Override
	public String toString() {
		return "potioneffect " + this.potionId + " " + this.length + " " + this.modifier + " " + this.isAmbient;
	}
}