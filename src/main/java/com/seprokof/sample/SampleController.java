package com.seprokof.sample;

import java.net.URI;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@Controller("/samples")
@Validated
@Tag(name = "Sample API")
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    private final SampleService sampleService;

    @Inject
    public SampleController(SampleService sampleService) {
        super();
        this.sampleService = sampleService;
    }

    @Post(consumes = MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates new sample", description = "Creates new sample.")
    @ApiResponse(responseCode = "201", description = "Resource created", headers = @Header(name = "location"))
    @ApiResponse(responseCode = "400", description = "Creation request is invalid", content = @Content(schema = @Schema(implementation = JsonError.class)))
    @ApiResponse(responseCode = "500", description = "Internal error happened while processing request", content = @Content(schema = @Schema(implementation = JsonError.class)))
    @Status(HttpStatus.CREATED)
    public Mono<HttpResponse<?>> add(@Valid @Body SampleEntity sampleRequest) {
        LOGGER.debug("Creating new {}", sampleRequest);
        return sampleService.save(sampleRequest)
                .doOnSuccess(s -> LOGGER.info("{} successfully created", s))
                .map(s -> HttpResponse.created(URI.create("/samples/" + s.getId())));
    }

    @Get(uri = "/{id}", produces = { MediaType.APPLICATION_JSON_STREAM, MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieves sample by id", description = "Retrieves sample by id.")
    @ApiResponse(responseCode = "404", description = "Sample not found", content = @Content(schema = @Schema(implementation = JsonError.class)))
    @ApiResponse(responseCode = "500", description = "Internal error happened while processing request", content = @Content(schema = @Schema(implementation = JsonError.class)))
    public Mono<SampleEntity> getById(Long id) {
        LOGGER.debug("Retrieving sample with id '{}'", id);
        return sampleService.findById(id).doOnSuccess(s -> LOGGER.info("Returned {}", s));
    }

}
