//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sdk.crash.crashreport.errorHandler.anr;

import android.app.ActivityManager;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.content.Context;
import android.os.Process;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.Strategy;
import com.sdk.crash.crashreport.common.Uploader;
import com.sdk.crash.crashreport.errorHandler.CrashDetailBean;
import com.sdk.crash.proguard.Logger;
import com.sdk.crash.proguard.ThreadPool;
import com.sdk.crash.proguard.Utils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class AnrManager {
    private AtomicInteger state = new AtomicInteger(0);
    private long preTraceTime = -1L;
    private final Context context;
    private final ThreadPool threadPool;
    private final String baseDir;

    public AnrManager(Context var1, ThreadPool pool) {
        this.context = Utils.getContext(var1);
        this.baseDir = CrashReport.instance().getStrategy().getBaseDir();
        this.threadPool = pool;
    }

    private static ProcessErrorStateInfo getProcessErrorState(Context context, long tryTimes) {
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
                    ProcessErrorStateInfo stateInfo;
                    if ((stateInfo = (ProcessErrorStateInfo) iterator.next()).condition == 2) {
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

    public final boolean getAnrOnThread(Thread var1) {
        boolean isMainThread = false;
        if (var1.getName().contains("main")) {
            isMainThread = true;
        }

        if (isMainThread) {
            try {
                synchronized (this) {
                    if (this.state.get() != 0) {
                        Logger.d("get Anr OnThread return ", new Object[0]);
                        return false;
                    }
                    this.state.set(1);
                }

                long time = System.currentTimeMillis();
                Strategy strategy = CrashReport.instance().getStrategy();
                if (Math.abs(time - this.preTraceTime) < strategy.getAnrTimeInterval()) {
                    Logger.w("should not process ANR too Fre in %d", new Object[]{strategy.getAnrTimeInterval()});
                    return false;
                }

                this.preTraceTime = time;
                this.state.set(1);

                ProcessErrorStateInfo errorStateInfo;
                if ((errorStateInfo = getProcessErrorState(this.context, 10000L)) == null) {
                    Logger.d("anr handler onThreadBlock proc state is unvisiable!", new Object[0]);
                    return false;
                }

                if (errorStateInfo.pid != Process.myPid()) {
                    Logger.d("onThreadBlock not mind proc!", new Object[]{errorStateInfo.processName});
                    return false;
                }

                Map<String, String> threadStack;
                try {
                    threadStack = Utils.getThreadStack(200000, false);
                } catch (Throwable var3) {
                    return false;
                }

                Logger.v("onThreadBlock found visiable anr , start to process!", new Object[0]);
                this.foundVisiableAnr(this.context, null, errorStateInfo, System.currentTimeMillis(), threadStack);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.state.set(0);
            }
            Utils.checkOutDateTrace(baseDir);
        } else {
            Logger.d("anr handler onThreadBlock only care main thread", new Object[0]);
        }
        return true;
    }

    private boolean foundVisiableAnr(Context var1, String anrPath, ProcessErrorStateInfo stateInfo, long time, Map<String, String> threadStack) {
//        long suffixName = time;
        File dir = new File(CrashReport.instance().getStrategy().getBaseDir());
        AnrInfoBean anrInfo = new AnrInfoBean();

        anrInfo.timeSuffix = time;
        anrInfo.filePath = dir.getAbsolutePath();
        anrInfo.processName = stateInfo != null ? stateInfo.processName : "";
        anrInfo.shortMsg = stateInfo != null ? stateInfo.shortMsg : "";
        anrInfo.longMsg = stateInfo != null ? stateInfo.longMsg : "";
        anrInfo.map = threadStack;
        if (threadStack != null) {
            Iterator iterator = threadStack.keySet().iterator();
            while (iterator.hasNext()) {
                String var17;
                if ((var17 = (String) iterator.next()).startsWith("main(")) {
                    anrInfo.mainInfo = threadStack.get(var17);
                    break;
                }
            }
        }

        Logger.d("anr tm:%d\ntr:%s\nproc:%s\nsMsg:%s\n lMsg:%s\n threads:%d", new Object[]{anrInfo.timeSuffix, anrInfo.filePath, anrInfo.processName, anrInfo.shortMsg, anrInfo.longMsg, anrInfo.map == null ? 0 : anrInfo.map.size()});

        Strategy strategy = CrashReport.instance().getStrategy();
        String msg = Utils.readFromText(anrInfo.longMsg);
        CrashDetailBean bean = CrashDetailBean.getErrorInfo(context, CrashDetailBean.TYPE_ANR, time, msg);
        bean.setFilePath(anrInfo.filePath);
        Uploader.instance().dumpFile(bean,strategy.isReportAnrImmediately());
        return true;
    }

}
