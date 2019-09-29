package com.sdk.crash.crashreport.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.test.runner.AndroidJUnit4;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.proguard.Utils;
import com.sdk.crash.proguard.UtilsTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
public class CrashStoreTest {
    Strategy mStrategy;

    @Before
    public void setUp() {
        Context context = getInstrumentation().getContext();
        mStrategy = new Strategy.Builder(context)
                .setBaseDir(context.getFilesDir() + File.separator + "test")
                .setCacheMaxCountPerDay(5)
                .setDailyReportLimit(5)
                .build();
        CrashReport.instance().initCrashReport(context, mStrategy, true);
    }

    @Test
    public void getMaxCacheCount() {
        int count = CrashStore.instance().getDailyCacheCount();
        System.out.println("cache.before =" + count);
        CrashStore.instance().addDailyCacheCount();
        int after = CrashStore.instance().getDailyCacheCount();
        Assert.assertEquals(count + 1, after);
    }

    @Test
    public void getMaxDailyReporterCount() {
        int count = CrashStore.instance().getDailyUploadCount();
        System.out.println("upload.before =" + count);
        CrashStore.instance().addDailyUploadCount();
        int after = CrashStore.instance().getDailyUploadCount();
        Assert.assertEquals(count + 1, after);
    }

    @Test
    public void getMd5CacheTest() {
        String md5 = Utils.getMd5("12345678");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time = format.format(new Date());
        CrashStore.instance().putMd5ToCache(md5, time);

        List<String> list = CrashStore.instance().getMd5List(md5);

        Assert.assertTrue(list.contains(time));

        CrashStore.instance().removeMd5Cache(md5, list);
        String md5Info = CrashStore.instance().getMd5Info(md5);
        Assert.assertTrue("{}".endsWith(md5Info));

        CrashStore.instance().clearMd5ToCache(md5);
        Assert.assertTrue("".endsWith(CrashStore.instance().getMd5Info(md5)));

        CrashStore.instance().init();
        md5Info = CrashStore.instance().getMd5Info(md5);
        Assert.assertTrue("{}".endsWith(md5Info));

        CrashStore.instance().clearMd5ToCache(md5);
        Assert.assertTrue("".endsWith(CrashStore.instance().getMd5Info(md5)));
    }

    @Test
    public void checkThroughDayStore() {
        Context context = getInstrumentation().getContext();
        SharedPreferences sp = context.getSharedPreferences("sp_crash_data", Context.MODE_PRIVATE);
        sp.edit().putInt("cache_count", 5).putLong("last_cache", Utils.getZeroTime(System.currentTimeMillis() - 24 * 3600 * 1000)).commit();
        sp.edit().putInt("upload_count", 5).putLong("last_upload", Utils.getZeroTime(System.currentTimeMillis() - 24 * 3600 * 1000)).commit();

        CrashStore.instance().addDailyCacheCount();
        Assert.assertEquals(CrashStore.instance().getDailyCacheCount(),1);
        CrashStore.instance().addDailyCacheCount();
        CrashStore.instance().addDailyCacheCount();
        Assert.assertEquals(CrashStore.instance().getDailyCacheCount(),3);

        CrashStore.instance().addDailyUploadCount();
        Assert.assertEquals(CrashStore.instance().getDailyUploadCount(),1);
        CrashStore.instance().addDailyUploadCount();
        Assert.assertEquals(CrashStore.instance().getDailyUploadCount(),2);
    }

}
