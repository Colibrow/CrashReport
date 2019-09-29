package com.sdk.crash.proguard;

import android.content.Context;
import android.os.Environment;
import android.support.test.runner.AndroidJUnit4;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.Strategy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
public class UtilsTest {
    Strategy mStrategy;

    @Before
    public void setUp() {
        Context context = getInstrumentation().getContext();
        mStrategy = new Strategy.Builder(context)
                .setBaseDir(context.getFilesDir() + File.separator + "test")
                .setCacheMaxCountPerDay(5)
                .setDailyReportLimit(5)
                .setDaysKeepInCache(3)
                .build();
        CrashReport.instance().initCrashReport(context, mStrategy, true);
    }

    @Test
    public void testOutDateFile() {
        File dir = new File(mStrategy.getBaseDir());
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            f.delete();
        }
        File file1 = new File(dir, "test1.err");
        File file2 = new File(dir, "test2.err");
        try {
            file1.createNewFile();
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.checkOutDateTrace(dir.getAbsolutePath());
        String[] list = dir.list();
        Assert.assertTrue(list.length == 2);

        int days = mStrategy.getDaysKeepInCache();
        long lastDaysByTime = System.currentTimeMillis() - ((days + 1) * 24 * 3600l * 1000);
        file1.setLastModified(lastDaysByTime);
        file2.setLastModified(lastDaysByTime);

        Utils.checkOutDateTrace(dir.getAbsolutePath());
        list = dir.list();
        Assert.assertTrue(list == null || list.length == 0);
    }
}
