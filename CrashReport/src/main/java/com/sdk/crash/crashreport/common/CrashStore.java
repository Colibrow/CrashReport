package com.sdk.crash.crashreport.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.proguard.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//ms5 to number
public class CrashStore {
    public static final String CRASH_MD5_NAME = "sp_crash_md5";
    public static final String CRASH_DATA_NAME = "sp_crash_data";
    private Map<String, List<String>> md5Map = new HashMap<>();
    private static final int MD5_CACHE_DAY = 3;

    private SharedPreferences preferences;

    private static class Singleton {
        private static CrashStore _instance = new CrashStore();
    }

    public static CrashStore instance() {
        return CrashStore.Singleton._instance;
    }

    public void init() {
        initAllCrashStore();
    }

    public SharedPreferences getPreferences() {
        if (preferences == null && CrashReport.instance().getContext() != null) {
            preferences = CrashReport.instance().getContext().getSharedPreferences(CRASH_MD5_NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    //初始化全部md5信息
    private void initAllCrashStore() {
        SharedPreferences sp = getPreferences();
        if (sp != null) {
            Map<String, ?> map = sp.getAll();
            for (String key : map.keySet()) {
                if (key != null) {
                    initMd5Map(key);
                }
            }
        }
    }

    /**
     * 初始化所有md5信息
     *
     * @param md5
     */
    private void initMd5Map(String md5) {
        SharedPreferences sp = getPreferences();
        if (sp != null) {
            String data = sp.getString(md5, "");
            try {
                if (!"".equals(data) && !"{}".equals(data)) {
                    JSONObject object = new JSONObject(data);
                    JSONArray timeArray = object.optJSONArray("times");
                    List<String> times = new ArrayList<>();
                    if (timeArray != null) {
                        for (int j = 0; j < timeArray.length(); j++) {
                            JSONObject time = timeArray.getJSONObject(j);
                            times.add(time.getString("data"));
                        }
                    }
                    md5Map.put(md5, times);
                } else {
                    long lastTime = getMd5LastTime(md5);
                    //超过指定日期删除
                    if (System.currentTimeMillis() - lastTime > MD5_CACHE_DAY * 24 * 3600 * 1000l) {
                        clearMd5ToCache(md5);
                        removeMd5LastTime(md5);
                    } else if ("{}".equals(data)) {
                        md5Map.put(md5, new ArrayList<String>());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加入缓存
     *
     * @param md5
     * @param time
     */
    public void putMd5ToCache(String md5, String time) {
        List<String> times = md5Map.get(md5);
        if (times == null) {
            times = new ArrayList<>();
        }
        times.add(time);
        md5Map.put(md5, times);
        restoreMd5CrashStore(md5);
        setMd5LastTime(md5, System.currentTimeMillis());
    }

    /**
     * 移除缓存
     *
     * @param md5
     * @param list
     */
    public void removeMd5Cache(String md5, List<String> list) {
        List<String> times = md5Map.get(md5);
        if (times != null && list != null) {
            times.removeAll(list);
            restoreMd5CrashStore(md5);
        }
    }

    /**
     * 清除md5的缓存
     *
     * @param md5
     */
    public void clearMd5ToCache(String md5) {
        md5Map.remove(md5);
        SharedPreferences sp = getPreferences();
        if (sp != null) {
            sp.edit().remove(md5).commit();
        }
    }

    //获取相应md5的数量
    public int getNumberByMd5(String md5) {
        List<String> list = md5Map.get(md5);
        if (list != null) {
            return list.size();
        }
        return -1;
    }

    public List<String> getMd5List(String md5) {
        return md5Map.get(md5);
    }

    /**
     * 根据md5获取json信息
     *
     * @param md5
     * @return
     */
    public String getMd5Info(String md5) {
        synchronized (md5Map) {
            SharedPreferences sp = getPreferences();
            if (sp != null) {
                List<String> times = md5Map.get(md5);
                return getMd5Info(md5, times);
            }
        }
        return "";
    }

    public String getMd5Info(String md5, List<String> times) {
        if (times == null) {
            return "";
        }
        if (times.isEmpty()) {
            return "{}";
        }
        synchronized (md5Map) {
            SharedPreferences sp = getPreferences();
            if (sp != null) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("md5", md5);
                    JSONArray array = new JSONArray();
                    if (times != null && !times.isEmpty()) {
                        for (String info : times) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("data", info);
                            array.put(jsonObject);
                        }
                    }
                    object.put("times", array);
                    return object.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return "{}";
    }

    public void restoreMd5CrashStore(String md5) {
        synchronized (md5Map) {
            SharedPreferences sp = getPreferences();
            if (sp != null) {
                String msg = getMd5Info(md5);
                if ("".equals(msg)) {
                    sp.edit().remove(md5).commit();
                } else {
                    sp.edit().putString(md5, msg).commit();
                }
            }
        }
    }

    private SharedPreferences dataPreferences;

    public SharedPreferences getDataPreferences() {
        if (dataPreferences == null && CrashReport.instance().getContext() != null) {
            dataPreferences = CrashReport.instance().getContext().getSharedPreferences(CRASH_DATA_NAME, Context.MODE_PRIVATE);
        }
        return dataPreferences;
    }

    //设置最后一次md5的时间信息
    private void setMd5LastTime(String md5, long time) {
        SharedPreferences sp = getDataPreferences();
        if (sp != null) {
            sp.edit().putLong(md5, time).commit();
        }
    }

    /**
     * 移除md5对于时间的值
     *
     * @param md5
     */
    private void removeMd5LastTime(String md5) {
        SharedPreferences sp = getDataPreferences();
        if (sp != null) {
            sp.edit().remove(md5).commit();
        }
    }

    /**
     * 获取md5下存储时间的值
     *
     * @param md5
     * @return
     */
    private long getMd5LastTime(String md5) {
        SharedPreferences sp = getDataPreferences();
        if (sp != null) {
            return sp.getLong(md5, 0);
        }
        return System.currentTimeMillis();
    }

    /**
     * 设置最后一次crash的时间
     *
     * @param time
     */
    public void setLastCrash(long time) {
        SharedPreferences sp = getDataPreferences();
        if (sp != null) {
            sp.edit().putLong("last_error", time).commit();
        }
    }

    /**
     * 获取最后一次crash的时间
     */
    public long getLastCrash() {
        SharedPreferences sp = getDataPreferences();
        if (sp != null) {
            return sp.getLong("last_error", 0);
        }
        return System.currentTimeMillis();
    }

    /**
     * 增加上传次数
     */
    public void addDailyUploadCount() {
        synchronized (CrashStore.class) {
            SharedPreferences sp = getDataPreferences();
            if (sp != null) {
                long currentZeroTime = Utils.getZeroTime(System.currentTimeMillis());
                long lastUpload = sp.getLong("last_upload", 0);
                SharedPreferences.Editor editor = sp.edit();
                if (lastUpload == 0 || lastUpload != currentZeroTime) {
                    editor.putInt("upload_count", 1).putLong("last_upload", currentZeroTime).commit();
                } else {
                    int count = sp.getInt("upload_count", 0);
                    editor.putInt("upload_count", count + 1).commit();
                }
            }
        }
    }

    /**
     * 获取已经上传的次数
     *
     * @return
     */
    public int getDailyUploadCount() {
        synchronized (CrashStore.class) {
            SharedPreferences sp = getDataPreferences();
            if (sp != null) {
                long currentZeroTime = Utils.getZeroTime(System.currentTimeMillis());
                long lastUpload = sp.getLong("last_upload", 0);
                if (currentZeroTime != lastUpload) {
                    return 0;
                }
                return sp.getInt("upload_count", 0);
            }
            return 0;
        }
    }

    /**
     * 新增本地缓存的次数
     */
    public void addDailyCacheCount() {
        synchronized (CrashStore.class) {
            SharedPreferences sp = getDataPreferences();
            if (sp != null) {
                long currentZeroTime = Utils.getZeroTime(System.currentTimeMillis());
                long lastCache = sp.getLong("last_cache", 0);
                SharedPreferences.Editor editor = sp.edit();
                if (lastCache == 0 || lastCache != currentZeroTime) {
                    editor.putInt("cache_count", 1).putLong("last_cache", currentZeroTime).commit();
                } else {
                    int count = sp.getInt("cache_count", 0);
                    editor.putInt("cache_count", count + 1).commit();
                }
            }
        }
    }

    /**
     * 获取本地缓存的次数
     */
    public int getDailyCacheCount() {
        synchronized (CrashStore.class) {
            SharedPreferences sp = getDataPreferences();
            if (sp != null) {
                long currentZeroTime = Utils.getZeroTime(System.currentTimeMillis());
                long lastCache = sp.getLong("last_cache", 0);
                if (currentZeroTime != lastCache) {
                    return 0;
                }
                return sp.getInt("cache_count", 0);
            }
            return 0;
        }
    }
}
