package com.seprokof.sample;

import java.io.Serializable;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents sample.
 * 
 * @author seprokof
 *
 */
@Data
@AllArgsConstructor
@Introspected
@Schema(name = "Sample", description = "Represents sample")
@MappedEntity(value = "sample", alias = "sample")
public class SampleEntity implements Serializable {
    private static final long serialVersionUID = -5410891143601576439L;

    @Id
    @GeneratedValue
    private Long id;
    private String data;

}
