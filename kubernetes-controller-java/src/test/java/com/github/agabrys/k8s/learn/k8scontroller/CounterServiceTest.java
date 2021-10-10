package com.github.agabrys.k8s.learn.k8scontroller;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static okhttp3.mockwebserver.SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.agabrys.k8s.learn.k8scontroller.CounterService.CounterServiceException;
import java.io.IOException;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CounterServiceTest {

  private static final String SERVICE_ENDPOINT = "/service";

  private CounterService service;
  private MockWebServer webServer;

  @BeforeEach
  void setUp() {
    webServer = new MockWebServer();
    service = new CounterService(webServer.url(SERVICE_ENDPOINT).toString());
  }

  @Test
  void verifyIncrementWhenServiceIsAvailable() {
    webServer.setDispatcher(createDispatcherForSuccessfulCall(SERVICE_ENDPOINT + "/increment"));

    assertDoesNotThrow(() -> service.increment());
  }

  @Test
  void verifyIncrementWhenServiceReturnsInvalidCode() {
    webServer.enqueue(new MockResponse().setResponseCode(HTTP_NOT_FOUND));

    var exception = assertThrows(CounterServiceException.class, () -> service.increment());

    assertThat(exception).hasMessage("The request couldn't be processed, error code: " + HTTP_NOT_FOUND);
  }

  @Test
  void verifyIncrementWhenServiceFails() {
    webServer.enqueue(new MockResponse().setBody("text").setSocketPolicy(DISCONNECT_DURING_RESPONSE_BODY));

    var exception = assertThrows(CounterServiceException.class, () -> service.increment());

    assertThat(exception).hasCauseInstanceOf(IOException.class);
  }

  @Test
  void verifyDecrementWhenServiceIsAvailable() {
    webServer.setDispatcher(createDispatcherForSuccessfulCall(SERVICE_ENDPOINT + "/decrement"));

    assertDoesNotThrow(() -> service.decrement());
  }

  @Test
  void verifyDecrementWhenServiceReturnsInvalidCode() {
    webServer.enqueue(new MockResponse().setResponseCode(HTTP_NOT_FOUND));

    var exception = assertThrows(CounterServiceException.class, () -> service.decrement());

    assertThat(exception).hasMessage("The request couldn't be processed, error code: " + HTTP_NOT_FOUND);
  }

  @Test
  void verifyDecrementWhenServiceFails() {
    webServer.enqueue(new MockResponse().setBody("text").setSocketPolicy(DISCONNECT_DURING_RESPONSE_BODY));

    var exception = assertThrows(CounterServiceException.class, () -> service.decrement());

    assertThat(exception).hasCauseInstanceOf(IOException.class);
  }

  private static Dispatcher createDispatcherForSuccessfulCall(String path) {
    return new Dispatcher() {
      @NotNull
      @Override
      public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
        if (recordedRequest.getPath().equals(path)) {
          return new MockResponse().setResponseCode(HTTP_OK);
        }
        // never
        fail("Invalid URL: " + recordedRequest.getPath());
        return null;
      }
    };
  }
}
