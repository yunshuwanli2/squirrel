package com.app.squirrel.serial;//package com.app.squirrel.serial;

import com.bjw.bean.ComBean;
import com.bjw.utils.SerialHelper;

import java.io.IOException;
import java.io.OutputStream;

public class Rs232Contol extends SerialHelper {

    private static Rs232Callback mRs232Callback;
    static OutputStream outputStream;

    public Rs232Contol(String sPort, int iBaudRate,Rs232Callback rs232Callback) {
        super(sPort,iBaudRate);
        mRs232Callback = rs232Callback;
    }


    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src != null && src.length > 0) {
            for(int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
            }

            return stringBuilder.toString();
        } else {
            return null;
        }
    }


    @Deprecated
    public static boolean sendData(String data) {
        try {
            System.out.println("发出字节数：" + data.getBytes("utf-8").length);
            System.out.println(data);
            byte[] dataArr = hexStringToBytes(data);
            System.out.println(dataArr);
            outputStream.write(dataArr, 0, dataArr.length);
            return true;
        } catch (IOException var2) {
            var2.printStackTrace();
            return false;
        }
    }



    public static void receiveData(String data) {
        data = data.trim().replace("\r", "").replace("\n", "");
        int length = data.length();
        String endStrTmp = data.substring(length - 8, length);
        if (!endStrTmp.equals(CommonConstant.endString)) {
            System.out.println("结尾符数据错误：当前结尾符为：" + endStrTmp);
        } else {
            String checkString = data.substring(length - 10, length - 8);
            String prefix = data.substring(0, length - 10);
            String sumStr = Rs232Utils.sumCheck(prefix);
            if (!checkString.equals(sumStr)) {
                System.out.println("校验码错误，数据中校验码为：" + checkString + "，计算校验码为：" + sumStr);
            } else {
                String startStrTmp = data.substring(0, 8);
                if (!startStrTmp.equals(CommonConstant.startString)) {
                    System.out.println("开始字符数据错误：当前开始字符为：" + startStrTmp);
                } else {
                    String baseData = data.substring(8, length - 10);
                    String bordHex = baseData.substring(0, 2);
                    int number = Integer.parseInt(bordHex, 16);
                    String dataLength = baseData.substring(2, 6);
                    int dataLength10 = Integer.parseInt(dataLength, 16);
                    String order = baseData.substring(6, 10);
                    String dateTimeStr = baseData.substring(10, 22);
                    String dataStr = "";
                    if (dataLength10 > 8) {
                        dataStr = baseData.substring(22);
                    }

                    System.out.println("数据结果为：" + dataStr);
                    if (!order.equals(CommonConstant.IN_OPEN)) {
                        int height1;
                        int height2;
                        String fireWarn;
                        if (order.equals(CommonConstant.IN_BORDINFO)) {
                            height1 = Integer.parseInt(dataStr.substring(0, 2), 16);
                            height2 = Integer.parseInt(dataStr.substring(2, 4), 16);
                             fireWarn = height1 + "." + height2;
                            int temperature = Integer.parseInt(dataStr.substring(4, 6), 16);
                            String smokeWarn = dataStr.substring(6, 8);
                             fireWarn = dataStr.substring(8, 10);
                            String timeSet = dataStr.substring(10, 12);
                            String times = "";
                            int dataStrLength = dataStr.length();
                            if (dataStrLength > 12) {
                                int n = (dataStrLength - 12) / 4;

                                for(int i = 0; i < n; ++i) {
                                    times = times + dataStr.substring(12 + i * 2, 12 + i * 2 + 2) + ":" + dataStr.substring(12 + i * 2 + 2, 12 + i * 2 + 4) + ";";
                                }

                                times = times.substring(0, times.length() - 1);
                            }

                            mRs232Callback.onReceiveBordInfo(number, fireWarn, temperature, smokeWarn, fireWarn, timeSet, times);
                        } else if (order.equals(CommonConstant.IN_RESET)) {
                            mRs232Callback.onReset(number);
                        } else if (order.equals(CommonConstant.IN_RESET_0)) {
                            mRs232Callback.onReset0(number);
                        } else if (order.equals(CommonConstant.IN_SET_WEIGHT)) {
                            mRs232Callback.onResetWeight(number);
                        } else if (order.equals(CommonConstant.IN_SET_TIME)) {
                            mRs232Callback.onSetTime(number);
                        } else if (order.equals(CommonConstant.IN_WEIGHT)) {
                            height1 = Integer.parseInt(dataStr.substring(0, 2), 16);
                            height2 = Integer.parseInt(dataStr.substring(2, 4), 16);
                            fireWarn = height1 + "." + height2;
                            mRs232Callback.onReceiveWeight(number, fireWarn, dateTimeStr);
                        } else if (order.equals(CommonConstant.IN_FIRE_WARN)) {
                            String alertMSG = "";
                            String temperature = dataStr.substring(0, 2);
                            fireWarn = dataStr.substring(2, 4);
                            if (temperature.equals("01")) {
                                alertMSG = "垃圾桶温度过高警报，请联系管理员处理！";
                                System.out.println(alertMSG);
                                mRs232Callback.onFireWarn(number);
                            }

                            if (fireWarn.equals("01")) {
                                alertMSG = "垃圾桶有烟雾警报，请联系管理员处理！";
                                System.out.println(alertMSG);
                                mRs232Callback.onSmokeWarn(number);
                            }
                        } else if (order.equals(CommonConstant.IN_FULL_WARN)) {
                            mRs232Callback.onFullWarn(number);
                        } else if (order.equals(CommonConstant.IN_FIRE_TOOLS_EMPTY)) {
                            mRs232Callback.onFireToolsEmptyWarn(number);
                        } else if (order.equals(CommonConstant.IN_MACHINE_WARN)) {
                            mRs232Callback.onMachineWarn(number);
                        }
                    }

                }
            }
        }
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString != null && !hexString.equals("")) {
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];

            for(int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

 /*   public static void main(String[] args) {
        sendData("sendData");
    }
*/
    @Override
    protected void onDataReceived(ComBean comBean) {
        System.out.println("[onDataReceived] 接收到的数据为：" + comBean.bRec);
        receiveData(new String(comBean.bRec));
    }
}
