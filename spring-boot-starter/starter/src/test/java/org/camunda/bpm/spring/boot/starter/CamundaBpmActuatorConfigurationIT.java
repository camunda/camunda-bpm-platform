package org.camunda.bpm.spring.boot.starter;

import static org.junit.Assert.assertTrue;

import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CamundaBpmActuatorConfigurationIT extends AbstractCamundaAutoConfigurationIT{

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void jobExecutorHealthIndicatorTest() {
    final String body = getHealthBody();
    assertTrue("wrong body " + body, body.contains("jobExecutor\":{\"status\":\"UP\""));
  }

  @Test
  public void processEngineHealthIndicatorTest() {
    final String body = getHealthBody();
    assertTrue("wrong body " + body, body.contains("processEngine\":{\"status\":\"UP\",\"details\":{\"name\":\"testEngine\"}}"));
  }

  private String getHealthBody() {
    ResponseEntity<String> entity = testRestTemplate.getForEntity("/actuator/health", String.class);
    final String body = entity.getBody();
    return body;
  }
}
