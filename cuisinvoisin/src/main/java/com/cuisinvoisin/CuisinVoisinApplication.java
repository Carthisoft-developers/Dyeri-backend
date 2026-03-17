// com/cuisinvoisin/CuisinVoisinApplication.java
package com.cuisinvoisin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CuisinVoisinApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuisinVoisinApplication.class, args);
    }
}
