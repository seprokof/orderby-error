package com.seprokof;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.seprokof.sample.SampleEntity;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;

@Testcontainers
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApplicationTest implements TestPropertyProvider {

    @Container
    private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName("sampledb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    public void testSort() {
        for (int i = 0; i < 3; i++) {
            SampleEntity se = new SampleEntity(null, "data" + i);
            HttpResponse<?> response = client.toBlocking().exchange(HttpRequest.POST("/samples", se));
            assertEquals(201, response.getStatus().getCode());
        }
        HttpResponse<?> goodResponse = client.toBlocking().exchange(HttpRequest.GET("/samples"));
        assertEquals(200, goodResponse.getStatus().getCode());
        HttpResponse<?> badResponse = client.toBlocking().exchange(HttpRequest.GET("/samples?sort=-data"));
        assertEquals(200, badResponse.getStatus().getCode());
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("r2dbc.datasources.default.url", "r2dbc:postgresql://" + postgreSQLContainer.getContainerIpAddress()
                + ":" + postgreSQLContainer.getMappedPort(5432) + "/sampledb");
        return props;
    }

}