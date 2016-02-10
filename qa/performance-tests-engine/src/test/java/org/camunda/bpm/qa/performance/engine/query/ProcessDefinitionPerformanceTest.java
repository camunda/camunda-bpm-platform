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
package org.camunda.bpm.qa.performance.engine.query;

import static org.camunda.bpm.qa.performance.engine.junit.AuthHelper.withAuthentication;

import java.util.concurrent.Callable;

import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStepBehavior;
import org.camunda.bpm.qa.performance.engine.junit.ProcessEnginePerformanceTestCase;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessDefinitionPerformanceTest extends ProcessEnginePerformanceTestCase {

  @Test
  public void testQuery() {

    performanceTest().step(new PerfTestStepBehavior() {

      public void execute(PerfTestRunContext context) {

        executeQueries();

        withAuthentication(new Callable<Void>() {
          public Void call() throws Exception {

            executeQueries();

            return null;
          }
        }, engine, "someUser");

        withAuthentication(new Callable<Void>() {
          public Void call() throws Exception {

            executeQueries();

            return null;
          }
        }, engine, "someUser", "g1", "g2", "g3", "g4");

        withAuthentication(new Callable<Void>() {
          public Void call() throws Exception {

            executeQueries();

            return null;
          }
        }, engine, "someUser", "g1", "g2", "g3", "g4", "g1", "g2", "g3", "g4");

        String[]groupIds = new String[100];
        for (int i = 0; i < groupIds.length; i++) {
          groupIds[i] = "group-" + i;

        }

        withAuthentication(new Callable<Void>() {
          public Void call() throws Exception {

            executeQueries();

            return null;
          }
        }, engine, "someUser", groupIds);

      }

    })
    .run();
  }

  private void executeQueries() {
    repositoryService.createProcessDefinitionQuery().count();
    repositoryService.createProcessDefinitionQuery().listPage(0, 15);
  }

}
