package com.seprokof;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

//@formatter:off
@OpenAPIDefinition(
        info = @Info(
                title = "OrderBy Error Sample Application", 
                version = "0.0.1", 
                description = "Sample Application to demonstrate bug described in https://github.com/micronaut-projects/micronaut-data/issues/1377"
        )
)
//@formatter:on
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
