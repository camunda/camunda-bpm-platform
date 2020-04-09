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
package org.camunda.bpm.engine.test.api.authorization.externaltask;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.junit.Before;
import org.junit.Test;

public class GetTopicNamesAuthorizationTest extends AuthorizationTest {

  protected String instance1Id;
  protected String instance2Id;

  @Before
  public void setUp() throws Exception {
    testRule.deploy(
        "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchMultipleTopics.bpmn20.xml");

    instance1Id = startProcessInstanceByKey("oneExternalTaskProcess").getId();
    instance2Id = startProcessInstanceByKey("parallelExternalTaskProcess").getId();
    super.setUp();
  }

  @Test
  public void testGetTopicNamesWithoutAuthorization() {
    // when
    List<String> result = externalTaskService.getTopicNames();

    // then
    assertEquals(0,result.size());
  }

  @Test
  public void testGetTopicNamesWithReadOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    List<String> result = externalTaskService.getTopicNames();

    // then
    assertEquals(1, result.size());
    assertEquals("externalTaskTopic", result.get(0));
  }

  @Test
  public void testGetTopicNamesWithReadOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    List<String> result = externalTaskService.getTopicNames();

    // then
    assertEquals(4, result.size());
  }

  @Test
  public void testGetTopicNamesWithReadInstanceOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    List<String> result = externalTaskService.getTopicNames();

    // then
    assertEquals(4, result.size());
  }

  @Test
  public void testGetTopicNamesWithReadDefinitionWithMultiple() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);

    // when
    List<String> result = externalTaskService.getTopicNames();

    // then
    assertEquals(1, result.size());
    assertEquals("externalTaskTopic", result.get(0));
  }

  @Test
  public void testGetTopicNamesWithReadInstanceWithMultiple() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    List<String> result = externalTaskService.getTopicNames();

    // then
    assertEquals(4, result.size());
  }
}