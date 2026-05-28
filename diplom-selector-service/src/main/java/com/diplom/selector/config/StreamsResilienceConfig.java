package com.diplom.selector.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;

@Slf4j
@Configuration
public class StreamsResilienceConfig {

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamsUncaughtHandlerConfigurer() {
        return factoryBean -> factoryBean.setStreamsUncaughtExceptionHandler(ex -> {
            log.error("Uncaught Streams exception — replacing thread", ex);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
        });
    }
}
