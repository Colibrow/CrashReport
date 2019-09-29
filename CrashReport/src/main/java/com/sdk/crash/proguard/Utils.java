//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sdk.crash.proguard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.Strategy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

public class Utils {

    public Utils() {
    }


    public static String formatDate() {
        return formatDate(System.currentTimeMillis());
    }

    public static String formatDate(long var0) {
        try {
            return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)).format(new Date(var0));
        } catch (Exception var3) {
            return (new Date()).toString();
        }
    }

    public static String formatDate(Date var0) {
        if (var0 == null) {
            return null;
        } else {
            try {
                return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)).format(var0);
            } catch (Exception var2) {
                return (new Date()).toString();
            }
        }
    }

    public static long getRawTime() {
        try {
            return (System.currentTimeMillis() + (long) TimeZone.getDefault().getRawOffset()) / 86400000L * 86400000L - (long) TimeZone.getDefault().getRawOffset();
        } catch (Throwable var3) {
            if (!Logger.w(var3)) {
                var3.printStackTrace();
            }
            return -1L;
        }
    }

    public static String getMd5FromByte(byte[] var0) {
        if (var0 == null) {
            return "";
        } else {
            StringBuffer var1 = new StringBuffer();
            String var2 = null;

            for (int var3 = 0; var3 < var0.length; ++var3) {
                if ((var2 = Integer.toHexString(var0[var3] & 0xff)).length() == 1) {
                    var1.append("0");
                }

                var1.append(var2);
            }

            return var1.toString().toUpperCase();
        }
    }

    public static String getMd5(String msg) {
        try {
            return getMd5(msg.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMd5(byte[] var0) {
        if (var0 != null && var0.length != 0) {
            MessageDigest var1 = null;

            try {
                (var1 = MessageDigest.getInstance("MD5")).update(var0);
                return getMd5FromByte(var1.digest());
            } catch (Throwable var2) {
                if (!Logger.e(var2)) {
                    var2.printStackTrace();
                }
                return null;
            }
        } else {
            return "NULL";
        }
    }

    public static void sleep(long var0) {
        try {
            Thread.sleep(var0);
        } catch (InterruptedException var2) {
            var2.printStackTrace();
        }
    }

    public static boolean isEmpty(String var0) {
        return var0 == null || var0.trim().length() <= 0;
    }

    public static void deleteFile(String filePath) {
        if (filePath != null) {
            File var1;
            if ((var1 = new File(filePath)).isFile() && var1.exists() && var1.canWrite()) {
                var1.delete();
            }
        }
    }

    public static byte[] getByteFromLong(long var0) {
        try {
            return ("" + var0).getBytes("utf-8");
        } catch (UnsupportedEncodingException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static long getLongFromByte(byte[] var0) {
        if (var0 == null) {
            return -1L;
        } else {
            try {
                return Long.parseLong(new String(var0, "utf-8"));
            } catch (UnsupportedEncodingException var1) {
                var1.printStackTrace();
                return -1L;
            }
        }
    }

    public static Context getContext(Context ctx) {
        if (ctx == null) {
            return ctx;
        } else {
            Context var1;
            return (var1 = ctx.getApplicationContext()) == null ? ctx : var1;
        }
    }

    public static String readFromThrowable(Throwable var0) {
        if (var0 == null) {
            return "";
        } else {
            StringWriter var1 = new StringWriter();
            PrintWriter var2 = new PrintWriter(var1);
            var0.printStackTrace(var2);
            var2.flush();
            return var1.toString();
        }
    }

    public static void invokeProperty(Class<?> var0, String var1, Object var2, Object var3) {
        try {
            Field var5;
            (var5 = var0.getDeclaredField(var1)).setAccessible(true);
            var5.set((Object) null, var2);
        } catch (Exception var4) {
        }
    }

    public static Object invokeMethod(String var0, String var1, Object var2, Class<?>[] var3, Object[] var4) {
        try {
            Method var6;
            (var6 = Class.forName(var0).getDeclaredMethod(var1, var3)).setAccessible(true);
            return var6.invoke((Object) null, var4);
        } catch (Exception var5) {
            return null;
        }
    }

    public static void convertMapToParcel(Parcel var0, Map<String, String> var1) {
        if (var1 != null && var1.size() > 0) {
            int var2 = var1.size();
            ArrayList var3 = new ArrayList(var2);
            ArrayList var7 = new ArrayList(var2);
            Iterator var5 = var1.entrySet().iterator();

            while (var5.hasNext()) {
                Entry var4 = (Entry) var5.next();
                var3.add(var4.getKey());
                var7.add(var4.getValue());
            }

            Bundle var6;
            (var6 = new Bundle()).putStringArrayList("keys", var3);
            var6.putStringArrayList("values", var7);
            var0.writeBundle(var6);
        } else {
            var0.writeBundle((Bundle) null);
        }
    }

    public static Map<String, String> convertParcelToMap(Parcel var0) {
        Bundle var4;
        if ((var4 = var0.readBundle()) == null) {
            return null;
        } else {
            HashMap var1 = null;
            ArrayList var2 = var4.getStringArrayList("keys");
            ArrayList var5 = var4.getStringArrayList("values");
            if (var2 != null && var5 != null && var2.size() == var5.size()) {
                var1 = new HashMap(var2.size());

                for (int var3 = 0; var3 < var2.size(); ++var3) {
                    var1.put(var2.get(var3), var5.get(var3));
                }
            } else {
//                x.e("map parcel error!", new Object[0]);
            }
            return var1;
        }
    }

    public static byte[] convertParcelToByte(Parcelable parcel) {
        Parcel var1 = Parcel.obtain();
        parcel.writeToParcel(var1, 0);
        byte[] var2 = var1.marshall();
        var1.recycle();
        return var2;
    }

    public static Map<String, String> getThreadStack(int maxLength, boolean var1) {
        HashMap result = new HashMap(12);
        Map<Thread, StackTraceElement[]> allStacks = Thread.getAllStackTraces();
        if (allStacks == null) {
            return null;
        } else {
            Thread main = Looper.getMainLooper().getThread();
            if (!allStacks.containsKey(main)) {
                allStacks.put(main, main.getStackTrace());
            }

            StringBuilder builder = new StringBuilder();
            Iterator<Entry<Thread, StackTraceElement[]>> iterator = allStacks.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<Thread, StackTraceElement[]> entry = iterator.next();
                builder.setLength(0);

                if (entry.getValue() != null) {
                    StackTraceElement[] var5 = entry.getValue();
                    int length = var5.length;
                    for (int i = 0; i < length; i++) {
                        StackTraceElement element = var5[i];
                        if (maxLength > 0 && builder.length() >= maxLength) {
                            builder.append("\n[Stack over limit size :" + maxLength + " , has been cut!]");
                            break;
                        }
                        builder.append(element.toString()).append("\n");
                    }
                    result.put(entry.getKey().getName() + "(" + entry.getKey().getId() + ")", builder.toString());
                }
            }
        }
        return result;
    }

//    public static byte[] b(int var0, byte[] var1, byte[] var2) {
//        try {
//            PublicKey var4 = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(var2));
//            Cipher var5;
//            (var5 = Cipher.getInstance("RSA/ECB/PKCS1Padding")).init(1, var4);
//            return var5.doFinal(var1);
//        } catch (Exception var3) {
//            if (!x.b(var3)) {
//                var3.printStackTrace();
//            }
//
//            return null;
//        }
//    }

//    private static BufferedReader a(File var0) {
//        if (var0 != null && var0.exists() && var0.canRead()) {
//            try {
//                return new BufferedReader(new InputStreamReader(new FileInputStream(var0), "utf-8"));
//            } catch (Throwable var1) {
//                x.a(var1);
//                return null;
//            }
//        } else {
//            return null;
//        }
//    }

//    public static BufferedReader a(String var0, String var1) {
//        if (var0 == null) {
//            return null;
//        } else {
//            try {
//                File var3;
//                return (var3 = new File(var0, var1)).exists() && var3.canRead() ? a(var3) : null;
//            } catch (NullPointerException var2) {
//                x.a(var2);
//                return null;
//            }
//        }
//    }

    public static String dumpPhoneInfo(Context context) throws PackageManager.NameNotFoundException {
        StringBuilder builder = new StringBuilder();
        //应用的版本名称和版本号
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        builder.append("App Version: ");
//        pw.print("App Version: ");
        builder.append(pi.versionName);
        builder.append("_");
        builder.append(pi.versionCode + "\r\n");

        //android版本号
        builder.append("OS Version: ");
        builder.append(Build.VERSION.RELEASE);
        builder.append("_");
        builder.append(Build.VERSION.SDK_INT + "\r\n");

        //手机制造商
        builder.append("brand: ");
        builder.append(Build.MANUFACTURER + "\r\n");

        //手机型号
        builder.append("Model: ");
        builder.append(Build.MODEL + "\r\n");

        //cpu架构
        builder.append("CPU ABI: ");
        builder.append(Build.CPU_ABI + "\r\n");
        return builder.toString();
    }

    //检测过期的文件
    public static void checkOutDateTrace(String baseDir) {
        Strategy strategy = CrashReport.instance().getStrategy();
        int daysKeep = strategy.getDaysKeepInCache();
        if (daysKeep < 1) {
            daysKeep = 1;
        }
        if (daysKeep > Strategy.MAX_DAY_KEEP_CACHE) {
            daysKeep = Strategy.MAX_DAY_KEEP_CACHE;
        }

        long var1 = Utils.getRawTime() - daysKeep * 24 * 3600 * 1000L;

        File dir;
        if ((dir = new File(baseDir)).exists() && dir.isDirectory()) {
            try {
                File[] fileList;
                if ((fileList = dir.listFiles()) != null && fileList.length != 0) {
                    int deleteIndex = 0;
                    for (int i = 0; i < fileList.length; i++) {
                        File file = fileList[i];
                        String fileName = file.getName();

                        try {
                            if (file.lastModified() >= var1) {
                                continue;
                            }
                        } catch (Throwable var17) {
                            Logger.d("Trace file that has invalid format: " + fileName, new Object[0]);
                        }

                        if (file.delete()) {
                            ++deleteIndex;
                        }
                    }
                    Logger.d("Number of overdue trace files that has deleted: " + deleteIndex, new Object[0]);
                    return;
                }
                return;
            } catch (Throwable var18) {
                Logger.w(var18);
            }
        }
    }

    public static long getZeroTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    //获取当前进程
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
        if (list != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : list) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return context.getPackageName();
    }

    public static String readFromFile(File file) {
        StringBuilder builder = new StringBuilder();
        if (file != null && file.exists()) {
            BufferedReader reader = null;
            try {
                String s;
                reader = new BufferedReader(new FileReader(file));
                while ((s = reader.readLine()) != null) {
                    builder.append(s + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public static String readFromEncryptText(File file) {
        StringBuilder builder = new StringBuilder();
        if (file != null && file.exists()) {
            BufferedReader reader = null;
            try {
                String s;
                reader = new BufferedReader(new FileReader(file));
                while ((s = reader.readLine()) != null) {
                    builder.append(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public static String readFromText(String text) {
        StringBuilder builder = new StringBuilder();
        if (text != null && !"".equals(text)) {
            BufferedReader reader = null;

            try {
                String s;
                reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes("utf8"))));
                while ((s = reader.readLine()) != null) {
                    builder.append(s + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    /**
     * 判断网络状态是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        return info.isAvailable();
    }

    public static String encrypt(String msg) {
        try {
            return AESUtil.getInstance().encrypt(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(String msg) {
        try {
            return AESUtil.getInstance().decrypt(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
