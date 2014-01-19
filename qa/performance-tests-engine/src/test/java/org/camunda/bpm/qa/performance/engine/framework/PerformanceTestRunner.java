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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daniel Meyer
 *
 */
public class PerformanceTestRunner {

  protected ExecutorService executor;
  protected PerformanceTest test;
  protected PerformanceTestConfiguration configuration;

  // runner state
  protected AtomicLong completedRuns;
  protected PerformanceTestResults results;
  protected Object doneMonitor;
  protected boolean isDone;
  protected Throwable exception;

  protected long startTime;

  public PerformanceTestRunner(PerformanceTest test, PerformanceTestConfiguration configuration) {
    this.test = test;
    this.configuration = configuration;
    init();
  }

  protected void init() {
    executor = Executors.newFixedThreadPool(configuration.getNumberOfThreads());

    results = new PerformanceTestResults(configuration);

    completedRuns = new AtomicLong();
    doneMonitor = new Object();
    isDone = false;
  }

  public Future<PerformanceTestResults> execute() {

    PerformanceTestStep firstStep = test.getFirstStep();

    this.startTime = System.currentTimeMillis();

    for (int i = 0; i < configuration.getNumberOfRuns(); i++) {
      executor.execute(new PerformanceTestRun(this, firstStep));
    }

    return new Future<PerformanceTestResults>() {

      public boolean isDone() {
        synchronized (doneMonitor) {
          return isDone;
        }
      }

      public boolean isCancelled() {
        throw new UnsupportedOperationException("Cannot cancel a performance test.");
      }

      public PerformanceTestResults get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized (doneMonitor) {
          if(!isDone) {
            doneMonitor.wait(unit.convert(timeout, TimeUnit.MILLISECONDS));
            if(exception != null || !isDone) {
              throw new ExecutionException(exception);
            }
           }
        }
        return results;
      }

      public PerformanceTestResults get() throws InterruptedException, ExecutionException {
        synchronized (doneMonitor) {
          if(!isDone) {
            doneMonitor.wait();
            if(exception != null || !isDone) {
              throw new ExecutionException(exception);
            }
          }
        }
        return results;
      }

      public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Cannot cancel a performance test.");
      }
    };
  }

  /**
   * Invoked when a {@link PerformanceTestRun} completed a step
   *
   * @param run the current Run
   * @param currentStep the completed step
   */
  public void completedStep(PerformanceTestRun run, PerformanceTestStep currentStep) {

    PerformanceTestStep nextStep = currentStep.getNextStep();

    if(nextStep != null) {
      // if test has more steps, execute the next step
      run.setCurrentStep(nextStep);
      executor.execute(run);

    } else {
      // performance test run is completed
      completedRun(run);
    }
  }

  /**
   * Invoked when a {@link PerformanceTestRun} is completed.
   * @param run the completed run
   */
  public void completedRun(PerformanceTestRun run) {
    run.endRun();

    long currentlyCompleted = completedRuns.incrementAndGet();
    if(currentlyCompleted >= configuration.getNumberOfRuns()) {
      synchronized (doneMonitor) {

        results.setDuration(System.currentTimeMillis() - startTime);

        executor.shutdownNow();
        isDone = true;
        doneMonitor.notifyAll();
      }
    }
  }

  /**
   * @param performanceTestRun
   * @param t
   */
  public void failed(PerformanceTestRun performanceTestRun, Throwable t) {
    synchronized (doneMonitor) {
      this.exception = t;
      doneMonitor.notifyAll();
    }
  }

}
