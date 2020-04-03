package com.app.squirrel.serial;

public class CommonConstant {

    public static String startString = "d5c8d5c8";
    public static String endString = "13ab13ab";


    /**
     * app到单片机
     */
    //开门
    public static String OUT_OPEN = "0001";
    //读取电路板信息
    public static String OUT_BORDINFO = "0002";
    //垃圾桶置0
    public static String OUT_RESET = "0003";
    //垃圾桶零点校准
    public static String OUT_RESET_0 = "0004";
    //垃圾桶负载校准
    public static String OUT_SET_WEIGHT = "0005";
    //时间段设置
    public static String OUT_SET_TIME = "0006";
    //上传投递重量
    public static String OUT_WEIGHT =  "400a";
    //上传防火报警
    public static String OUT_FIRE_WARN =  "400b";
    //上传满载报警
    public static String OUT_FULL_WARN =  "400c";
    //上传灭火溶剂不足
    public static String OUT_FIRE_TOOLS_EMPTY =  "400d";
    //上传电机故障
    public static String OUT_MACHINE_WARN =  "400e";


    /**
     * 单片机到app
     */
    //开门
    public static String IN_OPEN =  "4001";
    //读取电路板信息
    public static String IN_BORDINFO =  "4002";
    //垃圾桶置0
    public static String IN_RESET =  "4003";
    //垃圾桶零点校准
    public static String IN_RESET_0 =  "4004";
    //垃圾桶负载校准
    public static String IN_SET_WEIGHT =  "4005";
    //时间段设置
    public static String IN_SET_TIME =  "4006";
    //上传投递重量
    public static String IN_WEIGHT = "000a";
    //上传防火报警
    public static String IN_FIRE_WARN = "000b";
    //上传满载报警
    public static String IN_FULL_WARN = "000c";
    //上传灭火溶剂不足
    public static String IN_FIRE_TOOLS_EMPTY = "000d";
    //上传电机故障
    public static String IN_MACHINE_WARN = "000e";


}
