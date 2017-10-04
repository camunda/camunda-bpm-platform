package org.camunda.bpm.spring.boot.starter;

import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CamundaJobExecutionAutoConfigurationIT extends AbstractCamundaAutoConfigurationIT {

  @Test
  public void jobConfigurationTest() {
    assertNotNull(jobExecutor);
  }

}
