package com.app.squirrel.serial;

import java.util.Calendar;

public class Rs232OutService {
    static Rs232Contol rs232Contol;

    public Rs232OutService(Rs232Callback callback) {
        rs232Contol = new Rs232Contol("/dev/ttyS1", 9600, callback);
    }

    public static void openDoor(int number) {
        sendData(number, CommonConstant.OUT_OPEN, (String) null);
    }

    public static void getBoardInfo(int number) {
        sendData(number, CommonConstant.OUT_BORDINFO, (String) null);
    }

    public static void reset(int number) {
        sendData(number, CommonConstant.OUT_RESET, (String) null);
    }

    public static void reset_0(int number) {
        sendData(number, CommonConstant.OUT_RESET_0, (String) null);
    }

    public static void resetWight(int number, int height) {
        String hexStr = Rs232Utils.lengthStr(Rs232Utils.intToHex(height), 2);
        sendData(number, CommonConstant.OUT_SET_WEIGHT, hexStr);
    }

    public static void setTime(int number, boolean isOn, String data) {
        String open = "";
        String times = "";
        if (isOn) {
            open = "01";
        } else {
            open = "00";
        }

        times = open;
        if (isNotBlank(data)) {
            int length = data.length() / 4;

            for (int i = 0; i < length; ++i) {
                String hour = Rs232Utils.lengthStr(Rs232Utils.intToHex(Integer.parseInt(data.substring(i, i + 2))), 2);
                String second = Rs232Utils.lengthStr(Rs232Utils.intToHex(Integer.parseInt(data.substring(i + 2, i + 4))), 2);
                times = times + hour + second;
            }
        }

        sendData(number, CommonConstant.OUT_SET_TIME, times);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void getHeight(int number) {
        sendData(number, CommonConstant.OUT_WEIGHT, (String) null);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void getFireWarn(int number) {
        sendData(number, CommonConstant.OUT_FIRE_WARN, (String) null);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void getFullWarn(int number) {
        sendData(number, CommonConstant.OUT_FULL_WARN, (String) null);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void getFireToolsNotEnough(int number) {
        sendData(number, CommonConstant.OUT_FIRE_TOOLS_EMPTY, (String) null);
    }

    private static void sendData(int number, String order, String data) {
        String dataLength = "";
        if (isBlank(data)) {
            data = "";
        }

        Calendar now = Calendar.getInstance();
        String year = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(1) - 2000), 2);
        String month = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(2) + 1), 2);
        String day = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(5)), 2);
        String hour = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(11)), 2);
        String minute = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(12)), 2);
        String second = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(13)), 2);
        String date = year + month + day + hour + minute + second;
        dataLength = Rs232Utils.lengthStr(Rs232Utils.intToHex((order + date + data).length() / 2), 4);
        String result = "";
        result = CommonConstant.startString + Rs232Utils.lengthStr(Rs232Utils.intToHex(number), 2) + dataLength + order + date + data;
        String sumStr = Rs232Utils.sumCheck(result);
        result = result + sumStr + new String(CommonConstant.endString);
        System.out.println(result);
        if (rs232Contol != null) {
            rs232Contol.sendTxt(result);
        }

    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                // 判断字符是否为空格、制表符、tab
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        getBoardInfo(1);
    }
}
