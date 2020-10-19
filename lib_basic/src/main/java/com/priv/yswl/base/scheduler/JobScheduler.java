package com.priv.yswl.base.scheduler;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务调度器
 */
public class JobScheduler implements Scheduler {

    public static final String TAG = "JobScheduler";
    private final ExecutorService cachedThreadPool;
    private Map<String, HandlerContainer> mHandlerMap;
    private DayLoopReceiver dayLooperReceiver;
    private int seed = 10;
    private volatile boolean startClear;

    /*singleton*/
    private static class JobSchedulerHolder {
        private static final JobScheduler INSTANCE = new JobScheduler();
    }

    protected static JobScheduler getInstance() {
        return JobSchedulerHolder.INSTANCE;
    }

    private JobScheduler() {
        mHandlerMap = new ConcurrentHashMap<>();
        mHandlerMap.put(Thread.currentThread().getName(), new HandlerContainer(genHandler()));
        cachedThreadPool = Executors.newCachedThreadPool();
    }
    /*singleton end*/

    private static class MainHandler extends Handler {
        private WeakReference<JobScheduler> obj;

        public MainHandler(JobScheduler scheduler) {
            obj = new WeakReference<>(scheduler);
        }

        public MainHandler(Looper looper, JobScheduler scheduler) {
            super(looper);
            obj = new WeakReference<>(scheduler);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (obj != null && obj.get() != null) {
                final JobScheduler scheduler = obj.get();
                if(msg.arg2 != 0) {//需要循环处理
                    Message m = Message.obtain(this,msg.what,msg.arg1,msg.arg2,msg.obj);
                    sendMessageDelayed(m, msg.arg2);
                }else {//不需要循环处理的,从列表remove掉此id
                    scheduler.getHandlerContainer().messages.remove(Integer.valueOf(msg.what));
                }
                Runnable runnable = (Runnable) msg.obj;
                if(msg.arg1 == 1) {//异步
                    scheduler.cachedThreadPool.execute(runnable);
                }else {//同步
                    runnable.run();
                }
            }
        }
    }

    private static class HandlerContainer {
        public HandlerContainer(Handler handler) {
            this.handler = handler;
        }
        public Handler handler;
        public List<Integer> messages = new ArrayList<>();
    }

    /*
     * 生成一个对应线程的Handler
     */
    private Handler genHandler() {
        Handler handler;
        if(Looper.myLooper() != null) {//主线程或者已经生成了looper的线程
//            Log.d("JobScheduler", "looper not null");
            handler = new MainHandler(this);
        }else {
            //这种方式需要用回调,暂时不采用
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Looper.prepare();
//                    handler = new MainHandler(JobScheduler.this);
//                    Looper.loop();
//                }
//            }).start();

            // 使用主线程looper,这样使所有任务都能同步执行在UI线程
            // 或者异步执行在新开的子线程
//            Log.d("JobScheduler", "looper null");
            handler = new MainHandler(Looper.getMainLooper(), this);
        }
        return handler;
    }

    /*
    获取当前的handler
     */
    private Handler getHandler() {
        return getHandlerContainer().handler;
    }

    private HandlerContainer getHandlerContainer() {
        HandlerContainer container = mHandlerMap.get(Thread.currentThread().getName());
        if(container == null) {//未生成对应的handler
            container = new HandlerContainer(genHandler());
            mHandlerMap.put(Thread.currentThread().getName(), container);
        }
        return container;
    }

    @Override
    public int addDailyJob(int hour, int minutes, Runnable runnable) {
        return addDailyJob(hour, minutes, runnable, false);
    }

    @Override
    public int addDailyJobAsync(int hour, int minutes, Runnable runnable) {
        return addDailyJob(hour, minutes, runnable, true);
    }

    private int addDailyJob(int hour, int minutes, Runnable r, boolean async) {
        if(dayLooperReceiver == null) {
            throw new RuntimeException("you should register scheduler first!");
        }

        int id = genId();
        boolean result = dayLooperReceiver.addJob(id, r, async, DayLoopReceiver.getTimeSeconds(hour, minutes, 0));
        return result ? id : 0;
    }
    @Override
    public int addLoopJob(int futureMillis, int interval, Runnable runnable) {
        return addLoopJob(futureMillis, interval, runnable, false);
    }

    @Override
    public int addLoopJob(int interval, Runnable runnable) {
        return addLoopJob(0, interval, runnable);
    }

    @Override
    public int addLoopJobAsync(int futureMillis, int interval, Runnable runnable) {
        return addLoopJob(futureMillis, interval, runnable, true);
    }

    @Override
    public int addLoopJobAsync(int interval, Runnable runnable) {
        return addLoopJobAsync(0, interval, runnable);
    }

    private int addLoopJob(int futureMillis, int interval, Runnable r, boolean async) {
        if(!startClear) {
            int id = genId();
            Handler handler = getHandler();
            Message msg = Message.obtain();
            msg.what = id;
            getHandlerContainer().messages.add(id);
            msg.obj = r;
            msg.arg1 = async ? 1 : 0;
            msg.arg2 = interval;
            boolean success = handler.sendMessageDelayed(msg, futureMillis);
            return success ? id : 0;
        }else {
            return 0;
        }
    }

    @Override
    public int addJob(int futureMillis, Runnable runnable) {
        return addJob(futureMillis, runnable, false);
    }

    @Override
    public int addJobAsync(int futureMillis, Runnable runnable) {
        return addJob(futureMillis,runnable, true);
    }

    private int addJob(int futureMillis, Runnable r, boolean async) {
        if(!startClear) {
            int id = genId();
            Handler handler = getHandler();
            Message msg = Message.obtain();
            msg.what = id;
            getHandlerContainer().messages.add(id);
            msg.obj = r;
            msg.arg1 = async ? 1 : 0;
            msg.arg2 = 0;
            boolean success = handler.sendMessageDelayed(msg, futureMillis);
            return success ? id : 0;
        }else {
            return 0;
        }
    }

    /**
     * 获取一个不重复的id
     * @return id 随机数
     */
    private int genId() {
        int id = ++ seed;
        if(seed == Integer.MAX_VALUE) {
            seed = 10;
        }
        return id;
    }

    @Override
    public void removeJob(int id) {
        for (Map.Entry<String, HandlerContainer> entry :
                mHandlerMap.entrySet()) {
            if (entry.getValue().messages.contains(id)) {
                if (!startClear) {
                    entry.getValue().handler.removeMessages(id);
                    entry.getValue().messages.remove(Integer.valueOf(id));
                }
            }
        }
        if(dayLooperReceiver != null) {
            dayLooperReceiver.removeJob(id);
        }
    }

    @Override
    public boolean hasJob(int id) {
        boolean hasMessage = false;
        for (Map.Entry<String, HandlerContainer> entry:
                mHandlerMap.entrySet()){
            hasMessage = entry.getValue().messages.contains(id);
        }
        return hasMessage
                || (dayLooperReceiver != null && dayLooperReceiver.containsJob(id));
    }

    @Override
    public void registerScheduler(Context context) {
        if(dayLooperReceiver == null) {
            dayLooperReceiver = new DayLoopReceiver(cachedThreadPool);
            context.registerReceiver(dayLooperReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        }else {
            Log.w("JobScheduler", "scheduler was registered already");
        }
    }

    @Override
    public void unregisterScheduler(Context context) {
        if(dayLooperReceiver != null) {
            context.unregisterReceiver(dayLooperReceiver);
            dayLooperReceiver = null;
        }
    }

    @Override
    public void removeAllJobs() {
        try {
            for (Map.Entry<String, HandlerContainer> entry :
                    mHandlerMap.entrySet()) {
                HandlerContainer container = entry.getValue();
                startClear = true;
                for (int id :
                        container.messages) {
                    container.handler.removeMessages(id);//移除任务
                }
                container.messages.clear();//移除所有id
                startClear = false;
            }
            if (dayLooperReceiver != null) {
                dayLooperReceiver.removeAllJobs();
            }
        }catch (Exception e) {
            Log.e("JobScheduler", "removeAllJobs fail", e);
        }
    }
}
