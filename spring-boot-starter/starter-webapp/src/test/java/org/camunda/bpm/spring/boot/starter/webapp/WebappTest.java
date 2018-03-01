package org.camunda.bpm.spring.boot.starter.webapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertEquals;

/**
 * @author Svetlana Dorokhova.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebappExampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebappTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void testLicenseEndpointNotAvailable() throws Exception {
    final ResponseEntity<String> response = testRestTemplate
      .getForEntity("http://localhost:" + this.port + "/api/admin/plugin/license/default/key", String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void testAdminEndpointAvailable() throws Exception {
    final ResponseEntity<String> response = testRestTemplate
      .getForEntity("http://localhost:" + this.port + "/app/admin", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

}
