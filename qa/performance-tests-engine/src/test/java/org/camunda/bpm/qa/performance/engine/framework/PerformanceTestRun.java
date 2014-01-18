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

import java.util.HashMap;
import java.util.Map;

/**
 * An individual run of a performance test. Holds all state related to a test run.
 *
 * @author Daniel Meyer
 *
 */
public class PerformanceTestRun implements PerformaceTestRunContext, Runnable {

  protected boolean isStarted;

  protected long runStartTime;
  protected long runEndTime;

  protected long stepStartTime;
  protected long stepEndTime;

  protected PerformanceTestStep currentStep;

  protected PerformanceTestRunner runner;

  protected Map<String, Object> runContext = new HashMap<String, Object>();

  public PerformanceTestRun(PerformanceTestRunner runner, PerformanceTestStep firstStep) {
    this.runner = runner;
    this.currentStep = firstStep;
  }

  public void startRun() {
    runStartTime = System.currentTimeMillis();
    isStarted = true;
  }

  public void endRun() {
    runEndTime = System.currentTimeMillis();
  }

  public void run() {
    try {
      if(!isStarted) {
        startRun();
      }
      StepBehavior stepBehavior = currentStep.getStepBehavior();
      stepBehavior.execute(this);
      runner.completedStep(this, currentStep);
    } catch(Throwable t) {
      runner.failed(this, t);
    }
  }

  public <T> T getVariable(String name) {
    Object var = runContext.get(name);
    if(var == null) {
      return null;
    } else {
      return (T) var;
    }
  }

  public void setVariable(String name, Object value) {
    runContext.put(name, value);
  }

  public void setCurrentStep(PerformanceTestStep currentStep) {
    this.currentStep = currentStep;
  }

  public long getRunStartTime() {
    return runStartTime;
  }

  public long getRunEndTime() {
    return runEndTime;
  }

  public PerformanceTestStep getCurrentStep() {
    return currentStep;
  }

  public PerformanceTestRunner getRunner() {
    return runner;
  }

  public long getStepEndTime() {
    return stepEndTime;
  }

  public long getStepStartTime() {
    return stepStartTime;
  }

}
