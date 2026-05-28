package com.diplom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/diplom_shop_test",
        "app.s3.access-key=test",
        "app.s3.secret-key=test",
        "app.s3.bucket=test",
        "app.s3.region=us-east-1"
})
class DiplomApplicationTests {

    @Test
    void contextLoads() {
    }
}
