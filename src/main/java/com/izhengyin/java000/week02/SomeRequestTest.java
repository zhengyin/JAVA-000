package com.izhengyin.java000.week02;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-10-28 17:34
 */
public class SomeRequestTest {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static final WebClient WEB_CLIENT = WebClient.builder().build();

    public static void main(String[] args) throws IOException,InterruptedException{
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("SomeRequestTest","xxx");
        String url = "http://httpbin.org/get";
        doOkHttp(url,headers);
        doHttpClient(url,headers);
        doWebClient(url,headers);
        TimeUnit.SECONDS.sleep(1);
    }

    private static void doOkHttp(String url , Map<String,String> customHeaders) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(url);
        customHeaders.forEach(builder::header);
        builder.header("user-agent","okHttpClient");
        Request request = builder.build();
        try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            System.out.println(response.body().string());
        }
    }

    private static void doHttpClient(String url , Map<String,String> customHeaders) throws IOException{
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        customHeaders.forEach(httpget::setHeader);
        httpget.setHeader("user-agent","httpClient");
        CloseableHttpResponse response = httpclient.execute(httpget);
        try {
           if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
           }
        } finally {
          response.close();
          httpclient.close();
        }

    }

    private static void doWebClient(String url , Map<String,String> customHeaders){
        WEB_CLIENT.get()
                .uri(url)
                .header("user-agent","webClient")
                .headers(httpHeaders -> customHeaders.forEach((key,value) -> httpHeaders.put(key, Arrays.asList(value))))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(System.out::println)
                .subscribe();
    }
}
