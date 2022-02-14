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
package org.camunda.bpm.engine.spring.test;

import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

public class Spring5TestHelper implements SpringTestHelper {

  @Override
  public void beforeTestClass(TestContextManager testContextManager) {
    testContextManager.registerTestExecutionListeners(new TestExecutionListener() {

      @Override
      public void prepareTestInstance(TestContext testContext) throws Exception {
      }

      @Override
      public void beforeTestMethod(TestContext testContext) throws Exception {
      }

      @Override
      public void beforeTestClass(TestContext testContext) throws Exception {
      }

      @Override
      public void afterTestMethod(TestContext testContext) throws Exception {
      }

      @Override
      public void afterTestClass(TestContext testContext) throws Exception {
        testContext.markApplicationContextDirty(HierarchyMode.EXHAUSTIVE);
      }
    });
  }
}
