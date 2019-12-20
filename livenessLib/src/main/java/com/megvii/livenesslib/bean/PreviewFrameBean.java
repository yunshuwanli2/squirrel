package com.megvii.livenesslib.bean;

public class PreviewFrameBean {

    private volatile byte[] frame;
    private volatile int width;
    private volatile int height;

    public byte[] getFrame() {
        return frame;
    }

    public void setFrame(byte[] frame, int width, int height) {
        synchronized (this) {
            this.frame = frame;
            this.width = width;
            this.height = height;
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
