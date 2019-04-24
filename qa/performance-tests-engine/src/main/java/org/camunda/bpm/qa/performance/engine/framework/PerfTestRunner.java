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
package org.camunda.bpm.qa.performance.engine.framework;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.qa.performance.engine.framework.activitylog.ActivityPerfTestWatcher;

/**
 * @author Daniel Meyer, Ingo Richtsmeier
 *
 */
public class PerfTestRunner {

  protected ExecutorService executor;
  protected PerfTest test;
  protected PerfTestConfiguration configuration;

  // global state
  public static PerfTestPass currentPass;
  protected PerfTestResults results;
  protected Object passMonitor;
  protected Object doneMonitor;
  protected boolean isDone;
  protected Throwable exception;
  protected List<PerfTestWatcher> watchers;

  public PerfTestRunner(PerfTest test, PerfTestConfiguration configuration) {
    this.test = test;
    this.configuration = configuration;
    init();
  }

  protected void init() {

    results = new PerfTestResults(configuration);

    doneMonitor = new Object();
    isDone = false;

    // init test watchers
    String testWatchers = configuration.getTestWatchers();
    if(testWatchers != null) {
      watchers = new ArrayList<PerfTestWatcher>();
      String[] watcherClassNames = testWatchers.split(",");
      for (String watcherClassName : watcherClassNames) {
        if(watcherClassName.length() > 0) {
          Object watcher = ReflectUtil.instantiate(watcherClassName);
          if(watcher instanceof PerfTestWatcher) {
            watchers.add((PerfTestWatcher) watcher);
          } else {
            throw new PerfTestException("Test watcher "+watcherClassName+" must implement "+PerfTestWatcher.class.getName());
          }
        }
      }
    }
    // add activity watcher
    if (configuration.getWatchActivities() != null) {
      if (watchers == null) {
        watchers = new ArrayList<PerfTestWatcher>();
      }
      watchers.add(new ActivityPerfTestWatcher(configuration.getWatchActivities()));
    }
    configuration.setStartTime(new Date(System.currentTimeMillis()));
  }

  public Future<PerfTestResults> execute() {

    // run a pass for each number of threads
    new Thread() {
      public void run() {
        for (int i = 1; i <= configuration.getNumberOfThreads(); i++) {
          runPassWithThreadCount(i);
        }

        synchronized (doneMonitor) {
          isDone = true;
          doneMonitor.notifyAll();
        }

      }
    }.start();

    return new Future<PerfTestResults>() {

      public boolean isDone() {
        synchronized (doneMonitor) {
          return isDone;
        }
      }

      public boolean isCancelled() {
        throw new UnsupportedOperationException("Cannot cancel a performance test.");
      }

      public PerfTestResults get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized (doneMonitor) {
          if(!isDone) {
            doneMonitor.wait(unit.convert(timeout, TimeUnit.MILLISECONDS));
            if(exception != null) {
              throw new ExecutionException(exception);
            }
           }
        }
        return results;
      }

      public PerfTestResults get() throws InterruptedException, ExecutionException {
        synchronized (doneMonitor) {
          if(!isDone) {
            doneMonitor.wait();
            if(exception != null) {
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

  public ExecutorService getExecutor() {
    return executor;
  }

  protected void runPassWithThreadCount(int passNumberOfThreads) {

    currentPass = new PerfTestPass(passNumberOfThreads);
    executor = Executors.newFixedThreadPool(passNumberOfThreads);

    // do a GC pause before running the test
    for(int i = 0; i<5; i++) {
      System.gc();
    }

    passMonitor = new Object();

    PerfTestStep firstStep = test.getFirstStep();
    int numberOfRuns = configuration.getNumberOfRuns();

    // first create the runs
    currentPass.createRuns(this, firstStep, numberOfRuns);

    // start the pass
    currentPass.startPass();
    notifyWatchersBeforePass();

    // now execute the runs
    for (PerfTestRun run : currentPass.getRuns().values()) {
      executor.execute(run);
    }

    synchronized (passMonitor) {
      if(!currentPass.isCompleted()) {
        try {
          passMonitor.wait();

          executor.shutdownNow();
          try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
            exception = e;
          }

        } catch (InterruptedException e) {
          throw new PerfTestException("Interrupted wile waiting for pass "+ passNumberOfThreads +" to complete.");
        }
      }
    }
  }

  protected void notifyWatchersBeforePass() {
    if (watchers != null) {
      for (PerfTestWatcher perfTestWatcher : watchers) {
        perfTestWatcher.beforePass(currentPass);
      }
    }
  }

  protected void notifyWatchersAfterPass() {
    if (watchers != null) {
      for (PerfTestWatcher perfTestWatcher : watchers) {
        perfTestWatcher.afterPass(currentPass);
      }
    }
  }

  /**
   * Invoked when a {@link PerfTestRun} completed a step
   *
   * @param run the current Run
   * @param currentStep the completed step
   */
  public void completedStep(PerfTestRun run, PerfTestStep currentStep) {
    PerfTestStep nextStep = currentStep.getNextStep();

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
   * Invoked when a {@link PerfTestRun} is completed.
   * @param run the completed run
   */
  public void completedRun(PerfTestRun run) {
    run.endRun();

    long currentlyCompleted = currentPass.completeRun();
    if(currentlyCompleted >= configuration.getNumberOfRuns()) {
      synchronized (passMonitor) {

        // record the results:
        currentPass.endPass();
        notifyWatchersAfterPass();

        results.getPassResults().add(currentPass.getResult());

        passMonitor.notifyAll();
      }
    }
  }

  public void failed(PerfTestRun perfTestRun, Throwable t) {
    synchronized (doneMonitor) {
      this.exception = t;
      isDone = true;
      synchronized (passMonitor) {
        passMonitor.notifyAll();
      }
      doneMonitor.notifyAll();
    }
  }

  public List<PerfTestWatcher> getWatchers() {
    return watchers;
  }

  public PerfTest getTest() {
    return test;
  }

  public void logStepResult(PerfTestRun perfTestRun, Object stepResult) {
    currentPass.logStepResult(perfTestRun.getCurrentStep(), stepResult);
  }

  public static void signalRun(String runId) {
    final PerfTestRun run = currentPass.getRun(runId);
    if (run.isWaitingForSignal()) {
      // only complete step if the run is already waiting for a signal
      run.getRunner().getExecutor().execute(new Runnable() {
        public void run() {
          run.getRunner().completedStep(run, run.getCurrentStep());
        }
      });
    }
  }

}
