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
package org.camunda.bpm.engine.test.api.externaltask;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Parameterized.class)
public class ExternalTaskSupportTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  @Parameters
  public static Collection<Object[]> processResources() {
    return Arrays.asList(new Object[][] {
      {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskSupportTest.businessRuleTask.bpmn20.xml"},
      {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskSupportTest.messageEndEvent.bpmn20.xml"},
      {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskSupportTest.messageIntermediateEvent.bpmn20.xml"},
      {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskSupportTest.sendTask.bpmn20.xml"}
    });
  }

  @Parameter
  public String processDefinitionResource;

  protected String deploymentId;

  @Before
  public void setUp() {
    deploymentId = rule.getRepositoryService()
        .createDeployment()
        .addClasspathResource(processDefinitionResource)
        .deploy()
        .getId();
  }

  @After
  public void tearDown() {
    if (deploymentId != null) {
      rule.getRepositoryService().deleteDeployment(deploymentId, true);
    }
  }

  @Test
  public void testExternalTaskSupport() {
    // given
    ProcessDefinition processDefinition = rule.getRepositoryService().createProcessDefinitionQuery().singleResult();

    // when
    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // then
    List<LockedExternalTask> externalTasks = rule
        .getExternalTaskService()
        .fetchAndLock(1, "aWorker")
        .topic("externalTaskTopic", 5000L)
        .execute();

    Assert.assertEquals(1, externalTasks.size());
    Assert.assertEquals(processInstance.getId(), externalTasks.get(0).getProcessInstanceId());

    // and it is possible to complete the external task successfully and end the process instance
    rule.getExternalTaskService().complete(externalTasks.get(0).getId(), "aWorker");

    Assert.assertEquals(0L, rule.getRuntimeService().createProcessInstanceQuery().count());
  }
}
