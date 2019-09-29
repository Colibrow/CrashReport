package com.sdk.crash.crashreport.callback;

import com.sdk.crash.crashreport.common.listener.ErrorInfoUploaderListener;
import com.sdk.crash.crashreport.common.listener.ErrorResultListener;
import com.sdk.crash.proguard.ThreadPool;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//不需要上传token信息
public class CrashRocketChatUploader implements ErrorInfoUploaderListener {

    @Override
    public void upload(String msgJson, String md5Json, final ErrorResultListener listener) {
        postMessage(msgJson, new CallBack() {
            @Override
            public void onResponse(int code, String message) {
                if (listener != null) {
                    if (code == 200) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }
            }
        });
    }

    protected void postMessage(String msg, CallBack callBack) {
        Map<String, String> params = new HashMap<>();
        params.put("text", msg);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        requestOnThread("http://appguuidea.8866.org:3000/hooks/dwoy8MpeJa7w3RwqD/tnfNbyLRJyKoGqDKDugPydSzG3AtGginLyn6WS3YFSHSneHX", params, null, callBack);
    }

    //线程加载net请求
    private void requestOnThread(final String url, final Map<String, String> params, final Map<String, File> files, final CallBack callBack) {
        ThreadPool.submitSingleTask(new Runnable() {
            @Override
            public void run() {
                postWithJson(url, params, files, new CallBack() {
                    @Override
                    public void onResponse(final int code, final String message) {
                        if (callBack != null) {
                            ThreadPool.submitUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (callBack != null) {
                                        callBack.onResponse(code, message);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void postWithJson(String url, Map<String, String> params, Map<String, File> files, CallBack callBack) {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(5 * 1000); // 缓存的最长时间
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST");

            //header
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey());
                sb.append("=");
                try {
                    sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);

            DataOutputStream outStream = new DataOutputStream(
                    conn.getOutputStream());
            outStream.write(sb.toString().getBytes());
            outStream.flush();
            outStream.close();

            int res = conn.getResponseCode();

            StringBuilder result = new StringBuilder();
            InputStream in;
            if (res == 200) {
                // 读取返回数据
                in = conn.getInputStream();
            } else {
                in = conn.getErrorStream();
            }
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8")); //$NON-NLS-1$
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n"); //$NON-NLS-1$
                }
                reader.close();
                if (callBack != null) {
                    callBack.onResponse(res, result.toString());
                }
            } else {
                if (callBack != null) {
                    callBack.onResponse(500, result.toString());
                }
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void postWithFormat(String url, Map<String, String> params, Map<String, File> files, CallBack callBack) {
        String BOUNDARY = java.util.UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        String PREFIX = "--";
        String LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(5 * 1000); // 缓存的最长时间
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST");

            //header
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                    + ";boundary=" + BOUNDARY);

            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX).append(BOUNDARY).append(LINEND);
                sb.append("Content-Disposition: form-data; name=\""
                        + entry.getKey() + "\"" + LINEND);
                sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                sb.append(LINEND);
                sb.append(entry.getValue());
                sb.append(LINEND);
            }

            DataOutputStream outStream = new DataOutputStream(
                    conn.getOutputStream());
            outStream.write(sb.toString().getBytes());
            outStream.flush();
            outStream.close();

            // 发送文件数据
            if (files != null) {
                for (Map.Entry<String, File> file : files.entrySet()) {
                    StringBuilder sb1 = new StringBuilder();
                    sb1.append(PREFIX);
                    sb1.append(BOUNDARY);
                    sb1.append(LINEND);
                    // name是post中传参的键 filename是文件的名称
                    sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                            + file.getKey() + "\"" + LINEND);
                    sb1.append("Content-Type: application/octet-stream; charset="
                            + CHARSET + LINEND);
                    sb1.append(LINEND);
                    outStream.write(sb1.toString().getBytes());

                    InputStream is = new FileInputStream(file.getValue());
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }
                    is.close();
                    outStream.write(LINEND.getBytes());
                }
            }

            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            // 得到响应码
            int res = conn.getResponseCode();

            StringBuilder result = new StringBuilder();
            InputStream in;
            if (res == 200) {
                // 读取返回数据
                in = conn.getInputStream();
            } else {
                in = conn.getErrorStream();
            }
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8")); //$NON-NLS-1$
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n"); //$NON-NLS-1$
                }
                reader.close();
                if (callBack != null) {
                    callBack.onResponse(res, result.toString());
                }
            } else {
                if (callBack != null) {
                    callBack.onResponse(500, result.toString());
                }
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface CallBack {
        void onResponse(int code, String message);
    }
}
