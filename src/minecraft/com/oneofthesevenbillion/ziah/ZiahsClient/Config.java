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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class Config {
    private final String comment;
    private final File file;
    private final Properties data;

    public Config(String comment, File file) {
        this.data = new Properties();
        this.comment = comment;
        this.file = file;
    }

    public Config(String comment, File file, URL fallbackUrl) throws IOException {
        this.data = new Properties();
        this.comment = comment;
        this.file = file;
        if (!this.file.exists()) this.load(fallbackUrl);
        else
            this.load();
    }

    public Config(String comment, File file, InputStream fallbackInputStream) throws IOException {
        this.data = new Properties();
        this.comment = comment;
        this.file = file;
        if (!this.file.exists()) this.load(fallbackInputStream);
        else
            this.load();
    }

    public String getComment() {
        return this.comment;
    }

    public File getFile() {
        return this.file;
    }

    public Properties getData() {
        return this.data;
    }

    public void load(URL url) throws IOException {
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        this.load(in);
    }

    public void load(InputStream in) throws IOException {
        this.data.load(in);
        this.save();
        in.close();
    }

    public void load() throws IOException {
        FileInputStream configStream = new FileInputStream(this.file);
        this.data.load(configStream);
        configStream.close();
    }

    public void save() throws IOException {
        FileOutputStream configStream = new FileOutputStream(this.file);
        this.data.store(configStream, this.comment);
        configStream.close();
    }
}
