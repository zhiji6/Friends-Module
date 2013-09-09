package com.oneofthesevenbillion.ziah.FriendModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.src.Minecraft;

public class NotificationData {
	private int slideCounter = 0;
	private int bounceCounter = 0;
	private boolean isOtherTick = true;
	private int closeTimer = 6000;
	private boolean done = false;
	private String title;
	private String descriptionL1;
	private String descriptionL2;
	private String descriptionL3;
	private String descriptionL4;
	private String descriptionL5;
	private String descriptionL6;
	private String descriptionL7;
	private String descriptionL8;
	private String descriptionL9;
	private String descriptionL10;

	public NotificationData(String title, String descriptionL1, String descriptionL2, String descriptionL3, String descriptionL4, String descriptionL5, String descriptionL6, String descriptionL7, String descriptionL8, String descriptionL9, String descriptionL10) {
		this.title = title;
		this.descriptionL1 = descriptionL1;
		this.descriptionL2 = descriptionL2;
		this.descriptionL3 = descriptionL3;
		this.descriptionL4 = descriptionL4;
		this.descriptionL5 = descriptionL5;
		this.descriptionL6 = descriptionL6;
		this.descriptionL7 = descriptionL7;
		this.descriptionL8 = descriptionL8;
		this.descriptionL9 = descriptionL9;
		this.descriptionL10 = descriptionL10;
	}

	public NotificationData(String title, String description) {
		this.title = title;
		Map<Integer, String> descriptionLines = new HashMap<Integer, String>();
		descriptionLines.put(0, "");
		descriptionLines.put(1, "");
		descriptionLines.put(2, "");
		descriptionLines.put(3, "");
		descriptionLines.put(4, "");
		descriptionLines.put(5, "");
		descriptionLines.put(6, "");
		descriptionLines.put(7, "");
		descriptionLines.put(8, "");
		descriptionLines.put(9, "");
		int l = 1;
		for (int i = 0; i < description.length(); i++) {
			descriptionLines.put(l - 1, descriptionLines.get(l - 1) + description.charAt(i));
			if ((i + 1) < description.length() && Minecraft.getMinecraft().fontRenderer.getStringWidth(descriptionLines.get(l - 1) + description.charAt(i + 1)) > (120 * 2) - 4 - 4) {
				if (l < 10) {
					l++;
				}else{
					break;
				}
			}
		}
		this.descriptionL1 = descriptionLines.get(0);
		this.descriptionL2 = descriptionLines.get(1);
		this.descriptionL3 = descriptionLines.get(2);
		this.descriptionL4 = descriptionLines.get(3);
		this.descriptionL5 = descriptionLines.get(4);
		this.descriptionL6 = descriptionLines.get(5);
		this.descriptionL7 = descriptionLines.get(6);
		this.descriptionL8 = descriptionLines.get(7);
		this.descriptionL9 = descriptionLines.get(8);
		this.descriptionL10 = descriptionLines.get(9);
	}

	public void tick() {
		if (this.done) return;
		if (this.closeTimer > 0) {
	    	if (this.slideCounter < 46) {
	    		this.slideCounter += 3;
	    	}else{
	    		if (this.bounceCounter < 5 && this.isOtherTick) {
		    		switch (this.bounceCounter) {
		    			case 0:
		    				this.slideCounter += 3;
		    				break;
		    			case 1:
		    				this.slideCounter += 2;
		    				break;
		    			case 2:
		    				this.slideCounter += 1;
		    				break;
		    			case 3:
		    				this.slideCounter -= 1;
		    				break;
		    			case 4:
		    				this.slideCounter -= 2;
		    				break;
		    			case 5:
		    				this.slideCounter -= 3;
		    				break;
		    		}
	    			this.bounceCounter++;
	    		}
	    	}
	    	this.closeTimer--;
    	}else{
			if (this.slideCounter > 0) {
				this.slideCounter -= 3;
			}else{
				this.done = true;
			}
		}
		this.isOtherTick = !this.isOtherTick;
	}

	public int getSlideCounter() {
		return this.slideCounter;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescriptionL1() {
		return this.descriptionL1;
	}

	public String getDescriptionL2() {
		return this.descriptionL2;
	}

	public String getDescriptionL3() {
		return this.descriptionL3;
	}

	public String getDescriptionL4() {
		return this.descriptionL4;
	}

	public String getDescriptionL5() {
		return this.descriptionL5;
	}

	public String getDescriptionL6() {
		return this.descriptionL6;
	}

	public String getDescriptionL7() {
		return this.descriptionL7;
	}

	public String getDescriptionL8() {
		return this.descriptionL8;
	}

	public String getDescriptionL9() {
		return this.descriptionL9;
	}

	public String getDescriptionL10() {
		return this.descriptionL10;
	}

	public boolean isDone() {
		return this.done;
	}
}