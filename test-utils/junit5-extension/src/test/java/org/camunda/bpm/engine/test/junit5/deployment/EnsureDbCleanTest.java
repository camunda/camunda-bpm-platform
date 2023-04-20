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
package org.camunda.bpm.engine.test.junit5.deployment;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

public class EnsureDbCleanTest {

  public static final String SUB_PROCESS = "processes/subProcess.bpmn";

  @BeforeAll
  public static void setup() {
    ClassUnderTest.isEnabled = true;
  }

  @AfterAll
  public static void tearDown() {
    ClassUnderTest.isEnabled = false;
  }

  @Test
  public void shouldFailTestsThatExpectCleanDbWhenDbIsDirty() {
    EngineExecutionResults results = EngineTestKit
      .engine("junit-jupiter")
      .selectors(
          selectMethod(ClassUnderTest.class, "shouldRaiseExceptionIfDbNotClean")
      )
      .execute();

    Events events = results.testEvents();
    events.finished().assertThatEvents().haveExactly(1, event(
        test("shouldRaiseExceptionIfDbNotClean"),
        finishedWithFailure(
            instanceOf(AssertionError.class),
            message(m -> m.contains("Database is not clean"))
          )));
  }

  /*
   * Should not be executed by the regular test suite, but is rather called
   * by the containing class
   */
  @EnabledIf("isEnabled")
  public static class ClassUnderTest {

    private static boolean isEnabled = false;

    public static boolean isEnabled() {
      return isEnabled;
    }

    @RegisterExtension
    ProcessEngineExtension extension = ProcessEngineExtension.builder()
      // fail if DB is dirty after test
      .ensureCleanAfterTest(true)
      .build();

    @Test
    public void shouldRaiseExceptionIfDbNotClean() {
      // when
      extension.getRepositoryService()
          .createDeployment()
          .addClasspathResource(SUB_PROCESS)
          .deploy();
    }
  }
}
