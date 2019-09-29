package com.sdk.crash.crashreport.errorHandler.anr;

import android.app.ActivityManager;
import android.content.Context;
import android.os.FileObserver;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.Strategy;
import com.sdk.crash.proguard.Logger;
import com.sdk.crash.proguard.ThreadPool;
import com.sdk.crash.proguard.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
class FileWatching {
    private Context context;
    private AtomicInteger state = new AtomicInteger(0);
    private FileObserver fileObserver;
    private boolean watchingState = true;
    private long preTraceTime = -1L;
    private ThreadPool threadPool;
    private String baseDir;

    public final void handleTrace(String tracePath) {
        synchronized (this) {
            if (this.state.get() != 0) {
                Logger.d("trace started return ", new Object[0]);
                return;
            }
            this.state.set(1);
        }

        try {
            Logger.d("read trace first dump for create time!", new Object[0]);
            long time = -1L;
            TraceFileHelper.TraceInfo var4;
            if ((var4 = TraceFileHelper.readFirstDumpInfo(tracePath, false)) != null) {
                time = var4.c;
            }

            if (time == -1L) {
                Logger.w("trace dump fail could not get time!", new Object[0]);
                time = System.currentTimeMillis();
            }

            Strategy strategy = CrashReport.instance().getStrategy();
            if (Math.abs(time - this.preTraceTime) < strategy.getAnrTimeInterval()) {
                Logger.w("should not process ANR too Fre in %d", new Object[]{strategy.getAnrTimeInterval()});
                return;
            }

            this.preTraceTime = time;
            this.state.set(1);

            Map threadStack;
            try {
                threadStack = Utils.getThreadStack(20480, false);
            } catch (Throwable var10) {
                Logger.w(var10);
                Logger.e("get all thread stack fail!", new Object[0]);
                return;
            }

            if (threadStack != null && threadStack.size() > 0) {
                ActivityManager.ProcessErrorStateInfo errorStateInfo;
                if ((errorStateInfo = getProcessErrorState(this.context, 10000L)) == null) {
                    Logger.d("proc state is unvisiable!", new Object[0]);
                    return;
                }

                if (errorStateInfo.pid != android.os.Process.myPid()) {
                    Logger.d("not mind proc!", new Object[]{errorStateInfo.processName});
                    return;
                }

                Logger.v("found visiable anr , start to process!", new Object[0]);
//                this.foundVisiableAnr(this.context, tracePath, errorStateInfo, time, threadStack);
                return;
            }

            Logger.w("can't get all thread skip this anr", new Object[0]);
        } catch (Throwable var11) {
            if (!Logger.w(var11)) {
                var11.printStackTrace();
            }
            Logger.e("handle anr error %s", new Object[]{var11.getClass().toString()});
            return;
        } finally {
            this.state.set(0);
        }
    }

    private static ActivityManager.ProcessErrorStateInfo getProcessErrorState(Context context, long tryTimes) {
        tryTimes = tryTimes < 0L ? 0L : tryTimes;
        Logger.d("to find!", new Object[0]);
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        long tryCounts = tryTimes / 500L;
        int index = 0;

        do {
            Logger.d("waiting!", new Object[0]);
            List infoList;
            if ((infoList = activityManager.getProcessesInErrorState()) != null) {
                Iterator iterator = infoList.iterator();

                while (iterator.hasNext()) {
                    ActivityManager.ProcessErrorStateInfo stateInfo;
                    if ((stateInfo = (ActivityManager.ProcessErrorStateInfo) iterator.next()).condition == 2) {
                        Logger.d("found!", new Object[0]);
                        return stateInfo;
                    }
                }
            }

            Utils.sleep(500L);
        } while ((long) (index++) < tryCounts);

        Logger.d("end!", new Object[0]);
        return null;
    }

    private synchronized void startFileWatch() {
        if (this.isStarted()) {
            Logger.d("start when started!", new Object[0]);
        } else {
            this.fileObserver = new FileObserver("/data/anr/", 8) {
                public final void onEvent(int var1, String var2) {
                    if (var2 != null) {
                        String tracePath;
                        if (!(tracePath = "/data/anr/" + var2).contains("trace")) {
                            Logger.d("not anr file %s", new Object[]{tracePath});
                        } else {
                            FileWatching.this.handleTrace(tracePath);
                        }
                    }
                }
            };

            try {
                this.fileObserver.startWatching();
                Logger.v("start anr monitor!", new Object[0]);
                this.threadPool.schedule(new Runnable() {
                    public final void run() {
                        Utils.checkOutDateTrace(baseDir);
                    }
                });
            } catch (Throwable var2) {
                this.fileObserver = null;
                Logger.d("start anr monitor failed!", new Object[0]);
                if (!Logger.w(var2)) {
                    var2.printStackTrace();
                }
            }
        }
    }

    private synchronized void stopFileWatch() {
        if (!this.isStarted()) {
            Logger.w("close when closed!", new Object[0]);
        } else {
            try {
                this.fileObserver.stopWatching();
                this.fileObserver = null;
                Logger.w("close anr monitor!", new Object[0]);
            } catch (Throwable var2) {
                Logger.w("stop anr monitor failed!", new Object[0]);
                if (!Logger.w(var2)) {
                    var2.printStackTrace();
                }
            }
        }
    }

    private synchronized boolean isStarted() {
        return this.fileObserver != null;
    }

    private synchronized void setFileWatching(boolean var1) {
        if (var1) {
            this.startFileWatch();
        } else {
            this.stopFileWatch();
        }
    }

    //是否在处理状态
    public final boolean isInHandler() {
        return this.state.get() != 0;
    }


    private synchronized boolean getWatchingState() {
        return this.watchingState;
    }

    private synchronized void setState(boolean var1) {
        if (this.watchingState != var1) {
            Logger.v("user change anr %b", new Object[]{var1});
            this.watchingState = var1;
        }
    }

//    public final void changeAnrWatching(boolean isStart) {
//        this.setState(isStart);
//        isStart = this.getWatchingState();
//
//        if (isStart != this.isStarted()) {
//            Logger.i("anr changed to %b", new Object[]{isStart});
//            this.setFileWatching(isStart);
//        }
//    }

}
