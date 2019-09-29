package com.sdk.crash.crashreport.common.listener;

public interface LoggerInterface {
    void debug(String msg);

    void verbose(String msg);

    void warn(String msg);

    void info(String msg);

    void error(String msg);
}
