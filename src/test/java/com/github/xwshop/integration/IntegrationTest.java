package com.github.xwshop.integration;

import com.alibaba.fastjson.JSON;
import com.github.xwshop.XwshopApplication;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = XwshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.yml")
public class IntegrationTest {
    @Autowired
    Environment environment;

    private HttpResponse doHttpGetRequest(String url, CloseableHttpClient httpClient) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Accept", "application/json");
        return httpClient.execute(httpGet);
    }

    private HttpResponse doHttpGetRequest(String url, CloseableHttpClient httpClient, String sessionId) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Set-Cookie", sessionId);
        return httpClient.execute(httpGet);
    }

    private HttpResponse doHttpPostRequest(String url, CloseableHttpClient httpClient, StringEntity stringEntity) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(stringEntity);
        return httpClient.execute(httpPost);
    }

    private HttpResponse doHttpPostRequest(String url, CloseableHttpClient httpClient, String sessionId) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Set-Cookie", sessionId);
        return httpClient.execute(httpPost);
    }

    private HttpResponse doHttpPostRequest(String url, CloseableHttpClient httpClient, StringEntity stringEntity, HttpClientContext context) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Conten-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(stringEntity);
        return httpClient.execute(httpPost, context);
    }

    private StringEntity mapToRequestEntity(Map<String, String> map) {
        String jsonString = JSON.toJSONString(map);
        return new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    }

    @Test
    public void loginLogoutTest() throws IOException {
        String port = environment.getProperty("local.server.port");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // context
        HttpContext localContext = new BasicHttpContext();
        HttpClientContext context = HttpClientContext.adapt(localContext);

        try {
            // 1. 最开始默认情况下，访问/api/status 处于未登录状态
            HttpResponse response = doHttpGetRequest("http://localhost:" + port + "/api/status", httpClient);
            Header[] allHeader = response.getAllHeaders();
            Map<String, String> responseMap = new HashMap<>();
            for (Header h : allHeader) {
                responseMap.put(h.getName(), h.getValue());
            }
            Assertions.assertEquals("false", responseMap.get("login"));

            // 2. 发送验证码
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("tel", "13812345678");
            requestMap.put("code", null);
            response = doHttpPostRequest("http://localhost:" + port + "/api/code", httpClient, mapToRequestEntity(requestMap));
            Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            // 3. 带着验证码进行登录，得到Cookie
            String sessionId = null;
            requestMap.clear();
            requestMap.put("tel", "13812345678");
            requestMap.put("code", "000000");
            response = doHttpPostRequest("http://localhost:" + port + "/api/login", httpClient, mapToRequestEntity(requestMap), context);
            List<Cookie> cookie = context.getCookieStore().getCookies();
            for (int i = 0; i < cookie.size(); i++) {
                if (cookie.get(i).getName().equals("JSESSIONID")) {
                    sessionId = cookie.get(i).getValue();
                }
            }
            Assertions.assertNotNull(sessionId);

            // 4. 带着Cookie访问 /api/status 应该处于登录状态
            response = doHttpGetRequest("http://localhost:" + port + "/api/status", httpClient, sessionId);
            Header[] allHeader4 = response.getAllHeaders();
            responseMap = new HashMap<>();
            for (Header h : allHeader4) {
                responseMap.put(h.getName(), h.getValue());
            }
            Assertions.assertEquals("true", responseMap.get("login"));

            // 5. 注销登录 调用/api/logout,注意注销登录也需要带上Cookie
            doHttpPostRequest("http://localhost:" + port + "/api/logout", httpClient, sessionId);

            // 6. 再次带着Cookie访问/api/status 恢复成为未登录状态
            response = doHttpGetRequest("http://localhost:" + port + "/api/status", httpClient, sessionId);
            Header[] allHeader6 = response.getAllHeaders();
            responseMap = new HashMap<>();
            for (Header h : allHeader6) {
                responseMap.put(h.getName(), h.getValue());
            }
            Assertions.assertEquals("false", responseMap.get("login"));
        } finally {
            httpClient.close();
        }
    }

    @Test
    public void returnHttpOKWhenParameterIsCorrect() throws IOException {
        String port = environment.getProperty("local.server.port");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("tel", "13812345678");
            requestMap.put("code", "000000");
            HttpResponse response = doHttpPostRequest("http://localhost:" + port + "/api/code", httpClient, mapToRequestEntity(requestMap));
            Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            response = doHttpPostRequest("http://localhost:" + port + "/api/login", httpClient, mapToRequestEntity(requestMap));
            Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        } finally {
            httpClient.close();
        }
    }

    @Test
    public void returnHttpBadRequestWhenParameterIsNotCorrect() throws IOException {
        String port = environment.getProperty("local.server.port");
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            // with empty tel
            Map<String, String> map = new HashMap<>();
            map.put("tel", null);
            map.put("code", null);
            HttpResponse httpResponse = doHttpPostRequest("http://localhost:" + port + "/api/code", client, mapToRequestEntity(map));
            Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, httpResponse.getStatusLine().getStatusCode());
            // with invalid tel
            map.put("tel", "138123");
            map.put("code", null);
            httpResponse = doHttpPostRequest("http://localhost:" + port + "/api/code", client, mapToRequestEntity(map));
            Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, httpResponse.getStatusLine().getStatusCode());
        } finally {
            client.close();
        }
    }
}


