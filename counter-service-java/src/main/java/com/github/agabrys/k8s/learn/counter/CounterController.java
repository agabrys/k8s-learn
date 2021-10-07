package com.github.agabrys.k8s.learn.counter;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/counter")
public class CounterController {

  private final AtomicInteger count = new AtomicInteger();

  @GetMapping("/value")
  public int getValue() {
    return count.get();
  }

  @GetMapping("/increment")
  public void increment() {
    count.incrementAndGet();
  }

  @GetMapping("/decrement")
  public void decrement() {
    count.updateAndGet(value -> value > 0 ? value - 1 : 0);
  }
}
