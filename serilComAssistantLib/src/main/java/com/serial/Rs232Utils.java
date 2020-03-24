package com.serial;


import com.priv.yswl.base.tool.L;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.serial.Rs232OutService.isBlank;

public class Rs232Utils {
    private static final String TAG = "Rs232Utils";

    public Rs232Utils() {
    }

    public static String lengthStr(String number, int length) {
        if (isBlank(number)) {
            number = "0";
        }

        int size = number.length();
        if (size < length) {
            for (int i = size; i <= length - size; ++i) {
                number = "0" + number;
            }
        } else if (size > length) {
            number = number.substring(size - length, size);
        }

        return number;
    }

    public static String str2HexStr(String str) {
        if (isBlank(str)) {
            return "";
        } else {
            char[] chars = "0123456789ABCDEF".toCharArray();
            StringBuilder sb = new StringBuilder("");
            byte[] bs = str.getBytes();

            for (int i = 0; i < bs.length; ++i) {
                int bit = (bs[i] & 240) >> 4;
                sb.append(chars[bit]);
                bit = bs[i] & 15;
                sb.append(chars[bit]);
            }

            return sb.toString().trim();
        }
    }

    public static String intToHex(int n) {
        StringBuilder sb = new StringBuilder(8);

        for (char[] b = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'}; n != 0; n /= 16) {
            sb = sb.append(b[n % 16]);
        }

        String a = sb.reverse().toString();
        return a;
    }

    //---------------------//字节数组转转hex字符串----------------------------------
    static public String ByteArrToHex(byte[] inBytArr)
    {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (int i = 0; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
        return String.format("%02x", inByte).toUpperCase();
    }

    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];

        for (int i = 0; i < bytes.length; ++i) {
            int n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 255);
        }

        return new String(bytes);
    }

    public static String daterToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(date);
        return dateString;
    }

    public static void main(String[] arg) {
        String aa = "d5c8d5c8010008000114020C110E29";
        L.d(TAG, sumCheck(aa));
    }

    public static String decToHex(String dec) {
        int data = Integer.parseInt(dec, 10);
        return Integer.toString(data, 16);
    }

    public static String sumCheck(String data) {
        int n = data.length() / 2;
        int sum = 0;

        for (int i = 0; i < n; ++i) {
            int value = Integer.parseInt(data.substring(i * 2, i * 2 + 2), 16);
            sum += value;
            L.d(TAG, "----------------------");
        }

        String result = Integer.toHexString(sum);
        return lengthStr(result, 2);
    }
}
