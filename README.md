项目:错误收集

项目介绍：
    通过收集运行过程中出现的非捕获crash以及anr,上报到服务端来达到错误分析的效果。

如何使用?

一.添加依赖
implementation project(':CrashReport')

二.接入
CrashReport.instance().setUploaderListener(new ErrorInfoUploaderListener() {
    @Override
    public void upload(String msgJson, String md5Json, ErrorResultListener listener) {
       //msgJson表示错误信息  //md5Json表示该错误的错误次数和时间
        if (listener != null) {
            listener.onSuccess();
        }
    }
}).initCrashReport(this, BuildConfig.DEBUG);


三.设置strategy规则，通过Builder方式构建,有一些一些列参数,如果不想配置可默认：
    /**
     * 是否在crash时打印错误日志
     */
    boolean isPrintCrash = true;
    /**
     * 发送crash是否退出
     */
    boolean isQuitCrash = true;
    /**
     * 保持最近几天的日志
     */
    int daysKeepInCache = 7;
    /**
     * 是否保持到缓存
     */
    boolean dumpToCache = true;
    /**
     * crash的处理时间间隔
     */
    long crashTimeInterval = 5000L;
    /**
     * anr的处理时间间隔
     */
    long anrTimeInterval = 10000L;

    /**
     * 立即上报crash
     */
    boolean reportCrashImmediately = false;

    /**
     * 立即上报anr
     */
    boolean reportAnrImmediately = false;

    /**
     * 目录
     */
    String baseDir;

    /**
     * 每天上报限制
     */
     long dailyReportLimit = 20;

     /**
      * 是否加密上传
      */
     boolean encrypyEnable;

     /**
      * 每天缓存文件数
      */
     int cacheMaxCountPerDay = 10;

 其中daysKeepInCache最大7天

例子：
Strategy strategy = new Strategy.Builder(this)
             .setEnableDumpToCache(true)
             .setQuitCrash(false)
             .build();
CrashReport.instance().setStrategy(strategy)
或者根据第1点调用Crash.instance().init(context,strategy,true);

四.sdk经过初始化后即可使用，如需要停止使用调用
CrashReport.instance().setEnable(false);

五.如需要自己打印日志可实现接口
CrashReport.instance().setLoggerListener(new LoggerInterface() {
        @Override
        public void debug(String msg) {

        }

        @Override
        public void verbose(String msg) {

        }

        @Override
        public void warn(String msg) {

        }

        @Override
        public void info(String msg) {

        }

        @Override
        public void error(String msg) {

        }
    });


混淆规则：
-keep public class com.sdk.crash.crashreport.errorHandler.CrashDetailBean
-keepclassmembers class com.sdk.crash.crashreport.CrashReport$LooperPrinter{ public <methods>;}
