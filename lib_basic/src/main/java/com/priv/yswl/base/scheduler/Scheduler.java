package com.priv.yswl.base.scheduler;

import android.content.Context;

/**
 * 计时器与调度器
 */
public interface Scheduler {

    /**
     * 添加每日任务,任务将在ui线程执行
     * @param hour 0点开始的小时数
     * @param minutes 分钟数
     * @param runnable 任务
     * @return id 标记
     */
    int addDailyJob(int hour, int minutes, Runnable runnable);

    /**
     * 添加每日任务,任务异步执行(非UI线程)
     * @param hour 0点开始的小时数
     * @param minutes 分钟数
     * @param runnable 任务
     * @return id 标记
     */
    int addDailyJobAsync(int hour, int minutes, Runnable runnable);

    /**
     * 等待futureMillis之后执行循环任务，UI线程中执行
     * @param futureMillis 执行任务之前等待的时间
     * @param interval 循环间隔时间,单位ms
     * @param runnable 任务
     * @return id 标记
     */
    int addLoopJob(int futureMillis, int interval, Runnable runnable);

    /**
     * 立即执行循环任务，UI线程中执行
     * @param interval 循环间隔时间,单位ms
     * @param runnable 任务
     * @return id 标记
     */
    int addLoopJob(int interval, Runnable runnable);

    /**
     * 等待futureMillis之后执行循环任务,任务异步执行(非UI线程)
     * @param futureMillis 执行任务之前等待的时间
     * @param interval 循环间隔时间,单位ms
     * @param runnable 任务
     * @return id 标记
     */
    int addLoopJobAsync(int futureMillis, int interval, Runnable runnable);

    /**
     * 立即执行循环任务,任务异步执行(非UI线程)
     * @param interval 循环间隔时间,单位ms
     * @param runnable 任务
     * @return id 标记
     */
    int addLoopJobAsync(int interval, Runnable runnable);

    /**
     * 未来某个时候执行任务，UI线程中执行
     * @param futureMillis 等待时间
     * @param runnable 任务
     * @return id 标记
     */
    int addJob(int futureMillis, Runnable runnable);

    /**
     * 未来某个时候执行任务,任务异步执行(非UI线程)
     * @param futureMillis 等待时间
     * @param runnable 任务
     * @return id 标记
     */
    int addJobAsync(int futureMillis, Runnable runnable);

    /**
     * 移除某个任务
     * @param id 标记
     */
    void removeJob(int id);

    /**
     * 判断是否存在此任务
     * @param id 任务id
     * @return 存在则返回true
     */
    boolean hasJob(int id);

    /**
     * 注册任务调度器
     * @param context 上下文
     */
    void registerScheduler(Context context);

    /**
     * 反注册任务调度器
     * @param context 上下文
     */
    void unregisterScheduler(Context context);

    /**
     * 移除所有任务
     */
    void removeAllJobs();

}
