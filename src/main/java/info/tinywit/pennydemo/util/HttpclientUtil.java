package info.tinywit.pennydemo.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpclientUtil {
    private static final Charset CHARSET = Consts.UTF_8;
    // 异常自动恢复处理, 使用HttpRequestRetryHandler接口实现请求的异常恢复
    private static final HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {

        // 自定义的恢复策略
        public boolean retryRequest(IOException exception,
                                    int executionCount,
                                    HttpContext context) {
            // 设置恢复策略，在发生异常时候将自动重试3次
            if (executionCount >= 3) {
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                return true;
            }
            if (exception instanceof SSLHandshakeException) {
                return false;
            }
            return false;
        }
    };
    // 使用ResponseHandler接口处理响应，HttpClient使用ResponseHandler会自动管理连接的释放，解决了对连接的释放管理
    private static final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

        // 自定义响应处理
        public String handleResponse(HttpResponse response)
                throws
                IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                if (entity.getContentEncoding() != null
                        && entity.getContentEncoding().getValue() != null) {
                    return EntityUtils.toString(entity,
                            entity.getContentEncoding().getValue());
                } else {
                    return EntityUtils.toString(entity,
                            CHARSET);
                }
            } else {
                return null;
            }
        }
    };

    private static final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(20000).setConnectionRequestTimeout(60000).build();
    private static final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    static {

        // socketConfig
        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoTimeout(60000).build();
        connectionManager.setDefaultSocketConfig(socketConfig);

        // connectionConfig
        MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(100).setMaxLineLength(10000).build();
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

    }

    /**
     * Get方式提交
     *
     * @param url 提交地址
     * @return 响应消息
     */
    public static String get(String url) {
        return get(url, null, CHARSET);
    }

    /**
     * Get方式提交
     *
     * @param url    提交地址
     * @param params 查询参数集, 键/值对
     * @return 响应消息
     */
    public static String get(String url, Map<String, String> params) {
        return get(url, params, CHARSET);
    }

    /**
     * Get方式提交,URL中不包含查询参数, 格式：http://www.g.cn
     *
     * @param url     提交地址
     * @param params  查询参数集, 键/值对
     * @param charset 参数提交编码集
     * @return 响应消息
     */
    public static String get(String url, Map<String, String> params, Charset charset) {
        if (url == null || StringUtils.isEmpty(url)) {
            return null;
        }
        List<NameValuePair> qparams = getParamsList(params);
        if (qparams != null && qparams.size() > 0) {
            charset = (charset == null ? CHARSET : charset);
            String formatParams = URLEncodedUtils.format(qparams, charset);
            url = url + "?" + formatParams;
        }
        HttpClient httpclient = getDefaultHttpClient(charset);
        HttpGet hg = new HttpGet(url);
        // 发送请求，得到响应
        String responseStr = null;
        try {
            responseStr = httpclient.execute(hg, responseHandler);
        } catch (ClientProtocolException e) {
            throw new RuntimeException("客户端连接协议错误", e);
        } catch (IOException e) {
            throw new RuntimeException("IO操作异常", e);
        } finally {
            abortConnection(hg, httpclient);
        }
        return responseStr;
    }

    /**
     * Post方式提交,URL中不包含提交参数, 格式：http://www.g.cn
     *
     * @param url    提交地址
     * @param params 提交参数集, 键/值对
     * @return 响应消息
     */
    public static String post(String url, Map<String, String> params) {
        return post(url, params, CHARSET);
    }

    /**
     * Post方式提交,URL中不包含提交参数, 格式：http://www.g.cn
     *
     * @param url  提交地址
     * @param body post请求body里面的字符串
     * @return 响应消息
     */
    public static String post(String url, String body) {
        return post(url, body, CHARSET);
    }

    /**
     * Post方式提交,URL中不包含提交参数, 格式：http://www.g.cn
     *
     * @param url     提交地址
     * @param params  提交参数集, 键/值对
     * @param charset 参数提交编码集
     * @return 响应消息
     */
    public static String post(String url, Map<String, String> params, Charset charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        HttpClient httpclient = getDefaultHttpClient(charset);
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(getParamsList(params), charset);
        HttpPost hp = new HttpPost(url);
        hp.setEntity(formEntity);
        // 发送请求，得到响应
        String responseStr = null;
        try {
            responseStr = httpclient.execute(hp, responseHandler);
        } catch (ClientProtocolException e) {
            throw new RuntimeException("客户端连接协议错误", e);
        } catch (IOException e) {
            throw new RuntimeException("IO操作异常", e);
        } finally {
            abortConnection(hp, httpclient);
        }
        return responseStr;
    }

    /**
     * Post方式提交,URL中不包含提交参数, 格式：http://www.g.cn
     *
     * @param url     提交地址
     * @param body    post请求body里面的字符串
     * @param charset 参数提交编码集
     * @return 响应消息
     */
    public static String post(String url, String body, Charset charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        HttpClient httpclient = getDefaultHttpClient(charset);
        HttpPost hp = new HttpPost(url);
        if (body != null) {
            hp.setEntity(new StringEntity(body, charset));
        }
        // 发送请求，得到响应
        String responseStr = null;
        try {
            responseStr = httpclient.execute(hp, responseHandler);
        } catch (ClientProtocolException e) {
            throw new RuntimeException("客户端连接协议错误", e);
        } catch (IOException e) {
            throw new RuntimeException("IO操作异常", e);
        } finally {
            abortConnection(hp, httpclient);
        }
        return responseStr;
    }

    /**
     * 获取DefaultHttpClient实例
     *
     * @param charset 参数编码集, 可空
     * @return DefaultHttpClient 对象
     * @throws Exception
     */
    private static HttpClient getDefaultHttpClient(final Charset charset) {

        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).setUserAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)").setRetryHandler(requestRetryHandler).build();
        return httpclient;
    }

    /**
     * 释放HttpClient连接
     *
     * @param hrb        请求对象
     * @param httpclient client对象
     */
    private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient) {
        if (hrb != null) {
            hrb.abort();
        }
    }

    /**
     * 将传入的键/值对参数转换为NameValuePair参数集
     *
     * @param paramsMap 参数集, 键/值对
     * @return NameValuePair参数集
     */
    private static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.size() == 0) {
            return null;
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> map : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
        }
        return params;
    }
}
