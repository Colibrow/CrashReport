package com.sdk.crash.crashreport.common.listener;

public interface ErrorResultListener {
    /**
     * 成功
     */
    void onSuccess();

    /**
     * 失败
     */
    void onFail();
}
