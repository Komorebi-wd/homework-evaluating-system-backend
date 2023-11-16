package com.example;

import com.github.jeffreyning.mybatisplus.conf.EnableMPP;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMPP
public class SbDemo1Application {

    public static void main(String[] args) {
        SpringApplication.run(SbDemo1Application.class, args);
    }

}
