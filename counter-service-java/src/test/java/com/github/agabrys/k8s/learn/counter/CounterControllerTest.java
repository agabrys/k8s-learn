package com.github.agabrys.k8s.learn.counter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class CounterControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void verify() throws Exception {
    expectValue(0);
    incrementValue();
    expectValue(1);
    incrementValue();
    incrementValue();
    expectValue(3);
    decrementValue();
    expectValue(2);
    decrementValue();
    decrementValue();
    expectValue(0);
    decrementValue();
    expectValue(0);
  }

  private void expectValue(int count) throws Exception {
    mockMvc.perform(get("/counter/value"))
      .andExpect(status().isOk())
      .andExpect(content().string(Integer.toString(count)));
  }

  private void incrementValue() throws Exception {
    mockMvc.perform(get("/counter/increment"))
      .andExpect(status().isOk());
  }

  private void decrementValue() throws Exception {
    mockMvc.perform(get("/counter/decrement"))
      .andExpect(status().isOk());
  }
}
