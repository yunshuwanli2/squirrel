package com.megvii.livenesslib;


import java.io.Serializable;

/**
 * @author nixn@yunhetong.net
 */
public class LiveBean implements Serializable {

    public String delta;

    public byte[] image_best;
    public byte[] image_env;
    public byte[] action1;
    public byte[] action2;
    public byte[] action3;

    public byte[] head;
    public byte[] idcard;

    public static void copyVaule(LiveBean bean1,LiveBean bean2){
        bean1.image_best = bean2.image_best;
        bean1.image_env = bean2.image_env;
        bean1.action1 = bean2.action1;
        bean1.action2 = bean2.action2;
        bean1.action3 = bean2.action3;
        bean1.delta = bean2.delta;
    }

}
