package com.serial;

import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.ToastUtil;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Calendar;

public class Rs232OutService {

    private static final String TAG = "Rs232OutService";
    static Rs232Contol rs232Contol;

    public Rs232OutService(Rs232Callback callback) {
        rs232Contol = new Rs232Contol("/dev/ttyS1", 9600, callback);
    }

    public void init() {
        try {
            rs232Contol.open();
        } catch (SecurityException e) {
            ToastUtil.showToast("打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            ToastUtil.showToast("打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            ToastUtil.showToast("打开串口失败:参数错误!");
        }
    }

    public void release() {
        rs232Contol.close();
    }

    public static boolean checkSerilIsOpen() {
        if (rs232Contol.isOpen()) {
            return true;
        }
        L.d(TAG, "串口没有打开");
        return false;
    }

    /**
     * app到单片机
     * 开门
     *
     * @param number
     */
    public static void openDoor(int number) {
        sendData(number, CommonConstant.OUT_OPEN, null);
    }


    /**
     * app到单片机
     * 读取电路板信息
     *
     * @param number
     */
    public static void getBoardInfo(int number) {
        sendData(number, CommonConstant.OUT_WEIGHT, null);
    }


    /**
     * app到单片机
     * 垃圾桶置0
     *
     * @param number
     */
    public static void reset(int number) {
        sendData(number, CommonConstant.OUT_RESET, null);
    }


    /**
     * app到单片机
     * 垃圾桶零点校准
     *
     * @param number
     */
    public static void reset_0(int number) {
        sendData(number, CommonConstant.OUT_RESET_0, null);
    }

    /**
     * app到单片机
     * 垃圾桶负载校准
     *
     * @param number
     */
    public static void resetWight(int number, int height) {

        String hexStr = Rs232Utils.lengthStr(Rs232Utils.intToHex(height), 2);
        sendData(number, CommonConstant.OUT_SET_WEIGHT, hexStr);
    }


    /**
     * app到单片机
     * 时间段设置
     *
     * @param number
     * @param isOn   true打开，false关闭
     * @param data   设置时间，格式：XX(时)XX(分)  XX(时)XX(分)
     *               例子，15301830 ，其中15:30和18:30两个时间
     */
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
            int length = data.length();
            for (int i = 0; i < length; ) {
                String hour = Rs232Utils.lengthStr(Rs232Utils.intToHex(Integer.parseInt(data.substring(i, i + 2))), 2);
                String second = Rs232Utils.lengthStr(Rs232Utils.intToHex(Integer.parseInt(data.substring(i + 2, i + 4))), 2);
                times += hour + second;
                i = i + 4;
            }
        }

        sendData(number, CommonConstant.OUT_SET_TIME, times);
    }


    /**
     * app到单片机
     * 返回上传投递重量
     *
     * @param number
     */
    @Deprecated
    public static void getHeight(int number) {
        sendData(number, CommonConstant.OUT_WEIGHT, null);
    }


    /**
     * app到单片机
     * 返回上传防火报警
     *
     * @param number
     */
    @Deprecated
    public static void getFireWarn(int number) {
        sendData(number, CommonConstant.OUT_FIRE_WARN, null);
    }


    /**
     * app到单片机
     * 返回上传满载报警
     *
     * @param number
     */
    @Deprecated
    public static void getFullWarn(int number) {
        sendData(number, CommonConstant.OUT_FULL_WARN, null);
    }


    /**
     * app到单片机
     * 返回上传灭火溶剂不足
     *
     * @param number
     */
    @Deprecated
    public static void getFireToolsNotEnough(int number) {
        sendData(number, CommonConstant.OUT_FIRE_TOOLS_EMPTY, null);
    }


    /**
     * app到单片机
     * 返回 电机报警
     *
     * @param number
     */
    @Deprecated
    public static void getmachineWarn(int number) {
        sendData(number, CommonConstant.OUT_MACHINE_WARN, null);
    }


    /**
     * 组装发送前数据
     *
     * @param number 主板地址
     * @param order  命令
     * @param data   具体数据
     * @return 返回具体数据字符串
     */
    private static void sendData(int number, String order, String data) {

        //-----------开始组装主要发送的数据字符串------------

        String dataLength = "";

        if (isBlank(data)) {
            data = "";
        }


        /**
         * 获取当前日期
         */
        Calendar now = Calendar.getInstance();
        String year = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(Calendar.YEAR) - 2000), 2);
        String month = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(Calendar.MONTH) + 1), 2);
        String day = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(Calendar.DAY_OF_MONTH)), 2);
        String hour = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(Calendar.HOUR_OF_DAY)), 2);
        String minute = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(Calendar.MINUTE)), 2);
        String second = Rs232Utils.lengthStr(Rs232Utils.intToHex(now.get(Calendar.SECOND)), 2);


        String date = year + month + day + hour + minute + second;
        L.d(TAG, "时间：" + date);
        dataLength = Rs232Utils.lengthStr(Rs232Utils.intToHex(((order + date + data).length()) / 2), 4);


        String result = "";

        result = CommonConstant.startString + Rs232Utils.lengthStr(Rs232Utils.intToHex(number), 2) + dataLength + order + date + data;

        String sumStr = Rs232Utils.sumCheck(result);

        result += sumStr + new String(CommonConstant.endString);
        //-----------字符串组装结束---------------------

        //-----------发送数据开始-------------------
        L.d(TAG, result);
        if (rs232Contol != null && checkSerilIsOpen()) {
            rs232Contol.sendHex(result);
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
