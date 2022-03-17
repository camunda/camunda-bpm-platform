package org.camunda.bpm.extension.junit5.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * @author Ingo Richtsmeier
 */
public class ProcessEngineExtensionDeploymentIdTest {

  @RegisterExtension
  CustomProcessEngineExtension extension = 
      (CustomProcessEngineExtension) CustomProcessEngineExtension.builder().build();
  
  @Test
  public void testDeploymentId() {
    assertEquals("mockedDeploymentId", extension.getDeploymentId());
  }

}
