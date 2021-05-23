package org.camunda.bpm.extension.junit5.deployment;

import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomProcessEngineExtension extends ProcessEngineExtension {
  
  private static final Logger LOG = LoggerFactory.getLogger(CustomProcessEngineExtension.class);
  
  @Override
  public void beforeTestExecution(ExtensionContext context) {
    LOG.debug("set mocked deploymentId");
    deploymentId = "mockedDeploymentId";
  }
  
  @Override
  public void afterTestExecution(ExtensionContext context) {
    LOG.debug("no undeployment needed");
  }
  
  public static CustomProcessEngineExtension builder() {
    return new CustomProcessEngineExtension();
  }
  
}
