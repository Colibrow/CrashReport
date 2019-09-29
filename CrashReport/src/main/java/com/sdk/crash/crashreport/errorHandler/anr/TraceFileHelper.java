//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sdk.crash.crashreport.errorHandler.anr;

import com.sdk.crash.proguard.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TraceFileHelper {
    public TraceFileHelper() {
    }

    public static TraceFileHelper.TraceInfo readTargetDumpInfo(final String var0, String var1, final boolean var2) {
        if (var0 != null && var1 != null) {
            final TraceFileHelper.TraceInfo var3 = new TraceFileHelper.TraceInfo();
            readTraceFile(var1, new TraceFileHelper.b() {
                public final boolean a(String var1, int var2x, String var3x, String var4) {
                    Logger.d("new thread %s", new Object[]{var1});
                    if (var3.a > 0L && var3.c > 0L && var3.b != null) {
                        if (var3.d == null) {
                            var3.d = new HashMap();
                        }

                        var3.d.put(var1, new String[]{var3x, var4, "" + var2x});
                        return true;
                    } else {
                        return true;
                    }
                }

                public final boolean a(long var1, long var3x, String var5) {
                    Logger.d("new process %s", new Object[]{var5});
                    if (!var5.equals(var0)) {
                        return true;
                    } else {
                        var3.a = var1;
                        var3.c = var3x;
                        return var2;
                    }
                }

                public final boolean a(long var1) {
                    Logger.d("process end %d", new Object[]{var1});
                    return var3.a <= 0L || var3.c <= 0L || var3.b == null;
                }
            });
            return var3.a > 0L && var3.c > 0L && var3.b != null ? var3 : null;
        } else {
            return null;
        }
    }

    public static TraceFileHelper.TraceInfo readFirstDumpInfo(String var0, final boolean var1) {
        if (var0 == null) {
            Logger.e("path:%s", new Object[]{var0});
            return null;
        } else {
            final TraceFileHelper.TraceInfo traceInfo = new TraceFileHelper.TraceInfo();
            readTraceFile(var0, new TraceFileHelper.b() {
                public final boolean a(String var1x, int var2x, String var3, String var4) {
                    Logger.d("new thread %s", new Object[]{var1x});
                    if (traceInfo.d == null) {
                        traceInfo.d = new HashMap();
                    }

                    traceInfo.d.put(var1x, new String[]{var3, var4, "" + var2x});
                    return true;
                }

                public final boolean a(long var1x, long var3, String var5) {
                    Logger.d("new process %s", new Object[]{var5});
                    traceInfo.a = var1x;
                    traceInfo.b = var5;
                    traceInfo.c = var3;
                    return var1;
                }

                public final boolean a(long var1x) {
                    Logger.d("process end %d", new Object[]{var1x});
                    return false;
                }
            });
            if (traceInfo.a > 0L && traceInfo.c > 0L && traceInfo.b != null) {
                return traceInfo;
            } else {
                Logger.e("first dump error %s", new Object[]{traceInfo.a + " " + traceInfo.c + " " + traceInfo.b});
                return null;
            }
        }
    }

    public static void readTraceFile(String param0, TraceFileHelper.b param1) {
        // $FF: Couldn't be decompiled
    }

    private static Object[] a(BufferedReader var0, Pattern... var1) throws IOException {
        if (var0 != null && var1 != null) {
            String var2 = null;

            while ((var2 = var0.readLine()) != null) {
                Pattern[] var3 = var1;
                int var4 = var1.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    Pattern var6;
                    if ((var6 = var3[var5]).matcher(var2).matches()) {
                        return new Object[]{var6, var2};
                    }
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private static String a(BufferedReader var0) throws IOException {
        StringBuffer var1 = new StringBuffer();
        String var2 = null;

        for (int var3 = 0; var3 < 3; ++var3) {
            if ((var2 = var0.readLine()) == null) {
                return null;
            }

            var1.append(var2 + "\n");
        }

        return var1.toString();
    }

    private static String b(BufferedReader var0) throws IOException {
        StringBuffer var1 = new StringBuffer();
        String var2 = null;

        while ((var2 = var0.readLine()) != null && var2.trim().length() > 0) {
            var1.append(var2 + "\n");
        }

        return var1.toString();
    }

    public static class TraceInfo {
        public long a;
        public String b;
        public long c;
        public Map<String, String[]> d;

        public TraceInfo() {
        }
    }

    public interface b {
        boolean a(long var1, long var3, String var5);

        boolean a(long var1);

        boolean a(String var1, int var2, String var3, String var4);
    }
}
