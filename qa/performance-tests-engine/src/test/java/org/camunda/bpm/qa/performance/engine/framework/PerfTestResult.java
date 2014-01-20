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
public class PerfTestResult {

  protected long duration;

  protected int numberOfThreads;

  protected List<PerfTestStepResult> stepResults = Collections.synchronizedList(new ArrayList<PerfTestStepResult>());

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public List<PerfTestStepResult> getStepResults() {
    return stepResults;
  }

  public int getNumberOfThreads() {
    return numberOfThreads;
  }

  public void setNumberOfThreads(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
  }

  public void setStepResults(List<PerfTestStepResult> stepResults) {
    this.stepResults = stepResults;
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
