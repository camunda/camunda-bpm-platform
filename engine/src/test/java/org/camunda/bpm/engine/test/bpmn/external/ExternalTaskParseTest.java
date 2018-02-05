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
package org.camunda.bpm.engine.test.bpmn.external;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskParseTest extends PluggableProcessEngineTestCase {

  public void testParseExternalTaskWithoutTopic() {
    DeploymentBuilder deploymentBuilder = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/external/ExternalTaskParseTest.testParseExternalTaskWithoutTopic.bpmn20.xml");

    try {
      deploymentBuilder.deploy();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTextPresent("External tasks must specify a 'topic' attribute in the camunda namespace", e.getMessage());
    }
  }

  @Deployment
  public void testParseExternalTaskWithExpressionTopic() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("topicName", "testTopicExpression");

    runtimeService.startProcessInstanceByKey("oneExternalTaskWithExpressionTopicProcess", variables);
    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();
    assertEquals("testTopicExpression", task.getTopicName());
  }

  @Deployment
  public void testParseExternalTaskWithStringTopic() {
    Map<String, Object> variables = new HashMap<String, Object>();

    runtimeService.startProcessInstanceByKey("oneExternalTaskWithStringTopicProcess", variables);
    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();
    assertEquals("testTopicString", task.getTopicName());
  }
}
