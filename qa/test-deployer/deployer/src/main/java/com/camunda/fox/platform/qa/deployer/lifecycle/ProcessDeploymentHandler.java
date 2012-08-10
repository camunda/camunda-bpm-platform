
package com.camunda.fox.platform.qa.deployer.lifecycle;

import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import com.camunda.fox.platform.qa.deployer.event.DeployProcessDefinitions;
import com.camunda.fox.platform.qa.deployer.event.UndeployProcessDefinitions;
import com.camunda.fox.platform.qa.deployer.fox.TestProcessDeployment;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class ProcessDeploymentHandler {
  
  private static final Logger logger = Logger.getLogger(ProcessDeploymentHandler.class.getName());
  
  @Inject
  private Instance<TestProcessDeployment> testProcessDeployment;
  
  public void beforeSuite(@Observes DeployProcessDefinitions event) {
    testProcessDeployment.get().deploy();
  }
  
  public void afterSuite(@Observes UndeployProcessDefinitions event) {
     testProcessDeployment.get().undeploy();
  }
}
