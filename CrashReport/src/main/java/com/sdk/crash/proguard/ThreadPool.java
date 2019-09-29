//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sdk.crash.proguard;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPool {
    private static final AtomicInteger a = new AtomicInteger(1);
    private static ThreadPool _instance;
    private ScheduledExecutorService service = null;
    private ScheduledFuture future;

    protected ThreadPool() {
        ThreadFactory var1 = new ThreadFactory() {
            public final Thread newThread(Runnable var1) {
                Thread var2;
                (var2 = new Thread(var1)).setName("BuglyThread-" + ThreadPool.a.getAndIncrement());
                return var2;
            }
        };
        this.service = Executors.newSingleThreadScheduledExecutor(var1);
        if (this.service == null || this.service.isShutdown()) {
            Logger.w("[AsyncTaskHandler] ScheduledExecutorService is not valiable!", new Object[0]);
        }
    }

    public static synchronized ThreadPool getInstance() {
        if (_instance == null) {
            _instance = new ThreadPool();
        }
        return _instance;
    }

    public final synchronized boolean schedule(Runnable runnable, long delay) {
        if (!this.isRunning()) {
            Logger.w("[AsyncTaskHandler] Async handler was closed, should not post task.", new Object[0]);
            return false;
        } else if (runnable == null) {
            Logger.w("[AsyncTaskHandler] Task input is null.", new Object[0]);
            return false;
        } else {
            delay = delay > 0L ? delay : 0L;
            Logger.d("[AsyncTaskHandler] Post a delay(time: %dms) task: %s", new Object[]{delay, runnable.getClass().getName()});

            try {
                future = this.service.schedule(runnable, delay, TimeUnit.MILLISECONDS);
                return true;
            } catch (Throwable var4) {
                return false;
            }
        }
    }

    public final synchronized boolean schedule(Runnable runnable) {
        if (!this.isRunning()) {
            Logger.w("[AsyncTaskHandler] Async handler was closed, should not post task.", new Object[0]);
            return false;
        } else if (runnable == null) {
            Logger.w("[AsyncTaskHandler] Task input is null.", new Object[0]);
            return false;
        } else {
            Logger.d("[AsyncTaskHandler] Post a normal task: %s", new Object[]{runnable.getClass().getName()});

            try {
                this.service.execute(runnable);
                return true;
            } catch (Throwable var2) {
                var2.getStackTrace();
                return false;
            }
        }
    }

    public final synchronized void shutdown() {
        if (this.service != null && !this.service.isShutdown()) {
            Logger.d("[AsyncTaskHandler] Close async handler.", new Object[0]);
            this.service.shutdownNow();
        }
    }

    public final synchronized void cancel() {
        if (future != null && !future.isCancelled()) {
            Logger.d("[AsyncTaskHandler] cancel current async handler.", new Object[0]);
            future.cancel(true);
            future = null;
        }
    }

    public final synchronized boolean isRunning() {
        return this.service != null && !this.service.isShutdown();
    }

    private static ExecutorService single = Executors.newSingleThreadExecutor();
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static ExecutorService ioExecutorService = new ThreadPoolExecutor(2 * CPU_COUNT + 1,
            2 * CPU_COUNT + 1,
            30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(128));

    public static void submitSingleTask(Runnable runnable) {
        single.execute(runnable);
    }

    public static void submitIoTask(Runnable runnable) {
        ioExecutorService.execute(runnable);
    }

    private static Executor UiExecutor = new MainThreadExecutor();

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }

    public static void submitUiThread(Runnable runnable) {
        UiExecutor.execute(runnable);
    }
}
