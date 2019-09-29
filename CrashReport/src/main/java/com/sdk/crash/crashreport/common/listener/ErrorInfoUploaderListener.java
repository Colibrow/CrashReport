package com.sdk.crash.crashreport.common.listener;

public interface ErrorInfoUploaderListener {
    /**
     *
     * @param msgJson
     * @param md5Json 格式()
     * @param listener
     */
    void upload(String msgJson, String md5Json, ErrorResultListener listener);
}
