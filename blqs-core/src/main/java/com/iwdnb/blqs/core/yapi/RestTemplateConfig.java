package com.iwdnb.blqs.core.yapi;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;

/**
 * restTemplate配置
 */
public class RestTemplateConfig implements InitializingBean {

    public static CloseableHttpClient acceptsUntrustedCertsHttpClient() {
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();

            // setup a Trust Strategy that allows all certificates.
            //
            SSLContext sslContext = null;

            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();

            builder.setSslcontext(sslContext);

            // don't check Hostnames, either.
            // -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

            // here's the special part:
            // -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
            // -- and create a Registry, to register it.
            //
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http",
                                                                                                                                  PlainConnectionSocketFactory.getSocketFactory()).register("https",
                                                                                                                                                                                            sslSocketFactory).build();

            // now, we create connection-manager using our Registry.
            // -- allows multi-threaded use
            PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            connMgr.setMaxTotal(200);
            connMgr.setDefaultMaxPerRoute(100);
            builder.setConnectionManager(connMgr);

            // 设置一个链接的最大存活时间
            builder.setKeepAliveStrategy((httpResponse, httpContext) -> 5 * 1000);

            // finally, build the HttpClient;
            // -- done!
            return builder.build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 创建restTemplate配置
     *
     * @return restTemplate配置
     */
    public static RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory());
        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();

        // 重新设置StringHttpMessageConverter字符集为UTF-8，解决中文乱码问题
        HttpMessageConverter<?> converterTarget = null;
        for (HttpMessageConverter<?> item : converterList) {
            if (StringHttpMessageConverter.class == item.getClass()) {
                converterTarget = item;
                break;
            }
        }
        if (null != converterTarget) {
            converterList.remove(converterTarget);
        }
        converterList.add(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        // 加入FastJson转换器
        converterList.add(new FastJsonHttpMessageConverter4());
        return restTemplate;
    }

    public static RestTemplate proxyRestTemplate() {

        return new RestTemplate(httpClientFactory());
    }

    /**
     * 创建http工厂bean
     *
     * @return 结果
     */
    public static ClientHttpRequestFactory simpleClientHttpRequestFactory() {

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(acceptsUntrustedCertsHttpClient());

        clientHttpRequestFactory.setConnectionRequestTimeout(150000);
        clientHttpRequestFactory.setReadTimeout(150000);
        clientHttpRequestFactory.setConnectTimeout(150000);
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> {

            return true;
        });

        return clientHttpRequestFactory;
    }

    public static SimpleClientHttpRequestFactory httpClientFactory() {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout(15000);
        httpRequestFactory.setConnectTimeout(15000);

        SocketAddress address = new InetSocketAddress("127.0.0.1", 8888);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        httpRequestFactory.setProxy(proxy);

        return httpRequestFactory;
    }

}
