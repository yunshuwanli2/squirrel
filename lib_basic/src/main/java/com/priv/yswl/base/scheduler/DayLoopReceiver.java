package com.priv.yswl.base.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 接收每日任务的处理
 */
public class DayLoopReceiver extends BroadcastReceiver {

    private final ExecutorService mThreadPool;

    public DayLoopReceiver(ExecutorService threadPool) {
        mThreadPool = threadPool;
    }

    private Map<Integer, JobContainer> jobMap = new ConcurrentHashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {
//        L.log("接收到时间变化广播");
//        L.log("当前时间秒数为:" + DateUtil.getTodayTimeSeconds() + "s");
        for (Map.Entry<Integer, JobContainer> entry :
                jobMap.entrySet()) {
            JobContainer job = entry.getValue();
//            L.log("每日任务, id=" + entry.getKey() + ", 设置的秒数为:" + job.timeSeconds + "s");
            // 这个广播每个整分钟数接收一次,所以当到达指定的时间点时,
            // 两者的时间差应该在1秒内,但是考虑到可能存在的时延,将误差范围延长至10秒
            if(Math.abs(getTodayTimeSeconds() - job.timeSeconds) < 10) {
                //小于1分钟,说明已到达触发时间
                if(job.async) {
                    mThreadPool.execute(job.runnable);
                }else {
                    job.runnable.run();
                }
            }
        }
    }

    /**
     * 添加一个任务
     * @param id 任务标记
     * @param r 任务
     * @param async 是否异步执行
     * @param timeSeconds 时间值
     * @return 如果添加成功,则返回true,如果已存在此任务,则返回false
     */
    protected boolean addJob(int id, Runnable r, boolean async, long timeSeconds) {
        JobContainer jobContainer = new JobContainer(r, async, timeSeconds);
        if(jobMap.containsKey(id)) {
            return false;
        }else {
            jobMap.put(id, jobContainer);
            return true;
        }
    }

    protected void removeJob(int id) {
        if(jobMap.containsKey(id)) {
            jobMap.remove(id);
        }
    }

    protected void removeAllJobs() {
        jobMap.clear();
    }

    protected boolean containsJob(int id) {
        return jobMap.containsKey(id);
    }

    private static class JobContainer {
        public JobContainer(Runnable r, boolean async, long timeSeconds) {
            runnable = r;
            this.async = async;
            this.timeSeconds = timeSeconds;
        }
        public Runnable runnable;
        public boolean async;
        public long timeSeconds;
    }


    /**
     * 获取今日0点到当前时间的总秒数
     */
    public static int getTodayTimeSeconds() {
        SimpleDateFormat format7 = new SimpleDateFormat("HHmmss", Locale.CHINA);
        String time = format7.format(new Date());
        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(2, 4));
        int second = Integer.parseInt(time.substring(4, 6));
        return getTimeSeconds(hour, minute, second);
    }

    public static int getTimeSeconds(int hour, int minute, int second) {
        return hour * 3600 + minute * 60 + second;
    }
}
