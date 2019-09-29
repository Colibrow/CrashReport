package com.sdk.crash.crashreport.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.test.runner.AndroidJUnit4;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.errorHandler.CrashDetailBean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
public class UploaderTest {
    Strategy mStrategy;

    @Before
    public void setUp() {
        Context context = getInstrumentation().getContext();
        mStrategy = new Strategy.Builder(context)
                .setBaseDir(context.getFilesDir() + File.separator + "test")
                .setCacheMaxCountPerDay(2)
                .setDailyReportLimit(2)
                .build();
        CrashReport.instance().initCrashReport(context, mStrategy, true);
    }

    @Test
    public void checkReporterLimit() {
        Context context = getInstrumentation().getContext();

        SharedPreferences sp = context.getSharedPreferences("sp_crash_data", Context.MODE_PRIVATE);
        sp.edit().putInt("upload_count", 0).putLong("last_upload", 0).commit();

        CrashStore.instance().addDailyUploadCount();
        Assert.assertEquals(CrashStore.instance().getDailyUploadCount(), 1);

        CrashStore.instance().addDailyUploadCount();

        boolean limit = new Uploader().checkLimit();
        Assert.assertTrue(limit);
    }

    @Test
    public void checkCacheLimit() {
        File file = new File(mStrategy.getBaseDir());
        for (File f : file.listFiles()) {
            f.delete();
        }
        Context context = getInstrumentation().getContext();

        SharedPreferences sp = context.getSharedPreferences("sp_crash_data", Context.MODE_PRIVATE);
        sp.edit().putInt("cache_count", 0).putLong("last_cache", 0).commit();

        Uploader uploader = new Uploader();

        CrashDetailBean bean;
        try {
            throw new RuntimeException("hhh this is a test");
        } catch (Exception e) {
            StringBuilder builder = new StringBuilder();
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement traceElement : trace) {
                builder.append("\tat " + traceElement);
            }
            bean = CrashDetailBean.getErrorInfo(context, "0", System.currentTimeMillis(), builder.toString());
        }

        uploader.dumpFile(bean, false);
        String[] list = file.list();
        Assert.assertTrue(list.length == 1);
        Assert.assertTrue(CrashStore.instance().getDailyCacheCount() == 1);

        try {
            throw new RuntimeException("hhh this is a test22222");
        } catch (Exception e) {
            StringBuilder builder = new StringBuilder();
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement traceElement : trace) {
                builder.append("\tat " + traceElement);
            }
            bean = CrashDetailBean.getErrorInfo(context, "0", System.currentTimeMillis(), builder.toString());
        }

        uploader.dumpFile(bean, false);
        list = file.list();
        Assert.assertTrue(list.length == 2);
        Assert.assertTrue(CrashStore.instance().getDailyCacheCount() == 2);

        try {
            throw new RuntimeException("hhh this is a test33333333");
        } catch (Exception e) {
            StringBuilder builder = new StringBuilder();
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement traceElement : trace) {
                builder.append("\tat " + traceElement);
            }
            bean = CrashDetailBean.getErrorInfo(context, "0", System.currentTimeMillis(), builder.toString());
        }

        uploader.dumpFile(bean, false);
        list = file.list();
        Assert.assertTrue(list.length == 2);
        Assert.assertTrue(CrashStore.instance().getDailyCacheCount() == 2);
    }
}
