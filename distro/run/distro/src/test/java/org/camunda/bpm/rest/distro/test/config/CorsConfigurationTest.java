package org.camunda.bpm.rest.distro.test.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.rest.distro.CamundaRestDistro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CamundaRestDistro.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("cors-test")
public class CorsConfigurationTest {

  @Autowired
  TestRestTemplate testRestTemplate;

  @LocalServerPort
  int localPort;

  @Before
  public void init() {
    // allow Origin header to be overridden
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    testRestTemplate.getRestTemplate().setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));
  }

  @Test
  public void shouldPassSameOriginRequest() {
    // given
    // same origin
    String origin = "http://localhost:" + localPort;

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ORIGIN, origin);

    // when
    ResponseEntity<List> response = testRestTemplate.exchange("/rest/task", HttpMethod.GET, new HttpEntity<>(headers), List.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getAccessControlAllowOrigin()).contains(origin);
  }

  @Test
  public void shouldFailCrossOriginRequest() {
    // given
    // cross origin but not allowed
    String origin = "http://localhost:8080";

    HttpHeaders headers = new HttpHeaders();
    headers.add("Origin", origin);

    // when
    ResponseEntity<List> response = testRestTemplate.exchange("/rest/task", HttpMethod.GET, new HttpEntity<>(headers), List.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getHeaders().getAccessControlAllowOrigin()).isNull();
  }
  
  @Test
  public void shouldPassCrossOriginRequest() {
    // given
    // cross origin but allowed
    String origin = "http://localhost:8081";
    
    HttpHeaders headers = new HttpHeaders();
    headers.add("Origin", origin);

    // when
    ResponseEntity<List> response = testRestTemplate.exchange("/rest/task", HttpMethod.GET, new HttpEntity<>(headers), List.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getAccessControlAllowOrigin()).contains(origin);
  }

  private class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
      logRequest(request, body);
      ClientHttpResponse response = execution.execute(request, body);
      logResponse(response);
      return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
      log.info("URI         : {}", request.getURI());
      log.info("Method      : {}", request.getMethod());
      log.info("Headers     :");
      request.getHeaders().forEach((k, v) -> log.info("    " + k + ": " + v));
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
      log.info("Status code  : {}", response.getStatusCode());
      log.info("Headers      :");
      response.getHeaders().forEach((k, v) -> log.info("    " + k + ": " + v));
    }
  }
}