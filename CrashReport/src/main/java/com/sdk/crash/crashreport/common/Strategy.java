package com.sdk.crash.crashreport.common;

import android.content.Context;

import java.io.File;

public class Strategy {
    public static int MAX_DAY_KEEP_CACHE = 7;
    /**
     * 是否在crash时打印错误日志
     */
    private boolean isPrintCrash = true;
    /**
     * 是否退出crash
     */
    private boolean isQuitCrash = true;
    /**
     * 保持最近几天的日志
     */
    private int daysKeepInCache = 7;
    /**
     * 是否保持到缓存
     */
    private boolean dumpToCache = true;
    /**
     * crash的处理时间间隔
     */
    private long crashTimeInterval = 5000L;
    /**
     * anr的处理时间间隔
     */
    private long anrTimeInterval = 10000L;

    /**
     * 立即上报crash
     */
    private boolean reportCrashImmediately = false;

    /**
     * 立即上报anr
     */
    private boolean reportAnrImmediately = false;

    /**
     * 目录
     */
    private String baseDir;

    /**
     * 每天上报限制
     */
    private long dailyReportLimit = 20;

    /**
     * 是否加密上传
     */
    private boolean encrypyEnable;

    /**
     * 每天缓存文件数
     */
    private int cacheMaxCountPerDay = 10;

    public void setBaseDir(String path) {
        this.baseDir = path;
    }

    public static class Builder {
        boolean isPrintCrash = true;
        boolean isQuitCrash = true;
        int daysKeepInCache = 7;
        boolean dumpToCache = true;
        long crashInterval = 5000L;
        long anrInterval = 10000L;
        boolean reportCrashImmediately = false;
        boolean reportAnrImmediately = false;
        String baseDir;
        long dailyReportLimit = 20;
        boolean encrypyEnable = false;
        int cacheMaxCountPerDay = 10;

        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("context can not be null");
            }
            baseDir = new File(context.getFilesDir(), Constants.FILE_DIR).getAbsolutePath();
        }

        public Builder setReportCrashImmediately(boolean report) {
            this.reportCrashImmediately = report;
            return this;
        }

        public Builder setReportAnrImmediately(boolean report) {
            this.reportAnrImmediately = report;
            return this;
        }

        public Builder setPrintCrash(boolean b) {
            this.isPrintCrash = b;
            return this;
        }

        public Builder setQuitCrash(boolean b) {
            this.isQuitCrash = b;
            return this;
        }

        public Builder setDaysKeepInCache(int days) {
            this.daysKeepInCache = days;
            return this;
        }

        public Builder setEnableDumpToCache(boolean b) {
            this.dumpToCache = b;
            return this;
        }

        public Builder setCrashInterval(long crashInterval) {
            this.crashInterval = crashInterval;
            return this;
        }

        public Builder setAnrInterval(long anrInterval) {
            this.anrInterval = anrInterval;
            return this;
        }

        public Builder setBaseDir(String dir) {
            this.baseDir = dir;
            return this;
        }

        public Builder setDailyReportLimit(long limit) {
            this.dailyReportLimit = limit;
            return this;
        }

        public Builder setEncrypyEnable(boolean enable) {
            this.encrypyEnable = enable;
            return this;
        }

        public Builder setCacheMaxCountPerDay(int count) {
            this.cacheMaxCountPerDay = count;
            return this;
        }

        public Strategy build() {
            Strategy strategy = new Strategy();
            strategy.isPrintCrash = isPrintCrash;
            strategy.isQuitCrash = isQuitCrash;
            strategy.daysKeepInCache = daysKeepInCache;
            strategy.dumpToCache = dumpToCache;
            strategy.crashTimeInterval = crashInterval;
            strategy.anrTimeInterval = anrInterval;
            strategy.baseDir = baseDir;
            strategy.reportAnrImmediately = reportAnrImmediately;
            strategy.reportCrashImmediately = reportCrashImmediately;
            strategy.dailyReportLimit = dailyReportLimit;
            strategy.encrypyEnable = encrypyEnable;
            strategy.cacheMaxCountPerDay = cacheMaxCountPerDay;
            return strategy;
        }
    }

    public boolean isPrintCrash() {
        return isPrintCrash;
    }

    public boolean isQuitCrash() {
        return isQuitCrash;
    }

    public int getDaysKeepInCache() {
        return daysKeepInCache;
    }

    public boolean isDumpToCache() {
        return dumpToCache;
    }

    public long getCrashTimeInterval() {
        return crashTimeInterval;
    }

    public long getAnrTimeInterval() {
        return anrTimeInterval;
    }

    public boolean isReportCrashImmediately() {
        return reportCrashImmediately;
    }

    public boolean isReportAnrImmediately() {
        return reportAnrImmediately;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public long getDailyReportLimit() {
        return dailyReportLimit;
    }

    public boolean isEncrypyEnable() {
        return encrypyEnable;
    }

    public int getCacheMaxCountPerDay() {
        return cacheMaxCountPerDay;
    }
}
