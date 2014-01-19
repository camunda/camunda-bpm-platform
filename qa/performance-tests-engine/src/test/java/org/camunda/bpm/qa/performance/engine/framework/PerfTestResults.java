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
package org.camunda.bpm.qa.performance.engine.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class PerfTestResults {

  /** the name of the test */
  protected String testName;

  /** the configuration used */
  protected PerfTestConfiguration configuration;

  protected long duration;

  protected List<PerfTestStepResult> stepResults = Collections.synchronizedList(new ArrayList<PerfTestStepResult>());

  public PerfTestResults(PerfTestConfiguration configuration) {
    this.configuration = configuration;
  }

  // getter / setters ////////////////////////////

  public String getTestName() {
    return testName;
  }

  public void setTestName(String testName) {
    this.testName = testName;
  }

  public PerfTestConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(PerfTestConfiguration configuration) {
    this.configuration = configuration;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public List<PerfTestStepResult> getStepResults() {
    return stepResults;
  }

  @Override
  public String toString() {
    return testName + " Completed " + configuration.getNumberOfRuns()
        + " runs using " + configuration.getNumberOfThreads() +" threads. Took " + duration + "ms.";
  }

  /**
   * log a step result. NOTE: this is expensive as it requires synchronization on the stepResultList.
   *
   * @param currentStep
   * @param stepResult
   */
  public void logStepResult(PerfTestStep currentStep, Object stepResult) {
    stepResults.add(new PerfTestStepResult(currentStep.getStepName(), stepResult));
  }

}
