package com.seprokof.sample;

import reactor.core.publisher.Mono;

public interface SampleService {
    
    public Mono<SampleEntity> save(SampleEntity entity);
    
    public Mono<SampleEntity> findById(Long id);

}
