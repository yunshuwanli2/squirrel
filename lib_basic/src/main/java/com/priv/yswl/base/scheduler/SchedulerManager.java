package com.priv.yswl.base.scheduler;

/**
 * 任务调度管理器
 */
public class SchedulerManager {

    /**
     * 获取一个任务调度器
     * @return 任务调度器
     */
    public static Scheduler getScheduler() {
        return JobScheduler.getInstance();
    }

    /**
     * 获取一个自定义的任务调度器
     * @param infinite 是否无限使用,如果这个值设置为true, 则intervals的最后一个参数将作为循环的间隔时间
     * @param intervals 时间间隔列表,表示每次调用之后的时间间隔 单位为秒
     * @return 自定义任务调度器
     */
    public static CustomScheduler getCustomScheduler(boolean infinite, int... intervals) {
        return new CustomScheduler(infinite, intervals);
    }
}
