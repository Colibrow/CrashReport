package com.sdk.crash.crashreport.errorHandler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.sdk.crash.proguard.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashDetailBean implements Parcelable {
    public static final String TYPE_CRASH = "0";
    public static final String TYPE_ANR = "1";

    private String errorTime;
    private String filePath;
    private String processName;
    private String threadName;
    private String errorMsg;
    private String type;
    private String appVersion;
    private String osVersion;
    private String brand;
    private String model;
    private String cpu;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(String errorTime) {
        this.errorTime = errorTime;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public CrashDetailBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.errorTime);
        dest.writeString(this.filePath);
        dest.writeString(this.processName);
        dest.writeString(this.threadName);
        dest.writeString(this.errorMsg);
        dest.writeString(this.type);
    }

    protected CrashDetailBean(Parcel in) {
        this.errorTime = in.readString();
        this.filePath = in.readString();
        this.processName = in.readString();
        this.threadName = in.readString();
        this.errorMsg = in.readString();
        this.type = in.readString();
    }

    public static final Creator<CrashDetailBean> CREATOR = new Creator<CrashDetailBean>() {
        @Override
        public CrashDetailBean createFromParcel(Parcel source) {
            return new CrashDetailBean(source);
        }

        @Override
        public CrashDetailBean[] newArray(int size) {
            return new CrashDetailBean[size];
        }
    };

    @Override
    public String toString() {
        return "CrashDetailBean{" +
                "errorTime='" + errorTime + '\'' +
                ", filePath='" + filePath + '\'' +
                ", processName='" + processName + '\'' +
                ", threadName='" + threadName + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", type='" + type + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", cpu='" + cpu + '\'' +
                '}';
    }

    public String toJson() {
        try {
            JSONObject object = new JSONObject();
            object.put("errorTime", errorTime);
//            object.put("filePath", filePath);
            object.put("processName", processName);
            object.put("threadName", threadName);
            object.put("errorMsg", errorMsg);
            object.put("type", type);
            object.put("appVersion", appVersion);
            object.put("osVersion", osVersion);
            object.put("brand", brand);
            object.put("model", model);
            object.put("cpu", cpu);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public static CrashDetailBean getErrorInfo(Context context, String type, long current, String errorMsg) {
        String processName = Utils.getCurProcessName(context);
        String threadName = Thread.currentThread().getName();
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;
        if (pm != null) {
            try {
                packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        String errorTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));

        CrashDetailBean bean = new CrashDetailBean();
        bean.setErrorMsg(errorMsg);
        bean.setErrorTime(errorTime);
        bean.setProcessName(processName);
        bean.setThreadName(threadName);
        bean.setType(type);
        if (packageInfo != null) {
            bean.setAppVersion(packageInfo.versionName + "_" + packageInfo.versionCode);
        }

        bean.setOsVersion(Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT);
        bean.setBrand(Build.MANUFACTURER);
        bean.setModel(Build.MODEL);
        bean.setCpu(Build.CPU_ABI);

        return bean;
    }
}
