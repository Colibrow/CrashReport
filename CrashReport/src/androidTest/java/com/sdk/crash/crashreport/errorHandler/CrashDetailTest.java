package com.sdk.crash.crashreport.errorHandler;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.Constants;
import com.sdk.crash.crashreport.common.CrashStore;
import com.sdk.crash.crashreport.common.Strategy;
import com.sdk.crash.crashreport.common.Uploader;
import com.sdk.crash.proguard.Utils;
import com.sdk.crash.proguard.UtilsTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
public class CrashDetailTest {

    @Before
    public void setUp() throws Exception {
        Context context = getInstrumentation().getContext();
        Strategy strategy = new Strategy.Builder(context)
                .setBaseDir(context.getFilesDir() + File.separator + "test")
                .setCacheMaxCountPerDay(5)
                .setDailyReportLimit(5)
                .build();
        CrashReport.instance().initCrashReport(context, strategy, true);
        Log.e("CrashDetailTest","setUp");
    }

    @Test
    public void createErrorInfo() {
        Context context = getInstrumentation().getContext();
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

        Assert.assertTrue(bean != null);

        if (bean != null) {
            final String errorMd5 = Utils.getMd5(bean.getErrorMsg());
            final int md5Count = CrashStore.instance().getNumberByMd5(errorMd5);
            final String errorJson = bean.toJson();

            bean.setFilePath(CrashReport.instance().getStrategy().getBaseDir());
            File file = new File(bean.getFilePath(), Constants.ERROR_PREFIX + errorMd5 + Constants.FILE_SUFFIX);

            System.out.println("before dump crash file is exist=" + file.exists());

            new Uploader().dumpFile(bean, false);

            Assert.assertTrue(file.exists());

//            file.delete();
        }
    }

    @Test
    public void scanToUploader() {
        Strategy strategy = CrashReport.instance().getStrategy();
        String dir = strategy.getBaseDir();
        File file = new File(dir);
        File[] list = file.listFiles();
        for (File f : list) {

        }
    }

}
