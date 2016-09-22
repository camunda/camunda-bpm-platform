/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.spring.test.application;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;

import java.util.concurrent.Callable;

/**
 * @author Daniel Meyer
 *
 */
public class PostDeployRegistrationPa extends SpringProcessApplication {

  protected boolean isPostDeployInvoked = false;
  protected boolean isPreUndeployInvoked = false;
  protected String deploymentId;

  @PostDeploy
  public void registerProcessApplication(ProcessEngine processEngine) {

    // lookup existing deployment
    ProcessDefinition processDefinition = processEngine.getRepositoryService()
      .createProcessDefinitionQuery()
      .processDefinitionKey("startToEnd")
      .latestVersion()
      .singleResult();

    deploymentId = processDefinition.getDeploymentId();

    // register with the process engine
    processEngine.getManagementService()
      .registerProcessApplication(deploymentId, getReference());


    isPostDeployInvoked = true;
  }

  @PreUndeploy
  public void unregisterProcessApplication(ProcessEngine processEngine) {

    // unregister with the process engine
    processEngine.getManagementService()
      .unregisterProcessApplication(deploymentId, true);

    isPreUndeployInvoked = true;

  }


  // customization of Process Application for unit test ////////////////////////////

  protected boolean isInvoked = false;

  @Override
  public void start() {
    // do not auto-deploy the process application : we want to manually deploy from the testcase
  }

  /** override execute to intercept calls from process engine and record that we are invoked. */
  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
    T result = super.execute(callable);
    isInvoked = true;
    return result;
  }

  public boolean isInvoked() {
    return isInvoked;
  }

  public boolean isPostDeployInvoked() {
    return isPostDeployInvoked;
  }

  public boolean isPreUndeployInvoked() {
    return isPreUndeployInvoked;
  }

}
