package org.camunda.bpm.spring.boot.starter.webapp.filter.csrf.it;

import org.camunda.bpm.spring.boot.starter.webapp.filter.csrf.it.util.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CsrfPreventionIT {

  @Test
  public void shouldSetCookieWebapp() {
    URLConnection connection = performRequest("http://localhost:8080/app/tasklist/default");

    String xsrfCookieValue = getXsrfCookieValue(connection);
    String xsrfTokenHeader = getXsrfTokenHeader(connection);

    assertThat(xsrfCookieValue).matches("XSRF-TOKEN=[A-Z0-9]{32};Path=/;SameSite=Strict");
    assertThat(xsrfTokenHeader).matches("[A-Z0-9]{32}");

    assertThat(xsrfCookieValue).contains(xsrfTokenHeader);
  }

  @Test
  public void shouldSetCookieWebappRest() {
    URLConnection connection = performRequest("http://localhost:8080/api/engine/engine/");

    String xsrfCookieValue = getXsrfCookieValue(connection);
    String xsrfTokenHeader = getXsrfTokenHeader(connection);

    assertThat(xsrfCookieValue).matches("XSRF-TOKEN=[A-Z0-9]{32};Path=/;SameSite=Strict");
    assertThat(xsrfTokenHeader).matches("[A-Z0-9]{32}");

    assertThat(xsrfCookieValue).contains(xsrfTokenHeader);
  }

  protected URLConnection performRequest(String url) {
    URLConnection connection = null;

    try {
      connection = new URL(url).openConnection();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      connection.connect();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return connection;
  }

  protected String getXsrfTokenHeader(URLConnection connection) {
    return connection.getHeaderField("X-XSRF-TOKEN");
  }

  protected String getXsrfCookieValue(URLConnection connection) {
    Map<String, List<String>> headerFields = connection.getHeaderFields();
    List<String> cookies = headerFields.get("Set-Cookie");

    for (String cookie : cookies) {
      if (cookie.startsWith("XSRF-TOKEN=")) {
        return cookie;
      }
    }

    return "";
  }

}
