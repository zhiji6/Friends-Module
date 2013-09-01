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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import net.minecraft.src.Minecraft;

import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiDownloading;
import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiMessage;

public class ThreadDownloading extends Thread {
    public File file;
    public URL url;
    public GuiDownloading dlgui;

    public ThreadDownloading(File file, URL url, GuiDownloading dlgui) {
        this.file = file;
        this.url = url;
        this.dlgui = dlgui;
    }

    @Override
    public void run() {
        try {
            InputStream in = this.url.openStream();
            OutputStream out = new FileOutputStream(this.file);
            float length = in.available();
            float i = 0.0F;
            while (true) {
                int curbyte = in.read();
                if (curbyte == -1) break;
                out.write(curbyte);
                this.dlgui.percentageUpdate((i != 0 && length != 0 ? (i <= length ? (i / length) * 100.0F : 100.0F) : 0.0F));
                i++;
            }
            out.close();
            in.close();
            this.onComplete();
            this.dlgui.finish();
        } catch (MalformedURLException e) {
            // Impossible
        } catch (UnsupportedEncodingException e) {
            // Probably Impossible
        } catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when downloading module!", e);
            this.file.delete();
            Minecraft.getMinecraft().displayGuiScreen(new GuiMessage(this.dlgui.getParent(), "An error has occurred while downloading the Module!\n" + e.getClass().getName() + "\n" + e.getMessage()));
        }
    }

    public void onComplete() {}
}