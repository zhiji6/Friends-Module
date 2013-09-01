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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateMod {
    public static String updateCheck(String modName, String curVersion) throws IOException {
        URL updateCheckUrl = new URL((new StringBuilder("http://oneofthesevenbillion.com/ziah/updatemod.php?action=updatecheck&mod=")).append(modName).append("&curversion=").append(curVersion).toString());
        URLConnection updateCheckConnection = updateCheckUrl.openConnection();
        BufferedReader updateCheckIn = new BufferedReader(new InputStreamReader(updateCheckConnection.getInputStream()));
        String updateCheckResponse = updateCheckIn.readLine();
        updateCheckIn.close();
        if (updateCheckResponse.startsWith("outdated")) return updateCheckResponse.substring(9, updateCheckResponse.length());
        else
            return "false";
    }

    public static boolean doUpdate(String modName, String newVersion, String fileName) throws IOException {
        URL updateUrl = new URL((new StringBuilder("http://oneofthesevenbillion.com/ziah/updatemod.php?action=doupdate&mod=")).append(modName).append("&newversion=").append(newVersion).toString());
        FileOutputStream updateFileStream = new FileOutputStream(new File((new StringBuilder("mods")).append(File.separator).append(fileName).toString()));
        BufferedInputStream updateInputStream = new BufferedInputStream(updateUrl.openStream());
        byte data[] = new byte[1024];
        int count;
        while ((count = updateInputStream.read(data, 0, 1024)) != -1)
            updateFileStream.write(data, 0, count);
        if (updateInputStream != null) updateInputStream.close();
        if (updateFileStream != null) updateFileStream.close();
        return true;
    }
}