package com.sdk.crash.crashreport.common;

import com.sdk.crash.crashreport.CrashReport;
import com.sdk.crash.crashreport.common.listener.ErrorInfoUploaderListener;
import com.sdk.crash.crashreport.common.listener.ErrorResultListener;
import com.sdk.crash.crashreport.errorHandler.CrashDetailBean;
import com.sdk.crash.proguard.Logger;
import com.sdk.crash.proguard.ThreadPool;
import com.sdk.crash.proguard.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

public class Uploader {

    int mCacheCountDay = 0;

    private static class Singleton {
        static Uploader instance = new Uploader();
    }

    public static Uploader instance() {
        return Singleton.instance;
    }

    private ErrorInfoUploaderListener getUploadListener() {
        return CrashReport.instance().getUploaderListener();
    }

    /**
     * 上报错误列表
     *
     * @param list
     */
    public void uploadListCrashInfo(final List<CrashDetailBean> list) {
        ThreadPool.submitIoTask(new Runnable() {
            @Override
            public void run() {
                if (checkLimit()) {
                    return;
                }
                for (CrashDetailBean bean : list) {
                    if (bean.getFilePath() != null) {
                        uploadFileInfo(new File(bean.getFilePath()));
                    } else {
                        final String errorMd5 = Utils.getMd5(bean.getErrorMsg());
                        final List<String> times = CrashStore.instance().getMd5List(errorMd5);
                        final String errorMd5Info = CrashStore.instance().getMd5Info(errorMd5, times);
                        final String errorJson = bean.toJson();
                        ThreadPool.submitUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getUploadListener() != null) {
                                    getUploadListener().upload(errorJson, errorMd5Info, new ErrorMd5ResultCall(null, times, errorMd5));
                                }
                            }
                        });
                    }
                    CrashStore.instance().addDailyUploadCount();
                }
            }
        });
    }

    /**
     * 是否超过上报限制
     *
     * @return
     */
    boolean checkLimit() {
        long startTime = System.currentTimeMillis();
        if (Utils.isNetAvailable(CrashReport.instance().getContext())) {
            if (CrashStore.instance().getDailyUploadCount() >= CrashReport.instance().getStrategy().getDailyReportLimit()) {
                Logger.e("upload exceed limit", new Object[0]);
                return true;
            }
            return false;
        } else {
            System.out.println("cost time=" + (System.currentTimeMillis() - startTime));
            return true;
        }
    }

    /**
     * 上传数据
     */
    public void uploadLogInfo(final String dir) {
        Logger.d("scan dir error info = " + dir);
        ThreadPool.submitIoTask(new Runnable() {
            @Override
            public void run() {
                Utils.checkOutDateTrace(dir);
                File dirFile = new File(dir);
                File[] list = dirFile.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (name.startsWith(Constants.ERROR_PREFIX) && name.endsWith(Constants.FILE_SUFFIX)) {
                            return true;
                        }
                        return false;
                    }
                });

                if (list != null) {
                    for (File f : list) {
                        uploadFileInfo(f);
                    }
                } else {
                    Logger.e("upload file list is null");
                }
            }
        });
    }

    /**
     * 上传文件
     *
     * @param f
     */
    private void uploadFileInfo(final File f) {
        final String text = Utils.readFromEncryptText(f);
        if (text != null && (text.equals("\n") || text.equals(""))) {
            final String errorMd5 = f.getName().replace(Constants.ERROR_PREFIX, "").replace(Constants.FILE_SUFFIX, "");
            final List<String> times = CrashStore.instance().getMd5List(errorMd5);
            final String errorMd5Info = CrashStore.instance().getMd5Info(errorMd5, times);
            ThreadPool.submitUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getUploadListener() != null) {
                        getUploadListener().upload("", errorMd5Info, new ErrorMd5ResultCall(f, times, errorMd5));
                    }
                }
            });
            if (f != null && f.exists()) {
                f.delete();
            }
        } else {
            try {
                final String decrypt = Utils.decrypt(text);
                Logger.d("upload file error info content = " + decrypt);
                final JSONObject object = new JSONObject(decrypt);
                String errorMsg = object.optString("errorMsg");
                if (errorMsg == null || "".endsWith(errorMsg)) {
                    return;
                }
                final String errorMd5 = Utils.getMd5(errorMsg);
                final List<String> times = CrashStore.instance().getMd5List(errorMd5);
                final String errorMd5Info = CrashStore.instance().getMd5Info(errorMd5, times);
                ThreadPool.submitUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Strategy strategy = CrashReport.instance().getStrategy();
                        if (getUploadListener() != null) {
                            getUploadListener().upload(strategy.isEncrypyEnable() ? text : decrypt, errorMd5Info, new ErrorMd5ResultCall(f, times, errorMd5));
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 上传数据
     */
    public void uploadListFileInfo(final List<File> files) {
        ThreadPool.submitIoTask(new Runnable() {
            @Override
            public void run() {
                for (File f : files) {
                    uploadFileInfo(f);
                }
            }
        });
    }

    //上传错误信息json
    public void uploadWithJson(final String errorJson, final String md5Info) {
        ThreadPool.submitIoTask(new Runnable() {
            @Override
            public void run() {
                if (checkLimit()) {
                    return;
                }
                Logger.d("uploadWithJson info content = " + errorJson + "_" + md5Info);
                ThreadPool.submitUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getUploadListener() != null) {
                            getUploadListener().upload(errorJson, md5Info, new ErrorMd5WithNoFileCall(md5Info));
                        }
                    }
                });
                CrashStore.instance().addDailyUploadCount();
            }
        });
    }

    public void dumpFile(final CrashDetailBean bean, boolean reportImmediately) {
        Strategy strategy = CrashReport.instance().getStrategy();
        final String errorMd5 = Utils.getMd5(bean.getErrorMsg());
        final int md5Count = CrashStore.instance().getNumberByMd5(errorMd5);
        final String errorJson = bean.toJson();
        if (bean.getFilePath() == null) {
            bean.setFilePath(strategy.getBaseDir());
        }
        File file = new File(bean.getFilePath(), Constants.ERROR_PREFIX + errorMd5 + Constants.FILE_SUFFIX);

        if (mCacheCountDay == 0) {
            mCacheCountDay = CrashStore.instance().getDailyCacheCount();
        }

        if (strategy.isDumpToCache() && !file.exists()) {
            FileLock lock = null;
            try {
                if (mCacheCountDay < strategy.getCacheMaxCountPerDay()) {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    if (md5Count == -1) {
                        file.setReadable(true);
                        FileOutputStream outputStream = new FileOutputStream(file);
                        FileChannel channel = outputStream.getChannel();
                        lock = channel.tryLock();
                        if (lock != null) {
                            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));
                            pw.println(Utils.encrypt(bean.toJson())); //加密
                            pw.flush();
                            pw.close();
                        }
                    }
                    mCacheCountDay++;
                    CrashStore.instance().addDailyCacheCount();
                }
            } catch (Exception e) {
                Logger.e(e);
            } finally {
                try {
                    if (lock != null) {
                        lock.release();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.checkOutDateTrace(file.getParentFile().getAbsolutePath());
        }
        CrashStore.instance().putMd5ToCache(errorMd5, bean.getErrorTime());

        if (reportImmediately) {
            if (file != null && file.exists()) {
                uploadFileInfo(file);
            } else {
                uploadWithJson(errorJson, errorMd5);
            }
        }
    }

    private static class ErrorMd5WithNoFileCall implements ErrorResultListener {

        private String errorMd5;

        public ErrorMd5WithNoFileCall(String md5) {
            this.errorMd5 = md5;
        }

        @Override
        public void onSuccess() {
            if (errorMd5 != null) {
                CrashStore.instance().clearMd5ToCache(errorMd5);
            }
        }

        @Override
        public void onFail() {
        }
    }

    private static class ErrorResultWithBeanCall implements ErrorResultListener {

        private CrashDetailBean crashBean;
        private String errorMd5;

        public ErrorResultWithBeanCall(CrashDetailBean bean, String md5) {
            this.crashBean = bean;
            this.errorMd5 = md5;
        }

        @Override
        public void onSuccess() {
            if (crashBean != null && crashBean.getFilePath() != null) {
                File file = new File(crashBean.getFilePath());
                if (file != null && file.exists()) {
                    file.delete();
                }
            }

            if (errorMd5 != null) {
                CrashStore.instance().clearMd5ToCache(errorMd5);
            }
        }

        @Override
        public void onFail() {
        }
    }

    private static class ErrorFileResultCall implements ErrorResultListener {

        private File crashFile;
        private String errorMd5;

        public ErrorFileResultCall(File f, String md5) {
            this.crashFile = f;
            this.errorMd5 = md5;
        }

        @Override
        public void onSuccess() {
            if (crashFile != null) {
                File file = crashFile;
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
            if (errorMd5 != null) {
                CrashStore.instance().clearMd5ToCache(errorMd5);
            }
        }

        @Override
        public void onFail() {
        }
    }

    private static class ErrorMd5ResultCall implements ErrorResultListener {

        private File crashFile;
        private List<String> list;
        private String errorMd5;

        public ErrorMd5ResultCall(File f, List<String> times, String md5) {
            this.list = times;
            this.errorMd5 = md5;
            this.crashFile = f;
        }

        @Override
        public void onSuccess() {
            if (crashFile != null) {
                File file = crashFile;
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
            if (list != null && errorMd5 != null) {
                CrashStore.instance().removeMd5Cache(errorMd5, list);
            }
        }

        @Override
        public void onFail() {
        }
    }
}
