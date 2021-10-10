package com.github.agabrys.k8s.learn.k8scontroller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class CounterService {

  private final String serviceUrl;

  public CounterService(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public void increment() {
    sendRequest("/increment");
  }

  public void decrement() {
    sendRequest("/decrement");
  }

  private void sendRequest(String path) {
    var request = HttpRequest.newBuilder(URI.create(serviceUrl + path)).build();
    HttpResponse<Void> response;
    try {
      response = HttpClient.newHttpClient().send(request, BodyHandlers.discarding());
    } catch (IOException | InterruptedException e) {
      throw new CounterServiceException(e);
    }
    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
      throw new CounterServiceException("The request couldn't be processed, error code: " + response.statusCode());
    }
  }

  public static class CounterServiceException extends RuntimeException {

    public CounterServiceException(String message) {
      super(message);
    }

    public CounterServiceException(Throwable cause) {
      super(cause);
    }
  }
}
