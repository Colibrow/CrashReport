package com.sdk.crash.proguard;

import android.util.Log;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.listener.LoggerInterface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class Logger {
    private static String CRASH_REPORT_INFO = "CrashReportInfo";
    public static String CRASH_REPORT = "CrashReport";
    private static boolean isDebug = false;

    public static LoggerInterface getLogManager() {
        return CrashReport.instance().getLoggerListener();
    }

    public static void setLogDebug(boolean debug) {
        isDebug = debug;
    }

    private static boolean log(int type, String var1, Object... args) {
        if (!isDebug) {
            return false;
        } else {
            var1 = var1 == null ? "null" : (args != null && args.length != 0 ? String.format(Locale.US, var1, args) : var1);
            switch (type) {
                case 0:
                    if (getLogManager() != null) {
                        getLogManager().verbose(var1);
                    } else {
                        Log.v(CRASH_REPORT, var1);
                    }
                    return true;
                case 1:
                    if (getLogManager() != null) {
                        getLogManager().debug(var1);
                    } else {
                        Log.d(CRASH_REPORT, var1);
                    }
                    return true;
                case 2:
                    if (getLogManager() != null) {
                        getLogManager().warn(var1);
                    } else {
                        Log.w(CRASH_REPORT, var1);
                    }
                    return true;
                case 3:
                    if (getLogManager() != null) {
                        getLogManager().error(var1);
                    } else {
                        Log.e(CRASH_REPORT, var1);
                    }
                    return true;
                case 4:
                default:
                    return false;
                case 5:
                    if (getLogManager() != null) {
                        getLogManager().info(var1);
                    } else {
                        Log.i(CRASH_REPORT_INFO, var1);
                    }
                    return true;
            }
        }
    }

    public static boolean v(String var0, Object... var1) {
        return log(0, var0, var1);
    }

    public static boolean v(Class var0, String var1, Object... var2) {
        String var3 = String.format(Locale.US, "[%s] %s", var0.getSimpleName(), var1);
        return log(0, var3, var2);
    }

    public static boolean info(String var0, Object... var1) {
        return log(5, var0, var1);
    }

    public static boolean d(String var0, Object... var1) {
        return log(1, var0, var1);
    }

    public static boolean d(Class var0, String var1, Object... var2) {
        String var3 = String.format(Locale.US, "[%s] %s", var0.getSimpleName(), var1);
        return log(1, var3, var2);
    }

    public static boolean w(String var0, Object... var1) {
        return log(2, var0, var1);
    }

    public static boolean w(Throwable var0) {
        Throwable var1 = var0;
        byte var2 = 2;
        String var3 = getThrowable(var1);
        return log(var2, var3);
    }

    public static boolean e(String var0, Object... var1) {
        return log(3, var0, var1);
    }

    public static boolean e(Throwable var0) {
        Throwable var1 = var0;
        byte var2 = 3;
        String var3 = getThrowable(var1);
        return log(var2, var3);
    }

    public static String getThrowable(Throwable var0) {
        if (var0 == null) {
            return "";
        } else {
            try {
                StringWriter var1 = new StringWriter();
                var0.printStackTrace(new PrintWriter(var1));
                return var1.getBuffer().toString();
            } catch (Throwable var2) {
                if (!w(var2)) {
                    var2.printStackTrace();
                }

                return "fail";
            }
        }
    }
}
