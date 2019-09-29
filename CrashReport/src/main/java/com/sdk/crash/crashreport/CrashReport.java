package com.sdk.crash.crashreport;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Printer;

import com.sdk.crash.crashreport.common.Constants;
import com.sdk.crash.crashreport.common.CrashStore;
import com.sdk.crash.crashreport.common.Strategy;
import com.sdk.crash.crashreport.common.Uploader;
import com.sdk.crash.crashreport.common.listener.ErrorInfoUploaderListener;
import com.sdk.crash.crashreport.common.listener.LoggerInterface;
import com.sdk.crash.crashreport.errorHandler.CrashHandler;
import com.sdk.crash.crashreport.errorHandler.anr.AnrManager;
import com.sdk.crash.proguard.Logger;
import com.sdk.crash.proguard.ThreadPool;
import com.sdk.crash.proguard.Utils;

import java.io.File;

public class CrashReport {
    private Context context;
    private AnrManager anrManager;
    private Strategy strategy;
    private static Strategy DEFAULT_STRATEGY = new Strategy();
    private ErrorInfoUploaderListener uploaderListener;
    private LoggerInterface loggerListener;
    private boolean isInited = false;
    private boolean enable = true;
    private WatchingThread mCoreThread;

    private CrashReport() {
    }

    private static CrashReport _instance;

    public static CrashReport instance() {
        if (_instance == null) {
            synchronized (CrashReport.class) {
                if (_instance == null) {
                    _instance = new CrashReport();
                }
            }
        }
        return _instance;
    }

    public void initCrashReport(Context ctx, Strategy s, boolean debug) {
        String key = Utils.getMd5("init" + Build.BRAND + Build.MODEL);
        String value = System.getProperty(key);
        if (value == null || !"true".equals(value)) {
            System.setProperty(key, "true");
            isInited = true;
            context = ctx;
            strategy = s;
            Logger.setLogDebug(debug);
            CrashHandler.getInstance().init(ctx);
            Looper.getMainLooper().setMessageLogging(new LooperPrinter());
            CrashStore.instance().init();
            File dir = initWorkDir();
            Uploader.instance().uploadLogInfo(dir.getAbsolutePath());
            startWatchThread();
        }
    }

    /**
     * 是否初始化
     *
     * @return
     */
    boolean hasInited() {
        return isInited;
    }

    public void initCrashReport(Context ctx, boolean debug) {
        initCrashReport(ctx, null, debug);
    }

    public Context getContext() {
        return context;
    }

    public void setStrategy(Strategy strategy) {
        if (strategy != null) {
            this.strategy = strategy;
            initWorkDir();
        }
    }

    public CrashReport setUploaderListener(ErrorInfoUploaderListener listener) {
        this.uploaderListener = listener;
        return this;
    }

    public ErrorInfoUploaderListener getUploaderListener() {
        return uploaderListener;
    }

    public CrashReport setLoggerListener(LoggerInterface listener) {
        this.loggerListener = listener;
        return this;
    }

    /**
     * 是否可用
     *
     * @return
     */
    public boolean isEnable() {
        return enable;
    }

    public CrashReport setEnable(boolean enable) {
        this.enable = enable;
        startWatchThread();
        return this;
    }

    /**
     * 开始检测线程
     */
    private void startWatchThread() {
        if (isInited) {
            if (!isEnable() && mCoreThread != null) {
                mCoreThread.cancel();
                mCoreThread = null;
            } else {
                if (mCoreThread == null) {
                    mCoreThread = new WatchingThread("watching");
                    mCoreThread.start();
                }
            }
        } else if (mCoreThread != null) {
            mCoreThread.cancel();
            mCoreThread = null;
        }
    }

    public LoggerInterface getLoggerListener() {
        return loggerListener;
    }

    private static Strategy getDefaultStrategy() {
        return DEFAULT_STRATEGY;
    }

    public Strategy getStrategy() {
        if (strategy == null) {
            return getDefaultStrategy();
        }
        return strategy;
    }

    public Uploader getUploader() {
        return Uploader.instance();
    }

    private File initWorkDir() {
        Strategy strategy = getStrategy();
        File dir;
        if (strategy != null && strategy.getBaseDir() != null) {
            dir = new File(strategy.getBaseDir());
        } else {
            dir = new File(context.getFilesDir(), Constants.FILE_DIR);
            strategy.setBaseDir(dir.getAbsolutePath());
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private AnrManager getAnrManager() {
        if (anrManager == null && context != null) {
            anrManager = new AnrManager(context, ThreadPool.getInstance());
        }
        return anrManager;
    }

    private volatile boolean isHandleEnd = true;

    private static class LooperPrinter implements Printer {
        @Override
        public void println(String x) {
            if (x.contains("Dispatching")) {
                instance().isHandleEnd = false;
            } else {
                instance().isHandleEnd = true;
            }
        }
    }

    private static class WatchingThread extends Thread {
        boolean isSchedule = false;
        boolean isCancel = false;

        public WatchingThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (!isCancel) {
                if (!instance().isHandleEnd && !isSchedule) {
                    long start = System.currentTimeMillis();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("cost time=" + (System.currentTimeMillis() - start));
                    if (!instance().isHandleEnd && !isSchedule) {
                        ThreadPool.getInstance().schedule(anrRunnable, 3000L);
                        isSchedule = true;
                        continue;
                    }
                } else {
                    if (instance().isHandleEnd && isSchedule) {
                        ThreadPool.getInstance().cancel();
                        isSchedule = false;
                    }
                }
            }
        }

        @Override
        public synchronized void start() {
            isCancel = false;
            super.start();
        }

        public void cancel() {
            isCancel = true;
        }
    }

    private static final Runnable anrRunnable = new Runnable() {

        @Override
        public void run() {
            //handle anr message
            instance().getAnrManager().getAnrOnThread(Looper.getMainLooper().getThread());
        }
    };
}
