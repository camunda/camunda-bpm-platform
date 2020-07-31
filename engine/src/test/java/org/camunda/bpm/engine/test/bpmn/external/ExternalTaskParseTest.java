/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.bpmn.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskParseTest extends PluggableProcessEngineTest {

  @Test
  public void testParseExternalTaskWithoutTopic() {
    DeploymentBuilder deploymentBuilder = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/external/ExternalTaskParseTest.testParseExternalTaskWithoutTopic.bpmn20.xml");

    try {
      deploymentBuilder.deploy();
      fail("exception expected");
    } catch (ParseException e) {
      testRule.assertTextPresent("External tasks must specify a 'topic' attribute in the camunda namespace", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().size()).isEqualTo(1);
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("externalTask");
    }
  }

  @Deployment
  @Test
  public void testParseExternalTaskWithExpressionTopic() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("topicName", "testTopicExpression");

    runtimeService.startProcessInstanceByKey("oneExternalTaskWithExpressionTopicProcess", variables);
    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();
    assertThat("testTopicExpression").isEqualTo(task.getTopicName());
  }

  @Deployment
  @Test
  public void testParseExternalTaskWithStringTopic() {
    Map<String, Object> variables = new HashMap<String, Object>();

    runtimeService.startProcessInstanceByKey("oneExternalTaskWithStringTopicProcess", variables);
    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();
    assertThat("testTopicString").isEqualTo(task.getTopicName());
  }
}
