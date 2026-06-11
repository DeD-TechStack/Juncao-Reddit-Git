package com.redgit.profile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class ProfileApplication {

    public static void main(String[] args) {
		SpringApplication.run(ProfileApplication.class, args);
	}

}
