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
package org.camunda.bpm.qa.performance.engine;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.performance.engine.framework.PerformanceTestBuilder;
import org.junit.Before;
import org.junit.Rule;

/**
 * <p>Base class for implementing a process engine performance test</p>
 *
 * @author Daniel Meyer
 *
 */
public abstract class ProcessEnginePerformanceTestBase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Rule
  public PerformanceTestConfigurationRule testConfigurationRule = new PerformanceTestConfigurationRule();

  @Rule
  public PerformanceTestResultRecorderRule resultRecorderRule = new PerformanceTestResultRecorderRule();

  protected ProcessEngine engine;

  @Before
  public void setup() {
    engine = processEngineRule.getProcessEngine();
  }

  protected PerformanceTestBuilder perfomanceTest() {
    return new PerformanceTestBuilder(testConfigurationRule.getPerformanceTestConfiguration(), resultRecorderRule);
  }
}
