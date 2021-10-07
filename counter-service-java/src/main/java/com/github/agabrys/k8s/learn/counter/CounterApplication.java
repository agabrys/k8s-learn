package com.github.agabrys.k8s.learn.counter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.github.agabrys.k8s.learn.counter")
@SpringBootApplication
public class CounterApplication {

  public static void main(String[] args) {
    SpringApplication.run(CounterApplication.class, args);
  }
}
