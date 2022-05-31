package com.seprokof.sample;

import javax.transaction.Transactional;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class SampleServiceImpl implements SampleService {

    private final SampleRepository sampleRepository;

    @Inject
    public SampleServiceImpl(SampleRepository sampleRepository) {
        super();
        this.sampleRepository = sampleRepository;
    }

    @Override
    @Transactional
    public Mono<SampleEntity> save(SampleEntity entity) {
        return sampleRepository.save(entity);
    }

    @Override
    @Transactional
    public Mono<SampleEntity> findById(Long id) {
        return sampleRepository.findById(id);
    }

}
