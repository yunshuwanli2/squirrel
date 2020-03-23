package com.app.squirrel.serial;//package com.app.squirrel.serial;

import android.os.Handler;
import android.os.Looper;

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


 /*   @Deprecated
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
    }*/

    /**
     *
     * @param data
     */
    public static void receiveData(String data) {
        data = data.trim().replace("\r", "").replace("\n", "");
        int length = data.length();

        //核对最后4个字节是否为结束符
        String endStrTmp = data.substring(length-8, length);
        if(!endStrTmp.equals(CommonConstant.endString)) {
            //数据错误，返回
            Looper.prepare();
            ToastUtil.showToast("结尾符数据错误：当前结尾符为：" + endStrTmp);
            Looper.loop();
            System.out.println("结尾符数据错误：当前结尾符为："+endStrTmp);
            return ;
        }


        //校验码
        String checkString = data.substring(length-10, length-8);
        //通过计算，获取校验数据
        String prefix = data.substring(0, length-10);
        //计算校验码
        String sumStr = Rs232Utils.sumCheck(prefix);
        //核对校验码是否正确
        if(!checkString.equals(sumStr)) {
            //数据错误，返回
            Looper.prepare();
            ToastUtil.showToast("校验码错误，数据中校验码为：" + checkString + "，计算校验码为：" + sumStr);
            Looper.loop();
            System.out.println("校验码错误，数据中校验码为："+checkString+"，计算校验码为："+sumStr);
            return ;
        }


        //核对开始字符
        String startStrTmp = data.substring(0, 8);
        if(!startStrTmp.equals(CommonConstant.startString)) {
            //数据错误，返回
            Looper.prepare();
            ToastUtil.showToast("开始字符数据错误：当前开始字符为：" + startStrTmp);
            Looper.loop();
            System.out.println("开始字符数据错误：当前开始字符为："+startStrTmp);
            return ;
        }


        /**
         * 以下开始获取解析真实数据
         */

        String baseData = data.substring(8,length-10);

        //主板地址,一个字节
        String bordHex = baseData.substring(0,2);
        int number = Integer.parseInt(bordHex, 16);

        //数据字节长度 两个字节
        String dataLength = baseData.substring(2,6);
        int dataLength10 = Integer.parseInt(dataLength, 16);

        //获取命令	两个字节
        String order = baseData.substring(6,10);

        //获取时间  六个字节
        String dateTimeStr = baseData.substring(10,22);
        //具体数据
        String dataStr = "";

        //判断是否有数据
        if(dataLength10 >8) {
            //有数据
            dataStr = baseData.substring(22);
        }

        /**
         * 打印数据
         */
        Looper.prepare();
        ToastUtil.showToast("数据结果为："+dataStr);
        Looper.loop();
        System.out.println("数据结果为："+dataStr);



        //通过命令判断功能
        if(order.equals(CommonConstant.IN_OPEN)) {
            //返回开门，时间为请求开门的发送时间，用来判断门是否开了
            //更新开门状态到后台TODO
            System.out.println("开门返回：" + dataStr);

        }else if(order.equals(CommonConstant.IN_BORDINFO)) {
            //返回读取板子信息，发送信息到后台


            int height1 = Integer.parseInt(dataStr.substring(0,2),16);
            int height2 = Integer.parseInt(dataStr.substring(2,4),16);
            //当前重量,单位 kg
            String height = height1+"."+height2;

            //当前温度，单位摄氏度
            int temperature =Integer.parseInt(dataStr.substring(4,6),16);

            //烟雾报警，01有报警，00表示正常
            String smokeWarn = dataStr.substring(6,8);

            //灭火器状态，01有报警，00表示正常
            String fireWarn = dataStr.substring(8,10);

            //时间状态，01有报警，00表示正常
            String timeSet = dataStr.substring(10,12);
            //多个时间段，使用;分隔
            String times = "";

            int dataStrLength = dataStr.length();
            if(dataStrLength > 12) {
                //时间段数量
                int n = (dataStrLength -12)/4;
                for(int i=0; i<n; i++) {
                    //时间格式为：xx(小时):xx(分钟）
                    times += dataStr.substring(12+i*2, 12+i*2+2)+":" + dataStr.substring(12+i*2+2, 12+i*2+4) +";";
                }
                times = times.substring(0,times.length()-1);
            }

            /**
             * 以下发送数据到后台接口，TODO
             */
            mRs232Callback.onReceiveBordInfo(number,height,temperature,smokeWarn,fireWarn,timeSet,times);


        }else if(order.equals(CommonConstant.IN_RESET)) {
            //返回垃圾桶置0，返回结果到后台。TODO

            mRs232Callback.onReset(number);

        }else if(order.equals(CommonConstant.IN_RESET_0)) {
            //返回垃圾桶零点校准，返回结果到后台。TODO
            mRs232Callback.onReset0(number);

        }else if(order.equals(CommonConstant.IN_SET_WEIGHT)) {
            //返回垃圾桶负载校准，返回结果到后台。TODO
            mRs232Callback.onResetWeight(number);

        }else if(order.equals(CommonConstant.IN_SET_TIME)) {
            //返回时间段设置，返回结果到后台。TODO
            mRs232Callback.onSetTime(number);

        }else if(order.equals(CommonConstant.IN_WEIGHT)) {
            Rs232OutService.getHeight(number);
            //返回上传投递重量，发送结果到后台。
			/*int height1 = Integer.parseInt(dataStr.substring(0,2),16);
			int height2 = Integer.parseInt(dataStr.substring(2,4),16);
			//投递重量,单位 kg
			String height = height1+"."+height2;*/
            String height = Integer.parseInt(dataStr.substring(0,4),16)+"";
            /**
             * 发送结果到后台，TODO
             */
            mRs232Callback.onReceiveWeight(number,height,dateTimeStr);

        }else if(order.equals(CommonConstant.IN_FIRE_WARN)) {
            //上传防火报警，界面提示，并发送到后台 TODO

            Rs232OutService.getFireWarn(1);

            String alertMSG = "";

            String temperature = dataStr.substring(0,2);
            String fireWarn = dataStr.substring(2,4);
            if(temperature.equals("01")) {
                alertMSG = "垃圾桶温度过高警报，请联系管理员处理！";
                //TODO ，提示到显示器见面，并发送到后台
                mRs232Callback.onFireWarn(number,alertMSG);
            }
            if(fireWarn.equals("01")) {
                alertMSG = "垃圾桶有烟雾警报，请联系管理员处理！";
                mRs232Callback.onSmokeWarn(number,alertMSG);
            }


        }else if(order.equals(CommonConstant.IN_FULL_WARN)) {
            //TODO 上传满载报警，界面提示，并发送到后台
            String alertMSG = "满了，请联系管理员处理！";
            Rs232OutService.getFullWarn(1);
            mRs232Callback.onFullWarn(number,alertMSG);

        }else if(order.equals(CommonConstant.IN_FIRE_TOOLS_EMPTY)) {
            //TODO 上传灭火溶剂不足，界面提示，并发送到后台
            String alertMSG = "溶剂灭火器不足，请联系管理员处理！";
            Rs232OutService.getFireToolsNotEnough(1);
            mRs232Callback.onFireToolsEmptyWarn(number,alertMSG);

        }else if(order.equals(CommonConstant.IN_MACHINE_WARN)) {
            //TODO 上传电机故障，界面提示，并发送到后台
            Rs232OutService.getmachineWarn(1);
            String alertMSG = "机器故障，请联系管理员处理！";
            Rs232OutService.getmachineWarn(number);
            mRs232Callback.onMachineWarn(number, alertMSG);

        }

    }

  /*  public static void receiveData(String data) {
        data = data.trim().replace("\r", "").replace("\n", "");
        int length = data.length();
        String endStrTmp = data.substring(length - 8, length);
        if (!endStrTmp.equals(CommonConstant.endString)) {
            Looper.prepare();
            ToastUtil.showToast("结尾符数据错误：当前结尾符为：" + endStrTmp);
            Looper.loop();
            L.d(TAG, "结尾符数据错误：当前结尾符为：" + endStrTmp);
        } else {
            String checkString = data.substring(length - 10, length - 8);
            String prefix = data.substring(0, length - 10);
            String sumStr = Rs232Utils.sumCheck(prefix);
            if (!checkString.equals(sumStr)) {
                Looper.prepare();
                ToastUtil.showToast("校验码错误，数据中校验码为：" + checkString + "，计算校验码为：" + sumStr);
                Looper.loop();
                L.d(TAG, "校验码错误，数据中校验码为：" + checkString + "，计算校验码为：" + sumStr);
            } else {
                String startStrTmp = data.substring(0, 8);
                if (!startStrTmp.equals(CommonConstant.startString)) {
                    Looper.prepare();
                    ToastUtil.showToast("开始字符数据错误：当前开始字符为：" + startStrTmp);
                    Looper.loop();
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
                    Looper.prepare();
                    ToastUtil.showToast("数据结果为：" + dataStr);
                    Looper.loop();
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
                            Rs232OutService.getFullWarn(number);
                            mRs232Callback.onFullWarn(number, alertMSG);
                        } else if (order.equals(CommonConstant.IN_FIRE_TOOLS_EMPTY)) {
                            Rs232OutService.getFireToolsNotEnough(number);
                            String alertMSG = "溶剂灭火器不足，请联系管理员处理！";
                            mRs232Callback.onFireToolsEmptyWarn(number, alertMSG);
                        } else if (order.equals(CommonConstant.IN_MACHINE_WARN)) {
                            String alertMSG = "机器故障，请联系管理员处理！";
                            Rs232OutService.getmachineWarn(number);
                            mRs232Callback.onMachineWarn(number, alertMSG);
                        }
                    }

                }
            }
        }
    }
*/
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
        Looper.prepare();
        ToastUtil.showToast("\n 接收到数据: ByteArrToHex(byte[]):" + Rs232Utils.ByteArrToHex(comBean.bRec));
        Looper.loop();
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
