package com.app.squirrel.serial;//package com.app.squirrel.serial;

import com.bjw.bean.ComBean;
import com.bjw.utils.SerialHelper;
import com.priv.yswl.base.tool.L;
import com.priv.yswl.base.tool.ToastUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

public class Rs232Contol extends SerialHelper {

    public static final String TAG = "Rs232Contol";
    private static Rs232Callback mRs232Callback;
    static OutputStream outputStream;
    DispQueueThread mDispQueue;

    public Rs232Contol(String sPort, int iBaudRate, Rs232Callback rs232Callback) {
        super(sPort, iBaudRate);
        mRs232Callback = rs232Callback;
        mDispQueue = new DispQueueThread();
        mDispQueue.start();
    }


    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src != null && src.length > 0) {
            for (int i = 0; i < src.length; ++i) {
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
            L.d(TAG, "发出字节数：" + data.getBytes("utf-8").length);
            L.d(TAG, data);
            byte[] dataArr = hexStringToBytes(data);
            L.d(TAG, new String(dataArr));
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
            ToastUtil.showToast("结尾符数据错误：当前结尾符为：" + endStrTmp);
            L.d(TAG, "结尾符数据错误：当前结尾符为：" + endStrTmp);
        } else {
            String checkString = data.substring(length - 10, length - 8);
            String prefix = data.substring(0, length - 10);
            String sumStr = Rs232Utils.sumCheck(prefix);
            if (!checkString.equals(sumStr)) {
                ToastUtil.showToast("校验码错误，数据中校验码为：" + checkString + "，计算校验码为：" + sumStr);
                L.d(TAG, "校验码错误，数据中校验码为：" + checkString + "，计算校验码为：" + sumStr);
            } else {
                String startStrTmp = data.substring(0, 8);
                if (!startStrTmp.equals(CommonConstant.startString)) {
                    ToastUtil.showToast("开始字符数据错误：当前开始字符为：" + startStrTmp);
                    L.d(TAG, "开始字符数据错误：当前开始字符为：" + startStrTmp);
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
                    ToastUtil.showToast("数据结果为：" + dataStr);
                    L.d(TAG, "数据结果为：" + dataStr);
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

                                for (int i = 0; i < n; ++i) {
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
                                alertMSG = "温度过高警报，请联系管理员处理！";
                                L.d(TAG, alertMSG);
                                mRs232Callback.onFireWarn(number, alertMSG);
                            }

                            if (fireWarn.equals("01")) {
                                alertMSG = "有烟雾警报，请联系管理员处理！";
                                L.d(TAG, alertMSG);
                                mRs232Callback.onSmokeWarn(number, alertMSG);
                            }
                        } else if (order.equals(CommonConstant.IN_FULL_WARN)) {
                            String alertMSG = "满了，请联系管理员处理！";
                            mRs232Callback.onFullWarn(number, alertMSG);
                        } else if (order.equals(CommonConstant.IN_FIRE_TOOLS_EMPTY)) {
                            String alertMSG = "消防工具没有了，请联系管理员处理！";
                            mRs232Callback.onFireToolsEmptyWarn(number, alertMSG);
                        } else if (order.equals(CommonConstant.IN_MACHINE_WARN)) {
                            String alertMSG = "机器故障，请联系管理员处理！";
                            mRs232Callback.onMachineWarn(number, alertMSG);
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

            for (int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /*   public static void main(String[] args) {
           sendData("sendData");
       }
   */
    @Override
    protected void onDataReceived(ComBean comBean) {
        ToastUtil.showToast("接收到数据");
//        String hexStr = bytesToHexString(comBean.bRec);
//        receiveData(hexStr);
        mDispQueue.AddQueue(comBean);
    }

    //----------------------------------------------------刷新显示线程
    private class DispQueueThread extends Thread {
        private Queue<ComBean> QueueList = new LinkedList<ComBean>();

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                final ComBean ComData;
                while ((ComData = QueueList.poll()) != null) {
                    dispRecData(ComData);
                    break;
                }
            }
        }

        public synchronized void AddQueue(ComBean ComData) {
            QueueList.add(ComData);
        }
    }


    //----------------------------------------------------显示接收数据
    private void dispRecData(ComBean ComRecData) {
        receiveData(Rs232Utils.ByteArrToHex(ComRecData.bRec));
    }
}
