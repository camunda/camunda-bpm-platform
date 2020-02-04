package org.camunda.bpm.rest.distro.test.config.cors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CorsConfigurationEnabledWildcardTest extends AbstractCorsConfigurationTest {
  
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
    assertThat(response.getHeaders().getAccessControlAllowOrigin()).contains("*");
  }

  @Test
  public void shouldPassCrossOriginRequest() {
    // given
    // cross origin but allowed through wildcard
    String origin = "http://other.origin";

    HttpHeaders headers = new HttpHeaders();
    headers.add("Origin", origin);

    // when
    ResponseEntity<List> response = testRestTemplate.exchange("/rest/task", HttpMethod.GET, new HttpEntity<>(headers), List.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getAccessControlAllowOrigin()).contains("*");
  }
}