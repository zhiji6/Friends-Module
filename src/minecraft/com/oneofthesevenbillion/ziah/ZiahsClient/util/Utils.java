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

package com.oneofthesevenbillion.ziah.ZiahsClient.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.Icon;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

public class Utils {
    private static List<Character> letters = Arrays.asList(new Character[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'});
    private static List<Character> numbers = Arrays.asList(new Character[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'});

    public static void setPrivateValue(@SuppressWarnings("rawtypes") Class classInstance, Object classObject, String variable, String obfuscatedVariable, Object value) throws Exception {
        Exception exception = null;
        try {
            ModLoader.setPrivateValue(classInstance, classObject, variable, value);
        }catch (Exception e) {
            if (e instanceof NoSuchFieldException) {
                try {
                    ModLoader.setPrivateValue(classInstance, classObject, obfuscatedVariable, value);
                }catch (Exception e1) {
                    exception = e1;
                }
            }else {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    @SuppressWarnings("unused")
    public static Object getPrivateValue(@SuppressWarnings("rawtypes") Class classInstance, Object classObject, String variable, String obfuscatedVariable) throws Exception {
        Exception exception = null;
        try {
            return ModLoader.getPrivateValue(classInstance, classObject, variable);
        }catch (Exception e) {
            if (e instanceof NoSuchFieldException) {
                try {
                    return ModLoader.getPrivateValue(classInstance, classObject, obfuscatedVariable);
                }catch (Exception e1) {
                    exception = e1;
                }
            }else {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
        throw new NoSuchFieldException("Unable to fetch private value");
    }

    public static String clean(String input, StringFormat format) {
        String cleaned = "";
        for (int i = 0; i < input.toCharArray().length; i++) {
            Character c = input.charAt(i);
            switch (format) {
                case JUST_LETTERS:
                    if (letters.contains(Character.toLowerCase(c))) {
                        cleaned += c;
                    }
                    break;
                case JUST_NUMBERS:
                    if (numbers.contains(Character.toLowerCase(c))) {
                        cleaned += c;
                    }
                    break;
                case JUST_LETTERS_AND_NUMBERS:
                    if (letters.contains(Character.toLowerCase(c)) || numbers.contains(Character.toLowerCase(c))) {
                        cleaned += c;
                    }
                    break;
                case NOT_LETTERS:
                    if (!letters.contains(Character.toLowerCase(c))) {
                        cleaned += c;
                    }
                    break;
                case NOT_NUMBERS:
                    if (!numbers.contains(Character.toLowerCase(c))) {
                        cleaned += c;
                    }
                    break;
                case NOT_LETTERS_OR_NUMBERS:
                    if (!letters.contains(Character.toLowerCase(c)) && !numbers.contains(Character.toLowerCase(c))) {
                        cleaned += c;
                    }
                    break;
            }
        }
        return cleaned;
    }

    public static void drawHorizontalLine(int x2, int x1, int y, int color) {
        if (x1 < x2) {
            int var5 = x2;
            x2 = x1;
            x1 = var5;
        }

        drawRect(x2, y, x1 + 1, y + 1, color);
    }

    public static void drawVerticalLine(int x, int y1, int y2, int color) {
        if (y2 < y1) {
            int var5 = y1;
            y1 = y2;
            y2 = var5;
        }

        drawRect(x, y1 + 1, x + 1, y2, color);
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color. Args: x1, y1, x2, y2, color
     */
    public static void drawRect(int par0, int par1, int par2, int par3, int par4) {
        int var5;

        if (par0 < par2) {
            var5 = par0;
            par0 = par2;
            par2 = var5;
        }

        if (par1 < par3) {
            var5 = par1;
            par1 = par3;
            par3 = var5;
        }

        float var10 = (par4 >> 24 & 255) / 255.0F;
        float var6 = (par4 >> 16 & 255) / 255.0F;
        float var7 = (par4 >> 8 & 255) / 255.0F;
        float var8 = (par4 & 255) / 255.0F;
        Tessellator var9 = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(var6, var7, var8, var10);
        var9.startDrawingQuads();
        var9.addVertex(par0, par3, 0.0D);
        var9.addVertex(par2, par3, 0.0D);
        var9.addVertex(par2, par1, 0.0D);
        var9.addVertex(par0, par1, 0.0D);
        var9.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     */
    public static void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6, float zLevel) {
        float var7 = (par5 >> 24 & 255) / 255.0F;
        float var8 = (par5 >> 16 & 255) / 255.0F;
        float var9 = (par5 >> 8 & 255) / 255.0F;
        float var10 = (par5 & 255) / 255.0F;
        float var11 = (par6 >> 24 & 255) / 255.0F;
        float var12 = (par6 >> 16 & 255) / 255.0F;
        float var13 = (par6 >> 8 & 255) / 255.0F;
        float var14 = (par6 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator var15 = Tessellator.instance;
        var15.startDrawingQuads();
        var15.setColorRGBA_F(var8, var9, var10, var7);
        var15.addVertex(par3, par2, zLevel);
        var15.addVertex(par1, par2, zLevel);
        var15.setColorRGBA_F(var12, var13, var14, var11);
        var15.addVertex(par1, par4, zLevel);
        var15.addVertex(par3, par4, zLevel);
        var15.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Renders the specified text to the screen, center-aligned.
     */
    public static void drawCenteredString(FontRenderer par1FontRenderer, String par2Str, int par3, int par4, int par5) {
        par1FontRenderer.drawStringWithShadow(par2Str, par3 - par1FontRenderer.getStringWidth(par2Str) / 2, par4, par5);
    }

    /**
     * Renders the specified text to the screen.
     */
    public static void drawString(FontRenderer par1FontRenderer, String par2Str, int par3, int par4, int par5) {
        par1FontRenderer.drawStringWithShadow(par2Str, par3, par4, par5);
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height, zLevel
     */
    public static void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6, float zLevel) {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;
        Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(par1 + 0, par2 + par6, zLevel, (par3 + 0) * var7, (par4 + par6) * var8);
        var9.addVertexWithUV(par1 + par5, par2 + par6, zLevel, (par3 + par5) * var7, (par4 + par6) * var8);
        var9.addVertexWithUV(par1 + par5, par2 + 0, zLevel, (par3 + par5) * var7, (par4 + 0) * var8);
        var9.addVertexWithUV(par1 + 0, par2 + 0, zLevel, (par3 + 0) * var7, (par4 + 0) * var8);
        var9.draw();
    }

    public static void drawTexturedModalRect(int x, int y, int width, int height) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0.0, 1.0);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1.0, 1.0);
        tessellator.addVertexWithUV(x + width, y, 0, 1.0, 0.0);
        tessellator.addVertexWithUV(x, y, 0, 0.0, 0.0);
        tessellator.draw();
    }

    public static void drawTexturedModelRectFromIcon(int par1, int par2, Icon par3Icon, int par4, int par5, float zLevel) {
        Tessellator var6 = Tessellator.instance;
        var6.startDrawingQuads();
        var6.addVertexWithUV(par1 + 0, par2 + par5, zLevel, par3Icon.getMinU(), par3Icon.getMaxV());
        var6.addVertexWithUV(par1 + par4, par2 + par5, zLevel, par3Icon.getMaxU(), par3Icon.getMaxV());
        var6.addVertexWithUV(par1 + par4, par2 + 0, zLevel, par3Icon.getMaxU(), par3Icon.getMinV());
        var6.addVertexWithUV(par1 + 0, par2 + 0, zLevel, par3Icon.getMinU(), par3Icon.getMinV());
        var6.draw();
    }
}