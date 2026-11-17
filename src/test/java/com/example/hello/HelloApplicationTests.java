package com.example.hello;

import com.example.hello.controller.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HelloApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsWithHelloController() {
        assertThat(context.getBean(HelloController.class)).isNotNull();
    }
}
