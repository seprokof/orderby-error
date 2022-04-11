package com.seprokof.sample;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.Sort.Order;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller("/samples")
@Validated
@Tag(name = "Sample API")
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    private final SampleRepository sampleRepository;

    @Inject
    public SampleController(SampleRepository sampleRepository) {
        super();
        this.sampleRepository = sampleRepository;
    }

    @Post(consumes = MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates new sample", description = "Creates new sample.")
    @ApiResponse(responseCode = "201", description = "Resource created", headers = @Header(name = "location"))
    @ApiResponse(responseCode = "400", description = "Creation request is invalid", content = @Content(schema = @Schema(implementation = JsonError.class)))
    @ApiResponse(responseCode = "500", description = "Internal error happened while processing request", content = @Content(schema = @Schema(implementation = JsonError.class)))
    @Status(HttpStatus.CREATED)
    public Mono<HttpResponse<?>> add(@Valid @Body SampleEntity sampleRequest) {
        LOGGER.debug("Creating new {}", sampleRequest);
        return sampleRepository.save(sampleRequest)
                .doOnSuccess(s -> LOGGER.info("{} successfully created", s))
                .map(s -> HttpResponse.created(URI.create("/samples/" + s.getId())));
    }

    @Get(produces = { MediaType.APPLICATION_JSON_STREAM, MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieves stored samples matched specified query or all samples if no query specified", description = "Retrieves stored samples matched specified query or all samples if no query specified.")
    @ApiResponse(responseCode = "400", description = "Query parameter is invalid", content = @Content(schema = @Schema(implementation = JsonError.class)))
    @ApiResponse(responseCode = "500", description = "Internal error happened while processing request", content = @Content(schema = @Schema(implementation = JsonError.class)))
    public Flux<SampleEntity> getAll(@Nullable @QueryValue String query, @Nullable @Positive @QueryValue Integer size,
            @Nullable @PositiveOrZero @QueryValue Integer page, @Nullable @QueryValue String sort) {
        LOGGER.debug("Retrieving samples matched query '{}'. Size is '{}', page is '{}', sort is '{}'", query, size,
                page, sort);
        QuerySpecification<SampleEntity> querySpec = null;

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            List<Order> orderList = Arrays.stream(sort.split(","))
                    .map(SampleController::convertOrder)
                    .collect(Collectors.toList());
            sorting = Sort.of(orderList);
        }

        Pageable pageable = Pageable.from(sorting);
        if (size != null) {
            pageable = Pageable.from(Optional.ofNullable(page).orElse(0), size, sorting);
        } else if (page != null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    "Size should be specified when page parameter is used");
        }

        AtomicInteger count = new AtomicInteger();
        Consumer<SampleEntity> countingConsumer = s -> {
            count.incrementAndGet();
            LOGGER.trace("{} returned from getAll", s);
        };
        return sampleRepository.findAll(querySpec, pageable)
                .doOnNext(countingConsumer)
                .doOnComplete(() -> LOGGER.info("{} sample(s) returned", count));
    }

    private static Order convertOrder(String sort) {
        sort = sort.trim();
        String direction = sort.substring(0, 1);
        String property = sort.substring(1, sort.length());
        if (!BeanIntrospection.getIntrospection(SampleEntity.class).getProperty(property).isPresent()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to apply sort as property '" + property + "' doesn't declared");
        }
        return "-".equals(direction) ? Order.desc(property) : Order.asc(property);
    }

}
