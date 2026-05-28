package com.diplom.selector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class SelectorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SelectorApplication.class, args);
    }
}
