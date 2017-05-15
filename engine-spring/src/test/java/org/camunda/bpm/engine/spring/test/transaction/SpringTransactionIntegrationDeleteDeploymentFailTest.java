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

package org.camunda.bpm.engine.spring.test.transaction;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.spring.impl.test.SpringProcessEngineTestCase;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Svetlana Dorokhova
 */

@ContextConfiguration("classpath:org/camunda/bpm/engine/spring/test/transaction/SpringTransactionIntegrationDeleteDeploymentFailTest-context.xml")
public class SpringTransactionIntegrationDeleteDeploymentFailTest extends SpringProcessEngineTestCase {

  private String deploymentId;

  @Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        commandContext
          .getDeploymentManager()
          .deleteDeployment(deploymentId, false, false, false);
        return null;
      }
    });


    super.tearDown();
  }

  public void testFailingAfterDeleteDeployment() {
    //given
    final BpmnModelInstance model = Bpmn.createExecutableProcess().startEvent().userTask().endEvent().done();
    deploymentId = processEngine.getRepositoryService().createDeployment().addModelInstance("model.bpmn", model).deploy().getId();

    //when
    // 1. delete deployment
    // 2. it fails in post command interceptor (see FailDeleteDeploymentsPlugin)
    // 3. transaction is rolling back
    // 4. DeleteDeploymentFailListener is called
    try {
      processEngine.getRepositoryService().deleteDeployment(deploymentId);
    } catch (Exception ex) {
      //expected exception
    }

    //then
    // DeleteDeploymentFailListener succeeded to registered deployments back
    assertEquals(1, processEngineConfiguration.getRegisteredDeployments().size());
  }

}
