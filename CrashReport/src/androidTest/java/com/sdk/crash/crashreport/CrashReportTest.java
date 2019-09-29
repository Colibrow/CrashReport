package com.sdk.crash.crashreport;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import com.sdk.crash.crashreport.common.Strategy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
public class CrashReportTest {

    @Test
    public void testInit() {
        Context context = getInstrumentation().getContext();
        String baseDir = context.getFilesDir() + File.separator + "test";

        Strategy strategy = new Strategy.Builder(context)
                .setBaseDir(baseDir)
                .setCacheMaxCountPerDay(5)
                .setDailyReportLimit(5)
                .build();
        CrashReport.instance().initCrashReport(context, strategy, true);
        Assert.assertEquals(CrashReport.instance().getStrategy().getCacheMaxCountPerDay(),5);

        strategy = new Strategy.Builder(context)
                .setBaseDir(baseDir)
                .setCacheMaxCountPerDay(3)
                .setDailyReportLimit(3)
                .build();
        CrashReport.instance().initCrashReport(context, strategy, true);
        //只会初始化一次

        Assert.assertEquals(CrashReport.instance().getStrategy().getCacheMaxCountPerDay(),5);
    }
}
