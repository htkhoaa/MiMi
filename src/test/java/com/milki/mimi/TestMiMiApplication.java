package com.milki.mimi;

import org.springframework.boot.SpringApplication;

public class TestMiMiApplication {

    public static void main(String[] args) {
        SpringApplication.from(MiMiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
