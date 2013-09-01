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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ArrayUtils {
    public static List arrayToList(Object data[]) {
        List newdata = new ArrayList();
        Object aobj[];
        int j = (aobj = data).length;
        for (int i = 0; i < j; i++) {
            Object curbyte = aobj[i];
            newdata.add(curbyte);
        }

        return newdata;
    }

    public static Object[] listToArray(List data) {
        Object newdata[] = new Object[data.size()];
        int i = 0;
        for (Iterator iterator = data.iterator(); iterator.hasNext();) {
            Object cur = iterator.next();
            newdata[i] = cur;
            i++;
        }

        return newdata;
    }

    public static char[] charArrayToPrimitive(Object[] data) {
        char newdata[] = new char[data.length];
        int i = 0;
        Object aobj[];
        int k = (aobj = data).length;
        for (int j = 0; j < k; j++) {
            Object cur = aobj[j];
            newdata[i] = ((Character) cur);
            i++;
        }

        return newdata;
    }

    public static byte[] byteArrayToPrimitive(Object[] data) {
        byte newdata[] = new byte[data.length];
        int i = 0;
        Object aobj[];
        int k = (aobj = data).length;
        for (int j = 0; j < k; j++) {
            Object cur = aobj[j];
            newdata[i] = ((Byte) cur);
            i++;
        }

        return newdata;
    }

    public static String listAsHex(List array) {
        String hex = "";
        int i = 0;
        for (Iterator iterator = array.iterator(); iterator.hasNext();) {
            byte curbyte = ((Byte) iterator.next()).byteValue();
            hex = (new StringBuilder(String.valueOf(hex))).append(Integer.toString(curbyte, 16)).toString();
            if (i < array.size() - 1) hex = (new StringBuilder(String.valueOf(hex))).append(", ").toString();
            i++;
        }

        return (new StringBuilder("[")).append(hex).append("]").toString();
    }

    public static String join(Object array[], String separator) {
        if (array == null) return null;
        if (separator == null) separator = "";
        int noOfItems = array.length - 0;
        if (noOfItems <= 0) return "";
        StringBuilder buf = new StringBuilder(noOfItems * 16);
        for (int i = 0; i < array.length; i++) {
            if (i > 0) buf.append(separator);
            if (array[i] != null) buf.append(array[i]);
        }

        return buf.toString();
    }

    public static Byte[] primitiveByteArrayToByteArray(byte data[]) {
        Byte newdata[] = new Byte[data.length];
        int i = 0;
        byte abyte0[];
        int k = (abyte0 = data).length;
        for (int j = 0; j < k; j++) {
            byte cur = abyte0[j];
            newdata[i] = Byte.valueOf(cur);
            i++;
        }

        return newdata;
    }

    public static String[] objectArrayToStringArray(Object data[]) {
        String newdata[] = new String[data.length];
        int i = 0;
        Object aobj[];
        int k = (aobj = data).length;
        for (int j = 0; j < k; j++) {
            Object cur = aobj[j];
            newdata[i] = (String) cur;
            i++;
        }

        return newdata;
    }

    public static <T> List<T> setToList(Set<T> set) {
        List<T> list = new ArrayList<T>();

        for (T item : set) {
            list.add(item);
        }

        return list;
    }
}