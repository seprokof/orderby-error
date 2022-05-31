package com.seprokof;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public void testWriteRead() {
        SampleEntity se = new SampleEntity(null, "data");
        HttpResponse<?> postResponse = client.toBlocking().exchange(HttpRequest.POST("/samples", se));
        assertEquals(201, postResponse.getStatus().getCode());
        String locationHeader = postResponse.getHeaders().get("Location");
        assertNotNull(locationHeader);
        String[] urlParts = locationHeader.split("/");
        assertEquals("1", urlParts[urlParts.length - 1]);

        HttpResponse<SampleEntity> getResponse = client.toBlocking()
                .exchange(HttpRequest.GET("/samples/" + urlParts[urlParts.length - 1]), SampleEntity.class);
        assertEquals(200, getResponse.getStatus().getCode());
        Optional<SampleEntity> body = getResponse.getBody();
        assertNotNull(body);
        assertTrue(body.isPresent());
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("r2dbc.datasources.default.url", "r2dbc:postgresql://" + postgreSQLContainer.getContainerIpAddress()
                + ":" + postgreSQLContainer.getMappedPort(5432) + "/sampledb");
        return props;
    }

}