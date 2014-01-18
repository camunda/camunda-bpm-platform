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
package org.camunda.bpm.qa.performance.engine.framework.enforcer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.qa.performance.engine.framework.PerformanceTestResults;
import org.junit.Assert;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Daniel Meyer
 *
 */
public class PerformanceTestResultEnforcerRule extends TestWatcher {

  protected PerformanceTestResults results;
  protected ProcessEngine processEngine;

  protected void succeeded(Description description) {

    Enforce enforce = description.getAnnotation(Enforce.class);
    if(enforce != null) {
      int endedProcessInstances = enforce.endedProcessInstnacesPerRun();
      if(endedProcessInstances > 0) {
        long count = processEngine.getHistoryService().createHistoricProcessInstanceQuery().count();
        if(count != (endedProcessInstances*results.getNumberOfRuns())) {
          Assert.fail("Expected "+endedProcessInstances+" ended process instances per run. Got "+results.getNumberOfRuns()+" runs but "+count+" process instances.");
        }
      }

    }
  }

  public void setResults(PerformanceTestResults results) {
    this.results = results;
  }

  /**
   * @param engine
   */
  public void setProcessEngine(ProcessEngine engine) {
    this.processEngine = engine;

  }

}
