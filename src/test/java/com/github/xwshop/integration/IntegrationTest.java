package com.github.xwshop.integration;

import com.alibaba.fastjson.JSON;
import com.github.xwshop.XwshopApplication;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = XwshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.yml")
public class IntegrationTest {
    @Autowired
    Environment environment;

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


