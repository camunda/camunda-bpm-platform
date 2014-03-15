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
import java.util.List;
import java.util.Map;

/**
 * An individual run of a performance test. Holds all state related to a test run.
 *
 * @author Daniel Meyer
 *
 */
public class PerfTestRun implements PerfTestRunContext, Runnable {

  protected boolean isStarted;

  protected long runStartTime;
  protected long runEndTime;

  protected long stepStartTime;
  protected long stepEndTime;

  protected PerfTestStep currentStep;

  protected PerfTestRunner runner;

  protected Map<String, Object> runContext = new HashMap<String, Object>();

  public PerfTestRun(PerfTestRunner runner, PerfTestStep firstStep) {
    this.runner = runner;
    this.currentStep = firstStep;
  }

  public void startRun() {
    runStartTime = System.currentTimeMillis();
    isStarted = true;
    notifyWatchersStartRun();
  }

  public void endRun() {
    runEndTime = System.currentTimeMillis();
    notifyWatchersEndRun();
  }

  public void run() {
    try {
      if(!isStarted) {
        startRun();
      }
      notifyWatchersBeforeStep();

      PerfTestRunContext.currentContext.set(this);

      PerfTestStepBehavior perfTestStepBehavior = currentStep.getStepBehavior();
      perfTestStepBehavior.execute(this);

      notifyWatchersAfterStep();
      runner.completedStep(this, currentStep);

    } catch(Throwable t) {
      runner.failed(this, t);

    } finally {
      PerfTestRunContext.currentContext.remove();

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

  public void setCurrentStep(PerfTestStep currentStep) {
    this.currentStep = currentStep;
  }

  public long getRunStartTime() {
    return runStartTime;
  }

  public long getRunEndTime() {
    return runEndTime;
  }

  public PerfTestStep getCurrentStep() {
    return currentStep;
  }

  public PerfTestRunner getRunner() {
    return runner;
  }

  public long getStepEndTime() {
    return stepEndTime;
  }

  public long getStepStartTime() {
    return stepStartTime;
  }

  protected void notifyWatchersStartRun() {
    List<PerfTestWatcher> watchers = runner.getWatchers();
    if(watchers != null) {
      for (PerfTestWatcher perfTestWatcher : watchers) {
        perfTestWatcher.beforeRun(runner.getTest(), this);
      }
    }
  }

  protected void notifyWatchersEndRun() {
    List<PerfTestWatcher> watchers = runner.getWatchers();
    if(watchers != null) {
      for (PerfTestWatcher perfTestWatcher : watchers) {
        perfTestWatcher.afterRun(runner.getTest(), this);
      }
    }
  }

  protected void notifyWatchersBeforeStep() {
    List<PerfTestWatcher> watchers = runner.getWatchers();
    if(watchers != null) {
      for (PerfTestWatcher perfTestWatcher : watchers) {
        perfTestWatcher.beforeStep(currentStep, this);
      }
    }
  }

  protected void notifyWatchersAfterStep() {
    List<PerfTestWatcher> watchers = runner.getWatchers();
    if(watchers != null) {
      for (PerfTestWatcher perfTestWatcher : watchers) {
        perfTestWatcher.afterStep(currentStep, this);
      }
    }
  }

  public void logStepResult(Object stepResult) {
    runner.logStepResult(this, stepResult);
  }

}
