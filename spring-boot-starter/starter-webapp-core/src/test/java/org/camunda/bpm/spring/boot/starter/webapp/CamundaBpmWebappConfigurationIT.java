package org.camunda.bpm.spring.boot.starter.webapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CamundaBpmWebappConfigurationIT {

  @Test
  public void startUpTest() {
    // context init test
  }
}
