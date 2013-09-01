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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ColorUtils {
    private static List minecraftColors = Arrays.asList(new Serializable[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "l", "k", "n", "m", "o", "r"});
    private static List ansiColors = Arrays.asList(new Serializable[] {"\033[0;30m", "\033[0;34m", "\033[0;32m", "\033[0;36m", "\033[0;31m", "\033[0;35m", "\033[0;33m", "\033[0;37m", "\033[1;30m", "\033[1;34m", "\033[1;32m", "\033[1;36m", "\033[1;31m", "\033[1;35m", "\033[1;33m", "\033[1;37m", "\033[0;1m", "", "\033[0;4m", "\033[0;9m", "\033[0;3m", "\033[0;0m"});
    public static String realMcColorCode = "\247";
    public static String fakeMcColorCode = "&";

    public static String fakeMCtoRealMCColor(String text) {
        String replaced = text.replace(realMcColorCode, fakeMcColorCode);
        for (Iterator iterator = minecraftColors.iterator(); iterator.hasNext();) {
            Serializable code = (Serializable) iterator.next();
            replaced = replaced.replace((new StringBuilder(String.valueOf(fakeMcColorCode))).append(code).toString(), (new StringBuilder(String.valueOf(realMcColorCode))).append(code).toString());
        }

        return (new StringBuilder(String.valueOf(realMcColorCode))).append("r").append(replaced).append(realMcColorCode).append("r").toString();
    }

    public static String removeAllColors(String text) {
        String replaced = text.replace(realMcColorCode, fakeMcColorCode);
        for (Iterator iterator = ansiColors.iterator(); iterator.hasNext();) {
            Serializable code = (Serializable) iterator.next();
            replaced = replaced.replace((String) code, "");
        }

        for (Iterator iterator1 = minecraftColors.iterator(); iterator1.hasNext();) {
            Serializable code = (Serializable) iterator1.next();
            replaced = replaced.replace((new StringBuilder(String.valueOf(fakeMcColorCode))).append(code).toString(), "");
        }

        return replaced;
    }

    public static String allMCtoANSIColor(String text) {
        String replaced = text.replace(realMcColorCode, fakeMcColorCode);
        for (int i = 0; i < minecraftColors.size(); i++)
            replaced = replaced.replace((new StringBuilder(String.valueOf(fakeMcColorCode))).append(minecraftColors.get(i)).toString(), (String) ansiColors.get(i));

        return (new StringBuilder("\033[0;0m")).append(replaced).append("\033[0;0m").toString();
    }
}