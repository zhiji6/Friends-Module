package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.Minecraft;

import org.lwjgl.opengl.GL11;

public class NotificationManager {
	private List<NotificationData> notifications = new ArrayList<NotificationData>();

	public void render(int screenWidth, int screenHeight) {
		if (this.notifications.size() > 0) {
			NotificationData notification = this.notifications.get(0);
			if (notification.isDone()) {
				this.notifications.remove(notification);
				return;
			}
			GL11.glPushMatrix();
		    GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		    Util.renderNotificationAt(Minecraft.getMinecraft().fontRenderer, notification.getTitle(), notification.getDescriptionL1(), notification.getDescriptionL2(), notification.getDescriptionL3(), notification.getDescriptionL4(), notification.getDescriptionL5(), notification.getDescriptionL6(), notification.getDescriptionL7(), notification.getDescriptionL8(), notification.getDescriptionL9(), notification.getDescriptionL10(), screenWidth - 120, ((screenHeight - 50) + 46) - notification.getSlideCounter(), screenWidth, screenHeight);
		    GL11.glPopMatrix();
			notification.tick();
		}
	}

	public List<NotificationData> getNotifications() {
		return this.notifications;
	}
}