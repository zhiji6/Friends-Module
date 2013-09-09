package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.oneofthesevenbillion.ziah.ZiahsClient.util.ArrayUtils;
import com.oneofthesevenbillion.ziah.ZiahsClient.util.Utils;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;

public class Util {
	private static ResourceLocation dirt = new ResourceLocation("textures/blocks/dirt.png");

	public static void renderNotificationAt(FontRenderer fontRenderer, String title, String descriptionL1, String descriptionL2, String descriptionL3, String descriptionL4, String descriptionL5, String descriptionL6, String descriptionL7, String descriptionL8, String descriptionL9, String descriptionL10, int x1, int y1, int x2, int y2) {
		title = fontRenderer.trimStringToWidth(title, ((x2 - x1) * 2) - 4 - 4);
		descriptionL1 = fontRenderer.trimStringToWidth(descriptionL1, ((x2 - x1) * 2) - 4 - 4);
		descriptionL2 = fontRenderer.trimStringToWidth(descriptionL2, ((x2 - x1) * 2) - 4 - 4);
		descriptionL3 = fontRenderer.trimStringToWidth(descriptionL3, ((x2 - x1) * 2) - 4 - 4);
		descriptionL4 = fontRenderer.trimStringToWidth(descriptionL4, ((x2 - x1) * 2) - 4 - 4);
		descriptionL5 = fontRenderer.trimStringToWidth(descriptionL5, ((x2 - x1) * 2) - 4 - 4);
		descriptionL6 = fontRenderer.trimStringToWidth(descriptionL6, ((x2 - x1) * 2) - 4 - 4);
		descriptionL7 = fontRenderer.trimStringToWidth(descriptionL7, ((x2 - x1) * 2) - 4 - 4);
		descriptionL8 = fontRenderer.trimStringToWidth(descriptionL8, ((x2 - x1) * 2) - 4 - 4);
		descriptionL9 = fontRenderer.trimStringToWidth(descriptionL9, ((x2 - x1) * 2) - 4 - 4);
		descriptionL10 = fontRenderer.trimStringToWidth(descriptionL10, ((x2 - x1) * 2) - 4 - 4);

		Minecraft.getMinecraft().func_110434_K().func_110577_a(Util.dirt);
		for (int y = y1; y < y2; y += 16) {
			for (int x = x1; x < x2; x += 16) {
				Utils.drawTexturedModalRect(x, y, 16, 16);
			}
		}

		Utils.drawHorizontalLine(x2, x1 - 1, y1 - 1, 0xFF888888);
		Utils.drawVerticalLine(x1 - 1, y1 - 1, y2, 0xFF888888);

	    GL11.glScalef(0.5F, 0.5F, 0.5F);
	    GL11.glTranslatef(x1, y1, 0.0F);
		fontRenderer.drawString(title, x1 + 4, y1 + 4, 0xFFFFFF);
		fontRenderer.drawString(descriptionL1, x1 + 4, y1 + 4 + 3 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL2, x1 + 4, y1 + 4 + 3 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL3, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL4, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL5, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL6, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9 + 9 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL7, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9 + 9 + 9 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL8, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9 + 9 + 9 + 9 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL9, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9 + 9 + 9 + 9 + 9 + 9 + 9, 0xDDDDDD);
		fontRenderer.drawString(descriptionL10, x1 + 4, y1 + 4 + 3 + 9 + 9 + 9 + 9 + 9 + 9 + 9 + 9 + 9 + 9, 0xDDDDDD);
	    GL11.glTranslatef(-x1, -y1, 0.0F);
	    GL11.glScalef(1.0F, 1.0F, 1.0F);
	}
}