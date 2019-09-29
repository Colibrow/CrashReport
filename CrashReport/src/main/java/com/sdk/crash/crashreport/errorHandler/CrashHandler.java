package com.sdk.crash.crashreport.errorHandler;

import android.content.Context;
import android.util.Log;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.CrashStore;
import com.sdk.crash.crashreport.common.Strategy;
import com.sdk.crash.proguard.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangwei on 2019/4/25.
 * ###步骤
 * 创建CrashHandler类，实现 Thread.UncaughtExceptionHandler 接口
 * 编写崩溃处理逻辑，崩溃的时候回调用uncaughtException（）方法
 * 在自定义application中注册CrashHandler
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";

    private static CrashHandler sInstance = new CrashHandler();

    //系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private Context mContext;

    //构造方法私有，防止外部构造多个实例，即采用单例模式
    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return sInstance;
    }

    //这里主要完成初始化工作
    public void init(Context context) {
        //获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);////崩溃时将catch住异常
        //获取Context，方便内部使用
        mContext = context.getApplicationContext();
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     * //崩溃时触发
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.w("TAG", "马上崩溃");
        Strategy strategy = CrashReport.instance().getStrategy();

        if (CrashReport.instance().isEnable()) {
            if (System.currentTimeMillis() - CrashStore.instance().getLastCrash() < strategy.getCrashTimeInterval()) {
                CrashStore.instance().setLastCrash(System.currentTimeMillis());
                Logger.w("should not process crash too Fre in %d", new Object[]{strategy.getCrashTimeInterval()});
                return;
            }

            //延时退出
            try {
                thread.sleep(500);
                //导出异常信息到缓存中
                dumpExceptionToCacheDir(ex);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CrashStore.instance().setLastCrash(System.currentTimeMillis());

            if (strategy.isPrintCrash()) {
                //打印出当前调用栈信息
                ex.printStackTrace();
            }
        }

        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            if (strategy.isQuitCrash()) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    private void dumpExceptionToCacheDir(Throwable ex) throws IOException {
        long current = System.currentTimeMillis();

        StringBuilder builder = new StringBuilder();
        //导出异常的调用栈信息
        builder.append(ex.toString() + "\n");
        StackTraceElement[] elements = ex.getStackTrace();
        if (elements != null) {
            for (int i = 0; i < elements.length; i++) {
                builder.append("        " + elements[i].toString() + "\n");
            }
        }

        Strategy strategy = CrashReport.instance().getStrategy();

        File dir = new File(strategy.getBaseDir());
        CrashDetailBean bean = CrashDetailBean.getErrorInfo(mContext, CrashDetailBean.TYPE_CRASH, current, builder.toString());
        bean.setFilePath(dir.getAbsolutePath());
        CrashReport.instance().getUploader().dumpFile(bean, strategy.isReportCrashImmediately());
    }

}