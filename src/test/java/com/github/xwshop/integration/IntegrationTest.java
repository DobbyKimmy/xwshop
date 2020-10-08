package com.github.xwshop.integration;

import com.alibaba.fastjson.JSON;
import com.github.xwshop.XwshopApplication;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
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

    /*private void doHttpGetRequest(String url,CloseableHttpClient httpClient){
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Accept", "application/json");
        httpClient.execute(httpGet);
    }*/

    // 代码还没有重构！！！
    @Test
    public void loginLogoutTest() throws IOException {
        // 最开始默认情况下，访问/api/status 处于未登录状态
        // 发送验证码
        // 带着验证码进行登录，得到Cookie
        // 带着Cookie访问 /api/status 应该处于登录状态
        // 调用/api/logout
        // 再次带着Cookie访问/api/status 恢复成为未登录状态


        String port = environment.getProperty("local.server.port");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // context
        HttpContext localContext = new BasicHttpContext();
        HttpClientContext context = HttpClientContext.adapt(localContext);


        try {
            // 1. 最开始默认情况下，访问/api/status 处于未登录状态
            HttpGet httpGet = new HttpGet("http://localhost:" + port + "/api/status");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Accept", "application/json");
            HttpResponse response = httpClient.execute(httpGet);
            Header[] allHeader = response.getAllHeaders();
            Map<String, String> responseMap = new HashMap<>();
            for (Header h : allHeader) {
                responseMap.put(h.getName(), h.getValue());
            }
            Assertions.assertEquals("false", responseMap.get("login"));

            // 2. 发送验证码
            HttpPost httpPost = new HttpPost("http://localhost:" + port + "/api/code");
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("tel", "13812345678");
            requestMap.put("code", null);
            String entity1 = JSON.toJSONString(requestMap);
            httpPost.setEntity(new StringEntity(entity1, ContentType.APPLICATION_JSON));
            httpClient.execute(httpPost, (ResponseHandler<String>) httpResponse -> {
                Assertions.assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
                return null;
            });

            // 3. 带着验证码进行登录，得到Cookie
            String sessionId = null;
            HttpPost httpPost1 = new HttpPost("http://localhost:" + port + "/api/login");
            Map<String, String> requestMap1 = new HashMap<>();
            requestMap1.put("tel", "13812345678");
            requestMap1.put("code", "000000");
            String entity2 = JSON.toJSONString(requestMap1);
            httpPost1.setEntity(new StringEntity(entity2, ContentType.APPLICATION_JSON));
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost1, context);
            List<Cookie> cookie = context.getCookieStore().getCookies();
            for (int i = 0; i < cookie.size(); i++) {
                if (cookie.get(i).getName().equals("JSESSIONID")) {
                    sessionId = cookie.get(i).getValue();
                }
            }
            System.out.println("=========");
            System.out.println(sessionId);
            System.out.println("=========");
            Assertions.assertNotNull(sessionId);

            // 4. 带着Cookie访问 /api/status 应该处于登录状态
            HttpGet httpGet4 = new HttpGet("http://localhost:" + port + "/api/status");
            httpGet4.setHeader("Content-Type", "application/json");
            httpGet4.setHeader("Accept", "application/json");
            httpGet4.setHeader("Set-Cookie", sessionId);
            HttpResponse response4 = httpClient.execute(httpGet4);
            Header[] allHeader4 = response4.getAllHeaders();
            Map<String, String> responseMap4 = new HashMap<>();
            for (Header h : allHeader4) {
                responseMap4.put(h.getName(), h.getValue());
            }
            Assertions.assertEquals("true", responseMap4.get("login"));
            // Assertions.assertNotNull(responseMap4.get("user"));
            // System.out.println(responseMap4.get("user"));

            // 5. 注销登录 调用/api/logout,注意注销登录也需要带上Cookie
            HttpPost httpPost5 = new HttpPost("http://localhost:" + port + "/api/logout");
            httpPost5.setHeader("Content-Type", "application/json");
            httpPost5.setHeader("Accept", "application/json");
            httpPost5.setHeader("Set-Cookie", sessionId);
            httpClient.execute(httpPost5);

            // 6. 再次带着Cookie访问/api/status 恢复成为未登录状态
            HttpGet httpGet6 = new HttpGet("http://localhost:" + port + "/api/status");
            httpGet6.setHeader("Content-Type", "application/json");
            httpGet6.setHeader("Accept", "application/json");
            httpGet6.setHeader("Set-Cookie", sessionId);
            HttpResponse response6 = httpClient.execute(httpGet6);
            Header[] allHeader6 = response6.getAllHeaders();
            Map<String, String> responseMap6 = new HashMap<>();
            for (Header h : allHeader6) {
                responseMap6.put(h.getName(), h.getValue());
            }
            Assertions.assertEquals("false", responseMap6.get("login"));
        } finally {
            httpClient.close();
        }
    }

    @Test
    public void returnHttpOKWhenParameterIsCorrect() throws IOException {
        String port = environment.getProperty("local.server.port");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost("http://localhost:" + port + "/api/code");
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("tel", "13812345678");
            requestMap.put("code", "000000");
            String entity = JSON.toJSONString(requestMap);
            httpPost.setEntity(new StringEntity(entity, ContentType.APPLICATION_JSON));
            httpClient.execute(httpPost, (ResponseHandler<String>) httpResponse -> {
                Assertions.assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
                return null;
            });

            HttpPost httpPost1 = new HttpPost("http://localhost:" + port + "/api/login");
            httpPost1.setEntity(new StringEntity(entity, ContentType.APPLICATION_JSON));
            httpClient.execute(httpPost1, (ResponseHandler<String>) httpResponse -> {
                Assertions.assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
                return null;
            });
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
            HttpPost httpPost = new HttpPost("http://localhost:" + port + "/api/code");
            Map<String, String> entity = new HashMap<>();
            entity.put("tel", null);
            entity.put("code", null);
            String stringEntity = JSON.toJSONString(entity);
            httpPost.setEntity(new StringEntity(stringEntity, ContentType.APPLICATION_JSON));
            client.execute(httpPost, (ResponseHandler<String>) httpResponse -> {
                Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, httpResponse.getStatusLine().getStatusCode());
                return null;
            });
            // with invalid tel
            HttpPost httpPost1 = new HttpPost("http://localhost:" + port + "/api/code");
            Map<String, String> entity1 = new HashMap<>();
            entity.put("tel", "138123");
            entity.put("code", null);
            String stringEntity1 = JSON.toJSONString(entity1);
            httpPost1.setEntity(new StringEntity(stringEntity1, ContentType.APPLICATION_JSON));
            client.execute(httpPost1, (ResponseHandler<String>) httpResponse -> {
                Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, httpResponse.getStatusLine().getStatusCode());
                return null;
            });
        } finally {
            client.close();
        }
    }
}


