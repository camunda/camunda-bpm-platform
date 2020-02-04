package org.camunda.bpm.rest.distro.test.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    log(request);
    try {
      ClientHttpResponse response = execution.execute(request, body);
      log(response);
      return response;
    } catch (IOException e) {
      log.error("Request execution failed:");
      e.printStackTrace();
      throw e;
    }
  }

  private void log(HttpMessage message) throws IOException {
    if(message instanceof HttpRequest) {
      HttpRequest request = (HttpRequest) message;
      log.info("URI: {}", request.getURI());
      log.info("Method: {}", request.getMethod());
    } else if(message instanceof ClientHttpResponse) {
      ClientHttpResponse response = (ClientHttpResponse) message;
      log.info("Status code: {}", response.getStatusCode());
    } else {
      return;
    }
    log.info("Headers:");
    message.getHeaders().forEach((k, v) -> log.info("    " + k + ": " + v));
  }
}