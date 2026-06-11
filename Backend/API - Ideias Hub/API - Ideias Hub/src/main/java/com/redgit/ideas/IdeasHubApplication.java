package com.redgit.ideas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@PropertySource("classpath:mongo.properties")
public class IdeasHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdeasHubApplication.class, args);
    }

}
