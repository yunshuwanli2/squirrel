package com.priv.yswl.base.scheduler;

/**
 * 自定义任务调度
 * Created on 16/6/29.
 */
public class CustomScheduler {

    private final boolean mInfinite;
    private final int[] mIntervals;
    private Runnable runnable;
    private int count = 0;
    private int jobId;

    /**
     *
     * @param infinite 是否无限使用,如果这个值设置为true, 则intervals的最后一个参数将作为循环的间隔时间
     * @param intervals 时间间隔列表,表示每次调用之后的时间间隔 单位为秒
     */
    public CustomScheduler(boolean infinite, int... intervals) {
        mInfinite = infinite;
        mIntervals = intervals;
    }

    /**
     * 根据设定的时间间隔,运行相应的任务,这个方法会调用Scheduler任务调度器的任务调度方法<br>
     * 这个方法并不会主动持续调用，需要你主动在某个触发条件里调用
     * @param r 任务
     * @return 是否成功指定运行任务
     */
    public boolean runJob(Runnable r) {
        runnable = r;
        int futureMillis;
        if(count < mIntervals.length) {
            futureMillis = mIntervals[count] * 1000;
            jobId = SchedulerManager.getScheduler().addJob(futureMillis, r);
            count ++;
            return true;
        }else if(mInfinite) {
            jobId = SchedulerManager.getScheduler().addJob(mIntervals[mIntervals.length - 1] * 1000, r);
            return true;
        }
        return false;
    }

    /**
     * 判断是否指定了任务
     * @return 指定了任务则返回true,否则返回false
     */
    public boolean hasJob() {
        return runnable != null;
    }

    public void removeJob() {
        runnable = null;
        if(jobId != 0) {
            SchedulerManager.getScheduler().removeJob(jobId);
        }
        count = mIntervals.length;
    }

    /**
     * 在预定的时间后执行之前已指定的任务
     * @return 是否成功指定运行任务
     */
    public boolean runJob() {
        return runnable != null && runJob(runnable);
    }
}
